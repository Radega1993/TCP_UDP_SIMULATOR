package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.simulation.FlowControlSnapshot;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpReceiverBufferSimulationTest {
    @Test
    void shouldPublishReceiverBufferCapacityAndAdvertisedWindow() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 6, NetworkConditions.none()));

        assertTrue(result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.FLOW_CONTROL_UPDATED)
                .map(event -> event.getFlowControlSnapshot())
                .allMatch(snapshot -> snapshot.getReceiverBufferCapacity() == 6));
        assertTrue(result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.FLOW_CONTROL_UPDATED)
                .map(event -> event.getFlowControlSnapshot())
                .allMatch(snapshot -> snapshot.getReceiverAdvertisedWindow() <= 6));
    }

    @Test
    void shouldNotDeliverSegmentWhenBufferIsSmallerThanFragment() {
        SimulationResult result = run(tcpScenario("ABCD", 4, 8, 2, NetworkConditions.none()));

        FlowControlSnapshot finalFlow = result.getFinalSnapshot().getFlowControlSnapshot();

        assertEquals("", result.getFinalSnapshot().getDeliveredMessage());
        assertEquals(0, finalFlow.getBytesAcknowledged());
        assertEquals(0, finalFlow.getBytesSent());
        assertTrue(TcpSimulationTestSupport.dataPackets(result).isEmpty());
    }

    @Test
    void shouldReleaseReceiverBufferForInOrderData() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 4, 4, NetworkConditions.none()));

        FlowControlSnapshot finalFlow = result.getFinalSnapshot().getFlowControlSnapshot();

        assertEquals(0, finalFlow.getReceiverBufferUsed());
        assertEquals(4, finalFlow.getReceiverAdvertisedWindow());
        assertEquals("ABCD", result.getFinalSnapshot().getDeliveredMessage());
    }
}
