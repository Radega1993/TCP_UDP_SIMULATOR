package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class TcpIpLayersPanel extends VBox {
    public TcpIpLayersPanel() {
        setSpacing(16);
    }

    public void setLearningLevel(LearningLevel level) {
        DashboardCard shell = new DashboardCard("MODELO PRÁCTICO", "Modelo TCP/IP",
                "Cuatro capas para entender la arquitectura real de Internet de forma clara y visual.");
        VBox stack = new VBox(12,
                layer("[]", "Aplicación", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION,
                        "Servicios usados por las aplicaciones.",
                        level == LearningLevel.BASIC ? List.of("HTTP", "DNS") : List.of("HTTP", "DNS", "SMTP", "TLS")),
                layer("<>", "Transporte", UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT,
                        "Comunicación extremo a extremo con TCP o UDP.",
                        level == LearningLevel.BASIC ? List.of("TCP", "UDP") : List.of("TCP", "UDP", "Puertos", "Control de flujo")),
                layer("( )", "Internet", UiTheme.LAYER_BG_NETWORK, UiTheme.LAYER_BORDER_NETWORK,
                        "Direccionamiento lógico y encaminamiento.",
                        level == LearningLevel.BASIC ? List.of("IP", "ICMP") : List.of("IP", "ICMP", "TTL", "Rutas")),
                layer("=/=", "Acceso a la red", UiTheme.LAYER_BG_LINK, UiTheme.LAYER_BORDER_LINK,
                        "Entrega local, tramas y acceso al medio.",
                        level == LearningLevel.BASIC ? List.of("Ethernet", "Wi-Fi") : List.of("Ethernet", "Wi-Fi", "ARP", "MAC"))
        );
        shell.setContent(stack);
        shell.setMaxWidth(Double.MAX_VALUE);
        getChildren().setAll(shell);
    }

    private VBox layer(String iconText, String titleText, String fill, String border, String description, List<String> chips) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 1.5;");

        Label icon = new Label(iconText);
        icon.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + border + ";");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #102033;");

        Label body = new Label(description);
        body.setWrapText(true);
        body.setStyle(UiTheme.BODY);

        VBox chipsBox = new VBox(6);
        for (String chip : chips) {
            Label label = new Label(chip);
            label.setStyle("-fx-background-color: rgba(255,255,255,0.75);"
                    + "-fx-background-radius: 999; -fx-border-radius: 999;"
                    + "-fx-border-color: rgba(255,255,255,0.9);"
                    + "-fx-padding: 6 10 6 10; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #17324a;");
            chipsBox.getChildren().add(label);
        }

        card.getChildren().addAll(icon, title, body, chipsBox);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }
}
