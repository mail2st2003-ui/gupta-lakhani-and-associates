import { Router } from 'express';
import { LeavesController } from '../controllers/leaves.controller';

const router = Router();
const leavesController = new LeavesController();

router.get('/', leavesController.getLeaves);
router.get('/user/:userUuid', leavesController.getUserLeaves);
router.post('/', leavesController.createLeave);
router.put('/:id', leavesController.updateLeave);
router.delete('/:id', leavesController.deleteLeave);

export default router;
