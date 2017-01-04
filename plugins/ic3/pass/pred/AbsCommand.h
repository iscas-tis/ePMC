/*! \file AbsCommand.h
    \brief Header file for AbsCommand classes.]
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
#ifndef __ABSCOMMAND_H__
#define __ABSCOMMAND_H__

namespace pred {

class AbsModel;

/*! \brief Abstraction of a command */
class AbsCommand {
	public:

	typedef HashMap<pred::Cluster,pred::SymbolicCluster> ClusterCache;
	ClusterCache cluster_cache;	
		
	AbsModel& am;

	const lang::Command& gt;
	EncodingManager& em;

	CVC3::Expr residual_guard;

	/* infrastructure for symbolic encoding */
	std::vector<Cluster> clusters;
	std::vector<SymbolicCluster> symbolic_clusters;

	SymbolicAbstractCommand sac;

	/*! \brief abstracts a guarded command by an abstract transition relation
	 * The abstraction is done w.r.t. the current predicate set in AbsModel::preds
	 *
	 * @see: AbsModel::preds
	 */
	bdd::BDD computeAbstractPost(const bdd::BDD& from, unsigned limit);
	bdd::BDD getAbstractPost();

	bool changeInRelevant(const PredSet& new_preds) const;
	bool changeInModified(const PredSet& new_preds) const;

	/*! \brief after refinement step: make ready for incremental exploration of abstract model
	    \post  clusters, symbolic_clusters, sac become usable */
	void Update(const PredSet& preds,
		    const PredSet& new_preds,
		    const std::vector<CVC3::Expr>& invar,
 		    const bdd::BDD& care_set);
		    	    
	/*! \brief finalize after incremental exploration of abstract model
	    \post the SymbolicAbstractCommand is ready to be used */
	void Finalize(const PredSet& preds);
	/*! \brief constructor */
	AbsCommand(AbsModel&,const lang::Command&);
	/*! \brief copy constructor */
	AbsCommand(const AbsCommand& rhs) : am(rhs.am), gt (rhs.gt) , em( rhs.em)
	{ *this = rhs; }
	/*! \brief assignment operator */
	AbsCommand& operator=(const AbsCommand& rhs);

	void setLogCompletion(bool value);
	void logClusterReport(int f, bool result);
	bool isClusterDone(int f) const;
	bool isDone();

	private:
	/**
		We use a heuristic that tries to compute,
		once and for all, the abstraction of a guarded command without
		using reachability information.
		Only if that fails, reachability information will be used.

		To keep track of what could be computed in this way
		and avoid re-computation, we use
		- symbolic_cluster_completed ... true <=> symbolic_cluster done
		- sac_completed              ... true <=> all symbolic_cluster done <=> command done

		The information is computed by
			AbsModelImpl::AbstractPost

	 */
	std::vector<bool> cluster_done;
	bool sac_done;
	bool log_completion;

};

}

#endif

