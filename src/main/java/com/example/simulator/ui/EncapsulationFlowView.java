package com.example.simulator.ui;

import com.example.simulator.presentation.layers.EncapsulationSnapshot;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

public class EncapsulationFlowView extends VBox {
    public EncapsulationFlowView() {
        setSpacing(16);
    }

    public void update(EncapsulationSnapshot snapshot, LearningLevel level, String protocolName) {
        DashboardCard shell = new DashboardCard("ENCAPSULACIÓN", "Encapsulación (con " + protocolName + ")",
                "Paso a paso visual de cómo el mensaje crece al bajar por las capas.");

        VBox steps = new VBox(12,
                step("1", "Datos de aplicación", packetRow(segment("Datos", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION, 240)),
                        "La aplicación genera el mensaje original."),
                step("2", snapshot.getTransportTitle() + " + Datos",
                        packetRow(
                                segment(snapshot.getTransportTitle().contains("UDP") ? "Header UDP" : "Header TCP",
                                        UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT, 132),
                                segment("Datos", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION, 220)
                        ),
                        "La capa de transporte añade puertos y control."),
                step("3", "Header IP + transporte + datos",
                        packetRow(
                                segment("Header IP", UiTheme.LAYER_BG_NETWORK, UiTheme.LAYER_BORDER_NETWORK, 110),
                                segment(snapshot.getTransportTitle().contains("UDP") ? "UDP" : "TCP",
                                        UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT, 110),
                                segment("Datos", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION, 180)
                        ),
                        "La capa de red añade dirección lógica y encaminamiento."),
                step("4", "Header de enlace + IP + transporte + datos",
                        packetRow(
                                segment("Header Enlace", UiTheme.LAYER_BG_LINK, UiTheme.LAYER_BORDER_LINK, 120),
                                segment("IP", UiTheme.LAYER_BG_NETWORK, UiTheme.LAYER_BORDER_NETWORK, 84),
                                segment(snapshot.getTransportTitle().contains("UDP") ? "UDP" : "TCP",
                                        UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT, 90),
                                segment("Datos", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION, 160)
                        ),
                        "La capa de enlace prepara la trama para la red local."),
                step("5", "Bits / transmisión", packetRow(segment("Señal", UiTheme.LAYER_BG_PHYSICAL, UiTheme.LAYER_BORDER_PHYSICAL, 320)),
                        "La información ya está lista para viajar por el medio físico.")
        );

        shell.setContent(steps);
        getChildren().setAll(shell);
    }

    private VBox step(String number, String titleText, HBox structure, String explanation) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle(UiTheme.PANEL_INSET_TINT);

        HBox header = new HBox(10);
        Label index = new Label(number);
        index.setStyle("-fx-background-color: #102033; -fx-text-fill: white; -fx-font-weight: bold;"
                + "-fx-background-radius: 999; -fx-padding: 5 9 5 9;");
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        header.getChildren().addAll(index, title);

        Label detail = new Label(explanation);
        detail.setWrapText(true);
        detail.setStyle(UiTheme.SUBTITLE);
        card.getChildren().addAll(header, structure, detail);
        return card;
    }

    private HBox packetRow(Region... segments) {
        HBox row = new HBox(0);
        for (Region segment : segments) {
            row.getChildren().add(segment);
            HBox.setHgrow(segment, Priority.NEVER);
        }
        row.setPadding(new Insets(2, 0, 2, 0));
        return row;
    }

    private StackPane segment(String text, String fill, String border, double width) {
        StackPane box = new StackPane();
        box.setPrefWidth(width);
        box.setMinHeight(44);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 0;"
                + "-fx-border-radius: 0;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 1.2;");
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        box.getChildren().add(label);
        if ("Datos".equals(text)) {
            Tooltip.install(box, tooltip("Payload", "Datos que una capa recibe de la capa superior y encapsula para transmitirlos."));
        } else if (text.contains("TCP")) {
            Tooltip.install(box, tooltip("Header TCP", "Contiene campos como puertos, número de secuencia, número de ACK y flags."));
        } else if (text.contains("UDP")) {
            Tooltip.install(box, tooltip("Header UDP", "Cabecera más simple, con menos sobrecarga que TCP."));
        }
        return box;
    }

    private Tooltip tooltip(String title, String body) {
        Tooltip tooltip = new Tooltip(title + "\n" + body);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(260);
        return tooltip;
    }
}
