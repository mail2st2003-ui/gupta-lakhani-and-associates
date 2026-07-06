# Backend for FirmSync

This is the backend service for the FirmSync Android application. It provides REST APIs for authentication and data management, wrapping Supabase as the underlying database and authentication provider.

## Features
- **Supabase Authentication**: Secure JWT-based authentication
- **Clean Architecture**: Separation of concerns (Controllers, Services, Repositories)
- **TypeScript & Express**: Strongly typed, robust routing

## Setup

1. Copy `.env.example` to `.env` and configure your Supabase credentials.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```

## Database Migration
Execute the `database/migration.sql` script in your Supabase project's SQL editor to generate the required tables.
