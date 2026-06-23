package repositories;

import db.DatabaseConfig;
import models.Bill;
import models.BillItem;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillRepository {

    public void save(Bill bill, Connection connShared) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO bills (bill_id, customer_id, cashier_id, bill_date, taxable_value, cgst, sgst, discount, final_amount, payment_mode, cash_received, cash_returned, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = connShared.prepareStatement(sql);
            pstmt.setString(1, bill.getBillId());
            pstmt.setString(2, bill.getCustomerId());
            pstmt.setString(3, bill.getCashierId());
            pstmt.setTimestamp(4, Timestamp.valueOf(bill.getBillDate()));
            pstmt.setBigDecimal(5, bill.getTaxableValue());
            pstmt.setBigDecimal(6, bill.getCgst());
            pstmt.setBigDecimal(7, bill.getSgst());
            pstmt.setBigDecimal(8, bill.getDiscount());
            pstmt.setBigDecimal(9, bill.getFinalAmount());
            pstmt.setString(10, bill.getPaymentMode() == null ? "CASH" : bill.getPaymentMode());
            pstmt.setBigDecimal(11, bill.getCashReceived() == null ? BigDecimal.ZERO : bill.getCashReceived());
            pstmt.setBigDecimal(12, bill.getCashReturned() == null ? BigDecimal.ZERO : bill.getCashReturned());
            pstmt.setString(13, bill.getStatus() == null ? "COMPLETED" : bill.getStatus());
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    public void saveItem(BillItem item, Connection connShared) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO bill_items (bill_item_id, bill_id, product_id, quantity, mrp, prp, taxable_value, cgst, sgst, discount, final_amount) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = connShared.prepareStatement(sql);
            pstmt.setString(1, item.getBillItemId());
            pstmt.setString(2, item.getBillId());
            pstmt.setString(3, item.getProductId());
            pstmt.setInt(4, item.getQuantity());
            pstmt.setBigDecimal(5, item.getMrp());
            pstmt.setBigDecimal(6, item.getPrp());
            pstmt.setBigDecimal(7, item.getTaxableValue());
            pstmt.setBigDecimal(8, item.getCgst());
            pstmt.setBigDecimal(9, item.getSgst());
            pstmt.setBigDecimal(10, item.getDiscount());
            pstmt.setBigDecimal(11, item.getFinalAmount());
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    public List<Bill> findByCashierAndDate(String cashierId, LocalDateTime dateStart, LocalDateTime dateEnd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Bill> bills = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT b.*, c.name AS customer_name, c.mobile_number AS customer_mobile, c.location AS customer_location " +
                         "FROM bills b LEFT JOIN customers c ON b.customer_id = c.customer_id " +
                         "WHERE b.cashier_id = ? AND b.bill_date >= ? AND b.bill_date <= ? ORDER BY b.bill_date DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cashierId);
            pstmt.setTimestamp(2, Timestamp.valueOf(dateStart));
            pstmt.setTimestamp(3, Timestamp.valueOf(dateEnd));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                bills.add(mapBill(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in BillRepository.findByCashierAndDate: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return bills;
    }

    public List<Bill> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Bill> bills = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT b.*, c.name AS customer_name, c.mobile_number AS customer_mobile, c.location AS customer_location " +
                         "FROM bills b LEFT JOIN customers c ON b.customer_id = c.customer_id ORDER BY b.bill_date DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                bills.add(mapBill(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in BillRepository.findAll: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return bills;
    }

    /**
     * Fetch all bills within a date/time range (no cashier filter).
     * Used by admin to view invoices for a specific day or month.
     */
    public List<Bill> findByDateRange(LocalDateTime dateStart, LocalDateTime dateEnd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Bill> bills = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT b.*, c.name AS customer_name, c.mobile_number AS customer_mobile, c.location AS customer_location " +
                         "FROM bills b LEFT JOIN customers c ON b.customer_id = c.customer_id " +
                         "WHERE b.bill_date >= ? AND b.bill_date <= ? ORDER BY b.bill_date DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(dateStart));
            pstmt.setTimestamp(2, Timestamp.valueOf(dateEnd));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                bills.add(mapBill(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error in BillRepository.findByDateRange: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return bills;
    }


    public Bill findById(String billId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT b.*, c.name AS customer_name, c.mobile_number AS customer_mobile, c.location AS customer_location " +
                         "FROM bills b LEFT JOIN customers c ON b.customer_id = c.customer_id " +
                         "WHERE b.bill_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, billId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapBill(rs);
            }
        } catch (SQLException e) {
            System.err.println("Database error in BillRepository.findById: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return null;
    }

    public List<BillItem> findItemsByBillId(String billId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BillItem> items = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT bi.*, p.product_name FROM bill_items bi " +
                         "JOIN products p ON bi.product_id = p.product_id " +
                         "WHERE bi.bill_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, billId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BillItem item = new BillItem(
                    rs.getString("bill_item_id"),
                    rs.getString("bill_id"),
                    rs.getString("product_id"),
                    rs.getInt("quantity"),
                    rs.getBigDecimal("mrp"),
                    rs.getBigDecimal("prp"),
                    rs.getBigDecimal("taxable_value"),
                    rs.getBigDecimal("cgst"),
                    rs.getBigDecimal("sgst"),
                    rs.getBigDecimal("discount"),
                    rs.getBigDecimal("final_amount")
                );
                item.setProductName(rs.getString("product_name"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Database error in BillRepository.findItemsByBillId: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return items;
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill(
            rs.getString("bill_id"),
            rs.getString("customer_id"),
            rs.getString("cashier_id"),
            rs.getTimestamp("bill_date").toLocalDateTime(),
            rs.getBigDecimal("taxable_value"),
            rs.getBigDecimal("cgst"),
            rs.getBigDecimal("sgst"),
            rs.getBigDecimal("discount"),
            rs.getBigDecimal("final_amount")
        );
        try {
            bill.setPaymentMode(rs.getString("payment_mode"));
        } catch (SQLException ignored) {}
        try {
            bill.setCashReceived(rs.getBigDecimal("cash_received"));
        } catch (SQLException ignored) {}
        try {
            bill.setCashReturned(rs.getBigDecimal("cash_returned"));
        } catch (SQLException ignored) {}
        try {
            bill.setStatus(rs.getString("status"));
        } catch (SQLException ignored) {}
        try {
            bill.setCustomerName(rs.getString("customer_name"));
            bill.setCustomerMobile(rs.getString("customer_mobile"));
            bill.setCustomerLocation(rs.getString("customer_location"));
        } catch (SQLException ignored) {}
        return bill;
    }

    public void updateBill(Bill bill, Connection conn) throws SQLException {
        String sql = "UPDATE bills SET taxable_value = ?, cgst = ?, sgst = ?, discount = ?, final_amount = ?, cash_received = ?, cash_returned = ?, status = ? WHERE bill_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, bill.getTaxableValue());
            pstmt.setBigDecimal(2, bill.getCgst());
            pstmt.setBigDecimal(3, bill.getSgst());
            pstmt.setBigDecimal(4, bill.getDiscount());
            pstmt.setBigDecimal(5, bill.getFinalAmount());
            pstmt.setBigDecimal(6, bill.getCashReceived() == null ? BigDecimal.ZERO : bill.getCashReceived());
            pstmt.setBigDecimal(7, bill.getCashReturned() == null ? BigDecimal.ZERO : bill.getCashReturned());
            pstmt.setString(8, bill.getStatus() == null ? "REVISED" : bill.getStatus());
            pstmt.setString(9, bill.getBillId());
            pstmt.executeUpdate();
        }
    }

    public void updateBillItem(BillItem item, Connection conn) throws SQLException {
        String sql = "UPDATE bill_items SET quantity = ?, taxable_value = ?, cgst = ?, sgst = ?, discount = ?, final_amount = ? WHERE bill_item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getQuantity());
            pstmt.setBigDecimal(2, item.getTaxableValue());
            pstmt.setBigDecimal(3, item.getCgst());
            pstmt.setBigDecimal(4, item.getSgst());
            pstmt.setBigDecimal(5, item.getDiscount());
            pstmt.setBigDecimal(6, item.getFinalAmount());
            pstmt.setString(7, item.getBillItemId());
            pstmt.executeUpdate();
        }
    }

    public void deleteBillItem(String billItemId, Connection conn) throws SQLException {
        String sql = "DELETE FROM bill_items WHERE bill_item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, billItemId);
            pstmt.executeUpdate();
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

    public static class PurchaseStats {
        public int monthQty = 0;
        public double monthAmount = 0.0;
        public int fyQty = 0;
        public double fyAmount = 0.0;
    }

    public PurchaseStats getPurchaseStats(String customerId) {
        PurchaseStats stats = new PurchaseStats();
        if (customerId == null || customerId.trim().isEmpty()) {
            return stats;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime monthStart = LocalDateTime.of(now.getYear(), now.getMonthValue(), 1, 0, 0, 0);
            
            int fyStartYear = now.getYear();
            if (now.getMonthValue() < 4) {
                fyStartYear -= 1;
            }
            LocalDateTime fyStart = LocalDateTime.of(fyStartYear, 4, 1, 0, 0, 0);

            String query = "SELECT " +
                           "  (SELECT COALESCE(SUM(bi.quantity), 0) FROM bill_items bi JOIN bills b ON bi.bill_id = b.bill_id WHERE b.customer_id = ? AND b.bill_date >= ?) AS month_qty, " +
                           "  (SELECT COALESCE(SUM(b.final_amount), 0) FROM bills b WHERE b.customer_id = ? AND b.bill_date >= ?) AS month_amount, " +
                           "  (SELECT COALESCE(SUM(bi.quantity), 0) FROM bill_items bi JOIN bills b ON bi.bill_id = b.bill_id WHERE b.customer_id = ? AND b.bill_date >= ?) AS fy_qty, " +
                           "  (SELECT COALESCE(SUM(b.final_amount), 0) FROM bills b WHERE b.customer_id = ? AND b.bill_date >= ?) AS fy_amount";
            
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, customerId);
            pstmt.setTimestamp(2, Timestamp.valueOf(monthStart));
            pstmt.setString(3, customerId);
            pstmt.setTimestamp(4, Timestamp.valueOf(monthStart));
            pstmt.setString(5, customerId);
            pstmt.setTimestamp(6, Timestamp.valueOf(fyStart));
            pstmt.setString(7, customerId);
            pstmt.setTimestamp(8, Timestamp.valueOf(fyStart));
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.monthQty = rs.getInt("month_qty");
                stats.monthAmount = rs.getDouble("month_amount");
                stats.fyQty = rs.getInt("fy_qty");
                stats.fyAmount = rs.getDouble("fy_amount");
            }
        } catch (SQLException e) {
            System.err.println("Database error in BillRepository.getPurchaseStats: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return stats;
    }
}
