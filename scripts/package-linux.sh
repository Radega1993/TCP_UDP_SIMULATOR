#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="${ROOT_DIR}/target/dist"
ARTIFACTS_DIR="${ROOT_DIR}/artifacts/linux"
APP_NAME="TCP-UDP-Simulator"
JAR_NAME="tcp-udp-simulator-java-1.0.0.jar"
MAIN_CLASS="com.example.simulator.Main"

mkdir -p "${DIST_DIR}" "${ARTIFACTS_DIR}/app-image" "${ARTIFACTS_DIR}/deb"

echo "==> Compilando proyecto y copiando dependencias..."
cd "${ROOT_DIR}"
mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="${DIST_DIR}"
cp "target/${JAR_NAME}" "${DIST_DIR}/"

echo "==> Generando app-image..."
jpackage \
  --type app-image \
  --name "${APP_NAME}" \
  --input "${DIST_DIR}" \
  --main-jar "${JAR_NAME}" \
  --main-class "${MAIN_CLASS}" \
  --icon "${ROOT_DIR}/icono.png" \
  --dest "${ARTIFACTS_DIR}/app-image"

echo "==> Generando paquete .deb..."
jpackage \
  --type deb \
  --name tcp-udp-simulator \
  --input "${DIST_DIR}" \
  --main-jar "${JAR_NAME}" \
  --main-class "${MAIN_CLASS}" \
  --icon "${ROOT_DIR}/icono.png" \
  --dest "${ARTIFACTS_DIR}/deb"

echo "Listo. Artefactos en:"
echo "  - ${ARTIFACTS_DIR}/app-image"
echo "  - ${ARTIFACTS_DIR}/deb"
