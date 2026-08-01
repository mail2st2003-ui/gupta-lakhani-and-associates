import { Request, Response } from 'express';
import { TasksService } from '../services/tasks.service';

export class TasksController {
  private tasksService = new TasksService();

  getTasks = async (req: Request, res: Response): Promise<void> => {
    try {
      const { employee_id } = req.query;
      const data = await this.tasksService.getTasks(employee_id as string);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  getTaskById = async (req: Request, res: Response): Promise<void> => {
    try {
      const { id } = req.params;
      const data = await this.tasksService.getTaskById(id as string);
      if (!data) {
        res.status(404).json({ error: 'Task not found' });
        return;
      }
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  createTask = async (req: Request, res: Response): Promise<void> => {
    try {
      const data = await this.tasksService.createTask(req.body);
      res.status(201).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  updateTask = async (req: Request, res: Response): Promise<void> => {
    try {
      const { id } = req.params;
      const data = await this.tasksService.updateTask(id as string, req.body);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  deleteTask = async (req: Request, res: Response): Promise<void> => {
    try {
      const { id } = req.params;
      const data = await this.tasksService.deleteTask(id as string);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };
}