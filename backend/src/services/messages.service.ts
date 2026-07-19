import { MessagesRepository } from '../repositories/messages.repository';

export class MessagesService {
  private messagesRepository = new MessagesRepository();

  async saveMessage(dbData: any) {
    return this.messagesRepository.saveMessage(dbData);
  }

  async getMessagesBetween(userUuid: string, otherUuid: string) {
    return this.messagesRepository.getMessagesBetween(userUuid, otherUuid);
  }

  async deleteMessagesBetween(userUuid: string, otherUuid: string) {
    return this.messagesRepository.deleteMessagesBetween(userUuid, otherUuid);
  }

  async getAllUserMessages(userUuid: string) {
    return this.messagesRepository.getAllUserMessages(userUuid);
  }
}
