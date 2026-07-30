package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Product;
import repositories.ProductRepository;
import java.io.IOException;
public class ProductHandler implements HttpHandler
{
private ProductRepository productRepo;
public ProductHandler()
{
this.productRepo = new ProductRepository();
}
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\n---ProductHandler: Fetch Product Request Received ---");
if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod()))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
if ("GET".equalsIgnoreCase(exchange.getRequestMethod()))
{
try
{
String query = exchange.getRequestURI().getQuery();
String productId = null;
if (query != null && query.contains("id="))
{
productId = query.split("id=")[1].split("&")[0];
}
if (productId == null || productId.isEmpty())
{
HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Product ID is required\"}");
return;
}
Product product = productRepo.getProductById(productId);
if (product != null)
{
String responseJson = String.format("{\"status\":\"success\", \"productId\":\"%s\", \"productName\":\"%s\", \"prp\":%s, \"gst\":%s}",product.getProductId(), product.getProductName(), product.getPrp(), product.getGstPercentage());
HandlerUtils.sendResponse(exchange, 200, responseJson);
System.out.println("API Reply: Product details sent for " + product.getProductName());
}
else
{
HandlerUtils.sendResponse(exchange, 404, "{\"status\":\"error\", \"message\":\"Product not found\"}");
System.out.println("API Reply: Product not found for ID " + productId);
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