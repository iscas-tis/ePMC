/*****************************************************************************/
/*!
 *\file dpllt_minisat.h
 *\brief Implementation of dpllt module based on minisat
 *
 * Author: Alexander Fuchs
 *
 * Created: Fri Sep 08 11:04:00 2006
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

#ifndef _cvc3__sat__dpllt_minisat_h_
#define _cvc3__sat__dpllt_minisat_h_

#include "dpllt.h"
#include "proof.h"
#include "cnf_manager.h"
#include <stack>


// forward declaration of the minisat solver
namespace MiniSat {
  class Solver;
}

namespace SAT {

class SatProof;

// an implementation of the DPLLT interface using an adapted MiniSat SAT solver
class DPLLTMiniSat : public DPLLT {
public:

protected:
  // determines if the derivation statistic of the solver
  // is printed after the derivation terminates.
  // can only be set with the constructor
  bool d_printStats;

  // if true then minisat will create a derivation
  // of the empty clause for an unsatisfiable problem.
  // see getProof().
  bool d_createProof;

  // if d_createProof, then the proof of the last unsatisfiable search
  SatProof* d_proof;

  // the dpllt interface operates in a stack fashion via checkSat and push.
  // each stack's data is stored in a level, which is just an instance of MiniSat.
  // whenever a checkSat or push is done on a solver that is already in a search,
  // a new solver is created and pushed.
  std::stack<MiniSat::Solver*> d_solvers;

  // returnes the currently active MiniSat instance
  //
  // must not be called when there is no active MiniSat instance,
  // i.e. d_solvers is empty.
  MiniSat::Solver* getActiveSolver();

  // creates a solver as an extension of the previous solver
  // (i.e. takes clauses/assignments/lemmas)
  // and pushes it on d_solvers
  void pushSolver();

  // called by checkSat and continueCheck to initiate a search
  // with a SAT solver
  CVC3::QueryResult search();



public:
  DPLLTMiniSat(TheoryAPI* theoryAPI, Decider* decider,
	       bool printStats = false, bool createProof = false);
  virtual ~DPLLTMiniSat();


  // Implementation of the DPLLT interface
  virtual CVC3::QueryResult checkSat(const CNF_Formula& cnf);
  virtual CVC3::QueryResult continueCheck(const CNF_Formula& cnf);
  virtual void push();
  virtual void pop();
  virtual void addAssertion(const CNF_Formula& cnf);
  virtual std::vector<SAT::Lit> getCurAssignments() ;
  virtual std::vector<std::vector<SAT::Lit> > getCurClauses();
  virtual Var::Val getValue(Var v);

  // if createProof was given returns a proof of the last unsatisfiable search,
  // otherwise (or if there was no unsatisfiable search) NULL
  // ownership remains with DPLLTMiniSat
  SatProof* getProof() { 
    DebugAssert((d_proof != NULL), "null proof foound") ;
    return d_proof; 
  };

  CVC3::Proof getSatProof(CNF_Manager*, CVC3::TheoryCore*) ;
};

}

#endif
