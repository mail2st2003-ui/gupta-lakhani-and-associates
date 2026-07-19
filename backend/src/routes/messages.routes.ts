import { Router } from 'express';
import { MessagesController } from '../controllers/messages.controller';

const router = Router();
const messagesController = new MessagesController();

router.post('/', messagesController.sendMessage);
router.get('/:userUuid', messagesController.getAllUserMessages);
router.get('/:userUuid/:otherUuid', messagesController.getUserMessages);
router.delete('/:userUuid/:otherUuid', messagesController.deleteUserMessages);

export default router;
