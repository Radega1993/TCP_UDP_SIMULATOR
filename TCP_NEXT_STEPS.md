# Proximos pasos para mejorar el simulador TCP

Este documento recoge mejoras recomendadas para evolucionar el modo TCP del simulador. La idea no es convertirlo de golpe en una implementacion TCP real completa, sino avanzar de forma didactica, mantenible y verificable.

## Objetivo general

El simulador TCP ya cubre los conceptos principales:

- handshake
- fragmentacion
- ventana deslizante
- ACK acumulativos
- buffer de recepcion
- retransmision por timeout
- duplicate ACK
- congestion simplificada
- cierre de conexion

Los siguientes pasos buscan hacerlo mas completo, mas realista y mas util para clase.

## Prioridad 1: Fast retransmit end-to-end

### Problema actual

La logica de `TcpCongestionControl` soporta `FAST_RETRANSMIT`, pero en el motor end-to-end actual no siempre se alcanza el caso de 3 duplicate ACK antes del timeout.

### Objetivo

Crear escenarios donde:

1. Se pierde un segmento intermedio.
2. Llegan varios segmentos posteriores.
3. El receptor emite ACK duplicados del byte faltante.
4. El cliente alcanza 3 duplicate ACK.
5. Se retransmite el segmento perdido sin esperar timeout.

### Tareas

- Ajustar el pipeline de envio para permitir mas segmentos en vuelo cuando la ventana efectiva lo permita.
- Revisar la interaccion entre `cwnd`, ventana de envio y ventana anunciada.
- Crear un escenario dedicado: `tcp-fast-retransmit.json`.
- Activar los tests `@Disabled` relacionados con fast retransmit.
- Mostrar en UI una etiqueta clara: `Fast retransmit`.

## Prioridad 2: RTO y RTT didacticos

### Problema actual

El timeout TCP usa un valor fijo simplificado.

### Objetivo

Mostrar como TCP estima el tiempo de retransmision a partir del RTT observado.

### Tareas

- Medir RTT de segmentos confirmados.
- Calcular un RTO simplificado.
- Mostrar valores en un panel:
  - RTT medido
  - RTT estimado
  - RTO actual
- Permitir modo simple:
  - timeout fijo
  - timeout adaptativo
- Crear tests para:
  - RTO aumenta con jitter alto
  - RTO no baja por debajo de un minimo
  - timeout usa el RTO calculado

## Prioridad 3: Handshake mas realista

### Problema actual

El handshake siempre se completa sin perdida.

### Objetivo

Permitir explicar que SYN, SYN-ACK y ACK tambien pueden perderse.

### Tareas

- Aplicar condiciones de red al handshake.
- Permitir perdida de SYN.
- Permitir perdida de SYN-ACK.
- Permitir retransmision de SYN.
- Permitir retransmision de SYN-ACK.
- Mostrar estados:
  - `CLOSED`
  - `LISTEN`
  - `SYN_SENT`
  - `SYN_RECEIVED`
  - `ESTABLISHED`
- Crear tests para:
  - perdida de SYN
  - perdida de SYN-ACK
  - retransmision de handshake
  - no enviar DATA antes de `ESTABLISHED`

## Prioridad 4: Cierre TCP mas completo

### Problema actual

El cierre se representa de forma didactica, pero faltan estados y temporizadores reales.

### Objetivo

Representar mejor el cierre TCP y sus estados.

### Tareas

- Anadir estados:
  - `FIN_WAIT_2`
  - `CLOSING`
  - `TIME_WAIT`
  - `CLOSE_WAIT`
  - `LAST_ACK`
- Simular perdida de FIN.
- Simular perdida de ACK final.
- Anadir explicacion de `TIME_WAIT`.
- Crear tests para:
  - no cerrar con bytes en vuelo
  - perdida de FIN
  - retransmision de FIN
  - transicion completa de estados

## Prioridad 5: Fast recovery

### Problema actual

El simulador diferencia timeout y duplicate ACK, pero no modela completamente fast recovery.

### Objetivo

Explicar la diferencia entre:

- timeout: caida fuerte de `cwnd`
- fast retransmit / fast recovery: recuperacion menos agresiva

### Tareas

- Mantener fase `FAST_RETRANSMIT` o anadir `FAST_RECOVERY`.
- Inflar temporalmente `cwnd` con ACK duplicados.
- Salir de fast recovery cuando llega ACK nuevo.
- Mostrar en grafica:
  - caida de `ssthresh`
  - ajuste de `cwnd`
  - recuperacion posterior
- Crear tests de transicion de fases.

## Prioridad 6: SACK como modo avanzado

### Objetivo

Anadir Selective Acknowledgement como modo opcional avanzado para explicar por que ACK acumulativo no siempre es suficiente.

### Tareas

- Permitir activar/desactivar SACK.
- Representar bloques recibidos fuera de orden.
- Mostrar que el emisor sabe que segmentos posteriores llegaron.
- Comparar:
  - TCP con ACK acumulativo
  - TCP con SACK
- Crear escenario `tcp-sack-reordering.json`.

## Prioridad 7: Zero window y control de flujo avanzado

### Objetivo

Explicar que ocurre cuando el receptor se queda sin buffer.

### Tareas

- Permitir ventana anunciada `WIN=0`.
- Frenar envio cuando la ventana sea 0.
- Implementar zero-window probe simplificado.
- Mostrar cuando el receptor libera buffer.
- Crear tests para:
  - no enviar DATA con `WIN=0`
  - reanudar envio al aumentar ventana
  - no perder datos por overflow

## Prioridad 8: Segmento TCP mas didactico

### Objetivo

Hacer mas clara la estructura del segmento TCP.

### Tareas

- Mostrar campos:
  - puerto origen
  - puerto destino
  - seq
  - ack
  - flags
  - window
  - checksum simplificado
  - payload
- Anadir inspector de cabecera TCP.
- Relacionar cada campo con lo que ocurre en la simulacion.

## Prioridad 9: Checksums y corrupcion

### Objetivo

Explicar que no todo fallo es perdida: tambien puede haber corrupcion.

### Tareas

- Anadir probabilidad de corrupcion.
- Marcar segmentos corruptos.
- Descartar segmentos corruptos en receptor.
- Forzar retransmision si no se confirma.
- Mostrar checksum correcto/incorrecto.

## Prioridad 10: Variantes de congestion

### Objetivo

Comparar distintas estrategias de congestion.

### Modos posibles

- TCP Tahoe
- TCP Reno
- TCP NewReno
- CUBIC simplificado

### Tareas

- Crear interfaz `CongestionControlStrategy`.
- Extraer la logica actual a una estrategia.
- Permitir elegir estrategia desde configuracion.
- Comparar curvas de `cwnd`.
- Crear tests por estrategia.

## Prioridad 11: Escenarios docentes nuevos

### Escenarios recomendados

- `tcp-handshake-loss`
- `tcp-fast-retransmit`
- `tcp-timeout-vs-fast-retransmit`
- `tcp-small-receiver-buffer`
- `tcp-zero-window`
- `tcp-reordering-without-sack`
- `tcp-reordering-with-sack`
- `tcp-high-jitter-rto`
- `tcp-congestion-reno`
- `tcp-connection-close-loss`

## Prioridad 12: Mejoras visuales

### Ideas

- Marcar visualmente bytes en vuelo.
- Dibujar la ventana deslizante sobre la secuencia de bytes.
- Mostrar ACK acumulativo como una barra que avanza.
- Resaltar huecos cuando faltan segmentos.
- Diferenciar retransmision por timeout y fast retransmit con colores distintos.
- Mostrar `cwnd`, `rwnd` y ventana efectiva juntas:
  - `sendWindow`
  - `receiverWindow`
  - `cwnd`
  - `effectiveWindow`

## Prioridad 13: Tests y calidad

### Tareas

- Mantener tests sin JavaFX.
- Anadir fixtures de escenarios reales.
- Activar tests deshabilitados cuando se implemente fast retransmit end-to-end.
- Crear tests de regresion para cada escenario docente.
- Validar invariantes:
  - bytes en vuelo nunca negativos
  - ACK nunca retrocede
  - `cwnd` nunca menor que MSS
  - no DATA antes de `ESTABLISHED`
  - no DATA despues de cierre
  - mensaje final no duplica payload

## Orden recomendado de implementacion

1. Fast retransmit end-to-end.
2. RTO/RTT didactico.
3. Handshake con perdidas.
4. Cierre TCP mas completo.
5. Fast recovery.
6. Zero window.
7. SACK.
8. Inspector de segmento TCP.
9. Checksums y corrupcion.
10. Variantes de congestion.

## Criterio de exito

El simulador no tiene que implementar TCP real al 100 %. Debe cumplir tres objetivos:

1. Ser fiel en los conceptos principales.
2. Ser claro para explicar en clase.
3. Ser robusto con tests de dominio sin depender de JavaFX.

