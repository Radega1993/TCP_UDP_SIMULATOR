#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="${ROOT_DIR}/target/dist"
VERSION="${1:-}"
APP_NAME="TCP-UDP-Simulator"
PACKAGE_NAME="tcp-udp-simulator"
MAIN_CLASS="com.example.simulator.Main"

cd "${ROOT_DIR}"
PROJECT_VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
if [[ -z "${VERSION}" ]]; then
  VERSION="${PROJECT_VERSION}"
fi

ARTIFACT_ID="$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)"
ARTIFACTS_DIR="${ROOT_DIR}/artifacts/linux/${VERSION}"
JAR_NAME="${ARTIFACT_ID}-${PROJECT_VERSION}.jar"

rm -rf "${DIST_DIR}" "${ARTIFACTS_DIR}/app-image/${APP_NAME}" "${ARTIFACTS_DIR}/deb"
mkdir -p "${DIST_DIR}" "${ARTIFACTS_DIR}/app-image" "${ARTIFACTS_DIR}/deb"

echo "==> Compilando proyecto y copiando dependencias..."
mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="${DIST_DIR}"
cp "target/${JAR_NAME}" "${DIST_DIR}/"

echo "==> Generando app-image ${VERSION}..."
jpackage \
  --type app-image \
  --name "${APP_NAME}" \
  --app-version "${VERSION}" \
  --input "${DIST_DIR}" \
  --main-jar "${JAR_NAME}" \
  --main-class "${MAIN_CLASS}" \
  --icon "${ROOT_DIR}/icono.png" \
  --dest "${ARTIFACTS_DIR}/app-image"

echo "==> Generando paquete .deb ${VERSION}..."
jpackage \
  --type deb \
  --name "${APP_NAME}" \
  --linux-package-name "${PACKAGE_NAME}" \
  --app-version "${VERSION}" \
  --linux-app-release "1" \
  --linux-shortcut \
  --input "${DIST_DIR}" \
  --main-jar "${JAR_NAME}" \
  --main-class "${MAIN_CLASS}" \
  --icon "${ROOT_DIR}/icono.png" \
  --dest "${ARTIFACTS_DIR}/deb"

echo "Listo. Artefactos en:"
echo "  - ${ARTIFACTS_DIR}/app-image"
echo "  - ${ARTIFACTS_DIR}/deb"
echo
echo "Para actualizar una instalación .deb existente, instala una versión mayor con:"
echo "  sudo apt install ./artifacts/linux/${VERSION}/deb/*.deb"
