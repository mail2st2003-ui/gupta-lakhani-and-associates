"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LeavesController = void 0;
const leaves_service_1 = require("../services/leaves.service");
class LeavesController {
    leavesService = new leaves_service_1.LeavesService();
    getLeaves = async (req, res) => {
        try {
            const { employee_id } = req.query;
            const data = await this.leavesService.getLeaves(employee_id);
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
            const data = await this.leavesService.updateLeave(id, req.body);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
}
exports.LeavesController = LeavesController;
