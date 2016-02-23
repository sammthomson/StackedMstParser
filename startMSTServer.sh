#!/bin/bash

BASE_DIR=$(dirname "${BASH_SOURCE[0]}")

java -cp "./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" \
  -Xms8g -Xmx8g \
  mst.DependencyEnglish2OProjParser \
  "${BASE_DIR}/models/wsj.model" \
  "${BASE_DIR}/tmp" \
  12345
