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