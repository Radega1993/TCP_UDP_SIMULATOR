package com.example.simulator.domain.ipv4;

import java.util.List;

public record IcmpPingResult(
        IcmpMessageType finalType,
        String consoleLine,
        String summary,
        boolean success,
        List<IcmpPingEvent> events
) {
}
