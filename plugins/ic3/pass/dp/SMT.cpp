/**************************** CPPFile ***************************

* FileName [SMT.cpp]

* PackageName [parser]

* Synopsis [Implementation of SMT]

* Description [These classes encapsulate an SMT solver.]

* SeeAlso []

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2006 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

using namespace std;

#include "cvc3/include/vc.h"

#include "util/Util.h"
#include "util/Cube.h"
#include "util/Database.h"

#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"

using namespace util;

#include "lang/ExprManager.h"
#include "SMT.h"
#include "CVC3SMT.h"
#include "MathSat.h"
#include "YicesSMT.h"
#include "CLP.h"
#include "FOCI.h"
#include "CSIsat.h"

namespace dp {

/** Code that starts different solvers */
void SMT::setSMT(SMT& __smt) { smt = __smt; }

void SMT::blockModel(CVC3::ExprHashMap<CVC3::Expr>& model) {
	vector<CVC3::Expr> vec;
	for(CVC3::ExprHashMap<CVC3::Expr>::iterator i=model.begin();i!=model.end();++i) {
		const CVC3::Expr& var (i->first);
		const CVC3::Expr& val (i->second);
		CVC3::Expr equality (val.getType().isBool() ? lang::vc.iffExpr(var,val) : lang::vc.eqExpr(var,val));
		vec.push_back(lang::vc.notExpr(equality));
	}
	Assert(lang::ExprManager::Disjunction(vec));
}





SMT& SMT::getSMT() {
	switch(Database::THEOREM_PROVER_USED) {
		case Database::CVC3:
			return getCVC3();
			break;
		case Database::Yices:
			return getYices();
			break;
		default:
			return getYices();
			break;
	}
}

SMT& SMT::getInterpolator() {
	switch(Database::INTERPOLATION) {
		case Database::MathSat:
			return getMathSat();
			break;
		case Database::FOCI:
			return getFOCI();
			break;
		case Database::CLP:
			return getCLP();
			break;
		case Database::CSIsat:
			return getCSIsat();
			break;
		default:
			return getFOCI();
			break;

	}
}

SMT& SMT::getDummy() {
	static SMT dummy;
	return dummy;
}

SMT& SMT::smt = getDummy();

SMT* mathsat_ptr = 0;

//! invoke MathSat
SMT& SMT::getMathSat() {
	if(mathsat_ptr == NULL) {
		mathsat_ptr = new MathSat();
	}
	return *mathsat_ptr;
}


YicesSMT* yices_smt_ptr = 0;

void SMT::Assert(const vector<CVC3::Expr>& v) { Assert(lang::ExprManager::Conjunction(v)); }


bool SMT::allSat ( const std::vector<CVC3::Expr>& v,
		      CubeConsumer&,
		      unsigned int limit) { return false; }

void destroyYices() {
  if (NULL != yices_smt_ptr) {
    delete yices_smt_ptr;
  }
}

//! invoke Yices
SMT& SMT::getYices() {
	if(yices_smt_ptr == NULL) {
		yices_smt_ptr = new YicesSMT();
        atexit(destroyYices);
	}
	return *yices_smt_ptr;
}

CLP* clp_ptr = 0;
//! invoke CLP-Prover
SMT& SMT::getCLP() {
	if(clp_ptr == NULL) {
		clp_ptr = new CLP();
	}
	return *clp_ptr;
}

CSIsat* csi_ptr = 0;

//! invoke CLP-Prover
SMT& SMT::getCSIsat() {
	if(csi_ptr == NULL) {
		csi_ptr = new CSIsat();
	}
	return *csi_ptr;
}


FOCI* foci_ptr = 0;
//! invoke FOCI
SMT& SMT::getFOCI() {
	if(foci_ptr == NULL) {
		foci_ptr = new FOCI();
	}
	return *foci_ptr;
}

CVC3SMT* cvc3_smt_ptr = 0;

//! invoke CVC3
SMT& SMT::getCVC3() {
	if(cvc3_smt_ptr == NULL) {
		cvc3_smt_ptr = new CVC3SMT();
	}
	return *cvc3_smt_ptr;
}



} //end of dp
