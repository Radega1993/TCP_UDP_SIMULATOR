# Resumen de la UI Implementada

Este documento describe los componentes visibles de la interfaz del simulador, cómo se muestran y en qué zona de la pantalla aparecen.

La UI actual tiene dos modos principales:

- `Simulación simple`
- `Comparar TCP vs UDP`

Ambos comparten la misma ventana principal, el mismo panel de controles y el mismo estilo visual general.

## Estructura general de la ventana

La ventana principal se organiza de arriba hacia abajo así:

1. `Header superior`
2. `Panel de controles`
3. `Área principal de contenido`
   - en modo simple: simulación individual + barra lateral derecha
   - en modo comparación: doble panel TCP/UDP + teoría/resumen/logs

La pantalla completa está dentro de un `ScrollPane`, así que el contenido puede desplazarse en pantallas más pequeñas sin romperse.

---

## 1. Header superior

### Componente

- `SimulatorHeader`

### Dónde aparece

- En la parte más alta de la ventana.

### Qué muestra

- Título principal del simulador.
- Subtítulo explicativo breve.

### Función visual

- Presentar la aplicación.
- Dar contexto didáctico rápido.
- Mantener una entrada visual clara antes de los controles.

---

## 2. Panel de controles

### Componente principal

- `ControlPanel`

### Dónde aparece

- Justo debajo del header.
- Ocupa el ancho principal superior del contenido.

### Qué contiene

#### Bloque `Escenario y protocolo`

- `Modo`
  - Permite alternar entre `Simulación simple` y `Comparar TCP vs UDP`.
- `Escenario`
  - Selector de escenarios cargados desde JSON.
- `Protocolo`
  - Selector de `TCP` o `UDP`.
  - En modo comparación queda deshabilitado, porque el modo usa ambos a la vez.
- `Mensaje`
  - Texto que se enviará desde cliente a servidor.

#### Bloque `Condiciones de red`

- `Pérdida (%)`
- `Latencia`
- `Jitter`
- `Duplicación`
- `Reordenación`
- `Bandwidth`
- `Fragmento`

Estos controles modifican la red simulada y afectan al comportamiento del motor.

#### Bloque `Visualización`

- `Velocidad`

Solo altera la rapidez visual de reproducción, no la lógica de red.

#### Bloque `Acciones`

- `Iniciar simulación` o `Iniciar comparación`
- `Pausar` / `Reanudar`
- `Paso`
- `Reiniciar`

#### Bloque `Revisión`

- `|<`
- `Paso <-`
- `Paso ->`
- `>|`
- indicador `Paso: actual/total`

Este bloque se muestra sobre todo en el modo simple cuando termina una simulación y hay snapshots guardados para revisión.

### Cómo se muestra

- Como una gran tarjeta con subgrupos internos.
- Cada grupo tiene borde suave, padding y labels claros.
- Los campos configurables muestran un icono `?` con hover para explicar qué hace cada valor.
- Los sliders muestran su valor visible con unidad:
  - `%`
  - `ms`
  - `x`

---

## 3. Área principal en modo simple

Cuando el selector `Modo` está en `Simulación simple`, aparece el layout clásico del simulador individual.

### Estructura horizontal

La zona principal se divide en dos grandes columnas:

1. `Columna principal de simulación`
2. `Barra lateral derecha`

---

## 3.1 Columna principal de simulación

### Dónde aparece

- Parte izquierda y central de la pantalla.

### Componentes

#### `MailboxPanel` del cliente

- Título: `Buzón cliente`
- Subtítulo: `Salida del emisor`
- Muestra el historial visual de paquetes que salen o quedan archivados del lado cliente.

#### `NetworkCanvas`

- Es la zona central principal.
- Muestra:
  - nodo `Cliente`
  - nodo `Servidor`
  - línea de red intermedia
  - espacio de tránsito para los paquetes animados

Es el foco visual principal del simulador.

#### `MailboxPanel` del servidor

- Título: `Buzón servidor`
- Subtítulo: `Llegadas al receptor`
- Muestra el historial visual de paquetes archivados en el lado servidor.

### Cómo se muestran

- Los tres bloques se colocan en fila dentro de `simulationRow`.
- El `NetworkCanvas` ocupa la zona protagonista.
- Los buzones laterales actúan como historial visual auxiliar.

---

## 3.2 Barra lateral derecha en modo simple

### Dónde aparece

- A la derecha del área principal.

### Componentes visibles de arriba a abajo

#### Botón `Teoría`

- Abre una ventana independiente.
- No ocupa una gran tarjeta de contenido fija.
- Sirve para consultar teoría contextual del escenario o protocolo actual.

#### `StatePanel`

- Muestra:
  - estado TCP del cliente
  - estado TCP del servidor
  - estado general o mensaje de estado

Es el panel de estado operativo rápido.

#### Tarjeta `Leyenda visual`

- Explica los colores de paquetes y eventos.
- Incluye referencia para:
  - `TCP SYN`
  - `TCP SYN-ACK`
  - `TCP ACK`
  - `TCP DATA`
  - `UDP`
  - `Perdido`
  - `Retransmitido`

#### `EventLogPanel`

- Registro textual cronológico de eventos.
- Muestra el flujo de lo que ocurre durante la simulación.
- Está compactado para no ocupar media pantalla en vertical.

#### Tarjeta `Mensajes y reconstrucción`

- Muestra dos paneles en paralelo:
  - `Cliente`
  - `Servidor`

Cada uno usa `MessageSummaryPanel`.

##### Panel `Cliente`

- Muestra el mensaje base o enviado.
- También puede mostrar fragmentación o contexto del protocolo.

##### Panel `Servidor`

- Muestra el mensaje recibido o reconstruido.
- Es especialmente útil para ver pérdidas, diferencias, reordenación o reconstrucción final.

### Cómo se muestra

- Todo aparece en una única columna derecha.
- Está pensado para lectura rápida mientras la simulación ocurre en la zona central.

---

## 4. Modales y ventanas auxiliares en modo simple

Hay dos ventanas secundarias importantes:

### 4.1 Ventana `Detalle del paquete`

### Cómo se abre

- Al hacer clic sobre cualquier `PacketNode`.

### Qué muestra

- Protocolo
- Tipo de paquete
- Origen
- Destino
- `SEQ`
- `ACK`
- `Payload`
- Estado

### Dónde aparece

- Como ventana independiente.
- No reserva espacio fijo en la UI principal.

### Objetivo

- Evitar saturar la barra lateral.
- Mostrar detalle solo cuando el usuario lo necesita.

### 4.2 Ventana `Teoría y ayuda`

### Cómo se abre

- Desde el botón `Teoría`.

### Qué muestra

- Texto contextual asociado al protocolo y escenario actual.

### Dónde aparece

- Como ventana independiente.

### Objetivo

- Mantener la teoría accesible sin colapsar la UI principal.

---

## 5. Representación de paquetes

### Componente

- `PacketNode`

### Dónde aparece

- En el `NetworkCanvas` durante el tránsito.
- En los `MailboxPanel` cuando se archivan visualmente.

### Qué muestra

- Tipo de paquete
- Metadata básica
  - `SEQ`
  - `ACK`
  - número de datagrama
- Vista previa del payload

### Estados visuales

- `En tránsito`
- `Entregado`
- `Perdido`
- `Retransmitido`

### Objetivo visual

- Que el alumno vea el protocolo “moverse”.
- Hacer legible el rol de cada paquete.

---

## 6. Área principal en modo comparación

Cuando el selector `Modo` está en `Comparar TCP vs UDP`, desaparece el layout simple y aparece una vista específica de comparación.

### Componente principal

- `ComparisonModeView`

### Dónde aparece

- En la misma zona donde antes se mostraba la vista simple.

### Estructura general

1. Título y subtítulo del modo comparación
2. Zona dual con dos paneles grandes
3. Fila de apoyo con resumen y teoría
4. Logs paralelos

---

## 6.1 Encabezado del modo comparación

### Qué muestra

- Título: `Comparación TCP vs UDP`
- Subtítulo explicando que ambos usan la misma entrada y la misma red

### Objetivo

- Dejar claro que se trata de una demo comparativa y no de una simulación individual.

---

## 6.2 Zona dual de comparación

### Componente

- Dos instancias de `ComparisonProtocolPane`

### Distribución

- Izquierda: `TCP`
- Derecha: `UDP`

### Cada panel incluye

#### Cabecera del panel

- Título del protocolo
- Subtítulo breve
- pequeña etiqueta visual indicando el lado

#### Zona central de simulación

- `MailboxPanel` cliente
- `NetworkCanvas`
- `MailboxPanel` servidor

#### Panel de estados

- `StatePanel`

#### Mensajes

- Dos `MessageSummaryPanel`:
  - mensaje base compartido
  - resultado recibido

### Objetivo

- Mostrar ambos comportamientos lado a lado con la misma base de entrada y red.

---

## 6.3 Resumen final comparado

### Componente

- `ComparisonSummaryPanel`

### Dónde aparece

- Debajo de la zona dual, en la fila de apoyo.

### Qué muestra

- Si TCP entregó completo
- Si UDP entregó completo
- Si TCP retransmitió
- Si UDP retransmitió
- Si TCP mantuvo orden
- Si UDP mantuvo orden o no lo garantiza
- Sobrecarga visible por cantidad de eventos
- Un texto final con observaciones interpretables

### Objetivo

- Convertir la comparación en una conclusión clara para clase.

---

## 6.4 Teoría comparativa visible

### Componente

- `TheoryComparisonPanel`

### Dónde aparece

- Junto al resumen comparado, en la misma fila de apoyo.

### Qué muestra

- Texto base `Comparación TCP vs UDP`
- Tabla comparativa con:
  - conexión previa
  - ACK
  - retransmisión
  - orden garantizado
  - sobrecarga
  - casos de uso

### Objetivo

- Tener teoría visible cerca de la demo, no escondida como texto aislado.

---

## 6.5 Logs comparados

### Componente

- `ComparisonLogPanel`

### Dónde aparece

- En la parte inferior del modo comparación.

### Qué muestra

- `Log TCP`
- `Log UDP`

Ambos se muestran en paralelo.

### Objetivo

- Poder leer eventos de ambos protocolos al mismo tiempo sin mezclarlos.

---

## 7. Relación entre controles y pantalla

## En modo simple

Los controles afectan a:

- protocolo mostrado
- mensaje enviado
- red simulada
- velocidad de reproducción
- revisión por pasos

La visualización resultante aparece en:

- red central
- buzones
- estados
- log
- paneles de mensaje
- detalle modal del paquete

## En modo comparación

Los controles compartidos afectan a ambos protocolos a la vez:

- mismo mensaje
- misma pérdida
- misma latencia
- mismo jitter
- misma duplicación
- misma reordenación
- mismo bandwidth
- misma fragmentación
- misma velocidad visual

La visualización resultante aparece en:

- panel TCP izquierdo
- panel UDP derecho
- resumen comparado
- teoría comparativa
- logs paralelos

---

## 8. Componentes UI principales del proyecto

Estos son los componentes más importantes que estructuran la interfaz:

- `SimulatorHeader`
- `ControlPanel`
- `NetworkCanvas`
- `PacketNode`
- `MailboxPanel`
- `StatePanel`
- `EventLogPanel`
- `MessageSummaryPanel`
- `ComparisonModeView`
- `ComparisonProtocolPane`
- `ComparisonSummaryPanel`
- `TheoryComparisonPanel`
- `ComparisonLogPanel`
- `DashboardCard`

---

## 9. Resumen rápido de ubicación

### Parte superior

- Header
- Controles

### Centro en modo simple

- buzón cliente
- red central
- buzón servidor
- barra lateral derecha

### Derecha en modo simple

- botón teoría
- estados
- leyenda
- log
- mensajes y reconstrucción

### Centro en modo comparación

- panel TCP izquierda
- panel UDP derecha

### Parte media-baja en modo comparación

- resumen final comparado
- teoría comparativa

### Parte baja en modo comparación

- logs TCP y UDP en paralelo

### Ventanas auxiliares

- detalle de paquete
- teoría y ayuda

---

## 10. Intención didáctica de la UI

La interfaz está planteada para que el usuario pueda:

- configurar la red sin tocar lógica interna
- ver visualmente el tránsito de paquetes
- entender diferencias entre TCP y UDP
- leer estados y eventos sin perder el foco visual
- abrir detalle o teoría solo cuando lo necesite
- comparar ambos protocolos en una sola demostración

En otras palabras, la UI está organizada para enseñar, no solo para “mostrar cosas”.
