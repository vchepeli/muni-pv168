package cz.muni.fi.pv168;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@DisplayName("Rental Manager Tests")
public class RentalManagerTest {

    private CarManagerImplementation carManager;
    private CustomerManagerImplementation customerManager;
    private RentManagerImplementation manager;
    
    // Test Data
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

    @BeforeEach
    public void setUp() {
        manager = new RentManagerImplementation();
        carManager = new CarManagerImplementation();
        customerManager = new CustomerManagerImplementation();
        
        // Initialize tables and clean DB
        carManager.tryCreateTables();
        customerManager.tryCreateTables();
        manager.tryCreateTables();
        cleanupDatabase();
        
        prepareTestData();
    }

    private void cleanupDatabase() {
        try (var session = HibernateSessionFactory.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM Rent").executeUpdate();
            session.createMutationQuery("DELETE FROM Car").executeUpdate();
            session.createMutationQuery("DELETE FROM Customer").executeUpdate();
            session.getTransaction().commit();
        }
    }

    private void prepareTestData() {
        car1 = CarManagerTest.newCar("Black", "0B6 6835", "Škoda", 200.0);
        car2 = CarManagerTest.newCar("Red", "7B4 0044", "BMW", 500.0);
        car3 = CarManagerTest.newCar("White", "8B5 0983", "Volkwagen", 300.0);

        customer1 = CustomerManagerTest.newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        customer2 = CustomerManagerTest.newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        customer3 = CustomerManagerTest.newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");

        carManager.addCar(car1);
        carManager.addCar(car2);
        carManager.addCar(car3);

        customerManager.addCustomer(customer1);
        customerManager.addCustomer(customer2);
        customerManager.addCustomer(customer3);

        carWithoutID = CarManagerTest.newCar("Green", "8B3 9763", "Audi", 400.0).withUuid(null);
        carNotInDB = CarManagerTest.newCar("Blue", "3B6 8463", "Peugeot", 0.0).withUuid("non-existent-id");

        customerWithoutID = CustomerManagerTest.newCustomer("Martin", "Pulec", "Brno", "5-11-24", "AK 897589").withUuid(null);
        customerNotInDB = CustomerManagerTest.newCustomer("Lukas", "Rucka", "Brno", "5-21-06", "AK 256354").withUuid("non-existent-id").withActive(true);
    }

    @Nested
    @DisplayName("Rent Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should successfully rent car to customer")
        public void rentCarToCustomer() {
            // Activate customers
            customer1 = customer1.withActive(true);
            customer2 = customer2.withActive(true);
            customerManager.updateCustomerInfo(customer1);
            customerManager.updateCustomerInfo(customer2);

            manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
            manager.rentCarToCustomer(car3, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

            // Verify
            List<Car> carsRentedToCustomer1 = Arrays.asList(carManager.findCarByID(car1.uuid()));
            List<Car> carsRentedToCustomer2 = Arrays.asList(carManager.findCarByID(car3.uuid()));

            CarManagerTest.assertCarDeepEquals(carsRentedToCustomer1, manager.getAllCustomerCars(customer1));
            CarManagerTest.assertCarDeepEquals(carsRentedToCustomer2, manager.getAllCustomerCars(customer2));
            
            // Check statuses - Refresh objects from DB first
            car1 = carManager.findCarByID(car1.uuid());
            car3 = carManager.findCarByID(car3.uuid());
            
            assertFalse(customer3.active());
            assertEquals(customer1, manager.findCustomerWithCar(car1));
            assertTrue(car2.available());
        }

        @Test
        @DisplayName("Should throw exception for invalid rent parameters")
        public void testInvalidRentParameters() {
            // Already rented (by setup above? No, setup is fresh per test)
            // But here we verify basic constraints
            
            // Null arguments
            assertThrows(IllegalArgumentException.class, () -> manager.rentCarToCustomer(null, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27")));
            assertThrows(IllegalArgumentException.class, () -> manager.rentCarToCustomer(car2, null, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27")));
            
            // Invalid IDs
            assertThrows(IllegalArgumentException.class, () -> manager.rentCarToCustomer(carWithoutID, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27")));
            assertThrows(IllegalArgumentException.class, () -> manager.rentCarToCustomer(car2, customerWithoutID, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27")));
            
            // Not in DB
            assertThrows(IllegalArgumentException.class, () -> manager.rentCarToCustomer(carNotInDB, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27")));
            assertThrows(IllegalArgumentException.class, () -> manager.rentCarToCustomer(car2, customerNotInDB, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27")));
        }

        @Test
        @DisplayName("Should throw exception for overlapping rental dates")
        public void rentOverlappingDates() {
            customer1 = customer1.withActive(true);
            customerManager.updateCustomerInfo(customer1);
            customer2 = customer2.withActive(true);
            customerManager.updateCustomerInfo(customer2);

            // Base rent
            manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
            
            // Inside
            assertThrows(IllegalArgumentException.class, () -> 
                manager.addRent(Rent.create(Date.valueOf("2012-03-22"), Date.valueOf("2012-03-30"), car1.uuid(), customer2.uuid())));
            
            // Enveloping
            assertThrows(IllegalArgumentException.class, () -> 
                manager.addRent(Rent.create(Date.valueOf("2012-03-20"), Date.valueOf("2012-04-01"), car1.uuid(), customer2.uuid())));
            
            // Start overlap
            assertThrows(IllegalArgumentException.class, () -> 
                manager.addRent(Rent.create(Date.valueOf("2012-03-20"), Date.valueOf("2012-03-22"), car1.uuid(), customer2.uuid())));
            
            // End overlap
            assertThrows(IllegalArgumentException.class, () -> 
                manager.addRent(Rent.create(Date.valueOf("2012-03-30"), Date.valueOf("2012-04-01"), car1.uuid(), customer2.uuid())));
            
            // Valid non-overlapping
            assertDoesNotThrow(() -> 
                manager.addRent(Rent.create(Date.valueOf("2012-04-01"), Date.valueOf("2012-04-05"), car1.uuid(), customer2.uuid())));
        }

        @Test
        @DisplayName("Should throw exception if start date is after end date")
        public void rentInvalidDates() {
            assertThrows(IllegalArgumentException.class, () -> 
                manager.addRent(Rent.create(Date.valueOf("2012-04-01"), Date.valueOf("2012-03-31"), car1.uuid(), customer1.uuid())));
        }
    }

    @Nested
    @DisplayName("Rent Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update rent dates")
        public void updateRent() {
            customer1 = customer1.withActive(true);
            customerManager.updateCustomerInfo(customer1);
            manager.rentCarToCustomer(car1, customer1, Date.valueOf("2012-03-21"), Date.valueOf("2012-03-31"));
            
            // Refresh from DB to get updated status
            car1 = carManager.findCarByID(car1.uuid());
            Rent rent = manager.findRentWithCar(car1);
            
            Rent extendedRent = rent.withDueDate(Date.valueOf("2012-04-05"));
            manager.updateRent(extendedRent);
            
            Rent updated = manager.findRentWithCar(car1);
            assertEquals(Date.valueOf("2012-04-05"), updated.dueDate());
        }
    }

    @Nested
    @DisplayName("Rent Deletion Tests")
    class DeletionTests {

        @Test
        @DisplayName("Should return car from customer")
        public void getCarFromCustomer() {
            customer1 = customer1.withActive(true);
            customer2 = customer2.withActive(true);
            customerManager.updateCustomerInfo(customer1);
            customerManager.updateCustomerInfo(customer2);

            manager.rentCarToCustomer(car3, customer2, Date.valueOf("2012-03-15"), Date.valueOf("2012-03-27"));

            // Return car
            manager.getCarFromCustomer(car3, customer2);

            // Verify car is available
            car3 = carManager.findCarByID(car3.uuid());
            assertTrue(car3.available());
            
            // Verify rent is gone
            assertThrows(IllegalArgumentException.class, () -> manager.findRentWithCar(car3));
        }
    }
}
