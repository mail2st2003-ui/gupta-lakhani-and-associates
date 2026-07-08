import { supabase } from '../config/supabase';

export class TodosRepository {
  async getTodosByUser(userUuid: string) {
    const { data, error } = await supabase
      .from('todos')
      .select('*')
      .eq('user_uuid', userUuid)
      .order('created_at', { ascending: false });

    if (error) throw error;
    return data;
  }

  async createTodo(todoData: any) {
    const { data, error } = await supabase
      .from('todos')
      .insert([todoData])
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async updateTodo(uuid: string, updateData: any) {
    const { data, error } = await supabase
      .from('todos')
      .update(updateData)
      .eq('uuid', uuid)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async deleteTodo(uuid: string) {
    const { data, error } = await supabase
      .from('todos')
      .delete()
      .eq('uuid', uuid);

    if (error) throw error;
    return { success: true, message: 'Todo deleted successfully' };
  }
}
