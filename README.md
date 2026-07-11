# D-Mart Billing System (MySQL Edition)

A frameworkless, high-performance Java web application for D-Mart billing, fully migrated to a MySQL database with portable execution capabilities.

---

## 🚀 How to Run (on Any Windows PC)

We have created a self-healing startup script that handles database initialization, schema loading, and starting the web server automatically.

### Step 1: Prerequisites
Make sure the target PC has:
1. **Java JDK 11 or higher** installed. 
   - 📥 If not installed, download from: [Adoptium Temurin JDK 17 (Recommended) or JDK 25 (Latest LTS)](https://adoptium.net/temurin/releases/)
2. **MySQL Server** installed. 
   - 📥 If not installed, download from: [MySQL Community Installer](https://dev.mysql.com/downloads/installer/) (Select the MySQL Server option during installation).
   - (Note: If MySQL is installed but not running, our `start.bat` script will automatically attempt to launch it on port `3306` in the background).

### Step 2: How to Build and Run the Project

Follow these steps to compile and run the D-Mart Billing System locally using the Windows Command Prompt (CMD).

   1. Compile the Source Code
      Run the following command to compile all the Java files. This will compile the code and place the output `.class` files into the `bin` directory:

```cmd
javac -cp "mysql-connector-j-8.4.0.jar;slf4j-api.jar;slf4j-nop.jar" -d bin src\db\*.java src\models\*.java src\repositories\*.java src\services\*.java src\handlers\*.java src\Main.java src\DumpUsers.java

   2. Start the Application Server
      Once the code is compiled successfully, run the following command. This will set the necessary database environment variables and start the local web server:

set "DB_URL=jdbc:mysql://localhost:3306/dmart?useSSL=false&allowPublicKeyRetrieval=true" && set "DB_USER=root" && set "DB_PASSWORD=root" && java -cp "bin;mysql-connector-j-8.4.0.jar;slf4j-api.jar;slf4j-nop.jar" Main

Note: Ensure your local MySQL server is running on port 3306 before executing the server command.

Launch the web server on http://localhost:8080.
  
      
#### Step 3: Admin
   1. Username : admin1 Password : admin123
   2. Username : cashier1 Password : cashier123
   3. Username : cashier2 Password : cashier123
---

## 🛠️ File Structure

- **`start.bat`**: Portable startup script for Windows.
- **`start.sh`**: Portable startup script for Linux/macOS.
- **`mysql-connector-j-8.4.0.jar`**: MySQL JDBC connection driver.
- **`schema.sql`**: The MySQL database schema and seed data.
- **`src/`**: Java source code.
- **`web/`**: Frontend files (HTML, CSS, JS).
