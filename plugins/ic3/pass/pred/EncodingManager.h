/*! \file AbsModel.h
    \brief Header file for EncodingManager classes.]
* \note This class encapsulates the symbolic encoding of the abstract model.
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
#ifndef __ENCODING_MANAGER_H__
#define __ENCODING_MANAGER_H__

namespace pred {
class EncodingManager;
class ChoiceRange;
class StateVar;
class SymbolicCluster;

/**
	\todo test in AbsModelImpl
	\todo encoding of transitions and states !
	\todo combination of clusters and composition of transition matrix MTBDD
*/

/** \brief represents probabilistic and nondeterministic choice in binary encoding via boolean variables */
class ChoiceRange {
public:
	ChoiceRange();
	ChoiceRange(const ChoiceRange&);
	ChoiceRange& operator=(const ChoiceRange&);
	ChoiceRange(EncodingManager& vm, int __index, int __lower, int __upper);


	/** 	\brief  binary encoding
		\pre    0 <= value <= 2^(upper-lower+1) - 1
		\return binary encoding of `value' as a BDD cube
	*/
	bdd::BDD getBDD(int value) const;


	const std::vector<bdd::BDD>& getChoiceVars() const { return choice_vars; }

	const bdd::BDD& getSupport() const { return support; }
	/**	\brief unique index value of the range */
	int getIndex() const { return index; }

	int getLower() const { return lower; }
	int getUpper() const { return upper; }

	std::string toString() const;
private:
	friend class EncodingManager;
	EncodingManager* em;
	int index; // (index)th choice range
	int lower, upper;
	std::vector<bdd::BDD> choice_vars;
	bdd::BDD support;
};


/** \brief represents boolean state variable of predicate abstraction */
class StateVar {
public:
	StateVar();
	StateVar(const StateVar&);
	bdd::BDD getBDD               ( int instance = 0 ) const;
	int      getBDDVariableIndex  ( int instance = 0 ) const;
	const Predicate& getPredicate () const;

	/**	\brief unique index value of the state variable */
	int getIndex() const { return index; }
	std::string getName() const { return name; }

	std::string toString() const;
private:
	friend class EncodingManager;
	friend struct ltStateVarPtr;
	StateVar(EncodingManager& __em, const Predicate& __predicate, int __index, int __offset);
	EncodingManager& em;
	int index;  // (index)th state variable
	int offset; // BDD variable offset
	Predicate predicate;
	std::string name;
	bdd::BDD present; // present state variable = dd_mgr.Variable(offset)
	bdd::BDD next;    // next state variable = dd_mgr.Variable(offset+1)
};

struct ltStateVarPtr {
	bool operator()(StateVar* sv1, StateVar* sv2) const {
		return sv1 && sv2 && (sv1->index < sv2->index);
	}
};


typedef std::unordered_map<std::pair<int,int>,CVC3::Expr> DecodingMap;

/**
	\brief encoding into BDDs
 */
class EncodingManager {
public:
	EncodingManager();

	EncodingManager (bdd::DDManager& dd_mgr,
			 int nr_of_reserved_nondet_variables,
			 int nr_of_reserved_prob_choice_variables,
			 int nr_of_time_frames = 2);
	EncodingManager (const EncodingManager&);
	EncodingManager& operator=(const EncodingManager&);
	~EncodingManager();
	ChoiceRange& createNondetChoiceRange (int nr_of_choices);
	ChoiceRange& getNondetChoiceRange    (int index) const;
	const ChoiceRange& getProbChoiceRange      () const;
	StateVar& createStateVar             (const Predicate& pred);
	StateVar& getStateVar                (int index) const;
	StateVar& getStateVarFromBDDIndex    (int index) const;
	StateVar& getStateVar		         (const Predicate& pred) const;
	bool      existStateVar             (const Predicate& pred) const;
	void resizeNondetChoiceRanges        (int new_size);
	void popNondetChoiceRange            (int nr = 1);
	std::string toString() const;
	static int getChoiceVarNeed(int nr_of_choices);
	static void computeVariableRange(int offset, int nr_of_choices, int &lower, int &upper);
	const std::vector<StateVar*>& getStateVarVector() const { return state_variables; }

	bdd::DDManager& getDDManager() const { return dd_mgr; }

	int getMaxNondetVar() const { return max_nondet_var; }
	int getCurrentMaxNondetVar() const {
		if(nondet_choice_ranges.size()==0) return 0;
		else return nondet_choice_ranges[nondet_choice_ranges.size()-1]->upper;
	}
	/*! convert BDD (over predicates) into expression */
	CVC3::Expr getExpr(const bdd::BDD& f) const;

	CVC3::Expr getExpr(const bdd::BDD&, const DecodingMap&) const;
	CVC3::Expr getExprRecur(DdNode*, int instance, const DecodingMap&) const;

	CVC3::Expr getExpr2(const bdd::BDD& f) const;
	CVC3::Expr getExprRecur(DdNode* node) const;

	const std::vector<CVC3::Expr>& getPredExprs() const { return pred_exprs; }
private:
	friend class StateVar;
	friend class ChoiceRange;
	friend class CubeEncoder;
	friend class SymbolicCluster;
	friend class SymbolicAbstractCommand;
	friend class SymbolicAbstractModel;
	bdd::DDManager& dd_mgr;

	ChoiceRange& createNondetChoiceRange (int, int, int);

	/* ---------------------------------------------------------------------------------------------------
	| non-determinism variables | probabilistic choice variables | state variable / next state variable
          first_nondet_choice         first_prob_choice                first_state
	*/
	// indices
	int first_nondet_choice;
	int first_prob_choice;
	int first_state;
	int nr_of_time_frames;
	std::vector<ChoiceRange*> nondet_choice_ranges;
	int max_nondet_var;
	ChoiceRange prob_choice_range;


	std::vector<StateVar*> state_variables;
	std::map<Predicate,StateVar*> state_variable_map;
	std::vector<CVC3::Expr> pred_exprs;
	bdd::BDD getBDDVariable(int bdd_variable_index);

	CVC3::Expr getIte(DdNode* left,
				      DdNode *right,
					  CVC3::Expr c,
					  CVC3::Expr le,
					  CVC3::Expr ri) const;
};

/** \brief encodes single transitions and states */
class CubeEncoder {
public:
	typedef std::vector< std::pair<StateVar*,int> > Interpretation;

	CubeEncoder();
	CubeEncoder(EncodingManager&,int);
	CubeEncoder(const CubeEncoder&);
	CubeEncoder& operator=(const CubeEncoder&);
	bool operator==(const CubeEncoder&) const;

	void encodeState(const Cube& c, bdd::BDD& result);
	void push(StateVar* v, int instance, const CVC3::Expr&);
	void resize (int size) { inter.resize(size); }
	std::pair<StateVar*,int>& operator[](int i) { return inter[i]; }
	const Interpretation& getInterpretation() const { return inter; }
	EncodingManager& getEncodingManager() const { assert(em); return *em; }
	std::string toString() const;
	bdd::BDD getSupport() const { return support; }
	int getMaxInstance() const { return max_instance; }
	const DecodingMap& getDecodingMap() const { return dmap; }
protected:
	friend class SymbolicCluster;
	void encodeTransition(const Cube& c, bdd::BDD& present, bdd::BDD& bdd) const;
	EncodingManager* em;

	Interpretation inter;
	std::vector<bdd::BDD> inter_bdd;
	int max_instance;
	bdd::BDD support;
	/** map BDD variable to corresponding expression */
	DecodingMap dmap;
};

/** \brief takes multiple encodings of cubes and converts them into symbolic matrix */
class SymbolicCluster {
public:
	typedef std::unordered_map<bdd::BDD,std::unordered_set<bdd::BDD> > Collection;

	/* construction */
	SymbolicCluster();
	SymbolicCluster(const SymbolicCluster& sc) { *this = sc; }
	SymbolicCluster(const CubeEncoder& __ce);
	SymbolicCluster(std::pair<EncodingManager&,int>);
	SymbolicCluster& operator=(const SymbolicCluster&);

	/* manipulation */
	void makeTop();
	void addTransition(const Cube& c);

	/* comparison */
	bool operator==(const SymbolicCluster&) const;
	inline bool operator!=(const SymbolicCluster& rhs) const { return !(*this == rhs); }

	/*! \brief simplify a BDD to just the variables that appear in the support of the SymbolicCluster */
	bdd::BDD Simplify(const bdd::BDD& f) const;

	/* getters and setters */
	bdd::BDD         getTransitions() const ;
	CubeEncoder&     getCubeEncoder() { return ce; }
	EncodingManager& getEncodingManager() const { assert(em); return *em; }
	int              getMaximalSetSize() const;
	bdd::BDD         getTrans01() const { assert(!trans01.isNull()); return trans01; }

	/* output */
	std::string toString(const Collection&) const;
	std::string toString() const;
protected:
	friend class SymbolicAbstractCommand;
	bdd::BDD computeBDD(EncodingManager& em, const ChoiceRange& cr);
	void addBDD(const bdd::BDD& prefix, const bdd::BDD& t);

	bdd::BDD encodeSet(const std::unordered_set<bdd::BDD>& s, EncodingManager& em, const ChoiceRange& cr) const ;
	bdd::BDD combineTransitions(EncodingManager& em, const ChoiceRange& cr);

	EncodingManager* em;
	CubeEncoder ce;
	Collection sc;
	/** (non-probabilistic) transition relation BDD */
	bdd::BDD trans01;
	bool top; // top element: all transitions
};



class NondetMatrix {
public:
	unsigned nr_of_branches;
	std::vector<double> distr;
	std::vector<unsigned> starts;
	std::vector<unsigned> succ;

	void traverseMatrix ( DdManager *ddman,
			      DdNode *dd,
			      const std::vector<bdd::BDD>& rvars,
			      const std::vector<bdd::BDD>& cvars,
			      int num_vars,
			      int level,
			      bdd::OddNode *row,
			      bdd::OddNode *col,
			      int r,
			      int c,
			      int branch);

	void traverseRow ( DdManager *ddman,
			   DdNode *dd,
			   const std::vector<bdd::BDD>& vars,
			   int num_vars,
			   int level,
			   bdd::OddNode *odd,
			   int i,
			   int code);

	void splitBDD( DdManager* ddman,
		       DdNode *dd,
                       const std::vector<bdd::BDD>& ndvars,
		       int num_ndvars,
		       std::vector<bdd::BDD>& matrices,
		       int& count,
		       int level);
};



/** \brief class that combines symbolic clusters into a single symbolic matrix */
class SymbolicAbstractCommand {
public:
	typedef std::vector<SymbolicCluster> Collection;
	typedef std::vector<double> Signature;
	typedef std::pair<StateVar* const,std::set<int> > VariableInstancePair;
	typedef std::map<StateVar*,std::set<int>, ltStateVarPtr > VariableInstances;

	/* constructors */
	SymbolicAbstractCommand();
	SymbolicAbstractCommand(EncodingManager&);
	SymbolicAbstractCommand(EncodingManager&, const Signature&);
	SymbolicAbstractCommand(const SymbolicAbstractCommand&);
	SymbolicAbstractCommand& operator=(const SymbolicAbstractCommand& sac);


	/* conversion */
	void computeMatrixStats(const std::vector<bdd::ODD2*>&, int& n, int& nc, int& nnz, int& nm) const;
	void toMatrix(const std::vector<bdd::ODD2*>& odd_vec,NondetMatrix& matrix) const;

	/* getters */

	bdd::BDD   getTransitionsOfBranch(int branch) const;
	bdd::BDD   getPost(const bdd::BDD& in, int branch) const;
	bdd::BDD   getPre(const bdd::BDD& in, int branch) const;

	void constrainTransitions(const bdd::BDD& c) { transitions &= c; }

	bdd::BDD   getTransitions      		      () const;
	bdd::BDD   getTrans01			      () const { return trans01; }
	bdd::MTBDD getTransitionsMTBDD 		      (bool quantify_out_prob_choice) const;

	const StateVar& getStateVar(unsigned instance, unsigned index) const {
		assert(instance < state_vars.size());
		assert(index < state_vars[instance].size());
		return *state_vars[instance][index]; }

	bdd::BDD   getGuard            		      () const;
	void       setGuard                           (const bdd::BDD& g) { guard = g; }
	EncodingManager& getEncodingManager           () const { assert(em); return *em; }
	const VariableInstances& getVariableInstances () const { return care; }
	const Signature& getSignature                 () const { return sig;  }
	const unsigned getNumberOfBranches            () const { return sig.size(); }
	unsigned getNumberOfDistributions               () const ;

	int getMaxNondetVar			      () const { return upper_nondet; }
	int getMinNondetVar			      () const { return lower_nondet; }

	/* setters */
	void setEncodingManager(EncodingManager& __em) { em = &__em; }
	void setSignature(const Signature& __sig) { sig = __sig; }
	void registerCubeEncoder      (const CubeEncoder&);
	void forceUnusedChoiceVarsToZero(int);
	void clear();

	/* trigger computations */
	/** \brief compute transition relation (with probabilities given externally)
	 *  \note  (1) the BDD contains probabilistic choice variables
	 *         (2) if constrain = false does not do conjunction with identity updates or guards
	 */
	bdd::BDD   computeBDD          (std::vector<SymbolicCluster>& scc, bool constrain = true);

	/** \brief compute transition relation
      *  \note  the BDD contains probabilistic choice variables
	  */
	bdd::MTBDD computeTransitionsMTBDD ();

	/** \brief compute qualitative transition relation
	  *  \note the BDD does not contain probabilistic choice variables
	  */
	bdd::BDD computeTrans01(const std::vector<SymbolicCluster>& scc);

protected:
	bdd::BDD getIdentityUpdates () const;
	bdd::BDD combineClusters    (std::vector<SymbolicCluster>& scc);
	void trackVariableInstance    (StateVar*,int);

	void computeVars();

	bdd::BDD forceUnusedChoiceVarsToZeroBDD(int) const;
	bdd::MTBDD forceUnusedChoiceVarsToZeroMTBDD(int) const;

	EncodingManager* em;
	Signature sig;
	bdd::BDD guard;
	bdd::BDD transitions;
	bdd::MTBDD transitions_mtbdd;
	VariableInstances care;
	std::vector<bdd::BDD> ndvars;
	std::vector<std::vector<bdd::BDD> > vars;
	std::vector<std::vector<StateVar*> > state_vars;

	int lower_nondet, upper_nondet; // interval in which the nondeterministic choice vars lie
	bdd::BDD trans01;

	bdd::BDD trans_with_prob;
};

class SymbolicAbstractModel {
public:
	typedef std::vector<SymbolicAbstractCommand*> Collection;
	SymbolicAbstractModel(EncodingManager& __em, unsigned nr_of_commands);
	~SymbolicAbstractModel();

	bdd::BDD getTransitionMatrixBDD() const { return transition_matrix; }
	bdd::MTBDD computeTransitionMatrixMTBDD(Collection&,bool quantify_out_prob_choice = true);
	bdd::BDD computeTransitionMatrixBDD(Collection&);
	EncodingManager& getEncodingManager() const { assert(em); return *em; }

	const std::vector<bdd::BDD>& getNondetVars() const { return nondet_vars; }

	int getNumberOfVariables() const { return nr_of_vars; }

//	std::vector<bdd::MTBDD> summands;
protected:
	void preprocessCommands(Collection& commands);

	int getMaxNondetVar(const Collection& commands) const;
	void computeNondetVars(int);

	EncodingManager* em;
	int nr_of_commands;
	ChoiceRange interleaving;
	bdd::BDD transition_matrix;
	std::vector<bdd::BDD> nondet_vars;
	int nr_of_vars;
};

} // end of namespace

#endif
