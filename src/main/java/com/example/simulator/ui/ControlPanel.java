package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.scenario.Scenario;
import com.example.simulator.presentation.viewmodel.SimulationMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;

public class ControlPanel extends VBox {
    private final ComboBox<SimulationMode> modeSelector = new ComboBox<>();
    private final ComboBox<ProtocolType> protocolSelector = new ComboBox<>();
    private final ComboBox<Scenario> scenarioSelector = new ComboBox<>();
    private final TextField messageField = new TextField("HOLAALUMNOS");
    private final Slider lossSlider = new Slider(0, 100, 20);
    private final Slider latencySlider = new Slider(80, 2500, 1200);
    private final Slider jitterSlider = new Slider(0, 800, 0);
    private final Slider duplicationSlider = new Slider(0, 100, 0);
    private final Slider reorderingSlider = new Slider(0, 100, 0);
    private final Slider speedSlider = new Slider(0.5, 3.0, 1.0);
    private final Spinner<Integer> fragmentSizeSpinner = new Spinner<>(1, 16, 8, 1);
    private final Spinner<Integer> bandwidthSpinner = new Spinner<>(0, 50, 0, 1);
    private final Spinner<Integer> tcpWindowSizeSpinner = new Spinner<>(1, 128, 24, 1);
    private final Spinner<Integer> tcpReceiverBufferSpinner = new Spinner<>(1, 128, 24, 1);
    private final Button runButton = new StyledButton("Iniciar simulación", StyledButton.Kind.PRIMARY);
    private final Button playPauseButton = new StyledButton("Pausar", StyledButton.Kind.EMPHASIS);
    private final Button liveStepButton = new StyledButton("Paso", StyledButton.Kind.EMPHASIS);
    private final Button resetButton = new StyledButton("Reiniciar", StyledButton.Kind.SOFT);
    private final Button reviewFirstButton = new StyledButton("|<", StyledButton.Kind.TERTIARY);
    private final Button reviewStepBackButton = new StyledButton("Paso <-", StyledButton.Kind.TERTIARY);
    private final Button reviewStepForwardButton = new StyledButton("Paso ->", StyledButton.Kind.TERTIARY);
    private final Button reviewLastButton = new StyledButton(">|", StyledButton.Kind.TERTIARY);
    private final Label stepIndicatorLabel = new Label("Paso: 0/0");
    private final HBox reviewControlsBox;
    private final DashboardCard actionsCard;

    public ControlPanel(List<Scenario> scenarios) {
        modeSelector.getItems().addAll(SimulationMode.SIMPLE, SimulationMode.COMPARE);
        modeSelector.setValue(SimulationMode.SIMPLE);

        protocolSelector.getItems().addAll(ProtocolType.TCP, ProtocolType.UDP);
        protocolSelector.setValue(ProtocolType.TCP);

        scenarioSelector.getItems().add(null);
        scenarioSelector.getItems().addAll(scenarios);
        scenarioSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Scenario scenario) {
                return scenario == null ? "Personalizado" : scenario.getTitle();
            }

            @Override
            public Scenario fromString(String string) {
                return null;
            }
        });
        scenarioSelector.setValue(null);

        fragmentSizeSpinner.setEditable(true);
        bandwidthSpinner.setEditable(true);
        tcpWindowSizeSpinner.setEditable(true);
        tcpReceiverBufferSpinner.setEditable(true);
        configureSlider(lossSlider, 25);
        configureSlider(latencySlider, 600);
        configureSlider(jitterSlider, 200);
        configureSlider(duplicationSlider, 25);
        configureSlider(reorderingSlider, 25);
        configureSlider(speedSlider, 0.5);

        stepIndicatorLabel.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        DashboardCard scenarioCard = new DashboardCard("CONFIGURACIÓN", "Escenario y protocolo", "Define el contexto base de la simulación.");
        scenarioCard.setContent(fieldGrid(
                field("Escenario", scenarioSelector),
                field("Protocolo", protocolSelector),
                field("Mensaje", messageField)
        ));

        DashboardCard networkCard = new DashboardCard("RED", "Condiciones de red", "Manipula el comportamiento observable de la red compartida.");
        networkCard.setContent(fieldGrid(
                field("Pérdida (%)", lossSlider),
                field("Latencia", latencySlider),
                field("Jitter", jitterSlider),
                field("Duplicación", duplicationSlider),
                field("Reordenación", reorderingSlider),
                field("Bandwidth", bandwidthSpinner),
                field("Tamaño de ventana", tcpWindowSizeSpinner),
                field("Buffer de recepción", tcpReceiverBufferSpinner),
                field("Fragmento", fragmentSizeSpinner)
        ));

        DashboardCard visualCard = new DashboardCard("VISUALIZACIÓN", "Reproducción", "Ajustes de lectura y ritmo de la demo.");
        visualCard.setContent(fieldGrid(field("Velocidad", speedSlider)));

        FlowPane primaryActions = new FlowPane(10, 10, runButton, playPauseButton, liveStepButton, resetButton);
        primaryActions.setAlignment(Pos.CENTER_LEFT);
        primaryActions.setPrefWrapLength(320);
        runButton.setPrefWidth(190);
        playPauseButton.setPrefWidth(118);
        liveStepButton.setPrefWidth(94);
        resetButton.setPrefWidth(118);

        reviewControlsBox = new HBox(10, reviewFirstButton, reviewStepBackButton, reviewStepForwardButton, reviewLastButton, stepIndicatorLabel);
        reviewControlsBox.setAlignment(Pos.CENTER_LEFT);
        reviewControlsBox.setVisible(false);
        reviewControlsBox.setManaged(false);
        reviewControlsBox.setPadding(new Insets(12));
        reviewControlsBox.setStyle(UiTheme.REVIEW_BAR);

        actionsCard = new DashboardCard("ACCIONES", "Controles de ejecución", "Gestiona la ejecución, pausa y revisión por pasos.");
        VBox actionsContent = new VBox(12,
                primaryActions,
                new Separator(),
                reviewControlsBox
        );
        actionsCard.setContent(actionsContent);

        FlowPane cards = new FlowPane(18, 18, scenarioCard, networkCard, visualCard, actionsCard);
        cards.setAlignment(Pos.TOP_LEFT);
        cards.setPrefWrapLength(1450);
        scenarioCard.setPrefWidth(320);
        networkCard.setPrefWidth(470);
        visualCard.setPrefWidth(260);
        actionsCard.setPrefWidth(320);

        setSpacing(18);
        getChildren().add(cards);
    }

    public void hideActionsSection() {
        actionsCard.setManaged(false);
        actionsCard.setVisible(false);
    }

    private void configureSlider(Slider slider, double majorUnit) {
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(majorUnit);
        slider.setStyle("-fx-accent: #2d6df6;");
    }

    private GridPane fieldGrid(FieldEntry... entries) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        ColumnConstraints left = new ColumnConstraints();
        left.setHgrow(Priority.NEVER);
        left.setMinWidth(116);
        ColumnConstraints right = new ColumnConstraints();
        right.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(left, right);

        for (int i = 0; i < entries.length; i++) {
            grid.add(fieldLabel(entries[i].label()), 0, i);
            grid.add(entries[i].node(), 1, i);
        }
        return grid;
    }

    private Node fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(UiTheme.FIELD_LABEL);

        String help = helpTextFor(text);
        if (help == null || help.isBlank()) {
            return label;
        }

        Label hint = new Label("?");
        hint.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2d6df6;"
                + "-fx-background-color: #e8f0ff; -fx-background-radius: 999; -fx-padding: 1 5 1 5;");
        Tooltip tooltip = buildTooltip(help);
        attachHoverTooltip(hint, tooltip);

        HBox box = new HBox(6, label, hint);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Tooltip buildTooltip(String help) {
        Tooltip tooltip = new Tooltip(help);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(320);
        tooltip.setShowDelay(Duration.millis(80));
        tooltip.setHideDelay(Duration.millis(80));
        return tooltip;
    }

    private void attachHoverTooltip(Node target, Tooltip tooltip) {
        target.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> tooltip.show(target, event.getScreenX() + 12, event.getScreenY() + 12));
        target.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            if (tooltip.isShowing()) {
                tooltip.setAnchorX(event.getScreenX() + 12);
                tooltip.setAnchorY(event.getScreenY() + 12);
            }
        });
        target.addEventHandler(MouseEvent.MOUSE_EXITED, event -> tooltip.hide());
    }

    private FieldEntry field(String label, Node node) {
        if (node instanceof TextField textField) {
            textField.setStyle(UiTheme.BODY
                    + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #d5e0ea;"
                    + "-fx-background-color: white; -fx-padding: 10 12 10 12;");
        } else if (node instanceof ComboBox<?> comboBox) {
            comboBox.setStyle(UiTheme.BODY + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 4 6 4 6;");
            comboBox.setMaxWidth(Double.MAX_VALUE);
        } else if (node instanceof Spinner<?> spinner) {
            spinner.setStyle(UiTheme.BODY + "-fx-background-radius: 12; -fx-border-radius: 12;");
            spinner.setMaxWidth(Double.MAX_VALUE);
        } else if (node instanceof Slider slider) {
            Label valueLabel = new Label(formatSliderValue(label, slider.getValue()));
            valueLabel.setMinWidth(66);
            valueLabel.setAlignment(Pos.CENTER_RIGHT);
            valueLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #274057;"
                    + "-fx-background-color: #f1f6fb; -fx-background-radius: 999; -fx-padding: 5 8 5 8;");
            slider.valueProperty().addListener((obs, oldValue, newValue) ->
                    valueLabel.setText(formatSliderValue(label, newValue.doubleValue())));
            HBox wrapper = new HBox(10, slider, valueLabel);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(slider, Priority.ALWAYS);
            slider.setMaxWidth(Double.MAX_VALUE);
            return new FieldEntry(label, wrapper);
        }
        return new FieldEntry(label, node);
    }

    private String formatSliderValue(String label, double value) {
        return switch (label) {
            case "Pérdida (%)", "Duplicación", "Reordenación" -> Math.round(value) + " %";
            case "Latencia", "Jitter" -> Math.round(value) + " ms";
            case "Velocidad" -> String.format(Locale.US, "%.1f x", value);
            default -> String.valueOf(Math.round(value));
        };
    }

    private String helpTextFor(String fieldLabel) {
        return switch (fieldLabel) {
            case "Modo" -> "Modo de trabajo de la pantalla.\nSimulación simple reproduce un protocolo cada vez.\nComparar TCP vs UDP lanza ambos con el mismo mensaje y la misma red.";
            case "Escenario" -> "Carga una situación preparada para clase.\nAl cambiarla se ajustan protocolo, mensaje y condiciones de red.";
            case "Protocolo" -> "Selecciona si deseas simular TCP o UDP.\nTCP añade conexión, ACK y retransmisión.\nUDP envía datagramas sin garantía de entrega ni orden.";
            case "Mensaje" -> "Texto que el cliente intentará enviar al servidor.\nSi el mensaje es más largo o el fragmento es pequeño, aparecerán más paquetes.";
            case "Pérdida (%)" -> "% pérdida\nProbabilidad de que un paquete se pierda antes de llegar al destino.\nSi sube, TCP tenderá a retransmitir más y UDP perderá más información.";
            case "Latencia" -> "Latencia\nTiempo base de tránsito de cada paquete.\nSi sube, todo tarda más en llegar aunque el protocolo siga comportándose igual.";
            case "Jitter" -> "Jitter\nVariación aleatoria añadida a la latencia base.\nSi sube, los paquetes llegan con un ritmo menos regular y es más fácil ver desorden.";
            case "Duplicación" -> "Duplicación\nProbabilidad de que un paquete aparezca repetido.\nSi sube, podrás ver copias extra del mismo paquete en la red.";
            case "Reordenación" -> "Reordenación\nPermite que algunos paquetes lleguen fuera de orden.\nTCP puede reconstruir mejor el flujo; UDP no lo garantiza.";
            case "Bandwidth" -> "Bandwidth\nLímite simplificado de paquetes por segundo.\nSi lo reduces, la red introduce cola y separa más los envíos.";
            case "Tamaño de ventana" -> "Cantidad máxima de datos que pueden permanecer enviados pero todavía no confirmados.\nSi aumentas este valor, TCP puede mantener más bytes en vuelo antes de esperar ACK.";
            case "Buffer de recepción" -> "Espacio disponible en el receptor para aceptar nuevos datos.\nSi lo reduces, el emisor tendrá que frenar antes porque la ventana anunciada será menor.";
            case "Fragmento" -> "Fragmento\nTamaño máximo de cada fragmento o segmento.\nSi lo reduces, el mensaje se divide en más paquetes y el proceso es más visible.";
            case "Velocidad" -> "Velocidad\nControla la rapidez visual de la simulación.\nNo cambia el comportamiento lógico del protocolo, solo la velocidad de animación.";
            default -> null;
        };
    }

    private record FieldEntry(String label, Node node) {
    }

    public ComboBox<SimulationMode> getModeSelector() {
        return modeSelector;
    }

    public ComboBox<ProtocolType> getProtocolSelector() {
        return protocolSelector;
    }

    public ComboBox<Scenario> getScenarioSelector() {
        return scenarioSelector;
    }

    public TextField getMessageField() {
        return messageField;
    }

    public Slider getLossSlider() {
        return lossSlider;
    }

    public Slider getSpeedSlider() {
        return speedSlider;
    }

    public Slider getLatencySlider() {
        return latencySlider;
    }

    public Slider getJitterSlider() {
        return jitterSlider;
    }

    public Slider getDuplicationSlider() {
        return duplicationSlider;
    }

    public Slider getReorderingSlider() {
        return reorderingSlider;
    }

    public Spinner<Integer> getFragmentSizeSpinner() {
        return fragmentSizeSpinner;
    }

    public Spinner<Integer> getBandwidthSpinner() {
        return bandwidthSpinner;
    }

    public Spinner<Integer> getTcpWindowSizeSpinner() {
        return tcpWindowSizeSpinner;
    }

    public Spinner<Integer> getTcpReceiverBufferSpinner() {
        return tcpReceiverBufferSpinner;
    }

    public Button getRunButton() {
        return runButton;
    }

    public Button getPlayPauseButton() {
        return playPauseButton;
    }

    public Button getLiveStepButton() {
        return liveStepButton;
    }

    public Button getResetButton() {
        return resetButton;
    }

    public Button getReviewFirstButton() {
        return reviewFirstButton;
    }

    public Button getReviewStepBackButton() {
        return reviewStepBackButton;
    }

    public Button getReviewStepForwardButton() {
        return reviewStepForwardButton;
    }

    public Button getReviewLastButton() {
        return reviewLastButton;
    }

    public Label getStepIndicatorLabel() {
        return stepIndicatorLabel;
    }

    public HBox getReviewControlsBox() {
        return reviewControlsBox;
    }
}
