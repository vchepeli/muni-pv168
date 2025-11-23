package cz.muni.fi.pv168;

import java.util.Objects;

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
    
    private Long ID;
    private String model;
    private String color;
    private Boolean available;
    private Double rentalPayment;
    private String licensePlate;
}
