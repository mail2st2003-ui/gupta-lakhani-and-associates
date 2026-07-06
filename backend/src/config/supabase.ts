import { createClient } from '@supabase/supabase-js';
import dotenv from 'dotenv';
import WebSocket from 'ws';

if (typeof global.WebSocket === 'undefined') {
  (global as any).WebSocket = WebSocket;
}

dotenv.config();

const supabaseUrl = process.env.SUPABASE_URL || '';
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY || '';

if (!supabaseUrl || !supabaseServiceKey) {
  console.error('Supabase URL and Service Role Key must be provided!');
}

export const supabase = createClient(supabaseUrl, supabaseServiceKey);
