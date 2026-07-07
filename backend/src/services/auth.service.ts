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
const pending2FASecrets = new Map<string, { secret: string, expiresAt: number }>();

export class AuthService extends BaseService {
  private userRepository = new UserRepository();

  async registerUser(data: any) {
    const { 
      email, password, full_name, first_name, middle_name, last_name, role, designation, department, custom_id, otp,
      dob, father_name, fathers_name, mother_name, mothers_name, permanent_address, address, current_address, contact, phone, emergency_contact, blood_group
    } = data;

    const normalizedEmail = email.trim().toLowerCase();
    const nameParts = String(full_name || '').trim().split(/\s+/).filter(Boolean);
    const resolvedFirstName = first_name || nameParts[0] || '';
    const resolvedLastName = last_name || (nameParts.length > 1 ? nameParts[nameParts.length - 1] : '');
    const resolvedMiddleName = middle_name || (nameParts.length > 2 ? nameParts.slice(1, -1).join(' ') : '');
    const resolvedFatherName = father_name || fathers_name || '';
    const resolvedMotherName = mother_name || mothers_name || '';
    const resolvedPermanentAddress = permanent_address || address || '';
    const resolvedCurrentAddress = current_address || resolvedPermanentAddress;
    const resolvedContact = contact || phone || '';
    const resolvedDesignation = designation || department || '';

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
        first_name: resolvedFirstName,
        middle_name: resolvedMiddleName,
        last_name: resolvedLastName,
        dob: dob || '',
        father_name: resolvedFatherName,
        mother_name: resolvedMotherName,
        permanent_address: resolvedPermanentAddress,
        current_address: resolvedCurrentAddress,
        contact: resolvedContact,
        official_email: normalizedEmail,
        personal_email: normalizedEmail,
        blood_group: blood_group || '',
        designation: resolvedDesignation
      });
    } catch (profileError) {
      console.error('Failed to create detailed profile:', profileError);
    }

    return newUser;
  }

  async updateProfile(userUuid: string, profileData: any) {
    return this.userRepository.upsertDetailedProfile({
      uuid: profileData.uuid || this.generateUUID(),
      user_uuid: userUuid,
      first_name: profileData.first_name || '',
      middle_name: profileData.middle_name || '',
      last_name: profileData.last_name || '',
      father_name: profileData.father_name || '',
      mother_name: profileData.mother_name || '',
      dob: profileData.dob || '',
      gender: profileData.gender || '',
      blood_group: profileData.blood_group || '',
      contact: profileData.contact || '',
      official_email: profileData.official_email || '',
      personal_email: profileData.personal_email || '',
      permanent_address: profileData.permanent_address || '',
      current_address: profileData.current_address || '',
      profile_image: profileData.profile_image || null,
      designation: profileData.designation || ''
    });
  }

  async updateProfileImage(userUuid: string, profileImage: string | null) {
    return this.userRepository.updateProfileImage(userUuid, profileImage);
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
    const normalizedEmail = email.trim().toLowerCase();
    const userProfile = await this.userRepository.getUserByEmail(normalizedEmail);
    if (!userProfile) throw new Error('User not found');

    const secret = authenticator.generateSecret();
    pending2FASecrets.set(normalizedEmail, {
      secret,
      expiresAt: Date.now() + 10 * 60 * 1000
    });

    const otpauth = authenticator.keyuri(normalizedEmail, 'FirmSync', secret);
    const qrCodeDataUrl = await qrcode.toDataURL(otpauth);

    return { secret, qrCode: qrCodeDataUrl };
  }

  async verify2FASetup(email: string, token: string) {
    const normalizedEmail = email.trim().toLowerCase();
    const pending = pending2FASecrets.get(normalizedEmail);
    if (!pending || pending.expiresAt < Date.now()) {
      pending2FASecrets.delete(normalizedEmail);
      throw new Error('2FA setup expired. Please start again.');
    }

    const isValid = authenticator.check(token, pending.secret);
    if (!isValid) throw new Error('Invalid 2FA code');

    const userProfile = await this.userRepository.getUserByEmail(normalizedEmail);
    if (!userProfile) throw new Error('User not found');

    await this.userRepository.update2FA(userProfile.uuid, true, pending.secret);
    pending2FASecrets.delete(normalizedEmail);
    return { success: true };
  }

  async verify2FALogin(email: string, password: string, token: string) {
    const normalizedEmail = email.trim().toLowerCase();
    const userProfile = await this.userRepository.getUserByEmail(normalizedEmail);
    if (!userProfile) throw new Error('Invalid Credentials');

    const isValidPassword = await bcrypt.compare(password, userProfile.password);
    if (!isValidPassword) throw new Error('Invalid Credentials');

    if (!userProfile.is_mfa_enabled) throw new Error('2FA is not enabled');
    if (!userProfile.mfa_secret) throw new Error('2FA secret is missing. Please set up 2FA again.');

    const isValidToken = authenticator.check(token, userProfile.mfa_secret);
    if (!isValidToken) throw new Error('Invalid 2FA code');

    const jwtToken = jwt.sign(
      { uuid: userProfile.uuid, email: userProfile.email, role: userProfile.role },
      process.env.JWT_SECRET || 'fallback-secret-key',
      { expiresIn: '30d' }
    );

    return {
      user: userProfile,
      session: {
        access_token: jwtToken,
        refresh_token: jwtToken
      }
    };
  }

  async disable2FA(email: string) {
    const normalizedEmail = email.trim().toLowerCase();
    const userProfile = await this.userRepository.getUserByEmail(normalizedEmail);
    if (!userProfile) throw new Error('User not found');
    pending2FASecrets.delete(normalizedEmail);
    await this.userRepository.update2FA(userProfile.uuid, false, null);
    return { success: true, message: '2FA disabled successfully' };
  }
}
