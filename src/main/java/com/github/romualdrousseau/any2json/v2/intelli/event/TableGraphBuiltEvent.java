package com.github.romualdrousseau.any2json.v2.intelli.event;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.SheetEvent;
import com.github.romualdrousseau.any2json.v2.intelli.DataTable;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class TableGraphBuiltEvent extends SheetEvent {

    public TableGraphBuiltEvent(Sheet source, TableGraph tableGraph) {
        super(source);
        this.tableGraph = tableGraph;
    }

    public TableGraph getTableGraph() {
        return this.tableGraph;
    }

    public void dumpTableGraph() {
        this.walkThroughTableGraph(this.tableGraph, 0, 0);
    }

    private int walkThroughTableGraph(TableGraph graph, int indent, int counter) {
        if (!graph.isRoot()) {
            StringBuffer out = new StringBuffer();

            for (int i = 0; i < indent; i++) {
                out.append("|- ");
            }

            for (Header header : graph.getTable().headers()) {
                out.append(header.getName()).append(" ");
            }

            if (graph.getTable() instanceof DataTable) {
                out.append("DATA(");
            } else {
                out.append("META(");
            }
            out.append(graph.getTable().getFirstColumn()).append(", ");
            out.append(graph.getTable().getFirstRow()).append(", ");
            out.append(graph.getTable().getLastColumn()).append(", ");
            out.append(graph.getTable().getLastRow()).append(", ");
            out.append(graph.getTable().getLastRow() - graph.getTable().getFirstRow() + 1).append(", ");
            out.append(graph.getTable().getNumberOfRows());
            out.append(")");

            if (graph.getTable() instanceof DataTable) {
                out.append(" (").append(counter + 1).append(")");
                counter++;
            }

            System.out.println(out.toString());
        }

        for (TableGraph child : graph.children()) {
            counter = walkThroughTableGraph(child, indent + 1, counter);
        }

        return counter;
    }

    private TableGraph tableGraph;
}