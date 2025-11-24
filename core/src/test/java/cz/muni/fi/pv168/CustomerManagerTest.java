package cz.muni.fi.pv168;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@DisplayName("Customer Manager Tests")
public class CustomerManagerTest {

    private CustomerManager manager;

    @BeforeEach
    public void setUp() {
        manager = new CustomerManagerImplementation();
        manager.tryCreateTables();
        // Clean up database
        try (var session = HibernateSessionFactory.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM Rent").executeUpdate();
            session.createMutationQuery("DELETE FROM Car").executeUpdate();
            session.createMutationQuery("DELETE FROM Customer").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Nested
    @DisplayName("Customer Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should add valid customer")
        public void testAddCustomer() {
            Customer customer = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
            manager.addCustomer(customer);
            
            String id = customer.uuid();
            assertNotNull(id);
            
            Customer result = manager.findCustomerByID(id);
            assertEquals(customer, result);
            assertNotSame(customer, result);
            assertCustomerDeepEquals(customer, result);
        }

        @Test
        @DisplayName("Should throw exception for duplicate driver license")
        public void testAddCustomerDuplicateLicense() {
            Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
            Customer customer2 = newCustomer("John", "Doe", "Praha", "1-23-45", "AK 373979");

            manager.addCustomer(customer1);
            assertThrows(IllegalArgumentException.class, () -> manager.addCustomer(customer2));
        }

        @Test
        @DisplayName("Should throw exception for invalid customer attributes")
        public void testAddInvalidCustomer() {
            assertThrows(IllegalArgumentException.class, () -> manager.addCustomer(null));
            
            Customer nullIdCustomer = newCustomer("Juraj", "Kolchak", "Brno", "5-34-13", "AK 474854").withUuid(null);
            assertThrows(IllegalArgumentException.class, () -> manager.addCustomer(nullIdCustomer));

            Customer nullDriversLicense = new Customer(UUID.randomUUID().toString(), "Juraj", "Kolchak", "Brno", "5-34-13", null, false);
            assertThrows(IllegalArgumentException.class, () -> manager.addCustomer(nullDriversLicense));
        }
    }

    @Nested
    @DisplayName("Customer Retrieval Tests")
    class RetrievalTests {

        @Test
        @DisplayName("Should find customer by ID")
        public void testFindCustomerByID() {
            Customer expResult = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
            manager.addCustomer(expResult);
            
            Customer result = manager.findCustomerByID(expResult.uuid());
            assertEquals(expResult, result);
        }

        @Test
        @DisplayName("Should return null for non-existent ID")
        public void testFindNonExistentCustomer() {
            assertNull(manager.findCustomerByID("non-existent-id"));
        }

        @Test
        @DisplayName("Should throw exception for null ID lookup")
        public void testFindCustomerNullID() {
            assertThrows(IllegalArgumentException.class, () -> manager.findCustomerByID(null));
        }

        @Test
        @DisplayName("Should get all customers")
        public void testGetAllCustomers() {
            assertTrue(manager.getAllCustomers().isEmpty());

            Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
            Customer customer2 = newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
            manager.addCustomer(customer1);
            manager.addCustomer(customer2);

            List<Customer> result = manager.getAllCustomers();
            assertEquals(2, result.size());
            assertCustomerDeepEquals(Arrays.asList(customer1, customer2), result);
        }

        @Test
        @DisplayName("Should get only active customers")
        public void testGetActiveCustomers() {
            Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979").withActive(true);
            Customer customer2 = newCustomer("Jutaj", "Kolchak", "Komarov", "5-34-86", "AK 372548").withActive(false);
            
            manager.addCustomer(customer1);
            manager.addCustomer(customer2);

            List<Customer> result = manager.getActiveCustomers();
            assertEquals(1, result.size());
            assertCustomerDeepEquals(customer1, result.get(0));
        }
    }

    @Nested
    @DisplayName("Customer Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update customer info")
        public void testUpdateCustomerInfo() {
            Customer customer = newCustomer("Petr", "Adamek", "Brno", "4-35-47", "AK 125798");
            manager.addCustomer(customer);
            String id = customer.uuid();
            
            customer = customer.withAddress("new address");
            manager.updateCustomerInfo(customer);
            
            Customer result = manager.findCustomerByID(id);
            assertCustomerDeepEquals(customer, result);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent customer")
        public void testUpdateNonExistentCustomer() {
            Customer customer = newCustomer("Hello", "World", "Google", "4-25-41", "AK 785428")
                    .withUuid("non-existent-id")
                    .withDriversLicense("UNIQUE-DL"); // Ensure unique DL to pass that check first
            
            assertThrows(TransactionException.class, () -> manager.updateCustomerInfo(customer));
        }
    }

    @Nested
    @DisplayName("Customer Deletion Tests")
    class DeletionTests {

        @Test
        @DisplayName("Should remove customer")
        public void testRemoveCustomer() {
            Customer customer = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
            manager.addCustomer(customer);
            
            assertNotNull(manager.findCustomerByID(customer.uuid()));
            manager.removeCustomer(customer);
            assertNull(manager.findCustomerByID(customer.uuid()));
        }
    }

    // Helpers
    public static Customer newCustomer(String firstName, String lastName, String address, String phoneNumber, String driversLicense) {
        return Customer.create(firstName, lastName, address, phoneNumber, driversLicense, false);
    }

    public static void assertCustomerDeepEquals(Customer expected, Customer actual) {
        assertEquals(expected.uuid(), actual.uuid());
        assertEquals(expected.firstName(), actual.firstName());
        assertEquals(expected.lastName(), actual.lastName());
        assertEquals(expected.address(), actual.address());
        assertEquals(expected.phoneNumber(), actual.phoneNumber());
        assertEquals(expected.active(), actual.active());
    }

    public static void assertCustomerDeepEquals(List<Customer> expected, List<Customer> actual) {
        assertEquals(expected.size(), actual.size());
        List<Customer> expectedSortedList = new java.util.ArrayList<>(expected);
        List<Customer> actualSortedList = new java.util.ArrayList<>(actual);
        expectedSortedList.sort(Comparator.comparing(Customer::uuid));
        actualSortedList.sort(Comparator.comparing(Customer::uuid));

        for (int i = 0; i < expectedSortedList.size(); i++) {
            assertCustomerDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
}