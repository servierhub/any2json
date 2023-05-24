package com.github.romualdrousseau.any2json.intelli.parser;

import com.github.romualdrousseau.any2json.intelli.DataTable;

public class DataTableGroupSubFooterParserFactory implements DataTableParserFactory {

    private boolean disablePivot = false;
    
    @Override
    public void disablePivot() {
        this.disablePivot = true;
    }

    @Override
    public DataTableParser getInstance(DataTable dataTable) {
        return new DataTableGroupSubFooterParser(dataTable, this.disablePivot);
    }

    
}