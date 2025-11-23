package cz.muni.fi.pv168;

import jakarta.persistence.*;

@Entity
@Table(name = "cars")
public record Car(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "car_id_seq")
    @SequenceGenerator(name = "car_id_seq", sequenceName = "car_id_sequence", allocationSize = 1)
    @Column(name = "id")
    Long ID,

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
        this(null, null, null, null, null, null);
    }

    public Car withID(Long id) {
        return new Car(id, model, color, available, rentalPayment, licensePlate);
    }

    public Car withStatus(Boolean status) {
        return new Car(ID, model, color, status, rentalPayment, licensePlate);
    }
}
