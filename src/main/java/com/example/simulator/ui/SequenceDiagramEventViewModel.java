package com.example.simulator.ui;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.simulation.Packet;

public class SequenceDiagramEventViewModel {
    private final String id;
    private final Endpoint from;
    private final Endpoint to;
    private final String label;
    private final String colorHex;
    private final Packet packet;
    private final String details;
    private final boolean lost;
    private final boolean retransmitted;

    public SequenceDiagramEventViewModel(String id, Endpoint from, Endpoint to, String label, String colorHex,
                                         Packet packet, String details, boolean lost, boolean retransmitted) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.label = label;
        this.colorHex = colorHex;
        this.packet = packet;
        this.details = details;
        this.lost = lost;
        this.retransmitted = retransmitted;
    }

    public String getId() {
        return id;
    }

    public Endpoint getFrom() {
        return from;
    }

    public Endpoint getTo() {
        return to;
    }

    public String getLabel() {
        return label;
    }

    public String getColorHex() {
        return colorHex;
    }

    public Packet getPacket() {
        return packet;
    }

    public String getDetails() {
        return details;
    }

    public boolean isLost() {
        return lost;
    }

    public boolean isRetransmitted() {
        return retransmitted;
    }
}
