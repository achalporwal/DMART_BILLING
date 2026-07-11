package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.User;
import repositories.UserRepository;

import java.io.IOException;

public class AuthHandler implements HttpHandler {
private final UserRepository userRepository = new UserRepository();

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
String userId = HandlerUtils.getJsonStringValue(body, "userId");
String password = HandlerUtils.getJsonStringValue(body, "password");

if (userId == null || password == null) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing userId or password\"}");
return;
}

User user = userRepository.findById(userId);
if (user != null && user.getPassword().equals(password)) {
if (!user.isActive()) {
HandlerUtils.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Account Inactive. Contact Head Admin.\"}");
return;
}
String responseJson = String.format(
"{\"status\":\"success\",\"user\":{\"userId\":\"%s\",\"name\":\"%s\",\"role\":\"%s\"}}",
user.getUserId(), user.getName(), user.getRole()
);
HandlerUtils.sendJsonResponse(exchange, 200, responseJson);
} else {
HandlerUtils.sendJsonResponse(exchange, 401, "{\"status\":\"error\",\"message\":\"Invalid credentials\"}");
}
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
}
}
}
