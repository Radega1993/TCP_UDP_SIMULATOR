package com.example.simulator.ui;

import com.example.simulator.application.dto.ComparisonSummary;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.SimulationResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ComparisonModeView extends BorderPane {
    private final ComparisonProtocolPane tcpPane;
    private final ComparisonProtocolPane udpPane;
    private final ComparisonSummaryPanel summaryPanel = new ComparisonSummaryPanel();
    private final TheoryComparisonPanel theoryPanel = new TheoryComparisonPanel();
    private final ComparisonLogPanel logPanel = new ComparisonLogPanel();

    public ComparisonModeView(Consumer<String> packetDetailsOpener) {
        setPadding(new Insets(0));
        setStyle("-fx-background-color: transparent;");

        DashboardCard headerCard = new DashboardCard(
                "COMPARACIÓN",
                "Comparación TCP vs UDP",
                "Misma entrada, misma red y misma temporización para observar diferencias de entrega, ACK y retransmisión."
        );
        headerCard.setStyle(UiTheme.HERO_CARD);
        setTop(headerCard);
        BorderPane.setMargin(headerCard, new Insets(0, 0, 18, 0));

        tcpPane = new ComparisonProtocolPane(ProtocolType.TCP, logPanel.getTcpLogArea(), packetDetailsOpener);
        udpPane = new ComparisonProtocolPane(ProtocolType.UDP, logPanel.getUdpLogArea(), packetDetailsOpener);
        tcpPane.setPrefWidth(600);
        udpPane.setPrefWidth(600);
        tcpPane.setMinWidth(560);
        udpPane.setMinWidth(560);
        tcpPane.setMaxWidth(Double.MAX_VALUE);
        udpPane.setMaxWidth(Double.MAX_VALUE);

        HBox simulationRow = new HBox(18, tcpPane, udpPane);
        simulationRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(tcpPane, Priority.ALWAYS);
        HBox.setHgrow(udpPane, Priority.ALWAYS);

        summaryPanel.setPrefWidth(420);
        summaryPanel.setMinWidth(360);
        summaryPanel.setMaxWidth(Double.MAX_VALUE);
        theoryPanel.setPrefWidth(520);
        theoryPanel.setMinWidth(420);
        theoryPanel.setMaxWidth(Double.MAX_VALUE);

        GridPane supportRow = new GridPane();
        supportRow.setHgap(18);
        supportRow.setVgap(0);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(42);
        left.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(58);
        right.setHgrow(Priority.ALWAYS);
        supportRow.getColumnConstraints().addAll(left, right);
        supportRow.add(summaryPanel, 0, 0);
        supportRow.add(theoryPanel, 1, 0);

        logPanel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(logPanel, Priority.NEVER);

        VBox centerColumn = new VBox(18, simulationRow, supportRow, logPanel);
        centerColumn.setPadding(new Insets(0));
        setCenter(centerColumn);

        Region bottomSpacer = new Region();
        bottomSpacer.setPrefHeight(4);
        setBottom(bottomSpacer);
    }

    public void start(SimulationResult tcpResult, SimulationResult udpResult, ComparisonSummary summary, double speedFactor) {
        summaryPanel.update(summary);
        tcpPane.loadAndPlay(tcpResult, speedFactor);
        udpPane.loadAndPlay(udpResult, speedFactor);
    }

    public void stop() {
        tcpPane.stop();
        udpPane.stop();
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
}
