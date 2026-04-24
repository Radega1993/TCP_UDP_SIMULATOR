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
    private static final double WIDTH = 560;
    private static final double LEFT_X = 110;
    private static final double RIGHT_X = 430;
    private static final double VIEWPORT_HEIGHT = 390;
    private static final double EVENT_TOP = 12;
    private static final double EVENT_GAP = 31;
    private final Pane contentPane = new Pane();
    private final ScrollPane scrollPane = new ScrollPane(contentPane);
    private final Map<String, SequenceDiagramEventViewModel> events = new LinkedHashMap<>();
    private final Consumer<String> detailsOpener;

    public SequenceDiagramView(String title, Consumer<String> detailsOpener) {
        this.detailsOpener = detailsOpener;
        setSpacing(8);
        setPadding(new Insets(6, 0, 0, 0));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: transparent;");
        setMinHeight(424);
        setPrefHeight(424);
        setMaxHeight(424);

        HBoxLike header = new HBoxLike();
        header.addCentered(new LifelineHeader("Cliente"), LEFT_X, 96);
        header.addCentered(new LifelineHeader("Servidor"), RIGHT_X, 104);

        contentPane.setPrefWidth(WIDTH);
        contentPane.setMinWidth(WIDTH);
        contentPane.setMaxWidth(WIDTH);
        contentPane.setPrefHeight(VIEWPORT_HEIGHT);
        contentPane.setMinHeight(VIEWPORT_HEIGHT);
        contentPane.setStyle("-fx-background-color: #fbfdff;");

        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinWidth(WIDTH);
        scrollPane.setPrefWidth(WIDTH);
        scrollPane.setMaxWidth(WIDTH);
        scrollPane.setPrefViewportWidth(WIDTH);
        scrollPane.setPrefViewportHeight(VIEWPORT_HEIGHT);
        scrollPane.setMinViewportHeight(VIEWPORT_HEIGHT);
        scrollPane.setMaxHeight(VIEWPORT_HEIGHT);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox diagramBody = new VBox(0, header, scrollPane);
        diagramBody.setAlignment(Pos.TOP_CENTER);
        diagramBody.setMinWidth(WIDTH);
        diagramBody.setPrefWidth(WIDTH);
        diagramBody.setMaxWidth(WIDTH);

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

    private void redraw() {
        contentPane.getChildren().clear();
        int visibleEvents = events.size();
        double totalHeight = Math.max(VIEWPORT_HEIGHT, 32 + (visibleEvents * EVENT_GAP));
        contentPane.setPrefHeight(totalHeight);
        contentPane.setMinHeight(totalHeight);

        Line leftLine = new Line(LEFT_X, 8, LEFT_X, totalHeight - 12);
        leftLine.setStroke(Color.web("#cfd9e5"));
        leftLine.getStrokeDashArray().addAll(7.0, 6.0);
        Line rightLine = new Line(RIGHT_X, 8, RIGHT_X, totalHeight - 12);
        rightLine.setStroke(Color.web("#cfd9e5"));
        rightLine.getStrokeDashArray().addAll(7.0, 6.0);
        contentPane.getChildren().addAll(leftLine, rightLine);

        int index = 0;
        for (SequenceDiagramEventViewModel model : events.values()) {
            SequenceDiagramMessageNode node = new SequenceDiagramMessageNode(model, detailsOpener);
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
        private HBoxLike() {
            setPrefWidth(WIDTH);
            setMinWidth(WIDTH);
            setMaxWidth(WIDTH);
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
