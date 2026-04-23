package com.example.simulator.infrastructure.repository.json;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTheoryRepositoryTest {
    @Test
    void loadsSprintOneTcpTheoryTopics() {
        JsonTheoryRepository repository = new JsonTheoryRepository();

        Set<String> topicIds = repository.findByScenario("protocol-tcp").stream()
                .map(topic -> topic.getId())
                .collect(Collectors.toSet());

        assertTrue(topicIds.contains("tcp"));
        assertTrue(topicIds.contains("tcp-handshake"));
        assertTrue(topicIds.contains("ack"));
        assertTrue(topicIds.contains("retransmission"));
    }
}
