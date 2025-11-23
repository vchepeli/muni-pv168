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
     * Instantiator for Hibernate to construct Rent records from database rows.
     * This method receives all fields from the database at once, avoiding
     * the need for Hibernate to modify final record fields after instantiation.
     */
    @Instantiator("instantiate")
    public static Rent instantiate(String id, Date rentDate, Date dueDate, String carID, String customerID) {
        return new Rent(id, rentDate, dueDate, carID, customerID);
    }

    /**
     * Factory method for creating new Rent instances with auto-generated UUID.
     * Use this when creating rents to be saved to the database.
     */
    public static Rent create(Date rentDate, Date dueDate, String carID, String customerID) {
        return new Rent(UUID.randomUUID().toString(), rentDate, dueDate, carID, customerID);
    }

    public Rent withID(String id) {
        return new Rent(id, rentDate, dueDate, carID, customerID);
    }
}
