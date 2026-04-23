package com.example.simulator.domain.protocol;

public enum PacketKind {
    SYN,
    SYN_ACK,
    ACK,
    DATA,
    FIN,
    UDP_DATAGRAM,
    RETRANSMISSION
}
