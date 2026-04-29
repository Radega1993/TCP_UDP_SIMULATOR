package com.example.simulator.domain.ipv4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IcmpPingSimulatorTest {
    @Test
    void returnsEchoReplyForReachableRemoteNetwork() {
        Ipv4SubnetResult subnet = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.2.20", 24);

        IcmpPingResult result = IcmpPingSimulator.simulate(subnet, "192.168.1.1", 64);

        assertTrue(result.success());
        assertEquals(IcmpMessageType.ECHO_REPLY, result.finalType());
        assertTrue(result.consoleLine().contains("Reply from 192.168.2.20"));
        assertTrue(result.events().stream().anyMatch(event -> event.type() == IcmpMessageType.ECHO_REQUEST));
        assertTrue(result.events().stream().anyMatch(event -> event.type() == IcmpMessageType.ECHO_REPLY));
    }

    @Test
    void returnsDestinationUnreachableWhenGatewayIsMissing() {
        Ipv4SubnetResult subnet = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.2.20", 24);

        IcmpPingResult result = IcmpPingSimulator.simulate(subnet, "", 64);

        assertFalse(result.success());
        assertEquals(IcmpMessageType.DESTINATION_UNREACHABLE, result.finalType());
        assertTrue(result.summary().contains("gateway"));
    }

    @Test
    void returnsTtlExceededWhenTtlExpiresAtRouter() {
        Ipv4SubnetResult subnet = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.2.20", 24);

        IcmpPingResult result = IcmpPingSimulator.simulate(subnet, "192.168.1.1", 1);

        assertFalse(result.success());
        assertEquals(IcmpMessageType.TTL_EXCEEDED, result.finalType());
        assertTrue(result.consoleLine().contains("TTL expired"));
    }
}
