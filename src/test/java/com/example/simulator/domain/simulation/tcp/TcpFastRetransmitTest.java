package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.createdPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpFastRetransmitTest {
    @Test
    @Disabled("El motor actual registra ACK duplicados, pero cwnd/timeout impide alcanzar 3 ACK duplicados antes del timeout; queda como test de especificación para activar fast retransmit end-to-end.")
    void shouldTriggerFastRetransmitAfterThreeDuplicateAcks() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKLMNOP", 2, 16, 16,
                network(0.0, 850L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertTrue(TcpSimulationTestSupport.logContains(result, "Fast retransmit activado"));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getCongestionSnapshot() != null
                        && event.getCongestionSnapshot().getReason().contains("Duplicate ACK x3")));
    }

    @Test
    void shouldRetransmitMissingSegmentWithSameSeqAndPayloadAfterLossRecovery() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKLMNOP", 2, 16, 16,
                network(0.0, 850L, 0L, 0.0, 0.0, 0, Set.of(2))));

        Packet retransmission = createdPackets(result, PacketKind.RETRANSMISSION).get(0);

        assertEquals(3, retransmission.getSeq());
        assertEquals("CD", retransmission.getPayload());
        assertTrue(retransmission.isRetransmission());
        assertEquals("ABCDEFGHIJKLMNOP", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldNotFastRetransmitWithOnlyTwoDuplicateAcks() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8,
                network(0.0, 850L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertTrue(result.getLogEntries().stream().noneMatch(entry -> entry.contains("Fast retransmit activado")));
    }
}
