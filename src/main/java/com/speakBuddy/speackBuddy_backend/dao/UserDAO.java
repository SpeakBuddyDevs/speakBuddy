package com.speakBuddy.speackBuddy_backend.dao;

import com.speakBuddy.speackBuddy_backend.models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

public class UserDAO {
    private static final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

    public User findByEmail(String emailLogin) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :emailLogin", User.class);
            query.setParameter("email", emailLogin);
            return query.uniqueResult();
        }
    }


}
