package uz.mini.twitter.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {

    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("mini-twitter-pu");

    private HibernateUtil() {
    }

    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }

    public static void shutdown() {
        FACTORY.close();
    }
}
