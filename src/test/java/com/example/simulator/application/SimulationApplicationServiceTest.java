package com.example.simulator.application;

import com.example.simulator.application.dto.SimulationCommand;
import com.example.simulator.application.services.SimulationApplicationService;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.DefaultSimulationEngine;
import com.example.simulator.infrastructure.repository.json.JsonScenarioRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationApplicationServiceTest {
    @Test
    void loadsScenariosFromJsonRepository() {
        SimulationApplicationService service = new SimulationApplicationService(
                new DefaultSimulationEngine(),
                new JsonScenarioRepository()
        );

        assertFalse(service.loadScenarios().isEmpty());
        assertTrue(service.loadScenario("tcp-handshake").isPresent());
    }

    @Test
    void startsSimulationFromApplicationLayer() {
        SimulationApplicationService service = new SimulationApplicationService(
                new DefaultSimulationEngine(),
                new JsonScenarioRepository()
        );

        var result = service.start(new SimulationCommand(ProtocolType.UDP, "ABCDEF", 3, 0.0));

        assertTrue(result.getFinalSnapshot().isCompleted());
    }
}
