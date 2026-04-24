package com.example.simulator.ui;

import com.example.simulator.domain.simulation.Packet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SequenceDiagramView extends VBox {
    private static final double DEFAULT_WIDTH = 560;
    private static final double DEFAULT_LEFT_X = 110;
    private static final double DEFAULT_RIGHT_X = 430;
    private static final double VIEWPORT_HEIGHT = 390;
    private static final double EVENT_TOP = 12;
    private static final double EVENT_GAP = 31;
    private final Pane contentPane = new Pane();
    private final ScrollPane scrollPane = new ScrollPane(contentPane);
    private final Map<String, SequenceDiagramEventViewModel> events = new LinkedHashMap<>();
    private final Consumer<String> detailsOpener;
    private final double width;
    private final double leftX;
    private final double rightX;
    private double viewportHeight = VIEWPORT_HEIGHT;

    public SequenceDiagramView(String title, Consumer<String> detailsOpener) {
        this(title, detailsOpener, DEFAULT_WIDTH, DEFAULT_LEFT_X, DEFAULT_RIGHT_X);
    }

    public SequenceDiagramView(String title, Consumer<String> detailsOpener,
                               double width, double leftX, double rightX) {
        this.detailsOpener = detailsOpener;
        this.width = width;
        this.leftX = leftX;
        this.rightX = rightX;
        setSpacing(8);
        setPadding(new Insets(6, 0, 0, 0));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: transparent;");
        setViewportHeight(VIEWPORT_HEIGHT);

        HBoxLike header = new HBoxLike(width);
        header.addCentered(new LifelineHeader("Cliente"), leftX, 96);
        header.addCentered(new LifelineHeader("Servidor"), rightX, 104);

        contentPane.setPrefWidth(width);
        contentPane.setMinWidth(width);
        contentPane.setMaxWidth(width);
        contentPane.setPrefHeight(viewportHeight);
        contentPane.setMinHeight(viewportHeight);
        contentPane.setStyle("-fx-background-color: #fbfdff;");

        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinWidth(width);
        scrollPane.setPrefWidth(width);
        scrollPane.setMaxWidth(width);
        scrollPane.setPrefViewportWidth(width);
        scrollPane.setPrefViewportHeight(viewportHeight);
        scrollPane.setMinViewportHeight(viewportHeight);
        scrollPane.setMaxHeight(viewportHeight);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox diagramBody = new VBox(0, header, scrollPane);
        diagramBody.setAlignment(Pos.TOP_CENTER);
        diagramBody.setMinWidth(width);
        diagramBody.setPrefWidth(width);
        diagramBody.setMaxWidth(width);

        getChildren().add(diagramBody);
        redraw();
    }

    public void addPacket(Packet packet) {
        SequenceDiagramEventViewModel model = SequenceDiagramMapper.fromPacket(packet);
        events.put(model.getId(), model);
        redraw();
    }

    public void markPacketLost(Packet packet) {
        SequenceDiagramEventViewModel existing = events.get(packet.getId());
        if (existing != null) {
            events.put(packet.getId(), new SequenceDiagramEventViewModel(
                    existing.getId(), existing.getFrom(), existing.getTo(), existing.getLabel(),
                    UiTheme.LOST, packet, existing.getDetails(), true, existing.isRetransmitted()
            ));
            redraw();
        }
    }

    public void markPacketDelivered(Packet packet) {
        SequenceDiagramEventViewModel existing = events.get(packet.getId());
        if (existing != null && existing.isLost()) {
            events.put(packet.getId(), new SequenceDiagramEventViewModel(
                    existing.getId(), existing.getFrom(), existing.getTo(), existing.getLabel(),
                    SequenceDiagramMapper.fromPacket(packet).getColorHex(), packet, existing.getDetails(), false, existing.isRetransmitted()
            ));
            redraw();
        }
    }

    public void addLogEvent(String message, com.example.simulator.domain.protocol.ProtocolType protocolType) {
        SequenceDiagramMapper.fromLog(message, protocolType).ifPresent(model -> {
            events.put(model.getId() + "-" + events.size(), model);
            redraw();
        });
    }

    public void reset() {
        events.clear();
        redraw();
    }

    public void setViewportHeight(double height) {
        viewportHeight = Math.max(220, height);
        setMinHeight(viewportHeight + 34);
        setPrefHeight(viewportHeight + 34);
        setMaxHeight(viewportHeight + 34);
        contentPane.setPrefHeight(viewportHeight);
        contentPane.setMinHeight(viewportHeight);
        scrollPane.setPrefViewportHeight(viewportHeight);
        scrollPane.setMinViewportHeight(viewportHeight);
        scrollPane.setMaxHeight(viewportHeight);
        redraw();
    }

    private void redraw() {
        contentPane.getChildren().clear();
        int visibleEvents = events.size();
        double totalHeight = Math.max(viewportHeight, 32 + (visibleEvents * EVENT_GAP));
        contentPane.setPrefHeight(totalHeight);
        contentPane.setMinHeight(totalHeight);

        Line leftLine = new Line(leftX, 8, leftX, totalHeight - 12);
        leftLine.setStroke(Color.web("#cfd9e5"));
        leftLine.getStrokeDashArray().addAll(7.0, 6.0);
        Line rightLine = new Line(rightX, 8, rightX, totalHeight - 12);
        rightLine.setStroke(Color.web("#cfd9e5"));
        rightLine.getStrokeDashArray().addAll(7.0, 6.0);
        contentPane.getChildren().addAll(leftLine, rightLine);

        int index = 0;
        for (SequenceDiagramEventViewModel model : events.values()) {
            SequenceDiagramMessageNode node = new SequenceDiagramMessageNode(model, detailsOpener, width, leftX, rightX);
            node.setLayoutX(0);
            node.setLayoutY(EVENT_TOP + (index * EVENT_GAP));
            contentPane.getChildren().add(node);
            index++;
        }
    }

    private static class LifelineHeader extends Label {
        private LifelineHeader(String text) {
            super(text);
            setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #122033;");
        }
    }

    private static class HBoxLike extends Pane {
        private HBoxLike(double width) {
            setPrefWidth(width);
            setMinWidth(width);
            setMaxWidth(width);
            setPrefHeight(26);
        }

        private void addCentered(Label label, double centerX, double width) {
            label.setAlignment(Pos.CENTER);
            label.setPrefWidth(width);
            label.setMinWidth(width);
            label.setMaxWidth(width);
            label.setLayoutX(centerX - (width / 2.0));
            label.setLayoutY(0);
            getChildren().add(label);
        }
    }
}
