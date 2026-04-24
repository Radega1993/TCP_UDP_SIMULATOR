package com.example.simulator.presentation.layers;

public class HeaderFieldViewModel {
    private final String name;
    private final String value;
    private final String help;

    public HeaderFieldViewModel(String name, String value, String help) {
        this.name = name;
        this.value = value;
        this.help = help;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getHelp() {
        return help;
    }
}
