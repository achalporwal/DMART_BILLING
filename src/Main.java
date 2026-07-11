import models.Customer;
import models.Product;
import models.User;
import models.BillItem;
import java.math.BigDecimal;
public class Main
{
public static void main(String[] args)
{
Customer customer = new Customer("CUSTOMER-001", "ACHAL", "1234567890", 29, "UJJAIN");
System.out.println("ID: " +customer.getCustomerId());
System.out.println("Name: " + customer.getName());
System.out.println("Age: " +customer.getAge());
System.out.println("Location: " + customer.getLocation());
System.out.println("CUSTOMER OK");

Product product = new Product("PRODUCT-101", "Atta 5kg", new BigDecimal("250.00"), new BigDecimal("230.00"), new BigDecimal("5.00"), 50);
System.out.println("Name: " + product.getProductName());
System.out.println("MRP: Rs " + product.getMrp());
System.out.println("Dmart Price: Rs " + product.getPrp());
System.out.println("Stock Available: " + product.getAvailableQuantity());
System.out.println("Low Stock Alert At: " + product.getAlertThreshold());
System.out.println("PRODUCT OK");

User cashier = new User("User-001", "Amay", "Amay123", "Cashier1", true);
System.out.println("Name :" +cashier.getName());
System.out.println("Role :" +cashier.getRole());
System.out.println("Status :" +cashier.isActive());
System.out.println("User OK");

BillItem item1 = new BillItem("ITEM-001", "BILL-1001", "PRODUCT-101", 2, new BigDecimal("250.00"), new BigDecimal("230.00"), new BigDecimal("438.10"), new BigDecimal("10.95"), new BigDecimal("10.95"), new BigDecimal("40.00"), new BigDecimal("460.00"));
item1.setProductName("Atta 5Kg");
System.out.println("Product: " +item1.getProductName());
System.out.println("Quantity: " +item1.getQuantity());
System.out.println("Total Amount: " +item1.getFinalAmount());
System.out.println("BILL ITEM OK");

}
}
