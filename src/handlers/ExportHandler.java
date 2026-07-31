package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
public class ExportHandler implements HttpHandler
{
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\n--- ExportHandler: Export Data Request ---");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Data exported to CSV format.\"}");
}
catch (Exception e)
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
}