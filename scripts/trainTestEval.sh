#!/bin/sh

BASE_DIR="$(dirname "${BASH_SOURCE[0]}")/.."

model_name=${1:-mymodel}
experiment_dir="${BASE_DIR}/experiments/${model_name}"
model="${experiment_dir}/wsj-02-21.model"
output="${experiment_dir}/wsj-22.predicted.conll"
eval_results="${experiment_dir}/results.txt"
train="${BASE_DIR}/data/wsj-02-21.part.CONLL"
test="${BASE_DIR}/data/wsj-22.mrg.CONLL"
order=2


cd ${BASE_DIR}

CLASSPATH="./target:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar"

mkdir -p "${experiment_dir}"

# Train
java -cp "${CLASSPATH}" -Xms5g -Xmx5g mst.DependencyParser train \
  separate-lab \
  separate-lab-cutoff:0 \
  iters:10 \
  decode-type:proj \
  order:"${order}" \
  train-file:"${train}" \
  model-name:"${model}" \
  format:CONLL

# Test
java -cp "${CLASSPATH}" -Xms5g -Xmx5g mst.DependencyParser test \
  separate-lab \
  model-name:"${model}" \
  decode-type:proj \
  order:"${order}" \
  test-file:"${test}" \
  output-file:"${output}" \
  format:CONLL

# Eval
java -cp "${CLASSPATH}" -Xms5g -Xmx5g mst.DependencyParser eval \
  gold-file:"${test}" \
  output-file:"${output}" \
  format:CONLL > "${eval_results}"
