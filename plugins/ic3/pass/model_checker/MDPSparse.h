#ifndef MDP_SPARSE_H
#define MDP_SPARSE_H

#include "Witness.h"

namespace model_checker {

typedef std::vector<Witness> Witnesses;

struct DFSEntry {
	unsigned comp;         //component
	unsigned root;         //root
	unsigned discover_time;//discovery time
	unsigned dfs_time;     //DFS time
};

typedef std::vector<DFSEntry> DFSMap;

struct MDPSparse
{

	MDPSparse(int max_iters, int term_crit, double term_crit_param);

	MDPSparse(
			bdd::DDManager& dd_mgr,
			const bdd::MTBDD& matrix,
			const std::vector<bdd::MTBDD>& rvars,
			const std::vector<bdd::MTBDD>& cvars,
			const std::vector<bdd::MTBDD>& ndvars,
			const std::vector<bdd::MTBDD>& pvars,
			int num_interleaving_choice_vars,
			const bdd::ODD& odd,
			int max_iters, int term_crit, double term_crit_param);

	/*
	 * Check if a state is refinable w.r.t. an action
	 */
	bool isRefinable(unsigned state,
			unsigned action) const;

	void getWitnessFromState(
			unsigned state,
			unsigned lower_action,
			unsigned upper_action,
			Witness& witness) const;

	void computeWitnesses(
			unsigned state,
			unsigned lower_choice,
			unsigned upper_choice,
			const std::vector<double> & lower_soln,
			const std::vector<double> & upper_soln,
			std::vector<Witness>& witness) const;

	void computeWitnesses(
			const std::vector<double>& init_vec,
			const std::vector<double>& yes_vec,
			const std::vector<double>& lower_soln,
			const std::vector<double>& upper_soln,
			const std::vector<int>& lower_chosen,
			const std::vector<int>& upper_chosen,
			Witnesses& witnesses
	) const;

	void computeGraph (
		bool min,
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		const std::vector<double>& lower_soln,
		const std::vector<double>& upper_soln,
		const std::vector<int>& chosen,
		ActionGraph& graph,
		bool smart = true) const;

	void computeGraph (
		bool min,
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		const std::vector<double>& lower_soln,
		const std::vector<double>& upper_soln,
		const std::vector<int>& lower_chosen,
		const std::vector<int>& upper_chosen,
		ActionGraph& graph,
		bool smart = true);

	void Until (
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		bool min,
		std::vector<double>& soln,
		std::vector<int> &chosen) const;

	/*! \note uses must information */
	void Until (
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		bool min1,
		bool min2,
		std::vector<double>& soln,
		std::vector<int> &chosen) const;

	void Until (
		const std::vector<unsigned>& order,
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		bool min1,
		bool min2,
		std::vector<double>& soln,
		std::vector<int> &chosen) const;


	void IntervalUntil (
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		bool min,
		std::vector<double>& lower_soln,
		std::vector<double>& upper_soln,
		std::vector<int>& lower_chosen,
		std::vector<int>& upper_chosen);

	void QualitativeUntil(
			const std::vector<std::vector<unsigned> >& back_set,
			const std::vector<double>& init_vec,
			const std::vector<double>& yes_vec,
			bool min1,
			bool min2,
			std::vector<double>& soln,
			std::vector<int> &chosen);


	double CTBoundedUntil (
		const std::vector<double>& init_vec,
		std::vector<double>& yes_vec,
		bool min,
		std::vector<double>& soln,
		std::vector<int> &chosen,
		double time);

        void calculate_P_s_alpha_B
	(const std::vector<double> &,
		   std::vector<double> &,
	 const std::vector<double> &) const;



	/** \brief convert MTBDD to sparse matrix
	    \note the MTBDD contains both probabilistic and nondeterministic choice variables
	*/
	static void buildMDPSparse (
		bdd::DDManager& dd_mgr,
		const bdd::MTBDD& matrix,
		const std::vector<bdd::MTBDD>& rvars,
		const std::vector<bdd::MTBDD>& cvars,
		const std::vector<bdd::MTBDD>& ndvars,
		const std::vector<bdd::MTBDD>& pvars,
		int num_interleaving_choice_vars,
		const bdd::ODD& odd,
		MDPSparse& sm);

	bool sanityCheck(const bdd::ODD& odd) const;


	/** \brief computes a sparse matrix, then computes the unbounded until on it
	    \param dd_mgr decision diagram manager
	    \param odd    ODD converter from symbolic to explicit-state
	    \param rvars  present-state (rows) variables
	    \param cvars  next-state (columns) variables
	    \param ndvars nondeterminism variables
            \param num_interleaving_choice_vars number of commands
	    \param init   initial state
	    \param trans  transition matrix
	    \param yes    yes states
	    \param maybe  maybe states
	    \param min    min/max
	    \param graph  graph into which optimal scheduler will be put
	    \return       optimal result
	*/
	void UnboundedUntil (
		bdd::DDManager& dd_mgr,
		bdd::ODD& odd,
		bdd::MTBDD &init,
		bdd::MTBDD &yes,
		bdd::MTBDD &maybe,
		bool min,		// min or max probabilities (true = min, false = max)
		ActionGraph &graph,
		Witnesses&,
		double& lower,
		double& upper);

	void BoundedUntil (
		bdd::DDManager& dd_mgr,
		bdd::ODD& odd,
		bdd::MTBDD &init,
		bdd::MTBDD &yes,
		bdd::MTBDD &maybe,
		bool min,		// min or max probabilities (true = min, false = max)
		ActionGraph &graph,
		Witnesses&,
		double& lower,
		double& upper,
		unsigned bound);

	/** \brief computes a sparse matrix, then computes the unbounded until on it
	    \param dd_mgr decision diagram manager
	    \param odd    ODD converter from symbolic to explicit-state
	    \param rvars  present-state (rows) variables
	    \param cvars  next-state (columns) variables
	    \param ndvars nondeterminism variables
            \param num_interleaving_choice_vars number of commands
	    \param init   initial state
	    \param trans  transition matrix
	    \param yes    yes states
	    \param maybe  maybe states
	    \param min    min/max
	    \param time   time bound
	    \param graph  graph into which optimal scheduler will be put
	    \return       optimal result
	*/
	static double CTBoundedUntil (
		bdd::DDManager& dd_mgr,
		bdd::ODD& odd,
		std::vector<bdd::MTBDD> &rvars,
		std::vector<bdd::MTBDD> &cvars,
		std::vector<bdd::MTBDD> &ndvars,
		std::vector<bdd::MTBDD> &pvars,
		int num_interleaving_choice_vars,
		bdd::MTBDD &init,
		bdd::MTBDD &trans,
		bdd::MTBDD &yes,
		bdd::MTBDD &maybe,
		bool min,		// min or max probabilities (true = min, false = max)
		double time,
		ActionGraph &graph);

	void toDOT(std::ostream& stream, bdd::ODD& odd, std::vector<double>& init_vec, std::vector<double>& yes_vec) const;

	unsigned n;			// num states
	unsigned nc;			// num choices
	unsigned nnz;		// num non zeros
	unsigned nm; 		// num matrices (upper bound on max num choices in a state)
	unsigned k;			// max num choices in a state
	bool use_counts;	// store counts? (as opposed to starts)
	double mem;		// memory used

	std::vector<double>        non_zeros;
	std::vector<unsigned int>  cols;
	std::vector<unsigned char> row_counts;
	std::vector<unsigned>      row_starts;
	std::vector<unsigned char> choice_counts;
	std::vector<unsigned>      choice_starts;
 	std::vector<long>          actions;
	std::vector<int>           updates;

	int max_iters;
	int term_crit;
	double term_crit_param;
	std::vector<double> init_vec;

	/* visualization info */
	int pivot_state;
	std::vector<std::string> state_info;
	std::vector<std::string> action_info;


	/* states included in current scheduler */
	std::vector<bool> include;

	void computePre(std::vector<std::vector<unsigned> >& pre) const;

	void traverse_mtbdd_matr_rec (
		DdManager *ddman,
		DdNode *dd,
		const std::vector<bdd::MTBDD>& rvars,
		const std::vector<bdd::MTBDD>& cvars,
		int num_vars,
		int level,
		bdd::OddNode *row,
		bdd::OddNode *col,
		int r,
		int c,
		int code,
		bool transpose,
		long action,
		int  branch );

	void traverse_mtbdd_vect_rec (
		DdManager *ddman,
		DdNode *dd,
		const std::vector<bdd::MTBDD>& vars,
		int num_vars,
		int level,
		bdd::OddNode *odd,
		int i,
		int code );

	static
	void split_mdp_rec(
		DdManager* ddman,
		DdNode *dd,
		const std::vector<bdd::MTBDD>& ndvars,
		int num_ndvars,
		std::vector<bdd::MTBDD>& matrices,
		int& count,
		int level = 0
		);

	static
	void compute_nondet_actions (
		DdManager *ddman,
		DdNode *dd,
		const std::vector<bdd::MTBDD>& ndvars,
		int num_ndvars,
		std::vector<long>& choices,
		int& count,
		int highest_level,  // explore up to that level
		long choice = 0,
		long interleaving_choice = -2,
		int level = 0);

	void aiSee(std::ostream& stream,
		   bdd::ODD& odd,
		   std::vector<double>& init_vec,
		   std::vector<double>& yes_vec,
		   const std::vector<double>& lower_soln,
		   const std::vector<double>& upper_soln,
		   const std::vector<int>& lower_chosen,
		   const std::vector<int>& upper_chosen
		) const;

	void computeReverseDFSOrder
					   (const std::vector<std::vector<unsigned> >& back_set,
						const std::vector<double>& yes_vec,
						std::vector<unsigned>& order);

	std::vector<unsigned> rdfs_order;


	void Until (
		const std::vector<unsigned>& order,
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		bool min,
		std::vector<double>& soln,
		std::vector<int> &chosen) const;

	void UntilCheck (
		const std::vector<unsigned>& order,
		const std::vector<double>& init_vec,
		const std::vector<double>& yes_vec,
		bool min,
		std::vector<double>& soln,
		std::vector<int> &chosen) const;

	void UntilCheck (
			const std::vector<double>& init_vec,
			const std::vector<double>& yes_vec,
			bool min,
			std::vector<double>& soln,
			std::vector<int> &chosen) const;

	/*! Tarjan's SCC algorithm */
	void strongComponents();

	DFSMap dfs_map;

	std::vector<std::vector<unsigned> > strong_components;
};

}

#endif

