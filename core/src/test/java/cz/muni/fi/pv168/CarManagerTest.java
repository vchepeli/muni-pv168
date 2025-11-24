package cz.muni.fi.pv168;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@DisplayName("Car Manager Tests")
public class CarManagerTest {

    private CarManager manager;

    @BeforeEach
    public void setUp() {
        manager = new CarManagerImplementation();
        manager.tryCreateTables();
        // Clean up any existing data
        try (var session = HibernateSessionFactory.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM Rent").executeUpdate();
            session.createMutationQuery("DELETE FROM Car").executeUpdate();
            session.createMutationQuery("DELETE FROM Customer").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Nested
    @DisplayName("Car Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should add a valid car")
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
        @DisplayName("Should throw exception when adding duplicate license plate")
        public void addCarDuplicatePlate() {
            Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
            Car car2 = newCar("Red", "0B6 6835", "BMW", 500.0);

            manager.addCar(car1);
            assertThrows(IllegalArgumentException.class, () -> manager.addCar(car2));
        }

        @Test
        @DisplayName("Should throw exception for invalid car attributes")
        public void addCarWithWrongAttributes() {
            assertThrows(IllegalArgumentException.class, () -> manager.addCar(null));
            assertThrows(IllegalArgumentException.class, () -> manager.addCar(newCar("Black", "0B6 6835", "Škoda", 200.0).withUuid(null)));
            
            // Null color
            assertThrows(IllegalArgumentException.class, () -> manager.addCar(new Car(UUID.randomUUID().toString(), "Škoda", null, true, 200.0, "0B6 6835")));
            // Null plate
            assertThrows(IllegalArgumentException.class, () -> manager.addCar(new Car(UUID.randomUUID().toString(), "Škoda", "Black", true, 200.0, null)));
            // Null model
            assertThrows(IllegalArgumentException.class, () -> manager.addCar(new Car(UUID.randomUUID().toString(), null, "Black", true, 200.0, "0B6 6835")));
            // Negative price
            assertThrows(IllegalArgumentException.class, () -> manager.addCar(newCar("Black", "0B6 6835", "Škoda", -1.0)));
        }
    }

    @Nested
    @DisplayName("Car Retrieval Tests")
    class RetrievalTests {

        @Test
        @DisplayName("Should find existing car by ID")
        public void findCarByID() {
            Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);
            manager.addCar(car);
            
            Car result = manager.findCarByID(car.uuid());
            assertEquals(car, result);
            assertCarDeepEquals(car, result);
        }

        @Test
        @DisplayName("Should return null for non-existent ID")
        public void findCarByNonExistentID() {
            assertNull(manager.findCarByID("non-existent-id"));
        }

        @Test
        @DisplayName("Should retrieve all cars")
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
        @DisplayName("Should retrieve only available cars")
        public void findAllAvailableCars() {
            assertTrue(manager.getAvailableCars().isEmpty());

            Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
            Car car2 = newCar("Red", "7B4 0044", "BMW", 500.0);

            manager.addCar(car1);
            manager.addCar(car2);

            List<Car> expected = Arrays.asList(car1, car2);
            List<Car> actual = manager.getAvailableCars();
            assertCarDeepEquals(expected, actual);

            // Mark car1 unavailable
            car1 = car1.withStatus(false);
            manager.updateCarInfo(car1);

            expected = Arrays.asList(car2);
            actual = manager.getAvailableCars();
            assertCarDeepEquals(expected, actual);
        }
    }

    @Nested
    @DisplayName("Car Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should successfully update car attributes")
        public void updateCarInfo() {
            Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
            manager.addCar(car1);
            String id = car1.uuid();

            // Update color
            car1 = manager.findCarByID(id).withColor("White");
            manager.updateCarInfo(car1);
            assertEquals("White", car1.color());

            // Update license plate
            car1 = manager.findCarByID(id).withLicensePlate("8B5 0983");
            manager.updateCarInfo(car1);
            assertEquals("8B5 0983", car1.licensePlate());

            // Update model
            car1 = manager.findCarByID(id).withModel("Volkswagen");
            manager.updateCarInfo(car1);
            assertEquals("Volkswagen", car1.model());

            // Update price
            car1 = manager.findCarByID(id).withRentalPayment(300.0);
            manager.updateCarInfo(car1);
            assertEquals(300.0, car1.rentalPayment());

            // Update status
            car1 = manager.findCarByID(id).withStatus(false);
            manager.updateCarInfo(car1);
            assertFalse(car1.available());
        }

        @Test
        @DisplayName("Should throw exception for invalid updates")
        public void updateCarInfoWithWrongAttributes() {
            Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);
            manager.addCar(car);
            String id = car.uuid();

            assertThrows(IllegalArgumentException.class, () -> manager.updateCarInfo(null));
            assertThrows(IllegalArgumentException.class, () -> manager.updateCarInfo(manager.findCarByID(id).withUuid(null)));
            assertThrows(IllegalArgumentException.class, () -> manager.updateCarInfo(manager.findCarByID(id).withColor(null)));
            assertThrows(IllegalArgumentException.class, () -> manager.updateCarInfo(manager.findCarByID(id).withLicensePlate(null)));
            assertThrows(IllegalArgumentException.class, () -> manager.updateCarInfo(manager.findCarByID(id).withModel(null)));
            assertThrows(IllegalArgumentException.class, () -> manager.updateCarInfo(manager.findCarByID(id).withRentalPayment(-1.0)));
            
            // Non-existent ID check
            Car nonExistentCar = manager.findCarByID(id).withUuid("different-id").withLicensePlate("unique-plate-123");
            assertThrows(TransactionException.class, () -> manager.updateCarInfo(nonExistentCar));
            
            // Duplicate plate check
            Car car2 = newCar("Red", "DUP-123", "BMW", 500.0);
            manager.addCar(car2);
            Car duplicateCar = manager.findCarByID(id).withLicensePlate("DUP-123");
            assertThrows(IllegalArgumentException.class, () -> manager.updateCarInfo(duplicateCar));
        }
    }

    @Nested
    @DisplayName("Car Deletion Tests")
    class DeletionTests {

        @Test
        @DisplayName("Should remove existing car")
        public void removeCar() {
            Car car1 = newCar("Black", "0B6 6835", "Škoda", 200.0);
            manager.addCar(car1);

            assertNotNull(manager.findCarByID(car1.uuid()));
            manager.removeCar(car1);
            assertNull(manager.findCarByID(car1.uuid()));
        }

        @Test
        @DisplayName("Should throw exception for invalid deletion")
        public void removeCarWithWrongAttributes() {
            Car car = newCar("Black", "0B6 6835", "Škoda", 200.0);

            assertThrows(IllegalArgumentException.class, () -> manager.removeCar(null));
            assertThrows(IllegalArgumentException.class, () -> manager.removeCar(car.withUuid(null)));
            assertThrows(TransactionException.class, () -> manager.removeCar(car.withUuid("non-existent-id")));
        }
    }

    // Helpers
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
        List<Car> expectedSortedList = new java.util.ArrayList<>(expected);
        List<Car> actualSortedList = new java.util.ArrayList<>(actual);
        expectedSortedList.sort(Comparator.comparing(Car::uuid));
        actualSortedList.sort(Comparator.comparing(Car::uuid));

        for (int i = 0; i < actualSortedList.size(); i++) {
            assertCarDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
}