#!/bin/bash
. ../auxiliary.sh

assemble_begin jani
prepare_plugin util
prepare_plugin value-basic
prepare_plugin dd
prepare_plugin expression-basic
prepare_plugin graph
prepare_plugin algorithm
prepare_plugin graphsolver
prepare_plugin prism-format
prepare_plugin command-help
prepare_plugin command-check
prepare_plugin propertysolver-propositional
prepare_plugin propertysolver-operator
prepare_plugin propertysolver-reward
prepare_plugin propertysolver-filter
prepare_plugin propertysolver-pctl
prepare_plugin dd-cudd
prepare_plugin dd-cudd-mtbdd
prepare_plugin graphsolver-iterative
prepare_plugin specialise-smg
prepare_plugin jani-model
prepare_plugin jani-interaction
assemble_end jani
