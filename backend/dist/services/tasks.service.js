"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TasksService = void 0;
const crypto_1 = require("crypto");
const supabase_1 = require("../config/supabase");
class TasksService {
    async getTasks(employeeId) {
        let query = supabase_1.supabase
            .from('tasks')
            .select('*, task_details(*)')
            .order('created_at', { ascending: false });
        if (employeeId) {
            query = query.eq('assigned_to_user_uuid', employeeId);
        }
        const { data, error } = await query;
        if (error)
            throw error;
        return data;
    }
    async createTask(taskData) {
        const isPersonal = taskData.is_personal ?? taskData.isPersonal ?? true;
        if (isPersonal) {
            const todoData = {
                uuid: taskData.uuid || taskData.id,
                user_uuid: taskData.user_uuid || taskData.employeeId,
                title: taskData.title,
                description: taskData.description || '',
                priority: taskData.priority || 'Medium',
                status: taskData.status || 'Pending',
                is_completed: taskData.is_completed ?? taskData.isCompleted ?? false,
                timestamp: taskData.timestamp || Date.now()
            };
            const { data, error } = await supabase_1.supabase
                .from('todos')
                .insert([todoData])
                .select()
                .single();
            if (error)
                throw error;
            return data;
        }
        const taskUuid = taskData.uuid || taskData.id;
        const taskRecord = {
            uuid: taskUuid,
            created_by_user_uuid: taskData.assigned_by || taskData.assignedBy || null,
            assigned_to_user_uuid: taskData.user_uuid || taskData.employeeId
        };
        const { data: createdTask, error: taskError } = await supabase_1.supabase
            .from('tasks')
            .insert([taskRecord])
            .select()
            .single();
        if (taskError)
            throw taskError;
        const detailsRecord = {
            uuid: taskData.details_uuid || (0, crypto_1.randomUUID)(),
            task_uuid: taskUuid,
            title: taskData.title,
            description: taskData.description || '',
            status: taskData.status || 'Pending',
            priority: taskData.priority || 'Medium',
            due_date: taskData.due_date || taskData.timestamp || Date.now()
        };
        const { data: details, error: detailsError } = await supabase_1.supabase
            .from('task_details')
            .insert([detailsRecord])
            .select()
            .single();
        if (detailsError)
            throw detailsError;
        return { ...createdTask, task_details: details };
    }
    async updateTask(taskId, updateData) {
        const dbData = {};
        if (updateData.status !== undefined)
            dbData.status = updateData.status;
        if (updateData.priority !== undefined)
            dbData.priority = updateData.priority;
        if (updateData.title !== undefined)
            dbData.title = updateData.title;
        if (updateData.description !== undefined)
            dbData.description = updateData.description;
        const { data, error } = await supabase_1.supabase
            .from('task_details')
            .update(dbData)
            .eq('task_uuid', taskId)
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async deleteTask(taskId) {
        const { error } = await supabase_1.supabase
            .from('tasks')
            .delete()
            .eq('uuid', taskId);
        if (error)
            throw error;
        return { message: 'Task deleted successfully' };
    }
}
exports.TasksService = TasksService;
