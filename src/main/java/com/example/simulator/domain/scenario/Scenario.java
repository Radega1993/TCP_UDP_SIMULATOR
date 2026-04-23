package com.example.simulator.domain.scenario;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.ProtocolType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scenario {
    private final String id;
    private final String title;
    private final ProtocolType protocol;
    private final String message;
    private final int fragmentSize;
    private final int tcpWindowSizeBytes;
    private final int tcpReceiverBufferBytes;
    private final NetworkConditions networkConditions;
    private final List<String> educationalTopics;

    public Scenario(String id, String title, ProtocolType protocol, String message, int fragmentSize,
                    NetworkConditions networkConditions, List<String> educationalTopics) {
        this(id, title, protocol, message, fragmentSize, 24, 24, networkConditions, educationalTopics);
    }

    public Scenario(String id, String title, ProtocolType protocol, String message, int fragmentSize,
                    int tcpWindowSizeBytes, int tcpReceiverBufferBytes,
                    NetworkConditions networkConditions, List<String> educationalTopics) {
        this.id = id;
        this.title = title;
        this.protocol = protocol;
        this.message = message;
        this.fragmentSize = Math.max(1, fragmentSize);
        this.tcpWindowSizeBytes = Math.max(1, tcpWindowSizeBytes);
        this.tcpReceiverBufferBytes = Math.max(1, tcpReceiverBufferBytes);
        this.networkConditions = networkConditions;
        this.educationalTopics = Collections.unmodifiableList(new ArrayList<>(educationalTopics));
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public String getMessage() {
        return message;
    }

    public int getFragmentSize() {
        return fragmentSize;
    }

    public int getTcpWindowSizeBytes() {
        return tcpWindowSizeBytes;
    }

    public int getTcpReceiverBufferBytes() {
        return tcpReceiverBufferBytes;
    }

    public NetworkConditions getNetworkConditions() {
        return networkConditions;
    }

    public List<String> getEducationalTopics() {
        return educationalTopics;
    }

    @Override
    public String toString() {
        return title;
    }
}
