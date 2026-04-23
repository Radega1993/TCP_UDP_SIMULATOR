package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.scenario.Scenario;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

public class ControlPanel extends DashboardCard {
    private final ComboBox<ProtocolType> protocolSelector = new ComboBox<>();
    private final ComboBox<Scenario> scenarioSelector = new ComboBox<>();
    private final TextField messageField = new TextField("HOLAALUMNOS");
    private final Slider lossSlider = new Slider(0, 100, 20);
    private final Slider speedSlider = new Slider(0.5, 3.0, 1.0);
    private final Spinner<Integer> fragmentSizeSpinner = new Spinner<>(1, 16, 8, 1);
    private final Spinner<Integer> widthSpinner = new Spinner<>(980, 2000, 1380, 20);
    private final Spinner<Integer> heightSpinner = new Spinner<>(680, 1400, 780, 20);
    private final ComboBox<String> windowPresetSelector = new ComboBox<>();
    private final Button runButton = button("Iniciar simulación", "#2563eb");
    private final Button playPauseButton = button("Pausar", "#0f766e");
    private final Button liveStepButton = button("Paso", "#0f766e");
    private final Button resetButton = button("Reiniciar", "#e2e8f0", "#0f172a");
    private final Button applySizeButton = button("Aplicar tamaño", "#0ea5e9");
    private final Button presetSizeButton = button("Usar preset", "#38bdf8");
    private final Button reviewFirstButton = button("|<", "#4338ca");
    private final Button reviewStepBackButton = button("Paso <-", "#6366f1");
    private final Button reviewStepForwardButton = button("Paso ->", "#4f46e5");
    private final Button reviewLastButton = button(">|", "#3730a3");
    private final Label stepIndicatorLabel = new Label("Paso: 0/0");
    private final HBox reviewControlsBox;

    public ControlPanel(List<Scenario> scenarios) {
        super("Controles de simulación", "Configura el escenario, la red y la velocidad visual desde un único panel.");

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

        widthSpinner.setEditable(true);
        heightSpinner.setEditable(true);
        fragmentSizeSpinner.setEditable(true);

        lossSlider.setShowTickLabels(true);
        lossSlider.setShowTickMarks(true);
        lossSlider.setMajorTickUnit(25);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);

        windowPresetSelector.getItems().addAll("1280x720", "1366x768", "1600x900", "1920x1080");
        windowPresetSelector.setValue("1366x768");

        stepIndicatorLabel.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");

        installTooltips();

        VBox scenarioSection = groupedSection("Escenario y protocolo",
                fieldGrid(
                        field("Escenario", scenarioSelector),
                        field("Protocolo", protocolSelector),
                        field("Mensaje", messageField)
                ));

        VBox networkSection = groupedSection("Condiciones de red",
                fieldGrid(
                        field("Pérdida (%)", lossSlider),
                        field("Fragmento", fragmentSizeSpinner)
                ));

        VBox visualSection = groupedSection("Visualización",
                fieldGrid(
                        field("Velocidad", speedSlider),
                        field("Ancho ventana", widthSpinner),
                        field("Alto ventana", heightSpinner),
                        field("Preset ventana", windowPresetSelector)
                ));

        HBox groupedSections = new HBox(10, scenarioSection, networkSection, visualSection);
        HBox.setHgrow(scenarioSection, Priority.ALWAYS);
        HBox.setHgrow(networkSection, Priority.ALWAYS);
        HBox.setHgrow(visualSection, Priority.ALWAYS);
        scenarioSection.setMaxWidth(Double.MAX_VALUE);
        networkSection.setMaxWidth(Double.MAX_VALUE);
        visualSection.setMaxWidth(Double.MAX_VALUE);

        runButton.setPrefWidth(168);
        playPauseButton.setPrefWidth(118);
        liveStepButton.setPrefWidth(92);
        resetButton.setPrefWidth(118);
        applySizeButton.setPrefWidth(132);
        presetSizeButton.setPrefWidth(122);

        Label actionsLabel = new Label("ACCIONES");
        actionsLabel.setStyle(UiTheme.SECTION_LABEL);

        FlowPane primaryActions = new FlowPane(8, 8, runButton, playPauseButton, liveStepButton);
        primaryActions.setAlignment(Pos.CENTER_LEFT);
        primaryActions.setPrefWrapLength(420);

        FlowPane secondaryActions = new FlowPane(8, 8, resetButton, applySizeButton, presetSizeButton);
        secondaryActions.setAlignment(Pos.CENTER_LEFT);
        secondaryActions.setPrefWrapLength(420);

        VBox actionsBox = new VBox(8, actionsLabel, primaryActions, secondaryActions);
        actionsBox.setPadding(new Insets(10));
        actionsBox.setStyle(UiTheme.PANEL_INSET_TINT);

        reviewControlsBox = new HBox(10, new Label("Revisión:"), reviewFirstButton, reviewStepBackButton, reviewStepForwardButton, reviewLastButton, stepIndicatorLabel);
        reviewControlsBox.setAlignment(Pos.CENTER_LEFT);
        reviewControlsBox.setVisible(false);
        reviewControlsBox.setManaged(false);
        reviewControlsBox.setPadding(new Insets(8, 10, 8, 10));
        reviewControlsBox.setStyle(UiTheme.REVIEW_BAR);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        reviewControlsBox.getChildren().add(1, spacer);

        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 0, 0, 0));

        getContentBox().getChildren().addAll(groupedSections, separator, actionsBox, reviewControlsBox);
    }

    private void installTooltips() {
        protocolSelector.setTooltip(new Tooltip("Selecciona si deseas simular TCP o UDP."));
        messageField.setTooltip(new Tooltip("Texto que el cliente intentará enviar al servidor."));
        speedSlider.setTooltip(new Tooltip("Controla la rapidez visual de la simulación. No cambia el comportamiento lógico del protocolo, solo la velocidad de animación."));
    }

    private static Button button(String text, String backgroundColor) {
        return button(text, backgroundColor, "white");
    }

    private static Button button(String text, String backgroundColor, String textColor) {
        Button button = new Button(text);
        String style;
        if ("#2563eb".equals(backgroundColor)) {
            style = UiTheme.PRIMARY_BUTTON;
        } else if ("#0f766e".equals(backgroundColor)) {
            style = UiTheme.EMPHASIS_BUTTON;
        } else {
            style = UiTheme.SOFT_BUTTON;
        }
        button.setStyle(style);
        button.setTextFill(javafx.scene.paint.Paint.valueOf(textColor));
        button.setPrefHeight(40);
        return button;
    }

    private VBox groupedSection(String title, Node content) {
        Label label = new Label(title);
        label.setStyle(UiTheme.SECTION_LABEL);

        VBox wrapper = new VBox(8, label, content);
        wrapper.setPadding(new Insets(10));
        wrapper.setStyle(UiTheme.PANEL_INSET_TINT);
        return wrapper;
    }

    private GridPane fieldGrid(FieldEntry... entries) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints left = new ColumnConstraints();
        left.setHgrow(Priority.NEVER);
        left.setMinWidth(104);
        ColumnConstraints right = new ColumnConstraints();
        right.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(left, right);

        for (int i = 0; i < entries.length; i++) {
            Label label = new Label(entries[i].label());
            label.setStyle(UiTheme.FIELD_LABEL);
            grid.add(label, 0, i);
            grid.add(entries[i].node(), 1, i);
        }
        return grid;
    }

    private FieldEntry field(String label, Node node) {
        if (node instanceof TextField textField) {
            textField.setStyle(UiTheme.BODY + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #d5e0ea; -fx-background-color: white; -fx-padding: 10 12 10 12;");
        } else if (node instanceof ComboBox<?> comboBox) {
            comboBox.setStyle(UiTheme.BODY + "-fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 4 6 4 6;");
            comboBox.setMaxWidth(Double.MAX_VALUE);
        } else if (node instanceof Spinner<?> spinner) {
            spinner.setStyle(UiTheme.BODY + "-fx-background-radius: 12; -fx-border-radius: 12;");
            spinner.setMaxWidth(Double.MAX_VALUE);
        } else if (node instanceof Slider slider) {
            slider.setMaxWidth(Double.MAX_VALUE);
            slider.setStyle("-fx-accent: #2d6df6;");
        }
        return new FieldEntry(label, node);
    }

    private record FieldEntry(String label, Node node) {
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

    public Spinner<Integer> getFragmentSizeSpinner() {
        return fragmentSizeSpinner;
    }

    public Spinner<Integer> getWidthSpinner() {
        return widthSpinner;
    }

    public Spinner<Integer> getHeightSpinner() {
        return heightSpinner;
    }

    public ComboBox<String> getWindowPresetSelector() {
        return windowPresetSelector;
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

    public Button getApplySizeButton() {
        return applySizeButton;
    }

    public Button getPresetSizeButton() {
        return presetSizeButton;
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
