# AulaRed 2.0.0

## Resumen

AulaRed 2.0.0 convierte el proyecto en un laboratorio visual de redes más amplio que el simulador TCP/UDP original. Esta versión incorpora identidad nueva, módulos IP avanzados, IPv6 light y un módulo completo de subnetting.

## Novedades principales

- Nueva identidad del proyecto: **AulaRed**.
- Home actualizada como laboratorio educativo de redes.
- Módulo IPv4 ampliado:
  - Fundamentos IPv4 visuales.
  - Routing básico con gateway.
  - TTL y descarte.
  - ICMP y ping.
  - Inspector de cabecera IP.
  - Tabla de rutas con longest prefix match simplificado.
  - Fragmentación IP y MTU.
  - ARP request, ARP reply y caché ARP.
- Nuevo módulo IPv6 light:
  - Representación visual IPv6.
  - Comparativa IPv4 vs IPv6.
  - Unicast y multicast.
- Nuevo módulo Subnetting:
  - Fundamentos visuales.
  - Calculadora completa.
  - Tabla de subredes con network, first host, last host y broadcast.
  - Vista binaria con bits de red y host.
  - Práctica aplicada con escenarios reales y drag and drop.
  - Introducción a VLSM.
- README actualizado con el estado real del proyecto.

## Artefactos

- Linux `.deb` actualizable: `aula-red_2.0.0-1_amd64.deb`
- Linux app-image comprimida: `AulaRed-2.0.0-linux-x64-app-image.tar.gz`
- JAR de aplicación: `aula-red-2.0.0.jar`
- Checksums: `SHA256SUMS.txt`

## Nota sobre Windows

El instalador `.exe` debe generarse en Windows con:

```powershell
.\scripts\package-windows.ps1 2.0.0
```

`jpackage` no permite generar instaladores Windows desde Linux; en este entorno solo están disponibles `app-image`, `deb` y `rpm`.

## Verificación

- Tests Maven: `113 tests`, `0 failures`, `0 errors`, `2 skipped`.
- Empaquetado Linux generado con `jpackage`.
