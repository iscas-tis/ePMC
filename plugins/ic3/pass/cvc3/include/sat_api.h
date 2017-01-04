///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// File: sat_api.h      						     //
// Author: Clark Barrett                                                     //
// Created: Tue Oct 22 11:30:54 2002					     //
// Description: Generic enhanced SAT API                                     //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////

#ifndef _SAT_API_H_
#define _SAT_API_H_

#include <vector>
#include <iostream>

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: SAT    							     //
// Author: Clark Barrett                                                     //
// Created: Tue Oct 22 12:02:53 2002					     //
// Description: API for generic SAT solver with enhanced interface features. //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////
class SatSolver {
public:
  typedef enum SATStatus {
    UNKNOWN,
    UNSATISFIABLE,
    SATISFIABLE,
    BUDGET_EXCEEDED,
    OUT_OF_MEMORY
  } SATStatus;

  // Constructor and Destructor
  SatSolver() {}
  virtual ~SatSolver() {}

  // Implementation must provide this function
  static SatSolver *Create();

  /////////////////////////////////////////////////////////////////////////////
  // Variables, Literals, and Clauses                                        //
  /////////////////////////////////////////////////////////////////////////////

  // Variables, literals and clauses are all simple union classes.  This makes
  // it easy to use integers or pointers to some more complex data structure
  // for the implementation while at the same time increasing safety by
  // imposing strict type requirements on users of the API.
  // The value -1 is reserved to represent an empty or NULL value

  union Var {
    long id;
    void *vptr;
    Var() : id(-1) {}
    bool IsNull() { return id == -1; }
    void Reset() { id = -1; }
  };

  union Lit {
    long id;
    void *vptr;
    Lit() : id(-1) {}
    bool IsNull() { return id == -1; }
    void Reset() { id = -1; }
  };

  union Clause {
    long id;
    void *vptr;
    Clause() : id(-1) {}
    bool IsNull() { return id == -1; }
    void Reset() { id = -1; }
  };

  // Return total number of variables
  virtual int NumVariables()=0;

  // Returns the first of nvar new variables.
  virtual Var AddVariables(int nvars)=0;

  // Return a new variable
  Var AddVariable() { return AddVariables(1); }

  // Get the varIndexth variable.  varIndex must be between 1 and
  // NumVariables() inclusive.
  virtual Var GetVar(int varIndex)=0;

  // Return the index (between 1 and NumVariables()) of v.
  virtual int GetVarIndex(Var v)=0;

  // Get the first variable.  Returns a NULL Var if there are no variables.
  virtual Var GetFirstVar()=0;

  // Get the next variable after var.  Returns a NULL Var if var is the last
  // variable.
  virtual Var GetNextVar(Var var)=0;

  // Return a literal made from the given var and phase (0 is positive phase, 1
  // is negative phase).
  virtual Lit MakeLit(Var var, int phase)=0;

  // Get var from literal.
  virtual Var GetVarFromLit(Lit lit)=0;

  // Get phase from literal ID.
  virtual int GetPhaseFromLit(Lit lit)=0;

  // Return total number of clauses
  virtual int NumClauses()=0;

  // Add a new clause.  Lits is a vector of literal ID's.  Note that this
  // function can be called at any time, even after the search for solution has
  // started.  A clause ID is returned which can be used to refer to this
  // clause in the future.
  virtual Clause AddClause(std::vector<Lit>& lits)=0;

  // Delete a clause.  This can only be done if the clause has unassigned
  // literals and it must delete not only the clause in question, but
  // any learned clauses which depend on it.  Since this may be difficult to
  // implement, implementing this function is not currently required.
  // DeleteClause returns true if the clause was successfully deleted, and
  // false otherwise.
  virtual bool DeleteClause(Clause clause) { return false; }

  // Get the clauseIndexth clause.  clauseIndex must be between 0 and
  // NumClauses()-1 inclusive.
  virtual Clause GetClause(int clauseIndex)=0;

  // Get the first clause.  Returns a NULL Clause if there are no clauses.
  virtual Clause GetFirstClause()=0;

  // Get the next clause after clause.  Returns a NULL Clause if clause is
  // the last clause.
  virtual Clause GetNextClause(Clause clause)=0;

  // Returns in lits the literals that make up clause.  lits is assumed to be
  // empty when the function is called.
  virtual void GetClauseLits(Clause clause, std::vector<Lit>* lits)=0;


  /////////////////////////////////////////////////////////////////////////////
  // Checking Satisfiability and Retrieving Solutions                        //
  /////////////////////////////////////////////////////////////////////////////


  // Main check for satisfiability.  The parameter allowNewClauses tells the
  // solver whether to expect additional clauses to be added by the API during
  // the search for a solution.  The default is that no new clauses will be
  // added.  If new clauses can be added, then certain optimizations such as
  // the pure literal rule must be disabled.
  virtual SATStatus Satisfiable(bool allowNewClauses=false)=0;

  // Get current value of variable. -1=unassigned, 0=false, 1=true
  virtual int GetVarAssignment(Var var)=0;

  // After Satisfiable has returned with a SATISFIABLE result, this function
  // may be called to search for the next satisfying assignment.  If one is
  // found then SATISFIABLE is returned.  If there are no more satisfying
  // assignments then UNSATISFIABLE is returned.
  virtual SATStatus Continue()=0;

  // Pop all decision levels and remove all assignments, but do not delete any
  // clauses
  virtual void Restart()=0;

  // Pop all decision levels, remove all assignments, and delete all clauses.
  virtual void Reset()=0;


  /////////////////////////////////////////////////////////////////////////////
  // Advanced Features                                                       //
  /////////////////////////////////////////////////////////////////////////////


  // The following four methods allow callback functions to be registered.
  // Each function that is registered may optionally provide a cookie (void *)
  // which will be passed back to that function whenever it is called.

  // Register a function f which is called every time the decision level
  // increases or decreases (i.e. every time the stack is pushed or popped).
  // The argument to f is the change in decision level.  For example, +1 is a
  // Push, -1 is a Pop.
  virtual void RegisterDLevelHook(void (*f)(void *, int), void *cookie)=0;

  // Register a function to replace the built-in decision heuristics.  Every
  // time a new decision needs to be made, the solver will call this function.
  // The function should return a literal which should be set to true.  If the
  // bool pointer is returned with the value false, then a literal was
  // successfully chosen.  If the value is true, this signals the solver to
  // exit with a satisfiable result.  If the bool value is false and the
  // literal is NULL, then this signals the solver to use its own internal
  // method for making the next decision.
  virtual void RegisterDecisionHook(Lit (*f)(void *, bool *), void *cookie)=0;

  // Register a function which is called every time the value of a variable is
  // changed (i.e. assigned or unassigned).  The first parameter is the
  // variable ID which has changed.  The second is the new value: -1=unassigned,
  // 0=false, 1=true
  virtual void RegisterAssignmentHook(void (*f)(void *, Var, int),
				      void *cookie)=0;

  // Register a function which will be called after Boolean propagation and
  // before making a new decision.  Note that the hook function may add new
  // clauses and this should be handled correctly.
  virtual void RegisterDeductionHook(void (*f)(void *), void *cookie)=0;


  /////////////////////////////////////////////////////////////////////////////
  // Setting Parameters                                                      //
  /////////////////////////////////////////////////////////////////////////////


  // Implementations are not required to implement any of these
  // parameter-adjusting routines.  Each function will return true if the request
  // is successful and false otherwise.

  // Implementation will define budget.  An example budget would be time.
  virtual bool SetBudget(int budget)      { return false; }

  // Set memory limit in bytes.
  virtual bool SetMemLimit(int mem_limit) { return false; }

  // Set parameters controlling randomness.  Implementation defines what this
  // means.
  virtual bool SetRandomness(int n)       { return false; }
  virtual bool SetRandSeed(int seed)      { return false; }

  // Enable or disable deletion of conflict clauses to help control memory.
  virtual bool EnableClauseDeletion()     { return false; }
  virtual bool DisableClauseDeletion()    { return false; }


  ///////////////////////////////////////////////////////////////////////////////
  // Statistics                                                                //
  ///////////////////////////////////////////////////////////////////////////////


  // As with the parameter functions, the statistics-gathering functions may or
  // may not be implemented.  They return -1 if not implemented, and the
  // correct value otherwise.

  // Return the amount of the budget (set by SetBudget) which has been used
  virtual int GetBudgetUsed()         { return -1; }

  // Return the amount of memory in use
  virtual int GetMemUsed()            { return -1; }

  // Return the number of decisions made so far
  virtual int GetNumDecisions()       { return -1; }

  // Return the number of conflicts (equal to the number of backtracks)
  virtual int GetNumConflicts()       { return -1; }

  // Return the number of conflicts generated by the registered external
  // conflict generator
  virtual int GetNumExtConflicts()    { return -1; }

  // Return the elapsed CPU time (in seconds) since the call to Satisfiable()
  virtual float GetTotalTime()        { return -1; }

  // Return the CPU time spent (in seconds) inside the SAT solver
  // (as opposed to in the registered hook functions)
  virtual float GetSATTime()          { return -1; }

  // Return the total number of literals in all clauses
  virtual int GetNumLiterals()        { return -1; }

  // Return the number of clauses that were deleted
  virtual int GetNumDeletedClauses()  { return -1; }

  // Return the total number of literals in all deleted clauses
  virtual int GetNumDeletedLiterals() { return -1; }

  // Return the number of unit propagations
  virtual int GetNumImplications()    { return -1; }

  // Return the maximum decision level reached
  virtual int GetMaxDLevel()          { return -1; }

  // Print all implemented statistics
  void PrintStatistics(std::ostream & os = std::cout);

};

#endif
