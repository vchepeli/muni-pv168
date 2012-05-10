package cz.muni.fi.pv168;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Singleton factory for managing Hibernate SessionFactory.
 * Provides thread-safe access to sessions for database operations.
 */
public class HibernateSessionFactory {
    private static volatile SessionFactory sessionFactory;

    /**
     * Get or create the SessionFactory instance.
     * Uses double-checked locking for thread-safe lazy initialization.
     *
     * @return the Hibernate SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateSessionFactory.class) {
                if (sessionFactory == null) {
                    try {
                        Configuration configuration = new Configuration();
                        configuration.configure("hibernate.cfg.xml");
                        sessionFactory = configuration.buildSessionFactory();
                    } catch (Throwable ex) {
                        throw new ExceptionInInitializerError(ex);
                    }
                }
            }
        }
        return sessionFactory;
    }

    /**
     * Shutdown the SessionFactory and release all resources.
     * Should be called when the application is shutting down.
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            try {
                sessionFactory.close();
            } catch (Exception ex) {
                System.err.println("Error closing SessionFactory: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                sessionFactory = null;
            }
        }
    }

    /**
     * Reinitialize the SessionFactory (useful for testing).
     */
    public static void reinitialize() {
        shutdown();
        sessionFactory = null;
    }
}
