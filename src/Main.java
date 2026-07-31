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
import repositories.UserRepository;
import repositories.BillRepository;
import java.util.ArrayList;
import java.util.List;
import services.BillingService;
import services.ReportingService;
import services.ReturnService;
import handlers.HandlerUtils;
import handlers.AuthHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import handlers.ProductHandler;
import handlers.CustomerHandler;
import handlers.BillHandler;
import handlers.ReturnHandler;
import handlers.AdminReportHandler;
import handlers.CustomerUpdateHandler;
import handlers.DraftHandler;
import handlers.CartSyncHandler;


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

System.out.println("Billing System: User Repository Test");
UserRepository userRepo = new UserRepository();
System.out.println("Checking database connectivity\n");
String testUserId = "USR-001"; 
String testPassword = "admin123";
System.out.println("Trying to login with ID: " + testUserId);
User loggedInUser = userRepo.authenticateUser(testUserId, testPassword);
if (loggedInUser != null)
{
System.out.println("Login SUCCESS");
System.out.println("Welcome, " + loggedInUser.getName() + "!");
System.out.println("Your Role is: " + loggedInUser.getRole());
}
else
{
System.out.println("Login FAILED. User not exists or password is not correct.");
}

System.out.println("Billing System: Bill Repository Test");
BillItem dummyItem1 = new BillItem("ITM001", "BILL001", "PRODUCT-101", 2, new BigDecimal("250.00"), new BigDecimal("250.00"), new BigDecimal("500.00"),new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("500.00"));
Bill dummyBill = new Bill();
dummyBill.setBillId("BILL001");
dummyBill.setCustomerName("Rahul Kumar");
dummyBill.setCashierId("USR-001");
dummyBill.addItem(dummyItem1);
dummyBill.setFinalAmount(new BigDecimal("650.00"));
BillRepository billRepo = new BillRepository();
System.out.println("Trying to save bill in Database...");
boolean isBillSaved = billRepo.saveCompleteBill(dummyBill);
if (isBillSaved) {
System.out.println("SUCCESS: Bill and items successfully saved in database");
System.out.println("You can check 'bills' and 'bill_items' tables in database");
}
else
{
System.out.println("FAILED: Bill not saved");
System.out.println("Kindly check 'bills' and 'bill_items' tables exists in database or not.");
}

System.out.println(" ");
System.out.println(" ");
System.out.println(" ");
System.out.println(" ");
System.out.println("DMart Testing: Billing Service");
BillingService billingService = new BillingService();
String customerName = "Achal Porwal";
String cashierId = "USR-001";
String[] scannedProducts = {"PRODUCT-101"}; 
int[] itemQuantities = {2};
billingService.processCheckout(customerName, cashierId, scannedProducts, itemQuantities);



System.out.println("\n=== DMart Testing: Daily Report ===");
ReportingService reportService = new ReportingService();
reportService.printDailySalesReport();



System.out.println("\n=== DMart Testing: Return Service ===");
ReturnService returnService = new ReturnService();
returnService.processReturn("PRODUCT-101", 1); 


System.out.println("\n=== DMart Testing: HandlerUtils ===");
String dummyJsonRequest = "{\"userId\":\"admin123\", \"password\":\"pass123\"}";
System.out.println("Request from browser: " + dummyJsonRequest);
String extractedUserId = handlers.HandlerUtils.extractJsonValue(dummyJsonRequest, "userId");
String extractedPassword = handlers.HandlerUtils.extractJsonValue(dummyJsonRequest, "password");
System.out.println("Extracted User ID: " + extractedUserId);
System.out.println("Extracted Password: " + extractedPassword);



System.out.println("\n=== DMart Testing: Live Web Server (AuthHandler) ===");
try
{
System.out.println("Starting DMart Web Server on Port 8080...");
HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
server.createContext("/api/login", new AuthHandler());
server.createContext("/api/product", new ProductHandler());
server.createContext("/api/customer", new CustomerHandler());
server.createContext("/api/checkout", new BillHandler());
server.createContext("/api/return", new ReturnHandler());
server.createContext("/api/admin/report", new AdminReportHandler());
server.createContext("/api/customer/update", new CustomerUpdateHandler());
server.createContext("/api/draft", new DraftHandler());
server.createContext("/api/cart/sync", new CartSyncHandler());
server.setExecutor(null);
server.start();
System.out.println("Server started successfully!");
System.out.println("Waiting for API requests from Postman or CMD...");
}
catch(Exception e)
{
System.out.println("Server failed to start: " + e.getMessage());
}







}
}
