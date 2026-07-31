package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class DraftHandler implements HttpHandler
{
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\n---DraftHandler: Pause Bill Request ---");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Bill saved as draft successfully!\"}");
}
catch (Exception e)
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
}