import models.Customer;
import models.Product;
import models.User;
import models.BillItem;
import models.Bill;
import java.math.BigDecimal;
import db.DatabaseConfig;
import java.sql.Connection;
import repositories.CustomerRepository;
import repositories.ProductRepository;

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

Bill bill = new Bill();
bill.setBillId("Bill-1001");
bill.setCustomerName("Achal");
bill.setCashierId(cashier.getUserId());
bill.addItem(item1);
bill.setFinalAmount(new BigDecimal("460.00"));
System.out.println("Customer Name: " +bill.getCustomerName());
System.out.println("Cahier ID: " +bill.getCashierId());
System.out.println("Bill OK");

System.out.println("Checking Database Connection\n");
try
{
Connection conn = DatabaseConfig.getConnection();
if (conn != null) 
{
System.out.println("Java successfully connected");
conn.close();
}
} 
catch(Exception e) 
{
System.out.println("Connection failed");
System.out.println("Reason: " + e.getMessage());
}

System.out.println("Testing Database Repositories\n");
CustomerRepository customerRepo = new CustomerRepository();
Customer newCustomer = new Customer("CUST-1001", "Rahul Sharma", "9876543210", 25, "Ujjain");
System.out.println("Customer Saved.");
boolean isSaved = customerRepo.saveCustomer(newCustomer);
if (isSaved)
{
System.out.println("Customer saved in MySQL database.");
}
else
{
System.out.println("Customer not saved/already exists");
}
System.out.println("\nFetching customer from Database.");
Customer dbCustomer=customerRepo.getCustomerByMobile("9876543210");
if (dbCustomer != null)
{
System.out.println("Customer found");
System.out.println("Name: " + dbCustomer.getName());
System.out.println("ID: " + dbCustomer.getCustomerId());
}
else
{
System.out.println("Customer Not found");
}

System.out.println("\nTesting Product Repository...");
ProductRepository productRepo = new ProductRepository();
boolean isProdSaved = productRepo.saveProduct(product);
if(isProdSaved) 
{
System.out.println("Product 'Atta 5kg' saved to Database!");
}
else
{
System.out.println("Product not saved/already exists");
}
System.out.println("\nFetching product from Database by ID...");
Product dbProduct = productRepo.getProductById("PRODUCT-101");
if (dbProduct != null)
{
System.out.println("Product found in DB.");
System.out.println("Scanned Name: " + dbProduct.getProductName());
System.out.println("Scanned Dmart Price: Rs " + dbProduct.getPrp());
System.out.println("Current Stock: " + dbProduct.getAvailableQuantity());
}
else
{
System.out.println("Product not found.");
}





}
}
