/*****************************************************************************/
/*!
 * \file translator.h
 * \brief An exception to be thrown by the smtlib translator.
 * 
 * Author: Clark Barrett
 * 
 * Created: Sat Jun 25 18:03:09 2005
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

#ifndef _cvc3__translator_h_
#define _cvc3__translator_h_

#include <string>
#include <fstream>
#include <vector>
#include <map>
#include "queryresult.h"

namespace CVC3 {

class Expr;
class Type;
class ExprManager;
class ExprStream;
class TheoryCore;
class TheoryUF;
class TheoryArith;
class TheoryArray;
class TheoryQuant;
class TheoryRecords;
class TheorySimulate;
class TheoryBitvector;
class TheoryDatatype;
template <class Data> class ExprMap;

//! Used to keep track of which subset of arith is being used
typedef enum {
  NOT_USED = 0,
  TERMS_ONLY,
  DIFF_ONLY,
  LINEAR,
  NONLINEAR
} ArithLang;

//All the translation code should go here
class Translator {
  ExprManager* d_em;
  const bool& d_translate;
  const bool& d_real2int;
  const bool& d_convertArith;
  const std::string& d_convertToDiff;
  bool d_iteLiftArith;
  const std::string& d_expResult;
  const std::string& d_category;
  bool d_convertArray;
  bool d_combineAssump;

  //! The log file for top-level API calls in the CVC3 input language
  std::ostream* d_osdump;
  std::ofstream d_osdumpFile;
  std::ifstream d_tmpFile;
  bool d_dump, d_dumpFileOpen;

  bool d_intIntArray, d_intRealArray, d_intIntRealArray, d_ax, d_unknown;
  bool d_realUsed;
  bool d_intUsed;
  bool d_intConstUsed;
  ArithLang d_langUsed;
  bool d_UFIDL_ok;
  bool d_arithUsed;

  Expr* d_zeroVar;
  int d_convertToBV;

  TheoryCore* d_theoryCore;
  TheoryUF* d_theoryUF;
  TheoryArith* d_theoryArith;
  TheoryArray* d_theoryArray;
  TheoryQuant* d_theoryQuant;
  TheoryRecords* d_theoryRecords;
  TheorySimulate* d_theorySimulate;
  TheoryBitvector* d_theoryBitvector;
  TheoryDatatype* d_theoryDatatype;

  std::vector<Expr> d_dumpExprs;

  std::map<std::string, Type>* d_arrayConvertMap;
  Type* d_indexType;
  Type* d_elementType;
  Type* d_arrayType;
  std::vector<Expr> d_equalities;

  std::string fileToSMTLIBIdentifier(const std::string& filename);
  Expr preprocessRec(const Expr& e, ExprMap<Expr>& cache);
  Expr preprocess(const Expr& e, ExprMap<Expr>& cache);
  Expr preprocess2Rec(const Expr& e, ExprMap<Expr>& cache, Type desiredType);
  Expr preprocess2(const Expr& e, ExprMap<Expr>& cache);
  bool containsArray(const Expr& e);
  Expr processType(const Expr& e);

public:
  // Constructors
  Translator(ExprManager* em,
             const bool& translate,
             const bool& real2int,
             const bool& convertArith,
             const std::string& convertToDiff,
             bool iteLiftArith,
             const std::string& expResult,
             const std::string& category,
             bool convertArray,
             bool combineAssump,
             int convertToBV);
  ~Translator();

  bool start(const std::string& dumpFile);
  /*! Dump the expression in the current output language
      \param dumpOnly When false, the expression is output both when
      translating and when producing a trace of commands.  When true, the
      expression is only output when producing a trace of commands
      (i.e. ignored during translation).
   */
  void dump(const Expr& e, bool dumpOnly = false);
  bool dumpAssertion(const Expr& e);
  bool dumpQuery(const Expr& e);
  void dumpQueryResult(QueryResult qres);
  void finish();

  void setTheoryCore(TheoryCore* theoryCore) { d_theoryCore = theoryCore; }
  void setTheoryUF(TheoryUF* theoryUF) { d_theoryUF = theoryUF; }
  void setTheoryArith(TheoryArith* theoryArith) { d_theoryArith = theoryArith; }
  void setTheoryArray(TheoryArray* theoryArray) { d_theoryArray = theoryArray; }
  void setTheoryQuant(TheoryQuant* theoryQuant) { d_theoryQuant = theoryQuant; }
  void setTheoryRecords(TheoryRecords* theoryRecords) { d_theoryRecords = theoryRecords; }
  void setTheorySimulate(TheorySimulate* theorySimulate) { d_theorySimulate = theorySimulate; }
  void setTheoryBitvector(TheoryBitvector* theoryBitvector) { d_theoryBitvector = theoryBitvector; }
  void setTheoryDatatype(TheoryDatatype* theoryDatatype) { d_theoryDatatype = theoryDatatype; }

  const std::string fixConstName(const std::string& s);
  //! Returns true if expression has been printed
  /*! If false is returned, array theory should print expression as usual */
  bool printArrayExpr(ExprStream& os, const Expr& e);

  Expr zeroVar();

}; // end of class Translator

} // end of namespace CVC3 

#endif
