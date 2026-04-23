package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatePanel extends DashboardCard {
    private final Label clientStateLabel = stateChip("Cliente: CLOSED");
    private final Label serverStateLabel = stateChip("Servidor: CLOSED");
    private final Label statusLabel = new Label("Listo para iniciar");

    public StatePanel() {
        super("Estados", "Resumen rápido del ciclo de la conexión.");

        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34475a; -fx-padding: 4 2 2 2;");

        VBox content = new VBox(10, clientStateLabel, serverStateLabel, statusLabel);
        content.setPadding(new Insets(8));
        content.setStyle(UiTheme.PANEL_INSET_TINT);
        getContentBox().getChildren().add(content);
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

    private Label stateChip(String text) {
        Label label = new Label(text);
        label.setStyle(UiTheme.CHIP);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
