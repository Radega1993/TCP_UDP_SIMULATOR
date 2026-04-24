package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.simulation.FlowControlSnapshot;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import com.example.simulator.domain.simulation.SimulationSnapshot;
import org.junit.jupiter.api.Test;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpSnapshotTest {
    @Test
    void finalSnapshotShouldContainTcpStateMessagesFlowAndCongestion() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        SimulationSnapshot snapshot = result.getFinalSnapshot();

        assertEquals(ProtocolType.TCP, snapshot.getProtocol());
        assertEquals(TcpState.CLOSED, snapshot.getClientState());
        assertEquals(TcpState.CLOSED, snapshot.getServerState());
        assertEquals("ABCDEFGH", snapshot.getSentMessage());
        assertEquals("ABCDEFGH", snapshot.getDeliveredMessage());
        assertTrue(snapshot.isCompleted());
        assertNotNull(snapshot.getFlowControlSnapshot());
        assertNotNull(snapshot.getCongestionSnapshot());
    }

    @Test
    void flowSnapshotsShouldBeConsistentWithInternalCounters() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        assertTrue(result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.FLOW_CONTROL_UPDATED)
                .map(event -> event.getFlowControlSnapshot())
                .allMatch(snapshot -> snapshot.getBytesInFlight()
                        == Math.max(0, snapshot.getBytesSent() - snapshot.getBytesAcknowledged())));
    }

    @Test
    void finalFlowSnapshotShouldShowNoPendingBytesAfterSuccessfulDelivery() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        FlowControlSnapshot flow = result.getFinalSnapshot().getFlowControlSnapshot();

        assertEquals(8, flow.getBytesSent());
        assertEquals(8, flow.getBytesAcknowledged());
        assertEquals(0, flow.getBytesInFlight());
        assertEquals(0, flow.getBytesPending());
    }

    @Test
    void congestionSnapshotsShouldHaveIncreasingHistorySteps() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKL", 2));

        int[] previous = {0};
        assertTrue(result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.CONGESTION_UPDATED)
                .map(event -> event.getCongestionSnapshot().getHistoryPoint())
                .allMatch(point -> {
                    boolean increasing = point.getStep() > previous[0];
                    previous[0] = point.getStep();
                    return increasing;
                }));
    }
}
