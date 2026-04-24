package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.simulation.FlowControlSnapshot;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpSlidingWindowSimulationTest {
    @Test
    void shouldNeverReportMoreBytesInFlightThanConfiguredWindow() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKLMNOP", 4, 8, 16,
                com.example.simulator.domain.network.NetworkConditions.none()));

        assertTrue(result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.FLOW_CONTROL_UPDATED)
                .map(event -> event.getFlowControlSnapshot())
                .allMatch(snapshot -> snapshot.getBytesInFlight() <= snapshot.getWindowSizeBytes()));
    }

    @Test
    void shouldAdvanceWindowAfterAcknowledgements() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 4, 8,
                com.example.simulator.domain.network.NetworkConditions.none()));

        FlowControlSnapshot finalFlow = result.getFinalSnapshot().getFlowControlSnapshot();

        assertEquals(8, finalFlow.getBytesSent());
        assertEquals(8, finalFlow.getBytesAcknowledged());
        assertEquals(0, finalFlow.getBytesInFlight());
    }

    @Test
    void shouldWaitWhenWindowIsSmallerThanNextSegment() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 4, 2, 8,
                com.example.simulator.domain.network.NetworkConditions.none()));

        assertEquals("", result.getFinalSnapshot().getDeliveredMessage());
        assertEquals(0, result.getFinalSnapshot().getFlowControlSnapshot().getBytesSent());
    }
}
