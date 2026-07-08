import { TodosRepository } from '../repositories/todos.repository';
import { randomUUID } from 'crypto';

export class TodosService {
  private todosRepository = new TodosRepository();

  async getTodosByUser(userUuid: string) {
    return this.todosRepository.getTodosByUser(userUuid);
  }

  async createTodo(todoData: any) {
    const dataToInsert = {
      uuid: todoData.uuid || todoData.id || randomUUID(),
      user_uuid: todoData.user_uuid || todoData.userId,
      title: todoData.title,
      description: todoData.description || '',
      priority: todoData.priority || 'Medium',
      status: todoData.status || 'Pending',
      is_completed: todoData.is_completed || false,
      timestamp: todoData.timestamp || Date.now(),
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
    };
    return this.todosRepository.createTodo(dataToInsert);
  }

  async updateTodo(uuid: string, updateData: any) {
    const dataToUpdate: any = {
      updated_at: new Date().toISOString()
    };
    if (updateData.title !== undefined) dataToUpdate.title = updateData.title;
    if (updateData.description !== undefined) dataToUpdate.description = updateData.description;
    if (updateData.priority !== undefined) dataToUpdate.priority = updateData.priority;
    if (updateData.status !== undefined) dataToUpdate.status = updateData.status;
    if (updateData.is_completed !== undefined) dataToUpdate.is_completed = updateData.is_completed;

    return this.todosRepository.updateTodo(uuid, dataToUpdate);
  }

  async deleteTodo(uuid: string) {
    return this.todosRepository.deleteTodo(uuid);
  }
}
