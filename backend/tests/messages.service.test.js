"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const messages_service_1 = require("../src/services/messages.service");
const messages_repository_1 = require("../src/repositories/messages.repository");
jest.mock('../src/repositories/messages.repository');
describe('MessagesService', () => {
    let messagesService;
    let mockMessagesRepository;
    beforeEach(() => {
        mockMessagesRepository = new messages_repository_1.MessagesRepository();
        messagesService = new messages_service_1.MessagesService();
        messagesService.messagesRepository = mockMessagesRepository;
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
