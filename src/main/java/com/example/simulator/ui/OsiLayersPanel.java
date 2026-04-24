package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class OsiLayersPanel extends VBox {
    public OsiLayersPanel() {
        setSpacing(16);
    }

    public void setLearningLevel(LearningLevel level) {
        DashboardCard shell = new DashboardCard("MODELO TEÓRICO", "Modelo OSI",
                "Siete capas apiladas para estudiar la comunicación de red con máximo detalle conceptual.");
        VBox stack = new VBox(8,
                layer(7, "@", "Aplicación", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION, examples(level, "HTTP", "DNS", "FTP")),
                layer(6, "[]", "Presentación", UiTheme.LAYER_BG_PRESENTATION, UiTheme.LAYER_BORDER_PRESENTATION, examples(level, "TLS", "JPEG", "UTF-8")),
                layer(5, "()", "Sesión", UiTheme.LAYER_BG_SESSION, UiTheme.LAYER_BORDER_SESSION, examples(level, "RPC", "NetBIOS", "Diálogo")),
                layer(4, "<>", "Transporte", UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT, examples(level, "TCP", "UDP", "Puertos")),
                layer(3, "##", "Red", UiTheme.LAYER_BG_NETWORK, UiTheme.LAYER_BORDER_NETWORK, examples(level, "IP", "ICMP", "Rutas")),
                layer(2, "=/=", "Enlace de datos", UiTheme.LAYER_BG_LINK, UiTheme.LAYER_BORDER_LINK, examples(level, "Ethernet", "PPP", "MAC")),
                layer(1, "~~", "Física", UiTheme.LAYER_BG_PHYSICAL, UiTheme.LAYER_BORDER_PHYSICAL, examples(level, "Cable", "Fibra", "Radio"))
        );
        shell.setContent(stack);
        shell.setMaxWidth(Double.MAX_VALUE);
        getChildren().setAll(shell);
    }

    private VBox layer(int number, String iconText, String name, String fill, String border, List<String> chips) {
        VBox row = new VBox(8);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 1.5;");

        Label index = new Label(number + "  " + iconText + "  " + name);
        index.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #102033;");

        Label examples = new Label(String.join("   •   ", chips));
        examples.setWrapText(true);
        examples.setStyle("-fx-font-size: 11px; -fx-text-fill: #465a70;");

        row.getChildren().addAll(index, examples);
        return row;
    }

    private List<String> examples(LearningLevel level, String a, String b, String c) {
        return level == LearningLevel.BASIC ? List.of(a, b) : List.of(a, b, c);
    }
}
