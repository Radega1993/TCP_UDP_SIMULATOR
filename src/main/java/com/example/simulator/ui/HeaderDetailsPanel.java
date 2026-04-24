package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.presentation.layers.EncapsulationSnapshot;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class HeaderDetailsPanel extends VBox {
    private final Label contextLabel = new Label();
    private final HeaderTabsPanel headerTabsPanel = new HeaderTabsPanel();
    private final PacketStructureBar packetStructureBar = new PacketStructureBar();
    private final StyledButton inspectButton = new StyledButton("Abrir inspector de paquete", StyledButton.Kind.PRIMARY);

    public HeaderDetailsPanel(Consumer<String> packetInspector) {
        setSpacing(16);
        inspectButton.setOnAction(event -> packetInspector.accept(inspectButton.getUserData() == null
                ? "No hay paquete disponible."
                : inspectButton.getUserData().toString()));
    }

    public void update(EncapsulationSnapshot snapshot, LearningLevel level, ProtocolType protocolType,
                       boolean usingRealPacket, String packetStructureText) {
        contextLabel.setText(usingRealPacket
                ? "Inspeccionando la estructura del último paquete observado o seleccionado."
                : "Mostrando una estructura didáctica representativa para entender mejor el protocolo activo.");
        contextLabel.setWrapText(true);
        contextLabel.setStyle(UiTheme.SUBTITLE);
        inspectButton.setUserData(packetStructureText);

        headerTabsPanel.update(snapshot, level, protocolType);
        packetStructureBar.update(snapshot, level);

        DashboardCard shell = new DashboardCard("DETALLE", "Cabeceras y estructura del paquete",
                "Consulta cabeceras simplificadas y una representación visual del paquete completo.");
        VBox content = new VBox(12, contextLabel, headerTabsPanel, packetStructureBar, inspectButton);
        content.setPadding(new Insets(2, 0, 0, 0));
        shell.setContent(content);
        getChildren().setAll(shell);
    }
}
