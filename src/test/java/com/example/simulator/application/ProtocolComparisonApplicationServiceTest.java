package com.example.simulator.application;

import com.example.simulator.application.dto.ComparisonCommand;
import com.example.simulator.application.services.ProtocolComparisonApplicationService;
import com.example.simulator.application.services.SimulationApplicationService;
import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.infrastructure.repository.json.JsonScenarioRepository;
import com.example.simulator.domain.simulation.DefaultSimulationEngine;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolComparisonApplicationServiceTest {
    @Test
    void runsTcpAndUdpWithSameSharedInput() {
        SimulationApplicationService simulationService = new SimulationApplicationService(
                new DefaultSimulationEngine(),
                new JsonScenarioRepository()
        );
        ProtocolComparisonApplicationService comparisonService = new ProtocolComparisonApplicationService(simulationService);

        ComparisonCommand command = new ComparisonCommand(
                "HOLAALUMNOS",
                4,
                new NetworkConditions(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(1))
        );

        var result = comparisonService.compare(command);

        assertEquals("HOLAALUMNOS", result.getTcpResult().getFinalSnapshot().getSentMessage());
        assertEquals("HOLAALUMNOS", result.getUdpResult().getFinalSnapshot().getSentMessage());
        assertTrue(result.getSummary().isTcpRetransmitted());
        assertFalse(result.getSummary().isUdpRetransmitted());
        assertTrue(result.getSummary().isTcpDeliveredComplete());
        assertFalse(result.getSummary().isUdpDeliveredComplete());
    }
}
