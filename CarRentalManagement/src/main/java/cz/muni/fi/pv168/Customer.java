package cz.muni.fi.pv168;

import jakarta.persistence.*;
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
    /**
     * Instantiator for Hibernate to construct Customer records from database rows.
     * This method receives all fields from the database at once, avoiding
     * the need for Hibernate to modify final record fields after instantiation.
     */
    @Instantiator
    public static Customer instantiate(String id, String firstName, String lastName, String address, String phoneNumber, String driversLicense, Boolean active) {
        return new Customer(id, firstName, lastName, address, phoneNumber, driversLicense, active);
    }

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
}
