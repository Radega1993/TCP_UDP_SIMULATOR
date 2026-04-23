package com.example.simulator.domain.simulation;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.PacketStatus;
import com.example.simulator.domain.protocol.ProtocolType;

public class Packet {
    private final String id;
    private final ProtocolType protocolType;
    private final Endpoint from;
    private final Endpoint to;
    private final PacketKind kind;
    private final int seq;
    private final int ack;
    private final String payload;
    private PacketStatus status;
    private final boolean retransmission;
    private final int windowSize;

    public Packet(String id, ProtocolType protocolType, Endpoint from, Endpoint to, PacketKind kind,
                  int seq, int ack, String payload, PacketStatus status, boolean retransmission) {
        this(id, protocolType, from, to, kind, seq, ack, payload, status, retransmission, 0);
    }

    public Packet(String id, ProtocolType protocolType, Endpoint from, Endpoint to, PacketKind kind,
                  int seq, int ack, String payload, PacketStatus status, boolean retransmission, int windowSize) {
        this.id = id;
        this.protocolType = protocolType;
        this.from = from;
        this.to = to;
        this.kind = kind;
        this.seq = seq;
        this.ack = ack;
        this.payload = payload;
        this.status = status;
        this.retransmission = retransmission;
        this.windowSize = Math.max(0, windowSize);
    }

    public String getId() {
        return id;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public Endpoint getFrom() {
        return from;
    }

    public Endpoint getTo() {
        return to;
    }

    public PacketKind getKind() {
        return kind;
    }

    public int getSeq() {
        return seq;
    }

    public int getAck() {
        return ack;
    }

    public String getPayload() {
        return payload;
    }

    public PacketStatus getStatus() {
        return status;
    }

    public void setStatus(PacketStatus status) {
        this.status = status;
    }

    public boolean isRetransmission() {
        return retransmission;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public String label() {
        String base = switch (kind) {
            case SYN -> "SYN";
            case SYN_ACK -> "SYN-ACK";
            case ACK -> "ACK=" + ack + (windowSize > 0 ? " WIN=" + windowSize : "");
            case DATA -> "DATA SEQ=" + seq;
            case FIN -> "FIN";
            case UDP_DATAGRAM -> "UDP";
            case RETRANSMISSION -> "RETX SEQ=" + seq;
        };
        if (payload != null && !payload.isBlank()
                && (kind == PacketKind.DATA || kind == PacketKind.UDP_DATAGRAM || kind == PacketKind.RETRANSMISSION)) {
            return base + " [" + payload + "]";
        }
        return base;
    }
}
