package com.example.simulator.application.services;

import com.example.simulator.application.dto.SimulationCommand;
import com.example.simulator.application.ports.in.LoadScenarioUseCase;
import com.example.simulator.application.ports.in.StartSimulationUseCase;
import com.example.simulator.application.ports.out.ScenarioRepository;
import com.example.simulator.domain.scenario.Scenario;
import com.example.simulator.domain.simulation.SimulationEngine;
import com.example.simulator.domain.simulation.SimulationResult;

import java.util.List;
import java.util.Optional;

public class SimulationApplicationService implements StartSimulationUseCase, LoadScenarioUseCase {
    private final SimulationEngine simulationEngine;
    private final ScenarioRepository scenarioRepository;

    public SimulationApplicationService(SimulationEngine simulationEngine, ScenarioRepository scenarioRepository) {
        this.simulationEngine = simulationEngine;
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    public SimulationResult start(SimulationCommand command) {
        return simulationEngine.run(command.toAdHocScenario());
    }

    public SimulationResult start(Scenario scenario) {
        return simulationEngine.run(scenario);
    }

    @Override
    public List<Scenario> loadScenarios() {
        return scenarioRepository.findAll();
    }

    @Override
    public Optional<Scenario> loadScenario(String scenarioId) {
        return scenarioRepository.findById(scenarioId);
    }
}
