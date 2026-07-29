package repositories;
import db.DatabaseConfig;
import models.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class ProductRepository
{
public boolean saveProduct(Product product) 
{
String query = "INSERT INTO products (product_id, product_name, mrp, prp, gst_percentage, available_quantity, alert_threshold, held_quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
try (Connection conn = DatabaseConfig.getConnection();
PreparedStatement stmt = conn.prepareStatement(query)) 
{
stmt.setString(1, product.getProductId());
stmt.setString(2, product.getProductName());
stmt.setBigDecimal(3, product.getMrp());
stmt.setBigDecimal(4, product.getPrp());
stmt.setBigDecimal(5, product.getGstPercentage());
stmt.setInt(6, product.getAvailableQuantity());
stmt.setInt(7, product.getAlertThreshold());
stmt.setInt(8, product.getHeldQuantity());
int rowsAffected = stmt.executeUpdate();
return rowsAffected > 0;
} 
catch (SQLException e)
{
System.out.println("Error saving product: " + e.getMessage());
return false;
}
}
public Product getProductById(String productId)
{
String query = "SELECT * FROM products WHERE product_id = ?";
try (Connection conn = DatabaseConfig.getConnection();
PreparedStatement stmt = conn.prepareStatement(query))
{
stmt.setString(1, productId);
ResultSet rs = stmt.executeQuery();
if (rs.next())
{
return new Product(rs.getString("product_id"), rs.getString("product_name"), rs.getBigDecimal("mrp"), rs.getBigDecimal("prp"), rs.getBigDecimal("gst_percentage"), rs.getInt("available_quantity"), rs.getInt("alert_threshold"), rs.getInt("held_quantity"));
}
}
catch (SQLException e)
{
System.out.println("Error fetching product: " + e.getMessage());
}
return null;
}
}