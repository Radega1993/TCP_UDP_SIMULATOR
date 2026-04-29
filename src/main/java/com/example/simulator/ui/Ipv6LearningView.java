package com.example.simulator.ui;

import com.example.simulator.domain.ipv6.Ipv6AddressExample;
import com.example.simulator.domain.ipv6.Ipv6LearningModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

public class Ipv6LearningView extends VBox {
    public Ipv6LearningView(Runnable onHome, Runnable onTheory, Runnable onHelp) {
        setSpacing(14);
        setPadding(new Insets(0, 0, 14, 0));
        setStyle("-fx-background-color: #f4f7fb;");
        getChildren().addAll(buildTopbar(onHome, onTheory, onHelp), buildContent());
    }

    private Node buildTopbar(Runnable onHome, Runnable onTheory, Runnable onHelp) {
        StackPane menu = new StackPane(icon("/icons/menu.svg", 20));
        menu.setOnMouseClicked(event -> onHome.run());
        menu.setStyle("-fx-min-width: 34; -fx-min-height: 34; -fx-alignment: center;"
                + "-fx-background-color: #e8f1ff; -fx-background-radius: 10; -fx-cursor: hand;");

        Label brand = new Label("AulaRed");
        brand.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #102a43;");
        HBox brandBox = new HBox(12, menu, brand);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);

        Label modePill = pill("Módulo IP: IPv6 light", "#eef2ff", "#4f46e5");
        Button theory = ghostButton("Teoría", "/icons/info.svg");
        theory.setOnAction(event -> onTheory.run());
        Button help = ghostButton("Ayuda", "/icons/help.svg");
        help.setOnAction(event -> onHelp.run());

        HBox topbar = new HBox(16, brandBox, modePill, new HBox(8, theory, help));
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(0, 18, 0, 18));
        topbar.setMinHeight(58);
        topbar.setStyle("-fx-background-color: #ffffff;"
                + "-fx-border-color: transparent transparent #d9e6f2 transparent;"
                + "-fx-border-width: 0 0 1 0;"
                + "-fx-effect: dropshadow(gaussian, rgba(17,42,67,0.04), 12, 0.2, 0, 2);");
        return topbar;
    }

    private Node buildContent() {
        GridPane layout = new GridPane();
        layout.setHgap(14);
        layout.setVgap(14);
        layout.setPadding(new Insets(0, 14, 0, 14));
        layout.getColumnConstraints().setAll(growingColumn(840), fixedColumn(360));
        layout.add(new VBox(14, buildHeroCard(), buildComparisonCard(), buildExamplesCard()), 0, 0);
        layout.add(buildTheoryCard(), 1, 0);
        return layout;
    }

    private Node buildHeroCard() {
        DashboardCard card = new DashboardCard("IPV6 LIGHT", "IPv6 visual", "Una primera mirada a direcciones largas, tipos básicos y comparación con IPv4.");
        card.setStyle(cardStyle());
        Label title = new Label("IPv6 usa 128 bits y se escribe en 8 bloques hexadecimales.");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #102a43;");
        Label body = new Label("La idea clave para empezar: IPv4 e IPv6 cumplen la misma función lógica, pero IPv6 tiene muchísimo más espacio de direcciones y cambia conceptos como broadcast por multicast.");
        body.setWrapText(true);
        body.setStyle("-fx-text-fill: #5f7390; -fx-line-spacing: 3;");
        card.setContent(new HBox(16, iconTile("/icons/ip.svg", 54, "#eef2ff"), new VBox(8, title, body)));
        return card;
    }

    private Node buildComparisonCard() {
        DashboardCard card = new DashboardCard("COMPARATIVA", "IPv4 vs IPv6", null);
        card.setStyle(cardStyle());
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.getColumnConstraints().setAll(growingColumn(340), growingColumn(340));
        grid.add(compareColumn("IPv4", "192.168.1.10", "32 bits", "Decimal con 4 octetos", "Broadcast existe"), 0, 0);
        grid.add(compareColumn("IPv6", "2001:db8:1::10", "128 bits", "Hexadecimal con 8 bloques", "Usa multicast en lugar de broadcast"), 1, 0);
        card.setContent(grid);
        return card;
    }

    private Node buildExamplesCard() {
        DashboardCard card = new DashboardCard("TIPOS", "Unicast y multicast", "Dos formas básicas de entender a quién va dirigido el paquete.");
        card.setStyle(cardStyle());
        VBox examples = new VBox(12);
        for (Ipv6AddressExample example : Ipv6LearningModel.examples()) {
            examples.getChildren().add(exampleBlock(example));
        }
        card.setContent(examples);
        return card;
    }

    private Node buildTheoryCard() {
        DashboardCard card = new DashboardCard("TEORÍA", "Ideas clave", null);
        card.setStyle(cardStyle());
        card.setContent(new VBox(12,
                theoryItem("/icons/ip.svg", "Representación", "IPv6 se escribe en hexadecimal, separado por dos puntos."),
                theoryItem("/icons/binary.svg", "128 bits", "Tiene cuatro veces más bits que IPv4, pero el salto de espacio es enorme."),
                theoryItem("/icons/pc.svg", "Unicast", "Un paquete va a una sola interfaz."),
                theoryItem("/icons/network.svg", "Multicast", "Un paquete va a un grupo de interfaces interesadas."),
                theoryItem("/icons/x.svg", "Sin broadcast clásico", "IPv6 evita el broadcast general y usa multicast para descubrir vecinos o grupos.")
        ));
        return card;
    }

    private Node compareColumn(String title, String address, String bits, String format, String behavior) {
        VBox box = new VBox(10,
                sectionLabel(title),
                metric("Ejemplo", address),
                metric("Tamaño", bits),
                metric("Formato", format),
                metric("Entrega", behavior)
        );
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: #fbfdff; -fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10;");
        return box;
    }

    private Node exampleBlock(Ipv6AddressExample example) {
        Label type = sectionLabel(example.type());
        Label address = new Label(example.address());
        address.setWrapText(true);
        address.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-text-fill: #173452;");
        FlowPane hextets = new FlowPane(8, 8);
        for (String hextet : example.hextets()) {
            Label block = new Label(hextet);
            block.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-text-fill: #4f46e5;"
                    + "-fx-background-color: #eef2ff; -fx-border-color: #c7d2fe;"
                    + "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 10 8 10;");
            hextets.getChildren().add(block);
        }
        Label purpose = new Label(example.purpose());
        purpose.setWrapText(true);
        purpose.setStyle("-fx-text-fill: #5f7390;");
        VBox box = new VBox(10, type, address, hextets, purpose);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: #ffffff; -fx-border-color: #edf3f9; -fx-background-radius: 10; -fx-border-radius: 10;");
        return box;
    }

    private Node metric(String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-weight: 800;");
        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        HBox row = new HBox(8, keyLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        keyLabel.setMinWidth(72);
        return row;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #102a43;");
        return label;
    }

    private Node theoryItem(String iconPath, String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, iconTile(iconPath, 22, "#f0f6ff"), new VBox(3, titleLabel, bodyLabel));
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node iconTile(String path, double size, String background) {
        StackPane pane = new StackPane(icon(path, size));
        pane.setMinSize(size + 14, size + 14);
        pane.setMaxSize(size + 14, size + 14);
        pane.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 9;");
        return pane;
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
        label.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-padding: 7 16 7 16; -fx-font-weight: 900;");
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

    private Node icon(String path, double size) {
        Image image = load(path);
        if (image == null) {
            Rectangle fallback = new Rectangle(size, size, Color.web("#4f46e5"));
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
        try (InputStream stream = Ipv6LearningView.class.getResourceAsStream(resourcePath)) {
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
}
