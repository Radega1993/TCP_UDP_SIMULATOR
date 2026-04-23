package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class EventLogPanel extends DashboardCard {
    private final TextArea logArea = new TextArea();

    public EventLogPanel() {
        super("Registro de eventos", "Secuencia temporal de lo que ocurre durante la simulación.");
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(14);
        logArea.setStyle(UiTheme.MUTED_TEXT_SURFACE);

        VBox wrapper = new VBox(logArea);
        wrapper.setPadding(new Insets(6));
        wrapper.setStyle(UiTheme.PANEL_INSET_TINT);

        getContentBox().getChildren().add(wrapper);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        DashboardCard.grow(wrapper);
    }

    public TextArea getLogArea() {
        return logArea;
    }
}
