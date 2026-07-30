package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.User;
import repositories.UserRepository;
import java.io.IOException;
import java.io.InputStream;
public class AuthHandler implements HttpHandler
{
private UserRepository userRepo;
public AuthHandler()
{
this.userRepo = new UserRepository();
}
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\nAuthHandler: Login Request Received");
if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod()))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
if ("POST".equalsIgnoreCase(exchange.getRequestMethod()))
{
try
{
InputStream is = exchange.getRequestBody();
String body = new String(is.readAllBytes());
System.out.println("Received JSON: " + body);
String userId = HandlerUtils.extractJsonValue(body, "userId");
String password = HandlerUtils.extractJsonValue(body, "password");
User user = userRepo.authenticateUser(userId, password);
if (user != null)
{
String responseJson = "{\"status\":\"success\", \"message\":\"Login Successful\", \"role\":\"" + user.getRole() + "\"}";
HandlerUtils.sendResponse(exchange, 200, responseJson);
System.out.println("API Reply: User " + userId + " logged in successfully.");
}
else
{
String errorJson = "{\"status\":\"error\", \"message\":\"Invalid credentials\"}"; 
HandlerUtils.sendResponse(exchange, 401, errorJson);
System.out.println("API Reply: Failed login attempt for user " + userId);
}
}
catch (Exception e)
{
System.out.println("Server Error: " + e.getMessage());
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
else
{
HandlerUtils.sendResponse(exchange, 405, "{\"status\":\"error\", \"message\":\"Method Not Allowed\"}");
}
}
}