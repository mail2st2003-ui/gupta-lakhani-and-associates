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
      
      // Enforce editing only if the leave is in 'Pending' state (optional server-side check, 
      // but usually you fetch it first. Since LeavesService handles update directly, we could just pass it)
      // For a robust check, we fetch the leave first
      const leaves = await this.leavesService.getLeaves();
      const existingLeave = leaves.find((l: any) => l.uuid === id);
      
      if (!existingLeave) {
         res.status(404).json({ error: 'Leave not found' });
         return;
      }
      if (existingLeave.status !== 'Pending') {
         res.status(400).json({ error: 'Only pending leaves can be edited' });
         return;
      }

      const data = await this.leavesService.updateLeave(id as string, req.body);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  deleteLeave = async (req: Request, res: Response): Promise<void> => {
    try {
      const { id } = req.params;
      const data = await this.leavesService.deleteLeave(id as string);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };

  getUserLeaves = async (req: Request, res: Response): Promise<void> => {
    try {
      const { userUuid } = req.params;
      const data = await this.leavesService.getLeaves(userUuid as string);
      res.status(200).json(data);
    } catch (error: any) {
      res.status(400).json({ error: error.message });
    }
  };
}