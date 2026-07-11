package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CartSyncHandler implements HttpHandler {
public static final Map<String, Map<String, Integer>> activeCarts = new ConcurrentHashMap<>();

@Override
public void handle(HttpExchange exchange) throws IOException {
String method = exchange.getRequestMethod();

if ("OPTIONS".equalsIgnoreCase(method)) {
HandlerUtils.handleCorsPreflight(exchange);
return;
}

try {
if ("POST".equalsIgnoreCase(method)) {
String body = HandlerUtils.readRequestBody(exchange);
String cashierId = HandlerUtils.getJsonStringValue(body, "cashierId");

if (cashierId == null || cashierId.trim().isEmpty()) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing cashierId\"}");
return;
}

Map<String, Integer> cartMap = new HashMap<>();

int cartIdx = body.indexOf("\"cart\"");
if (cartIdx != -1) {
int startBracket = body.indexOf("[", cartIdx);
int endBracket = body.indexOf("]", startBracket);
if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
String arrayContent = body.substring(startBracket + 1, endBracket).trim();
if (!arrayContent.isEmpty()) {
// Split by } objects
String[] items = arrayContent.split("(?<=\\}),?");
for (String item : items) {
String prodId = HandlerUtils.getJsonStringValue(item, "productId");
int qty = HandlerUtils.getJsonIntValue(item, "quantity", 0);
if (prodId != null && qty > 0) {
cartMap.put(prodId, qty);
}
}
}
}
}

activeCarts.put(cashierId, cartMap);
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\"}");
} else if ("DELETE".equalsIgnoreCase(method)) {
String query = exchange.getRequestURI().getQuery();
Map<String, String> params = HandlerUtils.parseQueryParams(query);
String cashierId = params.get("cashierId");
if (cashierId != null) {
activeCarts.remove(cashierId);
}
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\"}");
} else {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
}
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
}
}


public static int getLockedQuantity(String productId, String excludeCashierId) {
int lockedQty = 0;
for (Map.Entry<String, Map<String, Integer>> entry : activeCarts.entrySet()) {
if (excludeCashierId != null && excludeCashierId.equalsIgnoreCase(entry.getKey())) {
continue;
}
Map<String, Integer> cart = entry.getValue();
if (cart != null && cart.containsKey(productId)) {
lockedQty += cart.get(productId);
}
}
return lockedQty;
}
}
