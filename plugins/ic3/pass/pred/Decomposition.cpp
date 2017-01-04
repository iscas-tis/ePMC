#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"
#include "util/Cube.h"
#include "bdd/BDD.h"
#include "dp/SMT.h"

#include "Decomposition.h"
using namespace pred;
using namespace lang;


Decomposition::Decomposition(const Decomposition& c) { *this = c; }
Decomposition::Decomposition(const CVC3::Expr& __guard,
	const std::vector<PredSet>&    __mod_vec,
	const PredSet&    __rel,
	const std::vector<CVC3::Expr>& __invar) {
	Set(__guard,__mod_vec,__rel,__invar);
}

Decomposition& Decomposition::operator=(const Decomposition& f) {
	Set(f.guard,f.mod_vec,f.rel,f.invar);
	return *this;
}

void Decomposition::Set(const CVC3::Expr&         __guard,
	const std::vector<PredSet>&    __mod_vec,
	const PredSet&                 __rel,
	const std::vector<CVC3::Expr>& __invar) {
	guard = __guard;
	mod_vec.resize(__mod_vec.size());
	for(unsigned i=0;i < mod_vec.size(); ++i)
		mod_vec[i]   = __mod_vec[i];


	rel = __rel; invar = __invar;
}


/* implementation of Decomposition */
Decomposition::Decomposition() : guard(vc.trueExpr()) {}

void Decomposition::getSupportVec(const CVC3::Expr&  e,
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
void Decomposition::computeDecompositions(const std::vector<CVC3::Expr>& invariants, // invariants of the command
			      const Command& gt,    // the command
			      const std::vector<const PredSet*>& predset_vec,
			      std::vector<Decomposition>& result,
			      CVC3::Expr& e) {

	result.clear();

	/** \todo later optimize this

	std::vector<PredSet> mod_preds_vec(succ_preds.size());
	for(unsigned i=0; i<succ_preds.size(); ++i) {
		PredSet& mod_preds(mod_preds_vec[i]);
		for(unsigned i = 0; i<preds.size();++i) {
			const Predicate& p = preds[i];
				if(isModified(gt,p)) {
					mod_preds.Add(p);
				}
		}
	}
	*/


	// get the support
	std::set<CVC3::Expr> support;

	ExprManager::ComputeLvalues(gt.getGuard(), support);

	// weakest preconditions
	const lang::Alternatives& asss = gt.getAlternatives();
	for(unsigned i=1; i<predset_vec.size(); ++i) {
		const PredSet* mod_preds(predset_vec[i]);
		Alternative& assig = *(asss[i-1]);
		for(unsigned j = 0; j<mod_preds->size();++j) {
			const CVC3::Expr& expr = (*mod_preds)[j].getExpr();
			CVC3::Expr wp = assig(expr);
			ExprManager::ComputeLvalues(wp, support);
		}
	}

	// predicates
	for(unsigned i = 0; i<predset_vec[0]->size();++i) {
		const Predicate& p = (*predset_vec[0])[i];
		ExprManager::ComputeLvalues(p.getExpr(),support);
	}

	std::vector<CVC3::Expr> table(support.size());
	int counter = 0;




	for(std::set<CVC3::Expr>::const_iterator i=support.begin();i!=support.end();++i) {
		table[counter++] = *i;
	}

	typedef Signature<unsigned int> Sig;


	std::vector<CVC3::Expr> guard_conj;
	// conjuncts of the guards
	ExprManager::getTopLevelConjuncts(gt.getGuard(),guard_conj);

	unsigned size = guard_conj.size() + invariants.size();
	for(unsigned j=0;j< predset_vec.size(); ++j) {
		size += predset_vec[j]->size();
	}

	std::vector<Sig> partition(size,table.size());

	// invariants
	for(unsigned iv=0;iv<invariants.size();++iv) {
		const CVC3::Expr& e = invariants[iv];
		Sig& sig (partition[iv]);
		sig.insert(iv);
		getSupportVec(e,table,sig.s);
	}

	unsigned int K1 = invariants.size();



	for(unsigned i=0; i<guard_conj.size();++i) {
		Sig& sig (partition[i+K1]);
		sig.insert(i+K1);
		getSupportVec(guard_conj[i],table,sig.s);
	}

	unsigned int K2 = K1 + guard_conj.size();

	// potentially relevant predicates
	for(unsigned i = 0; i<predset_vec[0]->size();++i) {
		const Predicate& p = (*predset_vec[0])[i];
		const CVC3::Expr& e = p.getExpr();

		Sig& sig = partition[i+K2];
		sig.insert(i+K2);
		getSupportVec(e,table,sig.s);
	}

	std::vector<unsigned int> K3_vec (predset_vec.size());

	//initialize indices

	K3_vec[0] = K2 + predset_vec[0]->size();

	for(unsigned j=1;j< predset_vec.size(); ++j) {


		unsigned int K3(K3_vec[j-1]);
		const PredSet& mod_preds(*predset_vec[j]);
		// weakest preconditions
		Alternative& assig = *(asss[j-1]);
		for(unsigned i = 0; i<mod_preds.size();++i) {
			const CVC3::Expr& expr = mod_preds[i].getExpr();
			Sig& sig = partition[K3 + i];
			sig.insert(i+K3);
			CVC3::Expr wp = assig(expr);
			getSupportVec(wp,table,sig.s);
		}
		K3_vec[j] = K3 + mod_preds.size();
	}
	assert(K3_vec[predset_vec.size()-1] == partition.size());

	foreach(Sig& sig, partition)
	assert(sig.cset.size()==1);

	std::vector<Sig> new_partition;

	Signature<unsigned int>::partitionRefinement2(partition, new_partition);

	// each element containing a modified predicate gives rise
	// to a cluster, remaining conjuncts of the guard are just collected
	foreach(Sig& sig, new_partition) {
		std::set<unsigned int>& current_set (sig.cset);

		std::vector<CVC3::Expr> part_invar;
		std::vector<CVC3::Expr> part_guard_conj;
		std::vector<PredSet> part_mod_vec(predset_vec.size()-1);
		PredSet part_rel;

		unsigned nr_of_predicates(0);

		for(std::set<unsigned int>::iterator j=current_set.begin();j!=current_set.end();++j) {
			unsigned int nr (*j);
			if (nr < K1) {
				part_invar.push_back(invariants[nr]);
			} else if( K1 <= nr && nr < K2) {
				part_guard_conj.push_back(guard_conj[nr - K1]);
			} else if( K2 <= nr && nr < K3_vec[0]) {
				part_rel.Add((*predset_vec[0])[nr-K2]);
				++nr_of_predicates;
			} else {
				for(unsigned l=1;l<predset_vec.size();++l) {
					if(K3_vec[l-1] <= nr && nr < K3_vec[l]) {
						PredSet& part_mod(part_mod_vec[l-1]);
						const PredSet& mod_preds(*predset_vec[l]);
						part_mod.Add(mod_preds[nr-K3_vec[l-1]]);
						++nr_of_predicates;
					}
				}
			}
		}

		CVC3::Expr part_guard (ExprManager::Conjunction(part_guard_conj));

		// if cluster contains no predicates, continue with next cluster
		if(nr_of_predicates == 0) continue;

		// construct a cluster from current partition
		result.resize(result.size()+1);
		Decomposition& cluster = result.back();
		cluster.Set(part_guard,part_mod_vec,part_rel,part_invar);
	}
}


std::string Decomposition::toString() const {
	std::string result;
	result+="< \nrel = "+rel.toString()+" ,";
	foreach(const PredSet& mod,mod_vec)
		result+="\nmod = "+mod.toString()+", ";
	result+="\nINVAR: ";
	foreach(const CVC3::Expr& iv, invar) {
		result+=iv.toString()+" ";
	}
	return result;
}

bool Decomposition::isModified(const lang::Command& gt, const Predicate& p) {
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

size_t Decomposition::hash() const {
	size_t hash = 0;
	hash = guard.hash() + (hash << 6) + (hash << 16) - hash;
	foreach(const PredSet& mod,mod_vec)
		hash = mod.hash() + (hash << 6) + (hash << 16) - hash;
	hash = rel.hash() +  (hash << 6) + (hash << 16) - hash;
	return hash;
}

bool Decomposition::operator==(const Decomposition& c) const {
	bool result (true);
	result = result && mod_vec.size() == c.mod_vec.size();
	for(unsigned i=0;result && i<mod_vec.size();++i)
		result = result && mod_vec[i] == c.mod_vec[i];
	result = result && rel == c.rel && guard == c.guard;
	return result;
}

unsigned Decomposition::getNrOfPredicates() const {
	unsigned result = 0;
	result += rel.size();
	foreach(const PredSet& mod,mod_vec)
		result+= mod.size();
	return result;
}
