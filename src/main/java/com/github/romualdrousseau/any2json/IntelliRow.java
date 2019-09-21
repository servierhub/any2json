package com.github.romualdrousseau.any2json;

public class IntelliRow implements IRow {
    public IntelliRow(IntelliTable table, IRow row) {
        this.table = table;
        this.row = row;
    }

    public ITable getTable() {
        return this.table;
    }

    public int getGroupId() {
        return 0;
    }

    public boolean isEmpty(double ratio) {
        return false;
    }

    public int getNumberOfCells() {
        return this.table.getNumberOfHeaders();
    }

    public int getNumberOfMergedCellsAt(int imdex) {
        return 1;
    }

    public TableCell getCellAt(int index) {
        IHeader header = this.table.getHeaderAt(index);
        return getCell(header, false);
    }

    public TableCell getCell(IHeader header) {
        return getCell(header, false);
    }

    public TableCell getCell(IHeader header, boolean mergeValues) {
        if (this.row == null) {
            return null;
        }

        for (IHeader current : this.table.getBucket(header)) {
            if (this.row.getTable() == current.getTable()) {
                return this.row.getCell(current, mergeValues);
            }
        }

        for (IHeader current : this.table.getBucket(header)) {
            if (current.getTable().getGroupId() == 0
                    || this.row.getTable().getMetaTableAt(this.row.getGroupId()) == current.getTable()) {
                return new TableCell(current).setValue(current.getName()); // do something
            }
        }

        return null;
    }

    public String getCellValueAt(int index) {
        IHeader header = this.table.getHeaderAt(index);
        return getCellValue(header, false);
    }

    public String getCellValue(IHeader header) {
        return getCellValue(header, false);
    }

    public String getCellValue(IHeader header, boolean mergeValues) {
        if (this.row == null) {
            return null;
        }

        for (IHeader current : this.table.getBucket(header)) {
            if (this.row.getTable() == current.getTable()) {
                return this.row.getCellValue(current, mergeValues);
            }
        }

        for (IHeader current : this.table.getBucket(header)) {
            if (current.getTable().getGroupId() == 0
                    || this.row.getTable().getMetaTableAt(this.row.getGroupId()) == current.getTable()) {
                return current.getName(); // do something
            }
        }

        return null;
    }

    private IntelliTable table;
    private IRow row;
}
