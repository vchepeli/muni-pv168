package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.util.List;
import javax.sql.DataSource;

public interface CustomerManager {

    public void addCustomer(Customer customer) throws IllegalArgumentException, TransactionException;

    public void removeCustomer(Customer customer) throws IllegalArgumentException, TransactionException;

    public Customer findCustomerByID(Long ID) throws IllegalArgumentException, TransactionException;

    public List<Customer> getAllCustomers() throws IllegalArgumentException, TransactionException;

    public void updateCustomerInfo(Customer customer) throws IllegalArgumentException, TransactionException;

    public List<Customer> getActiveCustomers() throws IllegalArgumentException, TransactionException;
    
    public void setDataSource(DataSource ds);
    
    public void setLogger(FileOutputStream fs);
}
