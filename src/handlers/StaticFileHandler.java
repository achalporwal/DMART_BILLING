package handlers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler
{
@Override
public void handle(HttpExchange exchange) throws IOException
{
System.out.println("\n--- StaticFileHandler: Serving UI ---");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
HandlerUtils.sendResponse(exchange, 204, "");
return;
}
try
{
File file = new File("web/index.html");
if (file.exists())
{
byte[] bytes = Files.readAllBytes(file.toPath());
exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
exchange.sendResponseHeaders(200, bytes.length);
OutputStream os = exchange.getResponseBody();
os.write(bytes);
os.close();
}
else
{
HandlerUtils.sendResponse(exchange, 404, "{\"status\":\"error\", \"message\":\"UI File Not Found!\"}");
}
}
catch (Exception e)
{
HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\", \"message\":\"Server Error\"}");
}
}
}