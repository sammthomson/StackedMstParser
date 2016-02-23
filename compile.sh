#!/usr/bin/env bash

set -e

mkdir -p target
# clean
find target \( -name "*.class" \) -exec rm '{}' \;
# compile
javac -sourcepath src \
  -cp "./lib/mallet.jar:./lib/mallet-deps.jar" \
  -d target \
  src/mst/DependencyEnglish2OProjParser.java
