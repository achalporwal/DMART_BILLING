package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import services.ReturnService;
import java.io.IOException;
import java.io.InputStream;

public class ReturnHandler implements HttpHandler
{
private ReturnService returnService;
public ReturnHandler()
{
this.returnService = new ReturnService();
}
@Override
public void handle(HttpExchange exchange) throws IOException
{
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
if (exchange.getRequestMethod().equalsIgnoreCase("POST"))
{
InputStream is = exchange.getRequestBody();
String body = new String(is.readAllBytes());
String productId = HandlerUtils.extractJsonValue(body, "productId");
String qtyStr = HandlerUtils.extractJsonValue(body, "quantity");
if (productId == null || qtyStr == null)
{
HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Missing return details\"}");
return;
}
int quantity = Integer.parseInt(qtyStr);
returnService.processReturn(productId, quantity);
HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Return processed successfully!\"}");
}
else
{
HandlerUtils.sendResponse(exchange, 405, "{\"status\":\"error\", \"message\":\"Method Not Allowed\"}");
}
}
catch (Exception e)
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
}