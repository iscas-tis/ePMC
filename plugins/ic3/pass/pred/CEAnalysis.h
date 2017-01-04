/**************************** CPPHeaderFile ***************************

* FileName [CEAnalysis.h]

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

#ifndef __CEANALYSIS_H__
#define __CEANALYSIS_H__

#include "model_checker/ActionGraph.h"
#include "model_checker/Witness.h"

namespace pred {


/*! \brief analyze counterexamples, report if they are spurious, derive new predicates
*/
class CEAnalysis {
public:
	CEAnalysis(lang::Model& model,
		   AbsModel& absmodel,
		   unsigned max_nr = 100);

	lbool checkCE(
		model_checker::ActionGraph&,
		const CVC3::Expr& bad, double prob,
		const PredSet& preds);

	/*! \brief check if path is spurious
	    \param path path to check
	    \param bad  bad states
	    \return if spurious l_false, if real l_true, indeterminate l_undef
	*/
	lbool checkCE(const std::vector<std::pair<unsigned,double> >& path, const CVC3::Expr& bad);

	/*!
	 * Path analysis procedure of the strongest-evidence method
	 */
	lbool analyzeCE(model_checker::ActionGraph& g,
			const std::vector<model_checker::LabeledTransition >& path,
			const std::vector<int>& prob_choice,
			const CVC3::Expr& bad,
			std::hash_set<CVC3::Expr>& preds,
			double& prob);
	/*!
	 * Path analysis procedure of the strongest-deviation method
	 */
	lbool analyzePath(
			const std::vector<model_checker::LabeledTransition >& path,
			std::hash_set<CVC3::Expr>& preds,
			double& prob);

	lbool analyzePath(
			const std::vector<model_checker::LabeledTransition >& path,
			std::vector<CVC3::Expr>& preds,
			double& prob);

	lbool getPredicates(const std::vector<CVC3::Expr>& path, const model_checker::Witness& w, std::hash_set<CVC3::Expr>& exprs);


	bool nextProbChoice(const std::vector<model_checker::LabeledTransition >& path,
		     	    std::vector<int>& prob_choice, int& last_uncertainty) ;

	const std::hash_set<CVC3::Expr>& getPreds() { return predicates; }
	lbool getInterpolants(const std::vector<CVC3::Expr>& , std::hash_set<CVC3::Expr>& exprs);
	lbool getInterpolant(const std::vector<CVC3::Expr>& , std::hash_set<CVC3::Expr>& exprs);
	lbool getPredicates(const model_checker::Witness&, std::hash_set<CVC3::Expr>& );
	lbool getPredicates(const std::vector<model_checker::Witness>&, std::hash_set<CVC3::Expr>& );

	/************************** counterexample analysis by state splitting ********************************/
	CVC3::Expr getConjunctionOfGuards(const std::vector<int>& action_set);
	void splitOnActionSetPair(const std::vector<int>& action_set1,
				  const std::vector<int>& action_set2,
			          std::hash_set<CVC3::Expr>& exprs) ;
	void splitOnActionSets(const std::vector<std::vector<int> >&, std::hash_set<CVC3::Expr>& exprs ) ;
	lbool getSplit(model_checker::ActionGraph& graph,
		       std::hash_set<CVC3::Expr>& exprs) ;


private:
	lang::Model& model;
	AbsModel& absmodel;

	TransitionConstraint tc;

	unsigned max_nr;


	std::hash_set<CVC3::Expr> predicates;



	dp::YicesSMT smt;


	//! interpolating decision procedure
	dp::SMT& interpolator;

	lbool GeneratePredicates(
			const std::vector<std::vector<CVC3::Expr> >& symbolic_path,
			std::hash_set<CVC3::Expr>& preds );

};

}

#endif

