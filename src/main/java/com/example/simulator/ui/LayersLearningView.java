package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.presentation.layers.EncapsulationMapper;
import com.example.simulator.presentation.layers.EncapsulationSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

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
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 18, 0, 18));
        row.setMinHeight(58);
        row.setPrefHeight(58);
        row.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e6f2; -fx-border-width: 0 0 1 0;");

        Label brand = new Label("☰  Simulador visual de TCP y UDP");
        brand.setStyle("-fx-text-fill: #142d4c; -fx-font-size: 18px; -fx-font-weight: 900;");
        Label mode = pill("Modelo de capas: TCP/IP vs OSI", "#fff2e9", "#f25c05");
        Label ready = pill("✦ Listo para explorar", "#e7f8ef", "#087f4f");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label reset = action("↻ Reiniciar");
        Label compact = action("⌘ Vista compacta");
        Label help = action("? Ayuda");
        row.getChildren().addAll(brand, mode, ready, spacer, reset, compact, help);
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

        HBox row = new HBox(14, buildDesignSidebar(), centerShell, buildDetailsRail());
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: #f4f7fb;");
        HBox.setHgrow(centerShell, Priority.ALWAYS);
        centerShell.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private Node buildDesignSidebar() {
        VBox rail = new VBox(14);
        rail.setPrefWidth(330);
        rail.setMinWidth(300);

        DashboardCard config = new DashboardCard(null, "Configuración", null);
        config.setStyle(modelCardStyle());
        config.setPadding(new Insets(18));
        config.setContent(new VBox(12,
                fieldLabel("Vista"),
                segmented("Comparación", "Encapsulación"),
                fieldLabel("Protocolo de ejemplo"),
                protocolSelect(),
                fieldLabel("Escenario"),
                simpleSelect("Personalizado"),
                fieldLabel("Mensaje de ejemplo"),
                messageInput()
        ));

        DashboardCard unit = new DashboardCard(null, "Unidad de información", null);
        unit.setStyle(modelCardStyle());
        unit.setPadding(new Insets(18));
        Label p = new Label("Seleccione qué se muestra en cada capa");
        p.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 13px;");
        ToggleGroup group = new ToggleGroup();
        RadioButton full = radio("Encabezado + Datos", group, true);
        RadioButton header = radio("Solo encabezado", group, false);
        unit.setContent(new VBox(10, p, full, header));

        DashboardCard colors = new DashboardCard(null, "Colores por tipo de función", null);
        colors.setStyle(modelCardStyle());
        colors.setPadding(new Insets(18));
        colors.setContent(new VBox(13,
                colorRow("#19a663", "Aplicación / Sesión"),
                colorRow("#2f80ed", "Transporte"),
                colorRow("#8a55e6", "Red"),
                colorRow("#ff8b1a", "Enlace de datos"),
                colorRow("#ff5c5c", "Física")
        ));

        HBox tip = new HBox(14);
        tip.setPadding(new Insets(20));
        tip.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 14;"
                + "-fx-border-radius: 14; -fx-border-color: #d9e6f2;");
        Label icon = new Label("i");
        icon.setAlignment(Pos.CENTER);
        icon.setMinSize(30, 30);
        icon.setStyle("-fx-background-color: #2f80ed; -fx-background-radius: 999;"
                + "-fx-text-fill: #ffffff; -fx-font-weight: 900;");
        Label text = new Label("Los modelos OSI y TCP/IP ayudan a entender cómo viaja la información en una red.");
        text.setWrapText(true);
        text.setStyle("-fx-text-fill: #47617d; -fx-font-size: 13px;");
        tip.getChildren().addAll(icon, text);
        HBox.setHgrow(text, Priority.ALWAYS);

        rail.getChildren().addAll(config, unit, colors, tip);
        return rail;
    }

    private Node buildDetailsRail() {
        VBox rail = new VBox(14);
        rail.setPrefWidth(420);
        rail.setMinWidth(390);

        DashboardCard detail = new DashboardCard(null, "Detalle por capa", null);
        detail.setStyle(modelCardStyle());
        detail.setPadding(new Insets(18));
        Label selected = pill("Capa seleccionada", "#e7f8ef", "#087f4f");
        HBox head = new HBox(selected);
        head.setAlignment(Pos.CENTER_RIGHT);
        Label title = new Label("4. Aplicación (TCP/IP)");
        title.setStyle("-fx-text-fill: #19a663; -fx-font-size: 20px; -fx-font-weight: 900;");
        Label body = new Label("Esta capa agrupa los protocolos utilizados por las aplicaciones para comunicarse a través de la red.");
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #345573; -fx-font-size: 13px;");
        detail.setContent(new VBox(12, head, title, body,
                detailTitle("Funciones principales"),
                bullet("Interfaz entre aplicaciones y red"),
                bullet("Formato de datos y codificación"),
                bullet("Acceso a servicios de red"),
                detailTitle("Ejemplos de protocolos"),
                chips("HTTP", "DNS", "DHCP", "FTP", "SMTP")
        ));

        DashboardCard encap = new DashboardCard(null, "Encapsulación", "(Ejemplo: " + currentProtocol + ")");
        encap.setStyle(modelCardStyle());
        encap.setPadding(new Insets(18));
        Label encapBody = new Label("Así viaja \"" + currentMessage + "\" por la red");
        encapBody.setStyle("-fx-text-fill: #345573; -fx-font-size: 13px;");
        encap.setContent(new VBox(8,
                encapBody,
                encapStep("Datos de Aplicación", currentMessage, "#eaf8f0", "#a6e3c0", "#087f4f"),
                plus(),
                encapStep((currentProtocol == ProtocolType.UDP ? "Datagrama UDP" : "Segmento TCP"),
                        (currentProtocol == ProtocolType.UDP ? "UDP Header + Datos" : "TCP Header + Datos"),
                        "#eef6ff", "#a9ccff", "#1e6ccf"),
                plus(),
                encapStep("Paquete IP", "IP Header + Transporte", "#f3ecff", "#c9b1ff", "#7144d8"),
                plus(),
                encapStep("Trama Ethernet", "Ethernet Header + Paquete IP", "#fff1e3", "#ffc78b", "#e36d00"),
                plus(),
                encapStep("Bits", "1010 1100 1011 0101 ...", "#fff0ef", "#ffc0bd", "#e24545")
        ));
        rail.getChildren().addAll(detail, encap);
        return rail;
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
                comparisonPanel
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

    private Label pill(String text, String bg, String color) {
        Label label = new Label(text);
        label.setPadding(new Insets(7, 16, 7, 16));
        label.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 999;"
                + "-fx-text-fill: " + color + "; -fx-font-weight: 900; -fx-font-size: 12px;");
        return label;
    }

    private Label action(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(10, 14, 10, 14));
        label.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8;"
                + "-fx-border-radius: 8; -fx-border-color: #d9e6f2;"
                + "-fx-text-fill: #173452; -fx-font-size: 12px; -fx-font-weight: 900;");
        return label;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #193753; -fx-font-size: 13px; -fx-font-weight: 900;");
        return label;
    }

    private Node segmented(String left, String right) {
        HBox box = new HBox(0);
        Label a = new Label(left);
        Label b = new Label(right);
        a.setAlignment(Pos.CENTER);
        b.setAlignment(Pos.CENTER);
        a.setMaxWidth(Double.MAX_VALUE);
        b.setMaxWidth(Double.MAX_VALUE);
        a.setPadding(new Insets(11, 10, 11, 10));
        b.setPadding(new Insets(11, 10, 11, 10));
        a.setStyle("-fx-background-color: #2f80ed; -fx-text-fill: white; -fx-font-weight: 900;");
        b.setStyle("-fx-background-color: white; -fx-text-fill: #183653; -fx-font-weight: 900;");
        a.setOnMouseClicked(event -> currentMode = LayersMode.COMPARISON);
        b.setOnMouseClicked(event -> {
            currentMode = LayersMode.ENCAPSULATION;
            refreshView();
        });
        HBox.setHgrow(a, Priority.ALWAYS);
        HBox.setHgrow(b, Priority.ALWAYS);
        box.getChildren().addAll(a, b);
        box.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #d9e6f2;");
        return box;
    }

    private ComboBox<ProtocolType> protocolSelect() {
        ComboBox<ProtocolType> box = new ComboBox<>();
        box.getItems().addAll(ProtocolType.TCP, ProtocolType.UDP);
        box.setValue(currentProtocol);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setOnAction(event -> {
            currentProtocol = box.getValue();
            refreshView();
        });
        return box;
    }

    private ComboBox<String> simpleSelect(String value) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().add(value);
        box.setValue(value);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private TextField messageInput() {
        TextField field = new TextField(currentMessage);
        field.textProperty().addListener((obs, oldValue, newValue) -> currentMessage = newValue);
        return field;
    }

    private RadioButton radio(String text, ToggleGroup group, boolean selected) {
        RadioButton radio = new RadioButton(text);
        radio.setToggleGroup(group);
        radio.setSelected(selected);
        radio.setStyle("-fx-text-fill: #314e6b; -fx-font-size: 13px; -fx-font-weight: 700;");
        return radio;
    }

    private Node colorRow(String color, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(6.5);
        dot.setStyle("-fx-fill: " + color + ";");
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #314e6b; -fx-font-size: 13px;");
        row.getChildren().addAll(dot, label);
        return row;
    }

    private Label detailTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #102a43; -fx-font-size: 13px; -fx-font-weight: 900;");
        return label;
    }

    private Label bullet(String text) {
        Label label = new Label("• " + text);
        label.setStyle("-fx-text-fill: #345573; -fx-font-size: 13px;");
        return label;
    }

    private Node chips(String... values) {
        FlowPane pane = new FlowPane(9, 9);
        for (String value : values) {
            Label chip = new Label(value);
            chip.setPadding(new Insets(7, 12, 7, 12));
            chip.setStyle("-fx-background-color: #dff7e9; -fx-background-radius: 7;"
                    + "-fx-text-fill: #087f4f; -fx-font-size: 12px; -fx-font-weight: 900;");
            pane.getChildren().add(chip);
        }
        return pane;
    }

    private Label plus() {
        Label label = new Label("⊕");
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-text-fill: #456080; -fx-font-size: 20px;");
        return label;
    }

    private Node encapStep(String title, String value, String bg, String border, String color) {
        VBox step = new VBox(5);
        step.setAlignment(Pos.CENTER);
        step.setMaxWidth(255);
        step.setPadding(new Insets(14));
        step.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 7;"
                + "-fx-border-radius: 7; -fx-border-color: " + border + ";");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-font-weight: 900;");
        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        step.getChildren().addAll(titleLabel, valueLabel);
        VBox wrapper = new VBox(step);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private String modelCardStyle() {
        return "-fx-background-color: #ffffff; -fx-background-radius: 14;"
                + "-fx-border-radius: 14; -fx-border-color: #d9e6f2;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.035), 22, 0.20, 0, 8);";
    }
}
