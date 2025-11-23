package cz.muni.fi.pv168;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cars")
public record Car(
    @Id
    @Column(name = "id")
    String ID,

    @Column(name = "model")
    String model,

    @Column(name = "color")
    String color,

    @Column(name = "status")
    Boolean available,

    @Column(name = "payment")
    Double rentalPayment,

    @Column(name = "license_plate")
    String licensePlate
) {
    /**
     * Instantiator for Hibernate to construct Car records from database rows.
     * This method receives all fields from the database at once, avoiding
     * the need for Hibernate to modify final record fields after instantiation.
     */
    @Instantiator
    public static Car instantiate(String id, String model, String color, Boolean available, Double rentalPayment, String licensePlate) {
        return new Car(id, model, color, available, rentalPayment, licensePlate);
    }

    /**
     * Factory method for creating new Car instances with auto-generated UUID.
     * Use this when creating cars to be saved to the database.
     */
    public static Car create(String model, String color, Boolean available, Double rentalPayment, String licensePlate) {
        return new Car(UUID.randomUUID().toString(), model, color, available, rentalPayment, licensePlate);
    }

    public Car withID(String id) {
        return new Car(id, model, color, available, rentalPayment, licensePlate);
    }

    public Car withStatus(Boolean status) {
        return new Car(ID, model, color, status, rentalPayment, licensePlate);
    }
}
