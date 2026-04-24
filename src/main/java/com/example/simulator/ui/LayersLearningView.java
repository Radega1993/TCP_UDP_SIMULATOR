package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.presentation.layers.EncapsulationMapper;
import com.example.simulator.presentation.layers.EncapsulationSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.function.Consumer;

public class LayersLearningView extends BorderPane {
    private final Consumer<String> packetStructureOpener;

    private final LayersSidebar sidebar = new LayersSidebar();
    private final LayersContextSidebar contextSidebar = new LayersContextSidebar();
    private final EducationalFooterPanel footerPanel = new EducationalFooterPanel();

    private final StackPane mainContentStack = new StackPane();
    private final TcpIpLayersPanel tcpIpLayersPanel = new TcpIpLayersPanel();
    private final OsiLayersPanel osiLayersPanel = new OsiLayersPanel();
    private final ModelsComparisonPanel comparisonPanel = new ModelsComparisonPanel();
    private final EncapsulationFlowView encapsulationFlowView = new EncapsulationFlowView();
    private final HeaderDetailsPanel headerDetailsPanel;

    private final VBox comparisonDashboard = new VBox(18);
    private final VBox encapsulationDashboard = new VBox(18);
    private final VBox packetStructureDashboard = new VBox(18);

    private LayersMode currentMode = LayersMode.COMPARISON;
    private LearningLevel currentLevel = LearningLevel.BASIC;
    private ProtocolType currentProtocol = ProtocolType.TCP;
    private Packet selectedPacket;
    private String currentMessage = "HOLA";

    public LayersLearningView(Consumer<String> packetStructureOpener) {
        this.packetStructureOpener = packetStructureOpener;
        this.headerDetailsPanel = new HeaderDetailsPanel(packetStructureOpener);

        setStyle(UiTheme.APP_BACKGROUND);
        setPadding(new Insets(0));

        setTop(buildHeader());
        setCenter(buildBody());
        setBottom(footerPanel);
        BorderPane.setMargin(footerPanel, new Insets(18, 0, 0, 0));

        sidebar.setOnModeSelected(mode -> {
            currentMode = mode;
            refreshView();
        });
        sidebar.setOnLevelChanged(level -> {
            currentLevel = level;
            refreshView();
        });
        sidebar.setOnProtocolChanged(protocol -> {
            currentProtocol = protocol;
            refreshView();
        });
        sidebar.setSelectedMode(currentMode);
        sidebar.setLearningLevel(currentLevel);
        sidebar.setProtocol(currentProtocol);

        refreshView();
    }

    public void updatePacketContext(Packet packet, ProtocolType protocolType, String message) {
        selectedPacket = packet;
        if (protocolType != null) {
            currentProtocol = protocolType;
            sidebar.setProtocol(protocolType);
        }
        if (message != null && !message.isBlank()) {
            currentMessage = message;
        }
        refreshView();
    }

    private Node buildHeader() {
        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(22));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.92);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: #dbe5ef;");

        VBox iconCard = new VBox(8);
        iconCard.setAlignment(Pos.CENTER);
        iconCard.setPadding(new Insets(16));
        iconCard.setPrefWidth(136);
        iconCard.setStyle("-fx-background-color: linear-gradient(to bottom, #f4f6fb, #ffffff);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: #dbe5ef;");

        Label networkGlyph = new Label("[ TCP/IP ]\n[   OSI   ]");
        networkGlyph.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2f4b66; -fx-text-alignment: center;");
        networkGlyph.setAlignment(Pos.CENTER);

        Label glyphSubtitle = new Label("Capas\n& red");
        glyphSubtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7c8d; -fx-text-alignment: center;");
        glyphSubtitle.setWrapText(true);
        iconCard.getChildren().addAll(networkGlyph, glyphSubtitle);

        VBox textBox = new VBox(8);
        Label title = new Label("Modelo TCP/IP y OSI — Capas y Encapsulación");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #102033;");

        Label subtitle = new Label("Visualiza las capas, sus equivalencias y cómo se encapsulan los datos en la red.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5f7285;");

        textBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        row.getChildren().addAll(iconCard, textBox);
        BorderPane.setMargin(row, new Insets(0, 0, 18, 0));
        return row;
    }

    private Node buildBody() {
        ScrollPane centerScroll = new ScrollPane(mainContentStack);
        centerScroll.setFitToWidth(true);
        centerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        centerScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox centerShell = new VBox(centerScroll);
        VBox.setVgrow(centerScroll, Priority.ALWAYS);
        centerShell.setMaxWidth(Double.MAX_VALUE);

        DashboardCard contextShell = new DashboardCard("AYUDA CONTEXTUAL", "Ideas clave",
                "Resumen rápido para acompañar la explicación sin saturar la vista principal.");
        contextShell.setContent(contextSidebar);
        contextShell.setPrefWidth(320);
        contextShell.setMinWidth(300);

        HBox row = new HBox(18, sidebar, centerShell, contextShell);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(centerShell, Priority.ALWAYS);
        centerShell.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private void refreshView() {
        EncapsulationSnapshot snapshot = EncapsulationMapper.fromPacket(selectedPacket, currentProtocol, currentMessage);
        String packetStructureText = EncapsulationMapper.buildPacketStructureText(selectedPacket, currentProtocol, currentMessage);

        tcpIpLayersPanel.setLearningLevel(currentLevel);
        osiLayersPanel.setLearningLevel(currentLevel);
        comparisonPanel.setLearningLevel(currentLevel);
        encapsulationFlowView.update(snapshot, currentLevel, currentProtocol == ProtocolType.UDP ? "UDP" : "TCP");
        headerDetailsPanel.update(snapshot, currentLevel, currentProtocol, selectedPacket != null, packetStructureText);

        comparisonDashboard.getChildren().setAll(
                comparisonPanel,
                encapsulationFlowView,
                headerDetailsPanel
        );

        Label focusLabel = new Label("El paquete resultante combina cabeceras de enlace, red y transporte alrededor del payload.");
        focusLabel.setWrapText(true);
        focusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #365067;");
        VBox focusCard = new VBox(focusLabel);
        focusCard.setPadding(new Insets(14));
        focusCard.setStyle("-fx-background-color: #f5f9ff;"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: #dbeafe;");

        encapsulationDashboard.getChildren().setAll(
                focusCard,
                encapsulationFlowView,
                headerDetailsPanel
        );

        packetStructureDashboard.getChildren().setAll(headerDetailsPanel);

        Node activeContent = switch (currentMode) {
            case COMPARISON -> comparisonDashboard;
            case OSI -> osiLayersPanel;
            case TCP_IP -> tcpIpLayersPanel;
            case ENCAPSULATION -> encapsulationDashboard;
            case PACKET_STRUCTURE -> packetStructureDashboard;
        };

        mainContentStack.getChildren().setAll(activeContent);
        sidebar.setSelectedMode(currentMode);
        contextSidebar.update(currentMode, currentProtocol, currentLevel);
        footerPanel.update(currentMode, currentLevel);
    }
}
