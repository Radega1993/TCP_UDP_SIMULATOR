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
import java.util.Set;
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

    public String detailedTheoryForProtocol(ProtocolType protocolType) {
        Map<String, TheoryTopic> topics = new LinkedHashMap<>();
        appendTopics(topics, theoryService.getTheoryForScenario("*"));
        appendTopics(topics, theoryService.getTheoryForScenario("protocol-" + protocolType.name().toLowerCase()));

        List<String> primaryIds = protocolType == ProtocolType.TCP
                ? List.of("tcp", "tcp-handshake", "ack", "cumulative-ack", "retransmission",
                "sliding-window", "flow-control", "congestion", "slow-start",
                "congestion-avoidance", "fast-retransmit")
                : List.of("udp");
        List<String> networkIds = List.of("latency", "jitter", "packet-loss", "duplication", "reordering");

        StringBuilder builder = new StringBuilder();
        if (protocolType == ProtocolType.TCP) {
            builder.append("TCP\n")
                    .append("Guía docente para interpretar la simulación TCP y relacionarla con lo que ocurre en pantalla.\n\n")
                    .append(section("Conceptos base", topics, primaryIds))
                    .append("\n")
                    .append("Qué observar en la simulación\n")
                    .append("• Handshake 3-way antes de enviar datos.\n")
                    .append("• Segmentos con números de secuencia y ACK visibles.\n")
                    .append("• Posibles retransmisiones si hay pérdida.\n")
                    .append("• Ventana deslizante, bytes en vuelo y avance de ventana.\n\n")
                    .append(section("Condiciones de red y su efecto", topics, networkIds))
                    .append("\n")
                    .append("Cómo usar esta pantalla en clase\n")
                    .append("• Empieza con pérdida 0% para mostrar handshake, envío y cierre.\n")
                    .append("• Después sube la pérdida para provocar retransmisión.\n")
                    .append("• Reduce la ventana o el buffer para enseñar control de flujo.\n");
            return builder.toString();
        }

        builder.append("UDP\n")
                .append("Guía docente para interpretar la simulación UDP y explicar su simplicidad frente a TCP.\n\n")
                .append(section("Conceptos base", topics, primaryIds))
                .append("\n")
                .append("Qué observar en la simulación\n")
                .append("• No hay conexión previa ni handshake.\n")
                .append("• No hay ACK ni retransmisión automática.\n")
                .append("• La red puede perder, duplicar o reordenar datagramas.\n")
                .append("• El mensaje final puede llegar incompleto o desordenado.\n\n")
                .append(section("Condiciones de red y su efecto", topics, networkIds))
                .append("\n")
                .append("Cómo usar esta pantalla en clase\n")
                .append("• Empieza con pérdida 0% para mostrar el envío directo.\n")
                .append("• Luego activa pérdida o reordenación para enseñar ausencia de recuperación nativa.\n")
                .append("• Compara el mensaje enviado con el recibido para ver el impacto real.\n");
        return builder.toString();
    }

    public String detailedTheoryForComparison() {
        Map<String, TheoryTopic> topics = new LinkedHashMap<>();
        appendTopics(topics, theoryService.getTheoryForScenario("comparison"));
        appendTopics(topics, theoryService.getTheoryForScenario("*"));
        appendTopics(topics, theoryService.getTheoryForScenario("protocol-tcp"));
        appendTopics(topics, theoryService.getTheoryForScenario("protocol-udp"));

        StringBuilder builder = new StringBuilder();
        builder.append("Comparación TCP vs UDP\n")
                .append("Usa esta vista para explicar en paralelo cómo dos protocolos responden a la misma red y al mismo mensaje.\n\n")
                .append(section("Idea principal", topics, List.of("tcp-vs-udp-comparison")))
                .append("\n")
                .append("Tabla rápida de comparación\n")
                .append("• Conexión previa: TCP sí / UDP no\n")
                .append("• ACK: TCP sí / UDP no\n")
                .append("• Retransmisión: TCP sí / UDP no\n")
                .append("• Orden garantizado: TCP sí / UDP no\n")
                .append("• Sobrecarga: TCP mayor / UDP menor\n")
                .append("• Casos de uso: TCP web, email, archivos / UDP streaming, VoIP, juegos\n\n")
                .append("Qué observar en la comparación\n")
                .append("• TCP puede tardar más, pero recupera mejor pérdidas.\n")
                .append("• UDP empieza antes y con menos sobrecarga, pero no corrige errores.\n")
                .append("• Bajo la misma red, la diferencia entre fiabilidad y rapidez se hace visible.\n\n")
                .append(section("Condiciones de red y su efecto", topics,
                        List.of("latency", "jitter", "packet-loss", "duplication", "reordering")))
                .append("\n")
                .append("Cómo usar esta pantalla en clase\n")
                .append("• Ejecuta primero con red limpia para comparar sobrecarga básica.\n")
                .append("• Después introduce pérdida y reordenación para ver cómo divergen.\n")
                .append("• Cierra la explicación con el resumen comparado y los logs paralelos.\n");
        return builder.toString();
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

    private String section(String title, Map<String, TheoryTopic> topics, List<String> ids) {
        Set<String> idSet = Set.copyOf(ids);
        String body = topics.values().stream()
                .filter(topic -> idSet.contains(topic.getId()))
                .map(topic -> topic.getTitle() + "\n" + topic.getSummary() + formatBullets(topic))
                .collect(Collectors.joining("\n\n"));
        if (body.isBlank()) {
            return "";
        }
        return title + "\n" + body + "\n";
    }

    public StringProperty statusTextProperty() {
        return statusText;
    }
}
