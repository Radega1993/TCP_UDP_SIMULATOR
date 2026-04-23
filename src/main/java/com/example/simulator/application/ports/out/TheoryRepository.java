package com.example.simulator.application.ports.out;

import com.example.simulator.domain.education.TheoryTopic;

import java.util.List;

public interface TheoryRepository {
    List<TheoryTopic> findByScenario(String scenarioId);
}
