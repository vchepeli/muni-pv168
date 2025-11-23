package cz.muni.fi.pv168;

import java.sql.Date;
import jakarta.persistence.*;
import org.hibernate.annotations.Instantiator;

@Entity
@Table(name = "rents")
public record Rent(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rent_id_seq")
    @SequenceGenerator(name = "rent_id_seq", sequenceName = "rent_id_sequence", allocationSize = 1)
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

    @Instantiator
    public static Rent of(Long id, Date rentDate, Date dueDate, Long carID, Long customerID) {
        return new Rent(id, rentDate, dueDate, carID, customerID);
    }

    public Rent withID(Long id) {
        return new Rent(id, rentDate, dueDate, carID, customerID);
    }
}
