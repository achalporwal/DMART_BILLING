package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Customer;
import repositories.CustomerRepository;

import java.io.IOException;

public class CustomerUpdateHandler implements HttpHandler {
private final CustomerRepository customerRepository = new CustomerRepository();

@Override
public void handle(HttpExchange exchange) throws IOException {
String method = exchange.getRequestMethod();

if ("OPTIONS".equalsIgnoreCase(method)) {
HandlerUtils.handleCorsPreflight(exchange);
return;
}

if (!"POST".equalsIgnoreCase(method)) {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
return;
}

try {
String body = HandlerUtils.readRequestBody(exchange);
String customerId = HandlerUtils.getJsonStringValue(body, "customerId");
String name = HandlerUtils.getJsonStringValue(body, "name");
String mobileNumber = HandlerUtils.getJsonStringValue(body, "mobileNumber");
int age = HandlerUtils.getJsonIntValue(body, "age", 0);
String location = HandlerUtils.getJsonStringValue(body, "location");

if (customerId == null || customerId.trim().isEmpty() || name == null || mobileNumber == null) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing required fields: customerId, name, or mobileNumber\"}");
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

Customer customer = new Customer(customerId, name, mobileNumber, age, location);
customerRepository.update(customer);

String json = String.format(
"{\"status\":\"success\",\"customer\":{\"customerId\":\"%s\",\"name\":\"%s\",\"mobileNumber\":\"%s\",\"age\":%d,\"location\":\"%s\"}}",
customerId, name, mobileNumber, age, location
);
HandlerUtils.sendJsonResponse(exchange, 200, json);

} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal server error: " + e.getMessage() + "\"}");
}
}
}
