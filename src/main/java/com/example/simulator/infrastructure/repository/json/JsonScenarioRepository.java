package com.example.simulator.infrastructure.repository.json;

import com.example.simulator.application.ports.out.ScenarioRepository;
import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.scenario.Scenario;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JsonScenarioRepository implements ScenarioRepository {
    private final List<Scenario> scenarios;

    public JsonScenarioRepository() {
        ResourceIndex index = JsonResourceLoader.readResource("content/scenarios/index.json", ResourceIndex.class);
        this.scenarios = Arrays.stream(index.files)
                .map(file -> JsonResourceLoader.readResource("content/scenarios/" + file, ScenarioDocument.class))
                .map(ScenarioDocument::toDomain)
                .toList();
    }

    @Override
    public Optional<Scenario> findById(String id) {
        return scenarios.stream()
                .filter(scenario -> scenario.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Scenario> findAll() {
        return scenarios;
    }

    private static final class ResourceIndex {
        public String[] files;
    }

    private static final class ScenarioDocument {
        public String id;
        public String title;
        public String protocol;
        public String message;
        public int fragmentSize;
        public double packetLossRate;
        public long baseLatencyMillis = 1200L;
        public long jitterMillis;
        public double duplicationRate;
        public double reorderingRate;
        public int bandwidthPacketsPerSecond;
        public int tcpWindowSizeBytes = 24;
        public int tcpReceiverBufferBytes = 24;
        public int[] forcedLossIndexes;
        public String[] educationalTopics;

        private Scenario toDomain() {
            return new Scenario(
                    id,
                    title,
                    ProtocolType.valueOf(protocol),
                    message,
                    fragmentSize,
                    tcpWindowSizeBytes,
                    tcpReceiverBufferBytes,
                    new NetworkConditions(
                            packetLossRate,
                            baseLatencyMillis,
                            jitterMillis,
                            duplicationRate,
                            reorderingRate,
                            bandwidthPacketsPerSecond,
                            forcedLossIndexes == null ? java.util.Set.of() : Arrays.stream(forcedLossIndexes).boxed().collect(java.util.stream.Collectors.toSet())
                    ),
                    educationalTopics == null ? List.of() : List.of(educationalTopics)
            );
        }
    }
}
