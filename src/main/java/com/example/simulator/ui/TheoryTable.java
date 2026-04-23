package com.example.simulator.ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class TheoryTable extends GridPane {
    public TheoryTable() {
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(4, 0, 0, 0));

        ColumnConstraints concept = new ColumnConstraints();
        concept.setPercentWidth(42);
        concept.setHgrow(Priority.ALWAYS);
        ColumnConstraints tcp = new ColumnConstraints();
        tcp.setPercentWidth(29);
        tcp.setHgrow(Priority.ALWAYS);
        ColumnConstraints udp = new ColumnConstraints();
        udp.setPercentWidth(29);
        udp.setHgrow(Priority.ALWAYS);
        getColumnConstraints().addAll(concept, tcp, udp);
    }

    public void addHeader(String concept, String tcp, String udp) {
        add(buildHeader(concept, HPos.LEFT), 0, 0);
        add(buildHeader(tcp, HPos.CENTER), 1, 0);
        add(buildHeader(udp, HPos.CENTER), 2, 0);
    }

    public void addRow(int row, String concept, String tcp, String udp) {
        add(buildCell(concept, true), 0, row);
        add(buildCell(tcp, false), 1, row);
        add(buildCell(udp, false), 2, row);
    }

    private Node buildHeader(String text, HPos alignment) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #122033; "
                + "-fx-background-color: #eef3fb; -fx-background-radius: 12; -fx-padding: 10 12 10 12;");
        setHalignment(label, alignment);
        return label;
    }

    private Node buildCell(String text, boolean concept) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle((concept ? UiTheme.FIELD_LABEL : UiTheme.BODY)
                + "-fx-background-color: #f8fbfe; -fx-background-radius: 12; -fx-border-radius: 12; "
                + "-fx-border-color: #e0e8f0; -fx-padding: 10 12 10 12;");
        return label;
    }
}
