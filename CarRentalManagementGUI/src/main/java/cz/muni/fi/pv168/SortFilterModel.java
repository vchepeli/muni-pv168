package cz.muni.fi.pv168;

import java.util.Arrays;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

class SortFilterModel extends AbstractTableModel {

    public SortFilterModel(TableModel m) {
        model = m;
        rows = new Row[model.getRowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new Row();
            rows[i].index = i;
        }
    }

    public void sort(int c) {
        sortColumn = c;
        Arrays.sort(rows);
        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int r, int c) {
        return model.getValueAt(rows[r].index, c);
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return model.isCellEditable(rows[r].index, c);
    }

    @Override
    public void setValueAt(Object aValue, int r, int c) {
        model.setValueAt(aValue, rows[r].index, c);
    }

    @Override
    public int getRowCount() {
        return model.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return model.getColumnCount();
    }

    @Override
    public String getColumnName(int c) {
        return model.getColumnName(c);
    }

    @Override
    public Class getColumnClass(int c) {
        return model.getColumnClass(c);
    }

    private class Row implements Comparable {

        public int index;

        @Override
        public int compareTo(Object other) {
            Row otherRow = (Row) other;
            Object a = model.getValueAt(index, sortColumn);
            Object b = model.getValueAt(otherRow.index, sortColumn);
            if (a instanceof Comparable) {
                return ((Comparable) a).compareTo(b);
            } else {
                return a.toString().compareTo(b.toString());
            }

            //            return index - otherRow.index;
        }
    }
    private TableModel model;
    private int sortColumn;
    private Row[] rows;
}
