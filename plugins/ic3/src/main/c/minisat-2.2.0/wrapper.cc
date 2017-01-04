/**************************************************************************************[minisat.cc]
Copyright (c) 2008-2010, Niklas Sorensson
              2008, Koen Claessen

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**************************************************************************************************/

#include <stdlib.h>
#include "minisat/simp/SimpSolver.h"
/* midified by Li Yong */
using namespace Minisat;

struct minisat_solver_t : public SimpSolver {
    vec<Lit> clause;
    vec<Lit> assumps;
};

extern "C" {

typedef struct minisat_solver_t minisat_solver;

// This implementation of lbool may or not may be an exact mirror of the C++ implementation:
//
extern const int minisat_l_True  = 1;
extern const int minisat_l_False = 0;
extern const int minisat_l_Undef = -1;

static inline int toC(lbool a)
{
    return a == l_True  ? minisat_l_True
         : a == l_False ? minisat_l_False
         : minisat_l_Undef;
}

static inline lbool fromC(int a)
{
    return a == minisat_l_True  ? l_True
         : a == minisat_l_False ? l_False
         : l_Undef;
}


// TODO: why are these here?
int minisat_get_l_True     (void){ return minisat_l_True; }
int minisat_get_l_False    (void){ return minisat_l_False; }
int minisat_get_l_Undef    (void){ return minisat_l_Undef; }

// Solver C-API wrapper functions:
//
__attribute__ ((visibility("default"))) 
minisat_solver* minisat_new             (void){ return new minisat_solver_t(); }
__attribute__ ((visibility("default")))
void          minisat_delete          (minisat_solver *s){ delete s; }
__attribute__ ((visibility("default")))
int   minisat_newVar          (minisat_solver *s){ return s->newVar(); }
__attribute__ ((visibility("default")))
int   minisat_newLit          (minisat_solver *s){ return toInt(mkLit(s->newVar())); }
__attribute__ ((visibility("default")))
int   minisat_mkLit           (int x){ return toInt(mkLit(x)); }
__attribute__ ((visibility("default")))
int   minisat_mkLit_args      (int x, int sign){ return toInt(mkLit(x,sign)); }
__attribute__ ((visibility("default")))
int   minisat_negate          (int p){ return toInt(~toLit(p)); }
__attribute__ ((visibility("default")))
int   minisat_var             (int p){ return var(toLit(p)); }
__attribute__ ((visibility("default")))
int          minisat_sign            (int p){ return sign(toLit(p)); }
__attribute__ ((visibility("default")))
void         minisat_addClause_begin (minisat_solver *s){ s->clause.clear(); }
__attribute__ ((visibility("default")))
void         minisat_addClause_addLit(minisat_solver *s, int p){ s->clause.push(toLit(p)); }
__attribute__ ((visibility("default")))
int          minisat_addClause_commit(minisat_solver *s){ return s->addClause_(s->clause); }
__attribute__ ((visibility("default")))
int          minisat_simplify        (minisat_solver *s){ return s->simplify(); }

// NOTE: Currently these run with default settings for implicitly calling preprocessing. Turn off
// before if you don't need it. This may change in the future.
__attribute__ ((visibility("default")))
void         minisat_solve_begin     (minisat_solver *s){ s->assumps.clear(); }
__attribute__ ((visibility("default")))
void         minisat_solve_addLit    (minisat_solver *s, int p){ s->assumps.push(toLit(p)); }
__attribute__ ((visibility("default")))
int          minisat_solve_commit    (minisat_solver *s){ return s->solve(s->assumps); }
__attribute__ ((visibility("default")))
int minisat_limited_solve_commit (minisat_solver *s){ return toC(s->solveLimited(s->assumps)); }

__attribute__ ((visibility("default")))
int          minisat_okay            (minisat_solver *s){ return s->okay(); }
__attribute__ ((visibility("default")))
void         minisat_setPolarity     (minisat_solver *s, int v, int lb){ s->setPolarity(v, fromC(lb)); }
__attribute__ ((visibility("default")))
void         minisat_setDecisionVar  (minisat_solver *s, int v, int b){ s->setDecisionVar(v, b); }
__attribute__ ((visibility("default")))
int minisat_value_Var      (minisat_solver *s, int x){ return toC(s->value(x)); }
__attribute__ ((visibility("default")))
int minisat_value_Lit      (minisat_solver *s, int p){ return toC(s->value(toLit(p))); }
__attribute__ ((visibility("default")))
int minisat_modelValue_Var (minisat_solver *s, int x){ return toC(s->modelValue(x)); }
__attribute__ ((visibility("default")))
int minisat_modelValue_Lit (minisat_solver *s, int p){ return toC(s->modelValue(toLit(p))); }
__attribute__ ((visibility("default")))
int          minisat_num_assigns     (minisat_solver *s){ return s->nAssigns(); }
__attribute__ ((visibility("default")))
int          minisat_num_clauses     (minisat_solver *s){ return s->nClauses(); }
__attribute__ ((visibility("default")))
int          minisat_num_learnts     (minisat_solver *s){ return s->nLearnts(); }
__attribute__ ((visibility("default")))
int          minisat_num_vars        (minisat_solver *s){ return s->nVars(); }
__attribute__ ((visibility("default")))
int          minisat_num_freeVars    (minisat_solver *s){ return s->nFreeVars(); }
__attribute__ ((visibility("default")))
int          minisat_conflict_len    (minisat_solver *s){ return s->conflict.size(); }
__attribute__ ((visibility("default")))
int  minisat_conflict_nthLit (minisat_solver *s, int i){ return toInt(s->conflict[i]); }
__attribute__ ((visibility("default")))
void         minisat_set_verbosity   (minisat_solver *s, int v){ s->verbosity = v; }
__attribute__ ((visibility("default")))
int          minisat_get_verbosity   (minisat_solver *s){ return s->verbosity; }
__attribute__ ((visibility("default")))
int          minisat_num_conflicts   (minisat_solver *s){ return s->conflicts; }
__attribute__ ((visibility("default")))
int          minisat_num_decisions   (minisat_solver *s){ return s->decisions; }
__attribute__ ((visibility("default")))
int          minisat_num_restarts    (minisat_solver *s){ return s->starts; }
__attribute__ ((visibility("default")))
int          minisat_num_propagations(minisat_solver *s){ return s->propagations; }
__attribute__ ((visibility("default")))
void         minisat_set_conf_budget (minisat_solver* s, int x){ s->setConfBudget(x); }
__attribute__ ((visibility("default")))
void         minisat_set_prop_budget (minisat_solver* s, int x){ s->setPropBudget(x); }
__attribute__ ((visibility("default")))
void         minisat_no_budget       (minisat_solver* s){ s->budgetOff(); }


// Resource constraints:
__attribute__ ((visibility("default")))
void minisat_interrupt(minisat_solver* s) {s->interrupt (); }
__attribute__ ((visibility("default")))
void minisat_clearInterrupt(minisat_solver* s) {s->clearInterrupt (); }

// SimpSolver methods:
__attribute__ ((visibility("default")))
void         minisat_setFrozen       (minisat_solver* s, int v, int b) { s->setFrozen(v, b); }
__attribute__ ((visibility("default")))
int minisat_isEliminated    (minisat_solver* s, int v) { return s->isEliminated(v); }
__attribute__ ((visibility("default")))
int minisat_eliminate       (minisat_solver* s, int turn_off_elim){ return s->eliminate(turn_off_elim); }

// Convenience functions for actual c-programmers (not language interfacing people):
//
__attribute__ ((visibility("default")))
int  minisat_solve(minisat_solver *s, int len, int *ps)
{
    s->assumps.clear();
    for (int i = 0; i < len; i++)
        s->assumps.push(toLit(ps[i]));
    return s->solve(s->assumps);
}

__attribute__ ((visibility("default")))
int minisat_limited_solve(minisat_solver *s, int len, int *ps)
{
    s->assumps.clear();
    for (int i = 0; i < len; i++)
        s->assumps.push(toLit(ps[i]));
    return toC(s->solveLimited(s->assumps));
}

__attribute__ ((visibility("default")))
int  minisat_addClause(minisat_solver *s, int len, int *ps)
{
    s->clause.clear();
    for (int i = 0; i < len; i++)
        s->clause.push(toLit(ps[i]));
    return s->addClause_(s->clause);
}


}
