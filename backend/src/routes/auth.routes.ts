import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { body } from 'express-validator';

const router = Router();
const authController = new AuthController();

router.post('/register', [
  body('email').isEmail().withMessage('Valid email is required'),
  body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
  body('full_name').notEmpty().withMessage('Full name is required'),
  body('otp').notEmpty().withMessage('OTP is required')
], authController.register);

router.put('/profile/:userUuid', authController.updateProfile);
router.put('/profile/:userUuid/photo', authController.updateProfileImage);

router.post('/login', [
  body('email').isEmail().withMessage('Valid email is required'),
  body('password').notEmpty().withMessage('Password is required')
], authController.login);

router.post('/send-otp', [
  body('email').isEmail().withMessage('Valid email is required')
], authController.sendOtp);

router.post('/change-password', [
  body('email').isEmail().withMessage('Valid email is required'),
  body('currentPassword').notEmpty().withMessage('Current password is required'),
  body('newPassword').isLength({ min: 4 }).withMessage('New password must be at least 4 characters')
], authController.changePassword);

router.post('/reset-password', [
  body('email').isEmail().withMessage('Valid email is required'),
  body('otp').notEmpty().withMessage('OTP is required'),
  body('newPassword').isLength({ min: 6 }).withMessage('New password must be at least 6 characters')
], authController.resetPassword);

router.post('/2fa/setup', [
  body('email').isEmail().withMessage('Valid email is required')
], authController.setup2FA);

router.post('/2fa/verify-setup', [
  body('email').isEmail().withMessage('Valid email is required'),
  body('token').notEmpty().withMessage('Token is required')
], authController.verify2FASetup);

router.post('/2fa/login', [
  body('email').isEmail().withMessage('Valid email is required'),
  body('password').notEmpty().withMessage('Password is required'),
  body('token').notEmpty().withMessage('Token is required')
], authController.verify2FALogin);

router.post('/2fa/disable', [
  body('email').isEmail().withMessage('Valid email is required')
], authController.disable2FA);

export default router;
