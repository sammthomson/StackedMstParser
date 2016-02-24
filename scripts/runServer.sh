#!/bin/sh

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."

cd "${BASE_DIR}"

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"

java -cp "${CLASSPATH}" -Xms8g -Xmx8g mst.DependencyEnglish2OProjParser \
  "${BASE_DIR}/models/wsj.model" \
  12345
