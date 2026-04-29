package com.example.simulator.domain.subnetting;

public record SubnetBlock(
        int number,
        String network,
        String firstHost,
        String lastHost,
        String broadcast,
        String hostRange
) {
}
