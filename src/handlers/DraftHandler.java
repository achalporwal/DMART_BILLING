package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DatabaseConfig;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DraftHandler implements HttpHandler {

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
String cashierId = params.get("cashierId");

if (cashierId == null || cashierId.trim().isEmpty()) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing cashierId\"}");
return;
}

String draftJson = getDraft(cashierId);
if (draftJson != null) {
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"found\",\"draft\":" + draftJson + "}");
} else {
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"not_found\"}");
}
} else if ("POST".equalsIgnoreCase(method)) {
String body = HandlerUtils.readRequestBody(exchange);
String cashierId = HandlerUtils.getJsonStringValue(body, "cashierId");
String draftJson = HandlerUtils.getJsonStringValue(body, "draftJson");

if (cashierId == null || cashierId.trim().isEmpty()) {
HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing cashierId\"}");
return;
}

if (draftJson == null || draftJson.trim().isEmpty() || "[]".equals(draftJson.trim())) {
deleteDraft(cashierId);
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Draft cleared\"}");
} else {
saveDraft(cashierId, draftJson);
HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Draft saved\"}");
}
} else {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
}
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
}
}

private String getDraft(String cashierId) throws SQLException {
Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
conn = DatabaseConfig.getConnection();
pstmt = conn.prepareStatement("SELECT draft_json FROM cashier_drafts WHERE cashier_id = ?");
pstmt.setString(1, cashierId);
rs = pstmt.executeQuery();
if (rs.next()) {
return rs.getString("draft_json");
}
return null;
} finally {
if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
DatabaseConfig.releaseConnection(conn);
}
}

private void saveDraft(String cashierId, String draftJson) throws SQLException {
Connection conn = null;
PreparedStatement pstmt = null;
try {
conn = DatabaseConfig.getConnection();
boolean exists = false;
try (PreparedStatement checkPstmt = conn.prepareStatement("SELECT 1 FROM cashier_drafts WHERE cashier_id = ?")) {
checkPstmt.setString(1, cashierId);
try (ResultSet rs = checkPstmt.executeQuery()) {
exists = rs.next();
}
}

if (exists) {
pstmt = conn.prepareStatement("UPDATE cashier_drafts SET draft_json = ?, updated_at = CURRENT_TIMESTAMP WHERE cashier_id = ?");
pstmt.setString(1, draftJson);
pstmt.setString(2, cashierId);
} else {
pstmt = conn.prepareStatement("INSERT INTO cashier_drafts (cashier_id, draft_json) VALUES (?, ?)");
pstmt.setString(1, cashierId);
pstmt.setString(2, draftJson);
}
pstmt.executeUpdate();
} finally {
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
DatabaseConfig.releaseConnection(conn);
}
}

private void deleteDraft(String cashierId) throws SQLException {
Connection conn = null;
PreparedStatement pstmt = null;
try {
conn = DatabaseConfig.getConnection();
pstmt = conn.prepareStatement("DELETE FROM cashier_drafts WHERE cashier_id = ?");
pstmt.setString(1, cashierId);
pstmt.executeUpdate();
} finally {
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
DatabaseConfig.releaseConnection(conn);
}
}
}
