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
    public Customer() {
        this(UUID.randomUUID().toString(), null, null, null, null, null, null);
    }

    public Customer withID(String id) {
        return new Customer(id, firstName, lastName, address, phoneNumber, driversLicense, active);
    }

    public Customer withActive(Boolean status) {
        return new Customer(ID, firstName, lastName, address, phoneNumber, driversLicense, status);
    }
}
