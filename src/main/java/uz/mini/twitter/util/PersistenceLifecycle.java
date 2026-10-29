package uz.mini.twitter.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class PersistenceLifecycle implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Fail at startup if the database cannot be reached, rather than on a user's first request.
        HibernateUtil.initialize();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        HibernateUtil.shutdown();
    }
}
