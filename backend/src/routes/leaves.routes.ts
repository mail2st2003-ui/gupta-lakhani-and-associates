import { Router } from 'express';
import { LeavesController } from '../controllers/leaves.controller';

const router = Router();
const leavesController = new LeavesController();

router.get('/', leavesController.getLeaves);
router.post('/', leavesController.createLeave);
router.put('/:id', leavesController.updateLeave);

export default router;
