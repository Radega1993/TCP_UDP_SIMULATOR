package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.Group;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelsComparisonPanel extends VBox {
    private static final String APP_BG = "#eaf8f0";
    private static final String APP_BORDER = "#a6e3c0";
    private static final String TRANSPORT_BG = "#eef6ff";
    private static final String TRANSPORT_BORDER = "#a9ccff";
    private static final String INTERNET_BG = "#f3ecff";
    private static final String INTERNET_BORDER = "#c9b1ff";
    private static final String LINK_BG = "#fff1e3";
    private static final String LINK_BORDER = "#ffc78b";
    private static final String PHYSICAL_BG = "#fff0ef";
    private static final String PHYSICAL_BORDER = "#ffc0bd";
    private static final String TEXT = "#102a43";
    private static final String MUTED = "#5f7390";

    public ModelsComparisonPanel() {
        setSpacing(16);
    }

    public void setLearningLevel(LearningLevel level) {
        DashboardCard shell = new DashboardCard(null, "Comparación de modelos", null);
        shell.setPadding(new Insets(20));
        shell.setStyle(cardStyle());

        HBox title = new HBox(8);
        title.setAlignment(Pos.CENTER_LEFT);
        Label h3 = new Label("Comparación de modelos");
        h3.setStyle(sectionTitleStyle());
        Label info = new Label("ⓘ");
        info.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 14px;");
        title.getChildren().addAll(h3, info);

        GridPane models = new GridPane();
        models.setHgap(18);
        ColumnConstraints tcp = new ColumnConstraints(260, 285, Double.MAX_VALUE);
        tcp.setHgrow(Priority.ALWAYS);
        ColumnConstraints bridge = new ColumnConstraints(120, 130, 140);
        ColumnConstraints osi = new ColumnConstraints(260, 285, Double.MAX_VALUE);
        osi.setHgrow(Priority.ALWAYS);
        models.getColumnConstraints().setAll(tcp, bridge, osi);
        models.add(tcpIpColumn(), 0, 0);
        models.add(bridgeColumn(), 1, 0);
        models.add(osiColumn(), 2, 0);

        VBox content = new VBox(16, title, models, pduCard());
        shell.setContent(content);
        getChildren().setAll(shell);
    }

    private VBox tcpIpColumn() {
        VBox column = new VBox(0);
        column.getChildren().addAll(
                modelHeading("/icons/layers.svg", "Modelo TCP/IP", "4 capas", "#2f80ed", 48),
                tcpLayer("4. Aplicación", "Proporciona servicios de red a las aplicaciones.",
                        "Ejemplos: HTTP, DNS, DHCP, FTP", "/icons/app.svg", APP_BG, APP_BORDER, "#19a663", 136),
                spacer(16),
                tcpLayer("3. Transporte", "Comunicación extremo a extremo, control de flujo y errores.",
                        "Ejemplos: TCP, UDP", "/icons/transport.svg", TRANSPORT_BG, TRANSPORT_BORDER, "#2f80ed", 136),
                spacer(16),
                tcpLayer("2. Internet", "Direccionamiento lógico y enrutamiento de paquetes.",
                        "Ejemplos: IP, ICMP", "/icons/router.svg", INTERNET_BG, INTERNET_BORDER, "#8a55e6", 136),
                spacer(16),
                tcpLayer("1. Acceso a red", "Envío de datos a través del medio físico en la red local.",
                        "Ejemplos: Ethernet, Wi-Fi, ARP", "/icons/network.svg", LINK_BG, LINK_BORDER, "#ff8b1a", 136)
        );
        return column;
    }

    private VBox osiColumn() {
        VBox column = new VBox(0);
        column.getChildren().addAll(
                modelHeading("/icons/layers.svg", "Modelo OSI", "7 capas", "#8a55e6", 54),
                osiRow("7", "Aplicación", "Servicios de red a las aplicaciones. Ejemplos: HTTP, DNS, DHCP, FTP", "/icons/app.svg", APP_BG, APP_BORDER, "#19a663"),
                osiRow("6", "Presentación", "Traduce, cifra y comprime los datos. Ejemplos: TLS, SSL, JPEG, ASCII", "/icons/code.svg", APP_BG, APP_BORDER, "#19a663"),
                osiRow("5", "Sesión", "Establece, mantiene y termina sesiones. Ejemplos: NetBIOS, RPC, SIP", "/icons/users.svg", APP_BG, APP_BORDER, "#19a663"),
                osiRow("4", "Transporte", "Comunicación extremo a extremo. Ejemplos: TCP, UDP", "/icons/transport.svg", TRANSPORT_BG, TRANSPORT_BORDER, "#2f80ed"),
                osiRow("3", "Red", "Direccionamiento y enrutamiento. Ejemplos: IP, ICMP", "/icons/router.svg", INTERNET_BG, INTERNET_BORDER, "#8a55e6"),
                osiRow("2", "Enlace de datos", "Control de acceso al medio y detección de errores. Ejemplos: Ethernet, MAC, PPP", "/icons/network.svg", LINK_BG, LINK_BORDER, "#ff8b1a"),
                osiRow("1", "Física", "Transmisión de bits por el medio físico. Ejemplos: 100BASE-TX, Wi-Fi, Cable", "/icons/physical.svg", PHYSICAL_BG, PHYSICAL_BORDER, "#ff5c5c")
        );
        return column;
    }

    private VBox bridgeColumn() {
        VBox column = new VBox(0);
        column.setAlignment(Pos.TOP_CENTER);
        column.setPadding(new Insets(58, 0, 0, 0));
        Label title = new Label("Equivalencia\nentre modelos");
        title.setAlignment(Pos.CENTER);
        title.setStyle("-fx-text-fill: #173452; -fx-font-size: 15px; -fx-font-weight: 800;");
        column.getChildren().addAll(
                title,
                spacer(24),
                bridge("Se corresponde con", "#19a663", 156),
                spacer(26),
                bridge("Se corresponde con", "#2f80ed", 84),
                spacer(26),
                bridge("Se corresponde con", "#8a55e6", 84),
                spacer(26),
                bridge("Se corresponde con", "#ff8b1a", 146)
        );
        return column;
    }

    private Node modelHeading(String iconPath, String title, String subtitle, String color, double leftPadding) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(58);
        row.setPadding(new Insets(0, 0, 16, leftPadding));
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: 900;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 13px;");
        row.getChildren().addAll(iconBox(iconPath, 34, "#eff6ff"), new VBox(4, titleLabel, subtitleLabel));
        return row;
    }

    private Node tcpLayer(String title, String body, String examples, String iconPath,
                          String bg, String border, String accent, double height) {
        GridPane card = new GridPane();
        card.setHgap(16);
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(height);
        card.setPrefHeight(height);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-border-color: " + border + ";");
        ColumnConstraints text = new ColumnConstraints();
        text.setHgrow(Priority.ALWAYS);
        ColumnConstraints icon = new ColumnConstraints(62, 62, 62);
        card.getColumnConstraints().setAll(text, icon);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 17px; -fx-font-weight: 900;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #345573; -fx-font-size: 13px;");
        Label examplesLabel = new Label(examples);
        examplesLabel.setWrapText(true);
        examplesLabel.setStyle("-fx-text-fill: #203f60; -fx-font-size: 12px; -fx-font-weight: 800;");
        card.add(new VBox(9, titleLabel, bodyLabel, examplesLabel), 0, 0);
        card.add(iconBox(iconPath, 54, "rgba(255,255,255,0.7)"), 1, 0);
        return card;
    }

    private Node osiRow(String number, String title, String body, String iconPath,
                        String bg, String border, String accent) {
        GridPane row = new GridPane();
        row.setHgap(12);
        row.setAlignment(Pos.CENTER);
        row.setMinHeight(76);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8;"
                + "-fx-border-radius: 8; -fx-border-color: " + border + ";");
        ColumnConstraints numberCol = new ColumnConstraints(34, 34, 34);
        ColumnConstraints textCol = new ColumnConstraints();
        textCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints iconCol = new ColumnConstraints(46, 46, 46);
        row.getColumnConstraints().setAll(numberCol, textCol, iconCol);

        Label numberLabel = new Label(number);
        numberLabel.setAlignment(Pos.CENTER);
        numberLabel.setMinSize(30, 30);
        numberLabel.setPrefSize(30, 30);
        numberLabel.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 6;"
                + "-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: 900;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 13px; -fx-font-weight: 900;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #345573; -fx-font-size: 12px;");
        row.add(numberLabel, 0, 0);
        row.add(new VBox(4, titleLabel, bodyLabel), 1, 0);
        row.add(iconBox(iconPath, 42, "rgba(255,255,255,0.75)"), 2, 0);
        VBox.setMargin(row, new Insets(0, 0, 6, 0));
        return row;
    }

    private Node bridge(String text, String color, double height) {
        StackPane pane = new StackPane();
        pane.setMinHeight(height);
        pane.setPrefHeight(height);
        Label line = new Label("────     ────");
        line.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: 900;");
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: 800;");
        pane.getChildren().addAll(line, label);
        return pane;
    }

    private Node pduCard() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12;"
                + "-fx-border-radius: 12; -fx-border-color: #d9e6f2;");
        Label title = new Label("PDU en cada modelo");
        title.setStyle(sectionTitleStyle());
        Label subtitle = new Label("Unidad de datos en cada capa");
        subtitle.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 13px;");

        GridPane table = new GridPane();
        table.setVgap(0);
        String[] headers = {"Capa TCP/IP", "PDU", "Capa OSI equivalente", "PDU"};
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(i == 2 ? 35 : i == 1 ? 25 : 20);
            col.setHgrow(Priority.ALWAYS);
            table.getColumnConstraints().add(col);
            table.add(tableCell(headers[i], true), i, 0);
        }
        addPduRow(table, 1, "Aplicación", "Datos", "7. Aplicación / 6. Presentación / 5. Sesión", "Datos", "#19a663");
        addPduRow(table, 2, "Transporte", "Segmento (TCP) / Datagrama (UDP)", "4. Transporte", "Segmento", "#2f80ed");
        addPduRow(table, 3, "Internet", "Paquete (IP)", "3. Red", "Paquete", "#8a55e6");
        addPduRow(table, 4, "Acceso a Red", "Trama", "2. Enlace de datos", "Trama", "#ff8b1a");
        addPduRow(table, 5, "Física", "Bits", "1. Física", "Bits", "#ff5c5c");
        box.getChildren().addAll(title, subtitle, table);
        return box;
    }

    private void addPduRow(GridPane table, int row, String a, String b, String c, String d, String dot) {
        table.add(tableCell("●  " + a, false, dot), 0, row);
        table.add(tableCell(b, false), 1, row);
        table.add(tableCell(c, false), 2, row);
        table.add(tableCell(d, false), 3, row);
    }

    private Label tableCell(String text, boolean header) {
        return tableCell(text, header, header ? TEXT : "#294766");
    }

    private Label tableCell(String text, boolean header, String color) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(10));
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + (header ? 12 : 13)
                + "px; -fx-font-weight: " + (header ? 800 : 500)
                + "; -fx-border-color: #edf2f7; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private Node iconBox(String path, double size, String bg) {
        StackPane box = new StackPane();
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        box.setPadding(new Insets(size > 45 ? 12 : 8));
        box.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8;");
        Node icon = svgIcon(path, size - 18);
        if (icon != null) {
            box.getChildren().add(icon);
        } else {
            Label fallback = new Label(glyphFor(path));
            fallback.setStyle("-fx-text-fill: #294766; -fx-font-size: " + Math.max(15, size / 2.4)
                    + "px; -fx-font-weight: 900;");
            box.getChildren().add(fallback);
        }
        return box;
    }

    private Node svgIcon(String path, double targetSize) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null || !path.endsWith(".svg")) {
                return null;
            }
            String svg = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            Group group = new Group();
            Color stroke = Color.web(attr(svg, "stroke", "#294766"));
            double strokeWidth = doubleAttr(svg, "stroke-width", 2.0);
            StrokeLineCap lineCap = "round".equals(attr(svg, "stroke-linecap", ""))
                    ? StrokeLineCap.ROUND : StrokeLineCap.BUTT;
            StrokeLineJoin lineJoin = "round".equals(attr(svg, "stroke-linejoin", ""))
                    ? StrokeLineJoin.ROUND : StrokeLineJoin.MITER;

            Matcher matcher = Pattern.compile("<(path|rect|circle)\\b([^>]*)>").matcher(svg);
            while (matcher.find()) {
                Shape shape = switch (matcher.group(1)) {
                    case "path" -> pathShape(matcher.group(2));
                    case "rect" -> rectShape(matcher.group(2));
                    case "circle" -> circleShape(matcher.group(2));
                    default -> null;
                };
                if (shape != null) {
                    shape.setFill(Color.TRANSPARENT);
                    shape.setStroke(stroke);
                    shape.setStrokeWidth(strokeWidth);
                    shape.setStrokeLineCap(lineCap);
                    shape.setStrokeLineJoin(lineJoin);
                    group.getChildren().add(shape);
                }
            }
            if (group.getChildren().isEmpty()) {
                return null;
            }
            double scale = targetSize / 24.0;
            group.setScaleX(scale);
            group.setScaleY(scale);
            return group;
        } catch (IOException | IllegalArgumentException ignored) {
            return null;
        }
    }

    private Shape pathShape(String tag) {
        String d = attr(tag, "d", "");
        if (d.isBlank()) {
            return null;
        }
        SVGPath shape = new SVGPath();
        shape.setContent(d);
        return shape;
    }

    private Shape rectShape(String tag) {
        Rectangle shape = new Rectangle(
                doubleAttr(tag, "x", 0),
                doubleAttr(tag, "y", 0),
                doubleAttr(tag, "width", 0),
                doubleAttr(tag, "height", 0)
        );
        double rx = doubleAttr(tag, "rx", 0);
        shape.setArcWidth(rx * 2);
        shape.setArcHeight(rx * 2);
        return shape;
    }

    private Shape circleShape(String tag) {
        return new Circle(
                doubleAttr(tag, "cx", 0),
                doubleAttr(tag, "cy", 0),
                doubleAttr(tag, "r", 0)
        );
    }

    private String attr(String source, String name, String fallback) {
        Matcher matcher = Pattern.compile(name + "=\"([^\"]*)\"").matcher(source);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private double doubleAttr(String source, String name, double fallback) {
        String value = attr(source, name, "");
        if (value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String glyphFor(String path) {
        if (path.contains("app")) {
            return "▦";
        }
        if (path.contains("transport")) {
            return "⇄";
        }
        if (path.contains("router")) {
            return "⌘";
        }
        if (path.contains("network")) {
            return "▱";
        }
        if (path.contains("physical")) {
            return "⌁";
        }
        if (path.contains("code")) {
            return "{ }";
        }
        if (path.contains("users")) {
            return "◉";
        }
        return "▤";
    }

    private Region spacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        region.setPrefHeight(height);
        return region;
    }

    private String cardStyle() {
        return "-fx-background-color: #ffffff; -fx-background-radius: 14;"
                + "-fx-border-radius: 14; -fx-border-color: #d9e6f2;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.035), 22, 0.20, 0, 8);";
    }

    private String sectionTitleStyle() {
        return "-fx-text-fill: #0064ff; -fx-font-size: 14px; -fx-font-weight: 900;";
    }
}
