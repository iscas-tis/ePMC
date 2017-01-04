/*****************************************************************************/
/*!
 *\file theory_datatype_lazy.h
 *
 * Author: Clark Barrett
 *
 * Created: Wed Dec  1 22:24:32 2004
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

#ifndef _cvc3__include__theory_datatype_lazy_h_
#define _cvc3__include__theory_datatype_lazy_h_

#include "theory.h"
#include "smartcdo.h"
#include "cdmap.h"
#include "theory_datatype.h"

namespace CVC3 {

/*****************************************************************************/
/*!
 *\class TheoryDatatypeLazy
 *\ingroup Theories
 *\brief This theory handles datatypes.
 *
 * Author: Clark Barrett
 *
 * Created: Wed Dec  1 22:27:12 2004
 */
/*****************************************************************************/
class TheoryDatatypeLazy :public TheoryDatatype {

  typedef enum {
    MERGE1 = 0,
    MERGE2,
    ENQUEUE
  } ProcessKinds;

  CDList<Theorem> d_processQueue;
  CDList<ProcessKinds> d_processQueueKind;
  CDO<unsigned> d_processIndex;
  CDO<bool> d_typeComplete;

private:
  void instantiate(const Expr& e, const Unsigned& u);
  void initializeLabels(const Expr& e, const Type& t);
  void mergeLabels(const Theorem& thm, const Expr& e1, const Expr& e2);
  void mergeLabels(const Theorem& thm, const Expr& e,
                   unsigned position, bool positive);

public:
  TheoryDatatypeLazy(TheoryCore* theoryCore);
  ~TheoryDatatypeLazy() {}

  // Theory interface
  void checkSat(bool fullEffort);
  void setup(const Expr& e);
  void update(const Theorem& e, const Expr& d);

};

}

#endif
