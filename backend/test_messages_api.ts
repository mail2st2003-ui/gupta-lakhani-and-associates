import { MessagesRepository } from './src/repositories/messages.repository';

async function test() {
  const repo = new MessagesRepository();
  try {
    // any valid UUID format to test
    const data = await repo.getAllUserMessages('00000000-0000-0000-0000-000000000000');
    console.log("Success! Data length:", data.length);
  } catch (err: any) {
    console.error("Error occurred:", err.message);
  }
}

test();
