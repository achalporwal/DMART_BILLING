package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import services.ReportingService;
import services.ReportingService.SalesReportItem;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AdminReportHandler implements HttpHandler {
private final ReportingService reportingService = new ReportingService();

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

if (!HandlerUtils.checkRole(exchange, "SUPER_ADMIN")) {
HandlerUtils.sendJsonResponse(exchange, 403, "{\"message\":\"Forbidden: Only SUPER_ADMIN accounts can view reports.\"}");
return;
}

try {
String query = exchange.getRequestURI().getQuery();
Map<String, String> params = HandlerUtils.parseQueryParams(query);
String type = params.getOrDefault("type", "daily");

List<SalesReportItem> report;

if ("monthly".equalsIgnoreCase(type)) {
int year = LocalDate.now().getYear();
int month = LocalDate.now().getMonthValue();

String yrParam = params.get("year");
String moParam = params.get("month");
if (yrParam != null) {
try { year = Integer.parseInt(yrParam); } catch (NumberFormatException ignored) {}
}
if (moParam != null) {
try { month = Integer.parseInt(moParam); } catch (NumberFormatException ignored) {}
}
report = reportingService.getMonthlySalesReport(year, month);
} else {
LocalDate date = LocalDate.now();
String dtParam = params.get("date");
if (dtParam != null && !dtParam.trim().isEmpty()) {
try {
date = LocalDate.parse(dtParam);
} catch (Exception ignored) {}
}
report = reportingService.getDailySalesReport(date);
}

StringBuilder sb = new StringBuilder("[");
for (int i = 0; i < report.size(); i++) {
SalesReportItem item = report.get(i);
sb.append(String.format(
"{\"cashierId\":\"%s\",\"cashierName\":\"%s\",\"totalBills\":%d,\"totalRevenue\":%s}",
item.cashierId, item.cashierName, item.totalBills, item.totalRevenue.toString()
));
if (i < report.size() - 1) sb.append(",");
}
sb.append("]");

HandlerUtils.sendJsonResponse(exchange, 200, sb.toString());
} catch (Exception e) {
e.printStackTrace();
HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
}
}
}
