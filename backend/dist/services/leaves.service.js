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
        const dbData = {
            id: leaveData.id,
            employee_id: leaveData.employeeId,
            employee_name: leaveData.employeeName,
            employee_role: leaveData.employeeRole,
            recipient_ids: leaveData.recipientIds,
            recipient_names: leaveData.recipientNames,
            reason: leaveData.reason,
            start_date: leaveData.startDate,
            end_date: leaveData.endDate,
            status: leaveData.status,
            timestamp: leaveData.timestamp,
            response_comment: leaveData.responseComment,
            responded_by: leaveData.respondedBy
        };
        const { data, error } = await supabase_1.supabase
            .from('leave_requests')
            .insert([dbData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async updateLeave(leaveId, updateData) {
        const dbData = {};
        if (updateData.status !== undefined)
            dbData.status = updateData.status;
        if (updateData.responseComment !== undefined)
            dbData.response_comment = updateData.responseComment;
        if (updateData.respondedBy !== undefined)
            dbData.responded_by = updateData.respondedBy;
        const { data, error } = await supabase_1.supabase
            .from('leave_requests')
            .update(dbData)
            .eq('id', leaveId)
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
}
exports.LeavesService = LeavesService;
