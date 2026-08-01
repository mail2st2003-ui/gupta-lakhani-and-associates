-- Migration for Admin Task Management
CREATE TABLE IF NOT EXISTS public.tasks (
    uuid UUID PRIMARY KEY,
    title TEXT,
    description TEXT,
    created_by TEXT,
    assigned_to TEXT,
    assigned_by TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    status TEXT DEFAULT 'Pending',
    priority TEXT DEFAULT 'Medium'
);

ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS title TEXT;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS created_by TEXT;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS assigned_to TEXT;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS assigned_by TEXT;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now());
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now());
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'Pending';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS priority TEXT DEFAULT 'Medium';
