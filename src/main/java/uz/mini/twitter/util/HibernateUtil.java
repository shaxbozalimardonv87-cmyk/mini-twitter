package uz.mini.twitter.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {

    private static final EntityManagerFactory FACTORY =
            createFactory();

    private static EntityManagerFactory createFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", setting("DB_URL", "jdbc:postgresql://localhost:5432/mini_twitter"));
        properties.put("jakarta.persistence.jdbc.user", setting("DB_USERNAME", "postgres"));
        properties.put("jakarta.persistence.jdbc.password", setting("DB_PASSWORD", "postgres"));
        properties.put("jakarta.persistence.jdbc.driver", setting("DB_DRIVER", "org.postgresql.Driver"));
        properties.put("hibernate.hbm2ddl.auto", setting("DB_SCHEMA_ACTION", "update"));
        return Persistence.createEntityManagerFactory("mini-twitter-pu", properties);
    }

    private static String setting(String name, String fallback) {
        String value = System.getProperty(name);
        if (value == null) value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private HibernateUtil() {
    }

    public static void initialize() {
        // Calling this method initializes the static factory.
    }

    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }

    public static void shutdown() {
        if (FACTORY.isOpen()) FACTORY.close();
    }
}
