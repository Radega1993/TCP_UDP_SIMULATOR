package com.example.simulator.presentation.layers;

import java.util.List;

public class LayerExplanation {
    private final NetworkLayerType layerType;
    private final String name;
    private final String function;
    private final List<String> examples;

    public LayerExplanation(NetworkLayerType layerType, String name, String function, List<String> examples) {
        this.layerType = layerType;
        this.name = name;
        this.function = function;
        this.examples = List.copyOf(examples);
    }

    public NetworkLayerType getLayerType() {
        return layerType;
    }

    public String getName() {
        return name;
    }

    public String getFunction() {
        return function;
    }

    public List<String> getExamples() {
        return examples;
    }
}
