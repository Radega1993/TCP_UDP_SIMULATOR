package com.example.simulator.domain.ipv4;

import java.util.List;

public final class Ipv4HeaderInspector {
    private static final int IPV4_HEADER_BYTES = 20;
    private static final int EXAMPLE_PAYLOAD_BYTES = 32;

    private Ipv4HeaderInspector() {
    }

    public static Ipv4HeaderSnapshot inspect(Ipv4SubnetResult subnet, int ttl, IpTransportProtocol protocol) {
        if (ttl < 1 || ttl > 255) {
            throw new IllegalArgumentException("El TTL debe estar entre 1 y 255.");
        }
        int totalLength = IPV4_HEADER_BYTES + protocol.headerBytes() + EXAMPLE_PAYLOAD_BYTES;
        String flags = protocol == IpTransportProtocol.TCP ? "DF, no fragmentar" : "DF";
        return new Ipv4HeaderSnapshot(
                4,
                subnet.sourceIp(),
                subnet.destinationIp(),
                ttl,
                protocol,
                totalLength,
                flags,
                List.of(
                        new Ipv4HeaderField("Version", "4", 4, "Indica que el paquete usa IPv4."),
                        new Ipv4HeaderField("IP origen", subnet.sourceIp(), 32, "Dirección lógica del emisor."),
                        new Ipv4HeaderField("IP destino", subnet.destinationIp(), 32, "Dirección lógica del receptor."),
                        new Ipv4HeaderField("TTL", String.valueOf(ttl), 8, "Saltos restantes antes de descartar el paquete."),
                        new Ipv4HeaderField("Protocolo", protocol.protocolNumber() + " (" + protocol.displayName() + ")", 8, "Dice qué protocolo va dentro de IP."),
                        new Ipv4HeaderField("Longitud total", totalLength + " bytes", 16, "Tamaño del paquete IP completo."),
                        new Ipv4HeaderField("Flags", flags, 3, "Controlan fragmentación y reensamblado.")
                )
        );
    }
}
