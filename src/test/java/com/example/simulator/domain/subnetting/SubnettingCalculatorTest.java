package com.example.simulator.domain.subnetting;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubnettingCalculatorTest {
    @Test
    void splitsSlash24IntoFourSubnets() {
        SubnettingResult result = SubnettingCalculator.calculate("192.168.1.0", 24, SubnettingMode.BY_SUBNETS, 4);

        assertEquals(26, result.newCidr());
        assertEquals("255.255.255.192", result.newMask());
        assertEquals(4, result.generatedSubnets());
        assertEquals(62, result.hostsPerSubnet());
        assertEquals(64, result.increment());
        assertEquals("192.168.1.64", result.visibleSubnets().get(1).network());
        assertEquals("192.168.1.65", result.visibleSubnets().get(1).firstHost());
        assertEquals("192.168.1.126", result.visibleSubnets().get(1).lastHost());
        assertEquals("192.168.1.127", result.visibleSubnets().get(1).broadcast());
    }

    @Test
    void choosesMaskByHostsPerSubnet() {
        SubnettingResult result = SubnettingCalculator.calculate("192.168.1.0", 24, SubnettingMode.BY_HOSTS, 50);

        assertEquals(26, result.newCidr());
        assertTrue(result.hostsPerSubnet() >= 50);
    }

    @Test
    void assignsVlsmNetworksFromLargestToSmallest() {
        VlsmResult result = VlsmCalculator.calculate("192.168.1.0", 24, List.of(
                new VlsmNetworkRequest("Red C", 10),
                new VlsmNetworkRequest("Red A", 100),
                new VlsmNetworkRequest("Red B", 50)
        ));

        assertEquals(3, result.assignments().size());
        assertEquals("Red A", result.assignments().get(0).name());
        assertEquals(25, result.assignments().get(0).cidr());
        assertEquals("192.168.1.0", result.assignments().get(0).network());
        assertEquals("192.168.1.126", result.assignments().get(0).lastHost());

        assertEquals("Red B", result.assignments().get(1).name());
        assertEquals(26, result.assignments().get(1).cidr());
        assertEquals("192.168.1.128", result.assignments().get(1).network());

        assertEquals("Red C", result.assignments().get(2).name());
        assertEquals(28, result.assignments().get(2).cidr());
        assertEquals("192.168.1.192", result.assignments().get(2).network());
        assertEquals(48, result.freeAddresses());
    }

    @Test
    void buildsRoutingBetweenVlsmSubnetsWithTtl() {
        VlsmResult vlsm = VlsmCalculator.calculate("192.168.1.0", 24, List.of(
                new VlsmNetworkRequest("Aula", 100),
                new VlsmNetworkRequest("Servidores", 50)
        ));

        SubnetRoutingResult routing = SubnetRoutingSimulator.simulate(vlsm, 4);

        assertEquals("192.168.1.0/25", routing.sourceSubnet());
        assertEquals("192.168.1.128/26", routing.destinationSubnet());
        assertEquals(4, routing.hops().size());
        assertEquals("192.168.1.128/26", routing.routes().get(2).destinationCidr());
        assertTrue(routing.routes().get(2).selected());
    }

    @Test
    void marksSubnetRoutingAsExpiredWhenTtlRunsOut() {
        VlsmResult vlsm = VlsmCalculator.calculate("192.168.1.0", 24, List.of(
                new VlsmNetworkRequest("Aula", 100),
                new VlsmNetworkRequest("Servidores", 50)
        ));

        SubnetRoutingResult routing = SubnetRoutingSimulator.simulate(vlsm, 1);

        assertTrue(routing.ttlExpired());
        assertEquals("Cliente Aula", routing.expiredAt());
        assertEquals(1, routing.hops().size());
    }
}
