package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.HeaderTag;
import com.github.romualdrousseau.shuju.DataRow;

public class SimpleHeader extends AbstractHeader {

    public SimpleHeader(final AbstractTable table, final BaseCell cell) {
        super(table, cell);
    }

    private SimpleHeader(final SimpleHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.getCell().getValue();
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public boolean hasTag() {
        return false;
    }

    @Override
    public HeaderTag getTag() {
        return null;
    }

    @Override
    public String getEntityString() {
        return null;
    }

    @Override
    public DataRow buildTrainingRow(final String tagValue, final Header[] conflicts, final boolean ensureWordsExists) {
        return null;
    }

    private String name;
}
