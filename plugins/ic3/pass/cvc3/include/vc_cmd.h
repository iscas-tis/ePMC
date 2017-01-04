/*****************************************************************************/
/*!
 * \file vc_cmd.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Fri Dec 13 22:35:15 2002
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

#ifndef _cvc3__vc_cvc__vc_cmd_h_
#define _cvc3__vc_cvc__vc_cmd_h_

#include <string>
#include "compat_hash_map.h"
#include "exception.h"
#include "queryresult.h"

namespace CVC3 {

  class ValidityChecker;
  class Parser;
  class Context;  
  class Expr;

  template<class Data>
  class ExprMap;

class VCCmd {
  ValidityChecker* d_vc;
  Parser* d_parser;
  // TODO: move state variables into validity checker.
  typedef std::hash_map<const char*, Context*> CtxtMap;
  std::string d_name_of_cur_ctxt;
  CtxtMap d_map;
  bool d_calledFromParser;

  //! Print the symbols in e, cache results
  void printSymbols(Expr e, ExprMap<bool>& cache);
  //! Take a parsed Expr and evaluate it
  bool evaluateCommand(const Expr& e);
  // Fetch the next command and evaluate it.  Return true if
  // evaluation was successful, false otherwise.  In especially bad
  // cases an exception may be thrown.
  bool evaluateNext();
  void findAxioms(const Expr& e, ExprMap<bool>& skolemAxioms,
		  ExprMap<bool>& visited);
  Expr skolemizeAx(const Expr& e);
  void reportResult(QueryResult qres, bool checkingValidity = true);
  void printModel();
  void printCounterExample();

public:
  VCCmd(ValidityChecker* vc, Parser* parser, bool calledFromParser=false);
  ~VCCmd();

  // Main loop function
  void processCommands();
};

}

#endif
