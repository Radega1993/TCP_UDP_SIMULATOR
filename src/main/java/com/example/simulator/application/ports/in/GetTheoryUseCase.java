package com.example.simulator.application.ports.in;

import com.example.simulator.domain.education.TheoryTopic;

import java.util.List;

public interface GetTheoryUseCase {
    List<TheoryTopic> getTheoryForScenario(String scenarioId);
}
