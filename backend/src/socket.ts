import { Server as SocketIOServer, Socket } from 'socket.io';
import { Server } from 'http';
import { BaseService } from './services/base.service';
import { MessagesRepository } from './repositories/messages.repository';

class ChatService extends BaseService {
  public generateMessageUUID() {
    return this.generateUUID();
  }
}

export const setupSocketIO = (server: Server) => {
  const io = new SocketIOServer(server, {
    cors: {
      origin: "*",
      methods: ["GET", "POST"]
    }
  });

  const chatService = new ChatService();
  const messagesRepository = new MessagesRepository();
  
  // Mapping user_uuid to socket ID
  const userSockets = new Map<string, string>();

  io.on('connection', (socket: Socket) => {
    console.log(`User connected: ${socket.id}`);

    socket.on('register_user', (userUuid: string) => {
      userSockets.set(userUuid, socket.id);
      console.log(`User ${userUuid} registered to socket ${socket.id}`);
    });

    socket.on('send_message', async (data: { sender_uuid: string, recipient_uuid: string, content: string }) => {
      try {
        const messageUuid = chatService.generateMessageUUID();
        const timestamp = new Date().toISOString();
        
        const message = {
          uuid: messageUuid,
          sender_uuid: data.sender_uuid,
          recipient_uuid: data.recipient_uuid,
          content: data.content,
          is_encrypted: false,
          is_delivered: false,
          is_seen: false,
          created_at: timestamp
        };

        // Persist to DB
        await messagesRepository.saveMessage(message);

        // Send back acknowledgment to sender
        socket.emit('message_sent', message);

        // Forward to recipient if online
        const recipientSocketId = userSockets.get(data.recipient_uuid);
        if (recipientSocketId) {
          io.to(recipientSocketId).emit('receive_message', message);
          
          // Auto-mark delivered if recipient is online
          await messagesRepository.markAsDelivered(messageUuid);
          io.to(socket.id).emit('message_delivered', { message_uuid: messageUuid });
        }
      } catch (error) {
        console.error('Error sending message:', error);
      }
    });

    socket.on('mark_seen', async (data: { message_uuid: string, sender_uuid: string }) => {
      try {
        await messagesRepository.markAsSeen(data.message_uuid);
        
        // Notify original sender that message was seen
        const senderSocketId = userSockets.get(data.sender_uuid);
        if (senderSocketId) {
          io.to(senderSocketId).emit('message_seen', { message_uuid: data.message_uuid });
        }
      } catch (error) {
        console.error('Error marking message as seen:', error);
      }
    });

    socket.on('disconnect', () => {
      console.log(`User disconnected: ${socket.id}`);
      // Remove from map
      for (let [userUuid, socketId] of userSockets.entries()) {
        if (socketId === socket.id) {
          userSockets.delete(userUuid);
          break;
        }
      }
    });
  });

  return io;
};
