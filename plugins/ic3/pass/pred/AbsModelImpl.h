/*! \file AbsModelImpl.h
    \brief Header file for AbsModelImpl class.]
* \note This class expresses the abstract model
* that is submitted to the finite-state stochastic model
* checker.
* \author Bjoern Wachter
* \remarks Copyright (c) 2007 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de.
**********************************************************************/
#ifndef __ABSMODELIMPL_H__
#define __ABSMODELIMPL_H__

namespace pred {

/*! \brief An Implementation of model extraction */
class AbsModelImpl : public AbsModel {
public:
	virtual ~AbsModelImpl() {}

	/*! \brief Extract the AbsModel from a Model
	 * \pre  InitialExtract has not been called before
         * \post The initial states and the transition relation of the abstract model are constructed.
         *       The abstract model is ready to use.
	 *
         * @param: model the model from which to extract
	 */
	virtual void InitialExtract();

	/*! \brief Extract a new AbsModel after a refinement
	 * \pre  InitialExtract has been called before
         * \post The initial states and the transition relation of the abstract model are constructed.
         *       The abstract model is ready to use.
	 *
         * @param: new_preds the newly discovered predicates
	 */
	virtual void Refine(const PredSet& new_preds);

	AbsModelImpl(lang::Model&, dp::SMT&);
protected:
	/**************** model extraction ****************/
		/*! \brief compute local predicate set for guarded transition
	 *
	 * \post p contains the predicates from the DisjPredSet preds
         * sharing a variable with the guarded transition
         *
         * @param gt guarded transition
	 * @param p the relevant predicates
	 *
	 * Extracts the set of predicates relevant to the abstraction of
	 * a guarded transition. A predicate is relevant if it shares a
         * a variable with the guarded transition.
	 */
	void ComputeLocalPreds(const lang::Command& gt,PredSet& p);

	/*! \brief invoke construction of initial states and transitions of abstract model
	    \pre all desired predicates are present
	    @param model to be abstracted
	    @param predicates of interest
	 */
	void RefineExtractModel(PredSet&);

        /*! \brief abstracts an expression by a set of cubes covering it
	 * @param: e expression to be abstracted
	 * @param: preds set of predicates w.r.t. which to abstract
	 * @return: cube cover of the expression as a BDD
	 * @see: abstractCover(const CVC3::Expr&, const PredSet&)
	 */
        virtual bdd::BDD  abstractCover(const CVC3::Expr& e,
		        	   	const PredSet& preds,
				        const bdd::BDD& care_set);

        virtual bdd::BDD abstractInterior(const CVC3::Expr & e,
					  const PredSet & preds,
					  const bdd::BDD& care_set);

	/*! \brief extract predicates from the model
	 *  \post the predicates are put into preds
         *  @param model The model to go through
	 */
	void ExtractPredicates();

	/*! \brief translate property to property over boolean variables
	 *  \post The properties are put into the field properties of the AbsModel
	 *  @param props Properties of the concrete model
	 */
	void ExtractProperties(const lang::Properties& props);


	void computeGameBDD(const CVC3::Expr& assumption);

	bdd::DDManager allsat_ddmgr;

};

} // end of namespace magic

#endif
