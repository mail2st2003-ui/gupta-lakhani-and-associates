-- Update todo_items table
ALTER TABLE public.todo_items ADD COLUMN IF NOT EXISTS is_personal BOOLEAN DEFAULT false;
ALTER TABLE public.todo_items ADD COLUMN IF NOT EXISTS assigned_by TEXT;

-- Update leave_requests table
ALTER TABLE public.leave_requests ADD COLUMN IF NOT EXISTS employee_role TEXT;
ALTER TABLE public.leave_requests ADD COLUMN IF NOT EXISTS recipient_ids TEXT;
ALTER TABLE public.leave_requests ADD COLUMN IF NOT EXISTS recipient_names TEXT;
ALTER TABLE public.leave_requests ADD COLUMN IF NOT EXISTS response_comment TEXT;
ALTER TABLE public.leave_requests ADD COLUMN IF NOT EXISTS responded_by TEXT;

-- Update user_details table
ALTER TABLE public.user_details ADD COLUMN IF NOT EXISTS emergency_contact TEXT;

-- We need to change start_date and end_date from BIGINT to TEXT because Android sends Strings like "2026-07-08"
ALTER TABLE public.leave_requests ALTER COLUMN start_date TYPE TEXT USING start_date::TEXT;
ALTER TABLE public.leave_requests ALTER COLUMN end_date TYPE TEXT USING end_date::TEXT;
