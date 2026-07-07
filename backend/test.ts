import { supabase } from './src/config/supabase';

async function test() {
    const { data: listData } = await supabase.auth.admin.listUsers();
    console.log(listData?.users.map(u => u.email));
}
test();
