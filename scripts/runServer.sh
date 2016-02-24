#!/bin/sh

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."

MODEL=${1:-"${BASE_DIR}/models/wsj.model"}
PORT=${2:-12345}

cd "${BASE_DIR}"

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"

java -cp "${CLASSPATH}" -Xms5g -Xmx5g mst.SecondOrderProjectiveSocketServer \
  "${MODEL}" \
  ${PORT}
