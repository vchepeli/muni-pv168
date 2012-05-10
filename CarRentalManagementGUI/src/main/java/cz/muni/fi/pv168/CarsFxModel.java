package cz.muni.fi.pv168;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class CarsFxModel {

    private final ObservableList<Car> cars = FXCollections.observableArrayList();
    private final Set<Car> addedCars = new HashSet<>();
    private final Set<Car> updatedCars = new HashSet<>();
    private final Set<Car> deletedCars = new HashSet<>();

    public ObservableList<Car> getCars() {
        return cars;
    }

    public Set<Car> getAddedCars() {
        return Collections.unmodifiableSet(addedCars);
    }

    public Set<Car> getUpdatedCars() {
        return Collections.unmodifiableSet(updatedCars);
    }

    public Set<Car> getDeletedCars() {
        return Collections.unmodifiableSet(deletedCars);
    }

    public void mergeCars(List<Car> dbCars) {
        // 1. Keep offline (added) cars
        List<Car> offlineCars = new ArrayList<>(addedCars);

        // 2. Reset the list with fresh data from DB
        cars.setAll(dbCars);

        // 3. Add back the offline cars
        cars.addAll(offlineCars);
        
        // Reset tracking sets as we are syncing with DB state (except for the added ones we just re-added)
        updatedCars.clear();
        deletedCars.clear();
    }

    public void addCar(Car car) {
        if (car != null) {
            cars.add(car);
            addedCars.add(car);
        }
    }

    public void markForDeletion(Car car) {
        if (car != null) {
            // If it was just added locally, simply remove it from added set
            if (addedCars.contains(car)) {
                addedCars.remove(car);
            } else {
                // If it's persistent, mark for deletion
                deletedCars.add(car);
                updatedCars.remove(car);
            }
        }
    }
    
    public void unmarkFromDeletion(Car car) {
        deletedCars.remove(car);
    }

    public void removeCarFromList(Car car) {
        if (car != null) {
            cars.remove(car);
            addedCars.remove(car);
            updatedCars.remove(car);
            deletedCars.remove(car);
        }
    }
    
    /**
     * Updates a car in the list.
     * @param oldCar The car instance currently in the list.
     * @param newCar The new car instance with updated values.
     */
    public void updateCar(Car oldCar, Car newCar) {
        if (oldCar != null && newCar != null) {
            int index = cars.indexOf(oldCar);
            if (index >= 0) {
                cars.set(index, newCar);
                
                if (addedCars.contains(oldCar)) {
                    addedCars.remove(oldCar);
                    addedCars.add(newCar);
                } else {
                    updatedCars.remove(oldCar);
                    updatedCars.add(newCar);
                }
            }
        }
    }

    public void carResolved(Car car) {
        addedCars.remove(car);
        updatedCars.remove(car);
        deletedCars.remove(car);
    }
    
    public void removeFromDeleted(Car car) {
        deletedCars.remove(car);
    }
}
