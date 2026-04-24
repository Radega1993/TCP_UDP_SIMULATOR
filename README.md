# Simulador visual de TCP y UDP

Aplicación educativa de escritorio para aprender y enseñar cómo funcionan TCP, UDP, la comparación entre ambos protocolos y los modelos de capas TCP/IP y OSI.

El proyecto está desarrollado con Java 17, JavaFX y Maven. Es un proyecto open source creado por **Raül de Arriba**.

## Qué incluye

- Simulación visual de TCP.
- Simulación visual de UDP.
- Comparador TCP vs UDP usando el mismo mensaje y las mismas condiciones de red.
- Vista de diagrama temporal para seguir el intercambio de paquetes.
- Vista de paquetes para observar cliente, red y servidor.
- Modelo TCP/IP y modelo OSI con equivalencias visuales.
- Vista de encapsulación por capas.
- Paneles de teoría y ayuda contextual.
- Logs de eventos por protocolo.
- Revisión paso a paso de simulaciones.
- Empaquetado para Linux y Windows.

## Funcionalidades actuales

### TCP

- Handshake de tres pasos: `SYN`, `SYN-ACK`, `ACK`.
- Estados de conexión del cliente y servidor.
- Fragmentación del mensaje por tamaño configurable.
- ACK por segmento.
- Pérdida de paquetes.
- Retransmisión por timeout.
- Control visual de entrega.
- Cierre de conexión con `FIN` y `ACK`.
- Paneles de ventana deslizante, congestión, mensajes y eventos.

### UDP

- Envío de datagramas sin conexión.
- Datagramas numerados.
- Pérdida sin retransmisión.
- Reconstrucción parcial del mensaje recibido.
- Visualización clara de la diferencia entre rapidez/simplicidad y fiabilidad.

### Comparador TCP vs UDP

- Ejecuta TCP y UDP en paralelo.
- Usa el mismo mensaje para ambos protocolos.
- Aplica las mismas condiciones de red.
- Muestra diferencias en entrega, pérdidas, retransmisiones y eventos.
- Incluye vista temporal y vista de paquetes.
- Resume los resultados de ambos protocolos.

### Modelos TCP/IP y OSI

- Comparación visual entre el modelo TCP/IP y el modelo OSI.
- Capas con colores, iconos y descripción.
- Equivalencias entre capas.
- Tabla de PDU por modelo.
- Detalle por capa.
- Flujo de encapsulación con TCP o UDP.

## Requisitos

Para ejecutar desde código fuente:

- JDK 17 o superior.
- Maven 3.9 o superior.

Para generar instaladores:

- JDK completo con `jpackage`.
- En Linux, herramientas estándar de empaquetado `.deb`.
- En Windows, PowerShell y JDK con `jpackage`.

Comprueba tu entorno:

```bash
java -version
mvn -version
jpackage --version
```

## Instalación para usuarios

### Linux usando `.deb`

Descarga el paquete `.deb` de la versión que quieras instalar y ejecuta:

```bash
sudo apt install ./tcp-udp-simulator_<version>-1_amd64.deb
```

Si ya tienes una versión anterior instalada, instalar un `.deb` con una versión superior actualiza la aplicación.

### Linux usando app-image

Descarga la carpeta `TCP-UDP-Simulator` generada como app-image y ejecuta:

```bash
./TCP-UDP-Simulator/bin/TCP-UDP-Simulator
```

La app-image es portable. No actualiza una instalación anterior automáticamente; cada versión puede vivir en su propia carpeta.

### Windows usando `.exe`

Descarga el instalador `.exe` de la versión deseada y ejecútalo.

Si ya existe una versión anterior instalada, el instalador nuevo actualiza la aplicación siempre que mantenga el mismo identificador de actualización configurado en el script de empaquetado.

## Ejecutar en desarrollo

Clona el repositorio y entra en la carpeta del proyecto:

```bash
git clone <URL_DEL_REPOSITORIO>
cd network_simulator
```

Ejecuta la aplicación:

```bash
mvn clean javafx:run
```

Ejecuta los tests:

```bash
mvn test
```

## Crear instaladores

La versión principal se toma de `<version>` en `pom.xml`.

Para preparar una nueva versión:

```bash
mvn versions:set -DnewVersion=1.0.1
```

### Linux

Genera `.deb` y app-image:

```bash
./scripts/package-linux.sh
```

Salida:

- `artifacts/linux/<version>/deb/`
- `artifacts/linux/<version>/app-image/`

También puedes indicar una versión concreta para el instalador:

```bash
./scripts/package-linux.sh 1.0.1
```

### Windows

En PowerShell:

```powershell
.\scripts\package-windows.ps1
```

Salida:

- `artifacts/windows/<version>/exe/`

También puedes indicar una versión concreta:

```powershell
.\scripts\package-windows.ps1 1.0.1
```

Importante: no cambies el `--win-upgrade-uuid` del script entre versiones. Ese valor permite que Windows reconozca una versión nueva como actualización de la aplicación instalada.

## Versionado

El proyecto usa versionado semántico:

- `MAJOR`: cambios incompatibles o rediseños grandes.
- `MINOR`: nuevas funcionalidades.
- `PATCH`: correcciones y mejoras pequeñas.

Ejemplos:

- `1.0.0`: primera versión estable.
- `1.1.0`: nuevas vistas o funcionalidades.
- `1.1.1`: correcciones visuales o de bugs.

Para publicar una versión:

```bash
git tag v1.0.1
git push origin v1.0.1
```

Después puedes subir los artefactos generados a GitHub Releases.

## Estructura del proyecto

```text
src/main/java/com/example/simulator/
  app/                  Arranque e integración de la aplicación JavaFX
  application/          Casos de uso y servicios de aplicación
  domain/               Modelo de dominio y motor de simulación
  infrastructure/       Repositorios y carga de contenido JSON
  presentation/         ViewModels y adaptadores para UI
  ui/                   Componentes JavaFX

src/main/resources/
  content/              Teoría y escenarios
  icons/                Iconos usados por la interfaz

html/
  comparador/           Boceto HTML/CSS del comparador TCP vs UDP
  modelos/              Boceto HTML/CSS de modelos TCP/IP y OSI

scripts/
  package-linux.sh      Empaquetado Linux
  package-windows.ps1   Empaquetado Windows
```

## Colaborar

Las contribuciones son bienvenidas. Puedes colaborar corrigiendo bugs, mejorando la interfaz, añadiendo escenarios, ampliando teoría o escribiendo tests.

Flujo recomendado:

1. Haz un fork del repositorio.
2. Crea una rama descriptiva:

```bash
git checkout -b mejora/nueva-vista
```

3. Realiza los cambios.
4. Ejecuta los tests:

```bash
mvn test
```

5. Abre un pull request explicando:

- Qué cambia.
- Por qué se cambia.
- Cómo se ha probado.
- Capturas si el cambio afecta a la interfaz.

## Buenas prácticas para contribuir

- Mantén los cambios acotados.
- No mezcles refactors grandes con cambios funcionales.
- Añade o actualiza tests cuando cambie lógica de simulación.
- Si modificas UI, intenta respetar los bocetos de `html/`.
- No subas instaladores generados al repositorio; usa Releases para publicarlos.

## Uso recomendado en clase

1. Empieza con TCP sin pérdidas para explicar conexión, ACK y cierre.
2. Reduce el tamaño de fragmento para ver más segmentos.
3. Sube la pérdida al 30-40% para enseñar retransmisiones.
4. Repite el escenario con UDP.
5. Usa el comparador TCP vs UDP para discutir fiabilidad frente a simplicidad.
6. Cierra con el modelo TCP/IP y OSI para ubicar TCP, UDP, IP y Ethernet en sus capas.

## Autor

Creado por **Raül de Arriba**.

## Licencia

Proyecto open source. Añade un archivo `LICENSE` al repositorio para declarar formalmente la licencia de uso y distribución.
