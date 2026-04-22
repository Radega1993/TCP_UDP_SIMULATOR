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
  - Navegación de simulación paso a paso (`Paso <-` / `Paso ->`)
  - Panel de detalle del paquete seleccionado
  - Log de eventos con prefijos (`[SEND]`, `[ACK]`, `[LOST]`, `[RETRY]`, `[STATE]`)
  - Control de tamaño de ventana (manual + presets)
  - Inicio por defecto maximizado (pantalla completa de ventana)

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

La app abre por defecto maximizada. Si quieres un tamaño concreto, usa los controles de `Ancho`, `Alto`, `Preset ventana` y `Aplicar tamaño`.

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
- Al terminar la simulación aparece el bloque `Revisión`:
  - `|<`: ir al primer paso
  - `Paso <-`: retroceder un paso
  - `Paso ->`: avanzar un paso
  - `>|`: ir al último paso

## Organización de artefactos

Estructura recomendada (ya creada):

- `artifacts/linux/app-image/`
- `artifacts/linux/deb/`
- `artifacts/windows/exe/`
- `scripts/package-linux.sh`
- `scripts/package-windows.ps1`

## Generar instaladores

### Linux (.deb y app-image)

```bash
./scripts/package-linux.sh
```

Salida:

- `artifacts/linux/app-image/`
- `artifacts/linux/deb/`

### Windows (.exe)

En PowerShell:

```powershell
.\scripts\package-windows.ps1
```

Salida:

- `artifacts/windows/exe/`

> Nota: el script usa `icono.ico` para Windows.

## Subir paquetes a GitHub (Release)

La forma más práctica para binarios de escritorio es **GitHub Releases** (no guardar `.deb/.exe` dentro del repo en git normal).

### Opción con `gh` CLI

1. Crea un tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

2. Publica release con artefactos:

```bash
gh release create v1.0.0 \
  artifacts/linux/deb/*.deb \
  artifacts/linux/app-image/** \
  artifacts/windows/exe/*.exe \
  --title "v1.0.0" \
  --notes "Primera versión pública del simulador TCP/UDP"
```

### Opción manual (web)

1. Ve a `GitHub -> Releases -> Draft a new release`
2. Selecciona el tag.
3. Arrastra los archivos de:
   - `artifacts/linux/deb/`
   - `artifacts/windows/exe/`
   - (opcional) carpeta/zip de `app-image`
4. Publica la release.

## GitHub Packages (Maven package)

Si también quieres publicar el JAR como paquete Maven en GitHub Packages:

1. Configura `distributionManagement` en `pom.xml` apuntando a `https://maven.pkg.github.com/<OWNER>/<REPO>`
2. Configura credenciales en `~/.m2/settings.xml` con un token de GitHub (`write:packages`)
3. Publica con:

```bash
mvn deploy
```

Para instaladores de escritorio (`.deb`, `.exe`), sigue usando Releases.

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
