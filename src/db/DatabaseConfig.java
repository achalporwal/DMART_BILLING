package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseConfig
{
private static final String DB_URL = "jdbc:mysql://localhost:3306/dmart_db";
private static final String USERNAME = "root";
private static final String PASSWORD = ""; 
public static Connection getConnection() throws SQLException 
{
try
{
Class.forName("com.mysql.cj.jdbc.Driver");
}
catch(ClassNotFoundException e)
{
System.out.println("Jar file not found");
e.printStackTrace();
}
return DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
}
}