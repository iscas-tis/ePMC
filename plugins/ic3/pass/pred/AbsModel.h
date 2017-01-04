/*! \file AbsModel.h
    \brief Header file for AbsModel classes.]
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
#ifndef __ABSMODEL_H__
#define __ABSMODEL_H__

class ODDNode;

namespace pred {
class PredSet;
class AbsCommand;
class CFG;

/*! \brief an abstract boolean model
 *  \see AbsModelImpl
 * Interface class to abstract models: contains only output routines to MTBDDs and streams
 * while extraction routines are included in derived classes (currently only AbsModelImpl)
 */
class AbsModel {
public:
	/*! \brief Constructor that does nothing of interest */
	AbsModel (lang::Model& model, dp::SMT&);
	virtual ~AbsModel() {}
	virtual AbsModel& operator=(const AbsModel&);


	/*! \brief Return the current set of predicates
	 * @return: current PredSet
	*/
	const PredSet& getPreds() const;

	/*! \brief Extract the AbsModel from a Model
	 * \pre  InitialExtract has not been called before
         * \post The initial states and the transition relation of the abstract model are constructed.
         *       The abstract model is ready to use.
	 *
         * @param: model the model from which to extract
	 */
	virtual void InitialExtract() = 0;
	virtual void Refine(const PredSet&) = 0;

	/**
		\brief abstraction refinement loop
		\return 0 ... success, 1 ... failure, 2 ... timeout
	*/
	virtual int CEGAR();

	/**
		\brief do bookkeeping for newly discovered predicates
		\post inform the EncodingManager of the new predicate
	*/
	bool addPredicate(const Predicate &p, bool expensive_checks=false);

	/*! \brief Check each of the properties in model.getProperties()
	    \pre RefineExtract() has been called before
	    \see InitializeModelCheckingEngine(), ShutdownModelCheckingEngine(), Check()
	*/
	void CheckProperties();


	lang::Model& model;                 //! concrete model
	std::vector<CVC3::Expr> invar;           //! invariants on program variables

	PredSet preds;                  //! current predicates
	std::vector<std::string> b;                   //! names of BDD variables

	std::vector<boost::shared_ptr<AbsCommand> > abs_gt;//! abstractions of guarded transitions

	/* stuff needed for model checking */
	bdd::DDManager dd_mgr;            //! BDD manager in which transition and state sets are stored
	bdd::BDD initial_states;            //! set of initial abstract states

	CVC3::Expr goal_expr;
	lang::Properties properties;        //! abstract properties

	/*! \brief for a predicate returns its (BDD) variable index
         * @return BDD index
	 * @param pred The predicate for which we want to obtain the index
	 * @param instance The timeframe at which the predicate occurs in a transition
         * instance 0 = current state
	 * instance 1 = value of predicate according to assignment 1
	 * instance 2 = value of predicate according to assignment 1
	 *     ...     =                ...
         */
	int getIndex   (const Predicate& pred, unsigned instance = 0) const;

	dp::SMT& getSMT() const { return smt; }


	std::string getString(const Cube& c, const PredSet& preds) ;

	std::string getString(int state, const PredSet& preds);

	/*! \brief over-approximates an expression in current abstraction
	 * @param: e expression to be abstracted
	 * @param: preds set of predicates w.r.t. which to abstract
	 * @return: cube cover of the expression as a BDD
	 */
        virtual bdd::BDD abstractCover(const CVC3::Expr& e,
 			               const PredSet& preds,
  			               const bdd::BDD& care_set) = 0;

	/*! \brief under-approximates an expression in current abstraction
		 * @param: e expression to be abstracted
		 * @param: preds set of predicates w.r.t. which to abstract
		 * @return: cube interior of the expression as a BDD
		 */
	virtual bdd::BDD abstractInterior(const CVC3::Expr& e,
				   const PredSet& preds,
   				   const bdd::BDD& care_set) = 0;


	/*! returns the expression corresponding to an abstract state */
	CVC3::Expr getExpr(int state) const;
	std::string getString(int state) const;
	/*! computes the weakest precondition of a distribution over abstract states w.r.t. a command */
	CVC3::Expr getWP(unsigned action, const std::vector<unsigned>& states) const;

	/*!
		\brief Difference of two abstract states
		\param state1
		\param state2
		\param position in which two abstract states differ
		\return true if there is a difference
	*/
	bool getDiff(unsigned state1, unsigned state2, unsigned& position) const;

protected:
	friend class AbsCommand;
	friend class CFGCommand;

	dp::SMT& smt;


	/*! \brief get BDD index of the i-th bit nondeterminism variable */
	int getNondetIndex(unsigned i) const ;

	/*! \brief Return the BDD corresponding to predicate
	 * @see getIndex()
         */
	bdd::BDD getBDD (const Predicate& pred, unsigned instance = 0) const;

	/**************** model output ****************/

	/*! \brief computes mapping from BDD indices to names of boolean variables
	    \post b is initialized
         */
	void ComputeBooleanVariables();

	/*! \brief check a given property
	    \note traverses a property and recursively does PCTL model checking
	    \return l_true if true, l_false if false, l_undef if unknown
	    \pre InitializeModelCheckingEngine has been called before and
		 table should be empty or contain something sensible (as its contents are then assumed)
	    \post table contains for each node of property the result of model checking
	    \param prop        property to be checked
            \param fresh_preds potential predicates for refinement
	    \param table       table to remember results in recursive traversal
	    \see InitializeModelCheckingEngine() Reach()
	*/
	lbool Check(const lang::Property* prop, PredSet& new_preds,
		    std::map<const lang::Property*,bdd::MTBDD>& table);

	lbool Reach(const lang::Bound& b, lang::Until* until, bool, bdd::MTBDD b1, bdd::MTBDD b2, double threshold,
		    PredSet& fresh_preds,
		    std::map<const lang::Property*,bdd::MTBDD>& table);

	EncodingManager em;

	bdd::MTBDD trans_matrix_mtbdd;
	bdd::BDD trans01_bdd;
	bdd::BDD trans2;

	/*! BEGIN: intialized by ComputeBooleanVariables */
	std::vector<bdd::MTBDD> prob_vars_vector;
	std::vector<bdd::MTBDD> nondet_vars_vector;
	std::vector<bdd::MTBDD> pres_vars_vector;
	std::vector<bdd::MTBDD> next_vars_vector;
	std::vector<bdd::BDD> pres_vars_bdd;
	bdd::BDD pres_vars_cube;
	std::vector<bdd::BDD> next_vars_bdd;
	/*! END: intialized by ComputeBooleanVariables */

	bdd::BDD reach01;

	bdd::MTBDD reach;    //! reachable states , support = pres
	bdd::MTBDD trans;    //! probabilistic transition matrix, support = nondet + pres + next
	bdd::MTBDD trans01;  //! 0-1-version of trans, support = nondet + pres + next

	bdd::MTBDD initial_mtbdd;

	bdd::MTBDD nondetMask;
	bdd::MTBDD deadlocks;
	bdd::ODD odd;

	//ODDNode *odd;
	std::vector<unsigned> nondet_begins;

	int num_interleaving_choice_vars;

	void FixDeadlocks();
/*
	void buildGameGraph(
		model_checker::GameGraph& game_graph,
		model_checker::RegionMap&,
		model_checker::MDPSparse& sm,
		bdd::DDManager& dd_mgr,
		bdd::ODD& odd,
		bdd::MTBDD &init,
		bdd::MTBDD &trans,
		bdd::MTBDD &yes,
		bdd::MTBDD &maybe,
		bool min);
*/
	void buildCFG(
		CFG& cfg,
		model_checker::MDPSparse& sm,
		bdd::DDManager& dd_mgr,
		bdd::ODD& odd,
		bdd::MTBDD &init,
		bdd::MTBDD &trans,
		bdd::MTBDD &yes,
		bdd::MTBDD &maybe,
		bool min);
	void addChoiceSets(
	//model_checker::GameGraph& game_graph,
	const CVC3::Expr& state_expr,
	//model_checker::State state,
	//std::vector<model_checker::Distribution>& distr,
	const std::vector<int>& commands);
	void addChoiceSets(
	//model_checker::GameGraph& game_graph,
	//model_checker::State state,
	//std::vector<model_checker::Distribution>& distributions,
	const std::vector<int>& commands, const Cube&);

	// encodes static information about the guards of the program
	// namely which ones intersect
	bdd::BDD static_game_info;

	//void getConstraint(const HyperTransition& t1, const HyperTransition& t2, std::vector <CVC3::Expr>& result);
	void createInstantiationOfVariables(int time, CVC3::ExprHashMap<CVC3::Expr>& result);
	//CVC3::Expr getConstraint(const Transition& t, const std::map<int, CVC3::ExprHashMap<CVC3::Expr> >& instantiation);
	//CVC3::Expr getConstraint(const Transition& t, int offset, std::map<int,CVC3::ExprHashMap<CVC3::Expr> >& instantiation);

	std::map<int,CVC3::ExprHashMap<CVC3::Expr> > instantiation; // map state numbers to instantiations
};

} // end of namespace pred

#endif
