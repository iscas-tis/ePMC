/******************************** CPPFile *****************************

* FileName [PredSet.cpp]

* PackageName [main]

* Synopsis [Method definitions of PredSet class.]

* SeeAlso [PredSet.h]

* Author [Sagar Chaki]

* Copyright [ Copyright (c) 2002 by Carnegie Mellon University. All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: chaki+@cs.cmu.edu. ]

**********************************************************************/

#include <algorithm>

#include "util/Util.h"
#include "util/Cube.h"
#include "lang/Node.h"
#include "lang/ExprManager.h"

#include "util/Error.h"
#include "util/Database.h"
#include "pred/Predicate.h"
#include "pred/PredSet.h"
#include "util/Timer.h"
#include "util/Statistics.h"
using namespace util;
using namespace lang;

namespace pred {

/*********************************************************************/
//default constructor creates and empty predicate set
/*********************************************************************/
PredSet::PredSet()
{
}


size_t PredSet::hash() const {
	size_t hash = 0;
	for(std::vector<Predicate>::const_iterator i = preds.begin();i != preds.end();++i) {
		hash = (*i).hash() + (hash << 6) + (hash << 16) - hash;
	}
	return hash;
}


/*********************************************************************/
//operators
/*********************************************************************/
PredSet &PredSet::operator = (const PredSet &rhs)
{
  	preds = rhs.preds;
	table = rhs.table;
  	return *this;
}

bool PredSet::operator == (const PredSet &rhs) const
{
	if(preds.size()!= rhs.preds.size())
		return false;
	for(unsigned i = 0;i < preds.size();++i) {
		if(preds[i] != rhs.preds[i])
			return false;
	}
	return true;
}



/*********************************************************************/
//string representation
/*********************************************************************/
std::string PredSet::toString() const
{
	std::string res = "preds\n";
	
	unsigned counter = 0;
	for(std::vector<Predicate>::const_iterator i = preds.begin();i != preds.end();++counter) {
		if(i != preds.begin()) res += ", ";
		else res += "  ";
		res += i->toString() + "\n";
		++i;
	}
	return res + ";";
}


/*********************************************************************/
//add a new predicate with a number of redundancy checks. return true
//if the predicate was actually added and false otherwise
/*********************************************************************/
PredSet::ReturnCode PredSet::classifyExpr(const CVC3::Expr& e, bool expensive_checks) const
{
	//check if we already have the predicate
	if(find(e)!=-1)
		return CONTAINED;
	if(find(!e)!=-1)
		return COVERED;
	else if(e.isBoolConst()) {
		return TRIVIAL;
	}	

	if(expensive_checks) {
	
		Predicate p(e);
		CVC3::Expr ep = p.getExpr();
		
		if(find(ep)!=-1)
			return CONTAINED;
		if(find(!ep)!=-1)
			return COVERED;
		else if(ep.isBoolConst()) {
			return TRIVIAL;
		}
		//check if the predicate can be proved to be true or false. in that
		//case we do not add the predicate.
		if(ExprManager::IsTrue(ep) || ExprManager::IsFalse(ep)) {
			MSG(1,"Predicate %s is trivial (true or false)\n",ep.toString().c_str());
			return TRIVIAL;
		}
	
		//try to merge this predicate into already existing predicates
		for(unsigned int i = 0;i < preds.size();++i) {
			if(!ExprManager::LvalueCompatible(preds[i].getExpr(),ep)) continue;
			if(preds[i].Merge(p)) {
				return COVERED;
			}
		}
	}
	return ADDED;
}

/*********************************************************************/
//add a new predicate with a number of redundancy checks. return true
//if the predicate was actually added and false otherwise
/*********************************************************************/
PredSet::ReturnCode PredSet::Add(const Predicate &p, bool expensive_checks)
{
	ReturnCode result(classifyExpr(p.getExpr(),expensive_checks));
	switch(result) {
		case ADDED:
			preds.push_back(p);
			table[p.getExpr()] = preds.size()-1;
			break;
		default:
			break;
	}
	return result;
}


bool PredSet::isRedundant(const CVC3::Expr &e, bool expensive_checks) const {
	bool result = true;
	ReturnCode query(classifyExpr(e,expensive_checks));
	switch(query) {
		case ADDED:
			result = false;
			break;
		default:
			break;
	}
	return result;
}



/*********************************************************************/
//merge with another set of predicates
/*********************************************************************/
void PredSet::Merge(const PredSet &rhs, bool expensive_checks)
{
	for(std::vector<Predicate>::const_iterator i = rhs.preds.begin();i != rhs.preds.end();++i)
		Add(*i,expensive_checks);
}

/*********************************************************************/
//do the the two predsets have predicates in common?
/*********************************************************************/
bool PredSet::DisjointWith(const PredSet &rhs) const {
	if(rhs.preds.size()<=preds.size())
		for(Table::const_iterator i = rhs.table.begin();i != rhs.table.end();++i) {
			if(find(i->first)!=-1) return false;
		}
	else
		for(Table::const_iterator i = table.begin();i != table.end();++i) {
			if(rhs.find(i->first)!=-1) return false;
		}
	return true;
}

/*********************************************************************/
//clear the set of predicates
/*********************************************************************/
void PredSet::clear()
{
  	preds.clear();
	table.clear();
}

}

/*********************************************************************/
//end of PredSet.cpp
/*********************************************************************/
