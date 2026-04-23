package com.example.simulator.infrastructure.repository.json;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTheoryRepositoryTest {
    @Test
    void loadsSprintOneTcpTheoryTopics() {
        JsonTheoryRepository repository = new JsonTheoryRepository();

        Set<String> topicIds = repository.findByScenario("*").stream()
                .map(topic -> topic.getId())
                .collect(Collectors.toSet());

        assertTrue(topicIds.contains("intro"));
        assertTrue(topicIds.contains("how-to"));
        assertTrue(topicIds.contains("latency"));
        assertTrue(topicIds.contains("jitter"));
        assertTrue(topicIds.contains("packet-loss"));
        assertTrue(topicIds.contains("duplication"));
        assertTrue(topicIds.contains("reordering"));
        assertTrue(topicIds.contains("sliding-window"));
        assertTrue(topicIds.contains("cumulative-ack"));
        assertTrue(topicIds.contains("flow-control"));
        assertTrue(topicIds.contains("congestion"));
        assertTrue(topicIds.contains("slow-start"));
        assertTrue(topicIds.contains("congestion-avoidance"));
        assertTrue(topicIds.contains("fast-retransmit"));
    }

    @Test
    void loadsComparisonTheoryTopic() {
        JsonTheoryRepository repository = new JsonTheoryRepository();

        Set<String> topicIds = repository.findByScenario("comparison").stream()
                .map(topic -> topic.getId())
                .collect(Collectors.toSet());

        assertTrue(topicIds.contains("tcp-vs-udp-comparison"));
    }
}
