package cz.muni.fi.pv168;

import java.util.*;
import javax.swing.table.AbstractTableModel;

public class CustomersTableModel extends AbstractTableModel {

    private List<Customer> customers = new ArrayList<>();
    private ResourceBundle localization;

    public List<Customer> getCustomers()
    {
        return Collections.unmodifiableList(customers);
    }

    private Set<Customer> updatedCustomers;
    private Set<Customer> deletedCustomers;

    public Set<Customer> getUpdatedCustomers()
    {
        return Collections.unmodifiableSet(updatedCustomers);
    }

    public Set<Customer> getDeletedCustomers()
    {
        return Collections.unmodifiableSet(deletedCustomers);
    }
    
    public boolean hasNewCustomers()
    {
        for (Customer c : customers)
            if (c.getID() == null)
                return true;
        
        return false;
    }
    
    public void customerResolved(Customer customer)
    {
        updatedCustomers.remove(customer);
        deletedCustomers.remove(customer);
    }

    public void markCustomerForUpdate(Customer customer)
    {
        if (customer != null && customer.getID() != null) {
            updatedCustomers.add(customer);
        }
    }

    public CustomersTableModel(ResourceBundle localization)
    {
        this.localization = localization;

        updatedCustomers = new HashSet<>();
        deletedCustomers = new HashSet<>();
    }

    public void updateCustomers(List<Customer> newCustomers) {
        if (null == newCustomers) {
            return;
        }
        int firstRow = 0;
        int lastRow = customers.size() - 1;
        customers.clear();
        fireTableRowsDeleted(firstRow, lastRow < 0 ? 0 : lastRow);
        customers.addAll(newCustomers);
        Collections.sort(customers, CustomerByIDComparator);
        lastRow = customers.size() - 1;
        fireTableRowsInserted(firstRow, lastRow < 0 ? 0 : lastRow);
    }

    @Override
    public int getRowCount() {
        return (customers.size() + 1);
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return String.class;
            case 6:
                return Boolean.class;
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "ID";
            case 1:
                return localization.getString("first_name");
            case 2:
                return localization.getString("surname");
            case 3:
                return localization.getString("address");
            case 4:
                return localization.getString("phone_number");
            case 5:
                return localization.getString("driver_license");
            case 6:
                return localization.getString("active");
            default:
                throw new IllegalArgumentException("Column");
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex > customers.size()) {
            throw new IllegalArgumentException("Row Index Out Of Bounds.");
        }
        Customer customer = ((rowIndex == customers.size()) ? new Customer() : customers.get(rowIndex));
        switch (columnIndex) {
            case 0:
                return customer.getID();
            case 1:
                return customer.getFirstName();
            case 2:
                return customer.getLastName();
            case 3:
                return customer.getAddress();
            case 4:
                return customer.getPhoneNumber();
            case 5:
                return customer.getDriversLicense();
            case 6:
                return customer.getActive();
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Customer customer = null;
        if (rowIndex > customers.size()) {
            throw new IllegalArgumentException("Row Index Out of Bounds");
        }
        else if (rowIndex == customers.size())
        {
            customer = new Customer();
            add(customer);
        }
        else
            customer = customers.get(rowIndex);
        switch (columnIndex) {
            case 1:
            {
                if (customer.getID() != null)
                    updatedCustomers.add(customer);
                customer.setFirstName((String) aValue);
                break;
            }
            case 2:
            {
                if (customer.getID() != null)
                    updatedCustomers.add(customer);
                customer.setLastName((String) aValue);
                break;
            }
            case 3:
            {
                if (customer.getID() != null)
                    updatedCustomers.add(customer);
                customer.setAddress((String) aValue);
                break;
            }
            case 4:
            {
                if (customer.getID() != null)
                    updatedCustomers.add(customer);
                customer.setPhoneNumber((String) aValue);
                break;
            }
            case 5:
            {
                if (customer.getID() != null)
                    updatedCustomers.add(customer);
                customer.setDriversLicense((String) aValue);
                break;
            }
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return ((columnIndex > 0) && (columnIndex < 6));
    }
    private static Comparator<Customer> CustomerByIDComparator = new Comparator<Customer>() {

        @Override
        public int compare(Customer customer1, Customer customer2) {
            return Long.valueOf(customer1.getID()).compareTo(Long.valueOf(customer2.getID()));
        }
    };
    
    public void add(Customer customer)
    {
        customer.setActive(false);
        customers.add(customer);
        fireTableRowsInserted((customers.size() - 1), customers.size());
    }

    public void remove(Customer customer)
    {
        if (customer != null && customer.getID() != null) {
            deletedCustomers.add(customer);
            updatedCustomers.remove(customer);
        }
    }
}
