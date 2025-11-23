package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javax.sql.DataSource;

public class RentManagerImplementation implements RentManager {

    @Override
    public Customer findCustomerWithCar(Car car) throws IllegalArgumentException, TransactionException {
        if (null == car) {
            throw new IllegalArgumentException("Can't find Car with NULL pointer");
        }
        if (null == car.ID()) {
            throw new IllegalArgumentException("Can't find Car with NULL ID");
        }
        if (car.available()) {
            throw new IllegalArgumentException("Car is NOT RENTED");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Rent> query = session.createQuery(
                    "FROM Rent WHERE carID = :carId", Rent.class);
            query.setParameter("carId", car.ID());
            List<Rent> rents = query.list();
            if (rents.isEmpty()) {
                return null;
            }
            if (rents.size() > 1) {
                throw new IllegalArgumentException("Multiple Customers with same Car");
            }
            return customerManager.findCustomerByID(rents.get(0).customerID());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when getting customer from DB", ex);
            throw new TransactionException("Error when getting customer from DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public Rent findRentWithCar(Car car) throws IllegalArgumentException, TransactionException {
        if (null == car) {
            throw new IllegalArgumentException("Can't find Car with NULL pointer");
        }
        if (null == car.ID()) {
            throw new IllegalArgumentException("Can't find Car with NULL ID");
        }
        if (car.available()) {
            throw new IllegalArgumentException("Car is NOT RENTED");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Rent> query = session.createQuery(
                    "FROM Rent WHERE carID = :carId", Rent.class);
            query.setParameter("carId", car.ID());
            List<Rent> rents = query.list();
            if (rents.isEmpty()) {
                return null;
            }
            if (rents.size() > 1) {
                throw new IllegalArgumentException("Multiple Rents with same Car");
            }
            return rents.get(0);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when getting rent from DB", ex);
            throw new TransactionException("Error when getting rent from DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Rent> getAllRents() throws IllegalArgumentException, TransactionException {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Rent> query = session.createQuery("FROM Rent", Rent.class);
            return query.list();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when getting all Customers", ex);
            throw new TransactionException("Error when getting all Customers", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Car> getAllCustomerCars(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("CUSTOMER POINTS TO NULL");
        }
        if (null == customer.ID()) {
            throw new IllegalArgumentException("CUSTOMER ID IN NULL");
        }
        if (!customer.active()) {
            throw new IllegalArgumentException("CUSTOMER IS NOT ACTIVE");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Rent> query = session.createQuery(
                    "FROM Rent WHERE customerID = :customerId", Rent.class);
            query.setParameter("customerId", customer.ID());
            List<Rent> rents = query.list();
            List<Car> customerCars = new java.util.ArrayList<>();
            for (Rent rent : rents) {
                Car car = carManager.findCarByID(rent.carID());
                if (car != null) {
                    customerCars.add(car);
                }
            }
            return customerCars;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when getting Cars from Rent DB", ex);
            throw new TransactionException("Error when getting Cars from Rent DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public void rentCarToCustomer(Car car, Customer customer, Date rentDate, Date dueDate) throws IllegalArgumentException, TransactionException {
        if ((null == car) || (null == customer)) {
            throw new IllegalArgumentException("Can't insert null entry to DB");
        }
        if (null == car.ID()) {
            throw new IllegalArgumentException("Car ID is NULL");
        }
        if (null == customer.ID()) {
            throw new IllegalArgumentException("Customer ID is NULL");
        }
        if (!car.available()) {
            throw new IllegalArgumentException("Car is already rented");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Rent rent = Rent.create(rentDate, dueDate, car.ID(), customer.ID());
            session.persist(rent);
            transaction.commit();
            logger.log(Level.INFO, ("New Rent ID " + rent.ID() + " added"));

            carManager.updateCarInfo(car.withStatus(Boolean.FALSE));
            customerManager.updateCustomerInfo(customer.withActive(Boolean.TRUE));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error when renting car to customer in RentDB", ex);
            throw new TransactionException("Error when renting car to customer in RentDB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public void getCarFromCustomer(Car car, Customer customer) throws IllegalArgumentException, TransactionException {
        if ((null == car) || (null == customer)) {
            throw new IllegalArgumentException("Can't use NULL entry");
        }
        if ((null == car.ID()) || (null == customer.ID())) {
            throw new IllegalArgumentException("Customer or Car ID is NULL not exist in DB");
        }
        if (!customer.active()) {
            throw new IllegalArgumentException("Customer is not active");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Query<Rent> query = session.createQuery(
                    "FROM Rent WHERE customerID = :customerId AND carID = :carId", Rent.class);
            query.setParameter("customerId", customer.ID());
            query.setParameter("carId", car.ID());
            List<Rent> rents = query.list();
            if (rents.isEmpty()) {
                throw new TransactionException("Rent not found");
            }
            for (Rent rent : rents) {
                session.remove(rent);
            }
            transaction.commit();

            if (getAllCustomerCars(customer).isEmpty()) {
                customerManager.updateCustomerInfo(customer.withActive(Boolean.FALSE));
            } else {
                customerManager.updateCustomerInfo(customer);
            }
            carManager.updateCarInfo(car.withStatus(true));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error when DELETE from DB", ex);
            throw new TransactionException("Error when DELETE from DB", ex);
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

    @Override
    public void setDataSource(DataSource dataSource) {
        // Deprecated - using Hibernate SessionFactory instead
        // Keep for backward compatibility
        carManager.setDataSource(dataSource);
        customerManager.setDataSource(dataSource);
    }

    public static final Logger logger = Logger.getLogger(CarManagerImplementation.class.getName());
    private CustomerManager customerManager = new CustomerManagerImplementation();
    private CarManager carManager = new CarManagerImplementation();

    @Override
    public void setLogger(FileOutputStream fs) {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
}
