import { supabase } from '../config/supabase';
import { UserRepository } from '../repositories/user.repository';
// @ts-ignore
import * as SibApiV3Sdk from 'sib-api-v3-sdk';

const defaultClient = SibApiV3Sdk.ApiClient.instance;
const apiKey = defaultClient.authentications['api-key'];
apiKey.apiKey = process.env.BREVO_API_KEY;

// Simple in-memory store for OTPs. In production, use Redis or DB.
const otpStore = new Map<string, { otp: string, expiresAt: number }>();

export class AuthService {
  private userRepository = new UserRepository();

  async registerUser(data: any) {
    const { 
      email, password, full_name, role, department, custom_id, otp,
      age, dob, fathers_name, mothers_name, address, phone, emergency_contact, doj, blood_group
    } = data;

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
    const { data: authData, error: authError } = await supabase.auth.admin.createUser({
      email,
      password,
      email_confirm: true, // Auto confirm for now
      user_metadata: { full_name, role }
    });

    if (authError) {
      if (authError.message.includes('already registered') || authError.message.includes('already exists')) {
        const { data: listData } = await supabase.auth.admin.listUsers({ page: 1, perPage: 10000 });
        const existingAuth = listData?.users.find(u => u.email?.toLowerCase() === normalizedEmail);
        if (existingAuth) {
           authUserId = existingAuth.id;
           await supabase.auth.admin.updateUserById(authUserId, { password, user_metadata: { full_name, role } });
        } else {
           throw new Error(authError.message);
        }
      } else {
        throw new Error(authError.message);
      }
    } else {
      if (!authData.user) throw new Error('User creation failed');
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
    } catch (profileError) {
      console.error('Failed to create detailed profile:', profileError);
      // We don't fail the registration if this optional step errors out, but it's logged
    }

    return newUser;
  }

  async loginUser(data: any) {
    const { email, password } = data;

    // We can use signInWithPassword from the client auth for simplicity
    // But since this is a backend service acting as API, we could do it here
    const { data: authData, error: authError } = await supabase.auth.signInWithPassword({
      email,
      password
    });

    if (authError) {
      if (authError.message.includes('Invalid login credentials')) {
         throw new Error('Invalid Credentials'); // Map to standard format
      }
      throw new Error(authError.message);
    }

    if (!authData.user || !authData.session) throw new Error('Login failed');

    // Fetch our user record
    const userProfile = await this.userRepository.getUserByAuthId(authData.user.id);

    return {
      user: userProfile,
      session: authData.session
    };
  }

  async sendOtp(email: string) {
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
    } catch (error) {
      console.error('Brevo API Error:', error);
      throw new Error('Failed to send OTP email');
    }
  }

  async changePassword(data: any) {
    const { email, currentPassword, newPassword } = data;

    // Verify current password
    const { data: authData, error: authError } = await supabase.auth.signInWithPassword({
      email,
      password: currentPassword
    });

    if (authError || !authData.user) {
      throw new Error('Invalid current password');
    }

    // Update password using Admin API
    const { error: updateError } = await supabase.auth.admin.updateUserById(authData.user.id, {
      password: newPassword
    });

    if (updateError) {
      throw new Error('Failed to update password');
    }

    return { message: 'Password updated successfully' };
  }
}
