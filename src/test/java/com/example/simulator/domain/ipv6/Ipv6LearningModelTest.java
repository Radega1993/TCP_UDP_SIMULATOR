package com.example.simulator.domain.ipv6;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Ipv6LearningModelTest {
    @Test
    void splitsIpv6AddressIntoEightHextets() {
        assertEquals(8, Ipv6LearningModel.hextets("2001:0db8:0001:0000:0000:0000:0000:0010").size());
    }

    @Test
    void exposesUnicastAndMulticastExamples() {
        assertEquals("Unicast", Ipv6LearningModel.examples().get(0).type());
        assertEquals("Multicast", Ipv6LearningModel.examples().get(1).type());
    }
}
