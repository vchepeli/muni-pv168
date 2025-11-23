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

public class CarManagerImplementation implements CarManager {

    @Override
    public void setDataSource(DataSource dataSource) {
        // Deprecated - using Hibernate SessionFactory instead
        // Keep for backward compatibility
    }

    @Override
    public void addCar(Car car) throws TransactionException {
        //Check if the Arguments are valid:
        if (null == car) {
            throw new IllegalArgumentException("Can not INSERT NULL ENTRY to CARS");
        }
        if (null == car.ID()) {
            throw new IllegalArgumentException("Car ID is NULL");
        }
        if ((null == car.color()) || (null == car.licensePlate()) || (null == car.model())
                || (null == car.rentalPayment()) || (car.rentalPayment() < 0)) {
            throw new IllegalArgumentException("Car with WRONG PARAMETERS");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(car);
            transaction.commit();
            logger.log(Level.INFO, ("New Car ID " + car.ID() + " added"));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error when INSERT Car into DB" + car, ex);
            throw new TransactionException("Error when INSERT Car into DB" + car, ex);
        } finally {
            session.close();
        }
    }

    @Override
    public void removeCar(Car car) throws TransactionException {
        //Check if the Argument is valid:
        if (null == car) {
            throw new IllegalArgumentException("Can't DELETE NULL ENTRY from CarDB");
        }
        if (null == car.ID()) {
            throw new IllegalArgumentException("Can't DELETE Car with NO ID");
        }
        if (!car.available()) {
            throw new IllegalArgumentException("Can't DELETE rented Car");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Car managedCar = session.get(Car.class, car.ID());
            if (managedCar == null) {
                throw new TransactionException("Given Car does not exist in DB" + car);
            }
            session.remove(managedCar);
            transaction.commit();
            logger.log(Level.INFO, ("Car ID " + car.ID() + " removed"));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error when DELETE Car from DB", ex);
            throw new TransactionException("Error when DELETE Car from DB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public Car findCarByID(String ID) throws TransactionException {
        //Check if the Argument is valid:
        if (null == ID) {
            throw new IllegalArgumentException("Can't locate Car with null ID");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Car car = session.get(Car.class, ID);
            return car;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error SELECT Car from DB with ID" + ID, ex);
            throw new TransactionException("Error SELECT Car from DB with ID" + ID, ex);
        } finally {
            session.close();
        }
    }

    @Override
    public void updateCarInfo(Car car) throws TransactionException {
        //Check if the Argument is valid:
        if (null == car) {
            throw new IllegalArgumentException("Can't INSERT NULL ENTRY to CarDB");
        }
        if (null == car.ID()) {
            throw new IllegalArgumentException("Can't UPDATE Car with NULL ID");
        }
        if ((null == car.color()) || (null == car.licensePlate()) || (null == car.model())
                || (null == car.rentalPayment()) || (car.rentalPayment() < 0) || (null == car.available())) {
            throw new IllegalArgumentException("Car with WRONG PARAMETRS");
        }

        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(car);
            transaction.commit();
            logger.log(Level.INFO, ("Car ID " + car.ID() + " updated"));
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.log(Level.SEVERE, "Error UPDATE Car from DB with ID " + car.ID(), ex);
            throw new TransactionException("Error UPDATE Car from DB with ID " + car.ID(), ex);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Car> getAvailableCars() throws TransactionException {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Car> query = session.createQuery(
                    "FROM Car WHERE available = true", Car.class);
            return query.list();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when getting available Cars from CarsDB", ex);
            throw new TransactionException("Error when getting available Cars from CarsDB", ex);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Car> getAllCars() throws TransactionException {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            Query<Car> query = session.createQuery("FROM Car", Car.class);
            return query.list();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error when SELECT all cars from CarsDB", ex);
            throw new TransactionException("Error when SELECT all cars from CarsDB", ex);
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

    public static final Logger logger = Logger.getLogger(CarManagerImplementation.class.getName());

    @Override
    public void setLogger(FileOutputStream fs) {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
}
