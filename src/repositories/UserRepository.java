package repositories;

import db.DatabaseConfig;
import models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User findById(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            return findByIdOnConn(userId, conn);
        } catch (SQLException e) {
            System.err.println("Database error in UserRepository.findById: " + e.getMessage());
            return null;
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
    }

    /** Internal helper: find user using a supplied (shared) connection */
    private User findByIdOnConn(String userId, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("SELECT * FROM users WHERE user_id = ?");
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("password"),
                    rs.getInt("is_active") == 1
                );
            }
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (pstmt != null) try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Upsert: inserts if userId does not exist, updates if it does.
     * Uses a single connection to avoid deadlock on SQLite pool-of-1.
     */
    public User save(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConfig.getConnection();

            User existing = findByIdOnConn(user.getUserId(), conn);

            if (existing != null) {
                String sql = "UPDATE users SET name = ?, role = ?, password = ?, is_active = ? WHERE user_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getRole());
                pstmt.setString(3, user.getPassword());
                pstmt.setInt(4, user.isActive() ? 1 : 0);
                pstmt.setString(5, user.getUserId());
            } else {
                String sql = "INSERT INTO users (user_id, name, role, password, is_active) VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user.getUserId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getRole());
                pstmt.setString(4, user.getPassword());
                pstmt.setInt(5, user.isActive() ? 1 : 0);
            }
            pstmt.executeUpdate();
            return user;
        } catch (SQLException e) {
            System.err.println("Database error in UserRepository.save: " + e.getMessage());
            return null;
        } finally {
            closeResources(pstmt, null);
            DatabaseConfig.releaseConnection(conn);
        }
    }

    public boolean delete(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement("UPDATE users SET is_active = 1 - is_active WHERE user_id = ?");
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error in UserRepository.delete: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
            DatabaseConfig.releaseConnection(conn);
        }
    }

    public boolean resetPassword(String userId, String newPassword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?");
            pstmt.setString(1, newPassword);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error in UserRepository.resetPassword: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
            DatabaseConfig.releaseConnection(conn);
        }
    }

    public List<User> findAllSubAdmins() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> list = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement("SELECT * FROM users WHERE role = 'SUB_ADMIN' ORDER BY name");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new User(
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("password"),
                    rs.getInt("is_active") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database error in UserRepository.findAllSubAdmins: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return list;
    }

    private void closeResources(PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (pstmt != null) {
            try { pstmt.close(); } catch (SQLException ignored) {}
        }
    }
}
