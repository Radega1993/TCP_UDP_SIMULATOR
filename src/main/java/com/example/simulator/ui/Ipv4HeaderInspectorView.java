package com.example.simulator.ui;

import com.example.simulator.domain.ipv4.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

public class Ipv4HeaderInspectorView extends GridPane {
    private final ComboBox<IpTransportProtocol> protocolBox = new ComboBox<>();
    private final FlowPane headerBlocks = new FlowPane(8, 8);
    private final VBox fieldDetails = new VBox(10);
    private final Label summary = new Label();
    private final Label packetBar = new Label();
    private Ipv4SubnetResult currentSubnet;
    private int currentTtl = 64;

    public Ipv4HeaderInspectorView() {
        setHgap(14);
        setVgap(14);
        getColumnConstraints().setAll(growingColumn(820), fixedColumn(360));
        protocolBox.getItems().setAll(IpTransportProtocol.TCP, IpTransportProtocol.UDP);
        protocolBox.setValue(IpTransportProtocol.TCP);
        protocolBox.setMinHeight(38);
        protocolBox.valueProperty().addListener((obs, old, value) -> render());
        add(buildInspectorCard(), 0, 0);
        add(buildTheoryCard(), 1, 0);
        renderIdle();
    }

    public void updateContext(Ipv4SubnetResult subnet, int ttl) {
        currentSubnet = subnet;
        currentTtl = ttl;
        render();
    }

    public void renderError(String message) {
        summary.setText(message);
        summary.setStyle(statusStyle("#fff0f0", "#b91c1c"));
        headerBlocks.getChildren().setAll(emptyHint("No se puede leer la cabecera hasta corregir los datos IPv4."));
        fieldDetails.getChildren().setAll(emptyHint(message));
        packetBar.setText("Header IP + transporte + datos");
    }

    private Node buildInspectorCard() {
        DashboardCard card = new DashboardCard("INSPECTOR", "Cabecera IP", "Representación simplificada de los campos que viajan en un paquete IPv4.");
        card.setStyle(cardStyle());

        Label protocolLabel = new Label("Protocolo encapsulado");
        protocolLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-weight: 800;");
        HBox controls = new HBox(12, icon("/icons/ip.svg", 22), protocolLabel, protocolBox);
        controls.setAlignment(Pos.CENTER_LEFT);

        summary.setWrapText(true);
        packetBar.setWrapText(true);
        packetBar.setStyle("-fx-background-color: linear-gradient(to right, #f3ecff 0%, #f3ecff 34%, #eaf3ff 34%, #eaf3ff 68%, #e7f8ef 68%, #e7f8ef 100%);"
                + "-fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10;"
                + "-fx-padding: 14; -fx-font-weight: 900; -fx-text-fill: #173452;");

        DashboardCard blocks = new DashboardCard("WIRESHARK SIMPLE", "Campos de la cabecera", null);
        blocks.setHeaderVisible(false);
        blocks.setStyle(innerCardStyle());
        blocks.setContent(headerBlocks);

        DashboardCard details = new DashboardCard("LECTURA", "Qué significa cada campo", null);
        details.setHeaderVisible(false);
        details.setStyle(innerCardStyle());
        details.setContent(fieldDetails);

        card.setContent(new VBox(14, controls, summary, packetBar, blocks, details));
        return card;
    }

    private Node buildTheoryCard() {
        DashboardCard card = new DashboardCard("TEORÍA", "¿Qué contiene una cabecera IP?", null);
        card.setStyle(cardStyle());
        card.setContent(new VBox(12,
                theoryItem("/icons/ip.svg", "Direcciones IP", "Origen y destino lógico del paquete."),
                theoryItem("/icons/info.svg", "TTL", "Evita que un paquete circule para siempre."),
                theoryItem("/icons/transport.svg", "Protocolo", "Indica si dentro viaja TCP, UDP u otro protocolo."),
                theoryItem("/icons/binary.svg", "Longitud y flags", "Ayudan a saber tamaño y fragmentación.")
        ));
        return card;
    }

    private void render() {
        if (currentSubnet == null) {
            renderIdle();
            return;
        }
        Ipv4HeaderSnapshot snapshot = Ipv4HeaderInspector.inspect(currentSubnet, currentTtl, protocolBox.getValue());
        summary.setText("Lee este paquete como: IPv" + snapshot.version() + " desde " + snapshot.sourceIp()
                + " hacia " + snapshot.destinationIp() + ", TTL " + snapshot.ttl()
                + ", transportando " + snapshot.protocol().displayName() + ".");
        summary.setStyle(statusStyle("#eaf3ff", "#2f80ed"));
        packetBar.setText("Header IP 20 B  |  Header " + snapshot.protocol().displayName() + " "
                + snapshot.protocol().headerBytes() + " B  |  Datos 32 B  |  Total " + snapshot.totalLengthBytes() + " B");
        headerBlocks.getChildren().setAll(snapshot.fields().stream().map(this::fieldBlock).toArray(Node[]::new));
        fieldDetails.getChildren().setAll(snapshot.fields().stream().map(this::detailRow).toArray(Node[]::new));
    }

    private void renderIdle() {
        summary.setText("Configura IP origen, destino, máscara y TTL para inspeccionar la cabecera.");
        summary.setStyle(statusStyle("#f8fbff", "#5f7390"));
        headerBlocks.getChildren().setAll(emptyHint("Los bloques de cabecera aparecerán aquí."));
        fieldDetails.getChildren().setAll(emptyHint("Selecciona TCP o UDP para comparar el campo Protocolo."));
        packetBar.setText("Header IP + transporte + datos");
    }

    private Node fieldBlock(Ipv4HeaderField field) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(12));
        box.setMinWidth(Math.max(118, field.bits() * 5.2));
        box.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 8;"
                + "-fx-border-radius: 8; -fx-border-color: #bcd6ff;");
        Label name = new Label(field.name());
        name.setWrapText(true);
        name.setStyle("-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: #2f80ed;");
        Label value = new Label(field.value());
        value.setWrapText(true);
        value.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-text-fill: #173452;");
        Label bits = new Label(field.bits() + " bits");
        bits.setStyle("-fx-font-size: 10px; -fx-text-fill: #5f7390;");
        box.getChildren().addAll(name, value, bits);
        return box;
    }

    private Node detailRow(Ipv4HeaderField field) {
        Label name = new Label(field.name());
        name.setMinWidth(130);
        name.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label help = new Label(field.help());
        help.setWrapText(true);
        help.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, icon(iconForField(field.name()), 18), name, help);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #ffffff; -fx-border-color: #edf3f9; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox.setHgrow(help, Priority.ALWAYS);
        return row;
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

    private String iconForField(String name) {
        if (name.contains("origen")) return "/icons/pc.svg";
        if (name.contains("destino")) return "/icons/server.svg";
        if (name.equals("TTL")) return "/icons/info.svg";
        if (name.equals("Protocolo")) return "/icons/transport.svg";
        if (name.equals("Flags")) return "/icons/binary.svg";
        return "/icons/ip.svg";
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
        try (InputStream stream = Ipv4HeaderInspectorView.class.getResourceAsStream(resourcePath)) {
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
