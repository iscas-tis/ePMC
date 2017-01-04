/******************************** CPPFile *****************************

* FileName [Statistics.cpp]

* PackageName [util]

* Synopsis [Method definitions of Statistics class.]

* SeeAlso [Statistics.h]

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2007 by Saarland University. All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/


#include "Util.h"
#include "Error.h"

#include "Database.h"
#include "Statistics.h"

namespace util {


/*********************************************************************/
//static members
/*********************************************************************/
Timer Statistics::globalTimer;  // global time elapsed
Timer Statistics::flattenTimer; // time for flattening the model

Timer Statistics::extractTimer;
Timer Statistics::extractInitialTimer;
Timer Statistics::extractGCommandsTimer;
Timer Statistics::extractPreTimer; // preprocessing: filtered predicates & decomposition

Timer Statistics::smtTimer;     // time for SMT solving

Timer Statistics::mutexTimer;   // time for mutual exclusion of predicates
Timer Statistics::bddTimer;   // time for mutual exclusion of predicates
Timer Statistics::outputTimer;   // time spent in model output


Timer Statistics::abstractionTimer;
Timer Statistics::valueiterTimer;
Timer Statistics::ceanalysisTimer;
Timer Statistics::guardAbstractionTimer;


unsigned util::Statistics::nr_of_states = 0;
unsigned Statistics::nr_of_paths = 0;
unsigned Statistics::nr_of_refine = 0;
unsigned Statistics::nr_of_preds = 0;
double Statistics::lower_bound = 0;
double Statistics::upper_bound = 1;

/*********************************************************************/
//shutdown all running timers
/*********************************************************************/
void Statistics::ShutdownTimers()
{
  if(globalTimer.Running()) globalTimer.Stop();
  if(smtTimer.Running()) smtTimer.Stop();
  if(flattenTimer.Running()) smtTimer.Stop();
  if(mutexTimer.Running()) mutexTimer.Stop();
  if(bddTimer.Running()) bddTimer.Stop();
  if(outputTimer.Running()) outputTimer.Stop();
  if(extractPreTimer.Running()) outputTimer.Stop();

  if(extractTimer.Running()) extractTimer.Stop();
}

/*********************************************************************/
//display statistics
/*********************************************************************/
void Statistics::Display()
{
  if(Database::DISPLAY_STATISTICS) {

// 	if(Database::VERBOSITY_LEVEL>=1) {
// 		Message(1,"==================================[ Global Statistics ]===================================\n");
// 		Message(1,"global time         = %.1f milliseconds\n",globalTimer.Read() * 1000);
// 		Message(1,"cpu time            = %.1f milliseconds\n",cpuTime * 1000);
// 		Message(1,"flattening          = %.1f milliseconds\n",flattenTimer.Read() * 1000);
// 		Message(1,"predicate insertion = %.1f milliseconds\n",mutexTimer.Read() * 1000);
// 		Message(1,"output time         = %.1f milliseconds\n",outputTimer.Read() * 1000);
// 		Message(1,"==================================[ Model Extraction ]===================================\n");
// 		Message(1,"total time for model extraction = %.1f milliseconds\n",extractTimer.Read() * 1000);
// 		Message(1,"purpose:\n");
// 		Message(1,"* initial           = %.1f milliseconds\n",extractInitialTimer.Read() * 1000);
// 		Message(1,"* guarded commands  = %.1f milliseconds\n",extractGCommandsTimer.Read() * 1000);
// 		Message(1,"* Preprocessing     = %.1f milliseconds\n",extractPreTimer.Read() * 1000);
// 		Message(1,"activity:\n");
// 		Message(1,"* SMT               = %.1f milliseconds\n",smtTimer.Read() * 1000);
// 		Message(1,"* BDD               = %.1f milliseconds\n",bddTimer.Read() * 1000);
// 	} else {
//

	if(globalTimer.Running())
		globalTimer.Stop();
	if(abstractionTimer.Running())
		abstractionTimer.Stop();
	if(extractPreTimer.Running())
		extractPreTimer.Stop();
	if(guardAbstractionTimer.Running())	
		guardAbstractionTimer.Stop();
	if(extractGCommandsTimer.Running())
		extractGCommandsTimer.Stop();
	if(bddTimer.Running())
		bddTimer.Stop();
	if(valueiterTimer.Running())
		valueiterTimer.Stop();
	if(ceanalysisTimer.Running())
		ceanalysisTimer.Stop();
		
	double global (globalTimer.Read());
	MSG(0,"abstraction: %5.0f (%0.3f\% [pre: %0.3f\%, guards: %0.3f\%, transitions: %0.3f\%, BDD: %0.3f\%]) model checking: %5.0f (%0.3f\%) counterexample analysis: %5.0f (%0.3f\%)\n",
		abstractionTimer.Read(),
		100*abstractionTimer.Read()/global,
		100*extractPreTimer.Read()/abstractionTimer.Read(),
		100*guardAbstractionTimer.Read()/abstractionTimer.Read(),
		100*extractGCommandsTimer.Read()/abstractionTimer.Read(),
		100*bddTimer.Read()/abstractionTimer.Read(),
		valueiterTimer.Read(),
		100 * valueiterTimer.Read()/global,
		ceanalysisTimer.Read(),
		100 *ceanalysisTimer.Read()/global);
	MSG(0,"Probability lies in [%E %E]\n",lower_bound,upper_bound);
	MSG(0,"Stat # blocks %d |  # ref %d | # pred: %d | paths: %d | total: %6.0f \t\n", nr_of_states, nr_of_refine, nr_of_preds, nr_of_paths, globalTimer.Read());
// 	}
  }
}

} //end of util

/*********************************************************************/
//end of Statistics.cpp
/*********************************************************************/
