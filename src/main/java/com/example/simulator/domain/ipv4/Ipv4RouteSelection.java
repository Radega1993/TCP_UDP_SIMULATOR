package com.example.simulator.domain.ipv4;

import java.util.List;

public record Ipv4RouteSelection(
        String destinationIp,
        Ipv4RouteEntry selectedRoute,
        List<Ipv4RouteEntry> routes,
        String explanation
) {
}
