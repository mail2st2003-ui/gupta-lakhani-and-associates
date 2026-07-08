import { MessagesRepository } from '../repositories/messages.repository';

export class MessagesService {
  private messagesRepository = new MessagesRepository();

  async saveMessage(dbData: any) {
    return this.messagesRepository.saveMessage(dbData);
  }

  async getMessagesBetween(userUuid: string, otherUuid: string) {
    return this.messagesRepository.getMessagesBetween(userUuid, otherUuid);
  }
}
