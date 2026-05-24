-- Simple script to fix admin user - just update role and status
-- Run this in your Supabase SQL Editor

UPDATE users 
SET role = 'ADMIN',
    account_status = 'Active'
WHERE email = 'admin@bookbud.com';

-- Check what we have
SELECT user_id, username, email, role, account_status 
FROM users 
WHERE email = 'admin@bookbud.com';
