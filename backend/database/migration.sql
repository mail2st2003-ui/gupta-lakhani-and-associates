-- Users Table
CREATE TABLE public.users (
    uuid UUID PRIMARY KEY,
    auth_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'Staff',
    password_updated_at TIMESTAMP WITH TIME ZONE,
    is_mfa_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- User Details Table
CREATE TABLE public.user_details (
    uuid UUID PRIMARY KEY,
    user_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE UNIQUE,
    first_name TEXT,
    middle_name TEXT,
    last_name TEXT,
    father_name TEXT,
    mother_name TEXT,
    dob TEXT,
    gender TEXT,
    blood_group TEXT,
    contact TEXT,
    official_email TEXT,
    personal_email TEXT,
    permanent_address TEXT,
    current_address TEXT,
    profile_image TEXT,
    designation TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- Todos Table (Personal tasks)
CREATE TABLE public.todos (
    uuid UUID PRIMARY KEY,
    user_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    priority TEXT DEFAULT 'Medium',
    status TEXT DEFAULT 'Pending',
    is_completed BOOLEAN DEFAULT false,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- Tasks Table
CREATE TABLE public.tasks (
    uuid UUID PRIMARY KEY,
    created_by_user_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    assigned_to_user_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- Task Details Table
CREATE TABLE public.task_details (
    uuid UUID PRIMARY KEY,
    task_uuid UUID REFERENCES public.tasks(uuid) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'Pending',
    priority TEXT DEFAULT 'Medium',
    due_date BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- Attendance Logs Table
CREATE TABLE public.attendance_logs (
    uuid UUID PRIMARY KEY,
    user_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    type TEXT NOT NULL,
    status TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- Messages Table
CREATE TABLE public.messages (
    uuid UUID PRIMARY KEY,
    sender_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    recipient_uuid TEXT NOT NULL,
    content TEXT NOT NULL,
    is_encrypted BOOLEAN DEFAULT false,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- Leave Requests Table
CREATE TABLE public.leave_requests (
    uuid UUID PRIMARY KEY,
    user_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    start_date BIGINT NOT NULL,
    end_date BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status TEXT DEFAULT 'Pending',
    timestamp BIGINT NOT NULL,
    manager_uuid UUID REFERENCES public.users(uuid) ON DELETE SET NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- Summons Table
CREATE TABLE public.summons (
    uuid UUID PRIMARY KEY,
    staff_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    summoner_uuid UUID REFERENCES public.users(uuid) ON DELETE CASCADE,
    location TEXT NOT NULL,
    urgency TEXT DEFAULT 'Normal',
    is_cleared BOOLEAN DEFAULT false,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- System Alerts Table
CREATE TABLE public.system_alerts (
    uuid UUID PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    priority TEXT DEFAULT 'Info',
    timestamp BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);
