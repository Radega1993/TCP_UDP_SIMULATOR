package com.example.simulator.domain.ipv4;

public record IcmpPingEvent(
        int step,
        IcmpMessageType type,
        String fromDevice,
        String fromIp,
        String toDevice,
        String toIp,
        int ttlBefore,
        int ttlAfter,
        String description
) {
}
