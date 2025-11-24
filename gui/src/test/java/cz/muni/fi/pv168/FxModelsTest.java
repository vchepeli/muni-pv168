package cz.muni.fi.pv168;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DisplayName("JavaFX Models Tests")
public class FxModelsTest {

    @Nested
    @DisplayName("CarsFxModel Tests")
    class CarsModelTests {
        @Test
        @DisplayName("Should merge offline cars with DB cars correctly")
        void testCarsFxModelMerge() {
            CarsFxModel model = new CarsFxModel();
            
            // 1. Add offline car
            Car offlineCar = Car.create("OfflineModel", "Red", true, 100.0, "OFF-123");
            model.addCar(offlineCar); // In addedCars
            
            assertTrue(model.getAddedCars().contains(offlineCar));
            assertEquals(1, model.getCars().size());
            
            // 2. Simulate DB sync (fetching existing cars from DB)
            Car dbCar1 = Car.create("DBModel1", "Blue", true, 200.0, "DB-111");
            Car dbCar2 = Car.create("DBModel2", "Green", true, 300.0, "DB-222");
            List<Car> dbCars = new ArrayList<>();
            dbCars.add(dbCar1);
            dbCars.add(dbCar2);
            
            model.mergeCars(dbCars);
            
            // 3. Verify merge: should have 3 cars (2 from DB + 1 offline preserved)
            assertEquals(3, model.getCars().size());
            assertTrue(model.getCars().contains(offlineCar));
            assertTrue(model.getCars().contains(dbCar1));
            assertTrue(model.getCars().contains(dbCar2));
            
            // Offline car should still be in addedCars set
            assertTrue(model.getAddedCars().contains(offlineCar));
        }
    }

    @Nested
    @DisplayName("CustomersFxModel Tests")
    class CustomersModelTests {
        @Test
        @DisplayName("Should merge offline customers with DB customers correctly")
        void testCustomersFxModelMerge() {
            CustomersFxModel model = new CustomersFxModel();
            Customer offlineCust = Customer.create("Off", "Line", "Addr", "123", "LIC-1", true);
            model.addCustomer(offlineCust);
            
            assertTrue(model.getAddedCustomers().contains(offlineCust));
            
            List<Customer> dbCusts = new ArrayList<>();
            dbCusts.add(Customer.create("DB", "User", "DBAddr", "456", "LIC-2", true));
            
            model.mergeCustomers(dbCusts);
            
            assertEquals(2, model.getCustomers().size());
            assertTrue(model.getCustomers().contains(offlineCust));
            assertTrue(model.getAddedCustomers().contains(offlineCust));
        }
    }

    @Nested
    @DisplayName("RentsFxModel Tests")
    class RentsModelTests {
        @Test
        @DisplayName("Should merge offline rents with DB rents correctly")
        void testRentsFxModelMerge() {
            RentsFxModel model = new RentsFxModel();
            // Fake DB IDs for Car/Customer as they are just strings in Rent
            Rent offlineRent = Rent.create(java.sql.Date.valueOf("2023-01-01"), java.sql.Date.valueOf("2023-01-05"), UUID.randomUUID().toString(), UUID.randomUUID().toString());
            model.addRent(offlineRent);
            
            List<Rent> dbRents = new ArrayList<>();
            dbRents.add(Rent.create(java.sql.Date.valueOf("2023-02-01"), java.sql.Date.valueOf("2023-02-05"), UUID.randomUUID().toString(), UUID.randomUUID().toString()));
            
            model.mergeRents(dbRents);
            
            assertEquals(2, model.getRents().size());
            assertTrue(model.getRents().contains(offlineRent));
            assertTrue(model.getAddedRents().contains(offlineRent));
        }
    }
}