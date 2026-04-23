package com.example.simulator.presentation.viewmodel;

import com.example.simulator.application.dto.SimulationCommand;
import com.example.simulator.application.services.SimulationApplicationService;
import com.example.simulator.application.services.TheoryApplicationService;
import com.example.simulator.domain.education.TheoryTopic;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.scenario.Scenario;
import com.example.simulator.domain.simulation.SimulationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SimulationViewModel {
    private final SimulationApplicationService simulationService;
    private final TheoryApplicationService theoryService;
    private final StringProperty statusText = new SimpleStringProperty("Listo para iniciar");
    private SimulationResult latestResult;

    public SimulationViewModel(SimulationApplicationService simulationService, TheoryApplicationService theoryService) {
        this.simulationService = simulationService;
        this.theoryService = theoryService;
    }

    public SimulationResult start(SimulationCommand command) {
        latestResult = simulationService.start(command);
        statusText.set("Simulación preparada. Reproduciendo eventos.");
        return latestResult;
    }

    public SimulationResult start(Scenario scenario) {
        latestResult = simulationService.start(scenario);
        statusText.set("Escenario cargado. Reproduciendo eventos.");
        return latestResult;
    }

    public void reset() {
        latestResult = null;
        statusText.set("Listo para iniciar");
    }

    public List<Scenario> availableScenarios() {
        return simulationService.loadScenarios();
    }

    public List<TheoryTopic> theoryForLatestScenario() {
        if (latestResult == null) {
            return List.of();
        }
        return theoryService.getTheoryForScenario(latestResult.getScenario().getId());
    }

    public List<TheoryTopic> theoryForScenario(String scenarioId) {
        return theoryService.getTheoryForScenario(scenarioId);
    }

    public String helpTextForContext(String scenarioId, ProtocolType protocolType) {
        Map<String, TheoryTopic> topics = new LinkedHashMap<>();
        appendTopics(topics, theoryService.getTheoryForScenario("*"));
        if (protocolType != null) {
            appendTopics(topics, theoryService.getTheoryForScenario("protocol-" + protocolType.name().toLowerCase()));
        }
        if (scenarioId != null && !scenarioId.isBlank() && !scenarioId.equals("*")) {
            appendTopics(topics, theoryService.getTheoryForScenario(scenarioId));
        }
        return topics.values().stream()
                .map(topic -> topic.getTitle() + "\n" + topic.getSummary() + formatBullets(topic))
                .collect(Collectors.joining("\n\n"));
    }

    private void appendTopics(Map<String, TheoryTopic> target, List<TheoryTopic> topics) {
        for (TheoryTopic topic : topics) {
            target.putIfAbsent(topic.getId(), topic);
        }
    }

    private String formatBullets(TheoryTopic topic) {
        if (topic.getBullets().isEmpty()) {
            return "";
        }
        return "\n" + topic.getBullets().stream()
                .map(bullet -> "• " + bullet)
                .collect(Collectors.joining("\n"));
    }

    public StringProperty statusTextProperty() {
        return statusText;
    }
}
