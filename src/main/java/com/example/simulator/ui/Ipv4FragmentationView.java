package com.example.simulator.ui;

import com.example.simulator.domain.ipv4.Ipv4Fragment;
import com.example.simulator.domain.ipv4.Ipv4FragmentationResult;
import com.example.simulator.domain.ipv4.Ipv4FragmentationSimulator;
import com.example.simulator.domain.ipv4.Ipv4SubnetResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

public class Ipv4FragmentationView extends GridPane {
    private final Label mtuLabel = new Label();
    private final Label packetLabel = new Label();
    private final Label payloadLabel = new Label();
    private final Label decisionLabel = new Label();
    private final HBox fragmentBars = new HBox(8);
    private final VBox fragmentRows = new VBox(8);
    private final Label pathLabel = new Label();
    private Ipv4SubnetResult currentSubnet;

    public Ipv4FragmentationView() {
        setHgap(14);
        setVgap(14);
        getColumnConstraints().setAll(growingColumn(820), fixedColumn(360));
        add(buildFragmentationCard(), 0, 0);
        add(buildTheoryCard(), 1, 0);
        renderIdle();
    }

    public void updateContext(Ipv4SubnetResult subnet, int mtu) {
        currentSubnet = subnet;
        Ipv4FragmentationResult result = Ipv4FragmentationSimulator.fragment(mtu);
        render(result);
    }

    public void renderError(String message) {
        mtuLabel.setText("-");
        packetLabel.setText("-");
        payloadLabel.setText("-");
        pathLabel.setText("-");
        decisionLabel.setText(message);
        decisionLabel.setStyle(statusStyle("#fff0f0", "#b91c1c"));
        fragmentBars.getChildren().setAll(emptyHint("Corrige los datos para simular fragmentación."));
        fragmentRows.getChildren().setAll(emptyHint("No se puede calcular offsets ni flags MF todavía."));
    }

    private Node buildFragmentationCard() {
        DashboardCard card = new DashboardCard("FRAGMENTACIÓN IP", "MTU y fragmentos", "Cuando el paquete no cabe en el enlace, IPv4 puede dividirlo en fragmentos.");
        card.setStyle(cardStyle());

        GridPane summary = new GridPane();
        summary.setHgap(10);
        summary.getColumnConstraints().setAll(growingColumn(170), growingColumn(170), growingColumn(170));
        summary.add(summaryBox("MTU del enlace", mtuLabel, "/icons/network.svg"), 0, 0);
        summary.add(summaryBox("Paquete original", packetLabel, "/icons/ip.svg"), 1, 0);
        summary.add(summaryBox("Datos IP", payloadLabel, "/icons/binary.svg"), 2, 0);

        DashboardCard visual = new DashboardCard(null, "Visualización de fragmentos", null);
        visual.setHeaderVisible(false);
        visual.setStyle(innerCardStyle());
        fragmentBars.setAlignment(Pos.CENTER_LEFT);
        visual.setContent(fragmentBars);

        DashboardCard table = new DashboardCard(null, "Fragmento | Offset | Tamaño | MF", null);
        table.setHeaderVisible(false);
        table.setStyle(innerCardStyle());
        table.setContent(new VBox(8, tableHeader(), fragmentRows));

        pathLabel.setWrapText(true);
        pathLabel.setStyle(statusStyle("#f8fbff", "#345573"));
        decisionLabel.setWrapText(true);
        card.setContent(new VBox(14, summary, pathLabel, visual, table, decisionLabel));
        return card;
    }

    private Node buildTheoryCard() {
        DashboardCard card = new DashboardCard("TEORÍA", "¿Por qué se fragmenta?", null);
        card.setStyle(cardStyle());
        card.setContent(new VBox(12,
                theoryItem("/icons/network.svg", "Qué es MTU", "Es el tamaño máximo de trama que puede transportar un enlace."),
                theoryItem("/icons/ip.svg", "Paquete demasiado grande", "Si el paquete IP supera la MTU, no cabe entero en ese enlace."),
                theoryItem("/icons/binary.svg", "Offset", "Indica en qué posición empiezan los datos de cada fragmento. Se mide en bloques de 8 bytes."),
                theoryItem("/icons/check.svg", "Flag MF", "MF significa More Fragments. Está activo mientras quedan fragmentos por llegar.")
        ));
        return card;
    }

    private void render(Ipv4FragmentationResult result) {
        mtuLabel.setText(result.mtuBytes() + " bytes");
        packetLabel.setText(result.originalPacketSizeBytes() + " bytes");
        payloadLabel.setText(result.payloadSizeBytes() + " bytes de datos");
        pathLabel.setText(currentSubnet.sourceIp() + " → " + currentSubnet.destinationIp()
                + " · Cabecera IP " + result.headerSizeBytes() + " B · Máximo de datos por fragmento: "
                + result.maxDataPerFragmentBytes() + " B");
        if (result.fragmented()) {
            decisionLabel.setText("El paquete original no cabe en la MTU. Se divide en "
                    + result.fragments().size() + " fragmentos; todos salvo el último llevan MF=1.");
            decisionLabel.setStyle(statusStyle("#fff7e8", "#b45309"));
        } else {
            decisionLabel.setText("El paquete cabe completo en la MTU. No hace falta fragmentarlo.");
            decisionLabel.setStyle(statusStyle("#e7f8ef", "#087f4f"));
        }
        fragmentBars.getChildren().setAll(result.fragments().stream().map(fragment -> fragmentBar(fragment, result)).toArray(Node[]::new));
        fragmentRows.getChildren().setAll(result.fragments().stream().map(this::fragmentRow).toArray(Node[]::new));
    }

    private Node fragmentBar(Ipv4Fragment fragment, Ipv4FragmentationResult result) {
        double width = Math.max(110, 360.0 * fragment.dataSizeBytes() / result.payloadSizeBytes());
        Label number = new Label("F" + fragment.number());
        number.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label size = new Label(fragment.totalSizeBytes() + " B");
        size.setStyle("-fx-font-size: 11px; -fx-text-fill: #5f7390; -fx-font-weight: 800;");
        Label mf = new Label(fragment.moreFragments() ? "MF=1" : "MF=0");
        mf.setStyle(badgeStyle(fragment.moreFragments() ? "#eaf3ff" : "#e7f8ef", fragment.moreFragments() ? "#2f80ed" : "#087f4f"));
        VBox box = new VBox(6, number, size, mf);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(width);
        box.setMaxWidth(width);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + (fragment.moreFragments() ? "#f2f7ff" : "#f1fff7") + ";"
                + "-fx-border-color: " + (fragment.moreFragments() ? "#a9ccff" : "#7bd8a6") + ";"
                + "-fx-background-radius: 10; -fx-border-radius: 10;");
        return box;
    }

    private Node tableHeader() {
        HBox row = new HBox(8,
                tableCell("Fragmento", 95, true),
                tableCell("Offset", 150, true),
                tableCell("Tamaño", 140, true),
                tableCell("MF", 90, true)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node fragmentRow(Ipv4Fragment fragment) {
        HBox row = new HBox(8,
                tableCell("F" + fragment.number(), 95, false),
                tableCell(fragment.offsetUnits() + " (" + fragment.offsetBytes() + " B)", 150, false),
                tableCell(fragment.totalSizeBytes() + " B", 140, false),
                tableCell(fragment.moreFragments() ? "1" : "0", 90, false)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: " + (fragment.moreFragments() ? "#f8fbff" : "#e7f8ef") + ";"
                + "-fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10;");
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
        mtuLabel.setText("-");
        packetLabel.setText("-");
        payloadLabel.setText("-");
        pathLabel.setText("Configura la MTU para ver si el paquete se divide.");
        decisionLabel.setText("La fragmentación aparecerá aquí.");
        decisionLabel.setStyle(statusStyle("#f8fbff", "#5f7390"));
        fragmentBars.getChildren().setAll(emptyHint("Los fragmentos aparecerán como bloques."));
        fragmentRows.getChildren().setAll(emptyHint("La tabla mostrará offset, tamaño y MF."));
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
        try (InputStream stream = Ipv4FragmentationView.class.getResourceAsStream(resourcePath)) {
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

    private String badgeStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-padding: 5 10 5 10; -fx-font-weight: 900;";
    }
}
