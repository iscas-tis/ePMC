/******************************** CPPFile *****************************

* FileName [Predicate.cpp]

* PackageName [main]

* Synopsis [Method definitions of Predicate class.]

* SeeAlso [Predicate.h]

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
#include <typeinfo>
using namespace std;

#include "util/Util.h"
#include "util/Cube.h"
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "util/Database.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#define YYSTYPE int
using namespace lang;
using namespace util;

#include "pred/Predicate.h"

namespace pred {

/*********************************************************************/
//static members
/*********************************************************************/
const int pred::Predicate::NONE = -1;
const int Predicate::REQUIRED = 0;
const int Predicate::TENTATIVE = 1;

/*********************************************************************/
//constructors
/*********************************************************************/
Predicate::Predicate(const CVC3::Expr &__e,int t)
{
	assert((t == REQUIRED) || (t == TENTATIVE));
	assert(!__e.isNull());
	e = vc.simplify(__e);
	e = e.unnegate();
	type = t;
}

Predicate::Predicate(const Predicate &rhs)
{
  *this = rhs;
}

/*********************************************************************/
//operators
/*********************************************************************/
const Predicate &Predicate::operator = (const Predicate &rhs)
{
  e = rhs.e;
  type = rhs.type;
  return *this;
}

/*********************************************************************/
//string representation
/*********************************************************************/
string Predicate::toString() const
{
  //return "<" + e.toString() + ">";

	string result;

	CVC3::Expr a, x ,b;
	// a <= x <= b
	if(lang::ExprManager::IsInterval(e,a,x,b) ) {
		if( a == b)
			result = x.toString() + " = " + a.toString();
		else if(a==x)
			result = x.toString() + " <= " + b.toString();
		else if(b==x)
			result = x.toString() + " >= " + a.toString();
		else
			result = a.toString() + " <= "+ x.toString() + " <= " + b.toString();
	} else {
		result = e.toString();
	}

	return result ;
}

/*********************************************************************/
//merge a general predicate with another supplied as argument. any
//expression that is equivalent with or negation of some expression in
//this predicate will be discarded. any expression that is disjoint
//with all expressions in this predicate will be incorporated. remove
//from the argument any expression that was incorporated or
//discarded. return true iff at least one expression was incorporated.
/*********************************************************************/
bool Predicate::Merge(const Predicate &rhs) const
{
  CVC3::Expr e1 = rhs.e;
  bool equiv = ExprManager::EquivalentTo(e1,e);
  bool neg = !equiv && ExprManager::NegationOf(e1,e);
  return equiv || neg;
}

/*********************************************************************/
//given a set of variables return true if some expression contains at
//least one variable from the set.
/*********************************************************************/
bool Predicate::ContainsLvalue(const vector<CVC3::Expr> &vars) const
{
  vector<CVC3::Expr> evars = e.getVars();
	for(unsigned i=0; i<evars.size();++i) {
		for(unsigned j=0; i<evars.size();++j) {
			if(vars[i]==vars[j])
				return true;
		}
	}
	return false;

}

}
/*********************************************************************/
//end of Predicate.cpp
/*********************************************************************/
