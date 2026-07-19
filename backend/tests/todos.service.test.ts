import { TodosService } from '../src/services/todos.service';
import { TodosRepository } from '../src/repositories/todos.repository';

jest.mock('../src/repositories/todos.repository');

describe('TodosService', () => {
  let todosService: TodosService;
  let mockTodosRepository: jest.Mocked<TodosRepository>;

  beforeEach(() => {
    mockTodosRepository = new TodosRepository() as jest.Mocked<TodosRepository>;
    todosService = new TodosService();
    // Inject mock repository
    (todosService as any).todosRepository = mockTodosRepository;
    
    jest.clearAllMocks();
    
    // Mock Date.now() to return a predictable value
    jest.spyOn(Date, 'now').mockImplementation(() => 1000);
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  describe('getTodosByUser', () => {
    it('should map is_completed correctly based on status', async () => {
      mockTodosRepository.getTodosByUser.mockResolvedValue([
        { id: '1', title: 'Task 1', status: 'Pending' },
        { id: '2', title: 'Task 2', status: 'Complete' }
      ]);

      const result = await todosService.getTodosByUser('user123');
      
      expect(result).toHaveLength(2);
      expect(result[0].is_completed).toBe(false);
      expect(result[0].timestamp).toBe(1000);
      expect(result[1].is_completed).toBe(true);
    });
  });

  describe('createTodo', () => {
    it('should use default values if not provided', async () => {
      const todoData = {
        title: 'New Task'
      };

      mockTodosRepository.createTodo.mockImplementation(async (data) => data);

      const result = await todosService.createTodo(todoData);

      expect(result.title).toBe('New Task');
      expect(result.description).toBe('');
      expect(result.status).toBe('Pending');
      expect(result.is_completed).toBe(false);
      expect(result.uuid).toBeDefined(); // randomUUID is used
    });
  });

  describe('updateTodo', () => {
    it('should update partial fields and map is_completed', async () => {
      const updateData = {
        status: 'Complete'
      };

      mockTodosRepository.updateTodo.mockImplementation(async (uuid, data) => {
        return { uuid, title: 'Existing Task', ...data };
      });

      const result = await todosService.updateTodo('uuid-123', updateData);

      expect(result.status).toBe('Complete');
      expect(result.is_completed).toBe(true);
      expect(result.timestamp).toBe(1000);
    });
  });

  describe('deleteTodo', () => {
    it('should call repository delete', async () => {
      mockTodosRepository.deleteTodo.mockResolvedValue({ success: true } as any);

      const result = await todosService.deleteTodo('uuid-123');

      expect(mockTodosRepository.deleteTodo).toHaveBeenCalledWith('uuid-123');
      expect(result).toEqual({ success: true });
    });
  });
});
