/*************************<o******* CPPFile *****************************

* FileName [ExprManager.cpp]

* PackageName [parser]

* Synopsis [Method definitions of ExprManager class.]

* SeeAlso [ExprManager.h]

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2002 by Saarland University. All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#include "vcl.h"

#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"

#include "util/Util.h"
#include "util/Cube.h"
using namespace std;

#include "util/Database.h"
#include "dp/SMT.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Error.h"

#include "lang/SymbolTable.h"

#include "ExprManager.h"

using namespace dp;

/** BEGIN FIX **/
namespace std
{
namespace tr1 {

	template<> struct hash< pair<CVC3::Expr,CVC3::Expr> >
	{
		size_t operator()( const pair<CVC3::Expr,CVC3::Expr>& x ) const
		{
			size_t hash = 0;
			hash = x.first.hash() + (hash << 6) + (hash << 16) - hash;
			hash = x.second.hash() +  (hash << 6) + (hash << 16) - hash;
			return hash;
		}
	};
}
}
/** END FIX **/


namespace lang {





/* initialize CVC3 engine */
CVC3::CLFlags flags(CVC3::ValidityChecker::createFlags());
CVC3::VCL dummy (flags);
CVC3::VCL& vc (dummy);

CVC3::VCL* internal_vc;

dp::SMT& smt (dp::SMT::getSMT());

void ExprManager::Init() {
	//faster, buggy, new arithmetic theory solver
	//flags.setFlag("arith-new",true);
	//flags.setFlag("circuit",true);
	//flags.setFlag("proofs",true);
	//flags.setFlag("sat","fast");

	smt = dp::SMT::getSMT();
    //	internal_vc = new CVC3::VCL(flags);
    //	vc = *internal_vc;

	vc.reprocessFlags();
	vc.push();
}

void ExprManager::Done() {
  vc.pop();
}

CVC3::Expr ExprManager::Sum(const std::vector<CVC3::Expr>& vec) {
	CVC3::Expr result;
	if(vec.size() == 0) {
		result = vc.ratExpr(1,1);
	} else {
		result = vec[0];
		for(size_t i=1; i<vec.size();++i) {
			result = vc.plusExpr(result,vec[i]);
		}
	}
	return result;
}

CVC3::Expr ExprManager::Conjunction(const std::vector<CVC3::Expr>& vec) {
	CVC3::Expr result;
	switch(vec.size()) {
		case 0:
			result = vc.trueExpr();
			break;
		case 1:
			result = vec[0];
			break;
		default:
			result = vc.andExpr(vec);
			break;
	}
	return result;

}

CVC3::Expr ExprManager::Conjunction(const std::set<CVC3::Expr>& s) {
	std::vector<CVC3::Expr> vec(s.begin(),s.end());
	return Conjunction(vec);
}


CVC3::Expr ExprManager::Disjunction(const std::vector<CVC3::Expr>& vec) {
	CVC3::Expr result;
	switch(vec.size()) {
		case 0:
			result = vc.falseExpr();
			break;
		case 1:
			result = vec[0];
			break;
		default:
			result = vc.orExpr(vec);
			break;
	}
	return result;
}



void ExprManager::getTopLevelConjuncts(const CVC3::Expr& e, std::vector<CVC3::Expr>& result) {
	assert(!e.isNull());
	switch(e.getKind()) {
		case CVC3::AND: {
			const std::vector< CVC3::Expr > & kids = e.getKids();
			for(int i=0;i<e.arity(); getTopLevelConjuncts(kids[i++],result));
			break;
		}
		default: {
			result.push_back(e);
			break;
		}
	 }
}

void ExprManager::getTopLevelConjuncts(const CVC3::Expr& e, std::set<CVC3::Expr>& result) {
	switch(e.getKind()) {
		case CVC3::AND: {
			const std::vector< CVC3::Expr > & kids = e.getKids();
			getTopLevelConjuncts(kids[0],result);
			getTopLevelConjuncts(kids[1],result);
			break;
		}
		default: {
			result.insert(e);
			break;
		}
	 }
}



	void ExprManager::getDisjointSupportDecomposition(const CVC3::Expr& e,
				     			  std::vector<CVC3::Expr>& result) {
		std::vector<CVC3::Expr> conjuncts;
		getTopLevelConjuncts(e,conjuncts);
		std::vector< std::vector<CVC3::Expr> > decomp;
		SplitExprSet(conjuncts,decomp);
		result.resize(decomp.size());
		for(unsigned i=0;i<decomp.size();++i) {
			result[i] = Conjunction(decomp[i]);
		}
	}



	CVC3::ExprHashMap<bool> TrueCache;
	CVC3::ExprHashMap<bool> FalseCache;


	lbool ExprManager::queryCaches(const CVC3::ExprHashMap<bool>& cache1,
				      const CVC3::ExprHashMap<bool>& cache2,
				      const CVC3::Expr& e) {
		lbool result = l_undef;
		CVC3::ExprHashMap<bool>::const_iterator i = cache1.find(e);
		if(i!=cache1.end()) {
			result = i->second ? l_true : l_false;
		} else {
			CVC3::ExprHashMap<bool>::const_iterator j = cache2.find(e);
			if(j != cache2.end() && j->second == true) {
				result = l_false;
			}
		}
		return result;
	}

	lbool ExprManager::Solve(const CVC3::Expr& e) {
		smt.pushContext();
		smt.Assert(e);
		lbool query_result = smt.Solve();
		smt.popContext();
		return query_result;
	}



	bool ExprManager::IsTrue(const CVC3::Expr& e) {
		// phase 1: syntactic
		if(e.isBoolConst()) return e.isTrue();

		// phase 2: service from cache
		switch(queryCaches(TrueCache,FalseCache,e)) {
			case l_true: return true;
			case l_false: return false;
			default: break;
		}

		// phase 3: decompose and try to service from cache or SMT
		bool result = false;
		switch(Solve(!e)) {
			case l_false:
				result = true;
				break;
			case l_true:
				return false;
				break;
			default:
				MSG(0,"DON'T KNOW!\n");
				assert(false);
				break;
		}
		TrueCache.insert(e,result);
		return result;
	}



	bool ExprManager::IsFalse(const CVC3::Expr& e) {

		// phase 1: syntactic
		if(e.isBoolConst()) return e.isFalse();

		// phase 2: service from cache
		switch(queryCaches(FalseCache,TrueCache,e)) {
			case l_true: return true;
			case l_false: return false;
			default: break;
		}

		// phase 3: decompose and try to service from cache or SMT
		bool result = false;
		switch(Solve(e)) {
			case l_false:
				result = true;
				break;
			case l_true:
				result = false;
				break;
			case l_undef:
				MSG(0,"DON'T KNOW!\n");
				break;
		}
		FalseCache.insert(e,result);
		return result;
	}

	bool ExprManager::IsFalse(const CVC3::Expr& e,
				  const vector<CVC3::Expr>& variables,
				  CVC3::ExprHashMap< CVC3::Expr > &m) {
		bool result =false;
		CVC3::ExprHashMap<bool>::iterator i = FalseCache.find(e);
		if(i!=FalseCache.end()) {
			return i->second;
		}
		CVC3::ExprHashMap<bool>::iterator j = TrueCache.find(e);
		if(j!=TrueCache.end()) {
			if(j->second == true)
				return false;
		}

        //		dp::SMT& smt = SMT::getYices();

		smt.pushContext();
		smt.Assert(e);
		lbool query_result = smt.Solve();

		switch(query_result) {
			case l_false:
				result = true;
				break;
			case l_true:
// 				switch(smt.getModel(variables,m)) {
// 					case 1: // fine model available
// 						break;
// 					case 0: MSG(0,"no model available!!\n");
// 						break;
// 					default:
// 						break;
// 				}
				result = false;
				break;
			case l_undef:
				MSG(0,"DON'T KNOW!\n");
				break;
		}
		smt.popContext();
		FalseCache.insert(e,result);

		return result;
	}

	bool ExprManager::IsFalseWithAssumptions(const vector<CVC3::Expr>& v,
						 vector<CVC3::Expr>& assumptions,
						 const vector<CVC3::Expr>& variables,
						 CVC3::ExprHashMap< CVC3::Expr > &m) {
		bool result = false;


		dp::SMT& smt = SMT::getCVC3();

		smt.pushContext();
		for(size_t i=0; i<v.size();++i) {
			smt.Assert(v[i]);
		}

		lbool query_result = smt.Solve();

		switch(query_result) {
			case l_false: {
				std::vector<CVC3::Expr> core;
				smt.getUnsatCore(assumptions,core);

				for(size_t i=0;i<core.size();++i)
					MSG(0,"assumptions "+core[i].toString()+"\n");
				result = true;
				}
				break;
			case l_true:
				switch(smt.getModel(variables,m)) {
					case 1: // fine model available
						break;
					case 0: MSG(0,"no model available!!\n");
						break;
					default:
						break;
				}
				result = false;
				break;
			case l_undef:
				MSG(0,"DON'T KNOW!\n");
				break;
		}
		smt.popContext();

		return result;
	}

	typedef map<pair<CVC3::Expr,CVC3::Expr>,bool > DisjCacheType;
	DisjCacheType DisjCache;

	bool ExprManager::DisjointWith(const CVC3::Expr& e1, const CVC3::Expr& e2) {
		bool result = false;

		MSG(1,"disjointness of "+e1.toString()+" and "+e2.toString()+"\n");
		pair<CVC3::Expr,CVC3::Expr> query;
		if(e1 == e2) {
			return false;
		} else if(e1>e2) {
			query.first = e2;
			query.second = e1;
		} else {
			query.first  = e1;
			query.second = e2;
		}

		DisjCacheType::iterator i = DisjCache.find(query);
		if(i!=DisjCache.end()) {
			result = i->second;
		} else {
			switch(DisjointWithSyntactic(query.first,query.second)) {
				case l_true: result = true; break;
				case l_false: result = false; break;
				case l_undef: {
					std::vector<CVC3::Expr> decomp;
					getDisjointSupportDecomposition(vc.andExpr(query.first,query.second),decomp);
					result = false;
					for(std::vector<CVC3::Expr>::const_iterator i=decomp.begin();i!=decomp.end();++i) {
						if(IsFalse(*i))
						{
							result = true;
							break;
						}
					}
				}
				break;
			}
			DisjCache[query] = result;
		}
		return result;
	}

	bool IsStrictInequality(const CVC3::Expr& e,
		      	CVC3::Expr& bound,
			CVC3::Expr& x,
		      	int& kind) {

		bool result = false;
		kind = e.getKind();
		switch(kind) {
			case CVC3::GT: {
				const std::vector<CVC3::Expr>& kids = e.getKids();
				CVC3::Expr kid1 = kids[0];
				CVC3::Expr kid2 = kids[1];
				bound = kid1;
				x = kid2;

				result = true;
			}
			break;
			case CVC3::LT: {
				const std::vector<CVC3::Expr>& kids = e.getKids();
				CVC3::Expr kid1 = kids[0];
				CVC3::Expr kid2 = kids[1];
				bound = kid1;
				x = kid2;
				result = true;
			}
			break;
		}
		return result;
	}

	/*

		decompose an inequality:
			e = lower <= e <= upper

		special cases: lower <= e <= e
			       e <= e <= upper

	*/
	bool ExprManager::IsInterval(const CVC3::Expr& e,
			CVC3::Expr& lower,
			CVC3::Expr& x,
			CVC3::Expr& upper) {
		bool result = false;

		switch(e.getKind()) {
			case CVC3::AND: {
				const std::vector<CVC3::Expr>& kids = e.getKids();
				CVC3::Expr kid1 = kids[0];
				CVC3::Expr kid2 = kids[1];

				if(kid1.getKind() == CVC3::LE && kid2.getKind() == CVC3::GE) {
					const std::vector<CVC3::Expr>& kids1 = kid1.getKids();
					const std::vector<CVC3::Expr>& kids2 = kid2.getKids();

					if(kids1[1] == kids2[1]) {
						lower = kids1[0];
						x     = kids1[1];
						upper = kids2[0];
						result = true;
					}
				} else if(kid1.getKind() == CVC3::GE && kid2.getKind() == CVC3::LE) {
					const std::vector<CVC3::Expr>& kids1 = kid1.getKids();
					const std::vector<CVC3::Expr>& kids2 = kid2.getKids();

					if(kids1[1] == kids2[1]) {
						lower = kids2[0];
						x     = kids1[1];
						upper = kids1[0];
						result = true;
					}
				}
			}
			break;
			case CVC3::GE: {
				const std::vector<CVC3::Expr>& kids = e.getKids();
				CVC3::Expr kid1 = kids[0];
				CVC3::Expr kid2 = kids[1];
				lower = kid2;
				x     = kid2;
				upper = kid1;
				result = true;
			}
			break;
			case CVC3::LE: {
				const std::vector<CVC3::Expr>& kids = e.getKids();
				CVC3::Expr kid1 = kids[0];
				CVC3::Expr kid2 = kids[1];
				lower = kid1;
				x     = kid2;
				upper = kid2;
				result = true;
			}
			break;
		}
		return result;
	}


	CVC3::Expr ExprManager::getVariableInstance(CVC3::Expr& var, int time) {
		std::string name = var.toString() + "T" + util::intToString(time);
		return vc.varExpr(name,var.getType());
	}


	lbool ExprManager::DisjointWithSyntactic(const CVC3::Expr& e1, const CVC3::Expr& e2) {
		if(e1 == e2) return l_false;

		if(!LvalueCompatible(e1,e2))
			return l_false;

		lbool result = l_undef;

		// arithmetic: case : a <= e AND b>= e     VS      c <= e AND d >= e



		CVC3::Expr a,x,b,c,y,d;

		int kind1 = -1;
		int kind2 = -1;

		CVC3::Expr test;

		// a <= x <= b
		if(IsInterval(e1,a,x,b)) {
			// c <= y <= d
			if(IsInterval(e2,c,y,d) && x == y) {
				/* test if intervals empty */

				/* first interval unconstrained on the left ... and symmetric cases */
				if(a==x || d==x)      // cases: (-inf,b], [c,d] and [a,b], [c,inf)
					test = vc.leExpr(c,b);
				else if(b==x || c==x) // cases : [a,inf), [c,d] and [a,b], (-inf,d]
					test = vc.leExpr(a,d);
				else
					test = vc.orExpr(vc.andExpr(vc.leExpr(c,a),vc.leExpr(a,d)),
							vc.andExpr(vc.leExpr(a,c),vc.leExpr(c,b)) );

	/* 			else
					MSG(1," [a,b] = ["+a.toString()+","+b.toString()+"] , [c,d] = "+c.toString()+","+d.toString()+"] nontrivial "+test.toString()+"\n");*/
			}
			// y < d or y > d
			else if(IsStrictInequality(e2,d,y,kind2) && x == y) {
				switch(kind2) {
					case CVC3::GT:
						if(a==x) { // (-inf,d), (-inf,b]
							test = vc.trueExpr(); // definitely intersects
						} else if(b==x) { // (-inf,d), [a,inf)
							test = vc.ltExpr(a,d);
						} else { // (-inf,d), [a,b]
							test = vc.ltExpr(a,d);
						}
					break;
					case CVC3::LT:
						if(a==x) { // (d,inf), (-inf,b]
							test = vc.ltExpr(d,b);
						} else if(b==x) { // (d,inf), [a,inf)
							test = vc.trueExpr();
						} else { // (d,inf), [a,b]
							test = vc.ltExpr(d,b);
						}
					break;
					default:
					break;
				}

			}
		} else if(IsStrictInequality(e1,a,x,kind1)) {
			if(IsInterval(e2,c,y,d) && x == y) {
				switch(kind1) {
					case CVC3::GT:
						if(d==x) {     // case: (-inf,a), [c,inf)
							test = vc.ltExpr(c,a);
						}
						else if(c==x) {// case : (-inf,a), (-inf,d]
							test = vc.trueExpr();
						}
						else { // case: (-inf,a), [c,d]
							test = vc.ltExpr(c,a);

						}
					break;

					case CVC3::LT:
						if(d==x) {     // case: (a,inf), [c,inf)
							test = vc.trueExpr();
						}
						else if(c==x) {// case : (a,inf), (-inf,d]
							test = vc.ltExpr(a,d);
						}
						else { // case: (a,inf), [c,d]
							test = vc.ltExpr(a,d);
						}
					break;
					default:
					break;
				}

			} else if (IsStrictInequality(e2,d,y,kind2) && x == y) {
				switch(kind1) {
					case CVC3::GT:
					switch(kind2) {
						case CVC3::GT: // (-inf,a), (-inf,d)
							test = vc.trueExpr();
							break;
						case CVC3::LT: // (-inf,a), (d,inf)
							test = vc.ltExpr(a,d);
							break;
						default:
							break;
					}
					break;
					case CVC3::LT:
					switch(kind2) {
						case CVC3::GT: // (a,inf), (-inf,d)
							test = vc.ltExpr(a,d);
							break;
						case CVC3::LT: // (a,inf), (d,inf)
							test = vc.trueExpr();
							break;
						default:
							break;
					}
					break;
				}
			}
		}

		if(!test.isNull()) {
			test = vc.simplify(test);
			if(test == vc.falseExpr()) {
				result = l_true;
			} else if(test == vc.trueExpr()) {
				result = l_false;
			}
		}


		return result;
	}



	bool ExprManager::EquivalentTo(const CVC3::Expr& e1, const CVC3::Expr& e2) {
		if(e1 == e2) return true;
		return IsTrue(vc.iffExpr(e1,e2));
	}

	bool ExprManager::NegationOf(const CVC3::Expr& e1, const CVC3::Expr& e2) {
		if(vc.notExpr(e1)==e2) return true;

		return IsTrue(vc.iffExpr(vc.notExpr(e1),e2));
	}

	typedef HashMap<pair<CVC3::Expr,CVC3::Expr>, bool > LvalueCache;

	LvalueCache LvalueCompatCache;



	bool ExprManager::LvalueCompatible(const CVC3::Expr& e1, const CVC3::Expr& e2) {

		bool result = false;
		bool swap = (e2 < e1);
		if(e1 == e2) result = true;
		else {
			CVC3::Expr lhs = swap ? e1 : e2;
			CVC3::Expr rhs = swap ? e2 : e1;

			pair<CVC3::Expr,CVC3::Expr> ordered_pair(lhs,rhs);
			LvalueCache::const_iterator it =
			LvalueCompatCache.find(ordered_pair);
			if(it!=LvalueCompatCache.end()) {
				result = it->second;
			} else {


				if(e1.getKind()==CVC3::UCONST) {
					result = e1.subExprOf(e2);
				} else if(e2.getKind()==CVC3::UCONST) {
					result = e2.subExprOf(e1);
				}
				else {
					set<CVC3::Expr> set1;
					ComputeLvalues(e1, set1);
					set<CVC3::Expr> set2;
					ComputeLvalues(e2, set2);
					swap = set1.size() > set2.size();

					set<CVC3::Expr>& fst_set = swap ? set2 : set1;
					set<CVC3::Expr>& sec_set = swap ? set1 : set2;

					set<CVC3::Expr>::const_iterator sec_end = sec_set.end();
					for(set<CVC3::Expr>::const_iterator i = fst_set.begin();i!=fst_set.end();++i) {
						if(sec_set.find(*i)!=sec_end) {
							result = true;
							break;
						}
					}
				}
				LvalueCompatCache.insert(pair<pair<CVC3::Expr,CVC3::Expr>, bool> (ordered_pair,result));
			}
		}
		return result;
	}


	void ExprManager::CollectExprs(const CVC3::Expr& e, hash_set<CVC3::Expr>& exprs) {
		switch (e.getKind()) {
			case CVC3::TRUE_EXPR:
			case CVC3::FALSE_EXPR:
				return;
			case CVC3::NOT:
			case CVC3::AND:
			case CVC3::OR:
			case CVC3::IMPLIES:
			case CVC3::IFF:
			case CVC3::XOR:
			case CVC3::ITE: {
					const std::vector< CVC3::Expr > & kids = e.getKids();
					foreach(const CVC3::Expr& kid, kids) {
						CollectExprs(kid,exprs);
					}
				}
				break;
			default:
				exprs.insert(e);
				break;
		}
	}

	void ExprManager::CollectExprs(const CVC3::Expr& e, set<CVC3::Expr>& exprs) {
		switch (e.getKind()) {
			case CVC3::TRUE_EXPR:
			case CVC3::FALSE_EXPR:
				return;
			case CVC3::NOT:
			case CVC3::AND:
			case CVC3::OR:
			case CVC3::IMPLIES:
			case CVC3::IFF:
			case CVC3::XOR:
			case CVC3::ITE: {
					const std::vector< CVC3::Expr > & kids = e.getKids();
					foreach(const CVC3::Expr& kid, kids) {
						CollectExprs(kid,exprs);
					}
				}
				break;
			default:
				exprs.insert(e);
				break;
		}
	}

	void ExprManager::Filter(const std::vector<CVC3::Expr>& vec, const std::hash_set<CVC3::Expr>& exprs, std::vector<CVC3::Expr>& res) {
		res.clear();
		for(std::vector<CVC3::Expr>::const_iterator it = vec.begin(); it!=vec.end(); ++it) {
			if(exprs.find(*it)!=exprs.end()) {
				res.push_back(*it);
			}
		}
	}

	void ExprManager::Triage(const std::vector<CVC3::Expr>& vec,
				 const std::hash_set<CVC3::Expr>& exprs,
				 std::vector<CVC3::Expr>& in,
				 std::vector<CVC3::Expr>& out
				 ) {
		in.clear();
		out.clear();
		for(std::vector<CVC3::Expr>::const_iterator it = vec.begin(); it!=vec.end(); ++it) {
			if(exprs.find(*it)!=exprs.end()) {
				in.push_back(*it);
			} else {
				out.push_back(*it);
			}
		}
	}


void ExprManager::ComputeLvalues(const CVC3::Expr& e, set<CVC3::Expr>& result) {

		if(e.getKind()==CVC3::UCONST)
			result.insert(e);
		else {
			const std::vector< CVC3::Expr > & kids = e.getKids();
			for(unsigned i = 0; i<kids.size(); ++i) {
				ComputeLvalues(kids[i],result);
			}
		}
}

// /*********************************************************************/
// //split a set of expressions into disjoint groups. each group consists
// //of expressions related by common lvalues. expressions in different
// //groups are unrelated in terms of lvalues.
// /*********************************************************************/

void ExprManager::SplitExprSet(const vector<CVC3::Expr> &arg,vector< set<CVC3::Expr> > &res)
{
	// get the support of e
	std::set<CVC3::Expr> support;
	std::vector<std::set<CVC3::Expr> > arg_support(arg.size());

	for(unsigned i=0; i!=arg.size();++i) {
		ExprManager::ComputeLvalues(arg[i], arg_support[i]);
		support.insert(arg_support[i].begin(),arg_support[i].end());
	}

	std::vector<CVC3::Expr> table(support.size());
	int counter = 0;
	for(std::set<CVC3::Expr>::const_iterator i=support.begin();i!=support.end();++i) {
		table[counter++] = *i;
	}

	typedef Signature<unsigned> Sig;

	std::vector<Sig> partition(arg.size(),table.size());

	// add the expressions to partition
	for(unsigned i=0; i<arg.size();++i) {
		Sig& sig(partition[i]);
		sig.insert(i);
		for(unsigned k=0;k<table.size();++k) {
			sig.s[k] = arg_support[i].find(table[k])!=arg_support[i].end();
		}
	}

	std::vector<Sig> new_partition;
	Sig::partitionRefinement2(partition,new_partition);

	res.resize(new_partition.size());

	unsigned i = 0;
	foreach(Sig& sig, new_partition) {
		std::set<unsigned int>& current_set (sig.cset);
		res[i].clear();
		foreach(unsigned j , current_set)
			res[i].insert(arg[j]);
		++i;
	}
}

void ExprManager::SplitExprSet(const vector<CVC3::Expr> &arg,vector< vector<CVC3::Expr> > &res)
{


	// get the support of e
	std::set<CVC3::Expr> support;
	std::vector<std::set<CVC3::Expr> > arg_support(arg.size());

	for(unsigned i=0; i!=arg.size();++i) {
		ExprManager::ComputeLvalues(arg[i], arg_support[i]);
		support.insert(arg_support[i].begin(),arg_support[i].end());
	}

	std::vector<CVC3::Expr> table(support.size());
	int counter = 0;
	for(std::set<CVC3::Expr>::const_iterator i=support.begin();i!=support.end();++i) {
		table[counter++] = *i;
	}

	typedef Signature<unsigned> Sig;

	std::vector<Sig> partition(arg.size(),table.size());

	// add the expressions to partition
	for(unsigned i=0; i<arg.size();++i) {
		Sig& sig(partition[i]);
		sig.insert(i);
		for(unsigned k=0;k<table.size();++k) {
			sig.s[k] = arg_support[i].find(table[k])!=arg_support[i].end();
		}
	}

	std::vector<Sig> new_partition;
	Sig::partitionRefinement2(partition,new_partition);

	res.resize(new_partition.size());

	unsigned i = 0;
	foreach(Sig& sig, new_partition) {
		std::set<unsigned int>& current_set (sig.cset);
		res[i].clear();
		foreach(unsigned j , current_set)
			res[i].push_back(arg[j]);
		++i;
	}
}

CVC3::Expr ExprManager::getExprCube(const Cube& c, const std::vector<CVC3::Expr>& vec) {
	vector<CVC3::Expr> result;

	for(unsigned i=0;i<vec.size();++i) {
		switch(c[i]) {
			case l_true: result.push_back(vec[i]);   break;
			case l_false: result.push_back(!vec[i]); break;
			case l_undef: break;
		}
	}
	return Conjunction(result);
}

void ExprManager::addExprCube(const Cube& c, const std::vector<CVC3::Expr>& vec, std::vector<CVC3::Expr>& result) {
	for(unsigned i=0;i<vec.size();++i) {
		switch(c[i]) {
			case l_true: result.push_back(vec[i]);   break;
			case l_false: result.push_back(!vec[i]); break;
			case l_undef: break;
		}
	}
}


void ExprManager::SplitExprSet(const set<CVC3::Expr> &arg,vector< set<CVC3::Expr> > &res)
{

	// get the support of e
	std::set<CVC3::Expr> support;
	std::vector<std::set<CVC3::Expr> > arg_support(arg.size());

	unsigned i = 0;

	foreach(CVC3::Expr e, arg) {
		ExprManager::ComputeLvalues(e, arg_support[i]);
		support.insert(arg_support[i].begin(),arg_support[i].end());
		++i;
	}

	std::vector<CVC3::Expr> table(support.size());
	int counter = 0;
	for(std::set<CVC3::Expr>::const_iterator i=support.begin();i!=support.end();++i) {
		table[counter++] = *i;
	}

	typedef Signature<CVC3::Expr> Sig;

	std::vector<Sig> partition(arg.size(),table.size());

	i = 0;

	// add the expressions to partition
	foreach(CVC3::Expr e, arg) {
		Sig& sig(partition[i]);
		sig.insert(e);
		for(unsigned k=0;k<table.size();++k) {
			sig.s[k] = arg_support[i].find(table[k])!=arg_support[i].end();
		}
		++i;
	}

	std::vector<Sig> new_partition;
	Sig::partitionRefinement2(partition,new_partition);

	res.resize(new_partition.size());

	i = 0;
	foreach(Sig& sig, new_partition) {
		std::set<CVC3::Expr>& current_set (sig.cset);
		res[i].clear();
		foreach(CVC3::Expr j , current_set)
			res[i].insert(j);
		++i;
	}
}


inline
std::string FOCIEquivalence(const std::string& a, const std::string& b) {
	return " & [ | [ ~ " + a + " " + b + " ] "
                 + " | [ ~ " + b + " " + a + " ] ]";

}

/*! get the expression as a string that the FOCI interpolating theorem prover understands */
std::string ExprManager::toFOCIString(const CVC3::Expr& e, map<std::string,CVC3::Expr>& symbol_table) {

	std::string result;

	std::string skids[e.arity()];


	const std::vector< CVC3::Expr > & kids = e.getKids();

	/* start with child nodes */
	if(e.arity()>0) {
		for(unsigned i = 0; i<kids.size(); ++i) {
			skids[i] = toFOCIString(kids[i],symbol_table);
		}
	}

	switch(e.getKind()) {
		case CVC3::TRUE_EXPR:
			result = "true";
			break;
		case CVC3::FALSE_EXPR:
			result = "false";
			break;
		case CVC3::ID:
		case CVC3::UCONST:
		{
			result = e.toString();
			switch(e.getType().getExpr().getKind()) {
				case CVC3::BITVECTOR: {
					throw util::RuntimeError("bitvectors not supported\n");
					}
					break;
				case CVC3::INT:
				case CVC3::REAL:
				case CVC3::BOOLEAN: {
					result = e.toString();
					break;
				}
				default: break;
			}

			symbol_table[result] = e;

			break;
		}
		/* Let requires extra treatment as it binds variables
		   in subexpressions. These variables have to be handled
		   either by replacement or by introducing them into the
		   translation table. Out of laziness, we do the former. */
		case CVC3::LET:
			cout<<"LET expression"<<endl;

			break;
		case CVC3::RATIONAL_EXPR:
			result = e.getRational().toString();
			break;
		case CVC3::ITE:
			//throw util::RuntimeError("FOCI::toFOCIString: FOCI can't do ITE "+ e.toString() + "\n");
			result = " ite (" + skids[0] + "," + skids[1] + "," + skids[2]  + ")";
			break;
		case CVC3::NOT:
			result = " ~ " + skids[0];
			break;
		case CVC3::AND:
			result = " & [ " + skids[0] + " " + skids[1] + " ]";
			break;
		case CVC3::OR:
			result = " | [ " + skids[0] + " " + skids[1] + " ]";
			break;
		case CVC3::XOR:
			result = " ~ = "+ skids[0] + " " + skids[1];
			break;
		case CVC3::IMPLIES:
			result = " -> " + skids[0] + " " + skids[1] + " ";
			break;
		case CVC3::IFF:
			if(kids[0].getType().isBool()) {
				if(kids[0] == lang::vc.trueExpr())
					result = skids[1];
				else if(kids[1] == lang::vc.trueExpr())
					result = skids[0];
				else if(kids[0] == lang::vc.falseExpr())
					result = " ~ " + skids[1];
				else if(kids[1] == lang::vc.falseExpr())
					result = " ~ " + skids[0];
				else
					result = FOCIEquivalence (skids[0],skids[1]);
			}
			break;
		case CVC3::EQ:

			// expression transformation
			// e0 = ITE (c,e1,e2) translates to (c => e0 = e1) & (!c => e0 = e2)

			if(kids[1].isITE()) {

				const std::vector< CVC3::Expr > & ekids = kids[1].getKids();

				std::string e0 = toFOCIString(kids[0],symbol_table);
				std::string c = toFOCIString(ekids[0],symbol_table);
				std::string e1 = toFOCIString(ekids[1],symbol_table);
				std::string e2 = toFOCIString(ekids[2],symbol_table);
				result = " & [ | [ ~ " + c + "  = " + e0 + " " + e1 + " ] "
				       +     " | [ " + c + " = " + e0 + " " + e2 + " ] ]";

			} else if(kids[0].isITE()) { // symmetric case
				result = toFOCIString(lang::vc.eqExpr(kids[1],kids[0]), symbol_table);

			} else if(kids[0].getType().isBool()) {
				if(kids[0] == lang::vc.trueExpr())
					result = skids[1];
				else if(kids[1] == lang::vc.trueExpr())
					result = skids[0];
				else if(kids[0] == lang::vc.falseExpr())
					result = " ~ " + skids[1];
				else if(kids[1] == lang::vc.falseExpr())
					result = " ~ " + skids[0];
				else
					result = FOCIEquivalence(skids[0],skids[1]);
			} else
				result = " = " + skids[0] + " " + skids[1];
			break;
		case CVC3::NEQ:
			result = " ~ = " + skids[0] + " " + skids[1];
			break;

		/* arithmetic */
		case CVC3::LT:
			result = "~ <= "+skids[1] + " " + skids[0];
			break;
		case CVC3::GT:
			result = "~ <= "+skids[0] + " " + skids[1];
			break;
		case CVC3::LE:
			result = "<= " + skids[0] + " " + skids[1];
			break;
		case CVC3::GE:
			result = "<= " + skids[1] + " " + skids[0];
			break;
		case CVC3::PLUS:
			result += " + [ ";
			for(unsigned i = 0; i<kids.size(); ++i) {
				result += skids[i] + " ";
			}

			result += "] ";
			break;
		case CVC3::MINUS:
			result = " + [ " + skids[0] + " * -1 " + skids[1]+ " ] ";
			break;
		case CVC3::UMINUS:
			result = " -"+skids[0]+ " ";
			break;
		case CVC3::MULT:
			result = " * " + skids[0] + " " + skids[1];
			break;
		case CVC3::DIVIDE:
			throw util::RuntimeError("FOCI can't do DIVISION\n");
			result = skids[0] + "/" + skids[1];
			break;
		case CVC3::MOD:
			throw util::RuntimeError("FOCI can't do MODULUS\n");
			result = skids[0] + "%" + skids[1];
			break;

		/* bitvector operations */
		case CVC3::BVCONST:
		case CVC3::CONCAT:
		case CVC3::EXTRACT:
		case CVC3::BOOLEXTRACT:
		case CVC3::LEFTSHIFT:
		case CVC3::CONST_WIDTH_LEFTSHIFT:
		case CVC3::RIGHTSHIFT:
		case CVC3::BVSHL:
		case CVC3::BVLSHR:
		case CVC3::BVASHR:
		case CVC3::SX:
		case CVC3::BVREPEAT:
		case CVC3::BVZEROEXTEND:
		case CVC3::BVROTL:
		case CVC3::BVROTR:
		case CVC3::BVAND:
		case CVC3::BVOR:
		case CVC3::BVXOR:
		case CVC3::BVXNOR:
		case CVC3::BVNEG:
		case CVC3::BVNAND:
		case CVC3::BVNOR:
		case CVC3::BVCOMP:
		case CVC3::BVUMINUS:
		case CVC3::BVPLUS:
		case CVC3::BVSUB:
		case CVC3::BVMULT:
		case CVC3::BVUDIV:
		case CVC3::BVSDIV:
		case CVC3::BVUREM:
		case CVC3::BVSREM:
		case CVC3::BVSMOD:
		case CVC3::BVLT:
		case CVC3::BVLE:
		case CVC3::BVGT:
		case CVC3::BVGE:
		case CVC3::BVSLT:
		case CVC3::BVSLE:
		case CVC3::BVSGT:
		case CVC3::BVSGE:
		case CVC3::INTTOBV:
		case CVC3::BVTOINT:
		default:
			MSG(0,"unsupported expression "+lang::vc.getEM()->getKindName(e.getKind()));
			break;



			break;
	}
	return result;
}

std::string ExprManager::prettyPrint(const CVC3::Expr& e) {
	std::string result;
	std::vector<CVC3::Expr> vec;
	getTopLevelConjuncts(e,vec);
	if(vec.size() == 0)
		return "TRUE";
	result += vec[0].toString();
	for(unsigned i=1 ; i< vec.size(); ++i) {
		result += " & " + vec[i].toString();
	}
	return result;
}

} //end of namespace lang

/*********************************************************************/
//end of ExprManager.cpp
/*********************************************************************/
