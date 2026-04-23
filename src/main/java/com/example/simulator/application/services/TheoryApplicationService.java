package com.example.simulator.application.services;

import com.example.simulator.application.ports.in.GetTheoryUseCase;
import com.example.simulator.application.ports.out.TheoryRepository;
import com.example.simulator.domain.education.TheoryTopic;

import java.util.List;

public class TheoryApplicationService implements GetTheoryUseCase {
    private final TheoryRepository theoryRepository;

    public TheoryApplicationService(TheoryRepository theoryRepository) {
        this.theoryRepository = theoryRepository;
    }

    @Override
    public List<TheoryTopic> getTheoryForScenario(String scenarioId) {
        return theoryRepository.findByScenario(scenarioId);
    }
}
