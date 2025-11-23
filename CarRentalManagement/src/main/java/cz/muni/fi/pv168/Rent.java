package cz.muni.fi.pv168;

import java.sql.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "rents")
public record Rent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long ID,

    @Column(name = "rent_date")
    Date rentDate,

    @Column(name = "due_date")
    Date dueDate,

    @Column(name = "car")
    Long carID,

    @Column(name = "customer")
    Long customerID
) {
    public Rent() {
        this(null, null, null, null, null);
    }

    public Rent withID(Long id) {
        return new Rent(id, rentDate, dueDate, carID, customerID);
    }
}
