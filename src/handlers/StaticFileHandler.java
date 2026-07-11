package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    private final String baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Handle preflight CORS request just in case
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HandlerUtils.handleCorsPreflight(exchange);
            return;
        }

        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File(baseDir, path);
        if (!file.exists() || file.isDirectory()) {
            sendError(exchange, 404, "File Not Found: " + path);
            return;
        }

        String mimeType = getMimeType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        
        // Add CORS headers to allow browser connections
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        exchange.sendResponseHeaders(200, fileBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileBytes);
        }
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=utf-8";
        if (fileName.endsWith(".css")) return "text/css; charset=utf-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".json")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] response = message.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
