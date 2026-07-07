"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LeavesService = void 0;
const supabase_1 = require("../config/supabase");
class LeavesService {
    async getLeaves(employeeId) {
        let query = supabase_1.supabase.from('leave_requests').select('*').order('timestamp', { ascending: false });
        if (employeeId) {
            query = query.eq('employee_id', employeeId);
        }
        const { data, error } = await query;
        if (error)
            throw error;
        return data;
    }
    async createLeave(leaveData) {
        const { data, error } = await supabase_1.supabase
            .from('leave_requests')
            .insert([leaveData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async updateLeave(leaveId, updateData) {
        const { data, error } = await supabase_1.supabase
            .from('leave_requests')
            .update(updateData)
            .eq('id', leaveId)
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
}
exports.LeavesService = LeavesService;
