package model.service;

import model.dao.UserDao;
import model.entities.User;

public class UserService {
    private UserDao userDAO = new UserDao();

    public User login(String username, String password) throws Exception {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }
}