package model.dao;

import db.DbConfig;
import model.entities.Invoice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDao {

    public int insert(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (booking_id, invoice_number, issue_date, guest_name, " +
                "guest_email, guest_phone, room_number, room_type_name, check_in_date, check_out_date, " +
                "nights, price_per_night, discount_amount, total_amount, invoice_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS invoice_status_enum)) RETURNING id";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoice.getBookingId());
            stmt.setString(2, invoice.getInvoiceNumber());
            stmt.setDate(3, Date.valueOf(invoice.getIssueDate()));
            stmt.setString(4, invoice.getGuestName());
            stmt.setString(5, invoice.getGuestEmail());
            stmt.setString(6, invoice.getGuestPhone());
            stmt.setString(7, invoice.getRoomNumber());
            stmt.setString(8, invoice.getRoomTypeName());
            stmt.setDate(9, Date.valueOf(invoice.getCheckInDate()));
            stmt.setDate(10, Date.valueOf(invoice.getCheckOutDate()));
            stmt.setInt(11, invoice.getNights());
            stmt.setBigDecimal(12, invoice.getPricePerNight());
            stmt.setBigDecimal(13, invoice.getDiscountAmount());
            stmt.setBigDecimal(14, invoice.getTotalAmount());
            stmt.setString(15, invoice.getInvoiceStatus().name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    public Invoice getInvoiceById(int id) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToInvoice(rs);
            }
        }
        return null;
    }

    public Invoice getInvoiceByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE booking_id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToInvoice(rs);
            }
        }
        return null;
    }

    public List<Invoice> getInvoicesByUserId(int userId) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.* FROM invoices i " +
                "JOIN bookings b ON i.booking_id = b.id " +
                "WHERE b.user_id = ? " +
                "ORDER BY i.created_at DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        }
        return invoices;
    }

    public List<Invoice> getInvoicesByUserIdPaginated(int userId, int limit, int offset) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.* FROM invoices i " +
                "JOIN bookings b ON i.booking_id = b.id " +
                "WHERE b.user_id = ? " +
                "ORDER BY i.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        }
        return invoices;
    }

    public long getInvoiceCountByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM invoices i " +
                "JOIN bookings b ON i.booking_id = b.id " +
                "WHERE b.user_id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("count");
            }
        }
        return 0;
    }

    public boolean updateStatus(int invoiceId, String status) throws SQLException {
        String sql = "UPDATE invoices SET invoice_status = CAST(? AS invoice_status_enum) WHERE id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, invoiceId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("id"));
        invoice.setBookingId(rs.getInt("booking_id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));
        invoice.setIssueDate(rs.getDate("issue_date").toLocalDate());
        invoice.setGuestName(rs.getString("guest_name"));
        invoice.setGuestEmail(rs.getString("guest_email"));
        invoice.setGuestPhone(rs.getString("guest_phone"));
        invoice.setRoomNumber(rs.getString("room_number"));
        invoice.setRoomTypeName(rs.getString("room_type_name"));
        invoice.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        invoice.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        invoice.setNights(rs.getInt("nights"));
        invoice.setPricePerNight(rs.getBigDecimal("price_per_night"));
        invoice.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        invoice.setInvoiceStatus(Invoice.InvoiceStatus.valueOf(rs.getString("invoice_status")));
        invoice.setCreatedAt(rs.getTimestamp("created_at"));
        return invoice;
    }
}
