import { supabase } from '../config/supabase';

export class MessagesRepository {
  async saveMessage(messageData: any) {
    const { data, error } = await supabase
      .from('messages')
      .insert([messageData])
      .select()
      .single();
    if (error) throw error;
    return data;
  }

  async markAsDelivered(messageUuid: string) {
    const { error } = await supabase
      .from('messages')
      .update({ is_delivered: true })
      .eq('uuid', messageUuid);
    if (error) throw error;
  }

  async markAsSeen(messageUuid: string) {
    const { error } = await supabase
      .from('messages')
      .update({ is_seen: true })
      .eq('uuid', messageUuid);
    if (error) throw error;
  }

  async getMessagesBetween(user1Uuid: string, user2Uuid: string, limit: number = 50) {
    const { data, error } = await supabase
      .from('messages')
      .select('*')
      .or(`and(sender_uuid.eq.${user1Uuid},recipient_uuid.eq.${user2Uuid}),and(sender_uuid.eq.${user2Uuid},recipient_uuid.eq.${user1Uuid})`)
      .order('created_at', { ascending: false })
      .limit(limit);
    if (error) throw error;
    return data.reverse();
  }

  async deleteMessagesBetween(user1Uuid: string, user2Uuid: string) {
    const { error: error1 } = await supabase
      .from('messages')
      .delete()
      .match({ sender_uuid: user1Uuid, recipient_uuid: user2Uuid });
      
    if (error1) throw error1;

    const { error: error2 } = await supabase
      .from('messages')
      .delete()
      .match({ sender_uuid: user2Uuid, recipient_uuid: user1Uuid });

    if (error2) throw error2;
  }

  async getAllUserMessages(userUuid: string) {
    const { data, error } = await supabase
      .from('messages')
      .select('*')
      .or(`sender_uuid.eq.${userUuid},recipient_uuid.eq.${userUuid}`)
      .order('timestamp', { ascending: true });
    
    if (error) throw error;
    return data;
  }
}
