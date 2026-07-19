import { Request, Response } from 'express';
import { MessagesService } from '../services/messages.service';
import { randomUUID } from 'crypto';

export class MessagesController {
  private messagesService = new MessagesService();

  sendMessage = async (req: Request, res: Response): Promise<void> => {
    try {
      const messageData = req.body;
      const dbData = {
        uuid: messageData.uuid || randomUUID(),
        sender_uuid: messageData.sender_uuid,
        recipient_uuid: messageData.recipient_uuid,
        content: messageData.content,
        is_encrypted: messageData.is_encrypted !== undefined ? messageData.is_encrypted : false,
        is_delivered: false,
        is_seen: false,
        created_at: new Date(messageData.timestamp || Date.now()).toISOString()
      };
      
      const data = await this.messagesService.saveMessage(dbData);
      res.status(201).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  getUserMessages = async (req: Request, res: Response): Promise<void> => {
    try {
      const { userUuid, otherUuid } = req.params;
      const data = await this.messagesService.getMessagesBetween(userUuid as string, otherUuid as string);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  deleteUserMessages = async (req: Request, res: Response): Promise<void> => {
    try {
      const { userUuid, otherUuid } = req.params;
      await this.messagesService.deleteMessagesBetween(userUuid as string, otherUuid as string);
      res.status(200).json({ message: 'Chats deleted successfully' });
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };
}
