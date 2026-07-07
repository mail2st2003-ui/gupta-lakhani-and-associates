import { Router } from 'express';
import { TasksController } from '../controllers/tasks.controller';

const router = Router();
const tasksController = new TasksController();

router.get('/', tasksController.getTasks);
router.post('/', tasksController.createTask);
router.put('/:id', tasksController.updateTask);
router.delete('/:id', tasksController.deleteTask);

export default router;
