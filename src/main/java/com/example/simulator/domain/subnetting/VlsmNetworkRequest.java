package com.example.simulator.domain.subnetting;

public record VlsmNetworkRequest(
        String name,
        int requiredHosts
) {
    public VlsmNetworkRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cada red necesita un nombre.");
        }
        if (requiredHosts < 1) {
            throw new IllegalArgumentException("Los hosts requeridos deben ser mayores que 0.");
        }
        name = name.trim();
    }
}
