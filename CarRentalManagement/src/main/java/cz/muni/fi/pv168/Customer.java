package cz.muni.fi.pv168;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.UUID;

@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
public class Customer {
    @Id
    @Column(name = "id")
    private String ID;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "drivers_license")
    private String driversLicense;

    @Column(name = "status")
    private Boolean active;

    /**
     * Factory method for creating new Customer instances with auto-generated UUID.
     * Use this when creating customers to be saved to the database.
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
