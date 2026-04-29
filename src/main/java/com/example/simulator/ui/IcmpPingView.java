package com.example.simulator.ui;

import com.example.simulator.domain.ipv4.IcmpMessageType;
import com.example.simulator.domain.ipv4.IcmpPingEvent;
import com.example.simulator.domain.ipv4.IcmpPingResult;
import com.example.simulator.domain.ipv4.IcmpPingSimulator;
import com.example.simulator.domain.ipv4.Ipv4SubnetResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

public class IcmpPingView extends GridPane {
    private final VBox consoleRows = new VBox(8);
    private final VBox eventRows = new VBox(10);
    private final VBox diagramRows = new VBox(12);
    private final Label resultSummary = new Label("Pulsa Iniciar ping para diagnosticar la ruta.");
    private final Label statusBadge = new Label("Esperando ping");
    private Ipv4SubnetResult currentSubnet;
    private String currentGateway = "";
    private int currentTtl = 64;

    public IcmpPingView() {
        setHgap(14);
        setVgap(14);
        getColumnConstraints().setAll(growingColumn(820), fixedColumn(360));
        add(buildMainColumn(), 0, 0);
        add(buildSideColumn(), 1, 0);
        renderIdle();
    }

    public void updateContext(Ipv4SubnetResult subnet, String gateway, int ttl) {
        currentSubnet = subnet;
        currentGateway = gateway == null ? "" : gateway.trim();
        currentTtl = ttl;
        renderIdle();
    }

    public void renderError(String message) {
        statusBadge.setText("Datos no válidos");
        statusBadge.setStyle(pillStyle("#fff0f0", "#b91c1c"));
        resultSummary.setText(message);
        resultSummary.setStyle(statusStyle("#fff0f0", "#b91c1c"));
        consoleRows.getChildren().setAll(consoleLine("> ping -- esperando datos IPv4 válidos", false));
        diagramRows.getChildren().setAll(emptyHint("Corrige IP, máscara o gateway para iniciar ICMP."));
        eventRows.getChildren().setAll(emptyHint(message));
    }

    private Node buildMainColumn() {
        DashboardCard pingCard = new DashboardCard("ICMP", "Mini simulador de ping", "Comprueba si un destino responde y qué error devuelve la red.");
        pingCard.setStyle(cardStyle());

        Button start = new Button("Iniciar ping");
        start.setGraphic(icon("/icons/refresh.svg", 16));
        start.setGraphicTextGap(8);
        start.setMinHeight(42);
        start.setStyle("-fx-background-color: #2f80ed; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 0 16 0 16;");
        start.setOnAction(event -> startPing());

        statusBadge.setStyle(pillStyle("#eaf3ff", "#2f80ed"));
        HBox actionRow = new HBox(12, start, statusBadge);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        resultSummary.setWrapText(true);
        resultSummary.setStyle(statusStyle("#f8fbff", "#345573"));

        DashboardCard diagram = new DashboardCard("DIAGRAMA PARALELO", "Request y respuesta", null);
        diagram.setStyle(innerCardStyle());
        diagram.setContent(diagramRows);

        DashboardCard console = new DashboardCard("CONSOLA", "Salida tipo terminal", null);
        console.setStyle(innerCardStyle());
        console.setContent(consoleRows);

        pingCard.setContent(new VBox(14, actionRow, resultSummary, diagram, console));
        return pingCard;
    }

    private Node buildSideColumn() {
        DashboardCard types = new DashboardCard("TIPOS ICMP", "Mensajes que puede devolver", null);
        types.setStyle(cardStyle());
        types.setContent(new VBox(10,
                typeItem("/icons/ip.svg", "Echo Request", "Pregunta: ¿estás ahí?"),
                typeItem("/icons/check.svg", "Echo Reply", "Respuesta: sí, estoy disponible."),
                typeItem("/icons/x.svg", "Destination unreachable", "No hay ruta o no se puede llegar."),
                typeItem("/icons/info.svg", "TTL exceeded", "El paquete agotó su TTL.")
        ));

        DashboardCard events = new DashboardCard("PASO A PASO", "Eventos ICMP", null);
        events.setStyle(cardStyle());
        events.setContent(eventRows);

        DashboardCard theory = new DashboardCard("TEORÍA", "¿Qué está comprobando ping?", null);
        theory.setStyle(cardStyle());
        theory.setContent(theoryText("""
                Ping usa ICMP. Envía un Echo Request al destino y espera un Echo Reply.

                Si no vuelve un Reply, el error también enseña algo: puede faltar gateway, no existir ruta o agotarse el TTL.
                """));
        return new VBox(14, types, events, theory);
    }

    private void startPing() {
        if (currentSubnet == null) {
            renderError("Introduce primero una IP origen, una IP destino y una máscara válidas.");
            return;
        }
        IcmpPingResult result = IcmpPingSimulator.simulate(currentSubnet, currentGateway, currentTtl);
        statusBadge.setText(result.finalType().displayName());
        statusBadge.setStyle(pillStyle(result.success() ? "#e7f8ef" : "#fff0f0", result.success() ? "#087f4f" : "#b91c1c"));
        resultSummary.setText(result.summary());
        resultSummary.setStyle(statusStyle(result.success() ? "#e7f8ef" : "#fff0f0", result.success() ? "#087f4f" : "#b91c1c"));
        consoleRows.getChildren().setAll(
                consoleLine("> ping " + currentSubnet.destinationIp(), false),
                consoleLine(result.consoleLine(), !result.success())
        );
        diagramRows.getChildren().setAll(result.events().stream().map(this::diagramRow).toArray(Node[]::new));
        eventRows.getChildren().setAll(result.events().stream().map(this::eventRow).toArray(Node[]::new));
    }

    private void renderIdle() {
        statusBadge.setText("Esperando ping");
        statusBadge.setStyle(pillStyle("#eaf3ff", "#2f80ed"));
        resultSummary.setText("Pulsa Iniciar ping para enviar un Echo Request desde el cliente y observar qué responde la red.");
        resultSummary.setStyle(statusStyle("#f8fbff", "#345573"));
        String target = currentSubnet == null ? "destino" : currentSubnet.destinationIp();
        consoleRows.getChildren().setAll(consoleLine("> ping " + target, false), consoleLine("Esperando ejecución...", false));
        diagramRows.getChildren().setAll(emptyHint("Aquí verás Echo Request y Echo Reply en paralelo."));
        eventRows.getChildren().setAll(emptyHint("Los eventos ICMP aparecerán al iniciar el ping."));
    }

    private Node diagramRow(IcmpPingEvent event) {
        boolean error = event.type() == IcmpMessageType.DESTINATION_UNREACHABLE || event.type() == IcmpMessageType.TTL_EXCEEDED;
        HBox row = new HBox(10,
                endpoint(event.fromDevice(), event.fromIp(), iconForDevice(event.fromDevice())),
                arrow(event.type().displayName(), error),
                endpoint(event.toDevice(), event.toIp(), iconForDevice(event.toDevice())),
                ttlBadge(event)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: " + (error ? "#fff0f0" : "#fbfdff") + ";"
                + "-fx-border-color: " + (error ? "#ffcaca" : "#d9e6f2") + ";"
                + "-fx-background-radius: 10; -fx-border-radius: 10;");
        return row;
    }

    private Node endpoint(String name, String ip, String iconPath) {
        Label title = new Label(name);
        title.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label address = new Label(ip);
        address.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px;");
        VBox copy = new VBox(2, title, address);
        HBox box = new HBox(8, icon(iconPath, 24), copy);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(160);
        return box;
    }

    private Node arrow(String label, boolean error) {
        Line line = new Line(0, 0, 120, 0);
        line.setStroke(Color.web(error ? "#e03131" : "#2f80ed"));
        line.setStrokeWidth(2);
        Label text = new Label(label);
        text.setStyle("-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: " + (error ? "#b91c1c" : "#2f80ed") + ";");
        VBox box = new VBox(3, text, line);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Label ttlBadge(IcmpPingEvent event) {
        Label ttl = new Label(event.ttlBefore() == event.ttlAfter()
                ? "TTL " + event.ttlAfter()
                : "TTL " + event.ttlBefore() + " -> " + event.ttlAfter());
        ttl.setStyle(pillStyle(event.ttlAfter() == 0 ? "#fff0f0" : "#f8fbff", event.ttlAfter() == 0 ? "#b91c1c" : "#345573"));
        return ttl;
    }

    private Node eventRow(IcmpPingEvent event) {
        Label badge = new Label(String.valueOf(event.step()));
        badge.setAlignment(Pos.CENTER);
        badge.setMinSize(30, 30);
        badge.setMaxSize(30, 30);
        badge.setStyle("-fx-background-color: #eaf3ff; -fx-background-radius: 999; -fx-text-fill: #2f80ed; -fx-font-weight: 900;");
        Label title = new Label(event.type().displayName());
        title.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label body = new Label(event.description());
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, badge, new VBox(2, title, body));
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node typeItem(String iconPath, String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, iconTile(iconPath), new VBox(3, titleLabel, bodyLabel));
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Label consoleLine(String text, boolean error) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        label.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-padding: 10;"
                + "-fx-background-color: " + (error ? "#2b1115" : "#102033") + ";"
                + "-fx-text-fill: " + (error ? "#ffb4b4" : "#d9f99d") + ";"
                + "-fx-background-radius: 8;");
        return label;
    }

    private Node emptyHint(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(statusStyle("#f8fbff", "#5f7390"));
        return label;
    }

    private Node theoryText(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #5f7390; -fx-line-spacing: 3;");
        return label;
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
            if (png != null) {
                return png;
            }
            return null;
        }
        try (InputStream stream = IcmpPingView.class.getResourceAsStream(resourcePath)) {
            return stream == null ? null : new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String iconForDevice(String device) {
        String lower = device.toLowerCase();
        if (lower.contains("cliente")) {
            return "/icons/pc.svg";
        }
        if (lower.contains("servidor") || lower.contains("destino")) {
            return "/icons/server.svg";
        }
        if (lower.contains("router") || lower.contains("gateway")) {
            return "/icons/router.svg";
        }
        return "/icons/network.svg";
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

    private String pillStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-padding: 7 12 7 12; -fx-font-weight: 900;";
    }
}
