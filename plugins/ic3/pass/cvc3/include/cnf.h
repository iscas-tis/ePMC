/*****************************************************************************/
/*!
 *\file cnf.h
 *\brief Basic classes for reasoning about formulas in CNF
 *
 * Author: Clark Barrett
 *
 * Created: Mon Dec 12 20:32:33 2005
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

#ifndef _cvc3__include__cnf_h_
#define _cvc3__include__cnf_h_

#include <deque>
#include "compat_hash_map.h"
#include "cvc_util.h"
#include "cdo.h"
#include "cdlist.h"
#include "theorem.h"


namespace SAT {

class Var {
  int d_index;
public:
  enum Val { UNKNOWN = -1, FALSE_VAL, TRUE_VAL};
  static Val invertValue(Val);
  Var() : d_index(-1) {}
  Var(int index) :d_index(index) {}
  operator int() { return d_index; }
  bool isNull() const { return d_index == -1; }
  void reset() { d_index = -1; }
  int getIndex() const { return d_index; }
  bool isVar() const { return d_index > 0; }
  bool operator==(const Var& var) const { return (d_index == var.d_index); }
};
inline Var::Val Var::invertValue(Var::Val v)
{ return v == Var::UNKNOWN ? Var::UNKNOWN : Var::Val(1-v); }

class Lit {
  int d_index;
  static Lit mkLit(int index) { Lit l; l.d_index = index; return l; }
public:
  Lit() : d_index(0) {}
  explicit Lit(Var v, bool positive=true) {
    if (v.isNull()) d_index = 0;
    else d_index = positive ? v+1 : -v-1;
  }
  static Lit getTrue() { return mkLit(1); }
  static Lit getFalse() { return mkLit(-1); }

  bool isNull() const { return d_index == 0; }
  bool isPositive() const { return d_index > 1; }
  bool isInverted() const { return d_index < -1; }
  bool isFalse() const { return d_index == -1; }
  bool isTrue() const { return d_index == 1; }
  bool isVar() const { return abs(d_index) > 1; }
  int getID() const { return d_index; }
  Var getVar() const {
    DebugAssert(isVar(),"Bad call to Lit::getVar");
    return abs(d_index)-1;
  }
  void reset() { d_index = 0; }
  friend Lit operator!(const Lit& lit) { return mkLit(-lit.d_index); }
};

class Clause {
  int d_satisfied:1;
  int d_unit:1;
  std::vector<Lit> d_lits; 
  CVC3::Theorem d_reason; //the theorem for the clause, used in proofs. by yeting

 public:

  Clause(): d_satisfied(0), d_unit(0) { };
    
  Clause(const Clause& clause)
    : d_satisfied(clause.d_satisfied), d_unit(clause.d_unit),
      d_lits(clause.d_lits), d_reason(clause.d_reason) { };

  typedef std::vector<Lit>::const_iterator const_iterator;
  const_iterator begin() const { return d_lits.begin(); }
  const_iterator end() const { return d_lits.end(); }

  void clear() { d_satisfied = d_unit = 0; d_lits.clear(); }
  unsigned size() const { return d_lits.size(); }
  void addLiteral(Lit l) { if (!d_satisfied) d_lits.push_back(l); }
  unsigned getMaxVar() const;
  bool isSatisfied() const { return d_satisfied != 0; }
  bool isUnit() const { return d_unit != 0; }
  bool isNull() const { return d_lits.size() == 0; }
  void setSatisfied() { d_satisfied = 1; }
  void setUnit() { d_unit = 1; }
  void print() const;
  void setClauseTheorem(CVC3::Theorem thm){ d_reason = thm;}

  CVC3::Theorem getClauseTheorem() const { return d_reason;}
};


class CNF_Formula {
protected:
  Clause* d_current;

  virtual void setNumVars(unsigned numVars) = 0;
  void copy(const CNF_Formula& cnf);

public:
  CNF_Formula() : d_current(NULL) {}
  virtual ~CNF_Formula() {}

  typedef std::deque<Clause>::const_iterator const_iterator;

  virtual bool empty() const = 0;
  virtual const Clause& operator[](int i) const = 0;
  virtual const_iterator begin() const = 0;
  virtual const_iterator end() const = 0;
  virtual unsigned numVars() const = 0;
  virtual unsigned numClauses() const = 0;
  virtual void newClause() = 0;
  virtual void registerUnit() = 0;

  void addLiteral(Lit l, bool invert=false)
    { if (l.isVar() && unsigned(l.getVar()) > numVars())
        setNumVars(l.getVar());
      d_current->addLiteral(invert ? !l : l); }
  Clause& getCurrentClause() { return *d_current; }
  void print() const;
  const CNF_Formula& operator+=(const CNF_Formula& cnf);
  const CNF_Formula& operator+=(const Clause& c);
};


class CNF_Formula_Impl :public CNF_Formula {
  std::hash_map<int, bool> d_lits;
  std::deque<Clause> d_formula;
  unsigned d_numVars;
private:
  void setNumVars(unsigned numVars) { d_numVars = numVars; }
public:
  CNF_Formula_Impl() : CNF_Formula(), d_numVars(0) {}
  CNF_Formula_Impl(const CNF_Formula& cnf) : CNF_Formula() { copy(cnf); }
  ~CNF_Formula_Impl() {};

  bool empty() const { return d_formula.empty(); }
  const Clause& operator[](int i) const { return d_formula[i]; }
  const_iterator begin() const { return d_formula.begin(); }
  const_iterator end() const { return d_formula.end(); }
  unsigned numVars() const { return d_numVars; }
  unsigned numClauses() const { return d_formula.size(); }
  void deleteLast() { DebugAssert(d_formula.size() > 0, "size == 0"); d_formula.pop_back(); }
  void newClause();
  void registerUnit();

  void simplify();
  void reset();
};


class CD_CNF_Formula :public CNF_Formula {
  CVC3::CDList<Clause> d_formula;
  CVC3::CDO<unsigned> d_numVars;
private:
  void setNumVars(unsigned numVars) { d_numVars = numVars; }
public:
  CD_CNF_Formula(CVC3::Context* context)
    : CNF_Formula(), d_formula(context), d_numVars(context, 0, 0) {}
  ~CD_CNF_Formula() {}

  bool empty() const { return d_formula.empty(); }
  const Clause& operator[](int i) const { return d_formula[i]; }
  const_iterator begin() const { return d_formula.begin(); }
  const_iterator end() const { return d_formula.end(); }
  unsigned numVars() const { return d_numVars.get(); }
  unsigned numClauses() const { return d_formula.size(); }
  void deleteLast() { d_formula.pop_back(); }

  void newClause();
  void registerUnit();
};

}

#endif
