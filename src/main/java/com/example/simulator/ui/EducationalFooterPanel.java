package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class EducationalFooterPanel extends HBox {
    private final Label payloadLabel = new Label("Datos que una capa recibe de la capa superior y encapsula.");
    private final Label tcpLabel = new Label("Contiene campos como puertos, número de secuencia, número de ACK y flags.");
    private final Label udpLabel = new Label("Cabecera más simple, con menos sobrecarga que TCP.");

    public EducationalFooterPanel() {
        setSpacing(14);
        getChildren().addAll(
                pill("( )", "Payload", payloadLabel, UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION),
                pill("<>", "Header TCP", tcpLabel, UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT),
                pill("[ ]", "Header UDP", udpLabel, "#efe9ff", "#8A63D2")
        );
    }

    public void update(LayersMode mode, LearningLevel level) {
        payloadLabel.setText(level == LearningLevel.BASIC
                ? "Datos que una capa recibe de la capa superior y encapsula."
                : "Información útil de la aplicación que viaja dentro del paquete.");
        tcpLabel.setText(level == LearningLevel.BASIC
                ? "Contiene puertos, secuencia, ACK y flags."
                : "Header de transporte con puertos, secuencia, ACK, flags y posible ventana.");
        udpLabel.setText(level == LearningLevel.BASIC
                ? "Cabecera más simple, con menos sobrecarga que TCP."
                : "Header ligero con puertos y longitud total del datagrama.");
    }

    private VBox pill(String iconText, String titleText, Label body, String fill, String border) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: " + border + ";");
        Label title = new Label(iconText + "  " + titleText);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 11px; -fx-text-fill: #475b71;");
        box.getChildren().addAll(title, body);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }
}
