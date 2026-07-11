package repositories;

import db.DatabaseConfig;
import models.Product;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public Product findById(String productId) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            return findByIdOnConn(productId, conn);
        } catch (SQLException e) {
            System.err.println("Database error in ProductRepository.findById: " + e.getMessage());
            return null;
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private Product findByIdOnConn(String productId, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("SELECT * FROM products WHERE product_id = ?");
            pstmt.setString(1, productId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapProduct(rs);
            }
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public boolean updateQuantity(String productId, int quantityToSubtract, Connection connShared) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            String sql = "UPDATE products SET available_quantity = available_quantity - ? WHERE product_id = ? AND available_quantity >= ?";
            pstmt = connShared.prepareStatement(sql);
            pstmt.setInt(1, quantityToSubtract);
            pstmt.setString(2, productId);
            pstmt.setInt(3, quantityToSubtract);
            return pstmt.executeUpdate() > 0;
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Product> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM products ORDER BY product_id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in ProductRepository.findAll: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return products;
    }

    /**
     * Upsert: reuses a single connection for both the existence check and the write.
     */
    public Product save(Product product) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConfig.getConnection();
            Product existing = findByIdOnConn(product.getProductId(), conn);

            if (existing != null) {
                String sql = "UPDATE products SET product_name = ?, mrp = ?, prp = ?, gst_percentage = ?, available_quantity = ?, alert_threshold = ?, held_quantity = ? WHERE product_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, product.getProductName());
                pstmt.setBigDecimal(2, product.getMrp());
                pstmt.setBigDecimal(3, product.getPrp());
                pstmt.setBigDecimal(4, product.getGstPercentage());
                pstmt.setInt(5, product.getAvailableQuantity());
                pstmt.setInt(6, product.getAlertThreshold());
                pstmt.setInt(7, product.getHeldQuantity());
                pstmt.setString(8, product.getProductId());
            } else {
                String sql = "INSERT INTO products (product_id, product_name, mrp, prp, gst_percentage, available_quantity, alert_threshold, held_quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, product.getProductId());
                pstmt.setString(2, product.getProductName());
                pstmt.setBigDecimal(3, product.getMrp());
                pstmt.setBigDecimal(4, product.getPrp());
                pstmt.setBigDecimal(5, product.getGstPercentage());
                pstmt.setInt(6, product.getAvailableQuantity());
                pstmt.setInt(7, product.getAlertThreshold());
                pstmt.setInt(8, product.getHeldQuantity());
            }
            pstmt.executeUpdate();
            return product;
        } catch (SQLException e) {
            System.err.println("Database error in ProductRepository.save: " + e.getMessage());
            return null;
        } finally {
            closeResources(pstmt, null);
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        int threshold = 10;
        try {
            threshold = rs.getInt("alert_threshold");
        } catch (SQLException ignored) {}
        int heldQty = 0;
        try {
            heldQty = rs.getInt("held_quantity");
        } catch (SQLException ignored) {}
        return new Product(
            rs.getString("product_id"),
            rs.getString("product_name"),
            rs.getBigDecimal("mrp"),
            rs.getBigDecimal("prp"),
            rs.getBigDecimal("gst_percentage"),
            rs.getInt("available_quantity"),
            threshold,
            heldQty
        );
    }

    public boolean replenishQuantity(String productId, int quantityToAdd, Connection connShared) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            String sql = "UPDATE products SET available_quantity = available_quantity + ? WHERE product_id = ?";
            pstmt = connShared.prepareStatement(sql);
            pstmt.setInt(1, quantityToAdd);
            pstmt.setString(2, productId);
            return pstmt.executeUpdate() > 0;
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    public List<Product> findLowStockProducts(int dummyThreshold) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM products WHERE available_quantity < alert_threshold ORDER BY available_quantity ASC");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in ProductRepository.findLowStockProducts: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return products;
    }

    public List<Product> findDeadStockProducts() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Product> products = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT * FROM products WHERE product_id NOT IN (" +
                         "  SELECT DISTINCT bi.product_id FROM bill_items bi " +
                         "  JOIN bills b ON bi.bill_id = b.bill_id " +
                         "  WHERE b.bill_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH)" +
                         ") ORDER BY product_id";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in ProductRepository.findDeadStockProducts: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return products;
    }

    private void closeResources(PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
        if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
    }
}
