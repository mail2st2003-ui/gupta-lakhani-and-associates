import { supabase } from '../config/supabase';

export class TasksRepository {
  async getTasks(assignedToUserUuid?: string) {
    let query = supabase.from('tasks').select('*, task_details(*)').order('created_at', { ascending: false });
    if (assignedToUserUuid) {
      query = query.eq('assigned_to_user_uuid', assignedToUserUuid);
    }
    const { data, error } = await query;
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

  async createTaskDetails(detailsData: any) {
    const { data, error } = await supabase
      .from('task_details')
      .insert([detailsData])
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async updateTaskDetails(taskUuid: string, updateData: any) {
    const { data, error } = await supabase
      .from('task_details')
      .update(updateData)
      .eq('task_uuid', taskUuid)
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
  }
}
