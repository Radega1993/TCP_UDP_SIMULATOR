package com.example.simulator.domain.ipv4;

import java.util.List;

public record ArpSimulationResult(
        String requesterIp,
        String requesterMac,
        String targetIp,
        String targetMac,
        String targetDevice,
        boolean resolvingGateway,
        String question,
        List<ArpEvent> events,
        List<ArpCacheEntry> cacheEntries
) {
}
