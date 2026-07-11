package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Product;
import repositories.ProductRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductHandler implements HttpHandler {
    private final ProductRepository productRepository = new ProductRepository();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            HandlerUtils.handleCorsPreflight(exchange);
            return;
        }

        try {
            if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = HandlerUtils.parseQueryParams(query);
                String id = params.get("id");
                String cashierId = params.get("cashierId");

                if (id != null && !id.trim().isEmpty()) {
                    // Fetch single product
                    Product product = productRepository.findById(id);
                    if (product != null) {
                        int locked = CartSyncHandler.getLockedQuantity(product.getProductId(), cashierId);
                        int finalAvailable = Math.max(0, product.getAvailableQuantity() - product.getHeldQuantity() - locked);
                        String json = String.format(
                            "{\"productId\":\"%s\",\"productName\":\"%s\",\"mrp\":%s,\"prp\":%s,\"gstPercentage\":%s,\"availableQuantity\":%d,\"alertThreshold\":%d,\"heldQuantity\":%d}",
                            product.getProductId(), product.getProductName(),
                            product.getMrp().toString(), product.getPrp().toString(),
                            product.getGstPercentage().toString(), finalAvailable,
                            product.getAlertThreshold(), product.getHeldQuantity()
                        );
                        HandlerUtils.sendJsonResponse(exchange, 200, json);
                    } else {
                        HandlerUtils.sendJsonResponse(exchange, 404, "{\"message\":\"Product not found\"}");
                    }
                } else {
                    // List all products (e.g. for admin catalog viewer)
                    List<Product> products = productRepository.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < products.size(); i++) {
                        Product p = products.get(i);
                        int locked = CartSyncHandler.getLockedQuantity(p.getProductId(), cashierId);
                        int finalAvailable = Math.max(0, p.getAvailableQuantity() - p.getHeldQuantity() - locked);
                        sb.append(String.format(
                            "{\"productId\":\"%s\",\"productName\":\"%s\",\"mrp\":%s,\"prp\":%s,\"gstPercentage\":%s,\"availableQuantity\":%d,\"alertThreshold\":%d,\"heldQuantity\":%d}",
                            p.getProductId(), p.getProductName(),
                            p.getMrp().toString(), p.getPrp().toString(),
                            p.getGstPercentage().toString(), finalAvailable,
                            p.getAlertThreshold(), p.getHeldQuantity()
                        ));
                        if (i < products.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    HandlerUtils.sendJsonResponse(exchange, 200, sb.toString());
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                // Check if user is Super-Admin
                if (!HandlerUtils.checkRole(exchange, "SUPER_ADMIN")) {
                    HandlerUtils.sendJsonResponse(exchange, 403, "{\"message\":\"Forbidden: Only SUPER_ADMIN can add or update products.\"}");
                    return;
                }

                String body = HandlerUtils.readRequestBody(exchange);
                String productId = HandlerUtils.getJsonStringValue(body, "productId");
                String productName = HandlerUtils.getJsonStringValue(body, "productName");
                
                String mrpStr = HandlerUtils.getJsonStringValue(body, "mrp");
                String prpStr = HandlerUtils.getJsonStringValue(body, "prp");
                String gstStr = HandlerUtils.getJsonStringValue(body, "gstPercentage");
                int availableQuantity = HandlerUtils.getJsonIntValue(body, "availableQuantity", 0);
                int alertThreshold = HandlerUtils.getJsonIntValue(body, "alertThreshold", 10);
                int heldQuantity = HandlerUtils.getJsonIntValue(body, "heldQuantity", 0);

                if (productId == null || productName == null || mrpStr == null || prpStr == null || gstStr == null) {
                    HandlerUtils.sendJsonResponse(exchange, 400, "{\"message\":\"Missing required product fields\"}");
                    return;
                }

                Product p = new Product(
                    productId,
                    productName,
                    new BigDecimal(mrpStr),
                    new BigDecimal(prpStr),
                    new BigDecimal(gstStr),
                    availableQuantity,
                    alertThreshold,
                    heldQuantity
                );
                
                Product saved = productRepository.save(p);
                if (saved != null) {
                    HandlerUtils.sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Product saved successfully\"}");
                } else {
                    HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Failed to save product\"}");
                }
            } else {
                HandlerUtils.sendJsonResponse(exchange, 405, "{\"message\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendJsonResponse(exchange, 500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }
}
