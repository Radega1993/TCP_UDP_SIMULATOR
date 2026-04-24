package com.example.simulator.ui;

public enum LearningLevel {
    BASIC("Básico"),
    ADVANCED("Avanzado");

    private final String label;

    LearningLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
