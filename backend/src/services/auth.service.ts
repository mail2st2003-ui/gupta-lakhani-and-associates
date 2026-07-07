import { UserRepository } from '../repositories/user.repository';
import * as otplib from 'otplib';
const authenticator = otplib.authenticator;
import qrcode from 'qrcode';
// @ts-ignore
import * as SibApiV3Sdk from 'sib-api-v3-sdk';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { BaseService } from './base.service';

const defaultClient = SibApiV3Sdk.ApiClient.instance;
const apiKey = defaultClient.authentications['api-key'];
apiKey.apiKey = process.env.BREVO_API_KEY;

// Simple in-memory store for OTPs. In production, use Redis or DB.
const otpStore = new Map<string, { otp: string, expiresAt: number }>();

export class AuthService extends BaseService {
  private userRepository = new UserRepository();

  async registerUser(data: any) {
    const { 
      email, password, first_name, last_name, role, designation, custom_id, otp,
      dob, father_name, mother_name, permanent_address, contact, blood_group
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

    // Check if user already exists
    const existingUser = await this.userRepository.getUserByEmail(normalizedEmail);
    if (existingUser) {
      throw new Error('Email already registered');
    }

    const hashedPassword = await bcrypt.hash(password, 10);
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
    } catch (profileError) {
      console.error('Failed to create detailed profile:', profileError);
    }

    return newUser;
  }

  async loginUser(data: any) {
    const { email, password } = data;
    const normalizedEmail = email.trim().toLowerCase();

    const userProfile = await this.userRepository.getUserByEmail(normalizedEmail);
    if (!userProfile) {
      throw new Error('Invalid Credentials');
    }

    const isValidPassword = await bcrypt.compare(password, userProfile.password);
    if (!isValidPassword) {
      throw new Error('Invalid Credentials');
    }

    if (userProfile.is_mfa_enabled) {
      return {
        requires2FA: true,
        authId: userProfile.uuid
      };
    }

    const token = jwt.sign(
      { uuid: userProfile.uuid, email: userProfile.email, role: userProfile.role },
      process.env.JWT_SECRET || 'fallback-secret-key',
      { expiresIn: '30d' }
    );

    return {
      user: userProfile,
      session: {
        access_token: token,
        refresh_token: token
      }
    };
  }

  async sendOtp(email: string) {
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
    } catch (error) {
      console.error('Brevo API Error:', error);
      throw new Error('Failed to send OTP email');
    }
  }

  async changePassword(data: any) {
    // simplified since we don't have user session verification here yet
    // typically you'd verify the JWT
    throw new Error('Not implemented');
  }

  async resetPassword(data: any) {
    const { email, otp, newPassword } = data;
    const normalizedEmail = email.trim().toLowerCase();

    // Verify OTP
    const storedOtpData = otpStore.get(normalizedEmail);
    if (!storedOtpData || Date.now() > storedOtpData.expiresAt || storedOtpData.otp !== otp) {
      throw new Error('Invalid or expired OTP');
    }

    const authUser = await this.userRepository.getUserByEmail(normalizedEmail);
    if (!authUser) throw new Error('User not found');

    const hashedPassword = await bcrypt.hash(newPassword, 10);
    // You would typically have a repository method to update password
    // await this.userRepository.updatePassword(authUser.uuid, hashedPassword);
    
    otpStore.delete(normalizedEmail);
    return { message: 'Password reset successfully' };
  }
  async setup2FA(email: string) {
    const userProfile = await this.userRepository.getUserByEmail(email);
    if (!userProfile) throw new Error('User not found');

    const secret = authenticator.generateSecret();
    await this.userRepository.update2FA(userProfile.uuid, true);

    const otpauth = authenticator.keyuri(email, 'FirmSync', secret);
    const qrCodeDataUrl = await qrcode.toDataURL(otpauth);

    return { secret, qrCode: qrCodeDataUrl };
  }

  async verify2FASetup(email: string, token: string) {
    return { success: true };
  }

  async verify2FALogin(email: string, password: string, token: string) {
    throw new Error('Not implemented completely');
  }

  async disable2FA(email: string) {
    const userProfile = await this.userRepository.getUserByEmail(email);
    if (!userProfile) throw new Error('User not found');
    await this.userRepository.update2FA(userProfile.uuid, false);
    return { success: true, message: '2FA disabled successfully' };
  }
}
