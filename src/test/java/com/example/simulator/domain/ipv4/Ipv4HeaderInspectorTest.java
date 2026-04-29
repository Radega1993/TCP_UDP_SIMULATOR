package com.example.simulator.domain.ipv4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Ipv4HeaderInspectorTest {
    @Test
    void buildsTcpIpv4HeaderSnapshot() {
        Ipv4SubnetResult subnet = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.2.20", 24);

        Ipv4HeaderSnapshot header = Ipv4HeaderInspector.inspect(subnet, 64, IpTransportProtocol.TCP);

        assertEquals(4, header.version());
        assertEquals("192.168.1.10", header.sourceIp());
        assertEquals("192.168.2.20", header.destinationIp());
        assertEquals(64, header.ttl());
        assertEquals(IpTransportProtocol.TCP, header.protocol());
        assertEquals(72, header.totalLengthBytes());
        assertEquals(7, header.fields().size());
    }
}
