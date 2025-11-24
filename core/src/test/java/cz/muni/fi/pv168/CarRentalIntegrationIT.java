package cz.muni.fi.pv168;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Car Rental E2E Integration Tests")
public class CarRentalIntegrationIT {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    private CarManager carManager;
    private CustomerManager customerManager;
    private RentManager rentManager;

    @BeforeAll
    public static void setUpClass() {
        // Override Hibernate properties to use the container
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());
        System.setProperty("hibernate.connection.driver_class", postgres.getDriverClassName());
    }

    @BeforeEach
    public void setUp() {
        // Force re-initialization of SessionFactory to pick up new system properties
        HibernateSessionFactory.reinitialize();
        
        carManager = new CarManagerImplementation();
        customerManager = new CustomerManagerImplementation();
        rentManager = new RentManagerImplementation();

        carManager.tryCreateTables();
        customerManager.tryCreateTables();
        rentManager.tryCreateTables();
        
        cleanupDatabase();
    }
    
    private void cleanupDatabase() {
        try (var session = HibernateSessionFactory.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM Rent").executeUpdate();
            session.createMutationQuery("DELETE FROM Car").executeUpdate();
            session.createMutationQuery("DELETE FROM Customer").executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            // Ignore if tables don't exist yet
        }
    }

    @Test
    @DisplayName("Complete Rent Lifecycle: Add Car/Customer -> Rent -> Verify -> Return")
    public void testCompleteRentLifecycle() {
        // 1. Create and Add Car
        Car car = Car.create("Test Model", "Blue", true, 100.0, "TEST-123");
        carManager.addCar(car);
        assertNotNull(car.uuid());
        
        // 2. Create and Add Customer
        Customer customer = Customer.create("John", "Doe", "Test Address", "123456789", "DL-999", true);
        customerManager.addCustomer(customer);
        assertNotNull(customer.uuid());
        
        // 3. Rent Car to Customer
        Date rentDate = Date.valueOf(LocalDate.now());
        Date dueDate = Date.valueOf(LocalDate.now().plusDays(5));
        
        rentManager.rentCarToCustomer(car, customer, rentDate, dueDate);
        
        // 4. Verify Rent Exists
        Rent rent = rentManager.findRentWithCar(car);
        assertNotNull(rent);
        assertEquals(car.uuid(), rent.carID());
        assertEquals(customer.uuid(), rent.customerID());
        
        // 5. Verify Car is Unavailable
        Car rentedCar = carManager.findCarByID(car.uuid());
        assertFalse(rentedCar.available());
        
        // 6. Verify Customer has the car
        List<Car> customerCars = rentManager.getAllCustomerCars(customer);
        assertEquals(1, customerCars.size());
        assertEquals(car.uuid(), customerCars.get(0).uuid());
        
        // 7. Return Car
        rentManager.getCarFromCustomer(car, customer);
        
        // 8. Verify Car is Available again
        Car returnedCar = carManager.findCarByID(car.uuid());
        assertTrue(returnedCar.available());
        
        // 9. Verify Rent is Gone
        assertThrows(IllegalArgumentException.class, () -> rentManager.findRentWithCar(car));
    }
}
