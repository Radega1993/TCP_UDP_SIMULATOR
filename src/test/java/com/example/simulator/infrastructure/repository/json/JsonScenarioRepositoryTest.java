package com.example.simulator.infrastructure.repository.json;

import com.example.simulator.domain.scenario.Scenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonScenarioRepositoryTest {
    @Test
    void loadsExtendedNetworkConditionsFromJson() {
        JsonScenarioRepository repository = new JsonScenarioRepository();

        Scenario tcpScenario = repository.findById("tcp-handshake").orElseThrow();
        Scenario udpScenario = repository.findById("udp-basic").orElseThrow();
        Scenario congestionScenario = repository.findById("tcp-congestion-duplicate-ack").orElseThrow();

        assertEquals(1200L, tcpScenario.getNetworkConditions().getBaseLatencyMillis());
        assertEquals(0L, tcpScenario.getNetworkConditions().getJitterMillis());
        assertEquals(900L, udpScenario.getNetworkConditions().getBaseLatencyMillis());
        assertEquals(120L, udpScenario.getNetworkConditions().getJitterMillis());
        assertTrue(udpScenario.getNetworkConditions().getPacketLossRate() > 0.0);
        assertEquals(16, congestionScenario.getTcpWindowSizeBytes());
        assertEquals(16, congestionScenario.getTcpReceiverBufferBytes());
    }
}
