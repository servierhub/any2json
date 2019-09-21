package com.github.romualdrousseau.any2json;

import java.util.ArrayList;
import java.util.HashMap;

public class IntelliTable implements ITable {
    public IntelliTable(Sheet sheet, Iterable<ITable> tables, ITagClassifier classifier, String[] requiredTagList) {
        for (ITable table : tables) {
            if (table.isMetaTable()) {
                this.metaTables.add(table);
            } else {
                this.dataTables.add(table);
            }
        }

        for (ITable table : this.dataTables) {
            for (IRow row : table.rows()) {
                if (row != null) {
                    this.rowsAsList.add(row);
                }
            }
        }

        // move invalid table to metaTable
        // sort by read direction

        updateHeaderTags(classifier);
    }

    public int getGroupId() {
        return 0;
    }

    public boolean isMetaTable() {
        return false;
    }

    public int getNumberOfMetaTables() {
        return 0;
    }

    public ITable getMetaTableAt(int tableIndex) {
        return null;
    }

    public Iterable<ITable> metatables() {
        return null;
    }

    public int getNumberOfMetas() {
        return 0;
    }

    public IRow getMetaRowAt(int rowIndex) {
        return null;
    }

    public boolean hasHeaders() {
        return true;
    }

    public void clearHeaders() {
        this.headersAsList.clear();
        this.headersAsMap.clear();
    }

    public int getNumberOfHeaders() {
        return this.headersAsList.size();
    }

    public IHeader getHeaderAt(int index) {
        return this.headersAsList.get(index);
    }

    public IHeader getHeaderByTag(String tagName) {
        return null;
    }

    public Iterable<IHeader> headers() {
        return this.headersAsList;
    }

    public int getNumberOfColumns() {
        return this.headersAsList.size();
    }

    public int getNumberOfRows() {
        return this.rowsAsList.size();
    }

    public IRow getRowAt(int rowIndex) {
        return this.rowsAsList.get(rowIndex);
    }

    public Iterable<IRow> rows() {
        return new RowIterable(this);
    }

    public void resetHeaderTags() {
        for (ITable table : this.metaTables) {
            table.resetHeaderTags();
        }

        for (ITable table : this.dataTables) {
            table.resetHeaderTags();

            for (ITable metaTable : table.metatables()) {
                metaTable.resetHeaderTags();
            }
        }
    }

    public void updateHeaderTags(ITagClassifier classifier) {
        for (ITable table : this.metaTables) {
            table.updateHeaderTags(classifier);
        }

        for (ITable table : this.dataTables) {
            table.updateHeaderTags(classifier);

            for (ITable metaTable : table.metatables()) {
                metaTable.updateHeaderTags(classifier);
            }
        }

        for (ITable table : this.metaTables) {
            for (IHeader header : table.headers()) {
                addHeader(header);
            }
        }

        for (ITable table : this.dataTables) {
            for (ITable metaTable : table.metatables()) {
                for (IHeader header : metaTable.headers()) {
                    addHeader(header);
                }
            }

            for (IHeader header : table.headers()) {
                addHeader(header);
            }
        }
    }

    protected ArrayList<IHeader> getBucket(IHeader header) {
        return this.headersAsMap.get(header.getCleanName());
    }

    protected void addHeader(IHeader header) {
        ArrayList<IHeader> bucket = this.headersAsMap.get(header.getCleanName());
        if (bucket == null) {
            bucket = new ArrayList<IHeader>();
            this.headersAsMap.put(header.getCleanName(), bucket);
            this.headersAsList.add(header);
        }
        bucket.add(header);
    }

    private ArrayList<ITable> dataTables = new ArrayList<ITable>();
    private ArrayList<ITable> metaTables = new ArrayList<ITable>();
    private HashMap<String, ArrayList<IHeader>> headersAsMap = new HashMap<String, ArrayList<IHeader>>();
    private ArrayList<IHeader> headersAsList = new ArrayList<IHeader>();
    private ArrayList<IRow> rowsAsList = new ArrayList<IRow>();
}
