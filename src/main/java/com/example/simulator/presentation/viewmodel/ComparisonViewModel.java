package com.example.simulator.presentation.viewmodel;

import com.example.simulator.application.dto.ComparisonCommand;
import com.example.simulator.application.dto.ProtocolComparisonResult;
import com.example.simulator.application.services.ProtocolComparisonApplicationService;

public class ComparisonViewModel {
    private final ProtocolComparisonApplicationService comparisonService;
    private ProtocolComparisonResult latestResult;

    public ComparisonViewModel(ProtocolComparisonApplicationService comparisonService) {
        this.comparisonService = comparisonService;
    }

    public ProtocolComparisonResult startComparison(ComparisonCommand command) {
        latestResult = comparisonService.compare(command);
        return latestResult;
    }

    public ProtocolComparisonResult getLatestResult() {
        return latestResult;
    }
}
