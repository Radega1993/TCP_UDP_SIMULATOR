package com.example.simulator.domain.education;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheoryTopic {
    private final String id;
    private final String title;
    private final String summary;
    private final List<String> bullets;

    public TheoryTopic(String id, String title, String summary, List<String> bullets) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.bullets = Collections.unmodifiableList(new ArrayList<>(bullets));
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getBullets() {
        return bullets;
    }
}
