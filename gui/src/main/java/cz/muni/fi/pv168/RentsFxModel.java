package cz.muni.fi.pv168;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class RentsFxModel {

    private final ObservableList<Rent> rents = FXCollections.observableArrayList();
    private final Set<Rent> addedRents = new HashSet<>();
    private final Set<Rent> updatedRents = new HashSet<>();
    private final Set<Rent> deletedRents = new HashSet<>();

    public ObservableList<Rent> getRents() {
        return rents;
    }

    public Set<Rent> getAddedRents() {
        return Collections.unmodifiableSet(addedRents);
    }

    public Set<Rent> getUpdatedRents() {
        return Collections.unmodifiableSet(updatedRents);
    }

    public Set<Rent> getDeletedRents() {
        return Collections.unmodifiableSet(deletedRents);
    }

    public void mergeRents(List<Rent> dbRents) {
        List<Rent> offlineRents = new ArrayList<>(addedRents);

        rents.setAll(dbRents);
        rents.addAll(offlineRents);
        
        updatedRents.clear();
        deletedRents.clear();
    }

    public void addRent(Rent rent) {
        if (rent != null) {
            rents.add(rent);
            addedRents.add(rent);
        }
    }

    public void updateRent(Rent oldRent, Rent newRent) {
        if (oldRent != null && newRent != null) {
            int index = rents.indexOf(oldRent);
            if (index >= 0) {
                rents.set(index, newRent);
                
                if (addedRents.contains(oldRent)) {
                    addedRents.remove(oldRent);
                    addedRents.add(newRent);
                } else {
                    updatedRents.remove(oldRent);
                    updatedRents.add(newRent);
                }
            }
        }
    }

    public void markForDeletion(Rent rent) {
        if (rent != null) {
            if (addedRents.contains(rent)) {
                addedRents.remove(rent);
            } else {
                deletedRents.add(rent);
                updatedRents.remove(rent);
            }
        }
    }
    
    public void unmarkFromDeletion(Rent rent) {
        deletedRents.remove(rent);
    }

    public void removeRentFromList(Rent rent) {
        if (rent != null) {
            rents.remove(rent);
            addedRents.remove(rent);
            updatedRents.remove(rent);
            deletedRents.remove(rent);
        }
    }
    
    public void rentResolved(Rent rent) {
        addedRents.remove(rent);
        updatedRents.remove(rent);
        deletedRents.remove(rent);
    }
    
    public void removeFromDeleted(Rent rent) {
        deletedRents.remove(rent);
    }
}
