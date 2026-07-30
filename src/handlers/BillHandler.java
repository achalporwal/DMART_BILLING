package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import services.BillingService;
import java.io.IOException;
import java.io.InputStream;
public class BillHandler implements HttpHandler
{
private BillingService billingService;
public BillHandler()
{
this.billingService = new BillingService();
}
@Override
public void handle(HttpExchange exchange) throws IOException 
{
System.out.println("\n--- BillHandler: Checkout Request Received ---");
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
System.out.println("Received Cart Data: " + body);
String customerName = HandlerUtils.extractJsonValue(body, "customerName");
String cashierId = HandlerUtils.extractJsonValue(body, "cashierId");

String[] productIds = extractStringArray(body, "productIds");
String[] qtysStr = extractStringArray(body, "quantities");
                
if (customerName == null || cashierId == null || productIds.length == 0 || qtysStr.length == 0)
{
HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Incomplete cart data\"}");
return;
}

int[] quantities = new int[qtysStr.length];
for (int i = 0; i < qtysStr.length; i++)
{
quantities[i] = Integer.parseInt(qtysStr[i]);
}

boolean isSuccess = billingService.processCheckout(customerName, cashierId, productIds, quantities);

if (isSuccess)
{
HandlerUtils.sendResponse(exchange, 201, "{\"status\":\"success\", \"message\":\"Bill generated successfully!\"}");
System.out.println("API Reply: Bill generated & saved to database.");
}
else
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Failed to generate bill\"}");
System.out.println("API Reply: Billing failed internally.");
}
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

private String[] extractStringArray(String json, String key)
{
String searchKey = "\"" + key + "\":";
int start = json.indexOf(searchKey);
if(start == -1) return new String[0];
start = json.indexOf("[", start) + 1;
int end = json.indexOf("]", start);
       
String arrayContent = json.substring(start, end).replace("\"", "").replace(" ", "");
if(arrayContent.isEmpty()) return new String[0];
return arrayContent.split(",");
}
}