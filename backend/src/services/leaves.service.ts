import { supabase } from '../config/supabase';

export class LeavesService {
  async getLeaves(employeeId?: string) {
    let query = supabase.from('leave_requests').select('*').order('timestamp', { ascending: false });
    if (employeeId) {
      query = query.eq('employee_id', employeeId);
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

  async updateLeave(leaveId: string, updateData: any) {
    const { data, error } = await supabase
      .from('leave_requests')
      .update(updateData)
      .eq('id', leaveId)
      .select()
      .single();
    if (error) throw error;
    return data;
  }
}
