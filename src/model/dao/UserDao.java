package model.dao;

import db.DbConfig;
import model.entities.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND deleted_at IS NULL";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                return user;
            }
        }
        return null;
    }

    public List<User> findAllPaginated(int limit, int offset) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE deleted_at IS NULL LIMIT ? OFFSET ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setRole(rs.getString("role"));
                users.add(u);
            }
        }
        return users;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE deleted_at IS NULL";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<User> searchByUsername(String username, int limit, int offset) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE username LIKE ? AND deleted_at IS NULL LIMIT ? OFFSET ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + username + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setRole(rs.getString("role"));
                users.add(u);
            }
        }
        return users;
    }

    public int countSearchByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username LIKE ? AND deleted_at IS NULL";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + username + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean softDelete(int userId) throws SQLException {
        String sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}