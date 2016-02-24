#!/bin/bash

set -x

INPUT_FILE=$1
OUTPUT_FILE=$2
SERVER_HOSTNAME=${3:-localhost}
SERVER_PORT=${4:-12345}

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."

cd "${BASE_DIR}"

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"

echo "Running Client Program"
java -cp "${CLASSPATH}" mst.DependencyClient ${INPUT_FILE} ${OUTPUT_FILE} ${SERVER_HOSTNAME} ${SERVER_PORT}
