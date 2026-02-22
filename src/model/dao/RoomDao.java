package model.dao;

import db.DbConfig;
import model.entities.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    public List<Room> getAllRoomsPaginated(int limit, int offset) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.room_number, r.room_type_id, r.price_per_night, r.status, r.description, rt.name as room_type_name " +
                "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                "ORDER BY r.id LIMIT ? OFFSET ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        }
        return rooms;
    }

    public List<Room> getRoomsByType(String roomTypeName, int limit, int offset) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.room_number, r.room_type_id, r.price_per_night, r.status, r.description, rt.name as room_type_name " +
                "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE rt.name = ? ORDER BY r.id LIMIT ? OFFSET ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomTypeName);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        }
        return rooms;
    }

    // FIXED: cast status parameter to enum
    public List<Room> getRoomsByStatus(String status, int limit, int offset) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.room_number, r.room_type_id, r.price_per_night, r.status, r.description, rt.name as room_type_name " +
                "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE r.status = CAST(? AS room_status_enum) ORDER BY r.id LIMIT ? OFFSET ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        }
        return rooms;
    }

    // FIXED: cast status parameter to enum
    public List<Room> getRoomsByTypeAndStatus(String roomTypeName, String status, int limit, int offset) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.room_number, r.room_type_id, r.price_per_night, r.status, r.description, rt.name as room_type_name " +
                "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE rt.name = ? AND r.status = CAST(? AS room_status_enum) ORDER BY r.id LIMIT ? OFFSET ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomTypeName);
            stmt.setString(2, status);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        }
        return rooms;
    }

    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT r.id, r.room_number, r.room_type_id, r.price_per_night, r.status, r.description, rt.name as room_type_name " +
                "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id WHERE r.id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToRoom(rs);
            }
        }
        return null;
    }

    public long getTotalRoomsCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM rooms";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("count");
            }
        }
        return 0;
    }

    public long getRoomCountByType(String roomTypeName) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id WHERE rt.name = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomTypeName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("count");
            }
        }
        return 0;
    }

    // FIXED: cast status parameter to enum
    public long getRoomCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM rooms WHERE status = CAST(? AS room_status_enum)";
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

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getInt("id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomTypeId(rs.getInt("room_type_id"));
        room.setPricePerNight(rs.getBigDecimal("price_per_night"));
        room.setStatus(Room.RoomStatus.valueOf(rs.getString("status")));
        room.setDescription(rs.getString("description"));
        room.setRoomTypeName(rs.getString("room_type_name"));
        return room;
    }
}