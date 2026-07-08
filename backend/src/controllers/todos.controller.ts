import { Request, Response } from 'express';
import { TodosService } from '../services/todos.service';

export class TodosController {
  private todosService = new TodosService();

  getUserTodos = async (req: Request, res: Response): Promise<void> => {
    try {
      const { userUuid } = req.params;
      const data = await this.todosService.getTodosByUser(userUuid as string);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  createTodo = async (req: Request, res: Response): Promise<void> => {
    try {
      const data = await this.todosService.createTodo(req.body);
      res.status(201).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  updateTodo = async (req: Request, res: Response): Promise<void> => {
    try {
      const { id } = req.params;
      const data = await this.todosService.updateTodo(id as string, req.body);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  deleteTodo = async (req: Request, res: Response): Promise<void> => {
    try {
      const { id } = req.params;
      const data = await this.todosService.deleteTodo(id as string);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };
}
