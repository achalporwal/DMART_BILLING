package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Bill;
import models.BillItem;
import repositories.BillRepository;
import services.BillingService;
import services.BillingService.PurchaseItemRequest;
import services.ReturnService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BillHandler implements HttpHandler {
private final BillingService billingService = new BillingService();
private final ReturnService returnService = new ReturnService();
private final BillRepository billRepository = new BillRepository();

@Override
public void handle(HttpExchange exchange) throws IOException {
String method = exchange.getRequestMethod();

if ("OPTIONS".equalsIgnoreCase(method)) {
HandlerUtils.handleCorsPreflight(exchange);
return;
}

try {
if ("POST".equalsIgnoreCase(method)) {
String path = exchange.getRequestURI().getPath();
if ("/api/bill/return".equalsIgnoreCase(path)) {
String body = HandlerUtils.readRequestBody(exchange);
String billId = HandlerUtils.getJsonStringValue(body, "billId");
String productId = HandlerUtils.getJsonStringValue(body, "productId");
int quantity = HandlerUtils.getJsonIntValue(body, "quantity", 0);

if (billId == null || productId == null || quantity <= 0) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing required fields: billId, productId, or valid quantity\"}");
return;
}

try {
Bill updatedBill = returnService.processReturn(billId, productId, quantity);
String billJson = serializeBill(updatedBill);
HandlerUtils.sendJsonResponse(exchange, 200, billJson);
} catch (Exception e) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Return failed: " + e.getMessage() + "\"}");
}
return;
}

String cashierId = HandlerUtils.getUserId(exchange);
if (cashierId == null || cashierId.trim().isEmpty()) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing cashier authentication details (X-User-Id header)\"}");
return;
}

String body = HandlerUtils.readRequestBody(exchange);
String customerId = HandlerUtils.getJsonStringValue(body, "customerId");
String mobile = HandlerUtils.getJsonStringValue(body, "customerMobile");
String name = HandlerUtils.getJsonStringValue(body, "customerName");
int age = HandlerUtils.getJsonIntValue(body, "customerAge", 0);
String location = HandlerUtils.getJsonStringValue(body, "customerLocation");
String paymentMode = HandlerUtils.getJsonStringValue(body, "paymentMode");
java.math.BigDecimal cashReceived = HandlerUtils.getJsonBigDecimalValue(body, "cashReceived", java.math.BigDecimal.ZERO);
java.math.BigDecimal cashReturned = HandlerUtils.getJsonBigDecimalValue(body, "cashReturned", java.math.BigDecimal.ZERO);

if (mobile == null || name == null) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing customer details (mobile or name)\"}");
return;
}

List<PurchaseItemRequest> itemsRequest = new ArrayList<>();
int itemsIdx = body.indexOf("\"items\"");
if (itemsIdx != -1) {
int arrStart = body.indexOf("[", itemsIdx);
int arrEnd = body.indexOf("]", arrStart);
if (arrStart != -1 && arrEnd != -1) {
String arrayContent = body.substring(arrStart + 1, arrEnd);
// Split by objects
String[] objects = arrayContent.split("\\},\\s*\\{");
for (String obj : objects) {
String clean = obj.replace("{", "").replace("}", "").trim();
if (!clean.isEmpty()) {
String prodId = HandlerUtils.getJsonStringValue("{" + clean + "}", "productId");
int qty = HandlerUtils.getJsonIntValue("{" + clean + "}", "quantity", 1);
if (prodId != null) {
itemsRequest.add(new PurchaseItemRequest(prodId, qty));
}
}
}
}
}

try {
Bill generatedBill = billingService.generateBill(customerId, mobile, name, age, location, itemsRequest, cashierId, paymentMode, cashReceived, cashReturned);
String billJson = serializeBill(generatedBill);
HandlerUtils.sendJsonResponse(exchange, 201, billJson);
} catch (Exception e) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Transaction failed: " + e.getMessage() + "\"}");
}

} else if ("GET".equalsIgnoreCase(method)) {
String query = exchange.getRequestURI().getQuery();
Map<String, String> params = HandlerUtils.parseQueryParams(query);

String searchId = params.get("id");
if (searchId != null && !searchId.trim().isEmpty()) {
Bill bill = billRepository.findById(searchId.trim().toUpperCase());
if (bill != null) {
bill.setItems(billRepository.findItemsByBillId(bill.getBillId()));
String billJson = serializeBill(bill);
HandlerUtils.sendJsonResponse(exchange, 200, billJson);
} else {
HandlerUtils.sendJsonResponse(exchange, 404, "{\"message\":\"Invoice not found\"}");
}
return;
}


String cashierFilter = params.get("cashierId");
String dateParam     = params.get("date");
String monthParam    = params.get("month");
String yearParam     = params.get("year");
List<Bill> bills;

if (cashierFilter != null && !cashierFilter.trim().isEmpty()) {
LocalDate today = LocalDate.now();
LocalDateTime start = today.atStartOfDay();
LocalDateTime end   = today.atTime(LocalTime.MAX);
bills = billRepository.findByCashierAndDate(cashierFilter, start, end);

} else if (dateParam != null && !dateParam.trim().isEmpty()) {
try {
LocalDate date = LocalDate.parse(dateParam.trim());
LocalDateTime start = date.atStartOfDay();
LocalDateTime end   = date.atTime(LocalTime.MAX);
bills = billRepository.findByDateRange(start, end);
} catch (Exception e) {
bills = billRepository.findAll();
}

} else if (monthParam != null && yearParam != null) {
try {
int month = Integer.parseInt(monthParam.trim());
int year  = Integer.parseInt(yearParam.trim());
LocalDate firstDay = LocalDate.of(year, month, 1);
LocalDate lastDay  = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
LocalDateTime start = firstDay.atStartOfDay();
LocalDateTime end   = lastDay.atTime(LocalTime.MAX);
bills = billRepository.findByDateRange(start, end);
} catch (Exception e) {
bills = billRepository.findAll();
}

} else {
bills = billRepository.findAll();
}

StringBuilder sb = new StringBuilder("[");
for (int i = 0; i < bills.size(); i++) {
Bill b = bills.get(i);
b.setItems(billRepository.findItemsByBillId(b.getBillId()));
sb.append(serializeBill(b));
if (i < bills.size() - 1) sb.append(",");
}
sb.append("]");
HandlerUtils.sendJsonResponse(exchange, 200, sb.toString());
} else {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
}
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
}
}

private String serializeBill(Bill bill) {
StringBuilder sb = new StringBuilder();
sb.append("{");
sb.append(String.format("\"billId\":\"%s\",", bill.getBillId()));
sb.append(String.format("\"customerId\":\"%s\",", bill.getCustomerId() == null ? "" : bill.getCustomerId()));
sb.append(String.format("\"customerName\":\"%s\",", bill.getCustomerName() == null ? "Walk-in Customer" : bill.getCustomerName().replace("\"", "\\\"")));
sb.append(String.format("\"customerMobile\":\"%s\",", bill.getCustomerMobile() == null ? "" : bill.getCustomerMobile()));
sb.append(String.format("\"customerLocation\":\"%s\",", bill.getCustomerLocation() == null ? "" : bill.getCustomerLocation().replace("\"", "\\\"")));
sb.append(String.format("\"cashierId\":\"%s\",", bill.getCashierId()));
sb.append(String.format("\"billDate\":\"%s\",", bill.getBillDate().toString()));
sb.append(String.format("\"taxableValue\":%s,", bill.getTaxableValue().toString()));
sb.append(String.format("\"cgst\":%s,", bill.getCgst().toString()));
sb.append(String.format("\"sgst\":%s,", bill.getSgst().toString()));
sb.append(String.format("\"discount\":%s,", bill.getDiscount().toString()));
sb.append(String.format("\"finalAmount\":%s,", bill.getFinalAmount().toString()));
sb.append(String.format("\"paymentMode\":\"%s\",", bill.getPaymentMode() == null ? "CASH" : bill.getPaymentMode()));
sb.append(String.format("\"cashReceived\":%s,", bill.getCashReceived() == null ? "0.00" : bill.getCashReceived().toString()));
sb.append(String.format("\"cashReturned\":%s,", bill.getCashReturned() == null ? "0.00" : bill.getCashReturned().toString()));
sb.append(String.format("\"status\":\"%s\",", bill.getStatus() == null ? "COMPLETED" : bill.getStatus()));

sb.append("\"items\":[");
List<BillItem> items = bill.getItems();
for (int i = 0; i < items.size(); i++) {
BillItem item = items.get(i);
sb.append(String.format(
"{\"billItemId\":\"%s\",\"productId\":\"%s\",\"productName\":\"%s\",\"quantity\":%d,\"mrp\":%s,\"prp\":%s,\"taxableValue\":%s,\"cgst\":%s,\"sgst\":%s,\"discount\":%s,\"finalAmount\":%s}",
item.getBillItemId(), item.getProductId(), 
item.getProductName() == null ? "" : item.getProductName().replace("\"", "\\\""),
item.getQuantity(),
item.getMrp().toString(), item.getPrp().toString(), item.getTaxableValue().toString(),
item.getCgst().toString(), item.getSgst().toString(), item.getDiscount().toString(), item.getFinalAmount().toString()
));
if (i < items.size() - 1) sb.append(",");
}
sb.append("]");
sb.append("}");
return sb.toString();
}
}
