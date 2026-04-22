# Simulador TCP/UDP con JavaFX

Aplicación educativa para visualizar TCP y UDP de forma didáctica: handshake, fragmentación, pérdidas, retransmisión, ACK, estados de cliente/servidor y cierre de conexión.

## Características

- **TCP completo para clase**
  - 3-way handshake (`SYN -> SYN-ACK -> ACK`)
  - Fragmentación por tamaño configurable (1, 2, 3... caracteres por segmento)
  - ACK por segmento
  - Pérdida y retransmisión por timeout
  - Cierre de conexión (`FIN -> ACK -> FIN -> ACK`)
- **UDP visual**
  - Datagramas con numeración
  - Pérdida sin retransmisión
  - Reconstrucción parcial del mensaje
- **Interfaz docente**
  - Paquetes tipo tarjeta con color por protocolo/estado
  - Buzón visual de paquetes para cliente y servidor (no desaparecen)
  - Panel de detalle del paquete seleccionado
  - Log de eventos con prefijos (`[SEND]`, `[ACK]`, `[LOST]`, `[RETRY]`, `[STATE]`)
  - Control de tamaño de ventana (manual + presets)

## Requisitos

- **JDK 17+** (no solo JRE)
- **Maven 3.9+**
- Para generar instaladores: `jpackage` (viene con JDK moderno)

Verifica:

```bash
java -version
mvn -version
jpackage --version
```

## Ejecutar en desarrollo

```bash
mvn clean javafx:run
```

## Icono de la app

- El proyecto incluye **ambos formatos**:
  - `icono.png` (ejecución JavaFX y empaquetado Linux)
  - `icono.ico` (instalador Windows)
- Si quieres regenerar el `.ico` desde el `.png`:

```bash
convert icono.png -define icon:auto-resize=256,128,64,48,32,16 icono.ico
```

## Controles de la interfaz

- `Protocolo`: TCP o UDP
- `Mensaje`: texto a enviar
- `Pérdida (%)`: probabilidad de pérdida simulada
- `Velocidad`: factor de velocidad de animación
- `Fragmento (1,2,3...)`: tamaño del segmento TCP / datagrama UDP
- `Ancho` y `Alto ventana`: tamaño manual
- `Preset ventana`: resoluciones rápidas

## Crear aplicación instalable

### 1) Compilar y preparar artefactos

Desde la raíz del proyecto:

```bash
mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dist
cp target/tcp-udp-simulator-java-1.0.0.jar target/dist/
```

> En Windows, reemplaza `cp` por `copy`.

---

## Linux (AppImage/DEB)

### Opción A: App Image (portable)

```bash
jpackage \
  --type app-image \
  --name TCP-UDP-Simulator \
  --input target/dist \
  --main-jar tcp-udp-simulator-java-1.0.0.jar \
  --main-class com.example.simulator.Main \
  --icon icono.png
```

Ejecutable resultante:

```bash
./TCP-UDP-Simulator/bin/TCP-UDP-Simulator
```

### Opción B: Paquete `.deb`

```bash
jpackage \
  --type deb \
  --name tcp-udp-simulator \
  --input target/dist \
  --main-jar tcp-udp-simulator-java-1.0.0.jar \
  --main-class com.example.simulator.Main \
  --icon icono.png
```

Instalación:

```bash
sudo dpkg -i tcp-udp-simulator_*_amd64.deb
```

---

## Windows (EXE/MSI)

### 1) Generar instalador EXE (usa `icono.ico`)

```powershell
jpackage `
  --type exe `
  --name TCP-UDP-Simulator `
  --input target/dist `
  --main-jar tcp-udp-simulator-java-1.0.0.jar `
  --main-class com.example.simulator.Main `
  --icon icono.ico `
  --win-shortcut `
  --win-menu
```

También puedes usar `--type msi` si prefieres MSI.

## Solución de problemas rápida

- **No abre JavaFX**: confirma JDK 17+ y vuelve a ejecutar `mvn clean javafx:run`.
- **No encuentra `jpackage`**: instala/usa JDK completo y agrega `bin` al `PATH`.
- **Instalador sin icono en Windows**: usa `.ico` en lugar de `.png`.
- **Dependencias faltantes al empaquetar**: repite el paso de `dependency:copy-dependencies`.

## Uso recomendado en clase

1. TCP con fragmento `1` o `2` para mostrar secuencia exacta de segmentos.
2. Sube pérdida (30-40%) para observar retransmisiones y ACK.
3. Repite con UDP para contrastar ausencia de recuperación.
4. Enseña estado de conexión: `SYN_*`, `ESTABLISHED`, cierre con `FIN/ACK`.
