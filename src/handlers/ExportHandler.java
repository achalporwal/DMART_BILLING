package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.DatabaseConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExportHandler implements HttpHandler {

@Override
public void handle(HttpExchange exchange) throws IOException {
String method = exchange.getRequestMethod();

if ("OPTIONS".equalsIgnoreCase(method)) {
HandlerUtils.handleCorsPreflight(exchange);
return;
}

if (!HandlerUtils.checkRole(exchange, "SUPER_ADMIN")) {
HandlerUtils.sendJsonResponse(exchange, 403, "{\"message\":\"Forbidden: Only SUPER_ADMIN can export CSV reports.\"}");
return;
}

if (!"GET".equalsIgnoreCase(method)) {
HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
return;
}

String path = exchange.getRequestURI().getPath();
try {
if (path.endsWith("/sales")) {
exportSales(exchange);
} else if (path.endsWith("/customers")) {
exportCustomers(exchange);
} else if (path.endsWith("/items-sold")) {
exportItemsSold(exchange);
} else {
HandlerUtils.sendJsonResponse(exchange, 404, "{\"message\":\"Export endpoint not found\"}");
}
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error during export: " + e.getMessage() + "\"}");
}
}

private void exportSales(HttpExchange exchange) throws IOException, SQLException {
StringBuilder csv = new StringBuilder();
csv.append("Bill ID,Customer ID,Customer Name,Cashier ID,Date,Taxable Value,CGST,SGST,Discount,Final Amount\n");

Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
conn = DatabaseConfig.getConnection();
String sql = "SELECT b.*, c.name AS customer_name FROM bills b " +
"LEFT JOIN customers c ON b.customer_id = c.customer_id " +
"ORDER BY b.bill_date DESC";
pstmt = conn.prepareStatement(sql);
rs = pstmt.executeQuery();
while (rs.next()) {
csv.append(String.format("%s,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s\n",
rs.getString("bill_id"),
rs.getString("customer_id") == null ? "" : rs.getString("customer_id"),
rs.getString("customer_name") == null ? "Walk-in Customer" : rs.getString("customer_name").replace("\"", "\"\""),
rs.getString("cashier_id"),
rs.getTimestamp("bill_date").toString(),
rs.getBigDecimal("taxable_value").toString(),
rs.getBigDecimal("cgst").toString(),
rs.getBigDecimal("sgst").toString(),
rs.getBigDecimal("discount").toString(),
rs.getBigDecimal("final_amount").toString()
));
}
} finally {
closeResources(pstmt, rs);
DatabaseConfig.releaseConnection(conn);
}

sendCsvResponse(exchange, "sales_report.csv", csv.toString());
}

private void exportCustomers(HttpExchange exchange) throws IOException, SQLException {
StringBuilder csv = new StringBuilder();
csv.append("Customer ID,Name,Mobile Number,Age,Location\n");

Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
conn = DatabaseConfig.getConnection();
String sql = "SELECT * FROM customers ORDER BY name";
pstmt = conn.prepareStatement(sql);
rs = pstmt.executeQuery();
while (rs.next()) {
csv.append(String.format("%s,\"%s\",%s,%d,\"%s\"\n",
rs.getString("customer_id"),
rs.getString("name").replace("\"", "\"\""),
rs.getString("mobile_number"),
rs.getInt("age"),
rs.getString("location") == null ? "" : rs.getString("location").replace("\"", "\"\"")
));
}
} finally {
closeResources(pstmt, rs);
DatabaseConfig.releaseConnection(conn);
}

sendCsvResponse(exchange, "customers_list.csv", csv.toString());
}

private void exportItemsSold(HttpExchange exchange) throws IOException, SQLException {
StringBuilder csv = new StringBuilder();
csv.append("Bill ID,Date,Product ID,Product Name,Quantity,MRP,PRP,Taxable Value,CGST,SGST,Discount,Final Amount\n");

Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
conn = DatabaseConfig.getConnection();
String sql = "SELECT bi.*, b.bill_date, p.product_name FROM bill_items bi " +
"JOIN bills b ON bi.bill_id = b.bill_id " +
"JOIN products p ON bi.product_id = p.product_id " +
"ORDER BY b.bill_date DESC";
pstmt = conn.prepareStatement(sql);
rs = pstmt.executeQuery();
while (rs.next()) {
csv.append(String.format("%s,%s,%s,\"%s\",%d,%s,%s,%s,%s,%s,%s,%s\n",
rs.getString("bill_id"),
rs.getTimestamp("bill_date").toString(),
rs.getString("product_id"),
rs.getString("product_name").replace("\"", "\"\""),
rs.getInt("quantity"),
rs.getBigDecimal("mrp").toString(),
rs.getBigDecimal("prp").toString(),
rs.getBigDecimal("taxable_value").toString(),
rs.getBigDecimal("cgst").toString(),
rs.getBigDecimal("sgst").toString(),
rs.getBigDecimal("discount").toString(),
rs.getBigDecimal("final_amount").toString()
));
}
} finally {
closeResources(pstmt, rs);
DatabaseConfig.releaseConnection(conn);
}

sendCsvResponse(exchange, "items_sold_report.csv", csv.toString());
}

private void sendCsvResponse(HttpExchange exchange, String filename, String csvContent) throws IOException {
byte[] bytes = csvContent.getBytes("UTF-8");
exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=" + filename);
exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "X-User-Role, X-User-Id");

exchange.sendResponseHeaders(200, bytes.length);
try (OutputStream os = exchange.getResponseBody()) {
os.write(bytes);
}
}

private void closeResources(PreparedStatement pstmt, ResultSet rs) {
if (rs != null) {
try {
rs.close();
} catch (SQLException ignored) {}
}
if (pstmt != null) {
try {
pstmt.close();
} catch (SQLException ignored) {}
}
}
}
