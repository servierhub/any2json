package com.github.romualdrousseau.any2json.document.excel;

import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.TableRow;
import com.github.romualdrousseau.shuju.util.StringUtility;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelRow extends TableRow
{
	public ExcelRow(ExcelTable table, org.apache.poi.ss.usermodel.Row row) {
        this.table = table;
        this.row = row;
	}

	public int getNumberOfCells() {
		return this.table.lastColumn - this.table.firstColumn + 1;
    }

    public int getNumberOfMergedCellsAt(int i) {
        Cell cell = this.row.getCell(this.table.firstColumn + i);
		if(cell == null) {
			return 1;
        }

		int numberOfCells = 1;
		for(int j = 0; j < this.table.sheet.getNumMergedRegions(); j++) {
			CellRangeAddress region = this.table.sheet.getMergedRegion(j);
			if(region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
				numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
			}
        }

		return numberOfCells;
    }

	public String getCellValue(TableHeader header) {
		if(header == null) {
			throw new IllegalArgumentException();
		}

		String result = getInternalCellValueAt(header.getColumnIndex());
		if(result == null) {
			result = "";
		}

		for(int i = 1; i < header.getNumberOfCells(); i++) {
			String s = getInternalCellValueAt(header.getColumnIndex() + i);
			if(s != null) {
				result += s;
			}
		}

		return result;
    }

    public String getCellValueAt(int i) {
		if(i < 0 || i >= getNumberOfCells()) {
			throw new ArrayIndexOutOfBoundsException(i);
        }

        return getInternalCellValueAt(i);
    }

	private String getInternalCellValueAt(int i) {
		Cell cell = this.row.getCell(this.table.firstColumn + i);
		if(cell == null) {
			return null;
        }

		int type = this.table.evaluator.evaluateInCell(cell).getCellType();

		//int type = cell.getCellType();
		//if(type == Cell.CELL_TYPE_FORMULA) {
		//	type = evaluator.evaluateInCell(cell).getCellType();
		//}

		String value = this.table.formatter.formatCellValue(cell);

		// TRICKY: Get hidden decimals in case of a rounded numeric value
		if(type == Cell.CELL_TYPE_NUMERIC && value.matches("-?\\d+")) {
			double d = cell.getNumericCellValue();
			value = (Math.floor(d) == d) ? value : String.valueOf(d);
		}
		else if(type == Cell.CELL_TYPE_ERROR) {
			throw new UnsupportedOperationException("Unexceptected Cell Error at [" + row.getRowNum() + ";" + (this.table.firstColumn + i) + "]");
		}

		return StringUtility.cleanToken(value);
    }

    private ExcelTable table;
	private org.apache.poi.ss.usermodel.Row row;
}
