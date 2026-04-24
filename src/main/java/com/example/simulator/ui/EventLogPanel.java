package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class EventLogPanel extends DashboardCard {
    private final TextArea logArea = new TextArea();

    public EventLogPanel() {
        super("REGISTRO", "Registro de eventos", "Secuencia temporal de lo que ocurre durante la simulación.");
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(9);
        logArea.setStyle(UiTheme.MUTED_TEXT_SURFACE);

        VBox wrapper = new VBox(logArea);
        wrapper.setPadding(new Insets(4));
        wrapper.setStyle(UiTheme.PANEL_INSET);

        getContentBox().getChildren().add(wrapper);
        setMaxHeight(340);
    }

    public void setTitle(String title, String subtitle) {
        super.setTitle(title, subtitle);
    }

    public TextArea getLogArea() {
        return logArea;
    }

    public void setPreferredHeight(double height) {
        setPrefHeight(height);
        setMinHeight(height);
        setMaxHeight(height);
    }
}
