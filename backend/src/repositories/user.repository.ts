import { randomUUID } from 'crypto';
import { supabase } from '../config/supabase';

export class UserRepository {
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
      .select('*')
      .eq('uuid', uuid)
      .single();

    if (error && error.code !== 'PGRST116') throw error;
    return data;
  }

  async getUserByEmail(email: string) {
    const { data, error } = await supabase
      .from('users')
      .select('*')
      .eq('email', email)
      .single();

    if (error && error.code !== 'PGRST116') throw error;
    return data;
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
    const { data, error } = await supabase
      .from('user_details')
      .upsert([{
        uuid: randomUUID(),
        user_uuid: userUuid,
        profile_image: profileImage
      }], { onConflict: 'user_uuid' })
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
