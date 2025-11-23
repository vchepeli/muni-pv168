package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.sql.DataSource;

public class CustomerManagerImplementation implements CustomerManager {

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void addCustomer(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("Can't INSERT NULL entry");
        }
        if (null != customer.getID()) {
            throw new IllegalArgumentException("Customer ID was SET BEFORE");
        }
        if (null == customer.getFirstName() || null == customer.getLastName() || null == customer.getAddress() || null == customer.getPhoneNumber()
                || null == customer.getDriversLicense()) {
            throw new IllegalArgumentException("Customer with WRONG PARAMETERS");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(
                    "INSERT INTO CUSTOMERS (first_name, last_name, address, phone_number, drivers_license, status) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getAddress());
            statement.setString(4, customer.getPhoneNumber());
            statement.setString(5, customer.getDriversLicense());
            statement.setBoolean(6, customer.getActive());

            if (1 != statement.executeUpdate()) {
                throw new TransactionException("Can't INSERT Customer in DB" + customer);
            }
            Long ID = DBUtils.getID(statement.getGeneratedKeys());
            customer.setID(ID);
            logger.log(Level.INFO, ("New Customer ID " + customer.getID() + " added"));
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error INSERT Customer to DB", ex);
            throw new TransactionException("Error INSERT Customer to DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public void removeCustomer(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("Can not DELETE NULL ENTRY from Customers");
        }
        if (null == customer.getID()) {
            throw new IllegalArgumentException("Can't DELETE Customer with NULL ID");
        }
        if (customer.getActive()) {
            throw new IllegalArgumentException("Can't DELETE active Customer");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("DELETE FROM CUSTOMERS WHERE ID=?");
            statement.setLong(1, customer.getID());
            if (0 == statement.executeUpdate()) {
                throw new IllegalArgumentException("Can't locate Customer in DB");
            }
            logger.log(Level.INFO, ("Customer ID " + customer.getID() + " removed"));
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error DELETE Customer from DB", ex);
            throw new TransactionException("Error DELETE Customer from DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public Customer findCustomerByID(Long ID) throws IllegalArgumentException, TransactionException {
        if (null == ID) {
            throw new IllegalArgumentException("Can't find Customer with NULL ID");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT id,first_name,last_name,address,phone_number,drivers_license,status FROM CUSTOMERS WHERE id = ?");
            statement.setLong(1, ID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Customer result = getCustomerFromResultSet(resultSet);
                if (resultSet.next()) {
                    throw new TransactionException("Error multiple Customers with same ID found");
                }
                return result;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when gettind Customer from DB", ex);
            throw new TransactionException("Error when gettind Customer from DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<Customer> getAllCustomers() throws IllegalArgumentException, TransactionException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM CUSTOMERS");
            ResultSet resultSet = statement.executeQuery();
            List<Customer> allCustomers = new ArrayList<>();
            while (resultSet.next()) {
                allCustomers.add(getCustomerFromResultSet(resultSet));
            }
            return allCustomers;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when getting all Customers", ex);
            throw new TransactionException("Error when getting all Customers", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public void updateCustomerInfo(Customer customer) throws IllegalArgumentException, TransactionException {
        if (null == customer) {
            throw new IllegalArgumentException("Can't UPDATE NULL ENTRY to CustomersDB");
        }
        if (null == customer.getID()) {
            throw new IllegalArgumentException("Can't UPDATE Customer with WRONG ID");
        }
        if ((null == customer.getFirstName()) || (null == customer.getLastName()) || (null == customer.getAddress())
                || (null == customer.getPhoneNumber()) || (null == customer.getDriversLicense())) {
            throw new IllegalArgumentException("Customer with WRONG PARAMETRS");
        }

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("UPDATE CUSTOMERS SET first_name = ?, last_name = ?, address = ?,"
                    + "phone_number = ?, drivers_license = ?, status = ? WHERE id = ?");
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getAddress());
            statement.setString(4, customer.getPhoneNumber());
            statement.setString(5, customer.getDriversLicense());
            statement.setBoolean(6, customer.getActive());
            statement.setLong(7, customer.getID());
            if (0 == statement.executeUpdate()) {
                throw new TransactionException("Customer with given ID not exist");
            }
            logger.log(Level.INFO, ("Customer ID " + customer.getID() + " updated"));
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when UPDATE Customer in DB", ex);
            throw new TransactionException("Error when UPDATE Customer in DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<Customer> getActiveCustomers() throws IllegalArgumentException, TransactionException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM CUSTOMERS WHERE STATUS = TRUE");
            ResultSet resultSet = statement.executeQuery();
            List<Customer> activeCustomers = new ArrayList<>();
            while (resultSet.next()) {
                activeCustomers.add(getCustomerFromResultSet(resultSet));
            }
            return activeCustomers;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when getting all Customers from DB", ex);
            throw new TransactionException("Error when getting all Customers from DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    private Customer getCustomerFromResultSet(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setID(resultSet.getLong("id"));
        customer.setFirstName(resultSet.getString("first_name"));
        customer.setLastName(resultSet.getString("last_name"));
        customer.setAddress(resultSet.getString("address"));
        customer.setPhoneNumber(resultSet.getString("phone_number"));
        customer.setDriversLicense(resultSet.getString("drivers_license"));
        customer.setActive(resultSet.getBoolean("status"));
        return customer;
    }

    public void tryCreateTables() {
        if (null == dataSource) {
            throw new IllegalStateException("DataSource is not set");
        }
        try {
            DBUtils.tryCreateTables(dataSource);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error when trying to create tables");
        }
    }
    public static final Logger logger = Logger.getLogger(CustomerManagerImplementation.class.getName());
    private DataSource dataSource;

    @Override
    public void setLogger(FileOutputStream fs) {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
}
