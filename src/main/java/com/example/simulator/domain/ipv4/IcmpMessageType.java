package com.example.simulator.domain.ipv4;

public enum IcmpMessageType {
    ECHO_REQUEST("Echo Request"),
    ECHO_REPLY("Echo Reply"),
    DESTINATION_UNREACHABLE("Destination unreachable"),
    TTL_EXCEEDED("TTL exceeded");

    private final String displayName;

    IcmpMessageType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
