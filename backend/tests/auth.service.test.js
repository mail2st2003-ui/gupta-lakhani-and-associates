"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const auth_service_1 = require("../src/services/auth.service");
const user_repository_1 = require("../src/repositories/user.repository");
const bcryptjs_1 = __importDefault(require("bcryptjs"));
// Mock dependencies
jest.mock('../src/repositories/user.repository');
jest.mock('bcryptjs');
describe('AuthService', () => {
    let authService;
    let mockUserRepository;
    beforeEach(() => {
        mockUserRepository = new user_repository_1.UserRepository();
        authService = new auth_service_1.AuthService();
        // Inject mock repository
        authService.userRepository = mockUserRepository;
        // Clear all mocks before each test
        jest.clearAllMocks();
    });
    describe('loginUser', () => {
        it('should throw an error if user does not exist', async () => {
            mockUserRepository.getUserByEmail.mockResolvedValue(null);
            await expect(authService.loginUser({ email: 'test@example.com', password: 'password' })).rejects.toThrow('Invalid Credentials');
        });
        it('should return token if credentials are valid and 2FA is disabled', async () => {
            const mockUser = { uuid: '1', email: 'test@example.com', password: 'hashedpassword', is_mfa_enabled: false };
            mockUserRepository.getUserByEmail.mockResolvedValue(mockUser);
            bcryptjs_1.default.compare.mockResolvedValue(true);
            const result = await authService.loginUser({ email: 'test@example.com', password: 'password' });
            expect(result).toHaveProperty('session');
            expect(result).toHaveProperty('user');
            expect(result).not.toHaveProperty('requires2FA');
        });
        it('should return requires2FA if credentials are valid and 2FA is enabled', async () => {
            const mockUser = { uuid: '1', email: 'test@example.com', password: 'hashedpassword', is_mfa_enabled: true };
            mockUserRepository.getUserByEmail.mockResolvedValue(mockUser);
            bcryptjs_1.default.compare.mockResolvedValue(true);
            const result = await authService.loginUser({ email: 'test@example.com', password: 'password' });
            expect(result.requires2FA).toBe(true);
            expect(result).toHaveProperty('authId');
        });
    });
});
