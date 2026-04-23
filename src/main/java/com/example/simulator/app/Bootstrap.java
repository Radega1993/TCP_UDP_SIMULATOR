package com.example.simulator.app;

import com.example.simulator.application.services.SimulationApplicationService;
import com.example.simulator.application.services.ProtocolComparisonApplicationService;
import com.example.simulator.application.services.TheoryApplicationService;
import com.example.simulator.domain.simulation.DefaultSimulationEngine;
import com.example.simulator.infrastructure.repository.json.JsonScenarioRepository;
import com.example.simulator.infrastructure.repository.json.JsonTheoryRepository;
import com.example.simulator.presentation.viewmodel.ComparisonViewModel;
import com.example.simulator.presentation.viewmodel.SimulationViewModel;

public class Bootstrap {
    private final SimulationApplicationService simulationService = new SimulationApplicationService(
            new DefaultSimulationEngine(),
            new JsonScenarioRepository()
    );
    private final TheoryApplicationService theoryService = new TheoryApplicationService(new JsonTheoryRepository());

    public SimulationViewModel createSimulationViewModel() {
        return new SimulationViewModel(simulationService, theoryService);
    }

    public ComparisonViewModel createComparisonViewModel() {
        return new ComparisonViewModel(new ProtocolComparisonApplicationService(simulationService));
    }
}
