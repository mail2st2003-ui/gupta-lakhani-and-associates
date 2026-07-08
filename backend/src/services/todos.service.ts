import { TodosRepository } from '../repositories/todos.repository';
import { randomUUID } from 'crypto';

export class TodosService {
  private todosRepository = new TodosRepository();

  async getTodosByUser(userUuid: string) {
    const todos = await this.todosRepository.getTodosByUser(userUuid);
    return todos.map((t: any) => ({
      ...t,
      is_completed: t.status === 'Complete',
      timestamp: Date.now()
    }));
  }

  async createTodo(todoData: any) {
    const dataToInsert: any = {
      uuid: todoData.uuid || todoData.id || randomUUID(),
      user_uuid: todoData.user_uuid || todoData.userId,
      title: todoData.title,
      description: todoData.description || '',
      status: todoData.status || 'Pending'
    };
    const result = await this.todosRepository.createTodo(dataToInsert);
    return {
      ...result,
      is_completed: result.status === 'Complete',
      timestamp: Date.now()
    };
  }

  async updateTodo(uuid: string, updateData: any) {
    const dataToUpdate: any = {};
    if (updateData.title !== undefined) dataToUpdate.title = updateData.title;
    if (updateData.description !== undefined) dataToUpdate.description = updateData.description;
    if (updateData.status !== undefined) dataToUpdate.status = updateData.status;

    const result = await this.todosRepository.updateTodo(uuid, dataToUpdate);
    return {
      ...result,
      is_completed: result.status === 'Complete',
      timestamp: Date.now()
    };
  }

  async deleteTodo(uuid: string) {
    return this.todosRepository.deleteTodo(uuid);
  }
}
