package cz.muni.fi.pv168;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class CustomersFxModel {

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final Set<Customer> addedCustomers = new HashSet<>();
    private final Set<Customer> updatedCustomers = new HashSet<>();
    private final Set<Customer> deletedCustomers = new HashSet<>();

    public ObservableList<Customer> getCustomers() {
        return customers;
    }

    public Set<Customer> getAddedCustomers() {
        return Collections.unmodifiableSet(addedCustomers);
    }

    public Set<Customer> getUpdatedCustomers() {
        return Collections.unmodifiableSet(updatedCustomers);
    }

    public Set<Customer> getDeletedCustomers() {
        return Collections.unmodifiableSet(deletedCustomers);
    }

    public void mergeCustomers(List<Customer> dbCustomers) {
        List<Customer> offlineCustomers = new ArrayList<>(addedCustomers);

        customers.setAll(dbCustomers);
        customers.addAll(offlineCustomers);
        
        updatedCustomers.clear();
        deletedCustomers.clear();
    }

    public void addCustomer(Customer customer) {
        if (customer != null) {
            customers.add(customer);
            addedCustomers.add(customer);
        }
    }

    public void markForDeletion(Customer customer) {
        if (customer != null) {
            if (addedCustomers.contains(customer)) {
                addedCustomers.remove(customer);
            } else {
                deletedCustomers.add(customer);
                updatedCustomers.remove(customer);
            }
        }
    }
    
    public void unmarkFromDeletion(Customer customer) {
        deletedCustomers.remove(customer);
    }

    public void removeCustomerFromList(Customer customer) {
        if (customer != null) {
            customers.remove(customer);
            addedCustomers.remove(customer);
            updatedCustomers.remove(customer);
            deletedCustomers.remove(customer);
        }
    }

    public void updateCustomer(Customer oldCustomer, Customer newCustomer) {
        if (oldCustomer != null && newCustomer != null) {
            int index = customers.indexOf(oldCustomer);
            if (index >= 0) {
                customers.set(index, newCustomer);
                
                if (addedCustomers.contains(oldCustomer)) {
                    addedCustomers.remove(oldCustomer);
                    addedCustomers.add(newCustomer);
                } else {
                    updatedCustomers.remove(oldCustomer);
                    updatedCustomers.add(newCustomer);
                }
            }
        }
    }

    public void customerResolved(Customer customer) {
        addedCustomers.remove(customer);
        updatedCustomers.remove(customer);
        deletedCustomers.remove(customer);
    }
    
    public void removeFromDeleted(Customer customer) {
        deletedCustomers.remove(customer);
    }
}
