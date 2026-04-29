package com.example.simulator.domain.ipv4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Ipv4SubnetCalculatorTest {
    @Test
    void detectsDifferentSlash24Networks() {
        Ipv4SubnetResult result = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.2.20", 24);

        assertEquals("192.168.1.0", result.sourceNetwork());
        assertEquals("192.168.2.0", result.destinationNetwork());
        assertEquals("192.168.1.255", result.sourceBroadcast());
        assertEquals("192.168.1.1 - 192.168.1.254", result.sourceHostRange());
        assertFalse(result.sameNetwork());
        assertTrue(result.needsRouter());
    }

    @Test
    void detectsSameSlash24Network() {
        Ipv4SubnetResult result = Ipv4SubnetCalculator.calculate("10.0.0.10", "10.0.0.200", 24);

        assertEquals("10.0.0.0", result.sourceNetwork());
        assertEquals("10.0.0.0", result.destinationNetwork());
        assertTrue(result.sameNetwork());
        assertFalse(result.needsRouter());
    }

    @Test
    void rejectsInvalidIpv4() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Ipv4SubnetCalculator.calculate("192.168.1.300", "192.168.1.20", 24)
        );

        assertTrue(error.getMessage().contains("octeto"));
    }
}
