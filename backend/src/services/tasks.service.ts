import { supabase } from '../config/supabase';

export class TasksService {
  async getTasks(employeeId?: string) {
    let query = supabase.from('todo_items').select('*').order('timestamp', { ascending: false });
    if (employeeId) {
      query = query.eq('employee_id', employeeId);
    }
    const { data, error } = await query;
    if (error) throw error;
    return data;
  }

  async createTask(taskData: any) {
    const { data, error } = await supabase
      .from('todo_items')
      .insert([taskData])
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async updateTask(taskId: string, updateData: any) {
    const { data, error } = await supabase
      .from('todo_items')
      .update(updateData)
      .eq('id', taskId)
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async deleteTask(taskId: string) {
    const { error } = await supabase
      .from('todo_items')
      .delete()
      .eq('id', taskId);
    if (error) throw error;
    return { message: 'Task deleted successfully' };
  }
}
