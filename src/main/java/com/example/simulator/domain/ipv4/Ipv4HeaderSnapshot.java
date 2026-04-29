package com.example.simulator.domain.ipv4;

import java.util.List;

public record Ipv4HeaderSnapshot(
        int version,
        String sourceIp,
        String destinationIp,
        int ttl,
        IpTransportProtocol protocol,
        int totalLengthBytes,
        String flags,
        List<Ipv4HeaderField> fields
) {
}
