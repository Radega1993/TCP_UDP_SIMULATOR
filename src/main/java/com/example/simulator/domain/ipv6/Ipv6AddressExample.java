package com.example.simulator.domain.ipv6;

import java.util.List;

public record Ipv6AddressExample(
        String type,
        String address,
        String purpose,
        List<String> hextets
) {
}
