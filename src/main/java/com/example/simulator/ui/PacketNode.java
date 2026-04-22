package com.example.simulator.ui;

import com.example.simulator.model.Endpoint;
import com.example.simulator.model.Packet;
import com.example.simulator.model.PacketKind;
import com.example.simulator.model.ProtocolType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PacketNode extends StackPane {
    private final Packet packet;
    private final Rectangle background;
    private final Label titleLabel;
    private final Label metaLabel;
    private final Label payloadLabel;
    private final String defaultMeta;
    private final Color baseColor;

    public PacketNode(Packet packet) {
        this.packet = packet;
        this.baseColor = colorFor(packet);
        this.defaultMeta = buildMeta(packet);

        background = new Rectangle(150, 82);
        background.setArcHeight(16);
        background.setArcWidth(16);
        background.setFill(baseColor);
        background.setStroke(Color.web("#334155"));
        background.setStrokeWidth(2.0);

        titleLabel = new Label(buildTitle(packet));
        titleLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 12px; -fx-font-weight: bold;");
        metaLabel = new Label(defaultMeta);
        metaLabel.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 11px; -fx-font-weight: bold;");
        payloadLabel = new Label(buildPayloadPreview(packet));
        payloadLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 11px;");
        payloadLabel.setWrapText(true);
        payloadLabel.setMaxWidth(136);

        VBox content = new VBox(3, titleLabel, metaLabel, payloadLabel);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(6));

        setAlignment(Pos.CENTER);
        getChildren().addAll(background, content);
        if (packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION) {
            markRetransmitted();
        }
    }

    private Color colorFor(Packet packet) {
        if (packet.getProtocolType() == ProtocolType.UDP) {
            return Color.web("#c4b5fd");
        }
        return switch (packet.getKind()) {
            case SYN -> Color.web("#93c5fd");
            case SYN_ACK -> Color.web("#60a5fa");
            case ACK -> Color.web("#86efac");
            case DATA -> Color.web("#7dd3fc");
            case FIN -> Color.web("#d1d5db");
            case UDP_DATAGRAM -> Color.web("#c4b5fd");
            case RETRANSMISSION -> Color.web("#fed7aa");
        };
    }

    private String buildTitle(Packet packet) {
        if (packet.getProtocolType() == ProtocolType.UDP) {
            return "UDP #" + packet.getSeq();
        }
        return switch (packet.getKind()) {
            case SYN -> "TCP SYN";
            case SYN_ACK -> "TCP SYN-ACK";
            case ACK -> "TCP ACK";
            case DATA -> "TCP DATA";
            case FIN -> "TCP FIN";
            case RETRANSMISSION -> "TCP RETRY";
            case UDP_DATAGRAM -> "UDP";
        };
    }

    private String buildMeta(Packet packet) {
        if (packet.getProtocolType() == ProtocolType.UDP) {
            return "DG: " + packet.getSeq();
        }
        return switch (packet.getKind()) {
            case ACK -> "ACK: " + packet.getAck();
            case DATA, RETRANSMISSION -> "SEQ: " + packet.getSeq() + " ACK: " + packet.getAck();
            default -> "SEQ: " + packet.getSeq() + " ACK: " + packet.getAck();
        };
    }

    private String buildPayloadPreview(Packet packet) {
        if (packet.getPayload() == null || packet.getPayload().isBlank()) {
            return "-";
        }
        String payload = packet.getPayload();
        if (payload.length() > 12) {
            payload = payload.substring(0, 12) + "...";
        }
        return "\"" + payload + "\"";
    }

    public void markDelivered() {
        boolean retried = packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION;
        background.setStroke(retried ? Color.web("#f97316") : Color.web("#16a34a"));
        background.setStrokeWidth(3.0);
        setOpacity(1.0);
        metaLabel.setText(defaultMeta + (retried ? " | RETRY DELIVERED" : " | DELIVERED"));
    }

    public void markLost() {
        background.setFill(Color.web("#fecaca"));
        background.setStroke(Color.web("#b91c1c"));
        background.setStrokeWidth(3.0);
        setOpacity(0.75);
        metaLabel.setText(defaultMeta + " | LOST");
    }

    public void markRetransmitted() {
        background.setStroke(Color.web("#f97316"));
        background.setStrokeWidth(3.2);
        metaLabel.setText(defaultMeta + " | RETRY");
    }

    public Packet getPacket() {
        return packet;
    }

    public Endpoint from() {
        return packet.getFrom();
    }
}
