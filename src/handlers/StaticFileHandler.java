package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
public class StaticFileHandler implements HttpHandler
{
@Override
public void handle(HttpExchange exchange) throws IOException 
{
System.out.println("\n--- StaticFileHandler: UI File Requested ---");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) 
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Static files served successfully.\"}");
}
catch (Exception e)
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
}