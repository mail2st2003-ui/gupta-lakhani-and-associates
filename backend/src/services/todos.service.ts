import { TodosRepository } from '../repositories/todos.repository';
import { randomUUID } from 'crypto';

export class TodosService {
  private todosRepository = new TodosRepository();

  async getTodosByUser(userUuid: string) {
    return this.todosRepository.getTodosByUser(userUuid);
  }

  async createTodo(todoData: any) {
    const dataToInsert: any = {
      uuid: todoData.uuid || todoData.id || randomUUID(),
      user_uuid: todoData.user_uuid || todoData.userId,
      title: todoData.title,
      description: todoData.description || '',
      status: todoData.status || 'Pending'
    };
    return this.todosRepository.createTodo(dataToInsert);
  }

  async updateTodo(uuid: string, updateData: any) {
    const dataToUpdate: any = {};
    if (updateData.title !== undefined) dataToUpdate.title = updateData.title;
    if (updateData.description !== undefined) dataToUpdate.description = updateData.description;
    if (updateData.status !== undefined) dataToUpdate.status = updateData.status;

    return this.todosRepository.updateTodo(uuid, dataToUpdate);
  }

  async deleteTodo(uuid: string) {
    return this.todosRepository.deleteTodo(uuid);
  }
}
