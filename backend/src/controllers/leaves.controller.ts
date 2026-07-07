import { Request, Response } from 'express';
import { LeavesService } from '../services/leaves.service';

export class LeavesController {
  private leavesService = new LeavesService();

  getLeaves = async (req: Request, res: Response): Promise<void> => {
    try {
      const data = await this.leavesService.getLeaves();
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  createLeave = async (req: Request, res: Response): Promise<void> => {
    try {
      const data = await this.leavesService.createLeave(req.body);
      res.status(201).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  updateLeave = async (req: Request, res: Response): Promise<void> => {
    try {
      const { id } = req.params;
      const data = await this.leavesService.updateLeave(id as string, req.body);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };
}