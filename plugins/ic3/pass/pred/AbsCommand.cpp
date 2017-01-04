#include "util/Util.h"
#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Cube.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"
#include "dp/SMT.h"
#include "dp/YicesSMT.h"
#include "dp/CVC3SMT.h"

#include "bdd/BDD.h"
#include "bdd/ODD.h"

#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"


#include "EncodingManager.h"

#include "Cluster.h"
#include "AbsExpression.h"
#include "AbsModel.h"
#include "AbsCommand.h"

#include <cmath>
#include "pred/TransitionConstraint.h"
#include "pred/CEAnalysis.h"

using namespace dp;
using namespace bdd;
using namespace model_checker;
using namespace lang;

namespace pred {


AbsCommand::AbsCommand(
	AbsModel& __am,
	const Command& __gt)
	: am(__am), gt (__gt), em(am.em), sac(em),
	log_completion(true)
{
	SymbolicAbstractCommand::Signature sig(gt.getAlternatives().size());
	for(unsigned j = 0; j<gt.getAlternatives().size();++j) {
		assert(gt.getAlternatives()[j]);
		const lang::Alternative& a = *gt.getAlternatives()[j];
		sig[j] = a.getRateAsDouble();
	}
	sac.setSignature(sig);
}

/**
 * Check if the new predicates appear in any relevant set of predicates
 */
bool AbsCommand::changeInRelevant(
	const PredSet& new_preds) const {
	if(sac.getGuard().isNull()) return true;

	bool result = false;
	for(unsigned i=0;!result && i<clusters.size();++i) {
		const Cluster& c(clusters[i]);
		for(unsigned j=0;!result && j<new_preds.size();++j)
			if(c.rel.find(new_preds[j])!=-1) {
				result = true;
			}
	}
	return result;
}

/**
 * Check if the new predicates appear in any modified set of predicates
 */
bool AbsCommand::changeInModified(
		const PredSet& new_preds) const {
	if(sac.getGuard().isNull()) return true;

	bool result = false;
	for(unsigned i=0;!result && i<clusters.size();++i) {

		const Cluster& c(clusters[i]);
		for(unsigned j=0;!result && j<new_preds.size();++j)
		if(c.mod.find(new_preds[j])!=-1) {
					result = true;
		}
	}

	return result;
}


void AbsCommand::Update(const PredSet& preds,
			const PredSet& new_preds,
			const std::vector<CVC3::Expr>& invar,
 		    const bdd::BDD& care_set) {
	// compute clusters



	/*
	 * The following two
	 *
	 */
	std::vector<Cluster> old_clusters = clusters;

	util::Statistics::extractPreTimer.Start();
	Cluster::computeClusters(invar,gt,preds,clusters,
			         residual_guard, util::Database::CartesianAbstraction);
	util::Statistics::extractPreTimer.Stop();

	MSG(1,"AbsCommand::Update: clusters.size() %d\n",clusters.size());

	bool rel_changed = changeInRelevant(new_preds);
	//bool mod_changed = changeInModified(new_preds);



	log_completion = true;
	sac_done = false;
	cluster_done.resize(0);
	cluster_done.resize(clusters.size(),false);


	bdd::BDD old_guard(sac.getGuard());
	bdd::BDD old_trans(sac.getTransitions());
	CVC3::Expr guard_expr(gt.getGuard());



	if(rel_changed) {
		sac.clear();
		util::Statistics::guardAbstractionTimer.Start();
		sac.setGuard(AbsExpression::abstractCoverDecomp(guard_expr, am.em, preds,care_set,am.invar));
		//am.abstractInterior(guard_expr,preds);
		util::Statistics::guardAbstractionTimer.Stop();
	} else {
		MSG(1,"AbsCommand::Update: no change in relevant predicates\n")
		sac.clear();
		sac.setGuard(old_guard);
	}
	const int nr_of_assignments = gt.getAlternatives().size();

	// symbolic clusters
	symbolic_clusters.clear();
	symbolic_clusters.resize(clusters.size(),std::pair<EncodingManager&,int>(em,nr_of_assignments));


	EncodingManager& em(sac.getEncodingManager());

	util::Statistics::extractPreTimer.Start();


    if(clusters.size()==0) {
		symbolic_clusters.resize(1,std::pair<EncodingManager&,int>(em,nr_of_assignments));
		symbolic_clusters[0].makeTop();
	    sac_done = true;
	} else
	for(unsigned f=0;f<clusters.size();++f) {
		Cluster& cluster = clusters[f];
		ClusterCache::const_iterator cit (cluster_cache.find(cluster));
		SymbolicCluster& scluster = symbolic_clusters[f];
		CubeEncoder& cube_encoder(scluster.getCubeEncoder());

		/* check if we have already computed the abstraction of this cluster
		   case 1: re-use
		   case 2: re-compute
		*/
		if(cit != cluster_cache.end()) {
			scluster = cit->second;
			logClusterReport(f,true);
		} else {
			const unsigned cluster_mod_size = cluster.mod.size();
			const unsigned cluster_rel_size = cluster.rel.size();
			const unsigned N = nr_of_assignments*cluster_mod_size + cluster_rel_size;

			MSG(1,"current cluster: "+cluster.toString()+"\n");
			if(cluster.mod.size() + cluster.rel.size() == 0) {
				scluster.makeTop();
				logClusterReport(f,true);
				sac.registerCubeEncoder(cube_encoder);
				continue;
			}

			/** ramp up the SMT solving process */

			//constraints in the SMT solver

			cluster.transition_constraints.clear();
			cluster.transition_constraints.reserve(N);

			/** generate constraints for the different probabilistic branches of the command */
			/** \note The reverse order of iteration is done to make BDD operations faster */
			for(int i = nr_of_assignments-1; i>-1;--i) {
				assert(gt.getAlternatives()[i]);
				lang::Alternative& a = *gt.getAlternatives()[i];
				/** go through the different predicates */
				for(int j = cluster_mod_size-1; j>-1; --j) {
					const Predicate& pred = cluster.mod[j];
					CVC3::Expr wp = a(cluster.mod[j].getExpr());
					cube_encoder.push(&em.getStateVar(pred),i+1,wp);
					cluster.transition_constraints.push_back(wp);
				}
			}
			/** compute the constraints for the current state */
			for(int i = cluster_rel_size-1; i>-1;--i) {
				const Predicate& pred = cluster.rel[i];
				const CVC3::Expr& e(pred.getExpr());
				cube_encoder.push(&em.getStateVar(pred),0,e);
				cluster.transition_constraints.push_back(e);
			}

			/*!
					 * optimization that re-uses transition relation of previous iteration
					 * to constrain cube enumeration in present iteration
					 */
			if(!old_trans.isNull()) {
				old_trans &= care_set;
				bdd::BDD trans_simp(scluster.Simplify(old_trans));
				cluster.learned_constraint = em.getExpr(trans_simp,cube_encoder.getDecodingMap());
				MSG(1,"AbsCommand::Update: "+ cluster.learned_constraint.toString() + "\n")
			} else {
				cluster.learned_constraint = lang::vc.trueExpr();
			}

			cluster.smt.initIncrementalAllSat( cluster.transition_constraints );

			std::vector<CVC3::Expr> pre;

			if(!cluster.learned_constraint.isTrue()) {
				MSG(1,"AbsCommand::computeAbstractPost: learned_constraint " + cluster.learned_constraint.toString()+"\n")
				pre.push_back(cluster.learned_constraint);
			}

			pre.insert(pre.end(),cluster.invar.begin(),cluster.invar.end());
			pre.push_back(cluster.guard);
			cluster.smt.Assert(ExprManager::Conjunction(pre));


			logClusterReport(f,false);
		}
		sac.registerCubeEncoder(cube_encoder);
	}
	util::Statistics::extractPreTimer.Stop();

}

bdd::BDD AbsCommand::getAbstractPost() {
	if(sac.getTrans01().isNull())
		sac.computeTrans01(symbolic_clusters);
	return sac.getTrans01();
}

struct SymbolicCubeConsumer : CubeConsumer {
   SymbolicCluster& sc;

public:
	SymbolicCubeConsumer( SymbolicCluster& __sc) : sc ( __sc) {}
	void consume(const Cube& c) {
		sc.addTransition(c);
	}
};

/**	\brief Compute abstraction of a guarded transition
	\param command_nr number of the command (for debugging purposes)
	\param gt    Command to be abstracted
	\param agt   Abstraction of the input Command
	\param from  abstractCover states for which to compute out-going transitions
	\param limit Maximal number of SMT enumerations
	\param done  Clusters for which enumeration could be completed
	\return successor relation
*/
bdd::BDD AbsCommand::computeAbstractPost(const bdd::BDD& from,
				    unsigned limit) {
	if(isDone()) {
		return getAbstractPost();
	}

	if(from <= !sac.getGuard()) {
		for(unsigned f=0;f<clusters.size();++f)
			logClusterReport(f,true);
		//MSG(0,"guard not fulfilled\n")
		//assert(!sac.getGuard().isFalse());
		return em.getDDManager().False();
	}


	util::Statistics::extractGCommandsTimer.Start();

	MSG(1,"AbsCommand::AbstractPost: command "+gt.toString()+"\n");
	/* precomputations to localize the computation */
	//ComputeLocalPreds(gt, local_predset);
	for(unsigned f=0;f<clusters.size();++f) {
		if(isClusterDone(f)) continue;
		Cluster& cluster = clusters[f];
		SymbolicCluster& scluster = symbolic_clusters[f];

        //        dp::SMT &smt(dp::SMT::getYices());

        //context of the current guarded transition
		CVC3::Expr restriction(em.getExpr2(scluster.Simplify(from)));



		MSG(1,"AbsCommand::AbstractPost: cluster "+cluster.toString()+"\n");
		MSG(1,"AbsCommand::AbstractPost: restriction "+restriction.toString()+"\n");

		if(!cluster.smt.isOkay()) {
			util::Statistics::extractGCommandsTimer.Stop();
			return em.getDDManager().False();
		}

                //constraints in the SMT solver
		MSG(1,"current cluster: "+cluster.toString()+"\n");

		//count the number of AllSMT enumerations
		/** allSat enumeration */
		SymbolicCubeConsumer cube_consumer ( scluster );


		bool within_limit = cluster.smt.incrementalAllSat( restriction , cube_consumer, limit);
		logClusterReport(f,within_limit);
	} // end (for each cluster)

	/** ... done enumerating transitions */

	bdd::BDD local_trans01(sac.computeTrans01(symbolic_clusters));

	if(local_trans01.isFalse()) {
		MSG(0,"empty relation\n");
	}

	util::Statistics::extractGCommandsTimer.Stop();
	/** return the abstract transitions */
	return local_trans01;
}


void AbsCommand::setLogCompletion(bool value) {
		log_completion = value;
	}

void AbsCommand::logClusterReport(int f, bool result) {

	if(log_completion) {
		cluster_done[f] = result;
		if(result) {
			std::pair<Cluster,SymbolicCluster> p(clusters[f],symbolic_clusters[f]);
			//cluster_cache.insert(p);
		}
	}
}

bool AbsCommand::isClusterDone(int f) const {
	return cluster_done[f];
}

bool AbsCommand::isDone() {

	bool summary = true;
	for(unsigned int c=0;c<clusters.size();++c) {
		summary &= cluster_done[c];
	}
	sac_done = summary;
	return sac_done;
}


void AbsCommand::Finalize(const PredSet& preds) {
	setLogCompletion(true);
	for(unsigned int c=0;c<clusters.size();++c) {
		logClusterReport(c,true);
	}

	MSG(1,"| # Guard %s\n",residual_guard.toString().c_str());
	bdd::BDD result_bdd = sac.computeBDD(symbolic_clusters);
	MSG(1,"| # BDD (minterms %E)\n",result_bdd.CountMinterm(2* preds.size()));
	sac.computeTransitionsMTBDD();
}

AbsCommand& AbsCommand::operator=(const AbsCommand& rhs) {
	return *this;
}

}
