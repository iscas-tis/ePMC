Using an external unsat-core extractor with MathSAT 4
-----------------------------------------------------

MathSAT 4 supports two ways of computing an unsatisfiable core of an SMT
problem in CNF. One is proof-based, the other uses an external
boolean-unsat-core extractor.

Unsat core computation is activated by the -unsat_core=OUTNAME command-line
switch, where OUTNAME is the output file name. With -ext_unsat_core, MathSAT 4
uses an external tool. By default, such tool must be called
compute_unsat_core, and interaction with it happens via exchange of three
files. First, MathSAT 4 generates (in the current directory) a file called
smt_core.cnf. Then, the external tool is invoked with such file as argument,
and is expected to generate a file unsat_core.cnf. Finally, an auxiliary
program, called get_unsat_core_ids (which is distributed with MathSAT 4) is
invoked on such file, to generate an unsat_core_ids.txt file, from which
MathSAT 4 can compute the unsat core for the original problem. Both programs
should receive two arguments: the first is the name of the "bool+theory" unsat
core (smt_core.cnf) file, the second the name of the "bool only" unsat core
(unsat_core.cnf) file.

Both the names of the external programs and all the file names can be changed by setting appropriate environment variables:

    * MSAT_UNSAT_CORE_CMD overrides compute_unsat_core
    * MSAT_UNSAT_CORE_IDS_CMD overrides get_unsat_core_ids
    * MSAT_UNSAT_CORE_IN_NAME overrides smt_core.cnf
    * MSAT_UNSAT_CORE_OUT_NAME overrides unsat_core.cnf
    * MSAT_UNSAT_CORE_IDS_NAME overrides unsat_core_ids.txt
