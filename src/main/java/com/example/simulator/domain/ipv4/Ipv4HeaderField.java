package com.example.simulator.domain.ipv4;

public record Ipv4HeaderField(
        String name,
        String value,
        int bits,
        String help
) {
}
