#!/bin/sh

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."

MODEL=${1:-"${BASE_DIR}/models/wsj.model"}
PORT=${2:-12345}

cd "${BASE_DIR}"

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"

java -cp "${CLASSPATH}" -Xms8g -Xmx8g mst.DependencyEnglish2OProjParser \
  "${MODEL}" \
  ${PORT}
