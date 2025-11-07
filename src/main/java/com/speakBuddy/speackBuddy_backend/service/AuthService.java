package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dao.UserDAO;
import com.speakBuddy.speackBuddy_backend.models.User;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    private boolean validateUser(String email, String password) {
        User user = userDAO.findByEmail(email);

        if (user == null) {
            return false; //El usuario no existe
        }

        return BCrypt.checkpw(password, user.getPassword());
    }

}
