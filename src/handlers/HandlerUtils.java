package handlers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
public class HandlerUtils
{
public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException
{
exchange.getResponseHeaders().set("Content-Type", "application/json");
exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
{
exchange.sendResponseHeaders(204, -1);
return;
}
exchange.sendResponseHeaders(statusCode, response.getBytes().length);
OutputStream os = exchange.getResponseBody();
os.write(response.getBytes());
os.close();
}
public static String extractJsonValue(String jsonString, String key)
{
String searchKey = "\"" + key + "\"";
int startIndex = jsonString.indexOf(searchKey);
if (startIndex == -1) return null;
startIndex = jsonString.indexOf(":", startIndex) + 1;
while (startIndex < jsonString.length() && (jsonString.charAt(startIndex) == ' ' || jsonString.charAt(startIndex) == '\"'))
{
startIndex++;
}
int endIndex = startIndex;
while (endIndex < jsonString.length() && jsonString.charAt(endIndex) != '\"' && jsonString.charAt(endIndex) != ',' && jsonString.charAt(endIndex) != '}')
{
endIndex++;
}
return jsonString.substring(startIndex, endIndex).trim();
}
}