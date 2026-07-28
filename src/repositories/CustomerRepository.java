package repositories;
import db.DatabaseConfig;
import models.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class CustomerRepository
{
public boolean saveCustomer(Customer customer)
{
String query = "INSERT INTO customers (customer_id, name, mobile_number, age, location) VALUES (?, ?, ?, ?, ?)";
try (Connection conn = DatabaseConfig.getConnection();
PreparedStatement stmt = conn.prepareStatement(query))
{
stmt.setString(1, customer.getCustomerId());
stmt.setString(2, customer.getName());
stmt.setString(3, customer.getMobileNumber());
stmt.setInt(4, customer.getAge());
stmt.setString(5, customer.getLocation());
int rowsAffected = stmt.executeUpdate();
return rowsAffected > 0;
}
catch (SQLException e)
{
System.out.println("Error saving customer: " + e.getMessage());
return false;
}
}
public Customer getCustomerByMobile(String mobileNumber)
{
String query = "SELECT * FROM customers WHERE mobile_number = ?";
try(Connection conn = DatabaseConfig.getConnection();
PreparedStatement stmt = conn.prepareStatement(query))
{
stmt.setString(1, mobileNumber);
ResultSet rs = stmt.executeQuery();
if (rs.next())
{
return new Customer(rs.getString("customer_id"), rs.getString("name"), rs.getString("mobile_number"), rs.getInt("age"), rs.getString("location"));
}
}
catch(SQLException e)
{
System.out.println("Error fetching customer: " + e.getMessage());
}
return null;
}
}