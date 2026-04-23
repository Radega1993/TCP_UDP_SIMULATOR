package com.example.simulator.app;

import com.example.simulator.application.services.SimulationApplicationService;
import com.example.simulator.application.services.TheoryApplicationService;
import com.example.simulator.domain.simulation.DefaultSimulationEngine;
import com.example.simulator.infrastructure.repository.json.JsonScenarioRepository;
import com.example.simulator.infrastructure.repository.json.JsonTheoryRepository;
import com.example.simulator.presentation.viewmodel.SimulationViewModel;

public class Bootstrap {
    public SimulationViewModel createSimulationViewModel() {
        SimulationApplicationService simulationService = new SimulationApplicationService(
                new DefaultSimulationEngine(),
                new JsonScenarioRepository()
        );
        TheoryApplicationService theoryService = new TheoryApplicationService(new JsonTheoryRepository());
        return new SimulationViewModel(simulationService, theoryService);
    }
}
