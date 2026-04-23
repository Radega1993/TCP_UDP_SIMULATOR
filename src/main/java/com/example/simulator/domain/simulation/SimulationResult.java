package com.example.simulator.domain.simulation;

import com.example.simulator.domain.scenario.Scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationResult {
    private final Scenario scenario;
    private final List<SimulationEvent> events;
    private final SimulationSnapshot finalSnapshot;
    private final List<String> logEntries;

    public SimulationResult(Scenario scenario, List<SimulationEvent> events, SimulationSnapshot finalSnapshot, List<String> logEntries) {
        this.scenario = scenario;
        this.events = Collections.unmodifiableList(new ArrayList<>(events));
        this.finalSnapshot = finalSnapshot;
        this.logEntries = Collections.unmodifiableList(new ArrayList<>(logEntries));
    }

    public Scenario getScenario() {
        return scenario;
    }

    public List<SimulationEvent> getEvents() {
        return events;
    }

    public SimulationSnapshot getFinalSnapshot() {
        return finalSnapshot;
    }

    public List<String> getLogEntries() {
        return logEntries;
    }
}
