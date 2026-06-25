package services;

import db.DatabaseConfig;
import models.Bill;
import models.BillItem;
import models.Customer;
import models.Product;
import repositories.BillRepository;
import repositories.CustomerRepository;
import repositories.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BillingService {
private final BillRepository billRepository = new BillRepository();

public static class PurchaseItemRequest {
public String productId;
public int quantity;

public PurchaseItemRequest() {}

public PurchaseItemRequest(String productId, int quantity) {
this.productId = productId;
this.quantity = quantity;
}
}

public Bill generateBill(String customerId, String customerMobile, String customerName, int customerAge, String customerLocation,
List<PurchaseItemRequest> itemsRequest, String cashierId, String paymentMode,
BigDecimal cashReceived, BigDecimal cashReturned) throws Exception {

if (itemsRequest == null || itemsRequest.isEmpty()) {
throw new Exception("No products scanned for this transaction.");
}

Connection conn = null;
try {
conn = DatabaseConfig.getConnection();
conn.setAutoCommit(false);

Customer customer = null;
CustomerRepository custRepo = new CustomerRepository();

if (customerId != null && !customerId.trim().isEmpty()) {
customer = findCustomerById(customerId, conn);
if (customer != null) {
if (!customer.getMobileNumber().equals(customerMobile)) {
Customer col = findCustomerByMobile(customerMobile, conn);
if (col != null && !col.getCustomerId().equals(customerId)) {
throw new Exception("Mobile number " + customerMobile + " is already registered to another customer.");
}
}
customer.setName(customerName);
customer.setMobileNumber(customerMobile);
customer.setAge(customerAge);
customer.setLocation(customerLocation);
custRepo.update(customer, conn);
}
}

if (customer == null) {
customer = findCustomerByMobile(customerMobile, conn);
if (customer != null) {
customer.setName(customerName);
customer.setAge(customerAge);
customer.setLocation(customerLocation);
custRepo.update(customer, conn);
} else {
String newCustId = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
customer = new Customer(newCustId, customerName, customerMobile, customerAge, customerLocation);
saveCustomer(customer, conn);
}
}

String billId = "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

BigDecimal totalTaxable = BigDecimal.ZERO;
BigDecimal totalCgst = BigDecimal.ZERO;
BigDecimal totalSgst = BigDecimal.ZERO;
BigDecimal totalDiscount = BigDecimal.ZERO;
BigDecimal totalFinal = BigDecimal.ZERO;

Bill bill = new Bill();
bill.setBillId(billId);
bill.setCustomerId(customer.getCustomerId());
bill.setCashierId(cashierId);
bill.setBillDate(LocalDateTime.now());
bill.setPaymentMode(paymentMode == null ? "CASH" : paymentMode);
bill.setCashReceived(cashReceived == null ? BigDecimal.ZERO : cashReceived);
bill.setCashReturned(cashReturned == null ? BigDecimal.ZERO : cashReturned);
bill.setStatus("COMPLETED");

for (PurchaseItemRequest req : itemsRequest) {
Product product = findProductById(req.productId, conn);
if (product == null) {
throw new Exception("Product ID " + req.productId + " does not exist in inventory.");
}
if (product.getAvailableQuantity() < req.quantity) {
throw new Exception("Insufficient stock for '" + product.getProductName() +
"'. Available: " + product.getAvailableQuantity() +
", Requested: " + req.quantity);
}

boolean updated = deductProductQuantity(req.productId, req.quantity, conn);
if (!updated) {
throw new Exception("Failed to update inventory for '" + product.getProductName() + "'.");
}

BigDecimal qty = BigDecimal.valueOf(req.quantity);
BigDecimal mrp = product.getMrp();
BigDecimal prp = product.getPrp();
BigDecimal gstPct = product.getGstPercentage();

BigDecimal finalAmount = prp.multiply(qty).setScale(2, RoundingMode.HALF_UP);

BigDecimal onePlusGstDiv100 = BigDecimal.ONE.add(gstPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
BigDecimal taxableValue = finalAmount.divide(onePlusGstDiv100, 2, RoundingMode.HALF_UP);

BigDecimal totalGst = finalAmount.subtract(taxableValue);
BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
BigDecimal sgst = totalGst.subtract(cgst); // ensure no rounding pennies are lost

BigDecimal discount = mrp.multiply(qty).subtract(finalAmount).setScale(2, RoundingMode.HALF_UP);

totalTaxable  = totalTaxable.add(taxableValue);
totalCgst     = totalCgst.add(cgst);
totalSgst     = totalSgst.add(sgst);
totalDiscount = totalDiscount.add(discount);
totalFinal    = totalFinal.add(finalAmount);

String itemId = "BITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
BillItem item = new BillItem(itemId, billId, req.productId, req.quantity,
mrp, prp, taxableValue, cgst, sgst, discount, finalAmount);
item.setProductName(product.getProductName());

bill.addItem(item);
}

bill.setTaxableValue(totalTaxable);
bill.setCgst(totalCgst);
bill.setSgst(totalSgst);
bill.setDiscount(totalDiscount);
bill.setFinalAmount(totalFinal);
bill.setCustomerName(customer.getName());
bill.setCustomerMobile(customer.getMobileNumber());
bill.setCustomerLocation(customer.getLocation());

billRepository.save(bill, conn);

for (BillItem item : bill.getItems()) {
billRepository.saveItem(item, conn);
}

conn.commit();
return bill;

} catch (Exception e) {
if (conn != null) {
try { conn.rollback(); } catch (SQLException ex) {
System.err.println("Rollback failed: " + ex.getMessage());
}
}
throw e;
} finally {
if (conn != null) {
try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
DatabaseConfig.releaseConnection(conn);
}
}
}

private Customer findCustomerById(String customerId, Connection conn) throws SQLException {
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
pstmt = conn.prepareStatement("SELECT * FROM customers WHERE customer_id = ?");
pstmt.setString(1, customerId);
rs = pstmt.executeQuery();
if (rs.next()) {
return new Customer(
rs.getString("customer_id"),
rs.getString("name"),
rs.getString("mobile_number"),
rs.getInt("age"),
rs.getString("location")
);
}
return null;
} finally {
if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
}
}


private Customer findCustomerByMobile(String mobile, Connection conn) throws SQLException {
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
pstmt = conn.prepareStatement("SELECT * FROM customers WHERE mobile_number = ?");
pstmt.setString(1, mobile);
rs = pstmt.executeQuery();
if (rs.next()) {
return new Customer(
rs.getString("customer_id"),
rs.getString("name"),
rs.getString("mobile_number"),
rs.getInt("age"),
rs.getString("location")
);
}
return null;
} finally {
if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
}
}

private void saveCustomer(Customer c, Connection conn) throws SQLException {
PreparedStatement pstmt = null;
try {
pstmt = conn.prepareStatement(
"INSERT INTO customers (customer_id, name, mobile_number, age, location) VALUES (?, ?, ?, ?, ?)");
pstmt.setString(1, c.getCustomerId());
pstmt.setString(2, c.getName());
pstmt.setString(3, c.getMobileNumber());
pstmt.setInt(4, c.getAge());
pstmt.setString(5, c.getLocation());
pstmt.executeUpdate();
} finally {
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
}
}

private Product findProductById(String productId, Connection conn) throws SQLException {
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
pstmt = conn.prepareStatement("SELECT * FROM products WHERE product_id = ?");
pstmt.setString(1, productId);
rs = pstmt.executeQuery();
if (rs.next()) {
return new Product(
rs.getString("product_id"),
rs.getString("product_name"),
rs.getBigDecimal("mrp"),
rs.getBigDecimal("prp"),
rs.getBigDecimal("gst_percentage"),
rs.getInt("available_quantity")
);
}
return null;
} finally {
if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
}
}

private boolean deductProductQuantity(String productId, int qty, Connection conn) throws SQLException {
PreparedStatement pstmt = null;
try {
pstmt = conn.prepareStatement(
"UPDATE products SET available_quantity = available_quantity - ? WHERE product_id = ? AND available_quantity >= ?");
pstmt.setInt(1, qty);
pstmt.setString(2, productId);
pstmt.setInt(3, qty);
return pstmt.executeUpdate() > 0;
} finally {
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
}
}
}
