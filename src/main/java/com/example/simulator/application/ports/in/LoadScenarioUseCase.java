package com.example.simulator.application.ports.in;

import com.example.simulator.domain.scenario.Scenario;

import java.util.List;
import java.util.Optional;

public interface LoadScenarioUseCase {
    List<Scenario> loadScenarios();
    Optional<Scenario> loadScenario(String scenarioId);
}
