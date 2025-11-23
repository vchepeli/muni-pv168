package cz.muni.fi.pv168;

import jakarta.persistence.*;
import org.hibernate.annotations.Instantiator;
import java.util.UUID;

@Entity
@Table(name = "customers")
public record Customer(
    @Id
    @Column(name = "id")
    String ID,

    @Column(name = "first_name")
    String firstName,

    @Column(name = "last_name")
    String lastName,

    @Column(name = "address")
    String address,

    @Column(name = "phone_number")
    String phoneNumber,

    @Column(name = "drivers_license")
    String driversLicense,

    @Column(name = "status")
    Boolean active
) {
    @Instantiator
    public Customer(String ID, String firstName, String lastName, String address, String phoneNumber, String driversLicense, Boolean active) {}

    /**
     * Factory method for creating new Customer instances with auto-generated UUID.
     * Use this when creating customers to be saved to the database.
     * The record's canonical constructor is used by Hibernate for loading from DB.
     */
    public static Customer create(String firstName, String lastName, String address, String phoneNumber, String driversLicense, Boolean active) {
        return new Customer(UUID.randomUUID().toString(), firstName, lastName, address, phoneNumber, driversLicense, active);
    }

    public Customer withID(String id) {
        return new Customer(id, firstName, lastName, address, phoneNumber, driversLicense, active);
    }

    public Customer withActive(Boolean status) {
        return new Customer(ID, firstName, lastName, address, phoneNumber, driversLicense, status);
    }

    public Customer withAddress(String newAddress) {
        return new Customer(ID, firstName, lastName, newAddress, phoneNumber, driversLicense, active);
    }

    public Customer withFirstName(String newFirstName) {
        return new Customer(ID, newFirstName, lastName, address, phoneNumber, driversLicense, active);
    }

    public Customer withLastName(String newLastName) {
        return new Customer(ID, firstName, newLastName, address, phoneNumber, driversLicense, active);
    }

    public Customer withPhoneNumber(String newPhoneNumber) {
        return new Customer(ID, firstName, lastName, address, newPhoneNumber, driversLicense, active);
    }

    public Customer withDriversLicense(String newDriversLicense) {
        return new Customer(ID, firstName, lastName, address, phoneNumber, newDriversLicense, active);
    }
}
