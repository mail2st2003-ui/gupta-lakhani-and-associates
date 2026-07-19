"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LeavesController = void 0;
const leaves_service_1 = require("../services/leaves.service");
class LeavesController {
    leavesService = new leaves_service_1.LeavesService();
    getLeaves = async (req, res) => {
        try {
            const data = await this.leavesService.getLeaves();
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    createLeave = async (req, res) => {
        try {
            const data = await this.leavesService.createLeave(req.body);
            res.status(201).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    updateLeave = async (req, res) => {
        try {
            const { id } = req.params;
            // Enforce editing only if the leave is in 'Pending' state (optional server-side check, 
            // but usually you fetch it first. Since LeavesService handles update directly, we could just pass it)
            // For a robust check, we fetch the leave first
            const leaves = await this.leavesService.getLeaves();
            const existingLeave = leaves.find((l) => l.uuid === id);
            if (!existingLeave) {
                res.status(404).json({ error: 'Leave not found' });
                return;
            }
            if (existingLeave.status !== 'Pending') {
                res.status(400).json({ error: 'Only pending leaves can be edited' });
                return;
            }
            const data = await this.leavesService.updateLeave(id, req.body);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    deleteLeave = async (req, res) => {
        try {
            const { id } = req.params;
            const data = await this.leavesService.deleteLeave(id);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    getUserLeaves = async (req, res) => {
        try {
            const { userUuid } = req.params;
            const data = await this.leavesService.getLeaves(userUuid);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    checkOverlap = async (req, res) => {
        try {
            const { userUuid } = req.params;
            const { start, end, exclude } = req.query;
            const overlap = await this.leavesService.checkOverlap(userUuid, Number(start) || 0, Number(end) || 0, exclude);
            res.status(200).json({ overlap });
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
}
exports.LeavesController = LeavesController;
