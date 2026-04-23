package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class StatePanel extends DashboardCard {
    private final Label clientStateLabel = stateChip("Cliente", "CLOSED");
    private final Label serverStateLabel = stateChip("Servidor", "CLOSED");
    private final Label statusLabel = new Label("Listo para iniciar");

    public StatePanel() {
        super("ESTADO", "Estados", "Lectura rápida del ciclo de conexión y del estado actual.");

        GridPane chips = new GridPane();
        chips.setHgap(10);
        chips.setVgap(10);
        chips.add(clientStateLabel, 0, 0);
        chips.add(serverStateLabel, 1, 0);

        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34475a;");

        VBox statusSurface = new VBox(8, chips, statusLabel);
        statusSurface.setPadding(new Insets(10));
        statusSurface.setStyle(UiTheme.PANEL_INSET_TINT);
        getContentBox().getChildren().add(statusSurface);
    }

    public Label getClientStateLabel() {
        return clientStateLabel;
    }

    public Label getServerStateLabel() {
        return serverStateLabel;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    private Label stateChip(String role, String state) {
        Label label = new Label(role + ": " + state);
        label.setStyle(UiTheme.CHIP);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
