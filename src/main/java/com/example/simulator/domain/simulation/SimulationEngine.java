package com.example.simulator.domain.simulation;

import com.example.simulator.domain.scenario.Scenario;

public interface SimulationEngine {
    SimulationResult run(Scenario scenario);
}
