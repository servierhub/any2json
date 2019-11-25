package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.base.header.AbstractIntelliHeader;
import com.github.romualdrousseau.shuju.math.Vector;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;

public class TaggedHeader extends AbstractIntelliHeader {

    public TaggedHeader(final AbstractTable table, final AbstractCell cell) {
        super(table, cell);
    }

    private TaggedHeader(final TaggedHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v1);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public AbstractCell getCellForRow(final AbstractRow row) {
        return row.getCellAt(this.getColumnIndex());
    }

    @Override
    public AbstractHeader clone() {
        return new TaggedHeader(this);
    }

    @Override
    protected Vector buildEntityVector() {
        final Vector result = new Vector(this.getTable().getClassifier().getEntityList().getVectorSize());

        int n = 0;
        for (int i = 0; i < Math.min(this.getTable().getNumberOfRows(),
                this.getTable().getClassifier().getSampleCount()); i++) {
            final AbstractRow row = this.getTable().getRowAt(i);
            if (row == null) {
                continue;
            }

            final AbstractCell cell = row.getCellAt(this.getColumnIndex());
            if (cell.hasValue() && !cell.getEntityVector().isNull()) {
                result.add(cell.getEntityVector());
                n++;
            }
        }

        if (n > 0) {
            result.cond(DocumentFactory.DEFAULT_ENTITY_PROBABILITY * ((float) n), 0.0f, 1.0f);
        }

        return result;
    }

    private String name;
}
