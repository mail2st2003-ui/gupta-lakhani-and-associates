import { supabase } from '../config/supabase';

export class LeavesService {
  async getLeaves(userUuid?: string) {
    let query = supabase.from('leave_requests').select('*').order('created_at', { ascending: false });
    if (userUuid) {
      query = query.eq('user_uuid', userUuid);
    }
    const { data, error } = await query;
    if (error) throw error;
    return data;
  }

  async createLeave(leaveData: any) {
    const dbData: any = {
      uuid: leaveData.uuid || leaveData.id,
      user_uuid: leaveData.user_uuid || leaveData.employeeId,
      reason: leaveData.reason,
      start_date: leaveData.start_date || leaveData.startDate,
      end_date: leaveData.end_date || leaveData.endDate,
      status: leaveData.status || 'Pending',
    };

    const { data, error } = await supabase
      .from('leave_requests')
      .insert([dbData])
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async updateLeave(leaveUuid: string, updateData: any) {
    const dbData: any = {};
    if (updateData.status !== undefined) dbData.status = updateData.status;
    if (updateData.reason !== undefined) dbData.reason = updateData.reason;
    if (updateData.start_date !== undefined) dbData.start_date = updateData.start_date;
    if (updateData.end_date !== undefined) dbData.end_date = updateData.end_date;

    const { data, error } = await supabase
      .from('leave_requests')
      .update(dbData)
      .eq('uuid', leaveUuid)
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async deleteLeave(leaveUuid: string) {
    const { data, error } = await supabase
      .from('leave_requests')
      .delete()
      .eq('uuid', leaveUuid);
    if (error) throw error;
    return { success: true, message: 'Leave request deleted successfully' };
  }
}
