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

  async getUserByAuthId(authId: string) {
    const { data, error } = await supabase
      .from('users')
      .select('*')
      .eq('uuid', authId)
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

  async update2FA(authId: string, secret: string | null, isEnabled: boolean) {
    const { error } = await supabase
      .from('users')
      .update({
        is_mfa_enabled: isEnabled
      })
      .eq('uuid', authId);

    if (error) throw error;
  }
}
