import { supabase } from '../config/supabase';

export class LeavesRepository {
  async getLeaves(userUuid?: string) {
    let query = supabase.from('leave_requests').select('*').order('created_at', { ascending: false });
    if (userUuid) {
      query = query.eq('user_uuid', userUuid);
    }
    const { data, error } = await query;
    if (error) throw error;
    return data;
  }

  async checkOverlap(userUuid: string, startDate: number, endDate: number, excludeLeaveUuid?: string) {
    let query = supabase
      .from('leave_requests')
      .select('uuid')
      .eq('user_uuid', userUuid)
      .lte('start_date', endDate)
      .gte('end_date', startDate);
      
    if (excludeLeaveUuid) {
      query = query.neq('uuid', excludeLeaveUuid);
    }
    const { data, error } = await query;
    if (error) throw error;
    return data;
  }

  async createLeave(leaveData: any) {
    const { data, error } = await supabase
      .from('leave_requests')
      .insert([leaveData])
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async updateLeave(leaveUuid: string, updateData: any) {
    const { data, error } = await supabase
      .from('leave_requests')
      .update(updateData)
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
