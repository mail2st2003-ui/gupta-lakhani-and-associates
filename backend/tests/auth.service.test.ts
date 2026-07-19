import { AuthService } from '../src/services/auth.service';
import { UserRepository } from '../src/repositories/user.repository';
import bcrypt from 'bcryptjs';

// Mock dependencies
jest.mock('../src/repositories/user.repository');
jest.mock('bcryptjs');

describe('AuthService', () => {
  let authService: AuthService;
  let mockUserRepository: jest.Mocked<UserRepository>;

  beforeEach(() => {
    mockUserRepository = new UserRepository() as jest.Mocked<UserRepository>;
    authService = new AuthService();
    // Inject mock repository
    (authService as any).userRepository = mockUserRepository;
    
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
      (bcrypt.compare as jest.Mock).mockResolvedValue(true);

      const result = await authService.loginUser({ email: 'test@example.com', password: 'password' });
      
      expect(result).toHaveProperty('session');
      expect(result).toHaveProperty('user');
      expect(result).not.toHaveProperty('requires2FA');
    });

    it('should return requires2FA if credentials are valid and 2FA is enabled', async () => {
      const mockUser = { uuid: '1', email: 'test@example.com', password: 'hashedpassword', is_mfa_enabled: true };
      mockUserRepository.getUserByEmail.mockResolvedValue(mockUser);
      (bcrypt.compare as jest.Mock).mockResolvedValue(true);

      const result = await authService.loginUser({ email: 'test@example.com', password: 'password' });
      
      expect(result.requires2FA).toBe(true);
      expect(result).toHaveProperty('authId');
    });
  });
});
