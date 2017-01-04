/**************************** CPPHeaderFile ***************************

* FileName [Statistics.h]

* PackageName [util]

* Synopsis [Header file for Statistics class.]

* Description [This class encapsulates various statistics collected
* during the execution of the tool.]

* SeeAlso [Statistics.cpp]

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2007 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#ifndef __STATISTICS_H__
#define __STATISTICS_H__

namespace util {

//other classes needed
class Timer;

/*! \class Statistics Collects statistic information
    \note Statistics stores global timers and is used to print stats
    \ingroup util
*/
class Statistics
{
public:
	/*! \brief global execution time */
	static Timer globalTimer;
	/*! \brief model flattening */
	static Timer flattenTimer;
	/*! \brief extraction time for abstract model */
	static Timer extractTimer;
	/*! \brief extraction of initial abstract states */
	static Timer extractInitialTimer;
	/*! \brief extraction of abstract transition relation */
	static Timer extractGCommandsTimer;
	
	/*! \brief time for guard abstraction */
	static Timer guardAbstractionTimer;
	

	/*! \brief preprocessing time for model extraction */
	static Timer extractPreTimer;
	/*! \brief time spent by SMT solver */
	static Timer smtTimer;


	/*! \brief time spent by predicate abstraction */
	static Timer abstractionTimer;
	/*! \brief time spent by value iteration */
	static Timer valueiterTimer;
	/*! \brief time spent by counterexample analysis */
	static Timer ceanalysisTimer;

	/*! \brief time spent checking mutual exclusion of guards during flattening */
	static Timer mutexTimer;
	/*! \brief time spent by BDD manager */
	static Timer bddTimer;
	/*! \brief time spent for model output */
	static Timer outputTimer;
	/*! \brief constructor */
	Statistics() {}
	/*! \brief stop all timers */
	static void ShutdownTimers();
	/*! \brief print stats */
	static void Display();
	/*! \brief number of predicates */
	static unsigned nr_of_preds;
	static unsigned nr_of_paths ;
	static unsigned nr_of_refine;
	static unsigned nr_of_states;
	static double lower_bound ;
	static double upper_bound ;
};

} //namespace util

#endif //__STATISTICS_H__

/*********************************************************************/
//end of Statistics.h
/*********************************************************************/
