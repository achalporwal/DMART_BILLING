package repositories;

import db.DatabaseConfig;
import models.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CustomerRepository {
    
    public Customer findByMobileNumber(String mobileNumber) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT * FROM customers WHERE mobile_number = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mobileNumber);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                    rs.getString("customer_id"),
                    rs.getString("name"),
                    rs.getString("mobile_number"),
                    rs.getInt("age"),
                    rs.getString("location")
                );
            }
        } catch (SQLException e) {
            System.err.println("Database error in CustomerRepository.findByMobileNumber: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return null;
    }

    public Customer findById(String customerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT * FROM customers WHERE customer_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customerId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                    rs.getString("customer_id"),
                    rs.getString("name"),
                    rs.getString("mobile_number"),
                    rs.getInt("age"),
                    rs.getString("location")
                );
            }
        } catch (SQLException e) {
            System.err.println("Database error in CustomerRepository.findById: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return null;
    }

    public Customer save(Customer customer) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConfig.getConnection();
            if (customer.getCustomerId() == null || customer.getCustomerId().trim().isEmpty()) {
                customer.setCustomerId("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
            String sql = "INSERT INTO customers (customer_id, name, mobile_number, age, location) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customer.getCustomerId());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getMobileNumber());
            pstmt.setInt(4, customer.getAge());
            pstmt.setString(5, customer.getLocation());
            pstmt.executeUpdate();
            return customer;
        } catch (SQLException e) {
            System.err.println("Database error in CustomerRepository.save: " + e.getMessage());
            return null;
        } finally {
            closeResources(pstmt, null);
            DatabaseConfig.releaseConnection(conn);
        }
    }

    public void update(Customer customer, Connection conn) throws SQLException {
        String sql = "UPDATE customers SET name = ?, mobile_number = ?, age = ?, location = ? WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getMobileNumber());
            pstmt.setInt(3, customer.getAge());
            pstmt.setString(4, customer.getLocation());
            pstmt.setString(5, customer.getCustomerId());
            pstmt.executeUpdate();
        }
    }

    public void update(Customer customer) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "UPDATE customers SET name = ?, mobile_number = ?, age = ?, location = ? WHERE customer_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getMobileNumber());
            pstmt.setInt(3, customer.getAge());
            pstmt.setString(4, customer.getLocation());
            pstmt.setString(5, customer.getCustomerId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error in CustomerRepository.update: " + e.getMessage());
        } finally {
            closeResources(pstmt, null);
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private void closeResources(PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {}
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException ignored) {}
        }
    }
}
