package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Product;
import repositories.ProductRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class StockExportHandler implements HttpHandler {
    private final ProductRepository productRepository = new ProductRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            HandlerUtils.handleCorsPreflight(exchange);
            return;
        }

        // Only SUPER_ADMIN can export
        if (!HandlerUtils.checkRole(exchange, "SUPER_ADMIN")) {
            HandlerUtils.sendJsonResponse(exchange, 403, "{\"message\":\"Forbidden: Only SUPER_ADMIN can export CSV reports.\"}");
            return;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = HandlerUtils.parseQueryParams(query);
            
            int threshold = 10;
            if (params.containsKey("threshold")) {
                try {
                    threshold = Integer.parseInt(params.get("threshold"));
                } catch (NumberFormatException ignored) {}
            }

            List<Product> products = productRepository.findLowStockProducts(threshold);

            StringBuilder csv = new StringBuilder();
            csv.append("Product ID,Product Name,MRP,D-Mart Price,GST %,Available Quantity\n");

            for (Product p : products) {
                csv.append(String.format("%s,\"%s\",%s,%s,%s,%d\n",
                    p.getProductId(),
                    p.getProductName().replace("\"", "\"\""),
                    p.getMrp().toString(),
                    p.getPrp().toString(),
                    p.getGstPercentage().toString(),
                    p.getAvailableQuantity()
                ));
            }

            byte[] bytes = csv.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=low_stock_report.csv");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "X-User-Role, X-User-Id");
            
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }
}
