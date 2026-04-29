package com.example.simulator.domain.subnetting;

import org.junit.jupiter.api.Test;

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
}
