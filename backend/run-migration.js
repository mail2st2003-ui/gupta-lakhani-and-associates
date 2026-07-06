const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

// Connection string with URL-encoded password (Sanya@2026@ -> Sanya%402026%40)
const connectionString = 'postgres://postgres:Sanya%402026%40@db.fsztbtrjtiijripiyumu.supabase.co:5432/postgres';

async function runMigration() {
  const client = new Client({
    connectionString,
    ssl: { rejectUnauthorized: false }
  });

  try {
    console.log('Connecting to Supabase...');
    await client.connect();
    
    // UUID extension might be needed for uuid_generate_v4()
    console.log('Ensuring uuid-ossp extension exists...');
    await client.query('CREATE EXTENSION IF NOT EXISTS "uuid-ossp";');

    console.log('Reading migration.sql...');
    const sqlPath = path.join(__dirname, 'database', 'migration.sql');
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
