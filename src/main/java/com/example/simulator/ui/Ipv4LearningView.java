package com.example.simulator.ui;

import com.example.simulator.domain.ipv4.Ipv4SubnetCalculator;
import com.example.simulator.domain.ipv4.Ipv4SubnetResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.List;

public class Ipv4LearningView extends VBox {
    private final TextField sourceIpField = new TextField("192.168.1.10");
    private final TextField destinationIpField = new TextField("192.168.2.20");
    private final TextField gatewayField = new TextField("192.168.1.1");
    private final Spinner<Integer> ttlSpinner = new Spinner<>(1, 255, 64);
    private final ComboBox<MaskOption> maskBox = new ComboBox<>();
    private final Label readyPill = new Label("Listo para calcular");
    private final Label heroTitle = new Label();
    private final Label heroBody = new Label();
    private final StackPane resultIcon = new StackPane();
    private final HBox badges = new HBox(10);
    private final Label diagramSummary = new Label();
    private final Label sourceZoneNetwork = new Label();
    private final Label destinationZoneNetwork = new Label();
    private final Label sourceDeviceIp = new Label();
    private final Label destinationDeviceIp = new Label();
    private final Label gatewayIp = new Label();
    private final VBox resultsTable = new VBox(10);
    private final VBox feedbackBox = new VBox(12);
    private final VBox classroomList = new VBox(8);
    private final VBox binaryRows = new VBox(16);
    private final VBox decisionBox = new VBox(10);
    private final Label nextHopLabel = new Label();
    private final Label usedInterfaceLabel = new Label();
    private final Label routeDestinationNetworkLabel = new Label();
    private final Label ttlPacketBadge = new Label();
    private final Label routingStatusLabel = new Label();
    private final Label ttlExpiredPanel = new Label();
    private final Label ttlPacketDetailPanel = new Label();
    private final Label ttlPacketSourceLabel = new Label();
    private final Label ttlPacketDestinationLabel = new Label();
    private final Label ttlPacketCurrentLabel = new Label();
    private final VBox routingTimeline = new VBox(10);
    private final VBox routingTable = new VBox(8);
    private final StackPane clientRouteNode = new StackPane();
    private final StackPane switchRouteNode = new StackPane();
    private final StackPane routerRouteNode = new StackPane();
    private final StackPane serverRouteNode = new StackPane();
    private final Label ip2NextHopLabel = new Label();
    private final Label ip2UsedInterfaceLabel = new Label();
    private final Label ip2DestinationNetworkLabel = new Label();
    private final Label ip2StatusLabel = new Label();
    private final VBox ip2RoutingRows = new VBox(8);
    private final VBox ip2TrajectoryRows = new VBox(10);
    private final StackPane sprintContentStack = new StackPane();
    private final Button ip1Tab = new Button("Red y máscara");
    private final Button ip2Tab = new Button("Gateway y ruta");
    private final Button ip3Tab = new Button("TTL y descarte");
    private final Button ip4Tab = new Button("ICMP y ping");
    private final IcmpPingView icmpPingView = new IcmpPingView();
    private final Button ip5Tab = new Button("Cabecera IP");
    private final Ipv4HeaderInspectorView headerInspectorView = new Ipv4HeaderInspectorView();
    private final Button ip6Tab = new Button("Tabla de rutas");
    private final Ipv4RouteTableView routeTableView = new Ipv4RouteTableView();
    private Node ip1Screen;
    private Node ip2Screen;
    private Node ip3Screen;
    private Node ip4Screen;
    private Node ip5Screen;
    private Node ip6Screen;
    private IpSprint activeSprint = IpSprint.IP1;

    public Ipv4LearningView(Runnable onHome, Runnable onTheory, Runnable onHelp) {
        setSpacing(14);
        setPadding(new Insets(0, 0, 14, 0));
        setStyle("-fx-background-color: #f4f7fb;");

        maskBox.getItems().setAll(
                new MaskOption(8),
                new MaskOption(16),
                new MaskOption(24),
                new MaskOption(25),
                new MaskOption(26),
                new MaskOption(27),
                new MaskOption(28),
                new MaskOption(30),
                new MaskOption(31),
                new MaskOption(32)
        );
        maskBox.setValue(maskBox.getItems().get(2));
        maskBox.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(buildTopbar(onHome, onTheory, onHelp), buildSprintTabs(), buildMainLayout(), buildLegend());
        addImmediateFeedback();
        calculateAndRender();
        showSprint(IpSprint.IP1);
    }

    public void resetInputs() {
        sourceIpField.setText("192.168.1.10");
        destinationIpField.setText("192.168.2.20");
        gatewayField.setText("192.168.1.1");
        ttlSpinner.getValueFactory().setValue(64);
        maskBox.setValue(maskBox.getItems().stream().filter(option -> option.cidr == 24).findFirst().orElse(maskBox.getItems().get(0)));
        calculateAndRender();
    }

    private Node buildTopbar(Runnable onHome, Runnable onTheory, Runnable onHelp) {
        StackPane menu = new StackPane(icon("/icons/menu.svg", 20));
        menu.setOnMouseClicked(event -> onHome.run());
        menu.setStyle("-fx-min-width: 34; -fx-min-height: 34; -fx-alignment: center;"
                + "-fx-background-color: #e8f1ff; -fx-background-radius: 10;"
                + "-fx-cursor: hand;");

        Label brand = new Label("Simulador visual de TCP y UDP");
        brand.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #102a43;");
        HBox brandBox = new HBox(12, menu, brand);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);

        Label modePill = pill("Módulo IP: IPv4 y subredes", "#eaf3ff", "#2f80ed");
        readyPill.setStyle(pillStyle("#e7f8ef", "#087f4f"));

        Button reset = ghostButton("Reiniciar", "/icons/refresh.svg");
        reset.setOnAction(event -> resetInputs());
        Button theory = ghostButton("Teoría", "/icons/info.svg");
        theory.setOnAction(event -> onTheory.run());
        Button help = ghostButton("Ayuda", "/icons/help.svg");
        help.setOnAction(event -> onHelp.run());
        HBox actions = new HBox(8, reset, theory, help);

        HBox topbar = new HBox(16, brandBox, modePill, readyPill, actions);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(0, 18, 0, 18));
        topbar.setMinHeight(58);
        topbar.setStyle("-fx-background-color: #ffffff;"
                + "-fx-border-color: transparent transparent #d9e6f2 transparent;"
                + "-fx-border-width: 0 0 1 0;"
                + "-fx-effect: dropshadow(gaussian, rgba(17,42,67,0.04), 12, 0.2, 0, 2);");
        return topbar;
    }

    private Node buildMainLayout() {
        GridPane layout = new GridPane();
        layout.setHgap(14);
        layout.setVgap(14);
        layout.setPadding(new Insets(0, 14, 0, 14));
        layout.getColumnConstraints().setAll(
                fixedColumn(330),
                growingColumn(1100)
        );

        ip1Screen = buildIp1Screen();
        ip2Screen = buildIp2Screen();
        ip3Screen = buildIp3Screen();
        ip4Screen = buildIp4Screen();
        ip5Screen = buildIp5Screen();
        ip6Screen = buildIp6Screen();
        sprintContentStack.getChildren().setAll(ip1Screen, ip2Screen, ip3Screen, ip4Screen, ip5Screen, ip6Screen);
        layout.add(buildLeftColumn(), 0, 0);
        layout.add(sprintContentStack, 1, 0);
        return layout;
    }

    private Node buildSprintTabs() {
        ip1Tab.setGraphic(tabGraphic("/icons/ip.svg", "Red y máscara", "IP, red, broadcast y hosts"));
        ip2Tab.setGraphic(tabGraphic("/icons/router.svg", "Gateway y ruta", "Siguiente salto e interfaz"));
        ip3Tab.setGraphic(tabGraphic("/icons/info.svg", "TTL y descarte", "TTL baja en cada salto"));
        ip4Tab.setGraphic(tabGraphic("/icons/network.svg", "ICMP y ping", "Echo Request y Reply"));
        ip5Tab.setGraphic(tabGraphic("/icons/binary.svg", "Cabecera IP", "Inspector del paquete"));
        ip6Tab.setGraphic(tabGraphic("/icons/router.svg", "Tabla de rutas", "Longest prefix match"));
        ip1Tab.setText("");
        ip2Tab.setText("");
        ip3Tab.setText("");
        ip4Tab.setText("");
        ip5Tab.setText("");
        ip6Tab.setText("");
        HBox tabs = new HBox(10, ip1Tab, ip2Tab, ip3Tab, ip4Tab, ip5Tab, ip6Tab);
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.setPadding(new Insets(14, 18, 0, 18));
        ip1Tab.setOnAction(event -> showSprint(IpSprint.IP1));
        ip2Tab.setOnAction(event -> showSprint(IpSprint.IP2));
        ip3Tab.setOnAction(event -> showSprint(IpSprint.IP3));
        ip4Tab.setOnAction(event -> showSprint(IpSprint.IP4));
        ip5Tab.setOnAction(event -> showSprint(IpSprint.IP5));
        ip6Tab.setOnAction(event -> showSprint(IpSprint.IP6));
        return tabs;
    }

    private Node tabGraphic(String iconPath, String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5f7390;");
        VBox copy = new VBox(1, titleLabel, subtitleLabel);
        HBox box = new HBox(9, iconTile(iconPath), copy);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMouseTransparent(true);
        return box;
    }

    private Node buildIp1Screen() {
        GridPane layout = baseScreenGrid(680, 390);
        layout.add(buildIp1CenterColumn(), 0, 0);
        layout.add(buildRightColumn(), 1, 0);
        return layout;
    }

    private Node buildIp2Screen() {
        GridPane layout = baseScreenGrid(820, 360);
        layout.add(new VBox(14, buildIp2RoutingCard(), buildRoutingTableCard()), 0, 0);
        layout.add(buildTrajectoryCard(), 1, 0);
        return layout;
    }

    private Node buildIp3Screen() {
        GridPane layout = baseScreenGrid(820, 360);
        layout.add(new VBox(14, buildRoutingCard(true), buildTtlTheoryGrid()), 0, 0);
        layout.add(buildTtlPacketCard(), 1, 0);
        return layout;
    }

    private Node buildIp4Screen() {
        return icmpPingView;
    }

    private Node buildIp5Screen() {
        return headerInspectorView;
    }

    private Node buildIp6Screen() {
        return routeTableView;
    }

    private GridPane baseScreenGrid(double centerMinWidth, double rightWidth) {
        GridPane layout = new GridPane();
        layout.setHgap(14);
        layout.setVgap(14);
        layout.getColumnConstraints().setAll(
                growingColumn(centerMinWidth),
                fixedColumn(rightWidth)
        );
        return layout;
    }

    private ColumnConstraints fixedColumn(double width) {
        ColumnConstraints constraints = new ColumnConstraints(width, width, width);
        constraints.setFillWidth(true);
        return constraints;
    }

    private ColumnConstraints growingColumn(double minWidth) {
        ColumnConstraints constraints = new ColumnConstraints(minWidth, 900, Double.MAX_VALUE);
        constraints.setHgrow(Priority.ALWAYS);
        constraints.setFillWidth(true);
        return constraints;
    }

    private Node buildLeftColumn() {
        VBox left = new VBox(14, buildConfigCard(), buildTheoryCard());
        left.setFillWidth(true);
        return left;
    }

    private Node buildConfigCard() {
        DashboardCard card = new DashboardCard("CONFIGURACIÓN", "Configuración IPv4",
                "Introduce dos equipos y una máscara para saber si están en la misma red.");
        card.setStyle(cardStyle());
        card.setContent(new VBox(0,
                inputBlock("IP origen", sourceIpField, "/icons/pc.svg"),
                inputBlock("IP destino", destinationIpField, "/icons/server.svg"),
                maskBlock(),
                inputBlock("Gateway opcional", gatewayField, "/icons/router.svg"),
                ttlBlock(),
                primaryButton()
        ));
        return card;
    }

    private Node inputBlock(String labelText, TextField field, String icon) {
        Label label = fieldLabel(labelText);
        field.setMinHeight(42);
        field.setStyle(inputStyle());
        HBox box = new HBox(10, icon(icon, 20), field);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 12, 0, 12));
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8;"
                + "-fx-border-radius: 8; -fx-border-color: #d9e6f2;");
        HBox.setHgrow(field, Priority.ALWAYS);
        return new VBox(8, label, box);
    }

    private Node maskBlock() {
        Label label = fieldLabel("Máscara de subred");
        maskBox.setMinHeight(42);
        maskBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e6f2; -fx-border-radius: 8; -fx-background-radius: 8;");
        return new VBox(8, label, maskBox);
    }

    private Node ttlBlock() {
        Label label = fieldLabel("TTL inicial del paquete");
        ttlSpinner.setEditable(true);
        ttlSpinner.setMinHeight(42);
        ttlSpinner.setMaxWidth(Double.MAX_VALUE);
        ttlSpinner.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e6f2; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label hint = new Label("Prueba TTL 1 o 2 para ver el descarte.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px;");
        return new VBox(8, label, ttlSpinner, hint);
    }

    private Node primaryButton() {
        Button button = new Button("Calcular red");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(46);
        button.setStyle("-fx-background-color: #2f80ed; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8;");
        button.setOnAction(event -> calculateAndRender());
        VBox.setMargin(button, new Insets(22, 0, 0, 0));
        return button;
    }

    private Node buildTheoryCard() {
        DashboardCard card = new DashboardCard("CONCEPTOS", "Teoría esencial", null);
        card.setStyle(cardStyle());
        card.setContent(new VBox(18,
                theoryItem("/icons/ip.svg", "¿Qué es una IP?", "Identifica lógicamente a un dispositivo dentro de una red."),
                theoryItem("/icons/network.svg", "¿Qué es una red?", "Conjunto de direcciones que comparten la misma parte de red."),
                theoryItem("/icons/binary.svg", "¿Qué hace la máscara?", "Separa los bits de red de los bits disponibles para hosts."),
                theoryItem("/icons/info.svg", "¿Qué es TTL?", "Contador que baja en cada salto para que un paquete no circule para siempre."),
                theoryItem("/icons/check.svg", "¿Qué comprueba ping?", "Envía ICMP Echo Request y espera Echo Reply o un error útil."),
                theoryItem("/icons/router.svg", "¿Cómo elige ruta?", "Usa la tabla de rutas y gana la coincidencia más específica.")
        ));
        return card;
    }

    private Node theoryItem(String icon, String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390; -fx-line-spacing: 2;");
        VBox text = new VBox(4, titleLabel, bodyLabel);
        HBox row = new HBox(12, iconTile(icon), text);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node buildIp1CenterColumn() {
        VBox center = new VBox(14, buildHeroResult(), buildDiagramCard(), buildBottomGrid());
        center.setFillWidth(true);
        return center;
    }

    private Node buildHeroResult() {
        heroTitle.setStyle("-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: #132f4d;");
        heroBody.setWrapText(true);
        heroBody.setMaxWidth(780);
        heroBody.setStyle("-fx-text-fill: #5f7390; -fx-line-spacing: 2;");
        Label eyebrow = new Label("Resultado principal");
        eyebrow.setStyle("-fx-text-fill: #2f80ed; -fx-font-size: 12px; -fx-font-weight: 900;");
        VBox text = new VBox(6, eyebrow, heroTitle, heroBody);
        resultIcon.setMinSize(64, 64);
        resultIcon.setMaxSize(64, 64);
        HBox main = new HBox(18, resultIcon, text);
        main.setAlignment(Pos.CENTER_LEFT);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.setPadding(new Insets(18, 0, 0, 0));

        DashboardCard card = new DashboardCard(null, null, null);
        card.setHeaderVisible(false);
        card.setStyle(cardStyle());
        card.setContent(new VBox(main, badges));
        return card;
    }

    private Node buildDiagramCard() {
        DashboardCard card = new DashboardCard("MAPA", "Visualización de red", null);
        card.setStyle(cardStyle());
        HBox head = new HBox(diagramSummary);
        head.setAlignment(Pos.CENTER_RIGHT);
        diagramSummary.setStyle("-fx-text-fill: #5f7390; -fx-font-weight: 800;");

        GridPane diagram = new GridPane();
        diagram.setHgap(14);
        diagram.getColumnConstraints().setAll(growingColumn(180), fixedColumn(250), growingColumn(180));
        diagram.add(networkZone("origin", "Red origen", sourceZoneNetwork, "/icons/pc.svg", "PC Origen", sourceDeviceIp), 0, 0);
        diagram.add(gatewayNode(), 1, 0);
        diagram.add(networkZone("destination", "Red destino", destinationZoneNetwork, "/icons/server.svg", "Servidor", destinationDeviceIp), 2, 0);

        HBox path = new HBox(10,
                pathStep("1. El PC comprueba la máscara"),
                pathStep("2. Compara las redes"),
                pathStep("3. Decide directo o gateway"),
                pathStep("4. Entrega al destino")
        );
        path.setAlignment(Pos.CENTER);
        card.setContent(new VBox(14, head, diagram, path));
        return card;
    }

    private Node networkZone(String kind, String title, Label network, String icon, String deviceName, Label deviceIp) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #345573; -fx-font-weight: 800;");
        network.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        VBox zoneTitle = new VBox(4, titleLabel, network);
        zoneTitle.setAlignment(Pos.TOP_LEFT);

        Label name = new Label(deviceName);
        name.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        deviceIp.setStyle("-fx-text-fill: #5f7390;");
        VBox device = new VBox(8, icon(icon, 52), name, deviceIp);
        device.setAlignment(Pos.CENTER);
        device.setPadding(new Insets(18));
        device.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 14;"
                + "-fx-border-radius: 14; -fx-border-color: #d9e6f2;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.08), 18, 0.18, 0, 8);");

        BorderPane zone = new BorderPane();
        zone.setTop(zoneTitle);
        zone.setCenter(device);
        zone.setPadding(new Insets(18));
        zone.setMinHeight(300);
        String colors = "origin".equals(kind)
                ? "-fx-background-color: #f1fff7; -fx-border-color: #9be1bd;"
                : "-fx-background-color: #f2f7ff; -fx-border-color: #a9ccff;";
        zone.setStyle(colors + "-fx-background-radius: 14; -fx-border-radius: 14; -fx-border-style: dashed;");
        return zone;
    }

    private Node gatewayNode() {
        Line left = new Line(0, 0, 72, 0);
        left.setStroke(Color.web("#2f80ed"));
        left.setStrokeWidth(2);
        Line right = new Line(0, 0, 72, 0);
        right.setStroke(Color.web("#2f80ed"));
        right.setStrokeWidth(2);
        Label title = new Label("Gateway");
        title.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        gatewayIp.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f7390;");
        VBox router = new VBox(5, icon("/icons/router.svg", 46), title, gatewayIp);
        router.setAlignment(Pos.CENTER);
        router.setMinSize(138, 138);
        router.setMaxSize(138, 138);
        router.setStyle("-fx-background-color: #f6f0ff; -fx-background-radius: 999;"
                + "-fx-border-radius: 999; -fx-border-color: #cdb8ff;");
        HBox box = new HBox(0, left, router, right);
        box.setAlignment(Pos.CENTER);
        box.setMinHeight(300);
        return box;
    }

    private Node buildRoutingCard(boolean ttlFocus) {
        DashboardCard card = new DashboardCard(ttlFocus ? "TTL" : "RUTA",
                ttlFocus ? "Trayecto del paquete (TTL)" : "Escena de red",
                ttlFocus ? "Cada salto reduce el TTL. Si llega a 0, el paquete se descarta." : "Cómo sale el paquete de la red local.");
        card.setStyle(cardStyle());

        GridPane summary = new GridPane();
        summary.setHgap(10);
        summary.getColumnConstraints().setAll(growingColumn(160), growingColumn(160), growingColumn(160));
        summary.add(routeSummary("Siguiente salto", nextHopLabel, "/icons/router.svg"), 0, 0);
        summary.add(routeSummary("Interfaz usada", usedInterfaceLabel, "/icons/network.svg"), 1, 0);
        summary.add(routeSummary("Red destino", routeDestinationNetworkLabel, "/icons/server.svg"), 2, 0);

        ttlPacketBadge.setStyle(packetBadgeStyle("#eaf3ff", "#2f80ed"));
        HBox ttlRow = new HBox(10, ttlPacketBadge);
        ttlRow.setAlignment(Pos.CENTER);

        HBox scene = new HBox(8,
                routeDevice(clientRouteNode, "/icons/pc.svg", "Cliente"),
                routeLink(),
                routeDevice(switchRouteNode, "/icons/network.svg", "Switch"),
                routeLink(),
                routeDevice(routerRouteNode, "/icons/router.svg", "Router"),
                routeLink(),
                routeDevice(serverRouteNode, "/icons/server.svg", "Servidor")
        );
        scene.setAlignment(Pos.CENTER);
        scene.setPadding(new Insets(8, 0, 2, 0));

        routingStatusLabel.setWrapText(true);
        routingStatusLabel.setMaxWidth(Double.MAX_VALUE);
        ttlExpiredPanel.setWrapText(true);
        ttlExpiredPanel.setMaxWidth(Double.MAX_VALUE);

        GridPane details = new GridPane();
        details.setHgap(14);
        details.getColumnConstraints().setAll(growingColumn(300), growingColumn(300));

        DashboardCard timelineCard = new DashboardCard(null, "Timeline por saltos", null);
        timelineCard.setHeaderVisible(false);
        timelineCard.setStyle("-fx-background-color: #fbfdff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #edf3f9;");
        timelineCard.setContent(routingTimeline);

        DashboardCard tableCard = new DashboardCard(null, "Tabla de encaminamiento", null);
        tableCard.setHeaderVisible(false);
        tableCard.setStyle("-fx-background-color: #fbfdff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #edf3f9;");
        tableCard.setContent(routingTable);

        details.add(timelineCard, 0, 0);
        details.add(tableCard, 1, 0);
        ttlRow.setVisible(ttlFocus);
        ttlRow.setManaged(ttlFocus);
        ttlExpiredPanel.setVisible(ttlFocus);
        ttlExpiredPanel.setManaged(ttlFocus);
        details.setVisible(ttlFocus);
        details.setManaged(ttlFocus);
        card.setContent(new VBox(14, summary, ttlRow, scene, routingStatusLabel, ttlExpiredPanel, details));
        return card;
    }

    private Node buildIp2RoutingCard() {
        DashboardCard card = new DashboardCard("RUTA", "Escena de red", "Cliente → Switch → Router → Servidor");
        card.setStyle(cardStyle());

        HBox scene = new HBox(8,
                routeDevice(new StackPane(), "/icons/pc.svg", "Cliente"),
                routeLink(),
                routeDevice(new StackPane(), "/icons/network.svg", "Switch"),
                routeLink(),
                routeDevice(new StackPane(), "/icons/router.svg", "Router"),
                routeLink(),
                routeDevice(new StackPane(), "/icons/server.svg", "Servidor")
        );
        scene.setAlignment(Pos.CENTER);
        scene.setPadding(new Insets(18));
        scene.setStyle("-fx-background-color: #fbfdff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #d9e6f2;");

        GridPane summary = new GridPane();
        summary.setHgap(14);
        summary.getColumnConstraints().setAll(growingColumn(220), growingColumn(220));
        summary.add(routeSummary("Decisión de envío", ip2StatusLabel, "/icons/check.svg"), 0, 0);
        summary.add(new VBox(10,
                routeSummary("Siguiente salto", ip2NextHopLabel, "/icons/router.svg"),
                routeSummary("Interfaz de salida", ip2UsedInterfaceLabel, "/icons/network.svg"),
                routeSummary("Red destino", ip2DestinationNetworkLabel, "/icons/server.svg")
        ), 1, 0);

        card.setContent(new VBox(16, scene, summary));
        return card;
    }

    private Node buildRoutingTableCard() {
        DashboardCard card = new DashboardCard("TABLA", "Detalle por saltos", null);
        card.setStyle(cardStyle());
        card.setContent(ip2RoutingRows);
        return card;
    }

    private Node buildTrajectoryCard() {
        DashboardCard card = new DashboardCard("PASO A PASO", "Trayecto del paquete", null);
        card.setStyle(cardStyle());
        card.setContent(ip2TrajectoryRows);
        return card;
    }

    private Node buildTtlTheoryGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.getColumnConstraints().setAll(growingColumn(300), growingColumn(300));
        DashboardCard ttl = new DashboardCard("TTL", "¿Qué es TTL?", null);
        ttl.setStyle(cardStyle());
        ttl.setContent(theoryText("TTL limita cuántos saltos puede atravesar un paquete. En cada salto baja en 1."));
        DashboardCard loops = new DashboardCard("LOOPS", "¿Por qué evita loops?", null);
        loops.setStyle(cardStyle());
        loops.setContent(theoryText("Si la ruta tiene un bucle, el TTL llega a 0 y el paquete se descarta antes de circular para siempre."));
        grid.add(ttl, 0, 0);
        grid.add(loops, 1, 0);
        return grid;
    }

    private Node buildTtlPacketCard() {
        DashboardCard card = new DashboardCard("PAQUETE", "Paquete en tránsito", null);
        card.setStyle(cardStyle());
        VBox packet = new VBox(12,
                routeSummary("IP origen", ttlPacketSourceLabel, "/icons/pc.svg"),
                routeSummary("IP destino", ttlPacketDestinationLabel, "/icons/server.svg"),
                resultRow("Protocolo", "ICMP (Ping)"),
                routeSummary("TTL actual", ttlPacketCurrentLabel, "/icons/info.svg"),
                ttlPacketDetailPanel
        );
        card.setContent(packet);
        return card;
    }

    private Node theoryText(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #5f7390; -fx-line-spacing: 3;");
        return label;
    }

    private Node routeSummary(String title, Label value) {
        return routeSummary(title, value, null);
    }

    private Node routeSummary(String title, Label value, String iconPath) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        value.setWrapText(true);
        value.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        Node titleNode = iconPath == null ? titleLabel : new HBox(8, icon(iconPath, 16), titleLabel);
        if (titleNode instanceof HBox titleRow) {
            titleRow.setAlignment(Pos.CENTER_LEFT);
        }
        VBox box = new VBox(4, titleNode, value);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d9e6f2;");
        return box;
    }

    private Node routeDevice(StackPane stateNode, String icon, String title) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        stateNode.getChildren().setAll(icon(icon, 34));
        stateNode.setMinSize(60, 60);
        stateNode.setMaxSize(60, 60);
        VBox box = new VBox(7, stateNode, label);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(112);
        return box;
    }

    private Node routeLink() {
        Line line = new Line(0, 0, 58, 0);
        line.setStroke(Color.web("#9bb3ca"));
        line.setStrokeWidth(2);
        return line;
    }

    private Label pathStep(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-background-color: #f8fbff; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 10; -fx-border-radius: 10;"
                + "-fx-padding: 12; -fx-text-fill: #345573; -fx-font-weight: 800;");
        HBox.setHgrow(label, Priority.ALWAYS);
        return label;
    }

    private Node buildBottomGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.getColumnConstraints().setAll(growingColumn(420), growingColumn(300));

        DashboardCard binary = new DashboardCard(null, "Vista binaria simplificada", "Red vs host");
        binary.setStyle(cardStyle());
        binary.setContent(binaryRows);

        DashboardCard decision = new DashboardCard(null, "Decisión del equipo origen", null);
        decision.setStyle(cardStyle());
        decision.setContent(decisionBox);

        grid.add(binary, 0, 0);
        grid.add(decision, 1, 0);
        return grid;
    }

    private Node buildRightColumn() {
        DashboardCard results = new DashboardCard("RESULTADOS", "Resultados", "Cálculo automático de red y hosts.");
        results.setStyle(cardStyle());
        results.setContent(resultsTable);

        DashboardCard feedback = new DashboardCard("ESTADO", "Feedback visual", null);
        feedback.setStyle(cardStyle());
        feedback.setContent(feedbackBox);

        DashboardCard classroom = new DashboardCard("CLASE", "Para explicar en clase", null);
        classroom.setStyle(cardStyle());
        classroom.setContent(classroomList);

        return new VBox(14, results, feedback, classroom);
    }

    private Node buildLegend() {
        HBox legend = new HBox(26,
                legendTitle("Leyenda rápida"),
                legendDot("#2f80ed", "Equipo origen"),
                legendDot("#23415f", "Equipo destino"),
                legendDot("#8a55e6", "Gateway / Router"),
                legendDot("#19a663", "Resultado correcto"),
                legendDot("#ff4d4f", "Red diferente"),
                note()
        );
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(18, 24, 18, 24));
        legend.setStyle(cardStyle());
        VBox.setMargin(legend, new Insets(0, 14, 0, 14));
        return legend;
    }

    private Node note() {
        HBox note = new HBox(8, icon("/icons/info.svg", 18), new Label("Un equipo solo envía directo si destino y origen están en la misma red."));
        note.setAlignment(Pos.CENTER_LEFT);
        note.setPadding(new Insets(12, 16, 12, 16));
        note.setStyle("-fx-background-color: #f6faff; -fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10;");
        HBox.setHgrow(note, Priority.ALWAYS);
        return note;
    }

    private Label legendTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #0064ff; -fx-font-weight: 900;");
        return label;
    }

    private Node legendDot(String color, String text) {
        Circle dot = new Circle(6.5, Color.web(color));
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #486581; -fx-font-size: 13px;");
        return new HBox(8, dot, label);
    }

    private void addImmediateFeedback() {
        sourceIpField.textProperty().addListener((obs, old, value) -> calculateAndRender());
        destinationIpField.textProperty().addListener((obs, old, value) -> calculateAndRender());
        gatewayField.textProperty().addListener((obs, old, value) -> calculateAndRender());
        ttlSpinner.valueProperty().addListener((obs, old, value) -> calculateAndRender());
        maskBox.valueProperty().addListener((obs, old, value) -> calculateAndRender());
    }

    private void showSprint(IpSprint sprint) {
        activeSprint = sprint;
        if (ip1Screen != null) {
            ip1Screen.setVisible(sprint == IpSprint.IP1);
            ip1Screen.setManaged(sprint == IpSprint.IP1);
        }
        if (ip2Screen != null) {
            ip2Screen.setVisible(sprint == IpSprint.IP2);
            ip2Screen.setManaged(sprint == IpSprint.IP2);
        }
        if (ip3Screen != null) {
            ip3Screen.setVisible(sprint == IpSprint.IP3);
            ip3Screen.setManaged(sprint == IpSprint.IP3);
        }
        if (ip4Screen != null) {
            ip4Screen.setVisible(sprint == IpSprint.IP4);
            ip4Screen.setManaged(sprint == IpSprint.IP4);
        }
        if (ip5Screen != null) {
            ip5Screen.setVisible(sprint == IpSprint.IP5);
            ip5Screen.setManaged(sprint == IpSprint.IP5);
        }
        if (ip6Screen != null) {
            ip6Screen.setVisible(sprint == IpSprint.IP6);
            ip6Screen.setManaged(sprint == IpSprint.IP6);
        }
        styleSprintTab(ip1Tab, sprint == IpSprint.IP1, "#087f4f");
        styleSprintTab(ip2Tab, sprint == IpSprint.IP2, "#2f80ed");
        styleSprintTab(ip3Tab, sprint == IpSprint.IP3, "#b45309");
        styleSprintTab(ip4Tab, sprint == IpSprint.IP4, "#8a55e6");
        styleSprintTab(ip5Tab, sprint == IpSprint.IP5, "#ff8b1a");
        styleSprintTab(ip6Tab, sprint == IpSprint.IP6, "#0f9f8f");
    }

    private void styleSprintTab(Button tab, boolean active, String accent) {
        tab.setMinHeight(38);
        tab.setStyle("-fx-background-color: " + (active ? "#ffffff" : "transparent") + ";"
                + "-fx-border-color: " + (active ? "#d9e6f2" : "transparent") + ";"
                + "-fx-background-radius: 10; -fx-border-radius: 10;"
                + (active ? "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.08), 12, 0.18, 0, 4);" : "")
                + "-fx-text-fill: " + (active ? accent : "#173452") + ";"
                + "-fx-font-weight: 900; -fx-padding: 0 14 0 14;");
    }

    private void calculateAndRender() {
        try {
            int cidr = maskBox.getValue() == null ? 24 : maskBox.getValue().cidr;
            Ipv4SubnetResult result = Ipv4SubnetCalculator.calculate(sourceIpField.getText(), destinationIpField.getText(), cidr);
            validateGatewayIfPresent();
            renderResult(result);
            readyPill.setText("Listo para calcular");
            readyPill.setStyle(pillStyle("#e7f8ef", "#087f4f"));
        } catch (IllegalArgumentException ex) {
            renderError(ex.getMessage());
        }
    }

    private void validateGatewayIfPresent() {
        if (!gatewayField.getText().isBlank()) {
            Ipv4SubnetCalculator.parseIpv4(gatewayField.getText());
        }
    }

    private void renderResult(Ipv4SubnetResult result) {
        boolean same = result.sameNetwork();
        resultIcon.getChildren().setAll(icon(same ? "/icons/check.svg" : "/icons/x.svg", 34));
        resultIcon.setStyle("-fx-background-color: " + (same ? "#e7f8ef" : "#fff0f0") + "; -fx-background-radius: 18;");
        heroTitle.setText(same ? "Sí están en la misma red" : "No están en la misma red");
        heroBody.setText(same
                ? "El origen y el destino comparten la misma parte de red. El equipo puede enviar el paquete directamente al destino."
                : "El origen y el destino pertenecen a subredes diferentes. Para comunicarse, el equipo origen necesita usar el gateway.");
        badges.getChildren().setAll(
                badge("Misma red: " + (same ? "Sí" : "No"), same ? "#e7f8ef" : "#fff0f0", same ? "#087f4f" : "#ff4d4f"),
                badge("Necesita router: " + (result.needsRouter() ? "Sí" : "No"), result.needsRouter() ? "#e7f8ef" : "#fff7e8", result.needsRouter() ? "#087f4f" : "#b45309"),
                badge("Máscara: /" + result.cidr(), "#eaf3ff", "#2f80ed")
        );

        diagramSummary.setText(result.sourceNetwork() + "/" + result.cidr() + (same ? " = " : " ≠ ") + result.destinationNetwork() + "/" + result.cidr());
        sourceZoneNetwork.setText(result.sourceNetwork() + " /" + result.cidr());
        destinationZoneNetwork.setText(result.destinationNetwork() + " /" + result.cidr());
        sourceDeviceIp.setText(result.sourceIp());
        destinationDeviceIp.setText(result.destinationIp());
        gatewayIp.setText(gatewayField.getText().isBlank() ? "Sin gateway" : gatewayField.getText().trim());

        resultsTable.getChildren().setAll(
                resultRow("Red origen", result.sourceNetwork()),
                resultRow("Broadcast origen", result.sourceBroadcast()),
                resultRow("Rango origen", result.sourceHostRange()),
                resultRow("Red destino", result.destinationNetwork()),
                resultRow("Broadcast destino", result.destinationBroadcast()),
                resultRow("Rango destino", result.destinationHostRange())
        );
        feedbackBox.getChildren().setAll(
                feedback(same, same ? "Sí están en la misma red" : "No están en la misma red",
                        same ? "La parte de red coincide con la máscara /" + result.cidr() + "."
                                : firstDifferenceText(result)),
                feedback(!result.needsRouter(), result.needsRouter() ? "Sí necesita router" : "No necesita router",
                        result.needsRouter() ? "El paquete debe salir por el gateway." : "El paquete puede ir directo al destino.")
        );
        classroomList.getChildren().setAll(classroomLines(result));
        binaryRows.getChildren().setAll(binaryRow("IP origen", result.sourceIp(), result.destinationIp(), result.cidr(), false),
                binaryRow("IP destino", result.destinationIp(), result.sourceIp(), result.cidr(), true),
                binaryLegend());
        decisionBox.getChildren().setAll(
                decisionStep("Calcular red origen → " + result.sourceNetwork(), true),
                decisionStep("Calcular red destino → " + result.destinationNetwork(), same),
                arrow(),
                decisionFinal(same ? "Misma red → enviar directo" : "Redes distintas → enviar al gateway")
        );
        renderRouting(result);
    }

    private void renderRouting(Ipv4SubnetResult result) {
        String gateway = gatewayField.getText().trim();
        boolean same = result.sameNetwork();
        boolean hasGateway = !gateway.isBlank();
        boolean gatewayInSourceNetwork = hasGateway && Ipv4SubnetCalculator.calculate(result.sourceIp(), gateway, result.cidr()).sameNetwork();
        String destinationNetwork = result.destinationNetwork() + "/" + result.cidr();
        String nextHop = same ? result.destinationIp() : hasGateway ? gateway : "Gateway no configurado";
        String usedInterface = same
                ? "NIC local → " + result.sourceNetwork() + "/" + result.cidr()
                : "NIC local → gateway " + (hasGateway ? gateway : "pendiente");
        int initialTtl = ttlSpinner.getValue();
        List<RouteHop> hops = routeHops(result, nextHop, hasGateway, gatewayInSourceNetwork);
        TtlTrace ttlTrace = traceTtl(initialTtl, hops);

        nextHopLabel.setText(nextHop);
        usedInterfaceLabel.setText(usedInterface);
        routeDestinationNetworkLabel.setText(destinationNetwork);
        ttlPacketBadge.setText(ttlTrace.packetBadge());
        ttlPacketBadge.setStyle(packetBadgeStyle(ttlTrace.expired() ? "#fff0f0" : "#eaf3ff", ttlTrace.expired() ? "#b91c1c" : "#2f80ed"));
        ttlPacketSourceLabel.setText(result.sourceIp());
        ttlPacketDestinationLabel.setText(result.destinationIp());
        ttlPacketCurrentLabel.setText(ttlTrace.finalTtl() + " saltos restantes");

        if (ttlTrace.expired()) {
            routingStatusLabel.setText("TTL expirado: el paquete se descarta en " + ttlTrace.expiredAt() + " antes de llegar al destino.");
            routingStatusLabel.setStyle(routeStatusStyle("#fff0f0", "#b91c1c"));
        } else if (same) {
            routingStatusLabel.setText("Misma red: el cliente usa ARP para encontrar al destino y el switch lo entrega dentro de la LAN. El router no participa.");
            routingStatusLabel.setStyle(routeStatusStyle("#e7f8ef", "#087f4f"));
        } else if (!hasGateway) {
            routingStatusLabel.setText("Red distinta: el cliente necesita un gateway. Sin gateway configurado no sabe a qué router entregar el paquete.");
            routingStatusLabel.setStyle(routeStatusStyle("#fff0f0", "#b91c1c"));
        } else if (!gatewayInSourceNetwork) {
            routingStatusLabel.setText("Gateway fuera de la red origen: el cliente tampoco puede llegar a esa puerta de salida directamente.");
            routingStatusLabel.setStyle(routeStatusStyle("#fff7e8", "#b45309"));
        } else {
            routingStatusLabel.setText("Red distinta: el cliente entrega el paquete al gateway. El router lo recibe por la red origen y lo reenvía hacia la red destino.");
            routingStatusLabel.setStyle(routeStatusStyle("#e7f8ef", "#087f4f"));
        }
        ttlExpiredPanel.setText(ttlTrace.expired()
                ? "TTL expirado: al llegar a 0, el dispositivo descarta el paquete. Por eso un paquete puede no llegar nunca aunque la ruta exista."
                : "TTL correcto: el paquete conserva TTL después del recorrido visual.");
        ttlExpiredPanel.setStyle(routeStatusStyle(ttlTrace.expired() ? "#fff0f0" : "#f8fbff", ttlTrace.expired() ? "#b91c1c" : "#345573"));
        ttlPacketDetailPanel.setText(ttlTrace.expired()
                ? "Evento: TTL expirado en " + ttlTrace.expiredAt() + ". Acción: paquete descartado."
                : "Estado: paquete reenviado correctamente. TTL final: " + ttlTrace.finalTtl() + ".");
        ttlPacketDetailPanel.setStyle(routeStatusStyle(ttlTrace.expired() ? "#fff0f0" : "#e7f8ef", ttlTrace.expired() ? "#b91c1c" : "#087f4f"));

        styleRouteNode(clientRouteNode, "#eaf3ff", "#2f80ed");
        styleRouteNode(switchRouteNode, "#f0f6ff", "#5f7390");
        styleRouteNode(routerRouteNode, same ? "#f8fbff" : "#f6f0ff", same ? "#cbd6e2" : "#8a55e6");
        styleRouteNode(serverRouteNode, "#f2f7ff", "#23415f");

        routingTimeline.getChildren().setAll(routingSteps(result, nextHop, hasGateway, gatewayInSourceNetwork, initialTtl));
        routingTable.getChildren().setAll(routingRows(result, nextHop, hasGateway, gatewayInSourceNetwork));
        renderIp2(result, nextHop, usedInterface, hasGateway, gatewayInSourceNetwork);
        icmpPingView.updateContext(result, gateway, initialTtl);
        headerInspectorView.updateContext(result, initialTtl);
        routeTableView.updateContext(result, gateway);
    }

    private void renderIp2(Ipv4SubnetResult result, String nextHop, String usedInterface, boolean hasGateway, boolean gatewayInSourceNetwork) {
        ip2NextHopLabel.setText(nextHop);
        ip2UsedInterfaceLabel.setText(usedInterface);
        ip2DestinationNetworkLabel.setText(result.destinationNetwork() + "/" + result.cidr());
        if (result.sameNetwork()) {
            ip2StatusLabel.setText("Directo: origen y destino están en la misma red. El router no participa.");
            ip2StatusLabel.setStyle("-fx-text-fill: #087f4f; -fx-font-weight: 900;");
        } else if (!hasGateway) {
            ip2StatusLabel.setText("Bloqueado: falta gateway para salir de la red local.");
            ip2StatusLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 900;");
        } else if (!gatewayInSourceNetwork) {
            ip2StatusLabel.setText("Aviso: el gateway no pertenece a la red origen.");
            ip2StatusLabel.setStyle("-fx-text-fill: #b45309; -fx-font-weight: 900;");
        } else {
            ip2StatusLabel.setText("Gateway: el cliente envía el paquete a " + nextHop + " para alcanzar otra red.");
            ip2StatusLabel.setStyle("-fx-text-fill: #087f4f; -fx-font-weight: 900;");
        }
        ip2RoutingRows.getChildren().setAll(routeTableRows(result, nextHop, hasGateway, gatewayInSourceNetwork));
        ip2TrajectoryRows.getChildren().setAll(trajectoryRows(result, nextHop, hasGateway, gatewayInSourceNetwork));
    }

    private Node[] routingSteps(Ipv4SubnetResult result, String nextHop, boolean hasGateway, boolean gatewayInSourceNetwork, int initialTtl) {
        List<RouteHop> hops = routeHops(result, nextHop, hasGateway, gatewayInSourceNetwork);
        TtlTrace trace = traceTtl(initialTtl, hops);
        return hops.stream()
                .limit(trace.visibleHops())
                .map(hop -> timelineStep(hop.number(), hop.device(), hop.body(), hop.ttlBefore(), hop.ttlAfter(), hop.ttlAfter() == 0))
                .toArray(Node[]::new);
    }

    private List<RouteHop> routeHops(Ipv4SubnetResult result, String nextHop, boolean hasGateway, boolean gatewayInSourceNetwork) {
        if (result.sameNetwork()) {
            return List.of(
                    new RouteHop("1", "Cliente", "Detecta que " + result.destinationIp() + " está en " + result.sourceNetwork() + "/" + result.cidr() + "."),
                    new RouteHop("2", "Switch", "Conmuta la trama dentro de la red local."),
                    new RouteHop("3", "Servidor", "Recibe el paquete sin pasar por router.")
            );
        }
        if (!hasGateway || !gatewayInSourceNetwork) {
            return List.of(
                    new RouteHop("1", "Cliente", "Detecta una red distinta: " + result.destinationNetwork() + "/" + result.cidr() + "."),
                    new RouteHop("2", "Gateway", hasGateway ? "El gateway configurado no está alcanzable desde la red origen." : "Falta configurar la puerta de enlace."),
                    new RouteHop("3", "Entrega", "El paquete no puede salir correctamente de la LAN.")
            );
        }
        return List.of(
                new RouteHop("1", "Cliente", "Red destino distinta: prepara el paquete para " + result.destinationIp() + "."),
                new RouteHop("2", "Switch", "Entrega la trama al gateway " + nextHop + "."),
                new RouteHop("3", "Router", "TTL baja y el router reenvía hacia " + result.destinationNetwork() + "/" + result.cidr() + "."),
                new RouteHop("4", "Servidor", "Recibe el paquete desde su red.")
        );
    }

    private Node[] routingRows(Ipv4SubnetResult result, String nextHop, boolean hasGateway, boolean gatewayInSourceNetwork) {
        return routeTableRows(result, nextHop, hasGateway, gatewayInSourceNetwork);
    }

    private Node[] routeTableRows(Ipv4SubnetResult result, String nextHop, boolean hasGateway, boolean gatewayInSourceNetwork) {
        if (result.sameNetwork()) {
            return new Node[] {
                    tableHeader(),
                    routeRow("1", "Cliente", result.destinationIp()),
                    routeRow("2", "Switch", result.destinationIp()),
                    routeRow("3", "Servidor", "Entrega final")
            };
        }
        if (!hasGateway || !gatewayInSourceNetwork) {
            return new Node[] {
                    tableHeader(),
                    routeRow("1", "Cliente", nextHop),
                    routeRow("2", "Switch", hasGateway ? "Gateway no alcanzable" : "Sin gateway"),
                    routeRow("3", "Router", "No se usa")
            };
        }
        return new Node[] {
                tableHeader(),
                routeRow("1", "Cliente", nextHop),
                routeRow("2", "Switch", nextHop),
                routeRow("3", "Router", result.destinationNetwork() + "/" + result.cidr()),
                routeRow("4", "Servidor", "Entrega final")
        };
    }

    private Node[] trajectoryRows(Ipv4SubnetResult result, String nextHop, boolean hasGateway, boolean gatewayInSourceNetwork) {
        if (result.sameNetwork()) {
            return new Node[] {
                    trajectoryStep("1", "Cliente", result.sourceIp(), "Detecta que el destino está en su propia red.", true),
                    trajectoryStep("2", "Switch", "", "Reenvía dentro de la LAN.", true),
                    trajectoryStep("3", "Servidor", result.destinationIp(), "El paquete llega directamente.", true)
            };
        }
        if (!hasGateway || !gatewayInSourceNetwork) {
            return new Node[] {
                    trajectoryStep("1", "Cliente", result.sourceIp(), "Detecta que el destino está en otra red.", true),
                    trajectoryStep("2", "Gateway", nextHop, hasGateway ? "No es alcanzable desde la red origen." : "No hay puerta de enlace configurada.", false),
                    trajectoryStep("3", "Servidor", result.destinationIp(), "No recibe el paquete.", false)
            };
        }
        return new Node[] {
                trajectoryStep("1", "Cliente", result.sourceIp(), "Envía el paquete a su gateway predeterminado.", true),
                trajectoryStep("2", "Switch", "", "Reenvía la trama hacia el router.", true),
                trajectoryStep("3", "Router", nextHop, "Encamina el paquete hacia la red destino.", true),
                trajectoryStep("4", "Servidor", result.destinationIp(), "El paquete llega correctamente.", true)
        };
    }

    private Node trajectoryStep(String number, String title, String ip, String body, boolean ok) {
        Label numberLabel = new Label(number);
        numberLabel.setAlignment(Pos.CENTER);
        numberLabel.setMinSize(32, 32);
        numberLabel.setMaxSize(32, 32);
        numberLabel.setStyle("-fx-background-color: " + (ok ? "#17a765" : "#fff0f0") + ";"
                + "-fx-text-fill: " + (ok ? "#ffffff" : "#b91c1c") + ";"
                + "-fx-background-radius: 999; -fx-font-weight: 900;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label ipLabel = new Label(ip);
        ipLabel.setVisible(!ip.isBlank());
        ipLabel.setManaged(!ip.isBlank());
        ipLabel.setStyle("-fx-text-fill: #294766; -fx-font-weight: 800;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390;");
        Label state = new Label(ok ? "OK" : "STOP");
        state.setStyle(packetBadgeStyle(ok ? "#e7f8ef" : "#fff0f0", ok ? "#087f4f" : "#b91c1c"));
        HBox row = new HBox(12, numberLabel, new VBox(3, titleLabel, ipLabel, bodyLabel), state);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: #fbfdff; -fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10;");
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        return row;
    }

    private Node timelineStep(String number, String title, String body) {
        return timelineStep(number, title, body, -1, -1, false);
    }

    private Node timelineStep(String number, String title, String body, int ttlBefore, int ttlAfter, boolean expired) {
        Label badge = new Label(number);
        badge.setAlignment(Pos.CENTER);
        badge.setMinSize(30, 30);
        badge.setMaxSize(30, 30);
        badge.setStyle("-fx-background-color: " + (expired ? "#fff0f0" : "#eaf3ff") + "; -fx-background-radius: 999;"
                + "-fx-text-fill: " + (expired ? "#b91c1c" : "#2f80ed") + "; -fx-font-weight: 900;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390;");
        VBox text = new VBox(2, titleLabel, bodyLabel);
        if (ttlBefore >= 0) {
            Label ttl = new Label("TTL: " + ttlBefore + " → " + ttlAfter + (expired ? " · descartado" : ""));
            ttl.setStyle(packetBadgeStyle(expired ? "#fff0f0" : "#f8fbff", expired ? "#b91c1c" : "#345573"));
            text.getChildren().add(ttl);
        }
        HBox row = new HBox(10, badge, text);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node tableHeader() {
        HBox row = new HBox(8, tableCell("Salto", 46, true), tableCell("Dispositivo", 112, true), tableCell("Siguiente IP", 160, true));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node routeRow(String hop, String device, String nextIp) {
        HBox row = new HBox(8, tableCell(hop, 46, false), tableCell(device, 112, false), tableCell(nextIp, 160, false));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));
        return row;
    }

    private Label tableCell(String text, double width, boolean header) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(header
                ? "-fx-text-fill: #5f7390; -fx-font-size: 11px; -fx-font-weight: 900;"
                : "-fx-text-fill: #173452; -fx-font-weight: 800;");
        return label;
    }

    private void styleRouteNode(StackPane node, String background, String border) {
        node.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 12;"
                + "-fx-border-color: " + border + "; -fx-border-radius: 12;");
    }

    private String routeStatusStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 10; -fx-padding: 12; -fx-font-weight: 800;";
    }

    private String packetBadgeStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #d9e6f2;"
                + "-fx-padding: 7 12 7 12; -fx-font-weight: 900;";
    }

    private void renderError(String message) {
        readyPill.setText("Revisa los datos");
        readyPill.setStyle(pillStyle("#fff0f0", "#b91c1c"));
        resultIcon.getChildren().setAll(icon("/icons/x.svg", 34));
        resultIcon.setStyle("-fx-background-color: #fff0f0; -fx-background-radius: 18;");
        heroTitle.setText("Datos IPv4 no válidos");
        heroBody.setText(message);
        badges.getChildren().setAll(badge("Corrige IP o gateway", "#fff0f0", "#b91c1c"));
        nextHopLabel.setText("-");
        usedInterfaceLabel.setText("-");
        routeDestinationNetworkLabel.setText("-");
        ttlPacketBadge.setText("TTL: -");
        ip2NextHopLabel.setText("-");
        ip2UsedInterfaceLabel.setText("-");
        ip2DestinationNetworkLabel.setText("-");
        ip2StatusLabel.setText("No se puede construir la ruta hasta corregir los datos IPv4.");
        ttlPacketSourceLabel.setText("-");
        ttlPacketDestinationLabel.setText("-");
        ttlPacketCurrentLabel.setText("-");
        routingStatusLabel.setText("No se puede construir la ruta hasta corregir los datos IPv4.");
        routingStatusLabel.setStyle(routeStatusStyle("#fff0f0", "#b91c1c"));
        ttlExpiredPanel.setText("TTL expirado: no se evalúa hasta corregir IP, máscara y gateway.");
        ttlExpiredPanel.setStyle(routeStatusStyle("#fff0f0", "#b91c1c"));
        ttlPacketDetailPanel.setText("Corrige los datos para ver TTL y descarte.");
        ttlPacketDetailPanel.setStyle(routeStatusStyle("#fff0f0", "#b91c1c"));
        routingTimeline.getChildren().setAll(timelineStep("!", "Datos no válidos", message));
        routingTable.getChildren().setAll(tableHeader(), routeRow("-", "-", "-"));
        ip2RoutingRows.getChildren().setAll(tableHeader(), routeRow("-", "-", "-"));
        ip2TrajectoryRows.getChildren().setAll(trajectoryStep("!", "Datos no válidos", "", message, false));
        icmpPingView.renderError(message);
        headerInspectorView.renderError(message);
        routeTableView.renderError(message);
    }

    private Node[] classroomLines(Ipv4SubnetResult result) {
        return new Node[] {
                bullet("La máscara /" + result.cidr() + " usa " + result.cidr() + " bits para la red."),
                bullet(result.sourceIp() + " pertenece a " + result.sourceNetwork() + "/" + result.cidr() + "."),
                bullet(result.destinationIp() + " pertenece a " + result.destinationNetwork() + "/" + result.cidr() + "."),
                bullet(result.sameNetwork() ? "Si las redes son iguales, no hace falta gateway." : "Si las redes son distintas, se necesita gateway.")
        };
    }

    private Node binaryRow(String title, String ip, String compareWith, int cidr, boolean markDifferences) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        HBox bits = new HBox(8);
        String[] current = Ipv4SubnetCalculator.toBinaryOctets(ip).split("\\.");
        String[] other = Ipv4SubnetCalculator.toBinaryOctets(compareWith).split("\\.");
        int networkOctets = cidr / 8;
        for (int i = 0; i < current.length; i++) {
            boolean network = i < networkOctets;
            boolean different = markDifferences && network && !current[i].equals(other[i]);
            bits.getChildren().add(bit(current[i], network, different));
        }
        return new VBox(8, label, bits);
    }

    private Label bit(String value, boolean network, boolean different) {
        Label label = new Label(value);
        String bg = network ? "#f0f6ff" : "#fff7e8";
        String border = network ? "#bcd6ff" : "#ffd092";
        String text = "#173452";
        if (different) {
            bg = "#fff0f0";
            border = "#ffb9b9";
            text = "#ff4d4f";
        }
        label.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-padding: 9 11 9 11;"
                + "-fx-background-color: " + bg + "; -fx-border-color: " + border + ";"
                + "-fx-background-radius: 7; -fx-border-radius: 7; -fx-text-fill: " + text + ";");
        return label;
    }

    private Node binaryLegend() {
        return new HBox(16,
                legendDot("#2f80ed", "Bits de red"),
                legendDot("#ff8b1a", "Bits de host"),
                legendDot("#ff4d4f", "Diferencia")
        );
    }

    private String firstDifferenceText(Ipv4SubnetResult result) {
        String[] source = result.sourceNetwork().split("\\.");
        String[] destination = result.destinationNetwork().split("\\.");
        for (int i = 0; i < source.length; i++) {
            if (!source[i].equals(destination[i])) {
                return "Cambia el octeto " + (i + 1) + " con máscara /" + result.cidr() + ".";
            }
        }
        return "La parte de red no coincide con la máscara seleccionada.";
    }

    private Node resultRow(String label, String value) {
        Label key = new Label(label);
        key.setStyle("-fx-text-fill: #5f7390; -fx-font-weight: 700;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        HBox row = new HBox(8, key, val);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(11));
        row.setStyle("-fx-background-color: #fbfdff; -fx-border-color: #edf3f9; -fx-background-radius: 8; -fx-border-radius: 8;");
        key.setPrefWidth(145);
        return row;
    }

    private Node feedback(boolean positive, String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390;");
        HBox box = new HBox(12, icon(positive ? "/icons/check.svg" : "/icons/x.svg", 32), new VBox(3, titleLabel, bodyLabel));
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: " + (positive ? "#e7f8ef" : "#fff0f0") + "; -fx-background-radius: 10;");
        return box;
    }

    private Node bullet(String text) {
        Label label = new Label("• " + text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #345573; -fx-line-spacing: 3;");
        return label;
    }

    private Node decisionStep(String text, boolean ok) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-padding: 14; -fx-font-weight: 900; -fx-background-radius: 10;"
                + "-fx-background-color: " + (ok ? "#e7f8ef" : "#fff0f0") + ";"
                + "-fx-text-fill: " + (ok ? "#087f4f" : "#ff4d4f") + ";");
        return label;
    }

    private Node arrow() {
        Label label = new Label("↓");
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: #5f7390;");
        return label;
    }

    private Node decisionFinal(String text) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        label.setStyle("-fx-padding: 14; -fx-font-weight: 900; -fx-background-radius: 10;"
                + "-fx-background-color: #eaf3ff; -fx-text-fill: #2f80ed;");
        return label;
    }

    private Label badge(String text, String background, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-font-weight: 900; -fx-background-radius: 999; -fx-padding: 8 12 8 12;");
        return label;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(16, 0, 0, 0));
        label.setStyle("-fx-text-fill: #193753; -fx-font-weight: 800;");
        return label;
    }

    private Node iconTile(String path) {
        StackPane pane = new StackPane(icon(path, 24));
        pane.setMinSize(36, 36);
        pane.setMaxSize(36, 36);
        pane.setStyle("-fx-background-color: #f0f6ff; -fx-background-radius: 9;");
        return pane;
    }

    private Node icon(String path, double size) {
        Image image = load(path);
        if (image == null) {
            Rectangle fallback = new Rectangle(size, size, Color.web("#2f80ed"));
            fallback.setArcWidth(6);
            fallback.setArcHeight(6);
            return fallback;
        }
        ImageView view = new ImageView(image);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        return view;
    }

    private Image load(String resourcePath) {
        if (resourcePath.endsWith(".svg")) {
            Image png = load(resourcePath.substring(0, resourcePath.length() - 4) + ".png");
            if (png != null) {
                return png;
            }
            return null;
        }
        try (InputStream stream = Ipv4LearningView.class.getResourceAsStream(resourcePath)) {
            return stream == null ? null : new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Button ghostButton(String text, String iconPath) {
        Button button = new Button(text);
        button.setGraphic(icon(iconPath, 16));
        button.setGraphicTextGap(8);
        button.setMinHeight(38);
        button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-text-fill: #173452; -fx-font-weight: 800; -fx-padding: 0 14 0 14;");
        return button;
    }

    private Label pill(String text, String background, String color) {
        Label label = new Label(text);
        label.setStyle(pillStyle(background, color));
        return label;
    }

    private String pillStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-padding: 7 16 7 16; -fx-font-weight: 900;";
    }

    private String inputStyle() {
        return "-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #1f3b57;";
    }

    private String cardStyle() {
        return "-fx-background-color: #ffffff; -fx-background-radius: 14;"
                + "-fx-border-radius: 14; -fx-border-color: #d9e6f2;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.035), 22, 0.18, 0, 8);";
    }

    private TtlTrace traceTtl(int initialTtl, List<RouteHop> hops) {
        int ttl = initialTtl;
        int visibleHops = 0;
        String expiredAt = "";
        StringBuilder packetBadge = new StringBuilder("TTL: ").append(initialTtl);
        for (int index = 0; index < hops.size(); index++) {
            RouteHop hop = hops.get(index);
            int before = ttl;
            int after = Math.max(0, ttl - 1);
            hop.setTtl(before, after);
            ttl = after;
            visibleHops = index + 1;
            packetBadge.append(" → ").append(after);
            if (after == 0) {
                expiredAt = hop.device();
                break;
            }
        }
        return new TtlTrace(!expiredAt.isBlank(), expiredAt, visibleHops, ttl, packetBadge.toString());
    }

    private static final class RouteHop {
        private final String number;
        private final String device;
        private final String body;
        private int ttlBefore;
        private int ttlAfter;

        private RouteHop(String number, String device, String body) {
            this.number = number;
            this.device = device;
            this.body = body;
        }

        private void setTtl(int ttlBefore, int ttlAfter) {
            this.ttlBefore = ttlBefore;
            this.ttlAfter = ttlAfter;
        }

        private String number() {
            return number;
        }

        private String device() {
            return device;
        }

        private String body() {
            return body;
        }

        private int ttlBefore() {
            return ttlBefore;
        }

        private int ttlAfter() {
            return ttlAfter;
        }
    }

    private record TtlTrace(boolean expired, String expiredAt, int visibleHops, int finalTtl, String packetBadge) {
    }

    private enum IpSprint {
        IP1,
        IP2,
        IP3,
        IP4,
        IP5,
        IP6
    }

    private record MaskOption(int cidr) {
        @Override
        public String toString() {
            Ipv4SubnetResult result = Ipv4SubnetCalculator.calculate("0.0.0.0", "0.0.0.0", cidr);
            return result.decimalMask() + " (/" + cidr + ")";
        }
    }
}
