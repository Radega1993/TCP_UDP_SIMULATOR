package com.example.simulator.ui;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.Packet;

import java.util.Locale;
import java.util.Optional;

public final class SequenceDiagramMapper {
    private SequenceDiagramMapper() {
    }

    public static SequenceDiagramEventViewModel fromPacket(Packet packet) {
        return new SequenceDiagramEventViewModel(
                packet.getId(),
                packet.getFrom(),
                packet.getTo(),
                buildPacketLabel(packet),
                colorFor(packet),
                packet,
                buildDetails(packet),
                false,
                packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION
        );
    }

    public static Optional<SequenceDiagramEventViewModel> fromLog(String message, ProtocolType protocolType) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("duplicate ack")) {
            return Optional.of(new SequenceDiagramEventViewModel(
                    "log-dup-" + message.hashCode(),
                    Endpoint.SERVER,
                    Endpoint.CLIENT,
                    "Duplicate ACK",
                    "#4ade80",
                    null,
                    message,
                    false,
                    false
            ));
        }
        if (lower.contains("timeout")) {
            return Optional.of(new SequenceDiagramEventViewModel(
                    "log-timeout-" + message.hashCode(),
                    Endpoint.CLIENT,
                    Endpoint.SERVER,
                    "Timeout detectado",
                    UiTheme.LOST,
                    null,
                    message,
                    true,
                    false
            ));
        }
        if (lower.contains("buffer receptor lleno")) {
            return Optional.of(new SequenceDiagramEventViewModel(
                    "log-buffer-" + message.hashCode(),
                    Endpoint.SERVER,
                    Endpoint.CLIENT,
                    "Ventana cero / buffer lleno",
                    "#fbbf24",
                    null,
                    message,
                    false,
                    false
            ));
        }
        if (protocolType == ProtocolType.TCP && lower.contains("fast retransmit")) {
            return Optional.of(new SequenceDiagramEventViewModel(
                    "log-fast-" + message.hashCode(),
                    Endpoint.CLIENT,
                    Endpoint.SERVER,
                    "Fast retransmit",
                    UiTheme.RETRY,
                    null,
                    message,
                    false,
                    true
            ));
        }
        return Optional.empty();
    }

    private static String buildPacketLabel(Packet packet) {
        String payload = packet.getPayload() == null || packet.getPayload().isBlank() ? "" : " | \"" + shorten(packet.getPayload()) + "\"";
        return switch (packet.getKind()) {
            case SYN -> "SYN" + seq(packet);
            case SYN_ACK -> "SYN-ACK" + seq(packet) + ack(packet);
            case ACK -> (packet.isRetransmission() ? "ACK (RETX)" : "ACK") + ack(packet);
            case DATA -> "DATA" + seq(packet) + payload;
            case RETRANSMISSION -> "DATA (RETX)" + seq(packet) + payload;
            case UDP_DATAGRAM -> "UDP #" + packet.getSeq() + payload;
            case FIN -> "FIN" + seq(packet) + ack(packet);
        };
    }

    private static String buildDetails(Packet packet) {
        String payload = packet.getPayload() == null || packet.getPayload().isBlank() ? "-" : packet.getPayload();
        return "Protocolo: " + packet.getProtocolType() + "\n"
                + "Tipo: " + packet.getKind() + "\n"
                + "Origen: " + packet.getFrom() + "\n"
                + "Destino: " + packet.getTo() + "\n"
                + "SEQ: " + packet.getSeq() + "\n"
                + "ACK: " + packet.getAck() + "\n"
                + "Payload: " + payload + "\n"
                + "Retransmisión: " + (packet.isRetransmission() ? "sí" : "no");
    }

    private static String colorFor(Packet packet) {
        return switch (packet.getKind()) {
            case SYN -> UiTheme.TCP_SYN;
            case SYN_ACK -> UiTheme.TCP_SYN_ACK;
            case ACK -> UiTheme.TCP_ACK;
            case DATA -> UiTheme.TCP_DATA;
            case UDP_DATAGRAM -> UiTheme.UDP;
            case RETRANSMISSION -> UiTheme.RETRY;
            case FIN -> UiTheme.TEAL;
        };
    }

    private static String seq(Packet packet) {
        return packet.getSeq() > 0 ? " | SEQ=" + packet.getSeq() : "";
    }

    private static String ack(Packet packet) {
        return packet.getAck() > 0 ? " | ACK=" + packet.getAck() : "";
    }

    private static String shorten(String payload) {
        return payload.length() <= 10 ? payload : payload.substring(0, 10) + "...";
    }
}
