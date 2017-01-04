/**************************** CPPHeaderFile ***************************

* FileName [Database.h]

* PackageName [util]

* Synopsis [Header file containing all static constants, arguments and
* options to the tool as well as data structures.]

* Description []

* SeeAlso []

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2006 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#ifndef __DATABASE_H__
#define __DATABASE_H__

#define MAX_OPTS = 10

#include "Database.h"
#include "Util.h"

extern std::string model_name;

namespace lang {
  class Model;
}

namespace util {

/*! \class Database Save global settings of the tool in a central place
    \ingroup util
*/
namespace Database
{
	/** This function parses command line parameters and sets the flags in the database */
	void ProcessCommandLineArguments(const std::vector<std::string>& argv);

	/** This function invokes the parser */
	void parseInputs(lang::Model &);

	//set verbosity and statistics
	extern bool PrettyPrintModel;
	extern bool CheckProperties;


	extern std::string model_name;
	extern std::string model_file_name;


	/** debugging mode on ? */
	extern bool DEBUG;

	extern bool displayMarkovChain;
	extern bool displayCEX;


	/**! verbosity level of messages */
	extern int VERBOSITY_LEVEL;
	extern int DISPLAY_STATISTICS;

	//syntactic implication checks
	extern int USE_SYNTACTIC_CHECKS;
	//parser
	extern int INT_TYPE;
	extern int FLOAT_TYPE;
	extern int BOOL_TYPE;

	//theorem prover
	enum TP { Yices = 0,
		  YicesLight = 1,
		  CVC3 = 2
		  };
	extern TP THEOREM_PROVER_USED;
	extern int THEOREM_PROVER_CACHE;
	extern int THEOREM_PROVER_CACHE_SIZE;

		//theorem prover
	enum IP { FOCI = 0,
		  CLP = 1,
		  CSIsat= 2,
		  MathSat = 3
		  };

	extern IP INTERPOLATION;
	extern bool FACTORIZE_INTERPOLATION;

	extern bool SMT_TYPE_CHECK;
	extern bool DUMP_PROOF_SCRIPT;

	// model checking
	enum MCTechniques {
		sparse,
		hybrid,
		mtbdd,
		game
	} ;
	extern MCTechniques MCTechnique;

	enum ValiterOrder {
		random,  // like in PRISM
		rdfs,    // reverse DFS order
		topo     // topological order (with SCCs)
	};
	extern ValiterOrder valiter_order;


	// compute transitive closure of transition with trivial distribution
	extern bool MCPreprocessing;

	// schedule and counterexamples
	extern bool MCComputeSchedule;

	extern unsigned CEMaxNrOfPaths;
	extern unsigned CEGARStrategy;

	enum Strategy {
		strongestEvidence, // Hermanns/Wachter/Zhang CAV'08
		optDiff,           // Hermanns/Wachter/Zhang 2009
		optDiffPath,
		anyDiff,	   // Kattenbelt/Kwiatokwska/Parker/Norman VMCAI'09 strategy
		onlyScheduler      // Kattenbelt/Kwiatkowksa/Parker/Norman technical report
		
	};

	extern bool ProbEager;



	extern Strategy strategy;

	extern unsigned CEGARMaxSpurious;

	extern unsigned COIBound;


	extern double epsilon;
	extern double term_crit_param;
        extern int term_crit;
	extern int max_iters;
	extern bool extend_yes;


	extern bool CartesianAbstraction;

	// probabilistic choices to identical states are merged in model checking
	extern bool CompactProbChoices;
	extern bool CEGAR;
	extern bool PredsFromInit;
	extern bool PredsFromGuards;

	extern bool Split;

	extern bool incremental;

  class Options
  {


  };


};

} //namespace util

#endif //__DATABASE_H__

/*********************************************************************/
//end of Database.h
/*********************************************************************/
