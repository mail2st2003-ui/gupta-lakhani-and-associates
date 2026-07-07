"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserRepository = void 0;
const crypto_1 = require("crypto");
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
    async getUserByUuid(uuid) {
        const { data, error } = await supabase_1.supabase
            .from('users')
            .select('*')
            .eq('uuid', uuid)
            .single();
        if (error && error.code !== 'PGRST116')
            throw error;
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
        const { data, error } = await supabase_1.supabase
            .from('user_details')
            .insert([profileData])
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async upsertDetailedProfile(profileData) {
        const { data, error } = await supabase_1.supabase
            .from('user_details')
            .upsert([profileData], { onConflict: 'user_uuid' })
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async updateProfileImage(userUuid, profileImage) {
        const { data, error } = await supabase_1.supabase
            .from('user_details')
            .upsert([{
                uuid: (0, crypto_1.randomUUID)(),
                user_uuid: userUuid,
                profile_image: profileImage
            }], { onConflict: 'user_uuid' })
            .select()
            .single();
        if (error)
            throw error;
        return data;
    }
    async update2FA(uuid, isEnabled, secret = null) {
        const updateData = { is_mfa_enabled: isEnabled };
        if (isEnabled || secret === null) {
            updateData.mfa_secret = secret;
        }
        const { error } = await supabase_1.supabase
            .from('users')
            .update(updateData)
            .eq('uuid', uuid);
        if (error)
            throw error;
    }
}
exports.UserRepository = UserRepository;
