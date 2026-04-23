package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SimulatorHeader extends VBox {
    public SimulatorHeader() {
        Label eyebrow = new Label("SIMULADOR EDUCATIVO");
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #5f7c9c; -fx-letter-spacing: 1px;");

        Label title = new Label("Simulador visual de TCP y UDP");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #122033;");

        Label subtitle = new Label("Explora handshake, entrega, pérdidas y retransmisiones con una interfaz pensada para explicar redes en clase.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #627487;");

        Region accent = new Region();
        accent.setPrefSize(72, 4);
        accent.setMaxWidth(72);
        accent.setStyle("-fx-background-color: linear-gradient(to right, #2e6ef7, #60a5fa); -fx-background-radius: 999;");

        setSpacing(8);
        setPadding(new Insets(8, 4, 14, 4));
        setStyle("-fx-background-color: linear-gradient(to right, rgba(255,255,255,0.95), rgba(248,251,255,0.85));"
                + "-fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: #dde5ef; -fx-border-width: 1;");
        getChildren().addAll(eyebrow, title, subtitle, accent);
    }
}
