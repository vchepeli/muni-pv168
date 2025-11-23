package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.sql.Date;
import java.util.List;
import javax.sql.DataSource;

public interface RentManager {

    public Customer findCustomerWithCar(Car car) throws IllegalArgumentException, TransactionException;

    public List<Car> getAllCustomerCars(Customer customer) throws IllegalArgumentException, TransactionException;

    public void rentCarToCustomer(Car car, Customer customer, Date rentDate, Date dueDate) throws IllegalArgumentException, TransactionException;

    public void getCarFromCustomer(Car car, Customer customer) throws IllegalArgumentException, TransactionException;

    public Rent findRentWithCar(Car car) throws IllegalArgumentException, TransactionException;

    public List<Rent> getAllRents() throws IllegalArgumentException, TransactionException;

    public void setDataSource(DataSource ds);

    public void setLogger(FileOutputStream fs);

    public void tryCreateTables();
}
