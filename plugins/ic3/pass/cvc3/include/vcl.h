/*****************************************************************************/
/*!
 * \file vcl.h
 * \brief Main implementation of ValidityChecker for CVC3.
 *
 * Author: Clark Barrett
 *
 * Created: Wed Dec 11 14:40:39 2002
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 *
 * <hr>
 *
 */
/*****************************************************************************/

#ifndef _cvc3__include__vcl_h_
#define _cvc3__include__vcl_h_

#include <queue>

#include "vc.h"
#include "command_line_flags.h"
#include "statistics.h"
#include "cdmap.h"
#include "cdlist.h"

namespace CVC3 {

  class SearchEngine;
  class Theory;
  class TheoryCore;
  class TheoryUF;
  class TheoryArith;
  class TheoryArray;
  class TheoryQuant;
  class TheoryRecords;
  class TheorySimulate;
  class TheoryBitvector;
  class TheoryDatatype;
  class Translator;

class CVC_DLL VCL : public ValidityChecker {

  //! Pointers to main system components
  ExprManager* d_em;
  ContextManager* d_cm;
  TheoremManager* d_tm;
  SearchEngine* d_se;

  //! Pointers to theories
  TheoryCore* d_theoryCore;
  TheoryUF* d_theoryUF;
  TheoryArith* d_theoryArith;
  TheoryArray* d_theoryArray;
  TheoryQuant* d_theoryQuant;
  TheoryRecords* d_theoryRecords;
  TheorySimulate* d_theorySimulate;
  TheoryBitvector* d_theoryBitvector;
  TheoryDatatype* d_theoryDatatype;
  Translator* d_translator;

  //! All theories are stored in this common vector
  /*! This includes TheoryCore and TheoryArith. */
  std::vector<Theory*> d_theories;

  //! Command line flags
  CLFlags *d_flags;

  //! User-level view of the scope stack
  CDO<int> *d_stackLevel;

  //! Run-time statistics
  Statistics* d_statistics;

  //! Next index for user assertion
  size_t d_nextIdx;

  //! Structure to hold user assertions indexed by declaration order
  class UserAssertion {
    size_t d_idx;
    Theorem d_thm; //! The theorem of the assertion (a |- a)
    Theorem d_tcc; //! The proof of its TCC
  public:
    //! Default constructor
    UserAssertion() { }
    //! Constructor
    UserAssertion(const Theorem& thm, const Theorem& tcc, size_t idx)
      : d_idx(idx), d_thm(thm), d_tcc(tcc) { }
    //! Fetching a Theorem
    const Theorem& thm() const { return d_thm; }
    //! Fetching a TCC
    const Theorem& tcc() const { return d_tcc; }
    //! Auto-conversion to Theorem
    operator Theorem() { return d_thm; }
    //! Comparison for use in std::map, to sort in declaration order
    friend bool operator<(const UserAssertion& a1, const UserAssertion& a2) {
      return (a1.d_idx < a2.d_idx);
    }
  };

  //! Backtracking map of user assertions
  CDMap<Expr,UserAssertion>* d_userAssertions;

  //! Backtracking map of assertions when assertion batching is on
  CDList<Expr>* d_batchedAssertions;

  //! Index into batched Assertions
  CDO<unsigned>* d_batchedAssertionsIdx;

  //! Result of the last query()
  /*! Saved for printing assumptions and proofs.  Normally it is
   * Theorem3, but query() on a TCC returns a 2-valued Theorem. */
  Theorem3 d_lastQuery;

  //! Result of the last query(e, true) (for a TCC).
  Theorem d_lastQueryTCC;

  //! Closure of the last query(e): |- Gamma => e
  Theorem3 d_lastClosure;

  //! Whether to dump a trace (or a translated version)
  bool d_dump;

  // Private methods

  //! Construct the closure "|-_3 Gamma => phi" of thm = "Gamma |-_3 phi"
  Theorem3 deriveClosure(const Theorem3& thm);

  //! Recursive assumption graph traversal to find user assumptions
  /*!
   *  If an assumption has a TCC, traverse the proof of TCC and add its
   *  assumptions to the set recursively.
   */
  void getAssumptionsRec(const Theorem& thm,
			 std::set<UserAssertion>& assumptions,
			 bool addTCCs);

  //! Get set of user assertions from the set of assumptions
  void getAssumptions(const Assumptions& a, std::vector<Expr>& assumptions);

  //! Check the tcc
  Theorem checkTCC(const Expr& tcc);

#ifdef _CVC3_DEBUG_MODE
    //! Print an entry to the dump file: change of scope
    void dumpTrace(int scope);
#endif

  //! Initialize everything except flags
  void init(void);
  //! Destroy everything except flags
  void destroy(void);

public:
  // Takes the vector of command line flags.
  VCL(const CLFlags& flags);
  ~VCL();

  // Implementation of vc.h virtual functions

  CLFlags& getFlags() const { return *d_flags; }
  void reprocessFlags();

  TheoryCore* core();

  Type boolType();
  Type realType();
  Type intType();
  Type subrangeType(const Expr& l, const Expr& r);
  Type subtypeType(const Expr& pred, const Expr& witness);
  Type tupleType(const Type& type0, const Type& type1);
  Type tupleType(const Type& type0, const Type& type1, const Type& type2);
  Type tupleType(const std::vector<Type>& types);
  Type recordType(const std::string& field, const Type& type);
  Type recordType(const std::string& field0, const Type& type0,
  		  const std::string& field1, const Type& type1);
  Type recordType(const std::string& field0, const Type& type0,
  		  const std::string& field1, const Type& type1,
  		  const std::string& field2, const Type& type2);
  Type recordType(const std::vector<std::string>& fields,
		  const std::vector<Type>& types);
  Type dataType(const std::string& name,
                const std::string& constructor,
                const std::vector<std::string>& selectors,
                const std::vector<Expr>& types);
  Type dataType(const std::string& name,
                const std::vector<std::string>& constructors,
                const std::vector<std::vector<std::string> >& selectors,
                const std::vector<std::vector<Expr> >& types);
  void dataType(const std::vector<std::string>& names,
                const std::vector<std::vector<std::string> >& constructors,
                const std::vector<std::vector<std::vector<std::string> > >& selectors,
                const std::vector<std::vector<std::vector<Expr> > >& types,
                std::vector<Type>& returnTypes);
  Type arrayType(const Type& typeIndex, const Type& typeData);
  Type bitvecType(int n);
  Type funType(const Type& typeDom, const Type& typeRan);
  Type funType(const std::vector<Type>& typeDom, const Type& typeRan);
  Type createType(const std::string& typeName);
  Type createType(const std::string& typeName, const Type& def);
  Type lookupType(const std::string& typeName);

  ExprManager* getEM() { return d_em; }
  Expr varExpr(const std::string& name, const Type& type);
  Expr varExpr(const std::string& name, const Type& type, const Expr& def);
  Expr lookupVar(const std::string& name, Type* type);
  Type getType(const Expr& e);
  Type getBaseType(const Expr& e);
  Type getBaseType(const Type& e);
  Expr getTypePred(const Type&t, const Expr& e);
  Expr stringExpr(const std::string& str);
  Expr idExpr(const std::string& name);
  Expr listExpr(const std::vector<Expr>& kids);
  Expr listExpr(const Expr& e1);
  Expr listExpr(const Expr& e1, const Expr& e2);
  Expr listExpr(const Expr& e1, const Expr& e2, const Expr& e3);
  Expr listExpr(const std::string& op, const std::vector<Expr>& kids);
  Expr listExpr(const std::string& op, const Expr& e1);
  Expr listExpr(const std::string& op, const Expr& e1,
  		const Expr& e2);
  Expr listExpr(const std::string& op, const Expr& e1,
		const Expr& e2, const Expr& e3);
  void printExpr(const Expr& e);
  void printExpr(const Expr& e, std::ostream& os);
  Expr parseExpr(const Expr& e);
  Type parseType(const Expr& e);
  Expr importExpr(const Expr& e);
  Type importType(const Type& t);
  void cmdsFromString(const std::string& s, InputLanguage lang);
  Expr exprFromString(const std::string& s);

  Expr trueExpr();
  Expr falseExpr();
  Expr notExpr(const Expr& child);
  Expr andExpr(const Expr& left, const Expr& right);
  Expr andExpr(const std::vector<Expr>& children);
  Expr orExpr(const Expr& left, const Expr& right);
  Expr orExpr(const std::vector<Expr>& children);
  Expr impliesExpr(const Expr& hyp, const Expr& conc);
  Expr iffExpr(const Expr& left, const Expr& right);
  Expr eqExpr(const Expr& child0, const Expr& child1);
  Expr distinctExpr(const std::vector<Expr>& children);
  Expr iteExpr(const Expr& ifpart, const Expr& thenpart, const Expr& elsepart);

  Op createOp(const std::string& name, const Type& type);
  Op createOp(const std::string& name, const Type& type, const Expr& def);
  Op lookupOp(const std::string& name, Type* type);
  Expr funExpr(const Op& op, const Expr& child);
  Expr funExpr(const Op& op, const Expr& left, const Expr& right);
  Expr funExpr(const Op& op, const Expr& child0, const Expr& child1, const Expr& child2);
  Expr funExpr(const Op& op, const std::vector<Expr>& children);

  bool addPairToArithOrder(const Expr& smaller, const Expr& bigger);
  Expr ratExpr(int n, int d);
  Expr ratExpr(const std::string& n, const std::string& d, int base);
  Expr ratExpr(const std::string& n, int base);
  Expr uminusExpr(const Expr& child);
  Expr plusExpr(const Expr& left, const Expr& right);
  Expr plusExpr(const std::vector<Expr>& children);
  Expr minusExpr(const Expr& left, const Expr& right);
  Expr multExpr(const Expr& left, const Expr& right);
  Expr powExpr(const Expr& x, const Expr& n);
  Expr divideExpr(const Expr& left, const Expr& right);
  Expr ltExpr(const Expr& left, const Expr& right);
  Expr leExpr(const Expr& left, const Expr& right);
  Expr gtExpr(const Expr& left, const Expr& right);
  Expr geExpr(const Expr& left, const Expr& right);

  Expr recordExpr(const std::string& field, const Expr& expr);
  Expr recordExpr(const std::string& field0, const Expr& expr0,
  		  const std::string& field1, const Expr& expr1);
  Expr recordExpr(const std::string& field0, const Expr& expr0,
  		  const std::string& field1, const Expr& expr1,
  		  const std::string& field2, const Expr& expr2);
  Expr recordExpr(const std::vector<std::string>& fields,
		  const std::vector<Expr>& exprs);
  Expr recSelectExpr(const Expr& record, const std::string& field);
  Expr recUpdateExpr(const Expr& record, const std::string& field,
		     const Expr& newValue);

  Expr readExpr(const Expr& array, const Expr& index);
  Expr writeExpr(const Expr& array, const Expr& index, const Expr& newValue);

  Expr newBVConstExpr(const std::string& s, int base);
  Expr newBVConstExpr(const std::vector<bool>& bits);
  Expr newBVConstExpr(const Rational& r, int len);
  Expr newConcatExpr(const Expr& t1, const Expr& t2);
  Expr newConcatExpr(const std::vector<Expr>& kids);
  Expr newBVExtractExpr(const Expr& e, int hi, int low);
  Expr newBVNegExpr(const Expr& t1);
  Expr newBVAndExpr(const Expr& t1, const Expr& t2);
  Expr newBVAndExpr(const std::vector<Expr>& kids);
  Expr newBVOrExpr(const Expr& t1, const Expr& t2);
  Expr newBVOrExpr(const std::vector<Expr>& kids);
  Expr newBVXorExpr(const Expr& t1, const Expr& t2);
  Expr newBVXorExpr(const std::vector<Expr>& kids);
  Expr newBVXnorExpr(const Expr& t1, const Expr& t2);
  Expr newBVXnorExpr(const std::vector<Expr>& kids);
  Expr newBVNandExpr(const Expr& t1, const Expr& t2);
  Expr newBVNorExpr(const Expr& t1, const Expr& t2);
  Expr newBVCompExpr(const Expr& t1, const Expr& t2);
  Expr newBVLTExpr(const Expr& t1, const Expr& t2);
  Expr newBVLEExpr(const Expr& t1, const Expr& t2);
  Expr newBVSLTExpr(const Expr& t1, const Expr& t2);
  Expr newBVSLEExpr(const Expr& t1, const Expr& t2);
  Expr newSXExpr(const Expr& t1, int len);
  Expr newBVUminusExpr(const Expr& t1);
  Expr newBVSubExpr(const Expr& t1, const Expr& t2);
  Expr newBVPlusExpr(int numbits, const std::vector<Expr>& k);
  Expr newBVPlusExpr(int numbits, const Expr& t1, const Expr& t2);
  Expr newBVMultExpr(int numbits, const Expr& t1, const Expr& t2);
  Expr newBVUDivExpr(const Expr& t1, const Expr& t2);
  Expr newBVURemExpr(const Expr& t1, const Expr& t2);
  Expr newBVSDivExpr(const Expr& t1, const Expr& t2);
  Expr newBVSRemExpr(const Expr& t1, const Expr& t2);
  Expr newBVSModExpr(const Expr& t1, const Expr& t2);
  Expr newFixedLeftShiftExpr(const Expr& t1, int r);
  Expr newFixedConstWidthLeftShiftExpr(const Expr& t1, int r);
  Expr newFixedRightShiftExpr(const Expr& t1, int r);
  Rational computeBVConst(const Expr& e);

  Expr tupleExpr(const std::vector<Expr>& exprs);
  Expr tupleSelectExpr(const Expr& tuple, int index);
  Expr tupleUpdateExpr(const Expr& tuple, int index, const Expr& newValue);
  Expr datatypeConsExpr(const std::string& constructor,
                        const std::vector<Expr>& args);
  Expr datatypeSelExpr(const std::string& selector, const Expr& arg);
  Expr datatypeTestExpr(const std::string& constructor, const Expr& arg);
  Expr boundVarExpr(const std::string& name, const std::string& uid,
		    const Type& type);
  Expr forallExpr(const std::vector<Expr>& vars, const Expr& body);
  Expr forallExpr(const std::vector<Expr>& vars, const Expr& body, const Expr& trigger);
  Expr forallExpr(const std::vector<Expr>& vars, const Expr& body,
		  const std::vector<Expr>& triggers);
  Expr forallExpr(const std::vector<Expr>& vars, const Expr& body,
                  const std::vector<std::vector<Expr> >& triggers);

  void setTriggers(const Expr& e, const std::vector<std::vector<Expr> >& triggers);
  void setTriggers(const Expr& e, const std::vector<Expr>& triggers);
  void setTrigger(const Expr& e, const Expr& trigger);
  void setMultiTrigger(const Expr& e, const std::vector<Expr>& multiTrigger);

  Expr existsExpr(const std::vector<Expr>& vars, const Expr& body);
  Op lambdaExpr(const std::vector<Expr>& vars, const Expr& body);
  Op transClosure(const Op& op);
  Expr simulateExpr(const Expr& f, const Expr& s0,
		    const std::vector<Expr>& inputs, const Expr& n);

  void setResourceLimit(unsigned limit);
  void setTimeLimit(unsigned limit);
  void assertFormula(const Expr& e);
  void registerAtom(const Expr& e);
  Expr getImpliedLiteral();
  Expr simplify(const Expr& e);
  Theorem simplifyThm(const Expr& e);
  QueryResult query(const Expr& e);
  QueryResult checkUnsat(const Expr& e);
  QueryResult checkContinue();
  QueryResult restart(const Expr& e);
  void returnFromCheck();
  void getUserAssumptions(std::vector<Expr>& assumptions);
  void getInternalAssumptions(std::vector<Expr>& assumptions);
  void getAssumptions(std::vector<Expr>& assumptions);
  void getAssumptionsUsed(std::vector<Expr>& assumptions);
  Expr getProofQuery();
  void getCounterExample(std::vector<Expr>& assumptions, bool inOrder);
  void getConcreteModel(ExprMap<Expr> & m);
  QueryResult tryModelGeneration();
  FormulaValue value(const Expr& e);
  bool inconsistent(std::vector<Expr>& assumptions);
  bool inconsistent();
  bool incomplete();
  bool incomplete(std::vector<std::string>& reasons);
  Proof getProof();
  Expr getTCC();
  void getAssumptionsTCC(std::vector<Expr>& assumptions);
  Proof getProofTCC();
  Expr getClosure();
  Proof getProofClosure();

  int stackLevel();
  void push();
  void pop();
  void popto(int stackLevel);
  int scopeLevel();
  void pushScope();
  void popScope();
  void poptoScope(int scopeLevel);
  Context* getCurrentContext();
  void reset();

  void loadFile(const std::string& fileName,
		InputLanguage lang = PRESENTATION_LANG,
		bool interactive = false,
                bool calledFromParser = false);
  void loadFile(std::istream& is,
		InputLanguage lang = PRESENTATION_LANG,
		bool interactive = false);

  Statistics& getStatistics() { return *d_statistics; }
  void printStatistics() { std::cout << *d_statistics << std::endl; }
  unsigned long getMemory(int verbosity = 0);

};

}

#endif
