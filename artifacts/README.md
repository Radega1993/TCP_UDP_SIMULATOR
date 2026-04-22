# Artefactos de distribución

Esta carpeta organiza los instaladores/salidas por sistema:

- `linux/app-image/` -> salida de `jpackage --type app-image`
- `linux/deb/` -> salida de `jpackage --type deb`
- `windows/exe/` -> salida de `jpackage --type exe`

Los scripts oficiales para generar artefactos están en:

- `scripts/package-linux.sh`
- `scripts/package-windows.ps1`

> Recomendación: no versionar binarios grandes en git. Publicarlos en GitHub Releases.
