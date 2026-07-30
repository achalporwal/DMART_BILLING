package services;
import db.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportingService 
{
public void printDailySalesReport() 
{
System.out.println("\n Fetching Daily Sales Report");
String query = "SELECT COUNT(bill_id) as total_bills, SUM(final_amount) as total_revenue FROM bills";
try(Connection conn = DatabaseConfig.getConnection();
PreparedStatement stmt = conn.prepareStatement(query);
ResultSet rs = stmt.executeQuery()) 
{
if (rs.next())
{
System.out.println("===================================");
System.out.println("         DMART SALES REPORT        ");
System.out.println("===================================");
System.out.println("Total Bills Generated: " + rs.getInt("total_bills"));
double revenue = rs.getDouble("total_revenue");
System.out.println("Total Revenue Earned : Rs " + revenue);
System.out.println("===================================");
}

}
catch (Exception e)
{
System.out.println("Report Generation Failed: " + e.getMessage());
}
}
}