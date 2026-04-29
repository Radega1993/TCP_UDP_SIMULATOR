package com.example.simulator.domain.ipv4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Ipv4FragmentationSimulatorTest {
    @Test
    void fragmentsPacketUsingMtuAndEightByteOffsets() {
        Ipv4FragmentationResult result = Ipv4FragmentationSimulator.fragment(4000, 1500);

        assertTrue(result.fragmented());
        assertEquals(3, result.fragments().size());
        assertEquals(0, result.fragments().get(0).offsetUnits());
        assertEquals(185, result.fragments().get(1).offsetUnits());
        assertEquals(370, result.fragments().get(2).offsetUnits());
        assertTrue(result.fragments().get(0).moreFragments());
        assertTrue(result.fragments().get(1).moreFragments());
        assertFalse(result.fragments().get(2).moreFragments());
    }

    @Test
    void doesNotFragmentWhenPacketFitsMtu() {
        Ipv4FragmentationResult result = Ipv4FragmentationSimulator.fragment(1200, 1500);

        assertFalse(result.fragmented());
        assertEquals(1, result.fragments().size());
        assertEquals(1200, result.fragments().get(0).totalSizeBytes());
    }

    @Test
    void rejectsMtuSmallerThanHeader() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Ipv4FragmentationSimulator.fragment(4000, 20)
        );

        assertTrue(error.getMessage().contains("MTU"));
    }
}
