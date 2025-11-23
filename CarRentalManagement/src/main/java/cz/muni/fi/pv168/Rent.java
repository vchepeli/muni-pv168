package cz.muni.fi.pv168;

import java.sql.Date;
import jakarta.persistence.*;
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
    public Rent() {
        this(UUID.randomUUID().toString(), null, null, null, null);
    }

    public Rent withID(String id) {
        return new Rent(id, rentDate, dueDate, carID, customerID);
    }
}
