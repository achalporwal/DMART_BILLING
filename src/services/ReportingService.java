package services;

import db.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ReportingService {

    public static class SalesReportItem {
        public String cashierId;
        public String cashierName;
        public int totalBills;
        public BigDecimal totalRevenue;

        public SalesReportItem(String cashierId, String cashierName, int totalBills, BigDecimal totalRevenue) {
            this.cashierId = cashierId;
            this.cashierName = cashierName;
            this.totalBills = totalBills;
            this.totalRevenue = totalRevenue;
        }
    }

    public List<SalesReportItem> getDailySalesReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return getSalesReport(startOfDay, endOfDay);
    }

    public List<SalesReportItem> getMonthlySalesReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = ym.plusMonths(1).atDay(1).atStartOfDay();
        return getSalesReport(startOfMonth, endOfMonth);
    }

    private List<SalesReportItem> getSalesReport(LocalDateTime start, LocalDateTime end) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<SalesReportItem> report = new ArrayList<>();
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT u.user_id, u.name, COUNT(b.bill_id) as total_bills, COALESCE(SUM(b.final_amount), 0) as total_revenue " +
                         "FROM users u " +
                         "LEFT JOIN bills b ON u.user_id = b.cashier_id AND b.bill_date >= ? AND b.bill_date < ? " +
                         "WHERE u.role = 'SUB_ADMIN' " +
                         "GROUP BY u.user_id, u.name " +
                         "ORDER BY total_revenue DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                report.add(new SalesReportItem(
                    rs.getString("user_id"),
                    rs.getString("name"),
                    rs.getInt("total_bills"),
                    rs.getBigDecimal("total_revenue")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database error in ReportingService.getSalesReport: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
            DatabaseConfig.releaseConnection(conn);
        }
        return report;
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
