/*****************************************************************************/
/*!
 * \file circuit.h
 * \brief Circuit class
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

#ifndef _cvc3__include__circuit_h_
#define _cvc3__include__circuit_h_

#include "variable.h"
#include "theorem.h"

using namespace std;

namespace CVC3
{

class SearchEngineFast;

class Circuit
{
 private:
  Theorem d_thm;
  Literal d_lits[4];

 public:
  Circuit(SearchEngineFast* se, const Theorem& thm);
  bool propagate(SearchEngineFast* se);
};

} // namespace CVC3

#endif
