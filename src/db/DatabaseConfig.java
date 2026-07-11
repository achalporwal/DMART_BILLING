package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
public class DatabaseConfig 
{
private static String dbUrl = "jdbc:mysql://localhost:3306/dmart?useSSL=false&allowPublicKeyRetrieval=true";
private static String dbUser = "root";
private static String dbPassword = "root";
private static String dbDriver = "com.mysql.cj.jdbc.Driver";
private static int poolSize = 10;
private static BlockingQueue<Connection> connectionPool;
private static final java.util.Set<Connection> allConnections = java.util.concurrent.ConcurrentHashMap.newKeySet();
static
{
String envUrl = System.getenv("DB_URL");
if (envUrl != null) dbUrl = envUrl;
String envUser = System.getenv("DB_USER");
if (envUser != null) dbUser = envUser;
String envPassword = System.getenv("DB_PASSWORD");
if (envPassword != null) dbPassword = envPassword;
String envDriver = System.getenv("DB_DRIVER");
if (envDriver != null) dbDriver = envDriver;
String envPool = System.getenv("DB_POOL_SIZE");
if (envPool != null)
{
try 
{
poolSize = Integer.parseInt(envPool);
}
catch (NumberFormatException ignored) 
{}
}
}
public static synchronized void initialize() throws SQLException 
{
if (connectionPool != null) return;
try 
{
Class.forName(dbDriver);
} 
catch (ClassNotFoundException e) 
{
System.err.println("JDBC Driver class not found: " + dbDriver);
}
connectionPool = new LinkedBlockingQueue<>(poolSize);
for (int i = 0; i < poolSize; i++) 
{
connectionPool.offer(createNewConnection());
}
System.out.println("JDBC Connection Pool initialized with size " + poolSize + " using URL: " + dbUrl);
}
private static Connection createNewConnection() throws SQLException 
{
Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
allConnections.add(conn);
return conn;
}
private static void closePermanently(Connection conn) 
{
if (conn == null) return;
allConnections.remove(conn);
try 
{
if (!conn.isClosed()) 
{
conn.close();
}
} 
catch (SQLException ignored) 
{}
}
public static Connection getConnection() throws SQLException 
{
if (connectionPool == null) 
{
initialize();
}
try 
{
Connection conn = connectionPool.take();
boolean isValid = false;
try 
{
isValid = (conn != null && !conn.isClosed() && conn.isValid(2));
} 
catch (SQLException e) 
{
isValid = false;
}
if (!isValid) 
{
if (conn != null) 
{
closePermanently(conn);
}
conn = createNewConnection();
}
return conn;
} 
catch (InterruptedException e) 
{
Thread.currentThread().interrupt();
throw new SQLException("Thread interrupted while waiting for database connection", e);
}
}
public static void releaseConnection(Connection conn) 
{
if (conn == null) return;
try 
{
if (connectionPool == null || conn.isClosed() || !conn.isValid(2)) 
{
closePermanently(conn);
return;
}
if (!connectionPool.offer(conn)) 
{
closePermanently(conn);
}
} 
catch (SQLException e) 
{
closePermanently(conn);
}
}
public static synchronized void shutdown() 
{
if (connectionPool == null) return;
connectionPool.clear();
connectionPool = null;
for (Connection conn : allConnections) 
{
try 
{
if (conn != null && !conn.isClosed()) 
{
conn.close();
}
} 
catch (SQLException e) 
{
System.err.println("Error closing connection during shutdown: " + e.getMessage());
}
}
allConnections.clear();
System.out.println("JDBC Connection Pool shutdown complete.");
}
}
