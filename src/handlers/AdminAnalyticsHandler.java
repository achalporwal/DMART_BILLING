package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DatabaseConfig;
import models.Product;
import repositories.ProductRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminAnalyticsHandler implements HttpHandler {
private final ProductRepository productRepository = new ProductRepository();

@Override
public void handle(HttpExchange exchange) throws IOException {
String method = exchange.getRequestMethod();

if ("OPTIONS".equalsIgnoreCase(method)) {
HandlerUtils.handleCorsPreflight(exchange);
return;
}

if (!"GET".equalsIgnoreCase(method)) {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
return;
}

try {
String query = exchange.getRequestURI().getQuery();
Map<String, String> params = HandlerUtils.parseQueryParams(query);

int threshold = 10;
if (params.containsKey("threshold")) {
try {
threshold = Integer.parseInt(params.get("threshold"));
} catch (NumberFormatException ignored) {}
}

String cashierStatsTodayJson = getCashierStatsToday();
String dailySalesTrendJson = getDailySalesTrend();
String monthlyCashierShareJson = getMonthlyCashierShare();
String lowStockJson = getLowStockJson(threshold);
String deadStockJson = getDeadStockJson();

String responseJson = String.format(
"{" +
"\"cashierStatsToday\":%s," +
"\"dailySalesTrend\":%s," +
"\"monthlyCashierShare\":%s," +
"\"lowStockProducts\":%s," +
"\"deadStockProducts\":%s" +
"}",
cashierStatsTodayJson,
dailySalesTrendJson,
monthlyCashierShareJson,
lowStockJson,
deadStockJson
);

HandlerUtils.sendJsonResponse(exchange, 200, responseJson);

} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal server error: " + e.getMessage() + "\"}");
}
}

private String getCashierStatsToday() {
Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
StringBuilder sb = new StringBuilder("[");
try {
conn = DatabaseConfig.getConnection();
String sql = "SELECT u.user_id, u.name, COUNT(b.bill_id) as total_bills, COALESCE(SUM(b.final_amount), 0) as total_sales " +
"FROM users u " +
"LEFT JOIN bills b ON u.user_id = b.cashier_id AND DATE(b.bill_date) = CURDATE() " +
"WHERE u.role = 'SUB_ADMIN' " +
"GROUP BY u.user_id, u.name " +
"ORDER BY total_sales DESC";
pstmt = conn.prepareStatement(sql);
rs = pstmt.executeQuery();
boolean first = true;
while (rs.next()) {
if (!first) sb.append(",");
sb.append(String.format(
"{\"cashierId\":\"%s\",\"cashierName\":\"%s\",\"totalBills\":%d,\"totalSales\":%s}",
rs.getString("user_id"),
rs.getString("name").replace("\"", "\\\""),
rs.getInt("total_bills"),
rs.getBigDecimal("total_sales").toString()
));
first = false;
}
} catch (SQLException e) {
System.err.println("Error fetching cashier stats today: " + e.getMessage());
} finally {
closeResources(pstmt, rs);
DatabaseConfig.releaseConnection(conn);
}
sb.append("]");
return sb.toString();
}

private String getDailySalesTrend() {
Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
StringBuilder sb = new StringBuilder("[");
try {
conn = DatabaseConfig.getConnection();
String sql = "SELECT DATE_FORMAT(bill_date, '%Y-%m-%d') as sales_date, SUM(final_amount) as total_sales " +
"FROM bills " +
"WHERE bill_date >= DATE_SUB(NOW(), INTERVAL 15 DAY) " +
"GROUP BY sales_date " +
"ORDER BY sales_date ASC";
pstmt = conn.prepareStatement(sql);
rs = pstmt.executeQuery();
boolean first = true;
while (rs.next()) {
if (!first) sb.append(",");
sb.append(String.format(
"{\"date\":\"%s\",\"sales\":%s}",
rs.getString("sales_date"),
rs.getBigDecimal("total_sales").toString()
));
first = false;
}
} catch (SQLException e) {
System.err.println("Error fetching daily sales trend: " + e.getMessage());
} finally {
closeResources(pstmt, rs);
DatabaseConfig.releaseConnection(conn);
}
sb.append("]");
return sb.toString();
}

private String getMonthlyCashierShare() {
Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
StringBuilder sb = new StringBuilder("[");
try {
conn = DatabaseConfig.getConnection();
String sql = "SELECT u.user_id, u.name, COALESCE(SUM(b.final_amount), 0) as total_sales " +
"FROM users u " +
"LEFT JOIN bills b ON u.user_id = b.cashier_id AND DATE_FORMAT(b.bill_date, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m') " +
"WHERE u.role = 'SUB_ADMIN' " +
"GROUP BY u.user_id, u.name " +
"ORDER BY total_sales DESC";
pstmt = conn.prepareStatement(sql);
rs = pstmt.executeQuery();
boolean first = true;
while (rs.next()) {
if (!first) sb.append(",");
sb.append(String.format(
"{\"cashierName\":\"%s\",\"totalSales\":%s}",
rs.getString("name").replace("\"", "\\\""),
rs.getBigDecimal("total_sales").toString()
));
first = false;
}
} catch (SQLException e) {
System.err.println("Error fetching monthly cashier share: " + e.getMessage());
} finally {
closeResources(pstmt, rs);
DatabaseConfig.releaseConnection(conn);
}
sb.append("]");
return sb.toString();
}

private String getLowStockJson(int threshold) {
List<Product> products = productRepository.findLowStockProducts(threshold);
StringBuilder sb = new StringBuilder("[");
for (int i = 0; i < products.size(); i++) {
Product p = products.get(i);
sb.append(String.format(
"{\"productId\":\"%s\",\"productName\":\"%s\",\"mrp\":%s,\"prp\":%s,\"gstPercentage\":%s,\"availableQuantity\":%d}",
p.getProductId(),
p.getProductName().replace("\"", "\\\""),
p.getMrp().toString(),
p.getPrp().toString(),
p.getGstPercentage().toString(),
p.getAvailableQuantity()
));
if (i < products.size() - 1) sb.append(",");
}
sb.append("]");
return sb.toString();
}
private String getDeadStockJson() {
List<Product> products = productRepository.findDeadStockProducts();
StringBuilder sb = new StringBuilder("[");
for (int i = 0; i < products.size(); i++) {
Product p = products.get(i);
sb.append(String.format(
"{\"productId\":\"%s\",\"productName\":\"%s\",\"mrp\":%s,\"prp\":%s,\"gstPercentage\":%s,\"availableQuantity\":%d}",
p.getProductId(),
p.getProductName().replace("\"", "\\\""),
p.getMrp().toString(),
p.getPrp().toString(),
p.getGstPercentage().toString(),
p.getAvailableQuantity()
));
if (i < products.size() - 1) sb.append(",");
}
sb.append("]");
return sb.toString();
}

private void closeResources(PreparedStatement pstmt, ResultSet rs) {
if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
}
}
