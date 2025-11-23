package cz.muni.fi.pv168;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public record Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long ID,

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
        this(null, null, null, null, null, null, null);
    }

    public Customer withID(Long id) {
        return new Customer(id, firstName, lastName, address, phoneNumber, driversLicense, active);
    }

    public Customer withActive(Boolean status) {
        return new Customer(ID, firstName, lastName, address, phoneNumber, driversLicense, status);
    }
}
