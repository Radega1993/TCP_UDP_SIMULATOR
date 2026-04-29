package com.example.simulator.ui;

import com.example.simulator.domain.ipv4.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

public class ArpLearningView extends GridPane {
    private final Label questionLabel = new Label();
    private final Label targetLabel = new Label();
    private final Label targetMacLabel = new Label();
    private final Label decisionLabel = new Label();
    private final VBox eventRows = new VBox(10);
    private final VBox cacheRows = new VBox(8);
    private final Label clientIpLabel = new Label();
    private final Label targetIpLabel = new Label();
    private final Label broadcastBadge = new Label();

    public ArpLearningView() {
        setHgap(14);
        setVgap(14);
        getColumnConstraints().setAll(growingColumn(820), fixedColumn(360));
        add(buildArpCard(), 0, 0);
        add(buildTheoryCard(), 1, 0);
        renderIdle();
    }

    public void updateContext(Ipv4SubnetResult subnet, String gateway) {
        ArpSimulationResult result = ArpSimulator.simulate(subnet, gateway);
        render(result);
    }

    public void renderError(String message) {
        questionLabel.setText(message);
        questionLabel.setStyle(statusStyle("#fff0f0", "#b91c1c"));
        targetLabel.setText("-");
        targetMacLabel.setText("-");
        decisionLabel.setText("No se puede resolver ARP hasta corregir IP, máscara o gateway.");
        decisionLabel.setStyle(statusStyle("#fff0f0", "#b91c1c"));
        clientIpLabel.setText("-");
        targetIpLabel.setText("-");
        broadcastBadge.setText("Broadcast detenido");
        eventRows.getChildren().setAll(emptyHint(message));
        cacheRows.getChildren().setAll(emptyHint("La caché ARP aparecerá tras resolver una IP local."));
    }

    private Node buildArpCard() {
        DashboardCard card = new DashboardCard("ARP", "Resolución en red local", "Antes de enviar una trama Ethernet, el equipo necesita saber la MAC del siguiente salto.");
        card.setStyle(cardStyle());

        GridPane summary = new GridPane();
        summary.setHgap(10);
        summary.getColumnConstraints().setAll(growingColumn(220), growingColumn(220), growingColumn(220));
        summary.add(summaryBox("Pregunta ARP", questionLabel, "/icons/network.svg"), 0, 0);
        summary.add(summaryBox("IP resuelta", targetLabel, "/icons/router.svg"), 1, 0);
        summary.add(summaryBox("MAC encontrada", targetMacLabel, "/icons/check.svg"), 2, 0);

        DashboardCard diagram = new DashboardCard(null, "Diagrama ARP", null);
        diagram.setHeaderVisible(false);
        diagram.setStyle(innerCardStyle());
        diagram.setContent(buildDiagram());

        DashboardCard events = new DashboardCard(null, "Proceso ARP", null);
        events.setHeaderVisible(false);
        events.setStyle(innerCardStyle());
        events.setContent(eventRows);

        DashboardCard cache = new DashboardCard(null, "Cache ARP: IP → MAC", null);
        cache.setHeaderVisible(false);
        cache.setStyle(innerCardStyle());
        cache.setContent(new VBox(8, cacheHeader(), cacheRows));

        decisionLabel.setWrapText(true);
        card.setContent(new VBox(14, summary, diagram, events, cache, decisionLabel));
        return card;
    }

    private Node buildDiagram() {
        Pane pane = new Pane();
        pane.setMinHeight(250);
        pane.setPrefHeight(250);
        pane.setMaxHeight(250);
        Line clientToSwitch = link(150, 126, 360, 126);
        Line switchToRouter = link(420, 126, 640, 126);
        pane.getChildren().addAll(clientToSwitch, switchToRouter);
        pane.getChildren().add(device("/icons/pc.svg", "Cliente", clientIpLabel, "AA:AA:AA:AA:AA:10", 34, 78, "#eaf3ff"));
        pane.getChildren().add(device("/icons/network.svg", "Switch", new Label("Broadcast LAN"), "FF:FF:FF:FF:FF:FF", 330, 78, "#f8fbff"));
        pane.getChildren().add(device("/icons/router.svg", "Gateway", targetIpLabel, "BB:BB:BB:BB:BB:01", 620, 78, "#f6f0ff"));
        broadcastBadge.setLayoutX(260);
        broadcastBadge.setLayoutY(36);
        broadcastBadge.setStyle(statusStyle("#fff7e8", "#b45309"));
        pane.getChildren().add(broadcastBadge);
        return pane;
    }

    private Node buildTheoryCard() {
        DashboardCard card = new DashboardCard("TEORÍA", "IP vs MAC", null);
        card.setStyle(cardStyle());
        card.setContent(new VBox(12,
                theoryItem("/icons/ip.svg", "IP", "Identifica el destino lógico: equipo final o gateway."),
                theoryItem("/icons/physical.svg", "MAC", "Identifica la tarjeta de red dentro de la LAN."),
                theoryItem("/icons/network.svg", "Broadcast", "ARP Request pregunta a toda la red local: Who has esa IP?"),
                theoryItem("/icons/check.svg", "Cache ARP", "La respuesta se guarda para no preguntar otra vez en cada paquete.")
        ));
        return card;
    }

    private void render(ArpSimulationResult result) {
        questionLabel.setText(result.question());
        questionLabel.setStyle(statusStyle("#fff7e8", "#b45309"));
        targetLabel.setText(result.targetIp() + " · " + result.targetDevice());
        targetMacLabel.setText(result.targetMac());
        clientIpLabel.setText(result.requesterIp());
        targetIpLabel.setText(result.targetIp());
        broadcastBadge.setText("ARP Request broadcast: Who has " + result.targetIp() + "?");
        decisionLabel.setText(result.resolvingGateway()
                ? "Como el destino está fuera de la red local, el cliente no busca la MAC del servidor: busca la MAC del gateway."
                : "Como el destino está en la misma red, el cliente busca directamente la MAC del equipo destino.");
        decisionLabel.setStyle(statusStyle("#e7f8ef", "#087f4f"));
        eventRows.getChildren().setAll(result.events().stream().map(this::eventRow).toArray(Node[]::new));
        cacheRows.getChildren().setAll(result.cacheEntries().stream().map(this::cacheRow).toArray(Node[]::new));
    }

    private Node eventRow(ArpEvent event) {
        Label step = new Label(String.valueOf(event.step()));
        step.setAlignment(Pos.CENTER);
        step.setMinSize(30, 30);
        step.setMaxSize(30, 30);
        step.setStyle("-fx-background-color: " + (event.broadcast() ? "#fff7e8" : "#e7f8ef") + ";"
                + "-fx-text-fill: " + (event.broadcast() ? "#b45309" : "#087f4f") + ";"
                + "-fx-background-radius: 999; -fx-font-weight: 900;");
        Label title = new Label(event.title());
        title.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label body = new Label(event.message());
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, step, new VBox(2, title, body), badge(event.broadcast() ? "Broadcast" : "Unicast / cache", event.broadcast()));
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(11));
        row.setStyle("-fx-background-color: #ffffff; -fx-border-color: #edf3f9; -fx-background-radius: 10; -fx-border-radius: 10;");
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        return row;
    }

    private Node cacheHeader() {
        HBox row = new HBox(8, tableCell("IP", 170, true), tableCell("MAC", 190, true), tableCell("Dispositivo", 150, true));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node cacheRow(ArpCacheEntry entry) {
        HBox row = new HBox(8, tableCell(entry.ip(), 170, false), tableCell(entry.mac(), 190, false), tableCell(entry.device(), 150, false));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #e7f8ef; -fx-border-color: #7bd8a6; -fx-background-radius: 10; -fx-border-radius: 10;");
        return row;
    }

    private Node device(String iconPath, String name, Label ipLabel, String mac, double x, double y, String background) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        ipLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #173452; -fx-font-weight: 800;");
        Label macLabel = new Label(mac);
        macLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #5f7390;");
        VBox box = new VBox(5, icon(iconPath, 30), nameLabel, ipLabel, macLabel);
        box.setAlignment(Pos.CENTER);
        box.setLayoutX(x);
        box.setLayoutY(y);
        box.setMinSize(132, 112);
        box.setMaxSize(132, 112);
        box.setStyle("-fx-background-color: " + background + "; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 12; -fx-border-radius: 12;");
        return box;
    }

    private Line link(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.web("#9bb3ca"));
        line.setStrokeWidth(3);
        return line;
    }

    private Label badge(String text, boolean broadcast) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + (broadcast ? "#fff7e8" : "#e7f8ef") + ";"
                + "-fx-text-fill: " + (broadcast ? "#b45309" : "#087f4f") + ";"
                + "-fx-background-radius: 999; -fx-padding: 6 10 6 10; -fx-font-weight: 900;");
        return label;
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

    private Node summaryBox(String title, Label value, String iconPath) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        value.setWrapText(true);
        value.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        HBox head = new HBox(8, icon(iconPath, 16), titleLabel);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(4, head, value);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d9e6f2;");
        return box;
    }

    private Node theoryItem(String iconPath, String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, iconTile(iconPath), new VBox(3, titleLabel, bodyLabel));
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node emptyHint(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(statusStyle("#f8fbff", "#5f7390"));
        return label;
    }

    private void renderIdle() {
        questionLabel.setText("Who has ...?");
        targetLabel.setText("-");
        targetMacLabel.setText("-");
        clientIpLabel.setText("-");
        targetIpLabel.setText("-");
        broadcastBadge.setText("ARP Request broadcast");
        decisionLabel.setText("Configura la red para ver cómo el cliente encuentra la MAC del siguiente salto.");
        decisionLabel.setStyle(statusStyle("#f8fbff", "#5f7390"));
        eventRows.getChildren().setAll(emptyHint("Los pasos ARP aparecerán aquí."));
        cacheRows.getChildren().setAll(emptyHint("La caché ARP empezará vacía y se rellenará tras el reply."));
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

    private Node iconTile(String path) {
        StackPane pane = new StackPane(icon(path, 22));
        pane.setMinSize(34, 34);
        pane.setMaxSize(34, 34);
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
            if (png != null) return png;
            return null;
        }
        try (InputStream stream = ArpLearningView.class.getResourceAsStream(resourcePath)) {
            return stream == null ? null : new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String cardStyle() {
        return "-fx-background-color: #ffffff; -fx-background-radius: 14;"
                + "-fx-border-radius: 14; -fx-border-color: #d9e6f2;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.035), 22, 0.18, 0, 8);";
    }

    private String innerCardStyle() {
        return "-fx-background-color: #fbfdff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #edf3f9;";
    }

    private String statusStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 10; -fx-padding: 12; -fx-font-weight: 800;";
    }
}
