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
     * Compact constructor for validation (empty body means no validation).
     */
    public Car {}

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

    public Car withColor(String newColor) {
        return new Car(ID, model, newColor, available, rentalPayment, licensePlate);
    }

    public Car withModel(String newModel) {
        return new Car(ID, newModel, color, available, rentalPayment, licensePlate);
    }

    public Car withLicensePlate(String newLicensePlate) {
        return new Car(ID, model, color, available, rentalPayment, newLicensePlate);
    }

    public Car withRentalPayment(Double newRentalPayment) {
        return new Car(ID, model, color, available, newRentalPayment, licensePlate);
    }
}
