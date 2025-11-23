package cz.muni.fi.pv168;

import java.util.*;
import javax.swing.table.AbstractTableModel;

public class CarsTableModel extends AbstractTableModel {

    private List<Car> cars = new ArrayList<>();
    private ResourceBundle localization;

    public List<Car> getCars()
    {
        return Collections.unmodifiableList(cars);
    }

    private Set<Car> updatedCars;
    private Set<Car> deletedCars;

    public Set<Car> getUpdatedCars()
    {
        return Collections.unmodifiableSet(updatedCars);
    }

    public Set<Car> getDeletedCars()
    {
        return Collections.unmodifiableSet(deletedCars);
    }
    
    public boolean hasNewCars()
    {
        for (Car c : cars)
            if (c.ID() == null)
                return true;
        
        return false;
    }
    
    public void carResolved(Car car)
    {
        updatedCars.remove(car);
        deletedCars.remove(car);
    }

    public void markCarForUpdate(Car car)
    {
        if (car != null && car.ID() != null) {
            updatedCars.add(car);
        }
    }

    public CarsTableModel(ResourceBundle localization)
    {
        this.localization = localization;

        updatedCars = new HashSet<>();
        deletedCars = new HashSet<>();
    }
    
    public void updateCars(List<Car> newCars) {
        if (null == newCars) {
            return;
        }

        int firstRow = 0;
        int lastRow = cars.size() - 1;
        cars.clear();
        fireTableRowsDeleted(firstRow, lastRow < 0 ? 0 : lastRow);
        cars.addAll(newCars);
        Collections.sort(cars, carByIDComparator);
        lastRow = cars.size() - 1;
        fireTableRowsInserted(firstRow, lastRow < 0 ? 0 : lastRow);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
            case 2:
            case 3:
                return String.class;
            case 4:
                return Double.class;
            case 5:
                return Boolean.class;
            default:
                throw new IllegalArgumentException("Column index");
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "ID";
            case 1:
                return localization.getString("model");
            case 2:
                return localization.getString("colour");
            case 3:
                return localization.getString("license_plate");
            case 4:
                return localization.getString("price");
            case 5:
                return localization.getString("available");
            default:
                throw new IllegalArgumentException("Column");
        }
    }

    @Override
    public int getRowCount() {
        return (cars.size() + 1);
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex > cars.size()) {
            throw new IllegalArgumentException("Row Index Out of Bounds");
        }
        Car car = ((rowIndex == cars.size()) ? Car.create("", "", true, 0.0, "") : cars.get(rowIndex));
        switch (columnIndex) {
            case 0:
                return car.ID();
            case 1:
                return car.model();
            case 2:
                return car.color();
            case 3:
                return car.licensePlate();
            case 4:
                return car.rentalPayment();
            case 5:
                return car.available();
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Car car = null;
        if (rowIndex > cars.size()) {
            throw new IllegalArgumentException("Row Index Out of Bounds");
        }
        else if (rowIndex == cars.size())
        {
            car = Car.create("", "", true, 0.0, "");
            add(car);
        }
        else
            car = cars.get(rowIndex);

        Car updatedCar = car;
        switch (columnIndex) {
            case 1:
            {
                updatedCar = new Car(car.ID(), (String) aValue, car.color(), car.available(), car.rentalPayment(), car.licensePlate());
                break;
            }
            case 2:
            {
                updatedCar = new Car(car.ID(), car.model(), (String) aValue, car.available(), car.rentalPayment(), car.licensePlate());
                break;
            }
            case 3:
            {
                updatedCar = new Car(car.ID(), car.model(), car.color(), car.available(), car.rentalPayment(), (String) aValue);
                break;
            }
            case 4:
            {
                updatedCar = new Car(car.ID(), car.model(), car.color(), car.available(), (Double) aValue, car.licensePlate());
                break;
            }
            default:
                throw new IllegalArgumentException("Column Index");
        }

        if (updatedCar.ID() != null) {
            updatedCars.remove(car);
            updatedCars.add(updatedCar);
        }
        cars.set(rowIndex, updatedCar);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return ((columnIndex > 0) && (columnIndex < 5));
    }
    private static Comparator<Car> carByIDComparator = new Comparator<Car>() {

        @Override
        public int compare(Car car1, Car car2) {
            return Long.valueOf(car1.ID()).compareTo(Long.valueOf(car2.ID()));
        }
    };
    
    public void add(Car car)
    {
        car = car.withStatus(true);
        cars.add(car);
        fireTableRowsInserted((cars.size() - 1), cars.size());
    }

    public void remove(Car car)
    {
        if (car != null && car.ID() != null) {
            deletedCars.add(car);
            updatedCars.remove(car);
        }
    }
}
