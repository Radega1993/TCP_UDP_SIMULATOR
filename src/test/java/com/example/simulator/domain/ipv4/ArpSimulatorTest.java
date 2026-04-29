package com.example.simulator.domain.ipv4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArpSimulatorTest {
    @Test
    void resolvesGatewayWhenDestinationIsOutsideLocalNetwork() {
        Ipv4SubnetResult subnet = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.2.20", 24);
        ArpSimulationResult result = ArpSimulator.simulate(subnet, "192.168.1.1");

        assertTrue(result.resolvingGateway());
        assertEquals("192.168.1.1", result.targetIp());
        assertEquals(ArpSimulator.GATEWAY_MAC, result.targetMac());
        assertTrue(result.events().get(0).broadcast());
        assertEquals(1, result.cacheEntries().size());
    }

    @Test
    void resolvesDestinationDirectlyWhenSameNetwork() {
        Ipv4SubnetResult subnet = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.1.20", 24);
        ArpSimulationResult result = ArpSimulator.simulate(subnet, "192.168.1.1");

        assertFalse(result.resolvingGateway());
        assertEquals("192.168.1.20", result.targetIp());
        assertEquals(ArpSimulator.SERVER_MAC, result.targetMac());
    }
}
