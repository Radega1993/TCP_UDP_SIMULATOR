package com.example.simulator.domain.ipv4;

public record Ipv4RouterTable(
        String id,
        String name,
        String role,
        Ipv4RouteSelection selection
) {
}
