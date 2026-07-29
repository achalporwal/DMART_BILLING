package repositories;
import models.Bill;
import models.BillItem;
import db.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
public class BillRepository
{
public boolean saveCompleteBill(Bill bill)
{
String billQuery = "INSERT INTO bills (bill_id, customer_name, cashier_id, final_amount) VALUES (?, ?, ?, ?)";
String itemQuery = "INSERT INTO bill_items (item_id, bill_id, product_id, quantity, final_amount) VALUES (?, ?, ?, ?, ?)";
try (Connection conn = DatabaseConfig.getConnection())
{
conn.setAutoCommit(false); 
try (PreparedStatement billStmt = conn.prepareStatement(billQuery);
PreparedStatement itemStmt = conn.prepareStatement(itemQuery))
{
billStmt.setString(1, bill.getBillId());
billStmt.setString(2, bill.getCustomerName());
billStmt.setString(3, bill.getCashierId());
billStmt.setBigDecimal(4, bill.getFinalAmount());
billStmt.executeUpdate();
for (BillItem item : bill.getItems())
{
itemStmt.setString(1, item.getItemId());
itemStmt.setString(2, bill.getBillId());
itemStmt.setString(3, item.getProductId());
itemStmt.setInt(4, item.getQuantity());
itemStmt.setBigDecimal(5, item.getFinalAmount());
itemStmt.executeUpdate();
}
conn.commit();
return true;
}
catch (Exception e)
{
conn.rollback();
System.out.println("Transaction Failed, Bill Cancelled: " + e.getMessage());
return false;
}
}
catch (Exception e)
{
System.out.println("Database Error during Billing: " + e.getMessage());
return false;
}
}
}