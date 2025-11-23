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
            if (c.getID() == null)
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
        if (car != null && car.getID() != null) {
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
        Car car = ((rowIndex == cars.size()) ? new Car() : cars.get(rowIndex));
        switch (columnIndex) {
            case 0:
                return car.getID();
            case 1:
                return car.getModel();
            case 2:
                return car.getColor();
            case 3:
                return car.getLicensePlate();
            case 4:
                return car.getRentalPayment();
            case 5:
                return car.getAvailable();
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
            car = new Car();
            add(car);
        }
        else
            car = cars.get(rowIndex);
        switch (columnIndex) {
            case 1:
            {
                if (car.getID() != null)
                    updatedCars.add(car);
                car.setModel((String) aValue);
                break;
            }
            case 2:
            {
                if (car.getID() != null)
                    updatedCars.add(car);
                car.setColor((String) aValue);
                break;
            }
            case 3:
            {
                if (car.getID() != null)
                    updatedCars.add(car);
                car.setLicensePlate((String) aValue);
                break;
            }
            case 4:
            {
                if (car.getID() != null)
                    updatedCars.add(car);
                car.setRentalPayment((Double) aValue);
                break;
            }
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return ((columnIndex > 0) && (columnIndex < 5));
    }
    private static Comparator<Car> carByIDComparator = new Comparator<Car>() {

        @Override
        public int compare(Car car1, Car car2) {
            return Long.valueOf(car1.getID()).compareTo(Long.valueOf(car2.getID()));
        }
    };
    
    public void add(Car car)
    {
        car.setStatus(true);
        cars.add(car);
        fireTableRowsInserted((cars.size() - 1), cars.size());
    }

    public void remove(Car car)
    {
        if (car != null && car.getID() != null) {
            deletedCars.add(car);
            updatedCars.remove(car);
        }
    }
}
