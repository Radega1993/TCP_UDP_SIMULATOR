package com.example.simulator.presentation.layers;

import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.Packet;

import java.util.List;

public final class EncapsulationMapper {
    private EncapsulationMapper() {
    }

    public static EncapsulationSnapshot fromPacket(Packet packet, ProtocolType fallbackProtocol, String fallbackMessage) {
        ProtocolType protocol = packet != null ? packet.getProtocolType() : fallbackProtocol;
        String payload = packet != null && packet.getPayload() != null && !packet.getPayload().isBlank()
                ? packet.getPayload()
                : fallbackMessage;

        List<HeaderFieldViewModel> transportHeader = protocol == ProtocolType.UDP
                ? buildUdpHeader(packet, payload)
                : buildTcpHeader(packet);

        return new EncapsulationSnapshot(
                payload == null || payload.isBlank() ? "HOLA" : payload,
                protocol == ProtocolType.UDP ? "Header UDP" : "Header TCP",
                transportHeader,
                buildIpHeader(protocol),
                buildLinkHeader(),
                protocol == ProtocolType.UDP ? "Datagrama UDP" : "Segmento TCP",
                "Paquete IP",
                "Trama"
        );
    }

    public static String buildPacketStructureText(Packet packet, ProtocolType fallbackProtocol, String fallbackMessage) {
        EncapsulationSnapshot snapshot = fromPacket(packet, fallbackProtocol, fallbackMessage);
        StringBuilder builder = new StringBuilder();
        builder.append("Payload\n")
                .append(snapshot.getPayload())
                .append("\n\n")
                .append(snapshot.getTransportTitle())
                .append("\n");
        appendFields(builder, snapshot.getTransportHeader());
        builder.append("\nHeader IP\n");
        appendFields(builder, snapshot.getIpHeader());
        builder.append("\nHeader de enlace\n");
        appendFields(builder, snapshot.getLinkHeader());
        builder.append("\nUnidades resultantes\n")
                .append("• Transporte: ").append(snapshot.getTransportUnitName()).append("\n")
                .append("• Red: ").append(snapshot.getNetworkUnitName()).append("\n")
                .append("• Enlace: ").append(snapshot.getLinkUnitName()).append("\n");
        return builder.toString();
    }

    private static void appendFields(StringBuilder builder, List<HeaderFieldViewModel> fields) {
        for (HeaderFieldViewModel field : fields) {
            builder.append("• ").append(field.getName()).append(": ").append(field.getValue()).append("\n");
        }
    }

    private static List<HeaderFieldViewModel> buildTcpHeader(Packet packet) {
        int seq = packet != null ? packet.getSeq() : 101;
        int ack = packet != null ? packet.getAck() : 0;
        String flags = packet == null ? "ACK" : flagsFor(packet.getKind());
        return List.of(
                new HeaderFieldViewModel("Puerto origen", "49152", "Identifica la aplicación emisora."),
                new HeaderFieldViewModel("Puerto destino", "8080", "Identifica la aplicación receptora."),
                new HeaderFieldViewModel("Número de secuencia", String.valueOf(seq), "Posición de los datos dentro del flujo TCP."),
                new HeaderFieldViewModel("Número de ACK", String.valueOf(ack), "Siguiente byte esperado por el receptor."),
                new HeaderFieldViewModel("Flags", flags, "Indican control de conexión y estado del segmento.")
        );
    }

    private static List<HeaderFieldViewModel> buildUdpHeader(Packet packet, String payload) {
        int length = Math.max(8, payload == null ? 8 : payload.length() + 8);
        return List.of(
                new HeaderFieldViewModel("Puerto origen", "53000", "Identifica el proceso emisor."),
                new HeaderFieldViewModel("Puerto destino", "9000", "Identifica el proceso receptor."),
                new HeaderFieldViewModel("Longitud", String.valueOf(length), "Longitud total del datagrama UDP.")
        );
    }

    private static List<HeaderFieldViewModel> buildIpHeader(ProtocolType protocolType) {
        return List.of(
                new HeaderFieldViewModel("IP origen", "192.168.1.10", "Dirección lógica del emisor."),
                new HeaderFieldViewModel("IP destino", "192.168.1.20", "Dirección lógica del receptor."),
                new HeaderFieldViewModel("TTL", "64", "Límite de saltos antes de descartar el paquete."),
                new HeaderFieldViewModel("Protocolo", protocolType == ProtocolType.UDP ? "17 (UDP)" : "6 (TCP)", "Indica qué protocolo de transporte va encapsulado.")
        );
    }

    private static List<HeaderFieldViewModel> buildLinkHeader() {
        return List.of(
                new HeaderFieldViewModel("MAC origen", "00:1A:2B:3C:4D:10", "Dirección física del emisor en la red local."),
                new HeaderFieldViewModel("MAC destino", "00:1A:2B:3C:4D:20", "Dirección física del siguiente salto o receptor local."),
                new HeaderFieldViewModel("Tipo", "0x0800 (IPv4)", "Indica el protocolo encapsulado en la trama.")
        );
    }

    private static String flagsFor(PacketKind kind) {
        return switch (kind) {
            case SYN -> "SYN";
            case SYN_ACK -> "SYN, ACK";
            case ACK -> "ACK";
            case FIN -> "FIN";
            case RETRANSMISSION, DATA -> "PSH, ACK";
            case UDP_DATAGRAM -> "-";
        };
    }
}
