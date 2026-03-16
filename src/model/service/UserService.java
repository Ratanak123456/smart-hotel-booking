package model.service;

import model.dao.UserDao;
import model.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import exception.ValidationException;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDao userDAO = new UserDao();
    private static final int ROWS_PER_PAGE = 5;

    public User login(String username, String password) throws Exception {
        User user = userDAO.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public boolean register(String username, String email, String phoneNumber, String password) throws Exception {
        return register(username, email, phoneNumber, password, null);
    }

    public boolean register(String username, String email, String phoneNumber, String password, Long telegramChatId) throws Exception {
        // Validation: Username must not contain spaces
        if (username.contains(" ")) {
            throw new ValidationException("Username cannot contain spaces.");
        }

        // Validation: Email must end with @gmail.com
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            throw new ValidationException("Invalid email. Email must end with @gmail.com.");
        }

        // Validation: Phone number must be at least 8 characters
        if (phoneNumber == null || phoneNumber.trim().length() < 8) {
            throw new ValidationException("Phone number must be at least 8 characters long.");
        }

        // Validation: Strong password
        validatePassword(password);

        // Check if username or email already exists in database
        if (userDAO.findByUsername(username) != null) {
            throw new ValidationException("Username is already taken. Please choose another one.");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new ValidationException("Email is already registered. Please use another email.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPhoneNumber(phoneNumber);
        
        // Hash the password before saving
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        newUser.setPasswordHash(hashedPassword);
        
        newUser.setRole("USER"); // Default to USER as requested
        newUser.setTelegramChatId(telegramChatId);

        return userDAO.save(newUser);
    }

    private void validatePassword(String password) throws ValidationException {
        if (password.length() < 8) {
            throw new ValidationException("Password is too short. It must be at least 8 characters long.");
        }

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

        if (!hasUpper) {
            throw new ValidationException("Password must contain at least one uppercase letter.");
        }
        if (!hasLower) {
            throw new ValidationException("Password must contain at least one lowercase letter.");
        }
        if (!hasDigit) {
            throw new ValidationException("Password must contain at least one digit.");
        }
        if (!hasSpecial) {
            throw new ValidationException("Password must contain at least one special character.");
        }
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

    public boolean updateTelegramChatId(int userId, long chatId) throws SQLException {
        return userDAO.updateTelegramChatId(userId, chatId);
    }
}