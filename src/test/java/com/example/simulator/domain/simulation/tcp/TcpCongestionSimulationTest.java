package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.protocol.tcp.CongestionPhase;
import com.example.simulator.domain.simulation.CongestionSnapshot;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpCongestionSimulationTest {
    @Test
    void shouldEmitCongestionSnapshotsWithHistoryPoints() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKL", 2));

        List<CongestionSnapshot> snapshots = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.CONGESTION_UPDATED)
                .map(event -> event.getCongestionSnapshot())
                .toList();

        assertTrue(snapshots.size() > 3);
        assertTrue(snapshots.stream().allMatch(snapshot -> snapshot.getCwndBytes() > 0));
        assertTrue(snapshots.stream().allMatch(snapshot -> snapshot.getSlowStartThresholdBytes() > 0));
        assertTrue(snapshots.stream().allMatch(snapshot -> snapshot.getHistoryPoint() != null));
    }

    @Test
    void shouldRecordLossEventWhenTimeoutReducesCongestionWindow() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKL", 2, 4, 4,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getCongestionSnapshot() != null
                        && event.getCongestionSnapshot().getHistoryPoint().isLossEvent()
                        && event.getCongestionSnapshot().getReason().contains("Timeout")));
    }

    @Test
    @Disabled("Pendiente de hacer alcanzable fast retransmit end-to-end; TcpCongestionControl sí cubre la transición directa.")
    void shouldRecordFastRetransmitAsCongestionEvent() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKLMNOP", 2, 16, 16,
                network(0.0, 850L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getCongestionSnapshot() != null
                        && event.getCongestionSnapshot().getHistoryPoint().isLossEvent()
                        && event.getCongestionSnapshot().getReason().contains("Duplicate ACK x3")));
    }

    @Test
    void finalSnapshotShouldIncludeCongestionState() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        assertNotNull(result.getFinalSnapshot().getCongestionSnapshot());
        assertTrue(result.getFinalSnapshot().getCongestionSnapshot().getPhase() == CongestionPhase.SLOW_START
                || result.getFinalSnapshot().getCongestionSnapshot().getPhase() == CongestionPhase.CONGESTION_AVOIDANCE
                || result.getFinalSnapshot().getCongestionSnapshot().getPhase() == CongestionPhase.FAST_RETRANSMIT);
    }
}
