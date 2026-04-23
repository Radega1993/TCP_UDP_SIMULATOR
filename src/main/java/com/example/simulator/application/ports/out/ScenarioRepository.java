package com.example.simulator.application.ports.out;

import com.example.simulator.domain.scenario.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository {
    Optional<Scenario> findById(String id);
    List<Scenario> findAll();
}
