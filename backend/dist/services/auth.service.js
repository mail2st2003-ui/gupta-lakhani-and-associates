"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthService = void 0;
const supabase_1 = require("../config/supabase");
const user_repository_1 = require("../repositories/user.repository");
const otplib = __importStar(require("otplib"));
const authenticator = otplib.authenticator;
const qrcode_1 = __importDefault(require("qrcode"));
// @ts-ignore
const SibApiV3Sdk = __importStar(require("sib-api-v3-sdk"));
const defaultClient = SibApiV3Sdk.ApiClient.instance;
const apiKey = defaultClient.authentications['api-key'];
apiKey.apiKey = process.env.BREVO_API_KEY;
// Simple in-memory store for OTPs. In production, use Redis or DB.
const otpStore = new Map();
class AuthService {
    userRepository = new user_repository_1.UserRepository();
    async registerUser(data) {
        const { email, password, full_name, role, department, custom_id, otp, age, dob, fathers_name, mothers_name, address, phone, emergency_contact, doj, blood_group } = data;
        const normalizedEmail = email.trim().toLowerCase();
        // Validate OTP
        const stored = otpStore.get(normalizedEmail);
        if (!stored) {
            throw new Error('No OTP found for this email. Please request a new one.');
        }
        if (stored.expiresAt < Date.now()) {
            otpStore.delete(normalizedEmail);
            throw new Error('OTP has expired. Please request a new one.');
        }
        if (stored.otp !== otp) {
            throw new Error('Invalid OTP.');
        }
        // OTP is valid, proceed and remove it from store
        otpStore.delete(normalizedEmail);
        // Check if user already exists in custom users table
        const existingUser = await this.userRepository.getUserByEmail(email);
        if (existingUser) {
            throw new Error('Email already registered');
        }
        // 1. Create user in Supabase Auth
        let authUserId;
        const { data: authData, error: authError } = await supabase_1.supabase.auth.admin.createUser({
            email,
            password,
            email_confirm: true, // Auto confirm for now
            user_metadata: { full_name, role }
        });
        if (authError) {
            if (authError.message.includes('already registered') || authError.message.includes('already exists')) {
                const { data: listData } = await supabase_1.supabase.auth.admin.listUsers({ page: 1, perPage: 10000 });
                const existingAuth = listData?.users.find(u => u.email?.toLowerCase() === normalizedEmail);
                if (existingAuth) {
                    authUserId = existingAuth.id;
                    await supabase_1.supabase.auth.admin.updateUserById(authUserId, { password, user_metadata: { full_name, role } });
                }
                else {
                    throw new Error(authError.message);
                }
            }
            else {
                throw new Error(authError.message);
            }
        }
        else {
            if (!authData.user)
                throw new Error('User creation failed');
            authUserId = authData.user.id;
        }
        // 2. Create user record in our users table
        const newUser = await this.userRepository.createUser({
            auth_id: authUserId,
            email,
            full_name,
            role: role || 'Staff',
            department,
            custom_id,
            phone
        });
        // 3. Create detailed profile
        try {
            await this.userRepository.createDetailedProfile({
                id: newUser.id,
                age,
                dob,
                fathers_name,
                mothers_name,
                address,
                emergency_contact,
                doj,
                blood_group
            });
        }
        catch (profileError) {
            console.error('Failed to create detailed profile:', profileError);
            // We don't fail the registration if this optional step errors out, but it's logged
        }
        return newUser;
    }
    async loginUser(data) {
        const { email, password } = data;
        // We can use signInWithPassword from the client auth for simplicity
        // But since this is a backend service acting as API, we could do it here
        const { data: authData, error: authError } = await supabase_1.supabase.auth.signInWithPassword({
            email,
            password
        });
        if (authError) {
            if (authError.message.includes('Invalid login credentials')) {
                throw new Error('Invalid Credentials'); // Map to standard format
            }
            throw new Error(authError.message);
        }
        if (!authData.user || !authData.session)
            throw new Error('Login failed');
        // Fetch our user record
        const userProfile = await this.userRepository.getUserByAuthId(authData.user.id);
        if (userProfile.is_2fa_enabled) {
            return {
                requires2FA: true,
                authId: userProfile.auth_id
            };
        }
        return {
            user: userProfile,
            session: authData.session
        };
    }
    async setup2FA(email) {
        const userProfile = await this.userRepository.getUserByEmail(email);
        if (!userProfile)
            throw new Error('User not found');
        const secret = authenticator.generateSecret();
        await this.userRepository.update2FA(userProfile.auth_id, secret, false);
        const otpauth = authenticator.keyuri(email, 'FirmSync', secret);
        const qrCodeDataUrl = await qrcode_1.default.toDataURL(otpauth);
        return {
            secret,
            qrCode: qrCodeDataUrl
        };
    }
    async verify2FASetup(email, token) {
        const userProfile = await this.userRepository.getUserByEmail(email);
        if (!userProfile)
            throw new Error('User not found');
        if (!userProfile.totp_secret)
            throw new Error('2FA not setup');
        const isValid = authenticator.verify({ token, secret: userProfile.totp_secret });
        if (!isValid)
            throw new Error('Invalid 2FA code');
        await this.userRepository.update2FA(userProfile.auth_id, userProfile.totp_secret, true);
        return { success: true };
    }
    async verify2FALogin(email, password, token) {
        const userProfile = await this.userRepository.getUserByEmail(email);
        if (!userProfile || !userProfile.totp_secret)
            throw new Error('User or 2FA not configured');
        const isValid = authenticator.verify({ token, secret: userProfile.totp_secret });
        if (!isValid)
            throw new Error('Invalid 2FA code');
        // Re-authenticate with Supabase to get the session securely
        const { data: authData, error: authError } = await supabase_1.supabase.auth.signInWithPassword({ email, password });
        if (authError || !authData.session)
            throw new Error('Login failed');
        return {
            user: userProfile,
            session: authData.session
        };
    }
    async sendOtp(email) {
        const normalizedEmail = email.trim().toLowerCase();
        // Generate a random 6-digit OTP
        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        // Store it with a 10-minute expiration
        otpStore.set(normalizedEmail, {
            otp,
            expiresAt: Date.now() + 10 * 60 * 1000
        });
        console.log(`[DEBUG] OTP generated for ${email}: ${otp}`);
        // Send email using Brevo Transactional API
        const apiInstance = new SibApiV3Sdk.TransactionalEmailsApi();
        const sendSmtpEmail = new SibApiV3Sdk.SendSmtpEmail();
        sendSmtpEmail.subject = "Your FirmSync Registration PIN";
        sendSmtpEmail.htmlContent = `<html><body><p>Hello,</p><p>Your secure verification PIN for registration is: <strong>${otp}</strong></p><p>This PIN will expire in 10 minutes.</p></body></html>`;
        sendSmtpEmail.sender = { "name": "sanya tiwari", "email": "mail2st2003@gmail.com" };
        sendSmtpEmail.to = [{ "email": email }];
        try {
            await apiInstance.sendTransacEmail(sendSmtpEmail);
            return { message: "OTP sent successfully" };
        }
        catch (error) {
            console.error('Brevo API Error:', error);
            throw new Error('Failed to send OTP email');
        }
    }
    async changePassword(data) {
        const { email, currentPassword, newPassword } = data;
        // Verify current password
        const { data: authData, error: authError } = await supabase_1.supabase.auth.signInWithPassword({
            email,
            password: currentPassword
        });
        if (authError || !authData.user) {
            throw new Error('Invalid current password');
        }
        // Update password using Admin API
        const { error: updateError } = await supabase_1.supabase.auth.admin.updateUserById(authData.user.id, {
            password: newPassword
        });
        if (updateError) {
            throw new Error('Failed to update password');
        }
        return { message: 'Password updated successfully' };
    }
    async resetPassword(data) {
        const { email, otp, newPassword } = data;
        const normalizedEmail = email.trim().toLowerCase();
        // Verify OTP
        const storedOtpData = otpStore.get(normalizedEmail);
        if (!storedOtpData) {
            throw new Error('Invalid or expired OTP');
        }
        if (Date.now() > storedOtpData.expiresAt) {
            otpStore.delete(normalizedEmail);
            throw new Error('OTP has expired');
        }
        if (storedOtpData.otp !== otp) {
            throw new Error('Invalid OTP');
        }
        // Get user by email to get Auth ID
        const { data: users, error: userError } = await supabase_1.supabase.auth.admin.listUsers({
            page: 1,
            perPage: 10000
        });
        if (userError) {
            throw new Error('Failed to retrieve user data');
        }
        const authUser = users.users.find(u => u.email?.toLowerCase() === normalizedEmail);
        if (!authUser) {
            throw new Error('User not found');
        }
        // Update password using Admin API
        const { error: updateError } = await supabase_1.supabase.auth.admin.updateUserById(authUser.id, {
            password: newPassword
        });
        if (updateError) {
            throw new Error('Failed to reset password');
        }
        // Clear OTP after successful use
        otpStore.delete(normalizedEmail);
        return { message: 'Password reset successfully' };
    }
}
exports.AuthService = AuthService;
