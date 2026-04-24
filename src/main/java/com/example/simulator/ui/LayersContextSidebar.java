package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import javafx.scene.layout.VBox;

public class LayersContextSidebar extends VBox {
    public LayersContextSidebar() {
        setSpacing(12);
    }

    public void update(LayersMode mode, ProtocolType protocolType, LearningLevel level) {
        getChildren().setAll(
                new TheorySidebarCard(
                        "¿Qué es cada modelo?",
                        "OSI es un modelo teórico de 7 capas. TCP/IP es un modelo práctico de 4 capas usado en Internet.",
                        "Ambos modelos ayudan a entender redes desde dos perspectivas complementarias."
                ),
                new TheorySidebarCard(
                        "Funciones por capa",
                        switch (mode) {
                            case OSI -> "OSI separa funciones con mucho detalle para estudiar comunicación, formato, sesión, transporte, red y acceso al medio.";
                            case TCP_IP -> "TCP/IP agrupa funciones en cuatro grandes bloques prácticos: aplicación, transporte, Internet y acceso a red.";
                            case ENCAPSULATION -> "Cada capa añade cabeceras y prepara la unidad para que la siguiente pueda seguir encapsulando.";
                            case PACKET_STRUCTURE -> "Las cabeceras muestran qué metadatos necesita cada protocolo para cumplir su función.";
                            case COMPARISON -> "La comparación deja ver cómo varias capas OSI se agrupan dentro del modelo TCP/IP.";
                        },
                        "Ayuda rápida para situar la explicación principal."
                ),
                new TheorySidebarCard(
                        "Relación clave",
                        protocolType == ProtocolType.TCP
                                ? "TCP trabaja en Transporte, IP en Internet/Red y Ethernet en Acceso a red."
                                : "UDP trabaja en Transporte, IP en Internet/Red y Ethernet en Acceso a red.",
                        "Ubica rápidamente los protocolos más conocidos dentro de la arquitectura."
                ),
                new TheorySidebarCard(
                        "Ayuda contextual",
                        level == LearningLevel.BASIC
                                ? "En básico conviene fijarse primero en la función de cada capa y en el orden de encapsulación."
                                : "En avanzado conviene relacionar funciones con campos concretos de cabecera y tamaños aproximados.",
                        "Sugerencia docente para aprovechar mejor la vista actual."
                )
        );
    }
}
