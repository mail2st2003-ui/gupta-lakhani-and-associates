"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const auth_controller_1 = require("../controllers/auth.controller");
const express_validator_1 = require("express-validator");
const router = (0, express_1.Router)();
const authController = new auth_controller_1.AuthController();
router.post('/register', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required'),
    (0, express_validator_1.body)('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
    (0, express_validator_1.body)('full_name').notEmpty().withMessage('Full name is required'),
    (0, express_validator_1.body)('otp').notEmpty().withMessage('OTP is required')
], authController.register);
router.get('/profile/:userUuid', authController.getUserProfile);
router.put('/profile/:userUuid', authController.updateProfile);
router.put('/profile/:userUuid/photo', authController.updateProfileImage);
router.post('/login', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required'),
    (0, express_validator_1.body)('password').notEmpty().withMessage('Password is required')
], authController.login);
router.post('/send-otp', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required')
], authController.sendOtp);
router.post('/change-password', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required'),
    (0, express_validator_1.body)('currentPassword').notEmpty().withMessage('Current password is required'),
    (0, express_validator_1.body)('newPassword').isLength({ min: 4 }).withMessage('New password must be at least 4 characters')
], authController.changePassword);
router.post('/reset-password', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required'),
    (0, express_validator_1.body)('otp').notEmpty().withMessage('OTP is required'),
    (0, express_validator_1.body)('newPassword').isLength({ min: 6 }).withMessage('New password must be at least 6 characters')
], authController.resetPassword);
router.post('/2fa/setup', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required')
], authController.setup2FA);
router.post('/2fa/verify-setup', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required'),
    (0, express_validator_1.body)('token').notEmpty().withMessage('Token is required')
], authController.verify2FASetup);
router.post('/2fa/login', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required'),
    (0, express_validator_1.body)('password').notEmpty().withMessage('Password is required'),
    (0, express_validator_1.body)('token').notEmpty().withMessage('Token is required')
], authController.verify2FALogin);
router.post('/2fa/disable', [
    (0, express_validator_1.body)('email').isEmail().withMessage('Valid email is required')
], authController.disable2FA);
router.get('/users', authController.getAllUsers);
exports.default = router;
