import com.sun.net.httpserver.HttpServer;
import db.DatabaseConfig;
import handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {}
        }
        
        try {
            // 1. Initialize Database connection pool
            DatabaseConfig.initialize();
            // 2. Auto-initialize tables
            db.DbInitializer.initializeDatabase();
        } catch (SQLException e) {
            System.err.println("CRITICAL ERROR: Failed to initialize native JDBC connection pool: " + e.getMessage());
            System.err.println("Application will attempt to run, but database operations may fail. Configure properties or environment variables.");
        }

        try {
            // 2. Instantiate server on port 8080
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // 3. Register Controllers/API Handlers
            server.createContext("/api/auth/login", new AuthHandler());
            server.createContext("/api/customer/update", new CustomerUpdateHandler());
            server.createContext("/api/customer", new CustomerHandler());
            server.createContext("/api/product/cart-sync", new CartSyncHandler());
            server.createContext("/api/product", new ProductHandler());
            server.createContext("/api/draft", new DraftHandler());
            server.createContext("/api/bill", new BillHandler());
            server.createContext("/api/admin/reports", new AdminReportHandler());
            server.createContext("/api/admin/users", new AdminUserHandler());
            server.createContext("/api/admin/export/low-stock", new StockExportHandler());
            server.createContext("/api/admin/export", new ExportHandler());
            server.createContext("/api/admin/analytics", new AdminAnalyticsHandler());
            
            // Serve static files from web/ directory
            server.createContext("/", new StaticFileHandler("./web"));

            // 4. Configure thread pool for handling concurrent connections
            server.setExecutor(Executors.newFixedThreadPool(15));

            // 5. Boot HTTP Server
            server.start();
            System.out.println("========================================================");
            System.out.println("   D-Mart Billing System (Frameworkless Java Web App)   ");
            System.out.println("========================================================");
            System.out.println("  * Web Server: http://localhost:" + port);
            System.out.println("  * Database Pool Initialized");
            System.out.println("  * Press Ctrl+C to terminate");
            System.out.println("========================================================");

            // 6. Graceful JVM shutdown hooks
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nStopping web server...");
                server.stop(1);
                DatabaseConfig.shutdown();
            }));

        } catch (IOException e) {
            System.err.println("Fatal: Could not initialize HTTP server context: " + e.getMessage());
        }
    }
}
