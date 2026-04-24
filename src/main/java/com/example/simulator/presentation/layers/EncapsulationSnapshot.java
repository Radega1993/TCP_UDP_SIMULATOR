package com.example.simulator.presentation.layers;

import java.util.List;

public class EncapsulationSnapshot {
    private final String payload;
    private final String transportTitle;
    private final List<HeaderFieldViewModel> transportHeader;
    private final List<HeaderFieldViewModel> ipHeader;
    private final List<HeaderFieldViewModel> linkHeader;
    private final String transportUnitName;
    private final String networkUnitName;
    private final String linkUnitName;

    public EncapsulationSnapshot(String payload, String transportTitle,
                                 List<HeaderFieldViewModel> transportHeader,
                                 List<HeaderFieldViewModel> ipHeader,
                                 List<HeaderFieldViewModel> linkHeader,
                                 String transportUnitName, String networkUnitName, String linkUnitName) {
        this.payload = payload;
        this.transportTitle = transportTitle;
        this.transportHeader = List.copyOf(transportHeader);
        this.ipHeader = List.copyOf(ipHeader);
        this.linkHeader = List.copyOf(linkHeader);
        this.transportUnitName = transportUnitName;
        this.networkUnitName = networkUnitName;
        this.linkUnitName = linkUnitName;
    }

    public String getPayload() {
        return payload;
    }

    public String getTransportTitle() {
        return transportTitle;
    }

    public List<HeaderFieldViewModel> getTransportHeader() {
        return transportHeader;
    }

    public List<HeaderFieldViewModel> getIpHeader() {
        return ipHeader;
    }

    public List<HeaderFieldViewModel> getLinkHeader() {
        return linkHeader;
    }

    public String getTransportUnitName() {
        return transportUnitName;
    }

    public String getNetworkUnitName() {
        return networkUnitName;
    }

    public String getLinkUnitName() {
        return linkUnitName;
    }
}
