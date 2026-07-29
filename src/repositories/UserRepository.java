package repositories;
import models.User;
import db.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class UserRepository
{
public User authenticateUser(String userId, String password) 
{
String query = "SELECT * FROM users WHERE user_id = ? AND password = ? AND is_active = true";
try (Connection conn = DatabaseConfig.getConnection();
PreparedStatement stmt = conn.prepareStatement(query))
{
stmt.setString(1, userId);
stmt.setString(2, password);
ResultSet rs = stmt.executeQuery();
if (rs.next())
{
return new User(rs.getString("user_id"), rs.getString("name"), rs.getString("password"), rs.getString("role"), rs.getBoolean("is_active"));
}
} 
catch (Exception e)
{
System.out.println("User Login Error: " + e.getMessage());
}
return null;
}
}