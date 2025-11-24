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
        if (null == car.uuid()) {
            throw new IllegalArgumentException("Can't find Car with NULL ID");
        }
        if (car.available()) {
            throw new IllegalArgumentException("Car is NOT RENTED");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Rent> query = session.createQuery(
                    "FROM Rent WHERE carID = :carId", Rent.class);
            query.setParameter("carId", car.uuid());
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
        if (null == car.uuid()) {
            throw new IllegalArgumentException("Can't find Car with NULL ID");
        }
        if (car.available()) {
            throw new IllegalArgumentException("Car is NOT RENTED");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Rent> query = session.createQuery(
                    "FROM Rent WHERE carID = :carId", Rent.class);
            query.setParameter("carId", car.uuid());
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
        if (null == customer.uuid()) {
            throw new IllegalArgumentException("CUSTOMER ID IN NULL");
        }
        if (!customer.active()) {
            throw new IllegalArgumentException("CUSTOMER IS NOT ACTIVE");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Rent> query = session.createQuery(
                    "FROM Rent WHERE customerID = :customerId", Rent.class);
            query.setParameter("customerId", customer.uuid());
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
    public void addRent(Rent rent) throws IllegalArgumentException, TransactionException {
        if (null == rent) {
            throw new IllegalArgumentException("Can't insert null entry to DB");
        }
        if (null == rent.uuid()) {
            throw new IllegalArgumentException("Rent ID is NULL");
        }
        
        // Verify Car and Customer exist
        Car car = carManager.findCarByID(rent.carID());
        Customer customer = customerManager.findCustomerByID(rent.customerID());
        
        if (car == null || customer == null) {
             throw new IllegalArgumentException("Car or Customer does not exist");
        }

        if (rent.rentDate().after(rent.dueDate())) {
            throw new IllegalArgumentException("Rent start date must be before or equal to end date");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            
            // Check for overlapping rents for the same car
            Query<Long> overlapQuery = session.createQuery(
                "SELECT count(r) FROM Rent r WHERE r.carID = :carId AND " +
                "((:start BETWEEN r.rentDate AND r.dueDate) OR " +
                "(:end BETWEEN r.rentDate AND r.dueDate) OR " +
                "(r.rentDate BETWEEN :start AND :end))", Long.class);
            
            overlapQuery.setParameter("carId", rent.carID());
            overlapQuery.setParameter("start", rent.rentDate());
            overlapQuery.setParameter("end", rent.dueDate());
            
            if (overlapQuery.uniqueResult() > 0) {
                throw new IllegalArgumentException("Car is already rented for the selected period");
            }

            session.persist(rent);
            transaction.commit();
            logger.log(Level.INFO, ("New Rent ID " + rent.uuid() + " added"));
            session.close();

            carManager.updateCarInfo(car.withStatus(Boolean.FALSE));
            customerManager.updateCustomerInfo(customer.withActive(Boolean.TRUE));
        } catch (IllegalArgumentException ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        } catch (Exception ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error when adding rent to DB", ex);
            throw new TransactionException("Error when adding rent to DB", ex);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void rentCarToCustomer(Car car, Customer customer, Date rentDate, Date dueDate) throws IllegalArgumentException, TransactionException {
        if (car == null) {
            throw new IllegalArgumentException("Car argument is null");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer argument is null");
        }
        // Delegate to addRent by creating a new Rent object (maintaining legacy behavior if needed, though mostly unused now)
        Rent rent = Rent.create(rentDate, dueDate, car.uuid(), customer.uuid());
        addRent(rent);
    }

    @Override
    public void getCarFromCustomer(Car car, Customer customer) throws IllegalArgumentException, TransactionException {
        if ((null == car) || (null == customer)) {
            throw new IllegalArgumentException("Can't use NULL entry");
        }
        if ((null == car.uuid()) || (null == customer.uuid())) {
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
            query.setParameter("customerId", customer.uuid());
            query.setParameter("carId", car.uuid());
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
        } catch (IllegalArgumentException ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
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

    @Override
    public void updateRent(Rent rent) throws IllegalArgumentException, TransactionException {
        if (null == rent) {
            throw new IllegalArgumentException("Can't update NULL rent");
        }
        if (null == rent.uuid()) {
            throw new IllegalArgumentException("Can't update rent with NULL ID");
        }

        if (rent.rentDate().after(rent.dueDate())) {
            throw new IllegalArgumentException("Rent start date must be before or equal to end date");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            
            // Check for overlapping rents for the same car
            Query<Long> overlapQuery = session.createQuery(
                "SELECT count(r) FROM Rent r WHERE r.carID = :carId AND " +
                "r.id != :rentId AND " + // Exclude itself for updates (though UUID is unique, good practice)
                "((:start BETWEEN r.rentDate AND r.dueDate) OR " +
                "(:end BETWEEN r.rentDate AND r.dueDate) OR " +
                "(r.rentDate BETWEEN :start AND :end))", Long.class);
            
            overlapQuery.setParameter("carId", rent.carID());
            overlapQuery.setParameter("rentId", rent.uuid());
            overlapQuery.setParameter("start", rent.rentDate());
            overlapQuery.setParameter("end", rent.dueDate());
            
            if (overlapQuery.uniqueResult() > 0) {
                throw new IllegalArgumentException("Car is already rented for the selected period");
            }

            if (session.get(Rent.class, rent.uuid()) == null) {
                throw new TransactionException("Rent with ID " + rent.uuid() + " does not exist in DB");
            }
            session.merge(rent);
            transaction.commit();
            logger.log(Level.INFO, ("Rent ID " + rent.uuid() + " updated"));
        } catch (IllegalArgumentException ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        } catch (Exception ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error when updating Rent in DB", ex);
            throw new TransactionException("Error when updating Rent in DB", ex);
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
