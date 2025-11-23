package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javax.sql.DataSource;

public class CustomerManagerImplementation implements CustomerManager {

    @Override
    public void setDataSource(DataSource dataSource) {
        // Deprecated - using Hibernate SessionFactory instead
        // Keep for backward compatibility
    }

    @Override
    public void addCustomer(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("Can't INSERT NULL entry");
        }
        if (null == customer.ID()) {
            throw new IllegalArgumentException("Customer ID is NULL");
        }
        if (null == customer.firstName() || null == customer.lastName() || null == customer.address() || null == customer.phoneNumber()
                || null == customer.driversLicense()) {
            throw new IllegalArgumentException("Customer with WRONG PARAMETERS");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(customer);
            transaction.commit();
            logger.log(Level.INFO, ("New Customer ID " + customer.ID() + " added"));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error INSERT Customer to DB", ex);
            throw new TransactionException("Error INSERT Customer to DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public void removeCustomer(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("Can not DELETE NULL ENTRY from Customers");
        }
        if (null == customer.ID()) {
            throw new IllegalArgumentException("Can't DELETE Customer with NULL ID");
        }
        if (customer.active()) {
            throw new IllegalArgumentException("Can't DELETE active Customer");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Customer managedCustomer = session.get(Customer.class, customer.ID());
            if (managedCustomer == null) {
                throw new IllegalArgumentException("Can't locate Customer in DB");
            }
            session.remove(managedCustomer);
            transaction.commit();
            logger.log(Level.INFO, ("Customer ID " + customer.ID() + " removed"));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error DELETE Customer from DB", ex);
            throw new TransactionException("Error DELETE Customer from DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public Customer findCustomerByID(String ID) throws IllegalArgumentException, TransactionException {
        if (null == ID) {
            throw new IllegalArgumentException("Can't find Customer with NULL ID");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Customer customer = session.get(Customer.class, ID);
            return customer;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when gettind Customer from DB", ex);
            throw new TransactionException("Error when gettind Customer from DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Customer> getAllCustomers() throws IllegalArgumentException, TransactionException {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Customer> query = session.createQuery("FROM Customer", Customer.class);
            return query.list();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when getting all Customers", ex);
            throw new TransactionException("Error when getting all Customers", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public void updateCustomerInfo(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("Can't UPDATE NULL ENTRY to CustomersDB");
        }
        if (null == customer.ID()) {
            throw new IllegalArgumentException("Can't UPDATE Customer with WRONG ID");
        }
        if ((null == customer.firstName()) || (null == customer.lastName()) || (null == customer.address())
                || (null == customer.phoneNumber()) || (null == customer.driversLicense())) {
            throw new IllegalArgumentException("Customer with WRONG PARAMETRS");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Customer existingCustomer = session.get(Customer.class, customer.ID());
            if (existingCustomer == null) {
                throw new TransactionException("Customer with ID " + customer.ID() + " does not exist in DB");
            }
            session.merge(customer);
            transaction.commit();
            logger.log(Level.INFO, ("Customer ID " + customer.ID() + " updated"));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error when UPDATE Customer in DB", ex);
            throw new TransactionException("Error when UPDATE Customer in DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Customer> getActiveCustomers() throws IllegalArgumentException, TransactionException {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Customer> query = session.createQuery(
                    "FROM Customer WHERE active = true", Customer.class);
            return query.list();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when getting all Customers from DB", ex);
            throw new TransactionException("Error when getting all Customers from DB", ex);
        } finally {
            session.close();
        }
    }

    public void tryCreateTables() {
        try {
            // Hibernate auto-creates tables based on hibernate.cfg.xml hbm2ddl.auto setting
            HibernateSessionFactory.getSessionFactory().openSession().close();
        } catch (Exception ex) {
            throw new IllegalStateException("Error when trying to create tables", ex);
        }
    }

    public static final Logger logger = Logger.getLogger(CustomerManagerImplementation.class.getName());

    @Override
    public void setLogger(FileOutputStream fs) {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
}
