package model.dao;

import db.DbConfig;
import model.entities.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    public int insert(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, room_id, check_in_date, check_out_date, " +
                "base_price, discount_amount, total_price, status, telegram_chat_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS booking_status_enum), ?) RETURNING id";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, booking.getUserId());
            stmt.setInt(2, booking.getRoomId());
            stmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            stmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            stmt.setBigDecimal(5, booking.getBasePrice());
            stmt.setBigDecimal(6, booking.getDiscountAmount());
            stmt.setBigDecimal(7, booking.getTotalPrice());
            stmt.setString(8, booking.getStatus().name());
            if (booking.getTelegramChatId() != null) {
                stmt.setLong(9, booking.getTelegramChatId());
            } else {
                stmt.setNull(9, Types.BIGINT);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    public Booking getBookingById(int id) throws SQLException {
        String sql = "SELECT b.*, r.room_number, rt.name as room_type_name, " +
                "u.username, u.email as user_email, u.phone_number as user_phone " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBooking(rs);
            }
        }
        return null;
    }

    public List<Booking> getBookingsByUserId(int userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, rt.name as room_type_name, " +
                "u.username, u.email as user_email, u.phone_number as user_phone " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.user_id = ? " +
                "ORDER BY b.created_at DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    public List<Booking> getBookingsByUserIdPaginated(int userId, int limit, int offset) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, rt.name as room_type_name, " +
                "u.username, u.email as user_email, u.phone_number as user_phone " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.user_id = ? " +
                "ORDER BY b.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    public long getBookingCountByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM bookings WHERE user_id = ?";
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

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, rt.name as room_type_name, " +
                "u.username, u.email as user_email, u.phone_number as user_phone " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "JOIN users u ON b.user_id = u.id " +
                "ORDER BY b.created_at DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    public List<Booking> getBookingsByStatus(String status) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, rt.name as room_type_name, " +
                "u.username, u.email as user_email, u.phone_number as user_phone " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.status = CAST(? AS booking_status_enum) " +
                "ORDER BY b.created_at DESC";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    public List<Booking> getBookingsByStatusPaginated(String status, int limit, int offset) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, rt.name as room_type_name, " +
                "u.username, u.email as user_email, u.phone_number as user_phone " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.status = CAST(? AS booking_status_enum) " +
                "ORDER BY b.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    public long getBookingCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM bookings WHERE status = CAST(? AS booking_status_enum)";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("count");
            }
        }
        return 0;
    }

    public boolean updateStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET status = CAST(? AS booking_status_enum), updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, bookingId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    public boolean updateRoomStatus(int roomId, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = CAST(? AS room_status_enum) WHERE id = ?";

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, roomId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        }
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("id"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setRoomId(rs.getInt("room_id"));
        booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        booking.setBasePrice(rs.getBigDecimal("base_price"));
        booking.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        booking.setTotalPrice(rs.getBigDecimal("total_price"));
        booking.setStatus(Booking.BookingStatus.valueOf(rs.getString("status")));

        Long telegramChatId = rs.getLong("telegram_chat_id");
        if (!rs.wasNull()) {
            booking.setTelegramChatId(telegramChatId);
        }

        booking.setCreatedAt(rs.getTimestamp("created_at"));

        // Additional fields
        booking.setRoomNumber(rs.getString("room_number"));
        booking.setRoomTypeName(rs.getString("room_type_name"));
        booking.setUsername(rs.getString("username"));
        booking.setUserEmail(rs.getString("user_email"));
        booking.setUserPhone(rs.getString("user_phone"));

        return booking;
    }
}
