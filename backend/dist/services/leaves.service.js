"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LeavesService = void 0;
const supabase_1 = require("../config/supabase");
class LeavesService {
    async getLeaves(userUuid) {
        let query = supabase_1.supabase.from('leave_requests').select('*').order('created_at', { ascending: false });
        if (userUuid) {
            query = query.eq('user_uuid', userUuid);
        }
        const { data, error } = await query;
        if (error)
            throw error;
        return data;
    }
    async createLeave(leaveData) {
        const dbData = {
            uuid: leaveData.uuid || leaveData.id,
            user_uuid: leaveData.user_uuid || leaveData.employeeId,
            reason: leaveData.reason,
            start_date: leaveData.start_date || leaveData.startDate,
            end_date: leaveData.end_date || leaveData.endDate,
            status: leaveData.status || 'Pending',
            timestamp: leaveData.timestamp || Date.now(),
            comment: leaveData.comment || leaveData.responseComment || ''
        };
        const managerUuid = leaveData.manager_uuid || leaveData.managerUuid;
        if (managerUuid)
            dbData.manager_uuid = managerUuid;
        const { data, error } = await supabase_1.supabase
            .from('leave_requests')
            .insert([dbData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async updateLeave(leaveUuid, updateData) {
        const dbData = {};
        if (updateData.status !== undefined)
            dbData.status = updateData.status;
        if (updateData.comment !== undefined)
            dbData.comment = updateData.comment;
        if (updateData.manager_uuid !== undefined)
            dbData.manager_uuid = updateData.manager_uuid || null;
        const { data, error } = await supabase_1.supabase
            .from('leave_requests')
            .update(dbData)
            .eq('uuid', leaveUuid)
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
}
exports.LeavesService = LeavesService;
