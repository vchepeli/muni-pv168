package cz.muni.fi.pv168;

import java.util.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.hibernate.Session;

public class CustomerManagerTest {

    private CustomerManager manager;

    @Before
    public void setUp() {
        manager = new CustomerManagerImplementation();
        manager.tryCreateTables();
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

    public static Customer newCustomer(String firstName, String lastName, String address, String phoneNumber, String driversLicense) {
        return Customer.create(firstName, lastName, address, phoneNumber, driversLicense, false);
    }

    /**
     * Test of addCustomer method, of class CustomerManagerImplementation.
     */
    @Test
    public void testAddCustomer() {
        Customer customer = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        manager.addCustomer(customer);
        String ID = customer.ID();
        assertNotNull(ID);
        Customer result = manager.findCustomerByID(ID);
        assertEquals(customer, result);
        assertNotSame(customer, result);
        assertCustomerDeepEquals(customer, result);

        try {
            manager.addCustomer(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        customer = newCustomer("Juraj", "Kolchak", "Brno", "5-34-13", "AK 474854");
        customer = customer.withID(null);
        try {
            manager.addCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            Customer nullDriversLicense = new Customer(UUID.randomUUID().toString(), "Juraj", "Kolchak", "Brno", "5-34-13", null, false);
            manager.addCustomer(nullDriversLicense);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    /**
     * Test of removeCustomer method, of class CustomerManagerImplementation.
     */
    @Test
    public void testRemoveCustomer() {
        assertTrue(manager.getAllCustomers().isEmpty());

        Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        Customer customer2 = newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        Customer customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");
        manager.addCustomer(customer1);
        manager.addCustomer(customer2);
        manager.addCustomer(customer3);
        String ID1 = customer1.ID();
        String ID2 = customer2.ID();
        String ID3 = customer3.ID();
        customer1 = manager.findCustomerByID(ID1);
        customer2 = manager.findCustomerByID(ID2);
        customer3 = manager.findCustomerByID(ID3);
        List<Customer> expResult = Arrays.asList(customer1, customer2);
        manager.removeCustomer(customer3);
        List<Customer> result = manager.getAllCustomers();
        assertCustomerDeepEquals(expResult, result);

        //update Customer
        manager.removeCustomer(customer2);
        customer2 = newCustomer("Vasylyna", "Chepelyuk", "Louny", "7-89-53", "AK 235689");
        manager.addCustomer(customer2);
        ID2 = customer2.ID();
        customer2 = manager.findCustomerByID(ID2);

        expResult = Arrays.asList(customer2);

        customer1 = newCustomer("Andrii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        customer1 = customer1.withID(ID1);
        manager.updateCustomerInfo(customer1);
        manager.removeCustomer(customer1);
        result = manager.getAllCustomers();
        assertCustomerDeepEquals(expResult, result);
    }

    /**
     * Test of findCustomerByID method, of class CustomerManagerImplementation.
     */
    @Test
    public void testFindCustomerByID() {
        Customer expResult = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        manager.addCustomer(expResult);
        String ID = expResult.ID();
        Customer result = manager.findCustomerByID(ID);
        assertEquals(expResult, result);
        try {
            manager.findCustomerByID(null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        result = manager.findCustomerByID("non-existent-id");
        assertNull(result);
    }

    /**
     * Test of getAllCustomers method, of class CustomerManagerImplementation.
     */
    @Test
    public void testGetAllCustomers() {
        assertTrue(manager.getAllCustomers().isEmpty());

        Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        Customer customer2 = newCustomer("Juraj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        Customer customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");
        Customer customer4 = newCustomer("Martin", "Pulec", "Brno", "5-11-24", "AK 897589");
        Customer customer5 = newCustomer("Lukas", "Rucka", "Brno", "5-21-06", "AK 256354");
        manager.addCustomer(customer1);
        manager.addCustomer(customer2);
        manager.addCustomer(customer3);
        manager.addCustomer(customer4);
        manager.addCustomer(customer5);
        List<Customer> expResult = Arrays.asList(customer1, customer2, customer3, customer4, customer5);
        List<Customer> result = manager.getAllCustomers();
        assertEquals(5, expResult.size());
        assertCustomerDeepEquals(expResult, result);

        // add new Customer
        Customer customer = newCustomer("Petr", "Adamek", "Brno", "4-35-47", "AK 125798");
        manager.addCustomer(customer);
        result = manager.getAllCustomers();
        expResult = Arrays.asList(customer, customer1, customer2, customer3, customer4, customer5);
        assertCustomerDeepEquals(expResult, result);

        //remove Customer
        String ID = customer.ID();
        customer = customer.withAddress("new address");
        expResult = Arrays.asList(customer, customer1, customer2, customer3, customer4, customer5);
        // update Customer
        manager.updateCustomerInfo(customer);
        result = manager.getAllCustomers();
        assertCustomerDeepEquals(expResult, result);
    }

    /**
     * Test of updateCustomerInfo method, of class
     * CustomerManagerImplementation.
     */
    @Test
    public void testUpdateCustomerInfo() {
        Customer customer = newCustomer("Petr", "Adamek", "Brno", "4-35-47", "AK 125798");
        manager.addCustomer(customer);
        String ID = customer.ID();
        customer = customer.withAddress("new address");
        manager.updateCustomerInfo(customer);
        Customer result = manager.findCustomerByID(ID);
        assertCustomerDeepEquals(customer, result);

        customer = newCustomer("Hello", "World", "Google", "4-25-41", "AK 785428");
        customer = customer.withID("non-existent-id");

        try {
            manager.updateCustomerInfo(customer);
            fail();
        } catch (TransactionException ex) {
        }
    }

    /**
     * Test of getActiveCustomers method, of class
     * CustomerManagerImplementation.
     */
    @Test
    public void testGetActiveCustomers() {
        Customer customer1 = newCustomer("Vitalii", "Chepeliuk", "Komarov", "5-20-86", "AK 373979");
        Customer customer2 = newCustomer("Jutaj", "Kolchak", "Komarov", "5-34-86", "AK 372548");
        Customer customer3 = newCustomer("Martin", "Jirman", "Lazhot", "5-25-87", "AK 251245");
        Customer customer4 = newCustomer("Martin", "Pulec", "Brno", "5-11-24", "AK 897589");
        Customer customer5 = newCustomer("Lukas", "Rucka", "Brno", "5-21-06", "AK 256354");
        customer1 = customer1.withActive(true);
        customer2 = customer2.withActive(true);
        customer3 = customer3.withActive(true);
        customer4 = customer4.withActive(true);
        customer5 = customer5.withActive(true);
        manager.addCustomer(customer1);
        manager.addCustomer(customer2);
        manager.addCustomer(customer3);
        manager.addCustomer(customer4);
        manager.addCustomer(customer5);

        List expResult = Arrays.asList(customer1, customer2, customer3, customer4, customer5);
        List result = manager.getActiveCustomers();
        assertEquals(expResult, result);
        assertCustomerDeepEquals(expResult, result);

        expResult = Arrays.asList(customer1, customer2, customer4, customer5);
        String ID = customer3.ID();
        customer3 = customer3.withActive(false);
        manager.updateCustomerInfo(customer3);
        result = manager.getActiveCustomers();
        assertCustomerDeepEquals(expResult, result);

        customer3 = customer3.withActive(true);
        expResult = Arrays.asList(customer1, customer2, customer3, customer4, customer5);
        manager.updateCustomerInfo(customer3);
        result = manager.getActiveCustomers();
        assertCustomerDeepEquals(expResult, result);
    }

    public static void assertCustomerDeepEquals(Customer expected, Customer actual) {
        assertEquals(expected.ID(), actual.ID());
        assertEquals(expected.firstName(), actual.firstName());
        assertEquals(expected.lastName(), actual.lastName());
        assertEquals(expected.address(), actual.address());
        assertEquals(expected.phoneNumber(), actual.phoneNumber());
        assertEquals(expected.active(), actual.active());
    }

    public static void assertCustomerDeepEquals(List<Customer> expected, List<Customer> actual) {
        assertEquals(expected.size(), actual.size());
        List<Customer> expectedSortedList = new ArrayList<>(expected);
        List<Customer> actualSortedList = new ArrayList<>(actual);
        Collections.sort(expectedSortedList, CustomerByIDComparator);
        Collections.sort(actualSortedList, CustomerByIDComparator);

        for (int i = 0; i < expectedSortedList.size(); i++) {
            assertCustomerDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }

    private static Comparator<Customer> CustomerByIDComparator = new Comparator<Customer>() {
        @Override
        public int compare(Customer customer1, Customer customer2) {
            return customer1.ID().compareTo(customer2.ID());
        }
    };
}
