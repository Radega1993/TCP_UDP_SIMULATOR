package com.example.simulator.ui;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.PacketStatus;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.Packet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PacketNode extends StackPane {
    private final Packet packet;
    private final Rectangle background;
    private final Label titleLabel;
    private final Label metaLabel;
    private final Label payloadLabel;
    private final String defaultMeta;

    public PacketNode(Packet packet) {
        this.packet = packet;
        this.defaultMeta = buildMeta(packet);

        background = new Rectangle(118, 70);
        background.setArcHeight(18);
        background.setArcWidth(18);
        background.setFill(colorFor(packet));
        background.setStroke(Color.web("#35506b"));
        background.setStrokeWidth(1.5);

        titleLabel = new Label(buildTitle(packet));
        titleLabel.setStyle("-fx-text-fill: #102033; -fx-font-size: 11px; -fx-font-weight: bold;");
        metaLabel = new Label(defaultMeta);
        metaLabel.setStyle("-fx-text-fill: #304255; -fx-font-size: 9px; -fx-font-weight: bold;");
        payloadLabel = new Label(buildPayloadPreview(packet));
        payloadLabel.setStyle("-fx-text-fill: #15283c; -fx-font-size: 10px;");
        payloadLabel.setWrapText(true);
        payloadLabel.setMaxWidth(98);

        VBox content = new VBox(3, titleLabel, metaLabel, payloadLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(6, 8, 6, 8));

        setAlignment(Pos.CENTER);
        getChildren().addAll(background, content);
        setStyle("-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.12), 12, 0.18, 0, 4);");
        if (packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION) {
            markRetransmitted();
        }
    }

    private Color colorFor(Packet packet) {
        if (packet.getProtocolType() == ProtocolType.UDP) {
            return Color.web(UiTheme.UDP);
        }
        return switch (packet.getKind()) {
            case SYN -> Color.web(UiTheme.TCP_SYN);
            case SYN_ACK -> Color.web(UiTheme.TCP_SYN_ACK);
            case ACK -> Color.web(UiTheme.TCP_ACK);
            case DATA -> Color.web(UiTheme.TCP_DATA);
            case FIN -> Color.web("#d7dee7");
            case UDP_DATAGRAM -> Color.web(UiTheme.UDP);
            case RETRANSMISSION -> Color.web(UiTheme.RETRY);
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
            return "DG " + packet.getSeq();
        }
        return switch (packet.getKind()) {
            case ACK -> "ACK " + packet.getAck();
            case DATA, RETRANSMISSION -> "SEQ " + packet.getSeq() + " · ACK " + packet.getAck();
            default -> "SEQ " + packet.getSeq() + " · ACK " + packet.getAck();
        };
    }

    private String buildPayloadPreview(Packet packet) {
        if (packet.getPayload() == null || packet.getPayload().isBlank()) {
            return "Sin carga útil";
        }
        String payload = packet.getPayload();
        if (payload.length() > 10) {
            payload = payload.substring(0, 10) + "...";
        }
        return "\"" + payload + "\"";
    }

    public void markDelivered() {
        packet.setStatus(PacketStatus.DELIVERED);
        boolean retried = packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION;
        background.setStroke(retried ? Color.web("#f97316") : Color.web("#16a34a"));
        background.setStrokeWidth(2.8);
        setOpacity(1.0);
        metaLabel.setText(defaultMeta + (retried ? " · OK RETRY" : " · OK"));
    }

    public void markLost() {
        packet.setStatus(PacketStatus.LOST);
        background.setFill(Color.web(UiTheme.LOST));
        background.setStroke(Color.web("#b91c1c"));
        background.setStrokeWidth(2.8);
        setOpacity(0.8);
        metaLabel.setText(defaultMeta + " · LOST");
    }

    public void markRetransmitted() {
        background.setStroke(Color.web("#f97316"));
        background.setStrokeWidth(3.0);
        metaLabel.setText(defaultMeta + " · RETRY");
    }

    public Packet getPacket() {
        return packet;
    }

    public Endpoint from() {
        return packet.getFrom();
    }
}
