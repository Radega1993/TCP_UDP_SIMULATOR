package com.example.simulator.ui;

import com.example.simulator.application.dto.ComparisonSummary;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.SimulationResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.function.Consumer;

public class ComparisonModeView extends VBox {
    private final ComparisonProtocolPane tcpPane;
    private final ComparisonProtocolPane udpPane;
    private final ComparisonSummaryPanel summaryPanel = new ComparisonSummaryPanel();
    private final TheoryComparisonPanel theoryPanel = new TheoryComparisonPanel();
    private final ComparisonLogPanel logPanel = new ComparisonLogPanel();
    private final Label statusTitle = new Label("Simulación preparada");
    private final Label statusText = new Label("TCP y UDP usarán el mismo mensaje y las mismas condiciones de red.");
    private final Label tcpDelivered = new Label("-");
    private final Label tcpLost = new Label("-");
    private final Label tcpRetrans = new Label("-");
    private final Label tcpEvents = new Label("-");
    private final Label udpDelivered = new Label("-");
    private final Label udpLost = new Label("-");
    private final Label udpRetrans = new Label("-");
    private final Label udpEvents = new Label("-");

    public ComparisonModeView(Consumer<String> packetDetailsOpener) {
        setSpacing(14);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #f3f7fb;");

        tcpPane = new ComparisonProtocolPane(ProtocolType.TCP, logPanel.getTcpLogArea(), packetDetailsOpener);
        udpPane = new ComparisonProtocolPane(ProtocolType.UDP, logPanel.getUdpLogArea(), packetDetailsOpener);
        prepareProtocolPane(tcpPane);
        prepareProtocolPane(udpPane);

        GridPane appGrid = new GridPane();
        appGrid.setHgap(14);
        appGrid.setVgap(14);
        appGrid.setStyle("-fx-background-color: #f3f7fb;");
        ColumnConstraints left = new ColumnConstraints(300, 300, 330);
        ColumnConstraints center = new ColumnConstraints(720, 980, Double.MAX_VALUE);
        center.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints(430, 450, 470);
        appGrid.getColumnConstraints().setAll(left, center, right);

        VBox leftPanel = buildLeftPanel();
        VBox mainPanel = buildMainPanel();
        VBox rightPanel = buildRightPanel();
        appGrid.add(leftPanel, 0, 0);
        appGrid.add(mainPanel, 1, 0);
        appGrid.add(rightPanel, 2, 0);
        GridPane.setHgrow(mainPanel, Priority.ALWAYS);

        getChildren().setAll(appGrid, buildLegend());
    }

    public void start(SimulationResult tcpResult, SimulationResult udpResult, ComparisonSummary summary, double speedFactor) {
        summaryPanel.update(summary);
        updateMetricLabels(summary);
        statusTitle.setText("Simulación en curso");
        statusText.setText("TCP se recupera de pérdidas y UDP muestra el efecto directo de la red compartida.");
        tcpPane.loadAndPlay(tcpResult, speedFactor);
        udpPane.loadAndPlay(udpResult, speedFactor);
    }

    public void stop() {
        tcpPane.stop();
        udpPane.stop();
        statusTitle.setText("Simulación detenida");
        statusText.setText("Reinicia la comparación para volver a ejecutar ambos protocolos.");
    }

    public void pause() {
        tcpPane.pause();
        udpPane.pause();
    }

    public void play() {
        tcpPane.play();
        udpPane.play();
    }

    public void stepForward() {
        tcpPane.stepForward();
        udpPane.stepForward();
    }

    public void setViewMode(SimulationViewMode mode) {
        tcpPane.setViewMode(mode);
        udpPane.setViewMode(mode);
    }

    public boolean hasRemainingEvents() {
        return tcpPane.hasRemainingEvents() || udpPane.hasRemainingEvents();
    }

    public boolean isPaused() {
        return tcpPane.isPaused() && udpPane.isPaused();
    }

    private VBox buildLeftPanel() {
        VBox panel = new VBox(12);
        panel.getChildren().addAll(
                sideCard("Configuración común", "Escenario", "Personalizado", "Mensaje", "Mismo texto para TCP y UDP"),
                sideCard("Condiciones de red", "Pérdida, latencia y jitter", "Compartidos", "Reordenación y duplicación", "Misma red para ambos"),
                sideCard("Parámetros TCP", "Ventana y buffer", "Solo afectan a TCP", "Fragmento", "Base común del mensaje"),
                sideCard("Visualización", "Vista", "Temporal / Paquetes", "Control", "Usa la barra superior")
        );
        return panel;
    }

    private VBox buildMainPanel() {
        DashboardCard comparisonCard = new DashboardCard(null, "Comparación en tiempo real", "Misma entrada, misma red y misma temporización para observar fiabilidad frente a simplicidad.");
        comparisonCard.setStyle(compareCardStyle());
        comparisonCard.setPadding(new Insets(16, 20, 14, 20));
        comparisonCard.setMinHeight(545);
        comparisonCard.setPrefHeight(545);

        Label info = new Label("TCP mantiene estado, ACK y retransmisiones. UDP envía datagramas sin conexión ni recuperación nativa.");
        info.setWrapText(true);
        info.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 13px;");

        HBox protocolSummary = new HBox(54,
                protocolBadge("/icons/tcp.png", "TCP", "Confiable", "#2f80ed", "#eaf3ff"),
                protocolBadge("/icons/udp.png", "UDP", "No confiable", "#8a55e6", "#f1eafe"));
        protocolSummary.setAlignment(Pos.CENTER);

        HBox lanes = new HBox(14, tcpPane, udpPane);
        lanes.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(tcpPane, Priority.ALWAYS);
        HBox.setHgrow(udpPane, Priority.ALWAYS);
        tcpPane.setMaxWidth(Double.MAX_VALUE);
        udpPane.setMaxWidth(Double.MAX_VALUE);

        comparisonCard.setContent(new VBox(12, info, protocolSummary, lanes));

        GridPane metrics = new GridPane();
        metrics.setHgap(8);
        metrics.setVgap(8);
        for (int i = 0; i < 8; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(12.5);
            column.setHgrow(Priority.ALWAYS);
            metrics.getColumnConstraints().add(column);
        }
        metrics.add(metricCard("TCP entregados", tcpDelivered, "#2f80ed"), 0, 0);
        metrics.add(metricCard("TCP pérdidas", tcpLost, "#ff4d4f"), 1, 0);
        metrics.add(metricCard("TCP retrans.", tcpRetrans, "#1fad6b"), 2, 0);
        metrics.add(metricCard("TCP eventos", tcpEvents, "#2f80ed"), 3, 0);
        metrics.add(metricCard("UDP entregados", udpDelivered, "#8a55e6"), 4, 0);
        metrics.add(metricCard("UDP pérdidas", udpLost, "#ff4d4f"), 5, 0);
        metrics.add(metricCard("UDP retrans.", udpRetrans, "#8a55e6"), 6, 0);
        metrics.add(metricCard("UDP eventos", udpEvents, "#8a55e6"), 7, 0);
        VBox.setMargin(metrics, new Insets(8, 0, 0, 0));

        GridPane charts = new GridPane();
        charts.setHgap(10);
        charts.setVgap(10);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            column.setHgrow(Priority.ALWAYS);
            charts.getColumnConstraints().add(column);
        }
        charts.add(miniChart("Ventana TCP", "#2f80ed"), 0, 0);
        charts.add(miniChart("Bytes en vuelo", "#2f80ed"), 1, 0);
        charts.add(miniChart("Ritmo de envío", "#8a55e6"), 2, 0);
        charts.add(miniChart("Pérdidas acumuladas", "#ff4d4f"), 3, 0);

        VBox panel = new VBox(12, comparisonCard, metrics, charts);
        panel.setMaxWidth(Double.MAX_VALUE);
        return panel;
    }

    private VBox buildRightPanel() {
        VBox panel = new VBox(12);
        summaryPanel.setStyle(compareCardStyle());
        logPanel.setStyle(compareCardStyle());
        logPanel.setMaxWidth(Double.MAX_VALUE);
        theoryPanel.setStyle(compareCardStyle());
        theoryPanel.setVisible(false);
        theoryPanel.setManaged(false);

        HBox success = new HBox(16);
        success.setAlignment(Pos.CENTER_LEFT);
        success.setPadding(new Insets(18));
        success.setStyle("-fx-background-color: #e8f9f0; -fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: #bfead2;");
        Label check = new Label("✓");
        check.setAlignment(Pos.CENTER);
        check.setStyle("-fx-min-width: 42; -fx-min-height: 42; -fx-background-color: #1fad6b; -fx-background-radius: 999; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: 900;");
        statusTitle.setStyle("-fx-text-fill: #087f4f; -fx-font-weight: 900;");
        statusText.setWrapText(true);
        statusText.setStyle("-fx-text-fill: #3f6b5c; -fx-font-size: 13px;");
        success.getChildren().addAll(check, new VBox(6, statusTitle, statusText));

        panel.getChildren().addAll(summaryPanel, logPanel, success);
        return panel;
    }

    private DashboardCard sideCard(String title, String labelA, String valueA, String labelB, String valueB) {
        DashboardCard card = new DashboardCard(null, title, null);
        card.setStyle(compareCardStyle());
        card.setPadding(new Insets(16));
        card.setContent(new VBox(10, sideRow(labelA, valueA), sideRow(labelB, valueB)));
        return card;
    }

    private Node sideRow(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #183653; -fx-font-weight: 800; -fx-font-size: 12px;");
        Label v = new Label(value);
        v.setWrapText(true);
        v.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px;");
        return new VBox(4, l, v);
    }

    private Node protocolBadge(String iconPath, String title, String subtitle, String color, String bg) {
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(38, 38);
        iconBox.setPrefSize(38, 38);
        iconBox.setMaxSize(38, 38);
        iconBox.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 999;");
        try (InputStream stream = getClass().getResourceAsStream(iconPath)) {
            if (stream != null) {
                ImageView icon = new ImageView(new Image(stream));
                icon.setFitWidth(22);
                icon.setFitHeight(22);
                icon.setPreserveRatio(true);
                iconBox.getChildren().add(icon);
            } else {
                Label fallback = new Label(title.substring(0, 1));
                fallback.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: 900;");
                iconBox.getChildren().add(fallback);
            }
        } catch (Exception ex) {
            Label fallback = new Label(title.substring(0, 1));
            fallback.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: 900;");
            iconBox.getChildren().add(fallback);
        }
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 21px; -fx-font-weight: 900;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px;");
        return new HBox(12, iconBox, new VBox(2, titleLabel, subtitleLabel));
    }

    private Node metricCard(String title, Label value, String accent) {
        value.setText("-");
        value.setStyle("-fx-text-fill: #102a43; -fx-font-size: 17px; -fx-font-weight: 900;");
        Label label = new Label(title);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 11px; -fx-font-weight: 900;");
        VBox card = new VBox(7, label, value, progress(accent));
        card.setPadding(new Insets(12));
        card.setMinHeight(92);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d9e6f2;");
        return card;
    }

    private Node progress(String accent) {
        Region fill = new Region();
        fill.setPrefWidth(52);
        fill.setMaxWidth(52);
        fill.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 999;");
        HBox bar = new HBox(fill);
        bar.setMinHeight(7);
        bar.setPrefHeight(7);
        bar.setStyle("-fx-background-color: #e9eef6; -fx-background-radius: 999;");
        return bar;
    }

    private Node miniChart(String title, String accent) {
        DashboardCard card = new DashboardCard(null, title, "(vista didáctica)");
        card.setStyle(compareCardStyle());
        card.setPadding(new Insets(14, 14, 8, 14));
        Region chart = new Region();
        chart.setMinHeight(96);
        chart.setPrefHeight(96);
        chart.setStyle("-fx-background-color: linear-gradient(to bottom, #eef4fb 1px, transparent 1px);"
                + "-fx-background-radius: 8; -fx-border-color: #eaf1f8; -fx-border-radius: 8;"
                + "-fx-border-width: 0 0 3 0; -fx-border-insets: 0 0 18 0;");
        Label line = new Label("━ ╱╲ ╱╲ ━");
        line.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 28px; -fx-font-weight: 900;");
        card.setContent(new VBox(0, chart, line));
        return card;
    }

    private Node buildLegend() {
        HBox legend = new HBox(28,
                new Label("Leyenda"),
                new Label("→ Envío"),
                new Label("← Respuesta"),
                new Label("↔ ACK"),
                new Label("▣ DATA"),
                new Label("✖ Pérdida"),
                new Label("↻ Retransmisión")
        );
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(16, 24, 16, 24));
        legend.setStyle(compareCardStyle());
        return legend;
    }

    private void prepareProtocolPane(ComparisonProtocolPane pane) {
        pane.setStyle(compareCardStyle());
        pane.setMinWidth(315);
        pane.setPrefWidth(370);
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.setMinHeight(374);
        pane.setPrefHeight(374);
        pane.setMaxHeight(374);
    }

    private void updateMetricLabels(ComparisonSummary summary) {
        tcpDelivered.setText(summary.isTcpDeliveredComplete() ? "Completo" : "Parcial");
        udpDelivered.setText(summary.isUdpDeliveredComplete() ? "Completo" : "Parcial");
        tcpLost.setText(String.valueOf(summary.getTcpLostPackets()));
        udpLost.setText(String.valueOf(summary.getUdpLostPackets()));
        tcpRetrans.setText(summary.isTcpRetransmitted() ? "Sí" : "No");
        udpRetrans.setText(summary.isUdpRetransmitted() ? "Sí" : "No");
        tcpEvents.setText(String.valueOf(summary.getTcpPacketCount()));
        udpEvents.setText(String.valueOf(summary.getUdpPacketCount()));
    }

    private String compareCardStyle() {
        return "-fx-background-color: #ffffff;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: #d9e6f2;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.035), 22, 0.20, 0, 8);";
    }
}
