package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Customer;
import repositories.CustomerRepository;
import java.io.IOException;
import java.io.InputStream;
public class CustomerHandler implements HttpHandler
{
private CustomerRepository customerRepo;
public CustomerHandler()
{
this.customerRepo = new CustomerRepository();
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
if (exchange.getRequestMethod().equalsIgnoreCase("GET"))
{
String query = exchange.getRequestURI().getQuery();
String mobile = null;
if (query != null && query.contains("mobile="))
{
mobile = query.split("mobile=")[1].split("&")[0];
}
if (mobile == null || mobile.isEmpty())
{
HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Mobile number missing\"}");
return;
}
Customer customer = customerRepo.getCustomerByMobile(mobile);
if (customer != null)
{
String responseJson = "{\"status\":\"success\", \"customerId\":\"" + customer.getCustomerId() + "\", \"name\":\"" + customer.getName() + "\", \"mobile\":\"" + customer.getMobileNumber() + "\", \"age\":" + customer.getAge() + ", \"location\":\"" + customer.getLocation() + "\"}";
HandlerUtils.sendResponse(exchange, 200, responseJson);
}
else
{
HandlerUtils.sendResponse(exchange, 404, "{\"status\":\"error\", \"message\":\"Customer not found\"}");
}
} 
else if (exchange.getRequestMethod().equalsIgnoreCase("POST"))
{
InputStream is = exchange.getRequestBody();
String body = new String(is.readAllBytes());
String name = HandlerUtils.extractJsonValue(body, "name");
String mobile = HandlerUtils.extractJsonValue(body, "mobile");
String ageStr = HandlerUtils.extractJsonValue(body, "age");
String location = HandlerUtils.extractJsonValue(body, "location");
if (name == null || mobile == null || ageStr == null || location == null)
{
HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Incomplete data\"}");
return;
}
int age = Integer.parseInt(ageStr);
String customerId = "CUST-" + System.currentTimeMillis();
Customer newCustomer = new Customer(customerId, name, mobile, age, location);
boolean isSaved = customerRepo.saveCustomer(newCustomer);
if (isSaved)
{
HandlerUtils.sendResponse(exchange, 201, "{\"status\":\"success\", \"message\":\"Customer saved\", \"customerId\":\"" + customerId + "\"}");
}
else
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Database error\"}");
}
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