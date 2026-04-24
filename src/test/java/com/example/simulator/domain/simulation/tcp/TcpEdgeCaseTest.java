package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.createdPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpEdgeCaseTest {
    @Test
    void shouldHandleEmptyMessageWithoutCrashing() {
        SimulationResult result = run(tcpScenario("", 4));

        assertTrue(createdPackets(result, PacketKind.DATA).isEmpty());
        assertEquals("", result.getFinalSnapshot().getDeliveredMessage());
        assertTrue(result.getFinalSnapshot().isCompleted());
    }

    @Test
    void shouldHandleOneCharacterMessage() {
        SimulationResult result = run(tcpScenario("A", 4));

        assertEquals(1, createdPackets(result, PacketKind.DATA).size());
        assertEquals("A", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldHandleFragmentLargerThanMessage() {
        SimulationResult result = run(tcpScenario("ABC", 16));

        assertEquals(1, createdPackets(result, PacketKind.DATA).size());
        assertEquals("ABC", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldNotCrashWhenWindowIsSmallerThanFragment() {
        SimulationResult result = run(tcpScenario("ABCD", 4, 2, 8, NetworkConditions.none()));

        assertEquals("", result.getFinalSnapshot().getDeliveredMessage());
        assertTrue(result.getFinalSnapshot().isCompleted());
    }

    @Test
    void shouldNotCrashWhenReceiverBufferIsSmallerThanFragment() {
        SimulationResult result = run(tcpScenario("ABCD", 4, 8, 2, NetworkConditions.none()));

        assertEquals("", result.getFinalSnapshot().getDeliveredMessage());
        assertTrue(result.getFinalSnapshot().isCompleted());
    }

    @Test
    void shouldRecoverWhenHundredPercentLossIsLimitedToOriginalDataAttempts() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 4, 4,
                network(1.0, 900L, 0L, 0.0, 0.0, 0, Set.of())));

        assertTrue(createdPackets(result, PacketKind.RETRANSMISSION).size() >= 2);
        assertEquals("ABCD", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldHandleHighJitterDuplicationAndReorderingTogether() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8,
                network(0.0, 500L, 800L, 1.0, 1.0, 2, Set.of())));

        assertEquals("ABCDEFGH", result.getFinalSnapshot().getDeliveredMessage());
        assertTrue(result.getFinalSnapshot().isCompleted());
    }
}
