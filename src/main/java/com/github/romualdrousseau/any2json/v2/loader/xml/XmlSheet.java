package com.github.romualdrousseau.any2json.v2.loader.xml;

import com.github.romualdrousseau.shuju.util.StringUtility;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.v2.util.RowTranslatable;
import com.github.romualdrousseau.any2json.v2.util.RowTranslator;

class XmlSheet extends IntelliSheet implements RowTranslatable {

    public XmlSheet(Worksheet sheet) {
        this.sheet = sheet;
        this.rowTranslator = new RowTranslator(this, getLastRowNum() + 1);
    }

    @Override
    public String getName() {
        return this.sheet.getName();
    }

    @Override
    public int getLastColumnNum(int colIndex, int rowIndex) {
        Row row = this.getRowAt(rowIndex);
        if (row == null) {
            return 0;
        }

        int colNum = colIndex;
        Cell cell = row.getCellAt(colNum + 1);
        while (cell.hasData()) {
        // while (cell.hasData() && !StringUtility.isEmpty(cell.getData$())) {
            cell = row.getCellAt((++colNum) + 1);
        }

        return colNum - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getRows().size() - 1;
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
		if(cell == null) {
			return null;
        }
        return StringUtility.cleanToken(cell.getData$());
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
		if(cell == null) {
			return 1;
        }
		return cell.getMergeAcross() + 1;
    }

    @Override
    public boolean isTranslatableRow(int colIndex, int rowIndex) {
        double height = this.sheet.getRowAt(rowIndex + 1).getHeight();
        return (height < DocumentFactory.SEPARATOR_ROW_THRESHOLD);
    }

    private Row getRowAt(int rowIndex) {
        final int translatedRow = this.rowTranslator.rebase(0, rowIndex);
        if(translatedRow == -1) {
            return null;
        }
        Row row = this.sheet.getRowAt(translatedRow + 1);
        if (row == null) {
            return null;
        }
        return row;
    }

    private Cell getCellAt(int colIndex, int rowIndex) {
        final int translatedRow = this.rowTranslator.rebase(0, rowIndex);
        if(translatedRow == -1) {
            return null;
        }
        Cell cell = this.sheet.getCellAt(translatedRow + 1, colIndex + 1);
        if(!cell.hasData()) {
			return null;
        }
        return cell;
    }

    private Worksheet sheet;
    private RowTranslator rowTranslator;
}
