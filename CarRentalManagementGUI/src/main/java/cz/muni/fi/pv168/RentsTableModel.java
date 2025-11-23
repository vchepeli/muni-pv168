package cz.muni.fi.pv168;

import java.sql.Date;
import java.util.*;
import javax.swing.table.AbstractTableModel;

public class RentsTableModel extends AbstractTableModel {

    private List<Rent> rents = new ArrayList<>();
    private ResourceBundle localization;
    private Set<Rent> deletedRents;

    public List<Rent> getRents()
    {
        return Collections.unmodifiableList(rents);
    }

    public Set<Rent> getDeletedRents()
    {
        return Collections.unmodifiableSet(deletedRents);
    }

    public RentsTableModel(ResourceBundle localization)
    {
        this.localization = localization;
        deletedRents = new HashSet<>();
    }
    
    public boolean hasNewRents()
    {
        for (Rent r : rents)
            if (r.ID() == null)
                return true;
        
        return false;
    }
    
    public void updateRents(List<Rent> newInventories) {
        if (null == newInventories) {
            return;
        }
        int firstRow = 0;
        int lastRow = rents.size() - 1;
        rents.clear();
        fireTableRowsDeleted(firstRow, lastRow < 0 ? 0 : lastRow);
        rents.addAll(newInventories);
        Collections.sort(rents, rentByIDComparator);
        lastRow = rents.size() - 1;
        fireTableRowsInserted(firstRow, lastRow < 0 ? 0 : lastRow);
    }

    @Override
    public int getRowCount() {
        return rents.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2:
                return Long.class;
            case 3:
            case 4:
                return Date.class;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "ID";
            case 1:
                return (localization.getString("car") + " ID");
            case 2:
                return (localization.getString("customer") + " ID");
            case 3:
                return localization.getString("rent_date");
            case 4:
                return localization.getString("due_date");
            default:
                throw new IllegalArgumentException("Column");

        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= rents.size()) {
            throw new IllegalArgumentException("Row Index Out Of Bounds.");
        }
        Rent rent = rents.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rent.ID();
            case 1:
                return rent.carID();
            case 2:
                return rent.customerID();
            case 3:
                return rent.rentDate();
            case 4:
                return rent.dueDate();
            default:
                throw new IllegalArgumentException("Column Index");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= rents.size()) {
            throw new IllegalArgumentException("Row Index Out Of Bounds.");
        }
        Rent rent = rents.get(rowIndex);
        Rent updatedRent = rent;
        switch (columnIndex) {
            case 1:
                updatedRent = new Rent(rent.ID(), rent.rentDate(), rent.dueDate(), (Long) aValue, rent.customerID());
                break;
            case 2:
                updatedRent = new Rent(rent.ID(), rent.rentDate(), rent.dueDate(), rent.carID(), (Long) aValue);
                break;
            case 3:
                updatedRent = new Rent(rent.ID(), (Date) aValue, rent.dueDate(), rent.carID(), rent.customerID());
                break;
            case 4:
                updatedRent = new Rent(rent.ID(), rent.rentDate(), (Date) aValue, rent.carID(), rent.customerID());
                break;
            default:
                throw new IllegalArgumentException("Column Index");
        }
        rents.set(rowIndex, updatedRent);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    private static Comparator<Rent> rentByIDComparator = new Comparator<Rent>() {

        @Override
        public int compare(Rent rent1, Rent rent2) {
            return Long.valueOf(rent1.ID()).compareTo(Long.valueOf(rent2.ID()));
        }
    };
    
    public void add(Rent rent)
    {
        rents.add(rent);
        fireTableRowsInserted((rents.size() - 1), rents.size());
    }

    public void remove(Rent rent)
    {
        if (rent != null && rent.ID() != null) {
            deletedRents.add(rent);
        }
    }

    public void clearDeletedRents() {
        deletedRents.clear();
    }

    public void removeFromDeletedRents(Rent rent) {
        deletedRents.remove(rent);
    }
}
