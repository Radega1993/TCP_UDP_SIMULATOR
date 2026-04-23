package com.example.simulator.infrastructure.repository.json;

import com.example.simulator.application.ports.out.TheoryRepository;
import com.example.simulator.domain.education.TheoryTopic;

import java.util.Arrays;
import java.util.List;

public class JsonTheoryRepository implements TheoryRepository {
    private final List<TheoryEntry> entries;

    public JsonTheoryRepository() {
        ResourceIndex index = JsonResourceLoader.readResource("content/theory/index.json", ResourceIndex.class);
        this.entries = Arrays.stream(index.files)
                .map(file -> JsonResourceLoader.readResource("content/theory/" + file, TheoryEntry.class))
                .toList();
    }

    @Override
    public List<TheoryTopic> findByScenario(String scenarioId) {
        return entries.stream()
                .filter(entry -> entry.supports(scenarioId))
                .map(TheoryEntry::toDomain)
                .toList();
    }

    private static final class ResourceIndex {
        public String[] files;
    }

    private static final class TheoryEntry {
        public String id;
        public String title;
        public String summary;
        public String[] bullets;
        public String[] scenarios;

        private boolean supports(String scenarioId) {
            return scenarios != null && (Arrays.asList(scenarios).contains("*") || Arrays.asList(scenarios).contains(scenarioId));
        }

        private TheoryTopic toDomain() {
            return new TheoryTopic(
                    id,
                    title,
                    summary,
                    bullets == null ? List.of() : List.of(bullets)
            );
        }
    }
}
