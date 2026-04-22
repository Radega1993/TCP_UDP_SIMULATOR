$ErrorActionPreference = "Stop"

$Root = (Resolve-Path "$PSScriptRoot\..").Path
$DistDir = Join-Path $Root "target\dist"
$ArtifactsDir = Join-Path $Root "artifacts\windows"
$JAR = "tcp-udp-simulator-java-1.0.0.jar"
$MainClass = "com.example.simulator.Main"
$IconIco = Join-Path $Root "icono.ico"

New-Item -ItemType Directory -Force -Path $DistDir | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $ArtifactsDir "exe") | Out-Null

Write-Host "==> Compilando proyecto y copiando dependencias..."
Set-Location $Root
mvn clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$DistDir"
Copy-Item (Join-Path $Root "target\$JAR") $DistDir -Force

Write-Host "==> Generando instalador EXE..."
jpackage `
  --type exe `
  --name TCP-UDP-Simulator `
  --input $DistDir `
  --main-jar $JAR `
  --main-class $MainClass `
  --icon $IconIco `
  --win-shortcut `
  --win-menu `
  --dest (Join-Path $ArtifactsDir "exe")

Write-Host "Listo. Artefactos en $ArtifactsDir\exe"
