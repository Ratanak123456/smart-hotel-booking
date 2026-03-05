package model.service;

import model.dao.UserDao;
import model.entities.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDao userDAO = new UserDao();
    private static final int ROWS_PER_PAGE = 5;

    public User login(String username, String password) throws Exception {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean register(String username, String email, String phoneNumber, String password) throws Exception {
        // Validation: Email must end with @gmail.com
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            throw new Exception("Invalid email. Email must end with @gmail.com.");
        }

        // Validation: Strong password
        // At least 8 characters, one uppercase, one lowercase, one number, and one special character.
        if (!isValidPassword(password)) {
            throw new Exception("Password is too weak. It must be at least 8 characters long, " +
                    "contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }

        // Check if username or email already exists
        if (userDAO.findByUsername(username) != null) {
            throw new Exception("Username is already taken.");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new Exception("Email is already registered.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setPasswordHash(password); // In a real app, hash this!
        newUser.setRole("USER"); // Default to USER as requested

        return userDAO.save(newUser);
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        String specialChars = "!@#$%^&*()-_=+[]{}|;:',.<>?/";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (specialChars.contains(String.valueOf(c))) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public List<User> getAllUsersPaginated(int page) throws SQLException {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return userDAO.findAllPaginated(ROWS_PER_PAGE, offset);
    }

    public int getTotalUsersCount() throws SQLException {
        return userDAO.countAll();
    }

    public List<User> searchUsersByUsername(String username, int page) throws SQLException {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return userDAO.searchByUsername(username, ROWS_PER_PAGE, offset);
    }

    public int getSearchUsersCount(String username) throws SQLException {
        return userDAO.countSearchByUsername(username);
    }

    public boolean deleteUser(int userId) throws SQLException {
        return userDAO.softDelete(userId);
    }
}