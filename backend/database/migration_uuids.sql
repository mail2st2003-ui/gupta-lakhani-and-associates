-- WARNING: This will drop the existing tables and recreate them with UUID primary keys.

DROP TABLE IF EXISTS public.attendance_logs CASCADE;
CREATE TABLE public.attendance_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    employee_name TEXT,
    type TEXT NOT NULL,
    status TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    is_synced BOOLEAN DEFAULT true
);

DROP TABLE IF EXISTS public.system_alerts CASCADE;
CREATE TABLE public.system_alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    priority TEXT DEFAULT 'Info',
    sender_name TEXT,
    timestamp BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

DROP TABLE IF EXISTS public.summons CASCADE;
CREATE TABLE public.summons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    staff_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    staff_name TEXT,
    summoner_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    summoner_name TEXT,
    location TEXT NOT NULL,
    urgency TEXT DEFAULT 'Normal',
    is_cleared BOOLEAN DEFAULT false,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);
