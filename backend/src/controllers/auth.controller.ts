import { Request, Response } from 'express';
import { AuthService } from '../services/auth.service';
import { validationResult } from 'express-validator';

export class AuthController {
  private authService = new AuthService();

  register = async (req: Request, res: Response): Promise<void> => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        res.status(400).json({ errors: errors.array() });
        return;
      }

      const user = await this.authService.registerUser(req.body);
      res.status(201).json({ message: 'User registered successfully', user });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  updateProfile = async (req: Request, res: Response): Promise<void> => {
    try {
      const userUuid = String(req.params.userUuid);
      const data = await this.authService.updateProfile(userUuid, req.body);
      res.status(200).json({ message: 'Profile updated successfully', profile: data });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  updateProfileImage = async (req: Request, res: Response): Promise<void> => {
    try {
      const userUuid = String(req.params.userUuid);
      const data = await this.authService.updateProfileImage(userUuid, req.body.profile_image ?? null);
      res.status(200).json({ message: 'Profile image updated successfully', profile: data });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  login = async (req: Request, res: Response): Promise<void> => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        res.status(400).json({ errors: errors.array() });
        return;
      }

      const data = await this.authService.loginUser(req.body);
      res.status(200).json(data);
    } catch (error: any) {
      if (error.message === 'Invalid Credentials') {
        res.status(401).json({ error: 'Wrong email or password' });
        return;
      }
      res.status(400).json({ error: error.message });
    }
  };

  sendOtp = async (req: Request, res: Response): Promise<void> => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        res.status(400).json({ errors: errors.array() });
        return;
      }

      const { email } = req.body;
      const data = await this.authService.sendOtp(email);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  changePassword = async (req: Request, res: Response): Promise<void> => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        res.status(400).json({ errors: errors.array() });
        return;
      }

      const data = await this.authService.changePassword(req.body);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  resetPassword = async (req: Request, res: Response): Promise<void> => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        res.status(400).json({ errors: errors.array() });
        return;
      }

      const data = await this.authService.resetPassword(req.body);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  setup2FA = async (req: Request, res: Response): Promise<void> => {
    try {
      const { email } = req.body;
      if (!email) {
        res.status(400).json({ error: 'Email is required' });
        return;
      }
      const data = await this.authService.setup2FA(email);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  verify2FASetup = async (req: Request, res: Response): Promise<void> => {
    try {
      const { email, token } = req.body;
      if (!email || !token) {
        res.status(400).json({ error: 'Email and token are required' });
        return;
      }
      const data = await this.authService.verify2FASetup(email, token);
      res.status(200).json(data);
    } catch (error: any) {
      if (error.message === 'Invalid 2FA code') {
        res.status(401).json({ error: 'Invalid Google Authenticator code' });
        return;
      }
      res.status(400).json({ error: error.message });
    }
  };

  verify2FALogin = async (req: Request, res: Response): Promise<void> => {
    try {
      const { email, password, token } = req.body;
      if (!email || !password || !token) {
        res.status(400).json({ error: 'Email, password, and token are required' });
        return;
      }
      const data = await this.authService.verify2FALogin(email, password, token);
      res.status(200).json(data);
    } catch (error: any) {
      if (error.message === 'Invalid 2FA code') {
        res.status(401).json({ error: 'Invalid Google Authenticator code' });
        return;
      }
      res.status(400).json({ error: error.message });
    }
  };

  disable2FA = async (req: Request, res: Response): Promise<void> => {
    try {
      const { email } = req.body;
      if (!email) {
        res.status(400).json({ error: 'Email is required' });
        return;
      }
      const data = await this.authService.disable2FA(email);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  getAllUsers = async (req: Request, res: Response): Promise<void> => {
    try {
      const users = await this.authService.getAllUsers();
      res.status(200).json(users);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };
}
