package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
public class AdminUserHandler implements HttpHandler
{
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\n---AdminUserHandler: Manage Cashier Request ---");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
// Skeleton for adding user to DB
HandlerUtils.sendResponse(exchange, 201, "{\"status\":\"success\", \"message\":\"New cashier account created!\"}");
}
catch (Exception e)
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
}