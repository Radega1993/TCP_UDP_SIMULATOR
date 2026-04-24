package com.example.simulator.ui;

import com.example.simulator.presentation.layers.EncapsulationSnapshot;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class PacketStructureBar extends VBox {
    public PacketStructureBar() {
        setSpacing(12);
    }

    public void update(EncapsulationSnapshot snapshot, LearningLevel level) {
        int linkBytes = 14;
        int ipBytes = 20;
        int transportBytes = snapshot.getTransportTitle().contains("UDP") ? 8 : 20;
        int payloadBytes = Math.max(4, snapshot.getPayload().length());
        int total = linkBytes + ipBytes + transportBytes + payloadBytes;

        HBox bar = new HBox(
                segment("Enlace", linkBytes + " B", UiTheme.LAYER_BG_LINK, UiTheme.LAYER_BORDER_LINK, 16),
                segment("IP", ipBytes + " B", UiTheme.LAYER_BG_NETWORK, UiTheme.LAYER_BORDER_NETWORK, 18),
                segment(snapshot.getTransportTitle().contains("UDP") ? "UDP" : "TCP",
                        transportBytes + " B",
                        UiTheme.LAYER_BG_TRANSPORT,
                        UiTheme.LAYER_BORDER_TRANSPORT,
                        18),
                segment("Datos", payloadBytes + " B", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION, 48)
        );
        bar.setSpacing(0);

        Label totalLabel = new Label("Total: " + total + " bytes");
        totalLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #17324a;");

        Label detail = new Label(level == LearningLevel.BASIC
                ? "Barra segmentada del paquete con tamaños orientativos."
                : "Tamaños didácticos aproximados para entender qué parte ocupa cada cabecera.");
        detail.setWrapText(true);
        detail.setStyle(UiTheme.SUBTITLE);

        DashboardCard shell = new DashboardCard("PAQUETE", "Estructura de un paquete (ejemplo)",
                "Barra visual de cabeceras y datos con tamaños orientativos.");
        shell.setContent(new VBox(10, bar, totalLabel, detail));
        getChildren().setAll(shell);
    }

    private VBox segment(String titleText, String sizeText, String fill, String border, double weight) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 1.5;"
                + "-fx-background-radius: 0;"
                + "-fx-border-radius: 0;");
        box.setPrefWidth(weight * 6);
        HBox.setHgrow(box, Priority.ALWAYS);

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        Label size = new Label(sizeText);
        size.setStyle("-fx-font-size: 11px; -fx-text-fill: #5b6f86;");
        box.getChildren().addAll(title, size);
        return box;
    }
}
