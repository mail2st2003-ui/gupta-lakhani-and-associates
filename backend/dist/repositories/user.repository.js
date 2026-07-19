"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserRepository = void 0;
const crypto_1 = require("crypto");
const supabase_1 = require("../config/supabase");
class UserRepository {
    flattenUserProfile(user) {
        if (!user)
            return user;
        const details = Array.isArray(user.user_details) ? user.user_details[0] : user.user_details;
        const { user_details, ...baseUser } = user;
        if (!details)
            return baseUser;
        return {
            ...details,
            ...baseUser, // ensures baseUser.uuid overrides details.uuid
        };
    }
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
            .select('*, user_details(*)')
            .eq('uuid', uuid)
            .single();
        if (error && error.code !== 'PGRST116')
            throw error;
        return this.flattenUserProfile(data);
    }
    async getUserByEmail(email) {
        const { data, error } = await supabase_1.supabase
            .from('users')
            .select('*, user_details(*)')
            .eq('email', email)
            .single();
        if (error && error.code !== 'PGRST116')
            throw error;
        return this.flattenUserProfile(data);
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
        const { data: updated, error: updateError } = await supabase_1.supabase
            .from('user_details')
            .update({ profile_image: profileImage })
            .eq('user_uuid', userUuid)
            .select()
            .maybeSingle();
        if (updateError)
            throw updateError;
        if (updated)
            return updated;
        const { data, error } = await supabase_1.supabase
            .from('user_details')
            .insert([{
                uuid: (0, crypto_1.randomUUID)(),
                user_uuid: userUuid,
                profile_image: profileImage
            }])
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
    async updatePassword(uuid, hashedPassword) {
        const { error } = await supabase_1.supabase
            .from('users')
            .update({ password: hashedPassword })
            .eq('uuid', uuid);
        if (error)
            throw error;
    }
    async getAllUsers() {
        const { data, error } = await supabase_1.supabase
            .from('users')
            .select('*, user_details(*)');
        if (error)
            throw error;
        return (data || []).map((user) => this.flattenUserProfile(user));
    }
}
exports.UserRepository = UserRepository;
