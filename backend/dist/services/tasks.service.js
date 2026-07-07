"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TasksService = void 0;
const supabase_1 = require("../config/supabase");
class TasksService {
    async getTasks(employeeId) {
        let query = supabase_1.supabase.from('todo_items').select('*').order('timestamp', { ascending: false });
        if (employeeId) {
            query = query.eq('employee_id', employeeId);
        }
        const { data, error } = await query;
        if (error)
            throw error;
        return data;
    }
    async createTask(taskData) {
        const dbData = {
            id: taskData.id,
            employee_id: taskData.employeeId,
            title: taskData.title,
            description: taskData.description,
            priority: taskData.priority,
            status: taskData.status,
            is_completed: taskData.isCompleted,
            is_approved: taskData.isApproved,
            timestamp: taskData.timestamp,
            is_personal: taskData.isPersonal,
            assigned_by: taskData.assignedBy
        };
        const { data, error } = await supabase_1.supabase
            .from('todo_items')
            .insert([dbData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async updateTask(taskId, updateData) {
        const dbData = {};
        if (updateData.title !== undefined)
            dbData.title = updateData.title;
        if (updateData.description !== undefined)
            dbData.description = updateData.description;
        if (updateData.priority !== undefined)
            dbData.priority = updateData.priority;
        if (updateData.status !== undefined)
            dbData.status = updateData.status;
        if (updateData.isCompleted !== undefined)
            dbData.is_completed = updateData.isCompleted;
        if (updateData.isApproved !== undefined)
            dbData.is_approved = updateData.isApproved;
        if (updateData.isPersonal !== undefined)
            dbData.is_personal = updateData.isPersonal;
        if (updateData.assignedBy !== undefined)
            dbData.assigned_by = updateData.assignedBy;
        const { data, error } = await supabase_1.supabase
            .from('todo_items')
            .update(dbData)
            .eq('id', taskId)
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async deleteTask(taskId) {
        const { error } = await supabase_1.supabase
            .from('todo_items')
            .delete()
            .eq('id', taskId);
        if (error)
            throw error;
        return { message: 'Task deleted successfully' };
    }
}
exports.TasksService = TasksService;
