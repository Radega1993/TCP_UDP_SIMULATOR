package com.example.simulator.domain.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationLog {
    private final List<String> entries = new ArrayList<>();

    public void append(String message) {
        entries.add(message);
    }

    public List<String> entries() {
        return Collections.unmodifiableList(entries);
    }
}
