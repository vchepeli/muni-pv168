package cz.muni.fi.pv168;

import java.sql.Date;
import jakarta.persistence.*;
import org.hibernate.annotations.Instantiator;
import java.util.UUID;

@Entity
@Table(name = "rents")
public record Rent(
    @Id
    @Column(name = "id")
    String ID,

    @Column(name = "rent_date")
    Date rentDate,

    @Column(name = "due_date")
    Date dueDate,

    @Column(name = "car")
    String carID,

    @Column(name = "customer")
    String customerID
) {
    /**
     * Factory method for creating new Rent instances with auto-generated UUID.
     * Use this when creating rents to be saved to the database.
     */
    public static Rent create(Date rentDate, Date dueDate, String carID, String customerID) {
        return new Rent(UUID.randomUUID().toString(), rentDate, dueDate, carID, customerID);
    }

    /**
     * Canonical constructor for Hibernate instantiation.
     * This explicit constructor tells Hibernate how to instantiate Rent records
     * when loading from the database.
     */
    @Instantiator
    public Rent(String ID, Date rentDate, Date dueDate, String carID, String customerID) {
        this.ID = ID;
        this.rentDate = rentDate;
        this.dueDate = dueDate;
        this.carID = carID;
        this.customerID = customerID;
    }

    public Rent withID(String id) {
        return new Rent(id, rentDate, dueDate, carID, customerID);
    }
}
