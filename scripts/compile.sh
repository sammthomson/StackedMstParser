#!/usr/bin/env bash

set -e

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."

cd "${BASE_DIR}"

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"


mkdir -p target
# clean
find target \( -name "*.class" \) -exec rm '{}' \;
# compile
javac -sourcepath src \
  -cp "${CLASSPATH}" \
  -d target \
  src/mst/DependencyEnglish2OProjParser.java \
  src/mst/DependencyParser.java \
  src/mst/DependencyClient.java
