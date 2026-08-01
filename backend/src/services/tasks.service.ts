import { randomUUID } from 'crypto';
import { TasksRepository } from '../repositories/tasks.repository';

// Exact schema columns of public.tasks table in Supabase
const TASK_DB_COLUMNS = [
  'uuid',
  'title',
  'description',
  'created_by',
  'assigned_to',
  'assigned_by',
  'created_at',
  'assigned_at',
  'status',
  'priority'
] as const;

export class TasksService {
  private repository = new TasksRepository();

  /**
   * Filters incoming payload to only allow valid public.tasks table columns,
   * preventing PostgreSQL errors on extra client fields (e.g. isSynced, is_completed).
   */
  private filterTaskColumns(data: any): Record<string, any> {
    if (!data || typeof data !== 'object') return {};
    const sanitized: Record<string, any> = {};
    for (const col of TASK_DB_COLUMNS) {
      if (data[col] !== undefined) {
        sanitized[col] = data[col];
      }
    }
    return sanitized;
  }

  private formatTaskRecord(task: any) {
    if (!task) return null;

    return {
      uuid: task.uuid || randomUUID(),
      title: task.title || '',
      description: task.description || '',
      created_by: task.created_by || '',
      assigned_to: task.assigned_to || '',
      assigned_by: task.assigned_by || task.created_by || '',
      created_at: task.created_at || new Date().toISOString(),
      assigned_at: task.assigned_at || task.created_at || new Date().toISOString(),
      status: task.status || 'Pending',
      priority: task.priority || 'Medium'
    };
  }

  async getTasks(assignedTo?: string) {
    const rawTasks = await this.repository.getTasks(assignedTo);
    return (rawTasks || []).map((t: any) => this.formatTaskRecord(t)).filter(Boolean);
  }

  async getTaskById(taskId: string) {
    const rawTask = await this.repository.getTaskById(taskId);
    if (!rawTask) return null;
    return this.formatTaskRecord(rawTask);
  }

  async createTask(taskData: any) {
    const now = new Date().toISOString();
    const prepared = {
      uuid: taskData.uuid || randomUUID(),
      created_at: now,
      assigned_at: now,
      status: 'Pending',
      priority: 'Medium',
      ...taskData
    };

    const taskRecord = this.filterTaskColumns(prepared);
    const created = await this.repository.createTask(taskRecord);
    return this.formatTaskRecord(created);
  }

  async updateTask(taskId: string, updateData: any) {
    const taskUpdateData = this.filterTaskColumns(updateData);
    const updated = await this.repository.updateTask(taskId, taskUpdateData);
    return this.formatTaskRecord(updated);
  }

  async deleteTask(taskId: string) {
    return this.repository.deleteTask(taskId);
  }
}
