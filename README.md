# AulaRed

**AulaRed** es un laboratorio visual de redes para clase. Nació como una herramienta para explicar TCP y UDP, pero el proyecto ya cubre una ruta didáctica mucho más amplia: modelos de capas, IPv4, routing, ICMP, ARP, fragmentación, IPv6 y subnetting.

El proyecto está desarrollado con **Java 17**, **JavaFX** y **Maven**. Es open source y está creado por **Raül de Arriba**.

## Estado Actual

AulaRed funciona como una aplicación de escritorio modular para aprender redes paso a paso:

- TCP y UDP con visualización de paquetes.
- Comparador TCP vs UDP usando las mismas condiciones de red.
- Modelo TCP/IP y modelo OSI con encapsulación.
- Módulo IPv4 y subredes con routing básico.
- ICMP y ping.
- Inspector de cabecera IP.
- Tabla de rutas con longest prefix match simplificado.
- Fragmentación IPv4 y MTU.
- ARP request, ARP reply y caché ARP.
- IPv6 light con comparativa IPv4 vs IPv6.
- Módulo Subnetting con calculadora, binario y práctica aplicada.

## Módulos

### TCP

- Handshake de tres pasos: `SYN`, `SYN-ACK`, `ACK`.
- Estados de conexión del cliente y servidor.
- Fragmentación del mensaje por tamaño configurable.
- ACK por segmento.
- Pérdida de paquetes.
- Retransmisión por timeout.
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
- Usa el mismo mensaje y las mismas condiciones de red.
- Muestra diferencias en entrega, pérdidas, retransmisiones y eventos.
- Incluye vista temporal, vista de paquetes y resumen de resultados.

### Modelos TCP/IP y OSI

- Comparación visual entre modelos.
- Capas con colores, iconos y descripción.
- Equivalencias entre capas.
- Tabla de PDU.
- Flujo de encapsulación con TCP o UDP.

### IPv4 y Subredes

- Cálculo de red origen y destino.
- Broadcast y rango de hosts.
- Detección de misma red.
- Decisión de si necesita router.
- Routing básico con gateway.
- TTL y descarte por expiración.
- ICMP/ping con Echo Request, Echo Reply, Destination Unreachable y TTL Exceeded.
- Inspector de cabecera IP.
- Tabla de rutas con selección de ruta.
- Fragmentación IP por MTU.
- ARP request/reply y caché ARP.

### IPv6 Light

- Representación visual de direcciones IPv6.
- Comparativa IPv4 vs IPv6.
- Tipos básicos: unicast y multicast.

### Subnetting

- Fundamentos visuales: una red grande dividida en redes pequeñas.
- Calculadora por número de subredes o por hosts necesarios.
- Tabla con `Subred`, `Network`, `First Host`, `Last Host` y `Broadcast`.
- Vista binaria con bits de red y bits de host.
- Explicación visual del AND lógico.
- Práctica aplicada con escenarios reales:
  - Oficina con departamentos.
  - Empresa con redes separadas.
- Drag and drop de bloques de red hacia departamentos.
- Feedback correcto/incorrecto.
- Introducción a VLSM.

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

## Ejecutar en Desarrollo

```bash
git clone <URL_DEL_REPOSITORIO>
cd network_simulator
mvn clean javafx:run
```

Ejecutar tests:

```bash
mvn test
```

## Instalación Para Usuarios

### Linux Usando `.deb`

```bash
sudo apt install ./aula-red_<version>-1_amd64.deb
```

### Linux Usando App Image

Descarga la carpeta `AulaRed` generada como app-image y ejecuta:

```bash
./AulaRed/bin/AulaRed
```

### Windows Usando `.exe`

Descarga el instalador `.exe` de la versión deseada y ejecútalo.

## Crear Instaladores

La versión principal se toma de `<version>` en `pom.xml`.

Para preparar una nueva versión:

```bash
mvn versions:set -DnewVersion=1.0.1
```

### Linux

```bash
./scripts/package-linux.sh
```

Salida:

- `artifacts/linux/<version>/deb/`
- `artifacts/linux/<version>/app-image/`

También puedes indicar una versión concreta:

```bash
./scripts/package-linux.sh 1.0.1
```

### Windows

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

## Estructura Del Proyecto

```text
src/main/java/com/example/simulator/
  app/                  Arranque e integración de la aplicación JavaFX
  application/          Casos de uso y servicios de aplicación
  domain/               Modelo de dominio y lógica de red
  infrastructure/       Repositorios y carga de contenido JSON
  presentation/         ViewModels y adaptadores para UI
  ui/                   Componentes JavaFX

src/main/resources/
  content/              Teoría y escenarios
  icons/                Iconos usados por la interfaz

html/
  comparador/           Boceto HTML/CSS del comparador TCP vs UDP
  modelos/              Boceto HTML/CSS de modelos TCP/IP y OSI
  ip_sprints_1_2_3_pixel/ Bocetos visuales de módulos IP

scripts/
  package-linux.sh      Empaquetado Linux
  package-windows.ps1   Empaquetado Windows
```

## Versionado

El proyecto usa versionado semántico:

- `MAJOR`: cambios incompatibles o rediseños grandes.
- `MINOR`: nuevas funcionalidades.
- `PATCH`: correcciones y mejoras pequeñas.

Para publicar una versión:

```bash
git tag v1.0.1
git push origin v1.0.1
```

Después puedes subir los artefactos generados a GitHub Releases.

## Colaborar

Las contribuciones son bienvenidas: bugs, mejoras de interfaz, nuevos escenarios, ampliación de teoría o tests.

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

5. Abre un pull request explicando qué cambia, por qué se cambia y cómo se ha probado.

## Buenas Prácticas

- Mantén los cambios acotados.
- No mezcles refactors grandes con cambios funcionales.
- Añade o actualiza tests cuando cambie lógica de red.
- Si modificas UI, intenta respetar los bocetos de `html/`.
- No subas instaladores generados al repositorio; usa Releases para publicarlos.
