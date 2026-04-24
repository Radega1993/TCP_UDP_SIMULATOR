package com.example.simulator.domain.protocol.tcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpCongestionControlTest {
    @Test
    void shouldStartInSlowStartWithInitialCwndEqualToMss() {
        TcpCongestionControl congestion = new TcpCongestionControl(8, 32);

        assertEquals(8, congestion.currentWindowBytes());
        assertEquals(CongestionPhase.SLOW_START, congestion.phase());
        assertTrue(congestion.slowStartThresholdBytes() >= 8);
    }

    @Test
    void shouldGrowCwndInSlowStartOnAcknowledgement() {
        TcpCongestionControl congestion = new TcpCongestionControl(4, 128);

        congestion.onAcknowledgement(4);

        assertEquals(8, congestion.currentWindowBytes());
        assertEquals(CongestionPhase.SLOW_START, congestion.phase());
    }

    @Test
    void shouldEnterCongestionAvoidanceWhenCwndReachesSsthresh() {
        TcpCongestionControl congestion = new TcpCongestionControl(4, 16);

        congestion.onAcknowledgement(4);
        congestion.onAcknowledgement(4);
        congestion.onAcknowledgement(4);

        assertEquals(CongestionPhase.CONGESTION_AVOIDANCE, congestion.phase());
    }

    @Test
    void shouldGrowMoreSlowlyInCongestionAvoidanceThanSlowStart() {
        TcpCongestionControl congestion = new TcpCongestionControl(4, 16);
        congestion.onAcknowledgement(4);
        congestion.onAcknowledgement(4);
        congestion.onAcknowledgement(4);
        int afterSlowStart = congestion.currentWindowBytes();

        congestion.onAcknowledgement(4);

        assertTrue(congestion.currentWindowBytes() - afterSlowStart < 4);
    }

    @Test
    void shouldReduceSsthreshAndReturnToSlowStartOnTimeout() {
        TcpCongestionControl congestion = new TcpCongestionControl(4, 64);
        congestion.onAcknowledgement(20);
        int beforeTimeout = congestion.currentWindowBytes();

        congestion.onTimeout();

        assertEquals(4, congestion.currentWindowBytes());
        assertEquals(CongestionPhase.SLOW_START, congestion.phase());
        assertEquals(Math.max(4, beforeTimeout / 2), congestion.slowStartThresholdBytes());
    }

    @Test
    void shouldUseFastRetransmitAdjustmentDistinctFromTimeout() {
        TcpCongestionControl congestion = new TcpCongestionControl(4, 64);
        congestion.onAcknowledgement(20);

        congestion.onFastRetransmit();

        assertEquals(CongestionPhase.FAST_RETRANSMIT, congestion.phase());
        assertTrue(congestion.currentWindowBytes() >= 4);
        assertEquals(0, congestion.duplicateAckCount());
    }

    @Test
    void shouldLimitEffectiveWindowByReceiverWindowAndCwnd() {
        TcpCongestionControl congestion = new TcpCongestionControl(8, 64);

        assertEquals(4, congestion.effectiveWindowBytes(4));
        assertEquals(8, congestion.effectiveWindowBytes(64));
    }
}
