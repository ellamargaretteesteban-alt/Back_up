# MySQL Installation and Setup Guide for WellCo Application

## Step 1: Download MySQL

1. Go to: https://dev.mysql.com/downloads/installer/
2. Download **MySQL Installer for Windows** (the larger file, usually ~400MB)
   - Choose: `mysql-installer-community-8.x.x.x.msi` (recommended)
   - Or: `mysql-installer-web-community-8.x.x.x.msi` (smaller, downloads during install)

## Step 2: Install MySQL

1. **Run the installer** (right-click → Run as Administrator)
2. **Choose Setup Type:**
   - Select: **"Developer Default"** (includes MySQL Server, Workbench, and connectors)
   - Click "Next"

3. **Check Requirements:**
   - Installer will check for prerequisites
   - Install any missing components if prompted
   - Click "Execute" to install missing components
   - Click "Next" when all are installed

4. **Installation:**
   - Click "Execute" to install MySQL products
   - Wait for installation to complete (may take 5-10 minutes)
   - Click "Next" when done

## Step 3: Configure MySQL Server

1. **Type and Networking:**
   - **Config Type:** Choose "Development Computer" (default)
   - **Port:** Keep default `3306`
   - Click "Next"

2. **Authentication Method:**
   - **Important:** Choose **"Use Strong Password Encryption"** (recommended)
   - Or choose "Use Legacy Authentication" if you have compatibility issues
   - Click "Next"

3. **Accounts and Roles:**
   - **Root Password:** Enter a strong password (remember this!)
     - Example: `hfqe72161954_` (or your preferred password)
   - **Confirm Password:** Re-enter the same password
   - **Root Account:** Keep "Create Windows Service" checked
   - Click "Next"

4. **Windows Service:**
   - **Windows Service Name:** Keep default `MySQL80`
   - **Start MySQL at System Startup:** ✅ Checked (recommended)
   - Click "Next"

5. **Apply Configuration:**
   - Click "Execute" to apply all settings
   - Wait for configuration to complete
   - Click "Finish"

## Step 4: Verify Installation

1. **Open Command Prompt** (as Administrator)
2. **Test MySQL:**
   ```cmd
   mysql --version
   ```
   Should show: `mysql Ver 8.x.x`

3. **Start MySQL Service** (if not running):
   ```cmd
   net start MySQL80
   ```

4. **Connect to MySQL:**
   ```cmd
   mysql -u root -p
   ```
   - Enter your root password when prompted
   - You should see: `mysql>`

## Step 5: Create Database for WellCo

1. **In MySQL Command Line** (after logging in):
   ```sql
   CREATE DATABASE wellco;
   ```
   
2. **Verify database was created:**
   ```sql
   SHOW DATABASES;
   ```
   You should see `wellco` in the list

3. **Use the database:**
   ```sql
   USE wellco;
   ```

4. **Exit MySQL:**
   ```sql
   EXIT;
   ```

## Step 6: Update Your Java Application

1. **Check DatabaseManager.java:**
   - Open: `database/DatabaseManager.java`
   - Verify these lines match your MySQL setup:
     ```java
     private static final String DB_URL = "jdbc:mysql://localhost:3306/wellco?serverTimezone=UTC";
     private static final String DB_USER = "root";
     private static final String DB_PASS = "hfqe72161954_";  // Your MySQL root password
     ```

2. **Download MySQL JDBC Driver:**
   - Go to: https://dev.mysql.com/downloads/connector/j/
   - Select "Platform Independent"
   - Download: `mysql-connector-j-8.x.x.jar` (ZIP file)
   - Extract the JAR file

3. **Add JDBC Driver to Your Project:**
   
   **Option A: If using an IDE (Eclipse/IntelliJ/NetBeans):**
   - Right-click your project → Properties/Build Path
   - Go to "Libraries" or "Dependencies"
   - Click "Add External JARs" or "Add JARs"
   - Select the `mysql-connector-j-8.x.x.jar` file
   - Click "OK"

   **Option B: If using command line:**
   - Copy `mysql-connector-j-8.x.x.jar` to your project folder
   - Compile with:
     ```cmd
     javac -cp ".;mysql-connector-j-8.x.x.jar" *.java database/*.java
     ```
   - Run with:
     ```cmd
     java -cp ".;mysql-connector-j-8.x.x.jar" MainFrame
     ```

## Step 7: Test Your Application

1. **Start MySQL Service** (if not already running):
   ```cmd
   net start MySQL80
   ```

2. **Run your Java application:**
   - The console should show: `✓ Connected to MySQL database successfully.`
   - If you see errors, check the console output for specific issues

3. **Test Signup:**
   - Try creating a new account
   - Check that `accounts.txt` file is created/updated
   - Verify account appears in the file

## Step 8: Verify Database Connection (Optional)

1. **Open MySQL Command Line:**
   ```cmd
   mysql -u root -p
   ```

2. **Check if table was created:**
   ```sql
   USE wellco;
   SHOW TABLES;
   ```
   Should show: `users`

3. **View accounts:**
   ```sql
   SELECT * FROM users;
   ```
   Should show any accounts you've created

4. **Exit:**
   ```sql
   EXIT;
   ```

## Troubleshooting

### MySQL Service Won't Start
```cmd
net start MySQL80
```
If it fails, check Windows Services:
- Press `Win + R`, type `services.msc`
- Find "MySQL80"
- Right-click → Start
- If it fails, check the error log

### Can't Connect from Java
- Verify MySQL is running: `net start MySQL80`
- Check firewall isn't blocking port 3306
- Verify password in DatabaseManager.java matches MySQL root password
- Check database name is correct: `wellco`

### JDBC Driver Not Found
- Ensure `mysql-connector-j-8.x.x.jar` is in your classpath
- Check the file name matches what you're using
- Try downloading the latest version

### Access Denied Error
- Verify username is `root` (or your MySQL username)
- Verify password matches your MySQL root password
- Try resetting MySQL root password if needed

## Quick Reference Commands

```cmd
# Start MySQL
net start MySQL80

# Stop MySQL
net stop MySQL80

# Connect to MySQL
mysql -u root -p

# Check MySQL version
mysql --version

# Check if MySQL is running
sc query MySQL80
```

## Next Steps

Once MySQL is installed and working:
1. ✅ Your application will automatically create the `users` table
2. ✅ Accounts will be saved to the database
3. ✅ `accounts.txt` will be automatically updated
4. ✅ You can create, view, and manage accounts

---

**Need Help?**
- MySQL Documentation: https://dev.mysql.com/doc/
- MySQL Community Forum: https://forums.mysql.com/

