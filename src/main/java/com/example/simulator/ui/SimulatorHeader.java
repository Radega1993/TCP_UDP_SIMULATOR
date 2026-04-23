package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SimulatorHeader extends VBox {
    public SimulatorHeader() {
        Label eyebrow = new Label("Simulador Educativo");
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #54708b;");

        Label title = new Label("Simulador gráfico de TCP y UDP");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f1f33;");

        Label subtitle = new Label("Observa handshake, transmisión, ACK, pérdidas y retransmisiones con una vista clara para clase.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #526273;");
        subtitle.setWrapText(true);

        setSpacing(6);
        setPadding(new Insets(4, 4, 18, 4));
        getChildren().addAll(eyebrow, title, subtitle);
    }
}
