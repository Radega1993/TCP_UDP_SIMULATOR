# TCP en el simulador

Este documento explica que representa el modo TCP del simulador y que parametros se pueden configurar desde la interfaz.

## Que hace TCP

TCP, Transmission Control Protocol, es un protocolo de transporte orientado a conexion. En el simulador se usa para mostrar como una comunicacion fiable necesita preparar la conexion, enviar datos con numeros de secuencia, confirmar lo recibido y recuperar perdidas mediante retransmisiones.

En una simulacion TCP, el cliente intenta entregar un mensaje al servidor de forma ordenada y completa. Para conseguirlo, el simulador representa estos pasos:

1. Handshake de tres pasos.
   El cliente y el servidor pasan por la apertura de conexion usando SYN, SYN-ACK y ACK hasta quedar en estado `ESTABLISHED`.

2. Fragmentacion del mensaje.
   El texto configurado se divide en segmentos segun el tamano de fragmento elegido.

3. Envio con ventana deslizante.
   TCP no envia datos ilimitadamente: respeta una ventana de envio, una ventana efectiva y el estado del receptor.

4. Confirmaciones acumulativas.
   El servidor responde con ACK indicando el siguiente byte esperado. Si llegan segmentos en orden, el ACK avanza.

5. Buffer de recepcion.
   El receptor solo acepta datos si tiene espacio disponible. La ventana anunciada depende de ese buffer.

6. Perdidas y retransmisiones.
   Si un segmento se pierde, TCP puede retransmitirlo por timeout o por fast retransmit cuando detecta ACK duplicados.

7. Control de congestion.
   El simulador muestra la evolucion de `cwnd`, `ssthresh` y la fase de congestion, por ejemplo slow start o congestion avoidance.

8. Cierre de conexion.
   Al finalizar la entrega, TCP cierra la conexion con FIN y ACK hasta volver a `CLOSED`.

## Que se observa en pantalla

El modo TCP permite ver:

- Estados del cliente y del servidor.
- Segmentos TCP en movimiento.
- SYN, SYN-ACK, ACK, DATA, retransmisiones y FIN.
- Numeros de secuencia y ACK.
- Ventana de envio, bytes en vuelo y bytes confirmados.
- Buffer del receptor y ventana anunciada.
- Evolucion de congestion: `cwnd`, `ssthresh`, fase y eventos de perdida.
- Log educativo paso a paso.
- Diagrama temporal o escena visual, segun el modo de visualizacion.

## Parametros configurables

### Escenario y protocolo

| Parametro | Tipo | Valores / rango | Valor inicial | Que cambia |
|---|---:|---:|---:|---|
| Escenario | Selector | Personalizado o escenarios cargados | Personalizado | Carga una situacion preparada para clase y ajusta protocolo, mensaje, fragmento, red, ventana y buffer. |
| Protocolo | Selector | TCP / UDP | TCP | Elige el protocolo de transporte. En modo TCP activa conexion, ACK, retransmision, ventana y cierre. |
| Mensaje | Texto | Texto libre | `HOLAALUMNOS` | Es el contenido que el cliente intenta enviar al servidor. Si es mas largo, genera mas segmentos. |

### Condiciones de red

| Parametro | Tipo | Valores / rango | Valor inicial | Que cambia |
|---|---:|---:|---:|---|
| Perdida (%) | Slider | 0-100 % | 20 % | Probabilidad de que un paquete se pierda. En TCP provoca retransmisiones, timeout, duplicate ACK o fast retransmit. |
| Latencia | Slider | 80-2500 ms | 1200 ms | Tiempo base de transito de cada paquete. Aumentarla hace que la simulacion tarde mas en mostrar entregas y ACK. |
| Jitter | Slider | 0-800 ms | 0 ms | Variacion aleatoria sobre la latencia base. Hace que los paquetes lleguen con ritmo menos regular. |
| Duplicacion | Slider | 0-100 % | 0 % | Probabilidad de que aparezcan copias de paquetes. Sirve para explicar duplicados en la red. |
| Reordenacion | Slider | 0-100 % | 0 % | Permite que algunos paquetes lleguen fuera de orden. TCP puede recomponer el flujo mediante secuencias y ACK acumulativos. |
| Bandwidth | Spinner | 0-50 paquetes/s | 0 | Limite simplificado de paquetes por segundo. Con valores bajos, la red separa mas los envios y simula cola. `0` equivale a sin limite simplificado. |

### Parametros TCP

| Parametro | Tipo | Valores / rango | Valor inicial | Que cambia |
|---|---:|---:|---:|---|
| Tamano de ventana | Spinner | 1-128 bytes | 24 bytes | Maximo de datos que pueden estar enviados pero aun no confirmados. Si sube, TCP puede mantener mas bytes en vuelo. |
| Buffer de recepcion | Spinner | 1-128 bytes | 24 bytes | Espacio disponible en el receptor. Si baja, el servidor anuncia menos ventana y el cliente debe frenar. |
| Fragmento | Spinner | 1-16 caracteres/bytes | 8 | Tamano maximo de cada segmento. Si baja, el mensaje se divide en mas segmentos y se ven mas ACK y eventos. |

### Visualizacion

| Parametro | Tipo | Valores / rango | Valor inicial | Que cambia |
|---|---:|---:|---:|---|
| Velocidad | Slider | 0.5x-3.0x | 1.0x | Controla la rapidez visual de la reproduccion. No cambia la logica del protocolo. |
| Vista | Toggle | Diagrama / Escena | Diagrama | Cambia la forma de visualizar la simulacion activa. |

## Parametros internos relevantes

Estos valores no se configuran directamente desde el panel, pero afectan a como se interpreta TCP dentro del simulador:

| Parametro interno | Funcion |
|---|---|
| `SEQ` | Numero de secuencia del segmento. Indica la posicion del payload dentro del flujo TCP. |
| `ACK` | Siguiente byte esperado por el receptor. El simulador usa ACK acumulativos. |
| `WIN` | Ventana anunciada por el receptor. Depende del buffer disponible. |
| `cwnd` | Ventana de congestion. Limita cuantos bytes pueden enviarse desde la perspectiva de congestion. |
| `ssthresh` | Umbral entre slow start y congestion avoidance. Cambia tras perdidas o timeout. |
| Duplicate ACK | ACK repetido que indica que el receptor sigue esperando el mismo byte. Puede activar fast retransmit. |
| Timeout | Evento que ocurre cuando un segmento perdido no se confirma a tiempo. Provoca retransmision y reduce congestion. |

## Como interpretar algunos ajustes

### Para explicar fiabilidad

Usa TCP con una perdida entre 30 % y 40 %. El mensaje deberia terminar llegando completo porque TCP detecta la perdida y retransmite.

### Para explicar ventana deslizante

Reduce el tamano de ventana y usa un fragmento pequeno. Se vera que el emisor no puede enviar todo de golpe y debe esperar ACK.

### Para explicar buffer de recepcion

Reduce el buffer de recepcion. El receptor anunciara menos ventana disponible y el emisor tendra menos margen para enviar datos nuevos.

### Para explicar congestion

Usa escenarios de congestion o aumenta la perdida. Observa como cambian `cwnd`, `ssthresh` y la fase de congestion tras ACK, duplicate ACK o timeout.

### Para comparar TCP con UDP

Ejecuta el mismo mensaje y la misma perdida en el modo comparacion TCP vs UDP. TCP intentara recuperar la informacion perdida; UDP mostrara mejor la simplicidad y la falta de recuperacion nativa.

