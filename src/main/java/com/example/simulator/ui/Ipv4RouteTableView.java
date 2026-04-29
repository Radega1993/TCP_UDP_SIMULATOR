package com.example.simulator.ui;

import com.example.simulator.domain.ipv4.Ipv4RouteEntry;
import com.example.simulator.domain.ipv4.Ipv4RouteSelection;
import com.example.simulator.domain.ipv4.Ipv4RouterTable;
import com.example.simulator.domain.ipv4.Ipv4RoutingTableSimulator;
import com.example.simulator.domain.ipv4.Ipv4SubnetResult;
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
import java.util.List;

public class Ipv4RouteTableView extends GridPane {
    private final VBox routeRows = new VBox(8);
    private final VBox matchRows = new VBox(10);
    private final Pane routerScene = new Pane();
    private final Label decisionLabel = new Label();
    private final Label selectedRouteLabel = new Label();
    private final Label selectedGatewayLabel = new Label();
    private final Label selectedInterfaceLabel = new Label();
    private final Label destinationLabel = new Label();
    private final Label selectedRouterLabel = new Label();
    private List<Ipv4RouterTable> routerTables = List.of();
    private String selectedRouterId = "R1";
    private Ipv4SubnetResult currentSubnet;

    public Ipv4RouteTableView() {
        setHgap(14);
        setVgap(14);
        getColumnConstraints().setAll(growingColumn(820), fixedColumn(360));
        add(buildRoutesCard(), 0, 0);
        add(buildTheoryCard(), 1, 0);
        renderIdle();
    }

    public void updateContext(Ipv4SubnetResult subnet, String gateway) {
        currentSubnet = subnet;
        routerTables = Ipv4RoutingTableSimulator.buildTriangleTables(subnet, gateway);
        if (routerTables.stream().noneMatch(router -> router.id().equals(selectedRouterId))) {
            selectedRouterId = routerTables.get(0).id();
        }
        renderRouterScene();
        renderSelection(selectedRouter());
    }

    private void renderSelection(Ipv4RouterTable router) {
        Ipv4RouteSelection selection = router.selection();
        destinationLabel.setText(selection.destinationIp());
        selectedRouterLabel.setText(router.name() + " · " + router.role());
        selectedRouteLabel.setText(selection.selectedRoute().destinationCidr());
        selectedGatewayLabel.setText(selection.selectedRoute().gateway());
        selectedInterfaceLabel.setText(selection.selectedRoute().networkInterface());
        decisionLabel.setText(router.name() + ": " + selection.explanation());
        decisionLabel.setStyle(statusStyle("#e7f8ef", "#087f4f"));
        routeRows.getChildren().setAll(selection.routes().stream().map(this::routeRow).toArray(Node[]::new));
        matchRows.getChildren().setAll(selection.routes().stream().filter(Ipv4RouteEntry::matches).map(this::matchRow).toArray(Node[]::new));
    }

    public void renderError(String message) {
        destinationLabel.setText("-");
        selectedRouteLabel.setText("-");
        selectedGatewayLabel.setText("-");
        selectedInterfaceLabel.setText("-");
        selectedRouterLabel.setText("-");
        decisionLabel.setText(message);
        decisionLabel.setStyle(statusStyle("#fff0f0", "#b91c1c"));
        routerScene.getChildren().setAll(emptyHint("El diagrama aparecerá cuando los datos IPv4 sean válidos."));
        routeRows.getChildren().setAll(emptyHint("Corrige los datos IPv4 para calcular la tabla de rutas."));
        matchRows.getChildren().setAll(emptyHint("No se puede aplicar longest prefix match todavía."));
    }

    private Node buildRoutesCard() {
        DashboardCard card = new DashboardCard("ROUTING REAL", "Tabla de rutas", "El router compara el destino con cada red y elige el prefijo más largo.");
        card.setStyle(cardStyle());

        GridPane summary = new GridPane();
        summary.setHgap(10);
        summary.getColumnConstraints().setAll(growingColumn(170), growingColumn(170), growingColumn(170), growingColumn(170));
        summary.add(summaryBox("IP destino", destinationLabel, "/icons/server.svg"), 0, 0);
        summary.add(summaryBox("Ruta elegida", selectedRouteLabel, "/icons/check.svg"), 1, 0);
        summary.add(summaryBox("Gateway", selectedGatewayLabel, "/icons/router.svg"), 2, 0);
        summary.add(summaryBox("Interfaz", selectedInterfaceLabel, "/icons/network.svg"), 3, 0);

        selectedRouterLabel.setWrapText(true);
        selectedRouterLabel.setStyle(statusStyle("#f8fbff", "#345573"));
        DashboardCard diagram = new DashboardCard(null, "Topología de routers", "Haz clic en un router para ver su tabla.");
        diagram.setHeaderVisible(false);
        diagram.setStyle(innerCardStyle());
        routerScene.setMinHeight(260);
        routerScene.setPrefHeight(260);
        routerScene.setMaxHeight(260);
        diagram.setContent(new VBox(10, routerScene, selectedRouterLabel));

        DashboardCard table = new DashboardCard(null, "Destino | Máscara | Gateway | Interfaz", null);
        table.setHeaderVisible(false);
        table.setStyle(innerCardStyle());
        table.setContent(new VBox(8, tableHeader(), routeRows));

        DashboardCard matches = new DashboardCard(null, "Coincidencias encontradas", null);
        matches.setHeaderVisible(false);
        matches.setStyle(innerCardStyle());
        matches.setContent(matchRows);

        decisionLabel.setWrapText(true);
        card.setContent(new VBox(14, summary, diagram, table, matches, decisionLabel));
        return card;
    }

    private Node buildTheoryCard() {
        DashboardCard card = new DashboardCard("TEORÍA", "¿Cómo decide un router?", null);
        card.setStyle(cardStyle());
        card.setContent(new VBox(12,
                theoryItem("/icons/server.svg", "Mira la IP destino", "No decide por la IP origen: compara la dirección final del paquete."),
                theoryItem("/icons/binary.svg", "Aplica la máscara", "Cada fila representa una red. La máscara dice cuántos bits deben coincidir."),
                theoryItem("/icons/check.svg", "Elige la más específica", "Si varias rutas coinciden, gana la de mayor CIDR: /24 gana a /16 y /0."),
                theoryItem("/icons/router.svg", "Reenvía por interfaz", "La fila elegida indica el siguiente gateway y la interfaz de salida.")
        ));
        return card;
    }

    private Node tableHeader() {
        HBox row = new HBox(8,
                tableCell("Destino", 150, true),
                tableCell("Máscara", 135, true),
                tableCell("Gateway", 145, true),
                tableCell("Interfaz", 95, true),
                tableCell("Estado", 145, true)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node routeRow(Ipv4RouteEntry route) {
        String background = route.selected() ? "#e7f8ef" : route.matches() ? "#eaf3ff" : "#ffffff";
        String border = route.selected() ? "#7bd8a6" : route.matches() ? "#a9ccff" : "#edf3f9";
        HBox row = new HBox(8,
                tableCell(route.destinationCidr(), 150, false),
                tableCell(route.decimalMask(), 135, false),
                tableCell(route.gateway(), 145, false),
                tableCell(route.networkInterface(), 95, false),
                stateBadge(route)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: " + background + "; -fx-border-color: " + border + ";"
                + "-fx-background-radius: 10; -fx-border-radius: 10;");
        return row;
    }

    private void renderRouterScene() {
        routerScene.getChildren().clear();
        Line aToB = link(285, 56, 150, 190);
        Line bToC = link(150, 190, 430, 190);
        Line cToA = link(430, 190, 285, 56);
        routerScene.getChildren().addAll(aToB, bToC, cToA);
        routerScene.getChildren().addAll(
                networkBadge("10.0.12.0/30", 150, 130, "#f8fbff"),
                networkBadge("10.0.23.0/30", 260, 191, "#f8fbff"),
                networkBadge("10.0.13.0/30", 358, 130, "#f8fbff")
        );
        routerScene.getChildren().addAll(
                link(70, 84, 218, 84),
                link(495, 214, 625, 214),
                edgeDevice("/icons/pc.svg", "Cliente", currentSubnet.sourceIp(), 18, 42, "#e7f8ef"),
                edgeDevice("/icons/server.svg", "Servidor", currentSubnet.destinationIp(), 626, 172, "#eaf3ff")
        );
        routerScene.getChildren().add(routerNode("R1", "Router A", 220, 20));
        routerScene.getChildren().add(routerNode("R2", "Router B", 85, 154));
        routerScene.getChildren().add(routerNode("R3", "Router C", 365, 154));
        routerScene.getChildren().add(networkBadge("LAN origen " + currentSubnet.sourceNetwork() + "/" + currentSubnet.cidr(), 18, 12, "#e7f8ef"));
        routerScene.getChildren().add(networkBadge("LAN destino " + currentSubnet.destinationNetwork() + "/" + currentSubnet.cidr(), 490, 148, "#eaf3ff"));
    }

    private Line link(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.web("#9bb3ca"));
        line.setStrokeWidth(3);
        return line;
    }

    private Node routerNode(String id, String name, double x, double y) {
        boolean active = id.equals(selectedRouterId);
        Label idLabel = new Label(id);
        idLabel.setAlignment(Pos.CENTER);
        idLabel.setMinSize(32, 32);
        idLabel.setMaxSize(32, 32);
        idLabel.setStyle("-fx-background-color: " + (active ? "#0f9f8f" : "#eef5ff") + ";"
                + "-fx-text-fill: " + (active ? "#ffffff" : "#2f80ed") + ";"
                + "-fx-background-radius: 999; -fx-font-weight: 900;");
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label hint = new Label(active ? "Tabla visible" : "Ver tabla");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (active ? "#087f4f" : "#5f7390") + "; -fx-font-weight: 800;");
        VBox box = new VBox(5, icon("/icons/router.svg", 32), idLabel, nameLabel, hint);
        box.setAlignment(Pos.CENTER);
        box.setMinSize(130, 108);
        box.setMaxSize(130, 108);
        box.setLayoutX(x);
        box.setLayoutY(y);
        box.setOnMouseClicked(event -> {
            selectedRouterId = id;
            renderRouterScene();
            renderSelection(selectedRouter());
        });
        box.setStyle("-fx-background-color: " + (active ? "#e7f8ef" : "#ffffff") + ";"
                + "-fx-border-color: " + (active ? "#0f9f8f" : "#d9e6f2") + ";"
                + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.06), 14, 0.15, 0, 5);");
        return box;
    }

    private Node networkBadge(String text, double x, double y, String background) {
        Label label = new Label(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setWrapText(false);
        label.setStyle("-fx-background-color: " + background + "; -fx-text-fill: #173452;"
                + "-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #d9e6f2;"
                + "-fx-padding: 7 12 7 12; -fx-font-weight: 900;");
        return label;
    }

    private Node edgeDevice(String iconPath, String name, String ip, double x, double y, String background) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label ipLabel = new Label(ip);
        ipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5f7390; -fx-font-weight: 800;");
        VBox box = new VBox(5, icon(iconPath, 28), nameLabel, ipLabel);
        box.setAlignment(Pos.CENTER);
        box.setLayoutX(x);
        box.setLayoutY(y);
        box.setMinSize(116, 86);
        box.setMaxSize(116, 86);
        box.setStyle("-fx-background-color: " + background + "; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 12; -fx-border-radius: 12;");
        return box;
    }

    private Ipv4RouterTable selectedRouter() {
        return routerTables.stream()
                .filter(router -> router.id().equals(selectedRouterId))
                .findFirst()
                .orElse(routerTables.get(0));
    }

    private Node matchRow(Ipv4RouteEntry route) {
        Label title = new Label(route.destinationCidr());
        title.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label body = new Label(route.selected()
                ? "Elegida porque /" + route.cidr() + " es el prefijo más largo entre las coincidencias."
                : "También coincide, pero es menos específica que la ruta elegida.");
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, icon(route.selected() ? "/icons/check.svg" : "/icons/info.svg", 20), new VBox(2, title, body));
        row.setPadding(new Insets(11));
        row.setStyle("-fx-background-color: " + (route.selected() ? "#e7f8ef" : "#f8fbff")
                + "; -fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10;");
        return row;
    }

    private Label stateBadge(Ipv4RouteEntry route) {
        if (route.selected()) {
            return badge("Elegida", "#087f4f");
        }
        if (route.matches()) {
            return badge("Coincide", "#2f80ed");
        }
        return badge("No coincide", "#5f7390");
    }

    private Label badge(String text, String color) {
        Label label = new Label(text);
        label.setMinWidth(115);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: #ffffff; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #d9e6f2;"
                + "-fx-padding: 7 10 7 10; -fx-font-weight: 900;");
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
        destinationLabel.setText("-");
        selectedRouteLabel.setText("-");
        selectedGatewayLabel.setText("-");
        selectedInterfaceLabel.setText("-");
        selectedRouterLabel.setText("-");
        decisionLabel.setText("Calcula una red para ver qué fila de la tabla gana.");
        decisionLabel.setStyle(statusStyle("#f8fbff", "#5f7390"));
        routerScene.getChildren().setAll(emptyHint("El diagrama de tres routers aparecerá aquí."));
        routeRows.getChildren().setAll(emptyHint("La tabla aparecerá con las IP actuales."));
        matchRows.getChildren().setAll(emptyHint("Aquí verás las rutas que coinciden con la IP destino."));
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
        try (InputStream stream = Ipv4RouteTableView.class.getResourceAsStream(resourcePath)) {
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
