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
            const mappedUser = { ...user, name: user.full_name };
            res.status(201).json({ message: 'User registered successfully', user: mappedUser });
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
            if (data.user) {
                data.user = { ...data.user, name: data.user.full_name };
            }
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
}
exports.AuthController = AuthController;
