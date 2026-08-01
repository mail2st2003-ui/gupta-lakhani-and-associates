"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TasksService = void 0;
const crypto_1 = require("crypto");
const tasks_repository_1 = require("../repositories/tasks.repository");
// Exact schema columns of public.tasks table in Supabase
const TASK_DB_COLUMNS = [
    'uuid',
    'title',
    'description',
    'created_by',
    'assigned_to',
    'assigned_by',
    'created_at',
    'assigned_at',
    'status',
    'priority'
];
class TasksService {
    repository = new tasks_repository_1.TasksRepository();
    /**
     * Filters incoming payload to only allow valid public.tasks table columns,
     * preventing PostgreSQL errors on extra client fields (e.g. isSynced, is_completed).
     */
    filterTaskColumns(data) {
        if (!data || typeof data !== 'object')
            return {};
        const sanitized = {};
        for (const col of TASK_DB_COLUMNS) {
            if (data[col] !== undefined) {
                sanitized[col] = data[col];
            }
        }
        return sanitized;
    }
    formatTaskRecord(task) {
        if (!task)
            return null;
        return {
            uuid: task.uuid || (0, crypto_1.randomUUID)(),
            title: task.title || '',
            description: task.description || '',
            created_by: task.created_by || '',
            assigned_to: task.assigned_to || '',
            assigned_by: task.assigned_by || task.created_by || '',
            created_at: task.created_at || new Date().toISOString(),
            assigned_at: task.assigned_at || task.created_at || new Date().toISOString(),
            status: task.status || 'Pending',
            priority: task.priority || 'Medium'
        };
    }
    async getTasks(assignedTo) {
        const rawTasks = await this.repository.getTasks(assignedTo);
        return (rawTasks || []).map((t) => this.formatTaskRecord(t)).filter(Boolean);
    }
    async getTaskById(taskId) {
        const rawTask = await this.repository.getTaskById(taskId);
        if (!rawTask)
            return null;
        return this.formatTaskRecord(rawTask);
    }
    async createTask(taskData) {
        const now = new Date().toISOString();
        const prepared = {
            uuid: taskData.uuid || (0, crypto_1.randomUUID)(),
            created_at: now,
            assigned_at: now,
            status: 'Pending',
            priority: 'Medium',
            ...taskData
        };
        const taskRecord = this.filterTaskColumns(prepared);
        const created = await this.repository.createTask(taskRecord);
        return this.formatTaskRecord(created);
    }
    async updateTask(taskId, updateData) {
        const taskUpdateData = this.filterTaskColumns(updateData);
        const updated = await this.repository.updateTask(taskId, taskUpdateData);
        return this.formatTaskRecord(updated);
    }
    async deleteTask(taskId) {
        return this.repository.deleteTask(taskId);
    }
}
exports.TasksService = TasksService;
