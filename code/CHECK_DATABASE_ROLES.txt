-- SQL Script to Check User Roles in Database
-- Run this in MySQL to verify roles are correct

-- Connect to the wellco database
USE wellco;

-- Check all users and their roles
SELECT username, role, email, created_date 
FROM users 
ORDER BY username;

-- Check specifically for Admin and Manager accounts
SELECT username, role, email 
FROM users 
WHERE role IN ('Admin', 'Manager', 'admin', 'manager', 'ADMIN', 'MANAGER')
ORDER BY role, username;

-- Check specific users (jbgols and ellaest)
SELECT username, role, email, password 
FROM users 
WHERE username IN ('jbgols', 'ellaest');

-- Update roles manually if needed (UNCOMMENT TO USE):
-- UPDATE users SET role = 'Admin' WHERE username = 'jbgols';
-- UPDATE users SET role = 'Manager' WHERE username = 'ellaest';

-- Verify the updates
SELECT username, role FROM users WHERE username IN ('jbgols', 'ellaest');





