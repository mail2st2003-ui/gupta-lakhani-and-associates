import { randomUUID } from 'crypto';
import { supabase } from '../config/supabase';

export class UserRepository {
  private flattenUserProfile(user: any) {
    if (!user) return user;
    const details = Array.isArray(user.user_details) ? user.user_details[0] : user.user_details;
    const { user_details, ...baseUser } = user;

    if (!details) return baseUser;

    return {
      ...baseUser,
      first_name: details.first_name ?? null,
      last_name: details.last_name ?? null,
      designation: details.designation ?? null,
      profile_image: details.profile_image ?? null,
      contact: details.contact ?? null,
      emergency_contact: details.emergency_contact ?? null
    };
  }

  async createUser(userData: any) {
    const { data, error } = await supabase
      .from('users')
      .insert([userData])
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async getUserByUuid(uuid: string) {
    const { data, error } = await supabase
      .from('users')
      .select('*, user_details(*)')
      .eq('uuid', uuid)
      .single();

    if (error && error.code !== 'PGRST116') throw error;
    return this.flattenUserProfile(data);
  }

  async getUserByEmail(email: string) {
    const { data, error } = await supabase
      .from('users')
      .select('*, user_details(*)')
      .eq('email', email)
      .single();

    if (error && error.code !== 'PGRST116') throw error;
    return this.flattenUserProfile(data);
  }

  async createDetailedProfile(profileData: any) {
    const { data, error } = await supabase
      .from('user_details')
      .insert([profileData])
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async upsertDetailedProfile(profileData: any) {
    const { data, error } = await supabase
      .from('user_details')
      .upsert([profileData], { onConflict: 'user_uuid' })
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async updateProfileImage(userUuid: string, profileImage: string | null) {
    const { data: updated, error: updateError } = await supabase
      .from('user_details')
      .update({ profile_image: profileImage })
      .eq('user_uuid', userUuid)
      .select()
      .maybeSingle();

    if (updateError) throw updateError;
    if (updated) return updated;

    const { data, error } = await supabase
      .from('user_details')
      .insert([{
        uuid: randomUUID(),
        user_uuid: userUuid,
        profile_image: profileImage
      }])
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async update2FA(uuid: string, isEnabled: boolean, secret: string | null = null) {
    const updateData: any = { is_mfa_enabled: isEnabled };
    if (isEnabled || secret === null) {
      updateData.mfa_secret = secret;
    }

    const { error } = await supabase
      .from('users')
      .update(updateData)
      .eq('uuid', uuid);

    if (error) throw error;
  }
}
