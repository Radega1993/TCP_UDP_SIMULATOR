package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class LayersSidebar extends VBox {
    private final Map<LayersMode, VBox> navCards = new EnumMap<>(LayersMode.class);
    private final LearningLevelToggle levelToggle = new LearningLevelToggle();
    private final ProtocolFocusToggle protocolToggle = new ProtocolFocusToggle();
    private Consumer<LayersMode> onModeSelected;

    public LayersSidebar() {
        setSpacing(16);
        setPadding(new Insets(0));
        setPrefWidth(280);
        setMinWidth(260);

        DashboardCard navCard = new DashboardCard("VISTAS", "Recorrido",
                "Elige el enfoque visual que quieras explicar en clase.");
        VBox navList = new VBox(10,
                navItem(LayersMode.COMPARISON, "[=]", "Comparación de modelos", "OSI frente a TCP/IP"),
                navItem(LayersMode.OSI, "[7]", "Modelo OSI", "Las 7 capas clásicas"),
                navItem(LayersMode.TCP_IP, "[4]", "Modelo TCP/IP", "La pila práctica de Internet"),
                navItem(LayersMode.ENCAPSULATION, "[+]", "Encapsulación", "Cómo crece el paquete"),
                navItem(LayersMode.PACKET_STRUCTURE, "[#]", "Estructura del paquete", "Cabeceras y tamaños")
        );
        navCard.setContent(navList);

        DashboardCard levelCard = new DashboardCard("NIVEL", "Detalle didáctico",
                "Ajusta la profundidad según el grupo.");
        levelCard.setContent(levelToggle);

        DashboardCard protocolCard = new DashboardCard("PROTOCOLO ACTIVO", "Encapsulación y cabeceras",
                "Cambia el protocolo para ver diferencias claras entre TCP y UDP.");
        protocolCard.setContent(protocolToggle);

        DashboardCard didYouKnowCard = new DashboardCard("¿SABÍAS QUE?", "Idea clave",
                "Cada capa añade información necesaria para que los datos lleguen correctamente a su destino.");
        VBox factBody = new VBox(8);
        factBody.setPadding(new Insets(2, 0, 0, 0));
        Label icon = new Label("(i)");
        icon.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1d4ed8;");
        Label body = new Label("En la red real, el receptor va retirando cabeceras en orden inverso al emisor.");
        body.setWrapText(true);
        body.setStyle(UiTheme.SUBTITLE);
        factBody.getChildren().addAll(icon, body);
        didYouKnowCard.setContent(factBody);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(navCard, levelCard, protocolCard, spacer, didYouKnowCard);
        setStyle("-fx-background-color: rgba(247,249,252,0.85);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: #e3eaf1;"
                + "-fx-padding: 16;");
    }

    public void setOnModeSelected(Consumer<LayersMode> onModeSelected) {
        this.onModeSelected = onModeSelected;
    }

    public void setOnLevelChanged(Consumer<LearningLevel> onLevelChanged) {
        levelToggle.setOnLevelChanged(onLevelChanged);
    }

    public void setOnProtocolChanged(Consumer<ProtocolType> onProtocolChanged) {
        protocolToggle.setOnProtocolChanged(onProtocolChanged);
    }

    public void setSelectedMode(LayersMode mode) {
        navCards.forEach((value, card) -> card.setStyle(value == mode ? activeNavStyle() : idleNavStyle()));
    }

    public void setLearningLevel(LearningLevel level) {
        levelToggle.setLevel(level);
    }

    public void setProtocol(ProtocolType protocolType) {
        protocolToggle.setProtocol(protocolType);
    }

    private VBox navItem(LayersMode mode, String iconText, String titleText, String subtitleText) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setStyle(idleNavStyle());

        Label icon = new Label(iconText);
        icon.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #5b6f86;");
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        Label subtitle = new Label(subtitleText);
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #66788b;");

        card.getChildren().addAll(icon, title, subtitle);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setOnMouseClicked(event -> {
            setSelectedMode(mode);
            if (onModeSelected != null) {
                onModeSelected.accept(mode);
            }
        });
        navCards.put(mode, card);
        return card;
    }

    private String activeNavStyle() {
        return "-fx-background-color: linear-gradient(to right, #eef4ff, #f8fbff);"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: #8fb2ff;"
                + "-fx-border-width: 1.5;";
    }

    private String idleNavStyle() {
        return "-fx-background-color: #ffffff;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: #dbe5ef;";
    }
}
