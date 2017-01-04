/*****************************************************************************/
/*!
 *\file dpllt_basic.h
 *\brief Basic implementation of dpllt module
 *
 * Author: Clark Barrett
 *
 * Created: Mon Dec 12 19:06:58 2005
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 * 
 * <hr>
 */
/*****************************************************************************/

#ifndef _cvc3__sat__dpllt_basic_h_
#define _cvc3__sat__dpllt_basic_h_

#include "dpllt.h"
#include "sat_api.h"
#include "cdo.h"
#include "proof.h" 
#include "cnf_manager.h"
 
namespace SAT {

class DPLLTBasic :public DPLLT {

  CVC3::ContextManager* d_cm;

  bool d_ready;
  SatSolver* d_mng;
  CNF_Formula_Impl* d_cnf;
  CD_CNF_Formula* d_assertions;

  std::vector<SatSolver*> d_mngStack;
  std::vector<CNF_Formula_Impl*> d_cnfStack;
  std::vector<CD_CNF_Formula*> d_assertionsStack;
  bool d_printStats;

  CVC3::CDO<unsigned> d_pushLevel;
  CVC3::CDO<bool> d_readyPrev;  
  CVC3::CDO<unsigned> d_prevStackSize;
  CVC3::CDO<unsigned> d_prevAStackSize;

  void createManager();
  void generate_CDB (CNF_Formula_Impl& cnf);
  void handle_result(SatSolver::SATStatus outcome);
  void verify_solution();

public:
  DPLLTBasic(TheoryAPI* theoryAPI, Decider* decider, CVC3::ContextManager* cm,
	     bool printStats = false);
  virtual ~DPLLTBasic();

  void addNewClause(const Clause& c);
  void addNewClauses(CNF_Formula_Impl& cnf);

  SatSolver::Lit cvc2SAT(Lit l)
  { return l.isNull() ? SatSolver::Lit() :
      d_mng->MakeLit(d_mng->GetVar(l.getVar()), l.isPositive() ? 0 : 1); }

  Lit SAT2cvc(SatSolver::Lit l)
  { return l.IsNull() ? Lit() :
                        Lit(d_mng->GetVarIndex(d_mng->GetVarFromLit(l)),
                            d_mng->GetPhaseFromLit(l) == 0); }

  SatSolver* satSolver() { return d_mng; }

  // Implementation of virtual DPLLT methods

  void push();
  void pop();
  void addAssertion(const CNF_Formula& cnf);
  virtual std::vector<SAT::Lit> getCurAssignments() ;
  virtual std::vector<std::vector<SAT::Lit> > getCurClauses();

  CVC3::QueryResult checkSat(const CNF_Formula& cnf);
  CVC3::QueryResult continueCheck(const CNF_Formula& cnf);
  Var::Val getValue(Var v) { return Var::Val(d_mng->GetVarAssignment(d_mng->GetVar(v))); }
  
  CVC3::Proof getSatProof(CNF_Manager*, CVC3::TheoryCore*);

};

}

#endif
