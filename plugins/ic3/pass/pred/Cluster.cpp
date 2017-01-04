#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include <fstream>
#include <algorithm>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"
#include "util/Cube.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"
#include "dp/SMT.h"
#include "dp/YicesSMT.h"

#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"
#include "EncodingManager.h"
#include "Cluster.h"
#include "AbsModel.h"
#include "AbsModelImpl.h"

using namespace pred;
using namespace lang;
using namespace dp;

Cluster::Cluster(const CVC3::Expr& __guard,
	const PredSet&    __mod,
	const PredSet&    __rel,
	const std::vector<CVC3::Expr>& __invar)
:	guard(__guard), mod(__mod), rel(__rel), invar(__invar) {}

Cluster& Cluster::operator=(const Cluster& f) {
	Set(f.guard, f.mod, f.rel, f.invar);
	transition_constraints = f.transition_constraints;
	return *this;
}

void Cluster::Set(const CVC3::Expr& __guard,
	const PredSet&    __mod,
	const PredSet&    __rel,
	const std::vector<CVC3::Expr>& __invar) {
	guard = __guard; mod = __mod; rel = __rel; invar = __invar;
}



/* implementation of Cluster */
Cluster::Cluster() : guard(vc.trueExpr()) {}

//compute clusters for a guarded command given a set of predicates
void Cluster::computeClusters(const std::vector<CVC3::Expr>& invariants, // invariants of the command
			      const Command& gt,    // the command
			      const PredSet& preds,           // set of predicates
			      std::vector<Cluster>& result,
			      CVC3::Expr& e,
			      bool CartesianAbstraction) {

	result.clear();
	PredSet mod_preds;

	for(unsigned i = 0; i<preds.size();++i) {
		const Predicate& p = preds[i];
		if(isModified(gt,p)) {
			mod_preds.Add(p);
		}
	}

	if(CartesianAbstraction) {
		std::set<CVC3::Expr> gset;
		for(unsigned i = 0; i<mod_preds.size();++i) {
			PredSet singleton;
			singleton.Add(mod_preds[i]);
			CVC3::Expr g;
			Cluster::computeClusters(invariants, gt, preds, singleton, result, g);
			gset.insert(g);
		}
		e = ExprManager::Conjunction(gset);
	} else {
		Cluster::computeClusters(invariants, gt, preds, mod_preds, result, e);
	}

}

void getSupportVec(const CVC3::Expr&  e,
		   const std::vector<CVC3::Expr>& table,
		   Signature<unsigned>::boolVector& support) {
	if(support.size()!=table.size()) {
		support.resize(table.size(),false);
	}
	for(unsigned k=0;k<table.size();++k) {
		bool b = ExprManager::LvalueCompatible(table[k],e);
		support[k] = b;
	}
}

//compute clusters for a guarded command given a set of predicates
void Cluster::computeClusters(const std::vector<CVC3::Expr>& invariants, // invariants of the command
			      const Command& gt,    // the command
			      const PredSet& preds,           // set of predicates
			      const PredSet& mod_preds,
			      std::vector<Cluster>& result,
			      CVC3::Expr& e) {


	util::Timer part_ref_timer;
	util::Timer part_ref_pre_timer;
	util::Timer part_ref_post_timer;

	part_ref_pre_timer.Start();

	// get the support
	std::set<CVC3::Expr> support;

	ExprManager::ComputeLvalues(gt.getGuard(), support);

	// weakest preconditions
	const lang::Alternatives& asss = gt.getAlternatives();
	for(unsigned i = 0; i<mod_preds.size();++i) {
		const CVC3::Expr& expr = mod_preds[i].getExpr();
		for(size_t i = 0; i < asss.size(); ++i) {
			Alternative& assig = *(asss[i]);
			CVC3::Expr wp = assig(expr);
			ExprManager::ComputeLvalues(wp, support);
		}
	}

	// predicates
	for(unsigned i = 0; i<preds.size();++i) {
		const Predicate& p = preds[i];
		ExprManager::ComputeLvalues(p.getExpr(),support);
	}

	std::vector<CVC3::Expr> table(support.size());
	int counter = 0;
	foreach(const CVC3::Expr& e, support) {
		table[counter++] = e;
	}

	typedef Signature<unsigned int> Sig;

	std::vector<CVC3::Expr> guard_conj;
		// conjuncts of the guards
		ExprManager::getTopLevelConjuncts(gt.getGuard(),guard_conj);

	std::vector<Sig> partition       (invariants.size() +
									  guard_conj.size() +
									  preds.size() +
									  mod_preds.size(),table.size());

	// invariants
	for(unsigned iv=0;iv<invariants.size();++iv) {
		const CVC3::Expr& e = invariants[iv];
		Sig& sig(partition[iv]);
		assert(sig.s.size() == table.size());
		sig.insert(iv);
		getSupportVec(e,table,sig.s);
	}

	unsigned int K1 = invariants.size();



	for(unsigned i=0; i<guard_conj.size();++i) {
		assert(i+K1 < partition.size());
		Sig& sig(partition[i+K1]);
		sig.insert(i+K1);
		getSupportVec(guard_conj[i],table,sig.s);
		assert(sig.s.size() == table.size());
	}

	unsigned int K2 = K1 + guard_conj.size();


	// potentially relevant predicates
	for(unsigned i = 0; i<preds.size();++i) {
		const Predicate& p = preds[i];
		const CVC3::Expr& e = p.getExpr();
		Sig& sig (partition[i+K2]);
		sig.insert(i+K2);
		getSupportVec(e,table,sig.s);
		assert(sig.s.size() == table.size());
	}

	unsigned int K3 = K2 + preds.size();

	// weakest preconditions
	for(unsigned i = 0; i<mod_preds.size();++i) {
		const CVC3::Expr& expr = mod_preds[i].getExpr();
		Sig& sig(partition[i+K3]);
		sig.insert(i+K3);
		for(size_t a = 0; a < asss.size(); ++a) {
			Alternative& assig = *(asss[a]);
			CVC3::Expr wp = assig(expr);

			if(a == 0) {
				getSupportVec(wp,table,sig.s);
			}
			else {
				Signature<unsigned>::boolVector support_vec;
				getSupportVec(wp,table,support_vec);
				Signature<unsigned>::Disjunction(sig.s,support_vec);
			}
		}
		assert(sig.s.size() == table.size());
	}

	std::vector<Sig> new_partition;

	part_ref_pre_timer.Stop();

	part_ref_timer.Start();
	Signature<unsigned int>::partitionRefinement2(partition, new_partition);
	part_ref_timer.Stop();

	part_ref_post_timer.Start();

	// guard conjuncts not needed by any cluster
	std::vector<CVC3::Expr> orphaned_guard_conj;

	// each element containing a modified predicate gives rise
	// to a cluster, remaining conjuncts of the guard are just collected
	foreach(Sig& sig, new_partition) {
		std::vector<CVC3::Expr> part_invar;
		std::vector<CVC3::Expr> part_guard_conj;
		PredSet part_mod;
		PredSet part_rel;

		foreach(unsigned int nr, sig.cset) {
			if (nr < K1) {
				part_invar.push_back(invariants[nr]);
			} else if( K1 <= nr && nr < K2) {
				part_guard_conj.push_back(guard_conj[nr - K1]);
			} else if( K2 <= nr && nr < K3) {
				part_rel.Add(preds[nr-K2]);
			} else {
				part_mod.Add(mod_preds[nr-K3]);
			}
		}

		// check if part_mod is empty
		if(part_mod.empty()) {
			orphaned_guard_conj.insert(orphaned_guard_conj.begin(),part_guard_conj.begin(),part_guard_conj.end());
			continue;
		}


		CVC3::Expr part_guard (ExprManager::Conjunction(part_guard_conj));

		// construct a cluster from current partition
		result.resize(result.size()+1);
		Cluster& cluster = result.back();
		cluster.Set(part_guard,part_mod,part_rel,part_invar);
	}
	e = ExprManager::Conjunction(orphaned_guard_conj);
	part_ref_post_timer.Stop();
	double sum = ( part_ref_timer.Read() + part_ref_pre_timer.Read() + part_ref_post_timer.Read());
	MSG(1,"Cluster::computeClusters %E %E %E \n", part_ref_timer.Read()/sum,part_ref_pre_timer.Read()/sum,part_ref_post_timer.Read()/sum)

}


std::string Cluster::toString() const {
	std::string result;
	result+="< \nrel = "+rel.toString()+" ,\nmod = "+mod.toString()+",";
	result+="\nINVAR: ";
	for(unsigned iv=0;iv<invar.size();++iv) {
		result+=invar[iv].toString()+" ";
	}
	return result;
}

bool Cluster::isModified(const lang::Command& gt, const Predicate& p) {
	CVC3::Expr expr(p.getExpr());
	const Alternatives& asss = gt.getAlternatives();
	for(size_t i = 0; i < asss.size(); ++i) {
		Alternative& assig = *(asss[i]);
		CVC3::Expr wp = assig(expr);

		if(expr != wp)
			return true;
	}
	return false;
}

size_t Cluster::hash() const {
	size_t hash = 0;
	hash = guard.hash() + (hash << 6) + (hash << 16) - hash;
	hash = mod.hash() + (hash << 6) + (hash << 16) - hash;
	hash = rel.hash() +  (hash << 6) + (hash << 16) - hash;
	return hash;
}

bool Cluster::operator==(const Cluster& c) const {
	return rel == c.rel && mod == c.mod && guard == c.guard;
}
