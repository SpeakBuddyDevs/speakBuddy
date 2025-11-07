package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dao.UserDAO;
import com.speakBuddy.speackBuddy_backend.models.User;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.logging.Logger;

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    Logger log = Logger.getLogger(AuthService.class.getName());

    @Getter
    private User userLogged;

    private boolean validateUser(String email, String password) {
        User user = userDAO.findByEmail(email);

        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            userLogged = user;
            log.info("User logged successfully");
            return true; //El usuario no existe
        }
        else {
            log.info("User not found");
            return false;
        }
    }

    private void logout(){
        if (userLogged != null){
            log.info("Logout from user: " + userLogged.getEmail());
            userLogged = null;
        }else {
            log.info("There is no user logged" );
        }
    }



}
