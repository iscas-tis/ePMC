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
#include "bdd/BDD.h"
#include "bdd/ODD.h"
#include "EncodingManager.h"
#include "Decomposition.h"
#include "pred/CFGCommand.h"

using namespace dp;
using namespace bdd;
using namespace lang;

namespace pred {

extern EncodingManager dummy_em;
lang::Command dummy_command;

CFGCommand::CFGCommand() : command(dummy_command), em(dummy_em) {}


CFGCommand::CFGCommand(
	Command& __gt,
	EncodingManager& __em,
	const std::vector<CVC3::Expr>& __invar)
	: command (__gt), em(__em), sac(em),
	log_completion(true),
	invar(__invar)
{
	SymbolicAbstractCommand::Signature sig(command.getAlternatives().size());
	for(unsigned j = 0; j<command.getAlternatives().size();++j) {
		assert(command.getAlternatives()[j]);
		const lang::Alternative& a = *command.getAlternatives()[j];
		sig[j] = a.getRateAsDouble();
	}
	sac.setSignature(sig);
}

/**
 * Check if the new predicates appear in any relevant set of predicates
 */
bool CFGCommand::changeInRelevant(
	const PredSet& new_preds) const {
	if(sac.getGuard().isNull()) return true;

	bool result = false;
	for(unsigned i=0;!result && i<clusters.size();++i) {
		const Decomposition& c(clusters[i]);
		for(unsigned j=0;!result && j<new_preds.size();++j)
			if(c.rel.find(new_preds[j])!=-1) {
				result = true;
			}
	}
	return result;
}


void CFGCommand::Update() {

	std::vector<Decomposition> old_clusters = clusters;

	CVC3::Expr e;

	for(unsigned i=0; i<predset_vec.size();++i) {
		MSG(1,"CFGCommand::Update: " + predset_vec[i]->toString() + "\n")

	}


	// compute clusters
	Decomposition::computeDecompositions(
			invar,
			command,
			predset_vec,
			clusters,
			e);

	log_completion = true;
	sac_done = false;
	cluster_done.resize(0);
	cluster_done.resize(clusters.size(),false);

	bdd::BDD old_trans(sac.getTransitions());
	CVC3::Expr guard_expr(command.getGuard());

	sac.clear();

	sac.setGuard( em.getDDManager().True() );



	const int nr_of_assignments = command.getAlternatives().size();

	// symbolic clusters
	symbolic_clusters.clear();
	symbolic_clusters.resize(clusters.size(),std::pair<EncodingManager&,int>(em,nr_of_assignments));

	EncodingManager& em(sac.getEncodingManager());

	util::Statistics::extractPreTimer.Start();

    MSG(1,"CFGCommand::Update: clusters size: %d\n",clusters.size());
    if(clusters.size()==0) {
		symbolic_clusters.resize(1,std::pair<EncodingManager&,int>(em,nr_of_assignments));
		symbolic_clusters[0].makeTop();
		sac_done = true;
	} else
	for(unsigned f=0;f<clusters.size();++f) {
		Decomposition& cluster = clusters[f];
		DecompositionCache::const_iterator cit (cluster_cache.find(cluster));
		SymbolicCluster& scluster = symbolic_clusters[f];
		CubeEncoder& cube_encoder(scluster.getCubeEncoder());

		MSG(1,"CFGCommand::Update: cluster" +  cluster.toString()+" \n")

		/* check if we have already computed the abstraction of this cluster
		   case 1: re-use
		   case 2: re-compute
		*/
		if(cit != cluster_cache.end()) {
			scluster = cit->second;
			logClusterReport(f,true);
		} else {
			const unsigned cluster_rel_size = cluster.rel.size();
		    const unsigned N = cluster.getNrOfPredicates();
			if(N == 0) {
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
				const lang::Alternative& a = command[i];
				const PredSet& preds(cluster.mod_vec[i]);
				/** go through the different predicates */
				for(int j = preds.size()-1; j>-1; --j) {
					const Predicate& pred = preds[j];
					CVC3::Expr wp = a(pred.getExpr());
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
				bdd::BDD trans_simp(scluster.Simplify(old_trans));
				cluster.learned_constraint = em.getExpr(trans_simp,cube_encoder.getDecodingMap());
			} else {
				cluster.learned_constraint = lang::vc.trueExpr();
			}


			logClusterReport(f,false);
		}
		sac.registerCubeEncoder(cube_encoder);
	}
	util::Statistics::extractPreTimer.Stop();

}

bdd::BDD CFGCommand::getAbstractPost() {
	if(sac.getTransitions().isNull())
		sac.computeBDD(symbolic_clusters,false);
	return sac.getTransitions();
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
	\param command    Command to be abstracted
	\param agt   Abstraction of the input Command
	\param from  abstractCover states for which to compute out-going transitions
	\param limit Maximal number of SMT enumerations
	\param done  Clusters for which enumeration could be completed
	\return successor relation
*/
bdd::BDD CFGCommand::computeAbstractPost(const bdd::BDD& from,
				    unsigned limit) {

/*
	if(from <= !sac.getGuard()) {
		//for(unsigned f=0;f<clusters.size();++f)
		//	logClusterReport(f,true);
		//MSG(0,"guard not fulfilled\n")
		//assert(!sac.getGuard().isFalse());
		return em.getDDManager().False();
	}
	*/
	//if(isDone()) {
	//	return getAbstractPost();
	//}

	util::Statistics::extractGCommandsTimer.Start();

	MSG(1,"======================================================\n")
	MSG(1,"CFGCommand::AbstractPost: command "+command.toString()+"\n");

	bool empty(false);

	/* precomputations to localize the computation */
	//ComputeLocalPreds(command, local_predset);

	for(unsigned f=0;f<clusters.size();++f) {
		if(isClusterDone(f)) continue;
		const Decomposition& cluster = clusters[f];
		SymbolicCluster& scluster = symbolic_clusters[f];

		dp::YicesSMT smt;

        //context of the current guarded transition
		std::vector<CVC3::Expr> pre;

		if(!cluster.learned_constraint.isTrue()) {
			MSG(1,"CFGCommand::computeAbstractPost: learned_constraint " + cluster.learned_constraint.toString()+"\n")
			pre.push_back(cluster.learned_constraint);
		}

		CVC3::Expr restriction(em.getExpr2(scluster.Simplify(from)));

		MSG(1,"CFGCommand::AbstractPost: cluster "+cluster.toString()+"\n");
		MSG(1,"CFGCommand::AbstractPost: restriction "+restriction.toString()+"\n");
		pre.push_back(restriction);
		pre.insert(pre.end(),cluster.invar.begin(),cluster.invar.end());
		pre.push_back(cluster.guard);

		/** restrict to states of interest */
		smt.Assert(ExprManager::Conjunction(pre));


		empty = empty || !smt.isOkay();
		bool within_limit(true);
		if(!empty) {
			/** allSat enumeration */
			SymbolicCubeConsumer cube_consumer ( scluster );
			within_limit = smt.allSat( cluster.transition_constraints, cube_consumer, limit);
		}

		logClusterReport(f,within_limit);
	} // end (for each cluster)

	/** ... done enumerating transitions */

	bdd::BDD local_trans01(sac.computeBDD(symbolic_clusters,false));

	assert(!sac.getTransitions().isNull());

	if(local_trans01.isFalse()) {
		MSG(1,"empty relation\n");
	}

	int n, nc, nnz, nm;
	sac.computeMatrixStats(odd_vec, n, nc, nnz, nm);
	MSG(1,"CFGCommand::AbstractPost: n %d nc %d nnz %d nm %d\n",n,nc,nnz,nm);
	MSG(1,"CFGCommand::AbstractPost: command "+command.toString()+"\n");
	MSG(1,"======================================================\n");
	util::Statistics::extractGCommandsTimer.Stop();


	/** return the abstract transitions */
	return local_trans01;
}

bdd::BDD CFGCommand::computeAbstractPost(unsigned limit) {
	bdd::BDD from(em.getDDManager().True());
	return computeAbstractPost(from,limit);
}


void CFGCommand::setLogCompletion(bool value) {
		log_completion = value;
	}

void CFGCommand::logClusterReport(int f, bool result) {

	if(log_completion) {
		cluster_done[f] = result;
		if(result) {
			std::pair<Decomposition,SymbolicCluster> p(clusters[f],symbolic_clusters[f]);
			cluster_cache.insert(p);
		}
	}
}

bool CFGCommand::isClusterDone(int f) const {
	return cluster_done[f];
}

bool CFGCommand::isDone() {

	bool summary = true;
	for(unsigned int c=0;c<clusters.size();++c) {
		summary &= cluster_done[c];
	}
	sac_done = summary;
	return sac_done;
}


void CFGCommand::Finalize() {
	setLogCompletion(true);
	for(unsigned int c=0;c<clusters.size();++c) {
		logClusterReport(c,true);
	}
	assert(!sac.getTransitions().isNull());
}

CFGCommand& CFGCommand::operator=(const CFGCommand& rhs) {
	command = rhs.command;
	em = rhs.em;
	residual_guard = rhs.residual_guard;
	clusters = rhs.clusters;
	symbolic_clusters = rhs.symbolic_clusters;
	sac = rhs.sac;
	invar = rhs.invar;
	return *this;
}

}
