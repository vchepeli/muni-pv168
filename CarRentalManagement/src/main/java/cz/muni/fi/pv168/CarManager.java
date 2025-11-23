package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.util.List;
import javax.sql.DataSource;

public interface CarManager {

    public void addCar(Car car) throws TransactionException;

    public void removeCar(Car car) throws TransactionException;

    public Car findCarByID(String ID) throws TransactionException;

    public void updateCarInfo(Car car) throws TransactionException;

    public List<Car> getAllCars() throws TransactionException;

    public List<Car> getAvailableCars() throws TransactionException;

    public void setDataSource(DataSource ds);

    public void setLogger(FileOutputStream fs);
}
