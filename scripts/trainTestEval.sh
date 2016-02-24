#!/bin/sh

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."
train="${BASE_DIR}/data/wsj-02-21.part.CONLL"
test="${BASE_DIR}/data/wsj-22.mrg.CONLL"
model="${BASE_DIR}/models/wsj-02-21.part.CONLL.model"
out="${BASE_DIR}/wsj-22_pred.part.CONLL"
ord=2

cd ${BASE_DIR}

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"


# Train
java -cp "${CLASSPATH}" -Xms8g -Xmx8g mst.DependencyParser train \
  separate-lab \
  separate-lab-cutoff:0 \
  iters:10 \
  decode-type:proj order:"${ord}" \
  train-file:"${train}" \
  model-name:"${model}" \
  format:CONLL

# Test
java -cp "${CLASSPATH}" -Xms8g -Xmx8g mst.DependencyParser test \
  separate-lab \
  model-name:"${model}" \
  decode-type:proj \
  order:"${ord}" \
  test-file:"${test}" \
  output-file:"${out}" \
  format:CONLL

# Eval
java -cp "${CLASSPATH}" -Xms8g -Xmx8g mst.DependencyParser eval \
  gold-file:"${test}" \
  output-file:"${out}" \
  format:CONLL
