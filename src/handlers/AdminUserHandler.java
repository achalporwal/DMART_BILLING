package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.User;
import repositories.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminUserHandler implements HttpHandler {
private final UserRepository userRepository = new UserRepository();

@Override
public void handle(HttpExchange exchange) throws IOException {
String method = exchange.getRequestMethod();

if ("OPTIONS".equalsIgnoreCase(method)) {
HandlerUtils.handleCorsPreflight(exchange);
return;
}

if (!HandlerUtils.checkRole(exchange, "SUPER_ADMIN")) {
HandlerUtils.sendJsonResponse(exchange, 403, "{\"message\":\"Forbidden: Only SUPER_ADMIN can manage cashiers.\"}");
return;
}

try {
String path = exchange.getRequestURI().getPath();

if (path.endsWith("/reset-password")) {
if (!"POST".equalsIgnoreCase(method)) {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
return;
}
handleResetPassword(exchange);
} else {
if ("GET".equalsIgnoreCase(method)) {
handleGetUsers(exchange);
} else if ("POST".equalsIgnoreCase(method)) {
handleCreateOrUpdateUser(exchange);
} else if ("DELETE".equalsIgnoreCase(method)) {
handleDeleteUser(exchange);
} else {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
}
}
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
}
}

private void handleGetUsers(HttpExchange exchange) throws IOException {
List<User> cashiers = userRepository.findAllSubAdmins();
StringBuilder sb = new StringBuilder("[");
for (int i = 0; i < cashiers.size(); i++) {
User u = cashiers.get(i);
sb.append(String.format(
"{\"userId\":\"%s\",\"name\":\"%s\",\"role\":\"%s\",\"isActive\":%b}",
u.getUserId(), u.getName(), u.getRole(), u.isActive()
));
if (i < cashiers.size() - 1) sb.append(",");
}
sb.append("]");
HandlerUtils.sendJsonResponse(exchange, 200, sb.toString());
}

private void handleCreateOrUpdateUser(HttpExchange exchange) throws IOException {
String body = HandlerUtils.readRequestBody(exchange);
String userId = HandlerUtils.getJsonStringValue(body, "userId");
String name = HandlerUtils.getJsonStringValue(body, "name");
String password = HandlerUtils.getJsonStringValue(body, "password");

if (userId == null || name == null || password == null) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing required fields: userId, name, or password\"}");
return;
}

User user = new User(userId, name, "SUB_ADMIN", password);
User saved = userRepository.save(user);

if (saved != null) {
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Cashier profile saved successfully\"}");
} else {
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Failed to save cashier account\"}");
}
}

private void handleDeleteUser(HttpExchange exchange) throws IOException {
String query = exchange.getRequestURI().getQuery();
Map<String, String> params = HandlerUtils.parseQueryParams(query);
String id = params.get("id");

if (id == null || id.trim().isEmpty()) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing required parameter 'id'\"}");
return;
}

boolean deleted = userRepository.delete(id);
if (deleted) {
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Cashier account deleted successfully\"}");
} else {
HandlerUtils.sendJsonResponse(exchange, 404, "{\"message\":\"Cashier account not found or could not be deleted\"}");
}
}

private void handleResetPassword(HttpExchange exchange) throws IOException {
String body = HandlerUtils.readRequestBody(exchange);
String userId = HandlerUtils.getJsonStringValue(body, "userId");
String newPassword = HandlerUtils.getJsonStringValue(body, "newPassword");

if (userId == null || newPassword == null) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing required fields: userId or newPassword\"}");
return;
}

boolean reset = userRepository.resetPassword(userId, newPassword);
if (reset) {
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Cashier password reset successfully\"}");
} else {
HandlerUtils.sendJsonResponse(exchange, 404, "{\"message\":\"Cashier account not found\"}");
}
}
}
