package com.example.simulator.application.ports.in;

import com.example.simulator.application.dto.SimulationCommand;
import com.example.simulator.domain.simulation.SimulationResult;

public interface StartSimulationUseCase {
    SimulationResult start(SimulationCommand command);
}
