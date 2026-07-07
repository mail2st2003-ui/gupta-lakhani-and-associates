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
      .eq('auth_id', authId)
      .single();

    if (error && error.code !== 'PGRST116') throw error; // PGRST116 is not found
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
      .from('detailed_profiles')
      .insert([profileData])
      .select()
      .single();

    if (error) throw error;
    return data;
  }
}
