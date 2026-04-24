package com.example.simulator.domain.protocol.tcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpReceiverBufferTest {
    @Test
    void shouldConfigureInitialBufferCapacity() {
        TcpReceiverBuffer buffer = new TcpReceiverBuffer(24);

        assertEquals(24, buffer.capacityBytes());
        assertEquals(0, buffer.usedBytes());
        assertEquals(24, buffer.availableBytes());
    }

    @Test
    void shouldDecreaseAvailableSpaceWhenDataIsReserved() {
        TcpReceiverBuffer buffer = new TcpReceiverBuffer(10);

        buffer.reserve(4);

        assertEquals(4, buffer.usedBytes());
        assertEquals(6, buffer.availableBytes());
        assertTrue(buffer.canAccept(6));
        assertFalse(buffer.canAccept(7));
    }

    @Test
    void shouldIncreaseAdvertisedSpaceWhenBufferIsReleased() {
        TcpReceiverBuffer buffer = new TcpReceiverBuffer(10);
        buffer.reserve(8);

        buffer.release(5);

        assertEquals(3, buffer.usedBytes());
        assertEquals(7, buffer.availableBytes());
    }

    @Test
    void shouldNotOverflowCapacityWhenReserveIsLargerThanAvailableSpace() {
        TcpReceiverBuffer buffer = new TcpReceiverBuffer(5);

        buffer.reserve(99);

        assertEquals(5, buffer.usedBytes());
        assertEquals(0, buffer.availableBytes());
    }

    @Test
    void shouldNormalizeInvalidCapacityToOneByte() {
        TcpReceiverBuffer buffer = new TcpReceiverBuffer(0);

        assertEquals(1, buffer.capacityBytes());
    }
}
