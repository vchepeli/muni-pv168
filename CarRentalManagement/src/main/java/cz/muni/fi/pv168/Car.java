package cz.muni.fi.pv168;

import java.util.Objects;
import jakarta.persistence.*;

@Entity
@Table(name = "cars")
public class Car {

    public Car() {
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getRentalPayment() {
        return rentalPayment;
    }

    public void setRentalPayment(Double rentalPayment) {
        this.rentalPayment = rentalPayment;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setStatus(Boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Car{" + "ID=" + ID + ", model=" + model + ", color=" + color + ", status=" + available + ", rentalPayment=" + rentalPayment + ", licensePlate=" + licensePlate + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Car other = (Car) obj;
        
        return (this.ID == other.ID);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.ID);
        return hash;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long ID;

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
}
