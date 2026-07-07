"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TasksController = void 0;
const tasks_service_1 = require("../services/tasks.service");
class TasksController {
    tasksService = new tasks_service_1.TasksService();
    getTasks = async (req, res) => {
        try {
            const { employee_id, include_personal } = req.query;
            const data = await this.tasksService.getTasks(employee_id, include_personal === 'true' || include_personal === true);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    createTask = async (req, res) => {
        try {
            const data = await this.tasksService.createTask(req.body);
            res.status(201).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    updateTask = async (req, res) => {
        try {
            const { id } = req.params;
            const data = await this.tasksService.updateTask(id, req.body);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
    deleteTask = async (req, res) => {
        try {
            const { id } = req.params;
            const data = await this.tasksService.deleteTask(id);
            res.status(200).json(data);
        }
        catch (error) {
            res.status(400).json({ error: error.message });
        }
    };
}
exports.TasksController = TasksController;
