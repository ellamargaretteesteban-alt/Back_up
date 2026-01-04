# How to Check Database Roles

## Method 1: MySQL Command Line

1. **Open Command Prompt or PowerShell**
2. **Connect to MySQL:**
   ```bash
   mysql -u root -p
   ```
   Enter your password when prompted (default: `hfqe72161954_`)

3. **Select the database:**
   ```sql
   USE wellco;
   ```

4. **Check all users and roles:**
   ```sql
   SELECT username, role, email FROM users ORDER BY username;
   ```

5. **Check specific Admin/Manager accounts:**
   ```sql
   SELECT username, role, email FROM users WHERE username IN ('jbgols', 'ellaest');
   ```

6. **If roles are wrong, update them:**
   ```sql
   UPDATE users SET role = 'Admin' WHERE username = 'jbgols';
   UPDATE users SET role = 'Manager' WHERE username = 'ellaest';
   ```

7. **Verify the update:**
   ```sql
   SELECT username, role FROM users WHERE username IN ('jbgols', 'ellaest');
   ```

8. **Exit MySQL:**
   ```sql
   EXIT;
   ```

## Method 2: MySQL Workbench (GUI)

1. **Open MySQL Workbench**
2. **Connect to your MySQL server** (localhost:3306)
   - Username: `root`
   - Password: `hfqe72161954_`

3. **Open a new SQL tab** (File → New Query Tab)

4. **Run these queries:**
   ```sql
   USE wellco;
   
   -- Check all users
   SELECT username, role, email FROM users;
   
   -- Check specific users
   SELECT username, role FROM users WHERE username IN ('jbgols', 'ellaest');
   
   -- Update if needed
   UPDATE users SET role = 'Admin' WHERE username = 'jbgols';
   UPDATE users SET role = 'Manager' WHERE username = 'ellaest';
   ```

5. **Click the Execute button** (lightning bolt icon) or press `Ctrl+Enter`

## Method 3: Using the SQL Script File

1. **Open MySQL Command Line or Workbench**
2. **Run the script:**
   ```bash
   mysql -u root -p wellco < CHECK_DATABASE_ROLES.sql
   ```
   
   Or in MySQL Workbench:
   - File → Open SQL Script
   - Select `CHECK_DATABASE_ROLES.sql`
   - Execute

## Quick Check Commands

```sql
-- Quick check - see all roles
SELECT username, role FROM users;

-- See if Admin/Manager exist
SELECT username, role FROM users WHERE role LIKE '%admin%' OR role LIKE '%manager%';

-- Count by role
SELECT role, COUNT(*) as count FROM users GROUP BY role;
```

## If Roles Are Wrong

If you find that `jbgols` or `ellaest` have role = 'Customer' instead of 'Admin'/'Manager':

1. **Update in MySQL:**
   ```sql
   UPDATE users SET role = 'Admin' WHERE username = 'jbgols';
   UPDATE users SET role = 'Manager' WHERE username = 'ellaest';
   ```

2. **Restart your Java application** - it should now work correctly

3. **Or delete and let accounts.txt reload them:**
   ```sql
   DELETE FROM users WHERE username IN ('jbgols', 'ellaest');
   ```
   Then restart the app - it will reload from accounts.txt with correct roles.

## Database Connection Info

From your `DatabaseManager.java`:
- **Database:** `wellco`
- **Host:** `localhost:3306`
- **Username:** `root`
- **Password:** `hfqe72161954_`





