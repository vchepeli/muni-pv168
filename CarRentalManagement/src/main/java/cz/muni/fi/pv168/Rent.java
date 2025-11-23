package cz.muni.fi.pv168;

import java.sql.Date;
import java.util.Objects;

public class Rent {

    public Rent() {
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public Long getCarID() {
        return carID;
    }

    public void setCarID(Long carID) {
        this.carID = carID;
    }

    public Long getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Long customerID) {
        this.customerID = customerID;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getRentDate() {
        return rentDate;
    }

    public void setRentDate(Date rentDate) {
        this.rentDate = rentDate;
    }

    @Override
    public String toString() {
        return "Rent{" + "ID=" + ID + ", rentDate=" + rentDate + ", dueDate=" + dueDate + ", carID=" + carID + ", customerID=" + customerID + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rent other = (Rent) obj;
        
        return (this.ID == other.ID);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.ID);
        return hash;
    }

    private Long ID;
    private Date rentDate;
    private Date dueDate;
    private Long carID;
    private Long customerID;
}
