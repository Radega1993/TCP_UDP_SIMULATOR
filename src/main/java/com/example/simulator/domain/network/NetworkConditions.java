package com.example.simulator.domain.network;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class NetworkConditions {
    private final double packetLossRate;
    private final Set<Integer> forcedLossIndexes;

    public NetworkConditions(double packetLossRate, Set<Integer> forcedLossIndexes) {
        this.packetLossRate = clamp(packetLossRate);
        this.forcedLossIndexes = Collections.unmodifiableSet(new LinkedHashSet<>(forcedLossIndexes));
    }

    public static NetworkConditions none() {
        return new NetworkConditions(0.0, Set.of());
    }

    public double getPacketLossRate() {
        return packetLossRate;
    }

    public Set<Integer> getForcedLossIndexes() {
        return forcedLossIndexes;
    }

    private static double clamp(double packetLossRate) {
        if (packetLossRate < 0.0) {
            return 0.0;
        }
        if (packetLossRate > 1.0) {
            return 1.0;
        }
        return packetLossRate;
    }
}
