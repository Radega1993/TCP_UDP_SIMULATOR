package com.example.simulator.domain.ipv4;

import java.util.List;

public record Ipv4FragmentationResult(
        int originalPacketSizeBytes,
        int mtuBytes,
        int headerSizeBytes,
        int payloadSizeBytes,
        int maxDataPerFragmentBytes,
        boolean fragmented,
        List<Ipv4Fragment> fragments
) {
}
