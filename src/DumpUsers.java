import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import db.DatabaseConfig;

public class DumpUsers {
    public static void main(String[] args) {
        if (System.getProperty("DB_URL") == null && System.getenv("DB_URL") == null) {
            System.setProperty("DB_URL", "jdbc:mysql://localhost:3306/dmart?useSSL=false&allowPublicKeyRetrieval=true");
        }
        if (System.getProperty("DB_DRIVER") == null && System.getenv("DB_DRIVER") == null) {
            System.setProperty("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
        }
        if (System.getProperty("DB_USER") == null && System.getenv("DB_USER") == null) {
            System.setProperty("DB_USER", "root");
        }
        if (System.getProperty("DB_PASSWORD") == null && System.getenv("DB_PASSWORD") == null) {
            System.setProperty("DB_PASSWORD", "root");
        }
        try {
            DatabaseConfig.initialize();
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                System.out.println("USER_ID | NAME | ROLE | PASSWORD | IS_ACTIVE");
                System.out.println("-------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%s | %s | %s | %s | %d\n",
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("password"),
                        rs.getInt("is_active")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
