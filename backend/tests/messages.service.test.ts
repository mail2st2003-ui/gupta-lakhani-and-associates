import { MessagesService } from '../src/services/messages.service';
import { MessagesRepository } from '../src/repositories/messages.repository';

jest.mock('../src/repositories/messages.repository');

describe('MessagesService', () => {
  let messagesService: MessagesService;
  let mockMessagesRepository: jest.Mocked<MessagesRepository>;

  beforeEach(() => {
    mockMessagesRepository = new MessagesRepository() as jest.Mocked<MessagesRepository>;
    messagesService = new MessagesService();
    (messagesService as any).messagesRepository = mockMessagesRepository;
    
    jest.clearAllMocks();
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  describe('deleteMessagesBetween', () => {
    it('should call repository deleteMessagesBetween with correct user IDs', async () => {
      mockMessagesRepository.deleteMessagesBetween.mockResolvedValue(undefined);

      await messagesService.deleteMessagesBetween('user1', 'user2');

      expect(mockMessagesRepository.deleteMessagesBetween).toHaveBeenCalledWith('user1', 'user2');
    });
  });
});
