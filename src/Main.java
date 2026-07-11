import models.Customer;
import models.Product;
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
}
}
