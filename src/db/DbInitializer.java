package db;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
public class DbInitializer 
{
public static void initializeDatabase() 
{
Connection conn = null;
try 
{
conn = DatabaseConfig.getConnection();
// 1. Check if database tables are already initialized (check if 'users' table exists)
DatabaseMetaData dbm = conn.getMetaData();
String catalog = conn.getCatalog();
ResultSet tables = dbm.getTables(catalog, null, "users", null);
if (tables.next()) 
{
System.out.println("Database tables detected. Auto-initialization skipped.");
// Programmatic Migration Checks
try (Statement stmt = conn.createStatement()) 
{
// 1. users.is_active
try
{
ResultSet cols = dbm.getColumns(catalog, null, "users", "is_active");
if (!cols.next()) 
{
System.out.println("Migration: Adding 'is_active' column to 'users' table...");
stmt.execute("ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT 1");
}
cols.close();
} 
catch (Exception e) 
{ 
System.err.println("Users migration error: " + e.getMessage()); 
}
// 2. bills.payment_mode
try 
{
ResultSet cols = dbm.getColumns(catalog, null, "bills", "payment_mode");
if (!cols.next())
{
System.out.println("Migration: Adding 'payment_mode' column to 'bills' table...");
stmt.execute("ALTER TABLE bills ADD COLUMN payment_mode VARCHAR(20) NOT NULL DEFAULT 'CASH'");
}
cols.close();
} 
catch (Exception e) 
{ 
System.err.println("Bills payment_mode migration error: " + e.getMessage()); 
}
// 3. bills.cash_received
try 
{
ResultSet cols = dbm.getColumns(catalog, null, "bills", "cash_received");
if (!cols.next())
{
System.out.println("Migration: Adding 'cash_received' column to 'bills' table...");
stmt.execute("ALTER TABLE bills ADD COLUMN cash_received DECIMAL(10, 2) NOT NULL DEFAULT 0.00");
}
cols.close();
} 
catch (Exception e) 
{ 
System.err.println("Bills cash_received migration error: " + e.getMessage()); 
}
// 4. bills.cash_returned
try
{
ResultSet cols = dbm.getColumns(catalog, null, "bills", "cash_returned");
if (!cols.next())
{
System.out.println("Migration: Adding 'cash_returned' column to 'bills' table...");
stmt.execute("ALTER TABLE bills ADD COLUMN cash_returned DECIMAL(10, 2) NOT NULL DEFAULT 0.00");
}
cols.close();
} 
catch (Exception e) 
{ 
System.err.println("Bills cash_returned migration error: " + e.getMessage()); 
}
// 5. bills.status
try 
{
ResultSet cols = dbm.getColumns(catalog, null, "bills", "status");
if (!cols.next()) 
{
System.out.println("Migration: Adding 'status' column to 'bills' table...");
stmt.execute("ALTER TABLE bills ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'");
}
cols.close();
} 
catch (Exception e)
{ 
System.err.println("Bills status migration error: " + e.getMessage()); 
}
// 6. customers unique index
try 
{
DatabaseMetaData metadata = conn.getMetaData();
try (ResultSet rs = metadata.getIndexInfo(catalog, null, "customers", true, false)) 
{
boolean hasIndex = false;
while (rs != null && rs.next()) 
{
String indexName = rs.getString("INDEX_NAME");
if ("idx_customers_mobile".equalsIgnoreCase(indexName) || "mobile_number".equalsIgnoreCase(indexName)) 
{
hasIndex = true;
break;
}
}
if (!hasIndex) 
{
stmt.execute("CREATE UNIQUE INDEX idx_customers_mobile ON customers(mobile_number)");
}
}
} 
catch (Exception e) 
{ 
System.err.println("Index migration error: " + e.getMessage()); 
}
// 7. products.alert_threshold
try 
{
ResultSet cols = dbm.getColumns(catalog, null, "products", "alert_threshold");
if (!cols.next()) 
{
System.out.println("Migration: Adding 'alert_threshold' column to 'products' table...");
stmt.execute("ALTER TABLE products ADD COLUMN alert_threshold INT NOT NULL DEFAULT 10");
}
cols.close();
} 
catch (Exception e) 
{ 
System.err.println("Products alert_threshold migration error: " + e.getMessage()); 
}
// 8. products.held_quantity
try 
{
ResultSet cols = dbm.getColumns(catalog, null, "products", "held_quantity");
if (!cols.next()) 
{
System.out.println("Migration: Adding 'held_quantity' column to 'products' table...");
stmt.execute("ALTER TABLE products ADD COLUMN held_quantity INT NOT NULL DEFAULT 0");
}
cols.close();
} 
catch (Exception e) 
{ 
System.err.println("Products held_quantity migration error: " + e.getMessage()); 
}
// 9. cashier_drafts table
try 
{
ResultSet tbls = dbm.getTables(catalog, null, "cashier_drafts", null);
if (!tbls.next()) 
{
System.out.println("Migration: Creating 'cashier_drafts' table...");
stmt.execute("CREATE TABLE cashier_drafts (cashier_id VARCHAR(50) PRIMARY KEY, draft_json TEXT NOT NULL, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, FOREIGN KEY (cashier_id) REFERENCES users(user_id))");
}
tbls.close();
} 
catch (Exception e) 
{ 
System.err.println("cashier_drafts table creation error: " + e.getMessage());
}
} 
catch (Exception ex) 
{
System.err.println("Migration outer statement error: " + ex.getMessage());
}
return;
}
// 2. Read schema.sql from the project root directory
if (!Files.exists(Paths.get("schema.sql"))) 
{
System.err.println("Warning: schema.sql not found at project root. Skipping database seeding.");
return;
}
String schemaContent = new String(Files.readAllBytes(Paths.get("schema.sql")), "UTF-8");
// 3. Split the SQL file by semicolons to execute statements individually
// Note: This is a simple parser. It ignores lines starting with '--'.
String[] rawStatements = schemaContent.split(";");
try (Statement stmt = conn.createStatement()) 
{
for (String rawSql : rawStatements) 
{
String sql = cleanSqlStatement(rawSql);
if (sql.isEmpty()) 
{
continue;
}
try 
{
stmt.execute(sql);
} 
catch (Exception ex) 
{
System.err.println("Executing SQL statement failed: " + sql.substring(0, Math.min(sql.length(), 50)) + "... Error: " + ex.getMessage());
}
}
}
System.out.println("Database schema and seed data loaded successfully.");
} 
catch (Exception e) 
{
System.err.println("Warning: Database initialization error: " + e.getMessage());
} 
finally 
{
if (conn != null)
{
DatabaseConfig.releaseConnection(conn);
}
}
}
private static String cleanSqlStatement(String sql) 
{
String[] lines = sql.split("\n");
StringBuilder sb = new StringBuilder();
for (String line : lines) 
{
String trimmed = line.trim();
if (trimmed.isEmpty() || trimmed.startsWith("--")) 
{
continue;
}
sb.append(line).append("\n");
}
return sb.toString().trim();
}
}
