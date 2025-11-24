package cz.muni.fi.pv168;

import java.sql.Date;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.UUID;

@Entity
@Table(name = "rents")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
public class Rent {
    @Id
    @Column(name = "id")
    private String uuid;

    @Column(name = "rent_date")
    private Date rentDate;

    @Column(name = "due_date")
    private Date dueDate;

    @Column(name = "car")
    private String carID;

    @Column(name = "customer")
    private String customerID;

    /**
     * Factory method for creating new Rent instances with auto-generated UUID.
     * Use this when creating rents to be saved to the database.
     */
    public static Rent create(Date rentDate, Date dueDate, String carID, String customerID) {
        return new Rent(UUID.randomUUID().toString(), rentDate, dueDate, carID, customerID);
    }

    public Rent withUuid(String uuid) {
        return new Rent(uuid, rentDate, dueDate, carID, customerID);
    }

    public Rent withRentDate(Date newRentDate) {
        return new Rent(uuid, newRentDate, dueDate, carID, customerID);
    }

    public Rent withDueDate(Date newDueDate) {
        return new Rent(uuid, rentDate, newDueDate, carID, customerID);
    }
}
