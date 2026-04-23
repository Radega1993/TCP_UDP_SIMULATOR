package com.example.simulator.presentation.viewmodel;

public enum SimulationMode {
    SIMPLE("Simulación simple"),
    COMPARE("Comparar TCP vs UDP");

    private final String label;

    SimulationMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
