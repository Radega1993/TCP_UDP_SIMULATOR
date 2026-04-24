package com.example.simulator.ui;

public enum LayersMode {
    COMPARISON("Comparación de modelos"),
    OSI("Modelo OSI"),
    TCP_IP("Modelo TCP/IP"),
    ENCAPSULATION("Encapsulación"),
    PACKET_STRUCTURE("Estructura del paquete");

    private final String label;

    LayersMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
