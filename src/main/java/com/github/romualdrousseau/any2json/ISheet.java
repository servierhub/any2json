package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

public interface ISheet
{
    public String getName();

    public ISearchBitmap getSearchBitmap(int headerColumns, int headerRows);

	public ITable getTable();

    public ITable findTable(int headerColumns, int headerRows);

    public Iterable<ITable> findTables(int headerColumns, int headerRows);

    public ITable findTableWithItelliTag(ITagClassifier classifier);

    public ITable findTableWithItelliTag(ITagClassifier classifier, String[] requiredTagList);
}

