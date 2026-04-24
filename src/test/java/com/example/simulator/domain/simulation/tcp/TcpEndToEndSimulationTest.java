package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.createdPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpEndToEndSimulationTest {
    @Test
    void shouldDeliverSimpleMessageWithoutLoss() {
        SimulationResult result = run(tcpScenario("HOLA", 8));

        assertEquals("HOLA", result.getFinalSnapshot().getDeliveredMessage());
        assertEquals(TcpState.CLOSED, result.getFinalSnapshot().getClientState());
        assertEquals(TcpState.CLOSED, result.getFinalSnapshot().getServerState());
        assertTrue(result.getFinalSnapshot().isCompleted());
    }

    @Test
    void shouldDeliverFragmentedMessageWithoutLoss() {
        SimulationResult result = run(tcpScenario("HOLAALUMNOS", 8));

        assertEquals(2, createdPackets(result, PacketKind.DATA).size());
        assertEquals("HOLAALUMNOS", result.getFinalSnapshot().getDeliveredMessage());
        assertTrue(TcpSimulationTestSupport.serverAckPackets(result).stream().anyMatch(packet -> packet.getAck() == 12));
    }

    @Test
    void shouldDeliverMessageAfterDataLossAndRetransmission() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertFalse(createdPackets(result, PacketKind.RETRANSMISSION).isEmpty());
        assertEquals("ABCDEFGH", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldDeliverMessageWithReordering() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKL", 2, 12, 12,
                network(0.0, 1200L, 0L, 0.0, 1.0, 0, Set.of())));

        assertEquals("ABCDEFGHIJKL", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldRequireMultipleAckRoundsWithSmallWindow() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 2, 8, NetworkConditions.none()));

        assertEquals("ABCDEFGH", result.getFinalSnapshot().getDeliveredMessage());
        assertTrue(TcpSimulationTestSupport.serverAckPackets(result).size() >= 4);
    }

    @Test
    void shouldExposeCongestionChangesInLossScenario() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKL", 2, 8, 8,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertEquals("ABCDEFGHIJKL", result.getFinalSnapshot().getDeliveredMessage());
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getCongestionSnapshot() != null
                        && event.getCongestionSnapshot().getHistoryPoint().isLossEvent()));
    }
}
