"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserRepository = void 0;
const supabase_1 = require("../config/supabase");
class UserRepository {
    async createUser(userData) {
        const { data, error } = await supabase_1.supabase
            .from('users')
            .insert([userData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async getUserByAuthId(authId) {
        const { data, error } = await supabase_1.supabase
            .from('users')
            .select('*')
            .eq('auth_id', authId)
            .single();
        if (error && error.code !== 'PGRST116')
            throw error; // PGRST116 is not found
        return data;
    }
    async getUserByEmail(email) {
        const { data, error } = await supabase_1.supabase
            .from('users')
            .select('*')
            .eq('email', email)
            .single();
        if (error && error.code !== 'PGRST116')
            throw error;
        return data;
    }
    async createDetailedProfile(profileData) {
        // profileData currently has 'id' holding the user's auth ID or public.users ID
        // We map that to 'user_id' for the detailed_profiles table
        const dbData = {
            user_id: profileData.id,
            age: profileData.age,
            dob: profileData.dob,
            fathers_name: profileData.fathers_name,
            mothers_name: profileData.mothers_name,
            address: profileData.address,
            emergency_contact: profileData.emergency_contact,
            doj: profileData.doj,
            blood_group: profileData.blood_group
        };
        const { data, error } = await supabase_1.supabase
            .from('detailed_profiles')
            .insert([dbData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async update2FA(authId, secret, isEnabled) {
        const { data, error } = await supabase_1.supabase
            .from('users')
            .update({ totp_secret: secret, is_2fa_enabled: isEnabled })
            .eq('auth_id', authId)
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
}
exports.UserRepository = UserRepository;
