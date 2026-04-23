package com.example.simulator.domain.network;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class NetworkConditions {
    private final double packetLossRate;
    private final long baseLatencyMillis;
    private final long jitterMillis;
    private final double duplicationRate;
    private final double reorderingRate;
    private final int bandwidthPacketsPerSecond;
    private final Set<Integer> forcedLossIndexes;

    public NetworkConditions(double packetLossRate, Set<Integer> forcedLossIndexes) {
        this(packetLossRate, 1200L, 0L, 0.0, 0.0, 0, forcedLossIndexes);
    }

    public NetworkConditions(double packetLossRate, long baseLatencyMillis, long jitterMillis,
                             double duplicationRate, double reorderingRate, int bandwidthPacketsPerSecond,
                             Set<Integer> forcedLossIndexes) {
        this.packetLossRate = clampRate(packetLossRate);
        this.baseLatencyMillis = clampMillis(baseLatencyMillis);
        this.jitterMillis = clampMillis(jitterMillis);
        this.duplicationRate = clampRate(duplicationRate);
        this.reorderingRate = clampRate(reorderingRate);
        this.bandwidthPacketsPerSecond = Math.max(0, bandwidthPacketsPerSecond);
        this.forcedLossIndexes = Collections.unmodifiableSet(new LinkedHashSet<>(forcedLossIndexes));
    }

    public static NetworkConditions none() {
        return new NetworkConditions(0.0, 1200L, 0L, 0.0, 0.0, 0, Set.of());
    }

    public double getPacketLossRate() {
        return packetLossRate;
    }

    public long getBaseLatencyMillis() {
        return baseLatencyMillis;
    }

    public long getJitterMillis() {
        return jitterMillis;
    }

    public double getDuplicationRate() {
        return duplicationRate;
    }

    public double getReorderingRate() {
        return reorderingRate;
    }

    public int getBandwidthPacketsPerSecond() {
        return bandwidthPacketsPerSecond;
    }

    public Set<Integer> getForcedLossIndexes() {
        return forcedLossIndexes;
    }

    private static double clampRate(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    private static long clampMillis(long value) {
        return Math.max(0L, value);
    }
}
