import { supabase } from '../config/supabase';

export class TasksRepository {
  async getTasks(assignedTo?: string) {
    let query = supabase
      .from('tasks')
      .select('*')
      .order('created_at', { ascending: false });

    if (assignedTo) {
      query = query.or(`assigned_to.eq.${assignedTo},created_by.eq.${assignedTo}`);
    }

    const { data, error } = await query;
    if (error) throw error;
    return data || [];
  }

  async getTaskById(uuid: string) {
    const { data, error } = await supabase
      .from('tasks')
      .select('*')
      .eq('uuid', uuid)
      .maybeSingle();

    if (error) throw error;
    return data;
  }

  async createTask(taskData: any) {
    const { data, error } = await supabase
      .from('tasks')
      .insert([taskData])
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async updateTask(taskUuid: string, updateData: any) {
    const { data, error } = await supabase
      .from('tasks')
      .update(updateData)
      .eq('uuid', taskUuid)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async deleteTask(taskUuid: string) {
    const { error } = await supabase
      .from('tasks')
      .delete()
      .eq('uuid', taskUuid);

    if (error) throw error;
    return { message: 'Task deleted successfully' };
  }
}
