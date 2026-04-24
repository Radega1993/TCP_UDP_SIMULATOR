$ErrorActionPreference = "Stop"

$Root = (Resolve-Path "$PSScriptRoot\..").Path
$DistDir = Join-Path $Root "target\dist"
$Version = if ($args.Count -gt 0) { $args[0] } else { "" }
$AppName = "TCP-UDP-Simulator"
$MainClass = "com.example.simulator.Main"
$IconIco = Join-Path $Root "icono.ico"
$UpgradeUuid = "9f08fd3f-6c59-4dd5-9c47-4df9853523b6"

Set-Location $Root
$ProjectVersion = mvn help:evaluate "-Dexpression=project.version" -q "-DforceStdout"
if ([string]::IsNullOrWhiteSpace($Version)) {
  $Version = $ProjectVersion
}
$ArtifactId = mvn help:evaluate "-Dexpression=project.artifactId" -q "-DforceStdout"
$JAR = "$ArtifactId-$ProjectVersion.jar"
$ArtifactsDir = Join-Path $Root "artifacts\windows\$Version"
$ExeDir = Join-Path $ArtifactsDir "exe"

if (Test-Path $DistDir) { Remove-Item -Recurse -Force $DistDir }
if (Test-Path $ExeDir) { Remove-Item -Recurse -Force $ExeDir }
New-Item -ItemType Directory -Force -Path $DistDir | Out-Null
New-Item -ItemType Directory -Force -Path $ExeDir | Out-Null

Write-Host "==> Compilando proyecto y copiando dependencias..."
mvn clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$DistDir"
Copy-Item (Join-Path $Root "target\$JAR") $DistDir -Force

Write-Host "==> Generando instalador EXE $Version..."
jpackage `
  --type exe `
  --name $AppName `
  --app-version $Version `
  --input $DistDir `
  --main-jar $JAR `
  --main-class $MainClass `
  --icon $IconIco `
  --win-shortcut `
  --win-menu `
  --win-upgrade-uuid $UpgradeUuid `
  --dest $ExeDir

Write-Host "Listo. Artefactos en $ExeDir"
Write-Host ""
Write-Host "Para actualizar una instalación existente, ejecuta el EXE de una version mayor."
Write-Host "No cambies --win-upgrade-uuid entre versiones: es lo que permite que Windows detecte la actualización."
