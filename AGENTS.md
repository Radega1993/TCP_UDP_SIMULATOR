# Proyecto: Simulador gráfico TCP/UDP (JavaFX)

## Objetivo

Aplicación educativa para enseñar redes (TCP vs UDP) mediante simulación visual interactiva.

---

## Arquitectura

* Java 17 + JavaFX
* Patrón MVVM
* Separación estricta:

  * domain → lógica de red (TCP, UDP, simulación)
  * application → casos de uso
  * presentation → JavaFX (UI)
  * infrastructure → repositorios y utilidades

⚠️ NUNCA mezclar lógica de red con JavaFX

---

## UI / Diseño

* Estilo minimalista
* Fondo claro
* Bordes redondeados
* Colores suaves

### Colores estándar

* TCP SYN → azul claro
* TCP SYN-ACK → azul medio
* TCP ACK → verde
* TCP DATA → celeste
* UDP → morado
* Perdido → rojo
* Retransmitido → naranja

---

## Componentes UI obligatorios

* PacketNode → visual de paquete animado
* NetworkCanvas → zona central de red
* EventLogPanel → registro de eventos
* ControlPanel → controles de simulación
* StatePanel → estados TCP

---

## Reglas de desarrollo

* Usar JavaFX Properties en ViewModel
* UI solo renderiza datos
* No lógica de negocio en controllers
* Componentes reutilizables
* Código limpio y legible

---

## Simulación

Debe soportar:

* TCP:

  * handshake
  * ACK
  * retransmisión
  * ventana deslizante
  * control de congestión

* UDP:

  * envío sin conexión
  * pérdida de paquetes

---

## Eventos de simulación

* PACKET_SENT
* PACKET_RECEIVED
* PACKET_LOST
* ACK_RECEIVED
* RETRANSMISSION
* STATE_CHANGE

---

## Estilo de código

* Clases pequeñas
* Métodos claros
* Nombres descriptivos
* Evitar lógica duplicada

---

## Objetivo final

Herramienta educativa clara, visual y usable para:

* grado medio
* grado superior
* universidad
