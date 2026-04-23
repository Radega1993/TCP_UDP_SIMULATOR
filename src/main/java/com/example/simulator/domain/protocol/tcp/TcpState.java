package com.example.simulator.domain.protocol.tcp;

public enum TcpState {
    CLOSED,
    LISTEN,
    SYN_SENT,
    SYN_RECEIVED,
    ESTABLISHED,
    FIN_WAIT_1,
    CLOSE_WAIT,
    LAST_ACK,
    TIME_WAIT
}
