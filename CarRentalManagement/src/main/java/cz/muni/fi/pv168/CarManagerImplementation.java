package cz.muni.fi.pv168;

import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.sql.DataSource;

public class CarManagerImplementation implements CarManager {

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void addCar(Car car) throws TransactionException {
        //Check if the Arguments are valid:
        if (null == car) {
            throw new IllegalArgumentException("Can not INSERT NULL ENTRY to CARS");
        }
        if (null != car.getID()) {
            throw new IllegalArgumentException("Car ID was SET BEFORE");
        }
        if ((null == car.getColor()) || (null == car.getLicensePlate()) || (null == car.getModel())
                || (null == car.getRentalPayment()) || (car.getRentalPayment() < 0)) {
            throw new IllegalArgumentException("Car with WRONG PARAMETERS");
        }
        //Insert Car into DB:
        Connection connection = null;
        PreparedStatement statement;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("INSERT INTO CARS (color, license_plate, model, payment, "
                    + "status) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, car.getColor().toString());
            statement.setString(2, car.getLicensePlate());
            statement.setString(3, car.getModel());
            statement.setDouble(4, car.getRentalPayment());
            statement.setBoolean(5, car.getAvailable());

            int addedRows = statement.executeUpdate();
            if (1 != addedRows) {
                throw new TransactionException("DB error when trying to INSERT Car" + car);
            }
            Long ID = DBUtils.getID(statement.getGeneratedKeys());
            car.setID(ID);
            logger.log(Level.INFO, ("New Car ID " + car.getID() + " added"));
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when INSERT Car into DB" + car, ex);
            throw new TransactionException("Error when INSERT Car into DB" + car, ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public void removeCar(Car car) throws TransactionException {
        //Check if the Argument is valid:
        if (null == car) {
            throw new IllegalArgumentException("Can't DELETE NULL ENTRY from CarDB");
        }
        if (null == car.getID()) {
            throw new IllegalArgumentException("Can't DELETE Car with NO ID");
        }
        if (!car.getAvailable()) {
            throw new IllegalArgumentException("Can't DELETE rented Car");
        }
        //Remove Car from DB
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("DELETE FROM CARS WHERE id=?");
            statement.setLong(1, car.getID());

            if (0 == statement.executeUpdate()) {
                throw new TransactionException("Given Car does not exist in DB" + car);
            }
            
            logger.log(Level.INFO, ("Car ID " + car.getID() + " removed"));
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when DELETE Car from DB", ex);
            throw new TransactionException("Error when DELETE Car from DB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public Car findCarByID(Long ID) throws TransactionException {
        //Check if the Argument is valid:
        if (null == ID) {
            throw new IllegalArgumentException("Can't locate Car with null ID");
        }
        //Find Car in DB
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM CARS WHERE id=?");
            statement.setLong(1, ID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Car result = getCarFromResultSet(resultSet);
                if (resultSet.next()) {
                    throw new TransactionException("Error multiple cars with same ID found");
                }
                return result;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error SELECT Car from DB with ID" + ID, ex);
            throw new TransactionException("Error SELECT Car from DB with ID" + ID, ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public void updateCarInfo(Car car) throws TransactionException {
        //Check if the Argument is valid:
        if (null == car) {
            throw new IllegalArgumentException("Can't INSERT NULL ENTRY to CarDB");
        }
        if (null == car.getID()) {
            throw new IllegalArgumentException("Can't UPDATE Car with NULL ID");
        }
        if ((null == car.getColor()) || (null == car.getLicensePlate()) || (null == car.getModel())
                || (null == car.getRentalPayment()) || (car.getRentalPayment() < 0) || (null == car.getAvailable())) {
            throw new IllegalArgumentException("Car with WRONG PARAMETRS");
        }

        //Update Car in DB:
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("UPDATE CARS SET color = ?,license_plate = ?,model = ?,payment = ?,status = ? WHERE id=?");
            statement.setString(1, car.getColor().toString());
            statement.setString(2, car.getLicensePlate());
            statement.setString(3, car.getModel());
            statement.setDouble(4, car.getRentalPayment());
            statement.setBoolean(5, car.getAvailable());
            statement.setLong(6, car.getID());
            if (0 == statement.executeUpdate()) {
                throw new TransactionException("Error UPDATE Car from DB with ID " + car.getID());
            }
            logger.log(Level.INFO, ("Car ID " + car.getID() + " updated"));
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error UPDATE Car from DB with ID " + car.getID(), ex);
            throw new TransactionException("Error UPDATE Car from DB with ID " + car.getID(), ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<Car> getAvailableCars() throws TransactionException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM CARS WHERE status = TRUE");
            ResultSet resultSet = statement.executeQuery();
            List<Car> availableCars = new ArrayList<>();
            while (resultSet.next()) {
                availableCars.add(getCarFromResultSet(resultSet));
            }
            return availableCars;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when getting available Cars from CarsDB", ex);
            throw new TransactionException("Error when getting available Cars from CarsDB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    @Override
    public List<Car> getAllCars() throws TransactionException {

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM CARS");
            ResultSet resultSet = statement.executeQuery();

            List<Car> allCars = new ArrayList<>();
            while (resultSet.next()) {
                allCars.add(getCarFromResultSet(resultSet));
            }
            return allCars;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error when SELECT all cars from CarsDB", ex);
            throw new TransactionException("Error when SELECT all cars from CarsDB", ex);
        } finally {
            DBUtils.closeQuietly(connection);
        }
    }

    private Car getCarFromResultSet(ResultSet resultSet) throws SQLException {
        Car car = new Car();
        car.setID(resultSet.getLong("id"));
        car.setColor(resultSet.getString("color"));
        car.setLicensePlate(resultSet.getString("license_plate"));
        car.setModel(resultSet.getString("model"));
        car.setRentalPayment(resultSet.getDouble("payment"));
        car.setStatus(resultSet.getBoolean("status"));
        return car;
    }

    public void tryCreateTables() {
        if (null == dataSource) {
            throw new IllegalStateException("DataSource is not set");
        }
        try {
            DBUtils.tryCreateTables(dataSource);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error when trying to create tables");
        }
    }
    public static final Logger logger = Logger.getLogger(CarManagerImplementation.class.getName());
    private DataSource dataSource;

    @Override
    public void setLogger(FileOutputStream fs) {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
}
