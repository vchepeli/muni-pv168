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
    public Car() {
        this(UUID.randomUUID().toString(), null, null, null, null, null);
    }

    public Car withID(String id) {
        return new Car(id, model, color, available, rentalPayment, licensePlate);
    }

    public Car withStatus(Boolean status) {
        return new Car(ID, model, color, status, rentalPayment, licensePlate);
    }
}
