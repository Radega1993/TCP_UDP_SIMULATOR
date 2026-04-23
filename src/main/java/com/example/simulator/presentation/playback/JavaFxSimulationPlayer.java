package com.example.simulator.presentation.playback;

import com.example.simulator.domain.simulation.SimulationEvent;
import com.example.simulator.domain.simulation.SimulationResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class JavaFxSimulationPlayer {
    private final SimulationPlaybackListener listener;
    private final List<SimulationEvent> loadedEvents = new ArrayList<>();
    private Timeline activeTimeline;
    private double speedFactor = 1.0;
    private int nextEventIndex = 0;
    private boolean paused = true;

    public JavaFxSimulationPlayer(SimulationPlaybackListener listener) {
        this.listener = listener;
    }

    public void load(SimulationResult result, double speedFactor) {
        stop();
        loadedEvents.clear();
        loadedEvents.addAll(result.getEvents());
        this.speedFactor = Math.max(0.1, speedFactor);
        this.nextEventIndex = 0;
        this.paused = true;
        if (Platform.isFxApplicationThread()) {
            listener.onReset();
        } else {
            Platform.runLater(listener::onReset);
        }
    }

    public void play() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::play);
            return;
        }
        if (!hasRemainingEvents()) {
            return;
        }
        paused = false;
        dispatchImmediateEvents();
        scheduleNext();
    }

    public void pause() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::pause);
            return;
        }
        paused = true;
        if (activeTimeline != null) {
            activeTimeline.stop();
            activeTimeline = null;
        }
    }

    public void stepForward() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::stepForward);
            return;
        }
        pause();
        if (!hasRemainingEvents()) {
            return;
        }
        dispatch(loadedEvents.get(nextEventIndex));
        nextEventIndex++;
    }

    public void stop() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::stop);
            return;
        }
        paused = true;
        if (activeTimeline != null) {
            activeTimeline.stop();
            activeTimeline = null;
        }
        loadedEvents.clear();
        nextEventIndex = 0;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean hasRemainingEvents() {
        return nextEventIndex < loadedEvents.size();
    }

    private void scheduleNext() {
        if (paused || !hasRemainingEvents()) {
            return;
        }
        if (activeTimeline != null) {
            activeTimeline.stop();
        }
        long delay = computeDelayMillis();
        if (delay <= 0L) {
            dispatchImmediateEvents();
            scheduleNext();
            return;
        }
        activeTimeline = new Timeline(new KeyFrame(Duration.millis(delay), ignored -> {
            dispatch(loadedEvents.get(nextEventIndex));
            nextEventIndex++;
            dispatchImmediateEvents();
            activeTimeline = null;
            scheduleNext();
        }));
        activeTimeline.setCycleCount(1);
        activeTimeline.playFromStart();
    }

    private long computeDelayMillis() {
        if (nextEventIndex == 0) {
            return 0L;
        }
        long currentTime = loadedEvents.get(nextEventIndex).getTimestampMillis();
        long previousTime = loadedEvents.get(nextEventIndex - 1).getTimestampMillis();
        return Math.max(0L, Math.round((currentTime - previousTime) / speedFactor));
    }

    private void dispatchImmediateEvents() {
        while (!paused && hasRemainingEvents() && computeDelayMillis() == 0L) {
            dispatch(loadedEvents.get(nextEventIndex));
            nextEventIndex++;
        }
    }

    private void dispatch(SimulationEvent event) {
        switch (event.getType()) {
            case LOG -> listener.onLog(event.getMessage());
            case PACKET_CREATED -> listener.onPacketCreated(event.getPacket());
            case PACKET_DELIVERED -> listener.onPacketDelivered(event.getPacket());
            case PACKET_LOST -> listener.onPacketLost(event.getPacket());
            case TCP_STATE_CHANGED -> listener.onTcpStateChanged(event.getEndpoint(), event.getTcpState());
            case MESSAGE_DELIVERED -> listener.onMessageDelivered(event.getMessage());
            case SCENARIO_COMPLETED -> listener.onScenarioCompleted();
        }
    }
}
