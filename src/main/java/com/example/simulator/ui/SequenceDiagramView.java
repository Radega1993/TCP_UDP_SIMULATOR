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
    private final Label titleLabel = new Label("Diagrama secuencial");
    private final Pane contentPane = new Pane();
    private final ScrollPane scrollPane = new ScrollPane(contentPane);
    private final Map<String, SequenceDiagramEventViewModel> events = new LinkedHashMap<>();
    private final Consumer<String> detailsOpener;
    private String helpText = "Esta vista representa la comunicación como una secuencia temporal entre cliente y servidor. Es especialmente útil para analizar handshake, ACK, retransmisiones, orden de llegada y control de flujo.";

    public SequenceDiagramView(String title, Consumer<String> detailsOpener) {
        this.detailsOpener = detailsOpener;
        setSpacing(10);
        setPadding(new Insets(8));
        setStyle(UiTheme.PANEL_INSET_TINT);

        titleLabel.setText(title);
        titleLabel.setStyle(UiTheme.TITLE);

        Label help = new Label(helpText);
        help.setWrapText(true);
        help.setStyle(UiTheme.SUBTITLE);

        HBoxLike header = new HBoxLike();
        header.add(new LifelineHeader("Cliente"), LEFT_X - 48);
        header.add(new LifelineHeader("Servidor"), RIGHT_X - 52);

        contentPane.setPrefWidth(WIDTH);
        contentPane.setMinWidth(WIDTH);
        contentPane.setMaxWidth(WIDTH);
        contentPane.setStyle("-fx-background-color: #fbfdff; -fx-background-radius: 14;");

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(320);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        getChildren().addAll(titleLabel, help, header, scrollPane);
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
        double totalHeight = Math.max(280, 70 + (events.size() * 48));

        Line leftLine = new Line(LEFT_X, 18, LEFT_X, totalHeight - 16);
        leftLine.setStroke(Color.web("#cfd9e5"));
        leftLine.getStrokeDashArray().addAll(7.0, 6.0);
        Line rightLine = new Line(RIGHT_X, 18, RIGHT_X, totalHeight - 16);
        rightLine.setStroke(Color.web("#cfd9e5"));
        rightLine.getStrokeDashArray().addAll(7.0, 6.0);
        contentPane.getChildren().addAll(leftLine, rightLine);

        int index = 0;
        for (SequenceDiagramEventViewModel model : events.values()) {
            SequenceDiagramMessageNode node = new SequenceDiagramMessageNode(model, detailsOpener);
            node.setLayoutX(0);
            node.setLayoutY(28 + (index * 48));
            contentPane.getChildren().add(node);
            index++;
        }
        contentPane.setPrefHeight(totalHeight);
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

        private void add(Label label, double x) {
            label.setLayoutX(x);
            label.setLayoutY(0);
            getChildren().add(label);
        }
    }
}
