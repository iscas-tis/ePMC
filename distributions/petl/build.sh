#!/bin/bash
. ../auxiliary.sh

assemble_begin petl
prepare_plugin util
prepare_plugin value-basic
prepare_plugin dd
prepare_plugin expression-basic
prepare_plugin graph
prepare_plugin algorithm
prepare_plugin graphsolver
prepare_plugin graphsolver-iterative
prepare_plugin jani-model
prepare_plugin jani-interaction
prepare_plugin automata
prepare_plugin prism-format
prepare_plugin command-check
prepare_plugin command-help
prepare_plugin constraintsolver
prepare_plugin constraintsolver-smt-lib
prepare_plugin propertysolver-propositional
prepare_plugin propertysolver-operator
prepare_plugin propertysolver-pctl
prepare_plugin propertysolver-petl
assemble_end petl
