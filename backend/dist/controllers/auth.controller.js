"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthController = void 0;
const auth_service_1 = require("../services/auth.service");
const express_validator_1 = require("express-validator");
class AuthController {
    authService = new auth_service_1.AuthService();
    register = async (req, res) => {
        try {
            const errors = (0, express_validator_1.validationResult)(req);
            if (!errors.isEmpty()) {
                res.status(400).json({ errors: errors.array() });
                return;
            }
            const user = await this.authService.registerUser(req.body);
            res.status(201).json({ message: 'User registered successfully', user });
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    login = async (req, res) => {
        try {
            const errors = (0, express_validator_1.validationResult)(req);
            if (!errors.isEmpty()) {
                res.status(400).json({ errors: errors.array() });
                return;
            }
            const data = await this.authService.loginUser(req.body);
            res.status(200).json(data);
        }
        catch (error) {
            if (error.message === 'Invalid Credentials') {
                res.status(401).json({ error: 'Wrong email or password' });
                return;
            }
            res.status(400).json({ error: error.message });
        }
    };
    sendOtp = async (req, res) => {
        try {
            const errors = (0, express_validator_1.validationResult)(req);
            if (!errors.isEmpty()) {
                res.status(400).json({ errors: errors.array() });
                return;
            }
            const { email } = req.body;
            const data = await this.authService.sendOtp(email);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    changePassword = async (req, res) => {
        try {
            const errors = (0, express_validator_1.validationResult)(req);
            if (!errors.isEmpty()) {
                res.status(400).json({ errors: errors.array() });
                return;
            }
            const data = await this.authService.changePassword(req.body);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    resetPassword = async (req, res) => {
        try {
            const errors = (0, express_validator_1.validationResult)(req);
            if (!errors.isEmpty()) {
                res.status(400).json({ errors: errors.array() });
                return;
            }
            const data = await this.authService.resetPassword(req.body);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    setup2FA = async (req, res) => {
        try {
            const { email } = req.body;
            if (!email) {
                res.status(400).json({ error: 'Email is required' });
                return;
            }
            const data = await this.authService.setup2FA(email);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    verify2FASetup = async (req, res) => {
        try {
            const { email, token } = req.body;
            if (!email || !token) {
                res.status(400).json({ error: 'Email and token are required' });
                return;
            }
            const data = await this.authService.verify2FASetup(email, token);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    verify2FALogin = async (req, res) => {
        try {
            const { email, password, token } = req.body;
            if (!email || !password || !token) {
                res.status(400).json({ error: 'Email, password, and token are required' });
                return;
            }
            const data = await this.authService.verify2FALogin(email, password, token);
            res.status(200).json(data);
        }
        catch (error) {
            if (error.message === 'Invalid 2FA code') {
                res.status(401).json({ error: 'Invalid Google Authenticator code' });
                return;
            }
            res.status(400).json({ error: error.message });
        }
    };
    disable2FA = async (req, res) => {
        try {
            const { email } = req.body;
            if (!email) {
                res.status(400).json({ error: 'Email is required' });
                return;
            }
            const data = await this.authService.disable2FA(email);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
}
exports.AuthController = AuthController;
