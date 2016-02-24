#!/bin/sh

# $1 input file $2 output file

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."

cd "${BASE_DIR}"

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"

echo "Running Client Program"
java -cp "${CLASSPATH}" mst.DependencyClient $1 $2 12345
