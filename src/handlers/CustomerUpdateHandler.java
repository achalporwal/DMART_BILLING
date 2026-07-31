package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
//Real world mein hum isko CustomerRepository me UPDATE query banakar use karte hain. 
// Abhi hum ek skeleton bana rahe hain jisko aage database se map karenge.

import java.io.IOException;
public class CustomerUpdateHandler implements HttpHandler
{
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\n--- CustomerUpdateHandler: Update Request Received ---");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
if (exchange.getRequestMethod().equalsIgnoreCase("PUT"))
{
// Future Database Update Logic Will Go Here
HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Customer details updated successfully!\"}");
System.out.println("API Reply: Customer details updated.");
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