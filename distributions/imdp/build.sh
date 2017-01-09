#!/bin/bash
. ../auxiliary.sh

assemble_begin imdp
prepare_plugin util
prepare_plugin value-basic
prepare_plugin dd
prepare_plugin expression-basic
prepare_plugin graph
prepare_plugin algorithm
prepare_plugin graphsolver
prepare_plugin graphsolver-iterative
prepare_plugin propertysolver-pctl
prepare_plugin automata
prepare_plugin jani-model
prepare_plugin timedautomata
prepare_plugin prism-format
prepare_plugin command-help
prepare_plugin command-lump
prepare_plugin command-check
prepare_plugin constraintsolver
prepare_plugin constraintsolver-lp-solve
prepare_plugin propertysolver-propositional
prepare_plugin propertysolver-operator
prepare_plugin propertysolver-reward
prepare_plugin propertysolver-coalition
prepare_plugin propertysolver-multiobjective
prepare_plugin dd-cudd
prepare_plugin dd-cudd-mtbdd
prepare_plugin automaton-determinisation
prepare_plugin imdp
assemble_end imdp
