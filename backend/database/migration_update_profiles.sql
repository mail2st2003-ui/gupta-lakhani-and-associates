-- Migration to update detailed_profiles schema without data loss

-- 1. Rename the existing 'id' column (which holds the user reference) to 'user_id'
ALTER TABLE public.detailed_profiles RENAME COLUMN id TO user_id;

-- 2. Drop the existing primary key constraint
ALTER TABLE public.detailed_profiles DROP CONSTRAINT IF EXISTS detailed_profiles_pkey CASCADE;

-- 3. Add a new 'id' column with UUID v4 generation as the new primary key
ALTER TABLE public.detailed_profiles ADD COLUMN id UUID PRIMARY KEY DEFAULT uuid_generate_v4();

-- 4. Add a UNIQUE constraint to user_id so each user can only have one profile
ALTER TABLE public.detailed_profiles ADD CONSTRAINT detailed_profiles_user_id_key UNIQUE (user_id);
