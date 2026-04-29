package com.example.simulator.domain.ipv6;

import java.util.List;

public final class Ipv6LearningModel {
    private Ipv6LearningModel() {
    }

    public static List<String> hextets(String address) {
        String[] parts = address.split(":");
        if (parts.length != 8) {
            throw new IllegalArgumentException("La dirección IPv6 de ejemplo debe tener 8 bloques.");
        }
        return List.of(parts);
    }

    public static List<Ipv6AddressExample> examples() {
        return List.of(
                new Ipv6AddressExample(
                        "Unicast",
                        "2001:0db8:0001:0000:0000:0000:0000:0010",
                        "Identifica una interfaz concreta. El paquete va a un único destino.",
                        hextets("2001:0db8:0001:0000:0000:0000:0000:0010")
                ),
                new Ipv6AddressExample(
                        "Multicast",
                        "ff02:0000:0000:0000:0000:0000:0000:0001",
                        "Identifica un grupo. El paquete llega a varios nodos interesados.",
                        hextets("ff02:0000:0000:0000:0000:0000:0000:0001")
                )
        );
    }
}
