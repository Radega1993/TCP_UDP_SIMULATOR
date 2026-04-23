# Arquitectura base

El proyecto queda organizado en capas para que la simulación pueda ejecutarse sin JavaFX:

- `domain`: reglas puras de simulación, eventos, snapshots, escenarios y teoría.
- `application`: casos de uso y puertos.
- `infrastructure`: carga de escenarios y teoría desde JSON.
- `presentation`: `ViewModel` y reproductor JavaFX de eventos.
- `app`: bootstrap y arranque.

Reglas de trabajo:

- La UI no decide retransmisiones, estados TCP ni pérdidas.
- El dominio no importa clases de JavaFX.
- Los escenarios viven en `src/main/resources/content/scenarios`.
- El contenido educativo vive en `src/main/resources/content/theory`.
- Los tests deben cubrir `domain` y `application` antes de ampliar la UI.

Flujo actual:

1. `SimulatorApp` recoge entradas del usuario.
2. `SimulationViewModel` invoca `SimulationApplicationService`.
3. `DefaultSimulationEngine` genera `SimulationResult` con eventos y snapshot final.
4. `JavaFxSimulationPlayer` reproduce esos eventos en la UI.

Esto permite cambiar la capa de presentación en el futuro sin reescribir el motor de simulación.
