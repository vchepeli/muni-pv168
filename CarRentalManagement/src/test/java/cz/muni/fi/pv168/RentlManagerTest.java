package cz.muni.fi.pv168;

import static cz.muni.fi.pv168.CarManagerTest.assertCarDeepEquals;
import static cz.muni.fi.pv168.CarManagerTest.newCar;
import static cz.muni.fi.pv168.CustomerManagerTest.assertCustomerDeepEquals;
import static cz.muni.fi.pv168.CustomerManagerTest.newCustomer;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.hibernate.Session;

/**
 *
 * @author Jooji
 */
public class RentlManagerTest {

    private CarManagerImplementation carManager;
    private CustomerManagerImplementation customerManager;
    private RentManagerImplementation manager;
    private Car car1;
    private Car car2;
    private Car car3;
    private Car carWithoutID;
    private Car carNotInDB;
    private Customer customer1;
    private Customer customer2;
    private Customer customer3;
    private Customer customerWithoutID;
    private Customer customerNotInDB;

    private void prepareTestData() {
        car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        car2 = newCar("Red", "7B4 0044", "BMW", 500.0);
        car3 = newCar("White", "8B5 0983", "Volkwagen", 300.0);

        customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        customer2 = newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");

        carManager.addCar(car1);
        carManager.addCar(car2);
        carManager.addCar(car3);

        customerManager.addCustomer(customer1);
        customerManager.addCustomer(customer2);
        customerManager.addCustomer(customer3);

        carWithoutID = newCar("Green", "8B3 9763", "Audi", 400.0).withID(null);
        carNotInDB = newCar("Blue", "3B6 8463", "Peugeot", 0.0).withID("non-existent-id");

        customerWithoutID = newCustomer("Martin", "Pulec", "Brno", "5-11-24", "AK 897589").withID(null);
        customerNotInDB = newCustomer("Lukas", "Rucka", "Brno", "5-21-06", "AK 256354").withID("non-existent-id").withActive(true);
    }

    @Before
    public void setUp() {
        manager = new RentManagerImplementation();
        carManager = new CarManagerImplementation();
        customerManager = new CustomerManagerImplementation();
        carManager.tryCreateTables();
        customerManager.tryCreateTables();
        manager.tryCreateTables();
        cleanupDatabase();
        prepareTestData();
    }

    @After
    public void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM Rent").executeUpdate();
            session.createMutationQuery("DELETE FROM Car").executeUpdate();
            session.createMutationQuery("DELETE FROM Customer").executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }

    @Test
    public void findCustomerWithCar() {
        assertTrue(car1.available());
        assertTrue(car2.available());
        assertTrue(car3.available());

        manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertTrue(car2.available());
        assertTrue(car3.available());

        try {
            manager.findCustomerWithCar(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            manager.findCustomerWithCar(carWithoutID);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void getAllCustomerCars() {
        assertFalse(customer1.active());
        assertFalse(customer2.active());
        assertFalse(customer3.active());

        manager.rentCarToCustomer(car2, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
        manager.rentCarToCustomer(car3, customer1, Date.valueOf("2012-03-25"), Date.valueOf("2012-04-02"));
        manager.rentCarToCustomer(car1, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

        List<Car> carsRetnedtoCustomer1 = Arrays.asList(car2, car3);
        List<Car> carsRetnedtoCustomer2 = Arrays.asList(car1);

        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertCarDeepEquals(carsRetnedtoCustomer2, manager.getAllCustomerCars(customer2));
        assertFalse(customer3.active());

        try {
            manager.getAllCustomerCars(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getAllCustomerCars(customerWithoutID);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void rentCarToCustomer() {
        assertTrue(car1.available());
        assertTrue(car2.available());
        assertTrue(car3.available());

        manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
        manager.rentCarToCustomer(car3, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

        List<Car> carsRetnedtoCustomer1 = Arrays.asList(car1);
        List<Car> carsRetnedtoCustomer2 = Arrays.asList(car3);

        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertCarDeepEquals(carsRetnedtoCustomer2, manager.getAllCustomerCars(customer2));
        assertFalse(customer3.active());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertTrue(car2.available());
        assertEquals(customer2, manager.findCustomerWithCar(car3));
        assertCustomerDeepEquals(customer2, manager.findCustomerWithCar(car3));

        try {
            manager.rentCarToCustomer(car1, customer3, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(null, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(carWithoutID, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(carNotInDB, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (TransactionException e) {
        }

        try {
            manager.rentCarToCustomer(car2, null, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(car2, customerWithoutID, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.rentCarToCustomer(car2, customerNotInDB, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));
            fail();
        } catch (TransactionException e) {
        }

        // Check that previous tests didn't affect data in database
        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertCarDeepEquals(carsRetnedtoCustomer2, manager.getAllCustomerCars(customer2));
        assertFalse(customer3.active());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertTrue(car2.available());
        assertEquals(customer2, manager.findCustomerWithCar(car3));
        assertCustomerDeepEquals(customer2, manager.findCustomerWithCar(car3));
    }

    @Test
    public void getCarFromCustomer() {
        assertTrue(car1.available());
        assertTrue(car2.available());
        assertTrue(car3.available());

        manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
        manager.rentCarToCustomer(car2, customer1, Date.valueOf("2012-03-25"), Date.valueOf("2012-04-02"));
        manager.rentCarToCustomer(car3, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertEquals(customer1, manager.findCustomerWithCar(car2));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car2));
        assertEquals(customer2, manager.findCustomerWithCar(car3));
        assertCustomerDeepEquals(customer2, manager.findCustomerWithCar(car3));

        manager.getCarFromCustomer(car3, customer2);

        List<Car> carsRetnedtoCustomer1 = Arrays.asList(car1, car2);

        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertFalse(customer2.active());
        assertFalse(customer3.active());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertEquals(customer1, manager.findCustomerWithCar(car2));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car2));
        assertTrue(car3.available());

        try {
            manager.getCarFromCustomer(car3, customer1);
            fail();
        } catch (TransactionException e) {
        }

        try {
            manager.getCarFromCustomer(car3, customer2);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(null, customer1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(carWithoutID, customer1);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(carNotInDB, customer1);
            fail();
        } catch (TransactionException e) {
        }

        try {
            manager.getCarFromCustomer(car1, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(car1, customerWithoutID);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            manager.getCarFromCustomer(car1, customerNotInDB);
            fail();
        } catch (TransactionException e) {
        }

        // Check that previous tests didn't affect data in database
        assertCarDeepEquals(carsRetnedtoCustomer1, manager.getAllCustomerCars(customer1));
        assertFalse(customer2.active());
        assertFalse(customer3.active());

        assertEquals(customer1, manager.findCustomerWithCar(car1));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car1));
        assertEquals(customer1, manager.findCustomerWithCar(car2));
        assertCustomerDeepEquals(customer1, manager.findCustomerWithCar(car2));
        assertTrue(car3.available());
    }
}
