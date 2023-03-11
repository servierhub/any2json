package com.github.romualdrousseau.any2json.intelli.parser;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseHeader;
import com.github.romualdrousseau.any2json.base.RowGroup;
import com.github.romualdrousseau.any2json.intelli.header.MetaTableHeader;
import com.github.romualdrousseau.any2json.intelli.header.PivotKeyHeader;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.header.DataTableHeader;

public class DataTableGroupSubFooterParser extends DataTableParser {

    public static final int TABLE_META = 1;
    public static final int TABLE_HEADER = 2;
    public static final int TABLE_SUB_HEADER = 4;
    public static final int TABLE_DATA = 5;
    public static final int TABLE_SUB_FOOTER = 6;
    public static final int TABLE_FOOTER = 7;

    public DataTableGroupSubFooterParser(final DataTable dataTable) {
        this.dataTable = dataTable;
        this.splitRows = new ArrayList<Integer>();
        this.ignoreRows = new ArrayList<Integer>();

        this.firstRowCell = null;
        this.currRowGroup = null;
        this.firstRowHeader = false;
        this.firstRowGroupProcessed = false;
        this.footerProcessed = false;
    }

    @Override
    public void processSymbolFunc(final BaseCell cell) {
        final String symbol = cell.getSymbol();

        if (this.getColumn() == 0) {
            this.firstRowCell = null;
        }
        if (this.firstRowCell == null && cell.hasValue()) {
            this.firstRowCell = cell;
        }

        if (!this.footerProcessed) { 
            switch (this.getGroup()) {
            case TABLE_META:   
                this.processMeta(cell, symbol);
                break;

            case TABLE_HEADER:
                this.processHeader(cell, symbol);
                break;

            case TABLE_SUB_HEADER:
                this.processSubHeader(cell, symbol);
                break;

            case TABLE_DATA:
                this.processData(cell, symbol);
                break;

            case TABLE_SUB_FOOTER:
                this.processSubFooter(cell, symbol);
                break;

            case TABLE_FOOTER:
                this.processFooter(cell, symbol);
                break;
            }
        } else if (this.getGroup() == TABLE_FOOTER) {
            this.processSplit(cell, symbol);
        }
    }

    @Override
    public List<Integer> getSplitRows() {
        return this.splitRows;
    }

    @Override
    public List<Integer> getIgnoreRows() {
        return this.ignoreRows;
    }

    private void processMeta(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            this.dataTable.setFirstRowOffset(this.getRow() + 1);
        } else if (cell.hasValue()) {
            this.dataTable.addHeader(new MetaTableHeader(this.dataTable, cell));
        }
    }

    private void processHeader(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            this.dataTable.setFirstRowOffset(this.getRow() + 1);
            if (!this.firstRowHeader) {
                this.dataTable.setHeaderRowOffset(this.getRow());
                this.firstRowHeader = true;
            }
        } else if (!this.firstRowHeader) {
            if (symbol.equals("e") && cell.isPivotHeader()) {
                final PivotKeyHeader foundPivot = this.dataTable.findFirstPivotHeader();
                if (foundPivot == null) {
                    this.dataTable.addHeader(new PivotKeyHeader(this.dataTable, cell));
                } else {
                    foundPivot.addEntry(cell);
                }
            } else {
                if (cell.hasValue()) {
                    this.dataTable.addHeader(new DataTableHeader(this.dataTable, cell));
                }
            }
        } else {
            final BaseHeader header = this.dataTable.getHeaderAt(cell.getColumnIndex() - this.dataTable.getFirstColumn());
            if (cell.hasValue() && !header.getName().contains(cell.getValue())) {
                if (header instanceof PivotKeyHeader) {
                    final PivotKeyHeader pivotHeader = (PivotKeyHeader) header;
                    pivotHeader.updateValueName(pivotHeader.getValueName() + " " + cell.getValue());
                } else if (header instanceof DataTableHeader) {
                    final DataTableHeader dataHeader = (DataTableHeader) header;
                    dataHeader.updateName(dataHeader.getName() + " " + cell.getValue());
                }
            }
        }
    }

    private void processSubHeader(final BaseCell cell, final String symbol) {
        if (this.getRow() < (this.dataTable.getLastRow() - this.dataTable.getFirstRow())) {
            if (symbol.equals("$")) {
                this.ignoreRows.add(this.getRow() - this.dataTable.getFirstRowOffset());
                this.dataTable.getRowAt(this.getRow() - this.dataTable.getFirstRowOffset()).setIgnored(true);
            }
        } else {
            this.processFooter(cell, symbol);
        }
    }

    private void processData(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            if(this.currRowGroup == null) {
                this.currRowGroup = new RowGroup(this.getRow() - this.dataTable.getFirstRowOffset());
            }
            this.currRowGroup.incNumberOfRows();
        }
    }

    private void processSubFooter(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            if (this.firstRowCell != null) {
                this.currRowGroup.setCell(this.firstRowCell);
                this.dataTable.addRowGroup(this.currRowGroup);
            }

            if (!this.firstRowGroupProcessed) {
                MetaTableHeader meta = this.dataTable.findFirstMetaTableHeader();
                if (meta == null) {
                    meta = new MetaTableHeader(this.dataTable,
                            new BaseCell("#GROUP?", 0, 1, this.firstRowCell.getRawValue(), this.dataTable.getSheet().getClassifierFactory()));
                    this.dataTable.addHeader(meta);
                }
                meta.assignRowGroup(this.currRowGroup);
                this.firstRowGroupProcessed = true;
            }

            this.currRowGroup = null;
            this.ignoreRows.add(this.getRow() - this.dataTable.getFirstRowOffset());
            this.dataTable.getRowAt(this.getRow() - this.dataTable.getFirstRowOffset()).setIgnored(true);
        }
    }

    private void processFooter(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            final int n = this.dataTable.getLastRow() - this.dataTable.getFirstRow();
            this.dataTable.setLastRowOffset(this.getRow() - n - 1);
            this.splitRows.add(this.getRow() + 1);
            this.footerProcessed = true;
        }
    }

    private void processSplit(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            this.splitRows.add(this.getRow() + 1);
        }
    }

    private final DataTable dataTable;
    private final ArrayList<Integer> splitRows;
    private final ArrayList<Integer> ignoreRows;

    private BaseCell firstRowCell;
    private RowGroup currRowGroup;
    private boolean firstRowHeader;
    private boolean firstRowGroupProcessed;
    private boolean footerProcessed;
}
