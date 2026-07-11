package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Customer;
import repositories.CustomerRepository;

import java.io.IOException;
import java.util.Map;

public class CustomerHandler implements HttpHandler {
private final CustomerRepository customerRepository = new CustomerRepository();

@Override
public void handle(HttpExchange exchange) throws IOException {
String method = exchange.getRequestMethod();

if ("OPTIONS".equalsIgnoreCase(method)) {
HandlerUtils.handleCorsPreflight(exchange);
return;
}

try {
if ("GET".equalsIgnoreCase(method)) {
String query = exchange.getRequestURI().getQuery();
Map<String, String> params = HandlerUtils.parseQueryParams(query);
String mobile = params.get("mobile");
String id = params.get("id");

if ((mobile == null || mobile.trim().isEmpty()) && (id == null || id.trim().isEmpty())) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Query parameter 'mobile' or 'id' is required\"}");
return;
}

Customer customer = null;
if (id != null && !id.trim().isEmpty()) {
customer = customerRepository.findById(id.trim());
} else if (mobile != null && !mobile.trim().isEmpty()) {
customer = customerRepository.findByMobileNumber(mobile.trim());
}

if (customer != null) {
repositories.BillRepository billRepository = new repositories.BillRepository();
repositories.BillRepository.PurchaseStats stats = billRepository.getPurchaseStats(customer.getCustomerId());
String json = String.format(
"{\"status\":\"found\",\"customer\":{\"customerId\":\"%s\",\"name\":\"%s\",\"mobileNumber\":\"%s\",\"age\":%d,\"location\":\"%s\"," +
"\"monthQty\":%d,\"monthAmount\":%.2f,\"fyQty\":%d,\"fyAmount\":%.2f}}",
customer.getCustomerId(), customer.getName(), customer.getMobileNumber(), customer.getAge(), customer.getLocation(),
stats.monthQty, stats.monthAmount, stats.fyQty, stats.fyAmount
);
HandlerUtils.sendJsonResponse(exchange, 200, json);
} else {
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"not_found\"}");
}
} else if ("POST".equalsIgnoreCase(method)) {
String body = HandlerUtils.readRequestBody(exchange);
String customerId = HandlerUtils.getJsonStringValue(body, "customerId");
String name = HandlerUtils.getJsonStringValue(body, "name");
String mobileNumber = HandlerUtils.getJsonStringValue(body, "mobileNumber");
int age = HandlerUtils.getJsonIntValue(body, "age", 0);
String location = HandlerUtils.getJsonStringValue(body, "location");

if (name == null || mobileNumber == null) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing required fields: name or mobileNumber\"}");
return;
}

String ageStr = HandlerUtils.getJsonRawValue(body, "age");
if (ageStr != null && !ageStr.trim().isEmpty()) {
try {
if (ageStr.contains(".")) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Age cannot contain decimals.\"}");
return;
}
int parsedAge = Integer.parseInt(ageStr.trim());
if (parsedAge <= 0 || parsedAge > 150) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Age must be a positive integer not exceeding 150 years.\"}");
return;
}
age = parsedAge;
} catch (NumberFormatException e) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Age must be a valid integer.\"}");
return;
}
}

if (customerId != null && !customerId.trim().isEmpty()) {
Customer customer = new Customer(customerId, name, mobileNumber, age, location);
customerRepository.update(customer);
String json = String.format(
"{\"status\":\"success\",\"customer\":{\"customerId\":\"%s\",\"name\":\"%s\",\"mobileNumber\":\"%s\",\"age\":%d,\"location\":\"%s\"}}",
customerId, name, mobileNumber, age, location
);
HandlerUtils.sendJsonResponse(exchange, 200, json);
return;
}

Customer existing = customerRepository.findByMobileNumber(mobileNumber);
if (existing != null) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Customer with this mobile number already exists\"}");
return;
}

Customer customer = new Customer(null, name, mobileNumber, age, location);
Customer saved = customerRepository.save(customer);

if (saved != null) {
String json = String.format(
"{\"status\":\"success\",\"customer\":{\"customerId\":\"%s\",\"name\":\"%s\",\"mobileNumber\":\"%s\",\"age\":%d,\"location\":\"%s\"}}",
saved.getCustomerId(), saved.getName(), saved.getMobileNumber(), saved.getAge(), saved.getLocation()
);
HandlerUtils.sendJsonResponse(exchange, 201, json);
} else {
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Failed to save customer\"}");
}
} else {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
}
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal server error: " + e.getMessage() + "\"}");
}
}
}
