import { randomUUID } from 'crypto';
import { supabase } from '../config/supabase';

export class TasksService {
  async getTasks(employeeId?: string, includePersonal = false) {
    const results: any[] = [];

    if (includePersonal && employeeId) {
      const { data: todos, error: todosError } = await supabase
        .from('todos')
        .select('*')
        .eq('user_uuid', employeeId)
        .order('timestamp', { ascending: false });
      if (todosError) throw todosError;

      results.push(...(todos || []).map((todo: any) => ({
        uuid: todo.uuid,
        user_uuid: todo.user_uuid,
        title: todo.title || '',
        description: todo.description || '',
        priority: todo.priority || 'Medium',
        is_completed: todo.is_completed || false,
        status: todo.status || 'Pending',
        timestamp: todo.timestamp || Date.now(),
        is_personal: true,
        assigned_by: '',
        isSynced: true
      })));
    }

    let query = supabase
      .from('tasks')
      .select('*, task_details(*)')
      .order('created_at', { ascending: false });

    if (employeeId) {
      query = query.eq('assigned_to_user_uuid', employeeId);
    }

    const { data: assignedTasks, error } = await query;
    if (error) throw error;

    results.push(...(assignedTasks || []).map((task: any) => {
      const details = Array.isArray(task.task_details) ? task.task_details[0] : task.task_details;
      return {
        uuid: task.uuid,
        user_uuid: task.assigned_to_user_uuid,
        title: details?.title || '',
        description: details?.description || '',
        priority: details?.priority || 'Medium',
        is_completed: details?.status === 'Complete',
        status: details?.status || 'Pending',
        timestamp: details?.created_at ? new Date(details.created_at).getTime() : Date.now(),
        is_personal: false,
        assigned_by: task.created_by_user_uuid || '',
        isSynced: true
      };
    }));

    return results.sort((a, b) => b.timestamp - a.timestamp);
  }

  async createTask(taskData: any) {
    const isPersonal = taskData.is_personal ?? taskData.isPersonal ?? true;

    if (isPersonal) {
      const todoData = {
        uuid: taskData.uuid || taskData.id || randomUUID(),
        user_uuid: taskData.user_uuid || taskData.employeeId,
        title: taskData.title,
        description: taskData.description || '',
        priority: taskData.priority || 'Medium',
        status: taskData.status || 'Pending',
        is_completed: taskData.is_completed ?? taskData.isCompleted ?? false,
        timestamp: taskData.timestamp || Date.now()
      };

      if (!todoData.user_uuid) {
        throw new Error('Task user_uuid is required');
      }
      if (!todoData.title) {
        throw new Error('Task title is required');
      }

      const { data, error } = await supabase
        .from('todos')
        .upsert([todoData], { onConflict: 'uuid' })
        .select()
        .single();
      if (error) throw error;
      return data;
    }

    const taskUuid = taskData.uuid || taskData.id;
    const taskRecord = {
      uuid: taskUuid,
      created_by_user_uuid: taskData.assigned_by || taskData.assignedBy || null,
      assigned_to_user_uuid: taskData.user_uuid || taskData.employeeId
    };

    const { data: createdTask, error: taskError } = await supabase
      .from('tasks')
      .insert([taskRecord])
      .select()
      .single();
    if (taskError) throw taskError;

    const detailsRecord = {
      uuid: taskData.details_uuid || randomUUID(),
      task_uuid: taskUuid,
      title: taskData.title,
      description: taskData.description || '',
      status: taskData.status || 'Pending',
      priority: taskData.priority || 'Medium',
      due_date: taskData.due_date || taskData.timestamp || Date.now()
    };

    const { data: details, error: detailsError } = await supabase
      .from('task_details')
      .insert([detailsRecord])
      .select()
      .single();
    if (detailsError) throw detailsError;

    return { ...createdTask, task_details: details };
  }

  async updateTask(taskId: string, updateData: any) {
    const dbData: any = {};
    if (updateData.status !== undefined) dbData.status = updateData.status;
    if (updateData.priority !== undefined) dbData.priority = updateData.priority;
    if (updateData.title !== undefined) dbData.title = updateData.title;
    if (updateData.description !== undefined) dbData.description = updateData.description;

    const { data, error } = await supabase
      .from('task_details')
      .update(dbData)
      .eq('task_uuid', taskId)
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async deleteTask(taskId: string) {
    const { error } = await supabase
      .from('tasks')
      .delete()
      .eq('uuid', taskId);
    if (error) throw error;
    return { message: 'Task deleted successfully' };
  }
}
