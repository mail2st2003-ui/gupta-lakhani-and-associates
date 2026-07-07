const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

const connectionString = 'postgres://postgres:Sanya%402026%40@db.fsztbtrjtiijripiyumu.supabase.co:5432/postgres';

async function runMigration() {
  const client = new Client({
    connectionString,
    ssl: { rejectUnauthorized: false }
  });

  try {
    console.log('Connecting to Supabase...');
    await client.connect();
    
    console.log('Reading migration_update_tables.sql...');
    const sqlPath = path.join(__dirname, 'database', 'migration_update_tables.sql');
    const sql = fs.readFileSync(sqlPath, 'utf8');

    console.log('Executing migration...');
    await client.query(sql);

    console.log('Migration completed successfully!');
  } catch (err) {
    console.error('Error executing migration:', err);
  } finally {
    await client.end();
  }
}

runMigration();
