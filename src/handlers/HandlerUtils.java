package handlers;

import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HandlerUtils {

public static String readRequestBody(HttpExchange exchange) throws IOException {
try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"))) {
return reader.lines().collect(Collectors.joining("\n"));
}
}

public static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
byte[] responseBytes = jsonResponse.getBytes("UTF-8");
exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-User-Role, X-User-Id");

exchange.sendResponseHeaders(statusCode, responseBytes.length);
try (OutputStream os = exchange.getResponseBody()) {
os.write(responseBytes);
}
}

public static void handleCorsPreflight(HttpExchange exchange) throws IOException {
exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-User-Role, X-User-Id");
exchange.sendResponseHeaders(204, -1);
}

public static Map<String, String> parseQueryParams(String query) {
Map<String, String> params = new HashMap<>();
if (query == null || query.isEmpty()) return params;
for (String param : query.split("&")) {
String[] entry = param.split("=");
if (entry.length > 1) {
params.put(entry[0], entry[1]);
} else {
params.put(entry[0], "");
}
}
return params;
}

public static String getJsonStringValue(String json, String key) {
int keyIdx = json.indexOf("\"" + key + "\"");
if (keyIdx == -1) return null;
int colonIdx = json.indexOf(":", keyIdx);
if (colonIdx == -1) return null;
int quoteStart = json.indexOf("\"", colonIdx + 1);
if (quoteStart == -1) return null;
int commaIdx = json.indexOf(",", colonIdx + 1);
int braceIdx = json.indexOf("}", colonIdx + 1);
int boundary = Math.min(commaIdx == -1 ? Integer.MAX_VALUE : commaIdx, braceIdx == -1 ? Integer.MAX_VALUE : braceIdx);
if (quoteStart > boundary) return null; // No quotes before comma/brace means it is unquoted number/boolean
int quoteEnd = json.indexOf("\"", quoteStart + 1);
if (quoteEnd == -1) return null;
return json.substring(quoteStart + 1, quoteEnd);
}

public static String getJsonRawValue(String json, String key) {
int keyIdx = json.indexOf("\"" + key + "\"");
if (keyIdx == -1) return null;
int colonIdx = json.indexOf(":", keyIdx);
if (colonIdx == -1) return null;
int valStart = colonIdx + 1;
while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
valStart++;
}
if (valStart >= json.length()) return null;

if (json.charAt(valStart) == '"') {
int quoteEnd = json.indexOf("\"", valStart + 1);
if (quoteEnd == -1) return null;
return json.substring(valStart + 1, quoteEnd);
} else {
int valEnd = valStart;
while (valEnd < json.length() && json.charAt(valEnd) != ',' && json.charAt(valEnd) != '}' && json.charAt(valEnd) != ']') {
valEnd++;
}
return json.substring(valStart, valEnd).trim();
}
}

public static int getJsonIntValue(String json, String key, int defaultValue) {
int keyIdx = json.indexOf("\"" + key + "\"");
if (keyIdx == -1) return defaultValue;
int colonIdx = json.indexOf(":", keyIdx);
if (colonIdx == -1) return defaultValue;
int valStart = colonIdx + 1;
while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
valStart++;
}
int valEnd = valStart;
while (valEnd < json.length() && (Character.isDigit(json.charAt(valEnd)) || json.charAt(valEnd) == '-')) {
valEnd++;
}
try {
return Integer.parseInt(json.substring(valStart, valEnd));
} catch (NumberFormatException e) {
return defaultValue;
}
}

public static java.math.BigDecimal getJsonBigDecimalValue(String json, String key, java.math.BigDecimal defaultValue) {
int keyIdx = json.indexOf("\"" + key + "\"");
if (keyIdx == -1) return defaultValue;
int colonIdx = json.indexOf(":", keyIdx);
if (colonIdx == -1) return defaultValue;
int valStart = colonIdx + 1;
while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
valStart++;
}
int valEnd = valStart;
while (valEnd < json.length() && (Character.isDigit(json.charAt(valEnd)) || json.charAt(valEnd) == '-' || json.charAt(valEnd) == '.')) {
valEnd++;
}
try {
return new java.math.BigDecimal(json.substring(valStart, valEnd));
} catch (NumberFormatException e) {
String strVal = getJsonStringValue(json, key);
if (strVal != null) {
try {
return new java.math.BigDecimal(strVal);
} catch (NumberFormatException ex) {}
}
return defaultValue;
}
}

public static boolean checkRole(HttpExchange exchange, String requiredRole) {
String role = exchange.getRequestHeaders().getFirst("X-User-Role");
System.out.println("[AUTH] checkRole: URI=" + exchange.getRequestURI() + ", Header X-User-Role=" + role + ", Required=" + requiredRole);
return requiredRole.equals(role);
}

public static String getUserId(HttpExchange exchange) {
return exchange.getRequestHeaders().getFirst("X-User-Id");
}
}
