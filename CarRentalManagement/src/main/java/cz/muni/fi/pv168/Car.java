package cz.muni.fi.pv168;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.UUID;

@Entity
@Table(name = "cars")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
public class Car {
    @Id
    @Column(name = "id")
    private String uuid;

    @Column(name = "model")
    private String model;

    @Column(name = "color")
    private String color;

    @Column(name = "status")
    private Boolean available;

    @Column(name = "payment")
    private Double rentalPayment;

    @Column(name = "license_plate")
    private String licensePlate;

    /**
     * Factory method for creating new Car instances with auto-generated UUID.
     * Use this when creating cars to be saved to the database.
     */
    public static Car create(String model, String color, Boolean available, Double rentalPayment, String licensePlate) {
        return new Car(UUID.randomUUID().toString(), model, color, available, rentalPayment, licensePlate);
    }

    public Car withUuid(String uuid) {
        return new Car(uuid, model, color, available, rentalPayment, licensePlate);
    }

    public Car withStatus(Boolean status) {
        return new Car(uuid, model, color, status, rentalPayment, licensePlate);
    }

    public Car withColor(String newColor) {
        return new Car(uuid, model, newColor, available, rentalPayment, licensePlate);
    }

    public Car withModel(String newModel) {
        return new Car(uuid, newModel, color, available, rentalPayment, licensePlate);
    }

    public Car withLicensePlate(String newLicensePlate) {
        return new Car(uuid, model, color, available, rentalPayment, newLicensePlate);
    }

    public Car withRentalPayment(Double newRentalPayment) {
        return new Car(uuid, model, color, available, newRentalPayment, licensePlate);
    }
}
