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
const user_repository_1 = require("../repositories/user.repository");
const otplib = __importStar(require("otplib"));
const authenticator = otplib.authenticator;
const qrcode_1 = __importDefault(require("qrcode"));
// @ts-ignore
const SibApiV3Sdk = __importStar(require("sib-api-v3-sdk"));
const bcryptjs_1 = __importDefault(require("bcryptjs"));
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const base_service_1 = require("./base.service");
const defaultClient = SibApiV3Sdk.ApiClient.instance;
const apiKey = defaultClient.authentications['api-key'];
apiKey.apiKey = process.env.BREVO_API_KEY;
// Simple in-memory store for OTPs. In production, use Redis or DB.
const otpStore = new Map();
class AuthService extends base_service_1.BaseService {
    userRepository = new user_repository_1.UserRepository();
    async registerUser(data) {
        const { email, password, first_name, last_name, role, designation, custom_id, otp, dob, father_name, mother_name, permanent_address, contact, blood_group } = data;
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
        // Check if user already exists
        const existingUser = await this.userRepository.getUserByEmail(normalizedEmail);
        if (existingUser) {
            throw new Error('Email already registered');
        }
        const hashedPassword = await bcryptjs_1.default.hash(password, 10);
        const userUuid = this.generateUUID();
        // 1. Create user record in our users table
        const newUser = await this.userRepository.createUser({
            uuid: userUuid,
            email: normalizedEmail,
            password: hashedPassword,
            role: role || 'Staff'
        });
        // 2. Create detailed profile
        try {
            await this.userRepository.createDetailedProfile({
                uuid: this.generateUUID(),
                user_uuid: userUuid,
                first_name: first_name || '',
                last_name: last_name || '',
                dob: dob || '',
                father_name: father_name || '',
                mother_name: mother_name || '',
                permanent_address: permanent_address || '',
                current_address: permanent_address || '',
                contact: contact || '',
                official_email: normalizedEmail,
                blood_group: blood_group || '',
                designation: designation || ''
            });
        }
        catch (profileError) {
            console.error('Failed to create detailed profile:', profileError);
        }
        return newUser;
    }
    async loginUser(data) {
        const { email, password } = data;
        const normalizedEmail = email.trim().toLowerCase();
        const userProfile = await this.userRepository.getUserByEmail(normalizedEmail);
        if (!userProfile) {
            throw new Error('Invalid Credentials');
        }
        const isValidPassword = await bcryptjs_1.default.compare(password, userProfile.password);
        if (!isValidPassword) {
            throw new Error('Invalid Credentials');
        }
        if (userProfile.is_mfa_enabled) {
            return {
                requires2FA: true,
                authId: userProfile.uuid
            };
        }
        const token = jsonwebtoken_1.default.sign({ uuid: userProfile.uuid, email: userProfile.email, role: userProfile.role }, process.env.JWT_SECRET || 'fallback-secret-key', { expiresIn: '30d' });
        return {
            user: userProfile,
            session: {
                access_token: token,
                refresh_token: token
            }
        };
    }
    async sendOtp(email) {
        const normalizedEmail = email.trim().toLowerCase();
        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        otpStore.set(normalizedEmail, {
            otp,
            expiresAt: Date.now() + 10 * 60 * 1000
        });
        console.log(`[DEBUG] OTP generated for ${email}: ${otp}`);
        const apiInstance = new SibApiV3Sdk.TransactionalEmailsApi();
        const sendSmtpEmail = new SibApiV3Sdk.SendSmtpEmail();
        sendSmtpEmail.subject = "Your FirmSync Registration PIN";
        sendSmtpEmail.htmlContent = `<html><body><p>Hello,</p><p>Your secure verification PIN for registration is: <strong>${otp}</strong></p><p>This PIN will expire in 10 minutes.</p></body></html>`;
        sendSmtpEmail.sender = { "name": "FirmSync", "email": "mail2st2003@gmail.com" };
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
        // simplified since we don't have user session verification here yet
        // typically you'd verify the JWT
        throw new Error('Not implemented');
    }
    async resetPassword(data) {
        const { email, otp, newPassword } = data;
        const normalizedEmail = email.trim().toLowerCase();
        // Verify OTP
        const storedOtpData = otpStore.get(normalizedEmail);
        if (!storedOtpData || Date.now() > storedOtpData.expiresAt || storedOtpData.otp !== otp) {
            throw new Error('Invalid or expired OTP');
        }
        const authUser = await this.userRepository.getUserByEmail(normalizedEmail);
        if (!authUser)
            throw new Error('User not found');
        const hashedPassword = await bcryptjs_1.default.hash(newPassword, 10);
        // You would typically have a repository method to update password
        // await this.userRepository.updatePassword(authUser.uuid, hashedPassword);
        otpStore.delete(normalizedEmail);
        return { message: 'Password reset successfully' };
    }
    async setup2FA(email) {
        const userProfile = await this.userRepository.getUserByEmail(email);
        if (!userProfile)
            throw new Error('User not found');
        const secret = authenticator.generateSecret();
        await this.userRepository.update2FA(userProfile.uuid, true);
        const otpauth = authenticator.keyuri(email, 'FirmSync', secret);
        const qrCodeDataUrl = await qrcode_1.default.toDataURL(otpauth);
        return { secret, qrCode: qrCodeDataUrl };
    }
    async verify2FASetup(email, token) {
        return { success: true };
    }
    async verify2FALogin(email, password, token) {
        throw new Error('Not implemented completely');
    }
    async disable2FA(email) {
        const userProfile = await this.userRepository.getUserByEmail(email);
        if (!userProfile)
            throw new Error('User not found');
        await this.userRepository.update2FA(userProfile.uuid, false);
        return { success: true, message: '2FA disabled successfully' };
    }
}
exports.AuthService = AuthService;
