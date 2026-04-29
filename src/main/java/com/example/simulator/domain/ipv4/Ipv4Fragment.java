package com.example.simulator.domain.ipv4;

public record Ipv4Fragment(
        int number,
        int offsetUnits,
        int offsetBytes,
        int dataSizeBytes,
        int totalSizeBytes,
        boolean moreFragments
) {
}
