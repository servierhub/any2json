package com.github.romualdrousseau.any2json.classifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ILayoutClassifier;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.types.Tensor;
import com.github.romualdrousseau.shuju.preprocessing.Text;
import com.github.romualdrousseau.shuju.preprocessing.comparer.RegexComparer;

public class LayexClassifier implements ILayoutClassifier
{

    private final List<String> entities;
    private final Map<String, String> patterns;
    private final List<String> filters;
    private final List<String> pivotEntityList;
    private final List<String> metaLayexes;
    private final List<String> dataLayexes;

    private final RegexComparer comparer;

    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;
    private String recipe;

    public LayexClassifier(final List<String> entities, final Map<String, String> patterns, final List<String> filters,
            final List<String> pivotEntityList, final List<String> metaLayexes, final List<String> dataLayexes) {
        this.entities = entities;
        this.patterns = patterns;
        this.filters = filters;
        this.pivotEntityList = pivotEntityList;
        this.metaLayexes = metaLayexes;
        this.dataLayexes = dataLayexes;

        this.comparer = new RegexComparer(this.patterns);

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.recipe = null;
    }

    public LayexClassifier(final JSONObject json) {
        this.entities = JSON.<String>streamOf(json.getArray("entities")).toList();
        this.patterns = JSON.<JSONObject>streamOf(json.getArray("patterns"))
                .collect(Collectors.toMap(x -> x.getString("key"), x -> x.getString("value")));
        this.filters = JSON.<String>streamOf(json.getArray("filters")).toList();
        this.pivotEntityList = JSON.<String>streamOf(json.getArray("pivotEntityList")).toList();
        this.metaLayexes = JSON.<String>streamOf(json.getArray("metaLayexes")).toList();
        this.dataLayexes = JSON.<String>streamOf(json.getArray("dataLayexes")).toList();

        this.comparer = new RegexComparer(this.patterns);

        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.recipe = null;
    }

    public List<String> getFilters() {
        return this.filters;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public int getSampleCount() {
        return DocumentFactory.DEFAULT_SAMPLE_COUNT;
    }

    @Override
    public List<String> getEntityList() {
        return this.entities;
    }

    @Override
    public List<String> getPivotEntityList() {
        return this.pivotEntityList;
    }

    @Override
    public List<TableMatcher> getMetaMatcherList() {
        return this.metaMatchers;
    }

    @Override
    public void setMetaMatcherList(final List<TableMatcher> matchers) {
        this.metaMatchers = matchers;
    }

    @Override
    public List<TableMatcher> getDataMatcherList() {
        return this.dataMatchers;
    }

    @Override
    public void setDataMatcherList(final List<TableMatcher> matchers) {
        this.dataMatchers = matchers;
    }

    @Override
    public String getRecipe() {
        return this.recipe;
    }

    @Override
    public void setRecipe(final String recipe) {
        this.recipe = recipe;
    }

    @Override
    public String toEntityName(final String value) {
        return this.comparer.anonymize(value);
    }

    @Override
    public Optional<String> toEntityValue(final String value) {
        return this.comparer.find(value);
    }

    @Override
    public Tensor toEntityVector(final String value) {
        return Tensor.of(Text.to_categorical(value, this.entities, this.comparer).stream()
                .mapToDouble(x -> (double) x).toArray());
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject result = JSON.newObject();
        result.setArray("entities", JSON.arrayOf(this.entities));
        result.setArray("patterns", JSON.arrayOf(this.patterns));
        result.setArray("filters", JSON.arrayOf(this.filters));
        result.setArray("pivotEntityList", JSON.arrayOf(this.pivotEntityList));
        result.setArray("metaLayexes", JSON.arrayOf(this.metaLayexes));
        result.setArray("dataLayexes", JSON.arrayOf(this.dataLayexes));
        return result;
    }
}
