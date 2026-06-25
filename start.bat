@echo off
setlocal enabledelayedexpansion
title D-Mart Billing System

echo ===================================================
echo    D-Mart Billing System - Portable Launcher
echo ===================================================
echo.

rem --- Step 1: Check Java ---
java -version >nul 2>&1
if errorlevel 1 (
echo [ERROR] Java is not installed or not in PATH.
echo         Please install JDK 11 or higher: https://adoptium.net/
pause
exit /b 1
)
echo [OK] Java detected.

rem --- Step 2: Check and start MySQL database ---
netstat -ano | findstr :3306 | findstr LISTENING >nul 2>&1
if errorlevel 1 (
echo [INFO] MySQL is not running on port 3306. Attempting to start local instance...

rem Look for local or installed MySQL
set "MYSQLD_PATH=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
if not exist "!MYSQLD_PATH!" (
echo [ERROR] MySQL server binary not found at: !MYSQLD_PATH!
echo         Please install MySQL or ensure a MySQL instance is running on port 3306.
pause
exit /b 1
)

rem Initialize data directory if it doesn't exist
if not exist "mysql-data" (
echo [INFO] Initializing new database data directory...
mkdir mysql-data
"!MYSQLD_PATH!" --initialize-insecure --datadir="%cd%\mysql-data"
if errorlevel 1 (
echo [ERROR] Failed to initialize database data directory.
pause
exit /b 1
)
)

rem Start MySQL daemon in the background
echo [INFO] Starting MySQL database server in background...
start /b "" "!MYSQLD_PATH!" --datadir="%cd%\mysql-data" --port=3306 --console >mysql_server.log 2>&1

rem Wait for MySQL to start
echo [INFO] Waiting for database to start...
:wait_loop
timeout /t 2 >nul
netstat -ano | findstr :3306 >nul 2>&1
if errorlevel 1 (
goto wait_loop
)
echo [OK] Database started successfully.

rem Configure password and create DB
echo [INFO] Configuring database schema...
set "MYSQL_CLI_PATH=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
if exist "!MYSQL_CLI_PATH!" (
"!MYSQL_CLI_PATH!" -u root -e "CREATE DATABASE IF NOT EXISTS dmart;" >nul 2>&1
"!MYSQL_CLI_PATH!" -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root'; FLUSH PRIVILEGES;" >nul 2>&1
)
) else (
echo [OK] MySQL is already running on port 3306.
)

rem --- Step 3: Compile Java sources ---
echo.
echo [STEP] Compiling Java source files...
if not exist "bin" mkdir bin
javac -cp "mysql-connector-j-8.4.0.jar;slf4j-api.jar;slf4j-nop.jar" -d bin src\db\*.java src\models\*.java src\repositories\*.java src\services\*.java src\handlers\*.java src\Main.java src\DumpUsers.java
if errorlevel 1 (
echo [ERROR] Compilation failed.
pause
exit /b 1
)
echo [OK] Compilation successful.

rem --- Step 4: Start the server ---
echo.
echo [STEP] Starting D-Mart Billing HTTP Server...
echo.
echo  Access the app at:  http://localhost:8080
echo  Press Ctrl+C to stop the server.
echo.

set DB_URL=jdbc:mysql://localhost:3306/dmart?useSSL=false^&allowPublicKeyRetrieval=true
set DB_DRIVER=com.mysql.cj.jdbc.Driver
set DB_USER=root
set DB_PASSWORD=root

java -cp "bin;mysql-connector-j-8.4.0.jar;slf4j-api.jar;slf4j-nop.jar" Main

pause
