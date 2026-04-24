package com.example.simulator.domain.protocol.tcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpSlidingWindowTest {
    @Test
    void shouldAllowThreeEightByteSegmentsWithTwentyFourByteWindow() {
        TcpSlidingWindow window = new TcpSlidingWindow(24);

        assertTrue(window.canSend(8, 24));
        window.markSent(8);
        assertTrue(window.canSend(8, 24));
        window.markSent(8);
        assertTrue(window.canSend(8, 24));
        window.markSent(8);
        assertFalse(window.canSend(8, 24));
        assertEquals(24, window.bytesInFlight());
    }

    @Test
    void shouldAllowOnlyOneEightByteSegmentWithEightByteWindow() {
        TcpSlidingWindow window = new TcpSlidingWindow(8);

        assertTrue(window.canSend(8, 8));
        window.markSent(8);

        assertFalse(window.canSend(8, 8));
        assertEquals(8, window.bytesInFlight());
    }

    @Test
    void shouldReduceBytesInFlightWhenAckAdvancesWindow() {
        TcpSlidingWindow window = new TcpSlidingWindow(24);
        window.markSent(8);
        window.markSent(8);

        window.markAcknowledgedUpTo(8);

        assertEquals(16, window.bytesSent());
        assertEquals(8, window.bytesAcknowledged());
        assertEquals(8, window.bytesInFlight());
        assertTrue(window.canSend(8, 24));
    }

    @Test
    void shouldRespectReceiverAdvertisedWindowAsEffectiveLimit() {
        TcpSlidingWindow window = new TcpSlidingWindow(24);
        window.markSent(8);

        assertFalse(window.canSend(8, 8));
        assertTrue(window.canSend(8, 16));
    }

    @Test
    void shouldNormalizeInvalidConfiguredWindowToAtLeastOneByte() {
        TcpSlidingWindow window = new TcpSlidingWindow(0);

        assertEquals(1, window.configuredWindowBytes());
    }
}
