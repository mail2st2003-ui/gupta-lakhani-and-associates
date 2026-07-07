import { v4 as uuidv4 } from 'uuid';

export abstract class BaseService {
  protected generateUUID(): string {
    return uuidv4();
  }
}
