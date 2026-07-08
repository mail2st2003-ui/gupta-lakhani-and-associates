import { Router } from 'express';
import { TodosController } from '../controllers/todos.controller';

const router = Router();
const todosController = new TodosController();

router.get('/user/:userUuid', todosController.getUserTodos);
router.post('/', todosController.createTodo);
router.put('/:id', todosController.updateTodo);
router.delete('/:id', todosController.deleteTodo);

export default router;
