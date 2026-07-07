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
        const { data, error } = await supabase_1.supabase
            .from('todo_items')
            .insert([taskData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async updateTask(taskId, updateData) {
        const { data, error } = await supabase_1.supabase
            .from('todo_items')
            .update(updateData)
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
