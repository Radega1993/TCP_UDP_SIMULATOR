# AulaRed 2.1.0

Versión centrada en cerrar el módulo de Subnetting y pulir la navegación visual de los módulos docentes.

## Novedades

- Nuevo soporte VLSM con asignación automática por hosts requeridos.
- Integración de VLSM con routing entre subredes y TTL.
- Separación visual del módulo Subnetting en pestañas internas:
  - Subnetting
  - VLSM
- Nueva licencia open source MIT.
- Navbar superior unificado en Comparación TCP vs UDP.
- Navbar superior unificado en Modelos TCP/IP vs OSI.
- Eliminación de barras antiguas duplicadas en Comparación y Modelos.

## Subnetting

- Calculadora VLSM por lista de redes.
- Ordenación automática de redes grandes a pequeñas.
- Timeline de asignación.
- Tabla con red, primer host, último host y broadcast.
- Mapa de routing entre subredes.
- Saltos con TTL visible y descarte por expiración.

## Validación

- Tests ejecutados correctamente con Maven.
- 116 tests.
- 0 fallos.
- 0 errores.
- 2 omitidos.

## Artefactos

- Linux `.deb` actualizable: `aula-red_2.1.0-1_amd64.deb`
- Linux app-image comprimida: `AulaRed-2.1.0-linux-x64-app-image.tar.gz`
- JAR de aplicación: `aula-red-2.1.0.jar`
- Checksums: `SHA256SUMS.txt`

## Windows

El instalador `.exe` debe generarse desde Windows con:

```powershell
.\scripts\package-windows.ps1 2.1.0
```

El script mantiene el `--win-upgrade-uuid` para permitir actualizaciones sobre versiones anteriores.
