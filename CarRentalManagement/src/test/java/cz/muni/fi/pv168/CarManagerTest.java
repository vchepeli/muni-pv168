package cz.muni.fi.pv168;

import java.util.*;
import java.util.UUID;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.hibernate.Session;

public class CarManagerTest {

    private CarManager manager;

    @Before
    public void setUp() {
        manager = new CarManagerImplementation();
        // Create tables via Hibernate auto-creation
        manager.tryCreateTables();
        // Clean up any existing data
        cleanupDatabase();
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
    public void addCar() {
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);

        manager.addCar(car);

        String id = car.uuid();
        assertNotNull(id);
        assertTrue(car.available());

        Car result = manager.findCarByID(id);
        assertEquals(car, result);
        assertNotSame(car, result);
        assertCarDeepEquals(car, result);
    }

    @Test
    public void findCarByID() {
        assertNull(manager.findCarByID("non-existent-id"));
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);
        manager.addCar(car);
        String id = car.uuid();

        Car result = manager.findCarByID(id);
        assertEquals(car, result);
        assertCarDeepEquals(car, result);
    }

    @Test
    public void addCarWithWrongAttributes() {
        try {
            manager.addCar(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0).withUuid(null);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // Test with null color
        try {
            Car nullColorCar = new Car(UUID.randomUUID().toString(), "Škoda", null, true, 200.0, "0B6 6835");
            manager.addCar(nullColorCar);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // Test with null license plate
        try {
            Car nullPlateCar = new Car(UUID.randomUUID().toString(), "Škoda", "Black", true, 200.0, null);
            manager.addCar(nullPlateCar);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // Test with null model
        try {
            Car nullModelCar = new Car(UUID.randomUUID().toString(), null, "Black", true, 200.0, "0B6 6835");
            manager.addCar(nullModelCar);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // Test with negative payment
        car = newCar("Black", "0B6 6835", "Škoda", -1.0);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException e) {
        }

        car = newCar("Black", "0B6 6835", "Škoda", 0.0);
        try {
            manager.addCar(car);

            Car result = manager.findCarByID(car.uuid());
            assertNotNull(result);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void removeCar() {
        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);

        assertNotNull(manager.findCarByID(car1.uuid()));
        assertNotNull(manager.findCarByID(car2.uuid()));

        manager.removeCar(car1);

        assertNull(manager.findCarByID(car1.uuid()));
        assertNotNull(manager.findCarByID(car2.uuid()));
    }

    @Test
    public void removeCarWithWrongAttributes() {
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);

        try {
            manager.removeCar(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            Car carWithNullID = car.withUuid(null);
            manager.removeCar(carWithNullID);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            Car carWithNonExistentID = car.withUuid("non-existent-id");
            manager.removeCar(carWithNonExistentID);
            fail();
        } catch (TransactionException ex) {
        }
    }

    @Test
    public void updateCarInfo() {
        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);
        String id = car1.uuid();

        car1 = manager.findCarByID(id);
        car1 = car1.withColor("White");
        manager.updateCarInfo(car1);
        assertEquals("White", car1.color());
        assertEquals("0B6 6835", car1.licensePlate());
        assertEquals("Škoda", car1.model());
        assertEquals(Double.valueOf(200.0), Double.valueOf(car1.rentalPayment()));
        assertTrue(car1.available());

        car1 = manager.findCarByID(id);
        car1 = car1.withLicensePlate("8B5 0983");
        manager.updateCarInfo(car1);
        assertEquals("White", car1.color());
        assertEquals("8B5 0983", car1.licensePlate());
        assertEquals("Škoda", car1.model());
        assertEquals(Double.valueOf(200.0), Double.valueOf(car1.rentalPayment()));
        assertTrue(car1.available());

        car1 = manager.findCarByID(id);
        car1 = car1.withModel("Volkswagen");
        manager.updateCarInfo(car1);
        assertEquals("White", car1.color());
        assertEquals("8B5 0983", car1.licensePlate());
        assertEquals("Volkswagen", car1.model());
        assertEquals(Double.valueOf(200.0), Double.valueOf(car1.rentalPayment()));
        assertTrue(car1.available());

        car1 = manager.findCarByID(id);
        car1 = car1.withRentalPayment(300.0);
        manager.updateCarInfo(car1);
        assertEquals("White", car1.color());
        assertEquals("8B5 0983", car1.licensePlate());
        assertEquals("Volkswagen", car1.model());
        assertEquals(Double.valueOf(300.0), Double.valueOf(car1.rentalPayment()));
        assertTrue(car1.available());

        car1 = manager.findCarByID(id);
        car1 = car1.withRentalPayment(0.0);
        manager.updateCarInfo(car1);
        assertEquals("White", car1.color());
        assertEquals("8B5 0983", car1.licensePlate());
        assertEquals("Volkswagen", car1.model());
        assertEquals(Double.valueOf(0.0), Double.valueOf(car1.rentalPayment()));
        assertTrue(car1.available());

        car1 = manager.findCarByID(id);
        car1 = car1.withStatus(false);
        manager.updateCarInfo(car1);
        assertEquals("White", car1.color());
        assertEquals("8B5 0983", car1.licensePlate());
        assertEquals("Volkswagen", car1.model());
        assertEquals(Double.valueOf(0.0), Double.valueOf(car1.rentalPayment()));
        assertFalse(car1.available());

        assertCarDeepEquals(car2, manager.findCarByID(car2.uuid()));
    }

    @Test
    public void updateCarInfoWithWrongAttributes() {
        Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);

        manager.addCar(car);
        String id = car.uuid();

        try {
            manager.updateCarInfo(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car = car.withUuid(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car = car.withUuid("different-id");
            manager.updateCarInfo(car);
            fail();
        } catch (TransactionException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car = car.withColor(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car = car.withLicensePlate(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car = car.withModel(null);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            car = manager.findCarByID(id);
            car = car.withRentalPayment(-1.0);
            manager.updateCarInfo(car);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void getAllCars() {
        assertTrue(manager.getAllCars().isEmpty());

        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);

        List<Car> expected = Arrays.asList(car1, car2);
        List<Car> actual = manager.getAllCars();

        assertCarDeepEquals(expected, actual);
    }

    @Test
    public void findAllAvailableCars() {
        assertTrue(manager.getAvailableCars().isEmpty());

        Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
        Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

        manager.addCar(car1);
        manager.addCar(car2);

        List<Car> expected = Arrays.asList(car1, car2);
        List<Car> actual = manager.getAvailableCars();

        assertCarDeepEquals(expected, actual);

        car1 = car1.withStatus(false);
        manager.updateCarInfo(car1);

        expected = Arrays.asList(car2);
        actual = manager.getAvailableCars();

        assertCarDeepEquals(expected, actual);

        car2 = car2.withStatus(false);
        manager.updateCarInfo(car2);

        assertTrue(manager.getAvailableCars().isEmpty());
    }

    public static Car newCar(String colour, String licensePlate, String model, double payment) {
        return Car.create(model, colour, true, payment, licensePlate);
    }

    public static void assertCarDeepEquals(Car expected, Car actual) {
        assertEquals(expected.uuid(), actual.uuid());
        assertEquals(expected.color(), actual.color());
        assertEquals(expected.licensePlate(), actual.licensePlate());
        assertEquals(expected.model(), actual.model());
        assertEquals(expected.rentalPayment(), actual.rentalPayment());
        assertEquals(expected.available(), actual.available());
    }

    public static void assertCarDeepEquals(List<Car> expected, List<Car> actual) {
        assertEquals(expected.size(), actual.size());
        List<Car> expectedSortedList = new ArrayList<>(expected);
        List<Car> actualSortedList = new ArrayList<>(actual);
        Collections.sort(expectedSortedList, carByIDComparator);
        Collections.sort(actualSortedList, carByIDComparator);

        for (int i = 0; i < actualSortedList.size(); i++) {
            assertCarDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }

    private static Comparator<Car> carByIDComparator = new Comparator<Car>() {
        @Override
        public int compare(Car car1, Car car2) {
            return car1.uuid().compareTo(car2.uuid());
        }
    };
}
