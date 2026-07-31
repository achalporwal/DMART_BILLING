package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import services.ReportingService;
import java.io.IOException;

public class AdminReportHandler implements HttpHandler
{
private ReportingService reportService;
public AdminReportHandler()
{
this.reportService = new ReportingService();
}
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\n--- AdminReportHandler: Report Request Received ---");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
if (exchange.getRequestMethod().equalsIgnoreCase("GET"))
{
reportService.printDailySalesReport();
HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Report generated in server console.\"}");
System.out.println("API Reply: Sales Report requested by Admin.");
}
else
{
HandlerUtils.sendResponse(exchange, 405, "{\"status\":\"error\", \"message\":\"Method Not Allowed\"}");
}
}
catch (Exception e)
{
System.out.println("Server Error: " + e.getMessage());
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
}