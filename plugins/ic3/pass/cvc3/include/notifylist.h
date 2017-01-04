/*****************************************************************************/
/*!
 * \file notifylist.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Mon Jan 20 13:52:19 2003
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

#ifndef _cvc3__include__notifylist_h_ 
#define _cvc3__include__notifylist_h_  

#include "expr.h"
#include "cdlist.h"

namespace CVC3 {

  class Theory;

class NotifyList {
  CDList<Theory*> d_tlist;
  CDList<Expr> d_elist;

public:
  NotifyList(Context* c) : d_tlist(c), d_elist(c) {
    IF_DEBUG(d_elist.setName("CDList[NotifyList]");)
  }
  unsigned size() const { return d_tlist.size(); }
  void add(Theory* t, const Expr& e) { d_tlist.push_back(t); d_elist.push_back(e); }
  Theory* getTheory(int i) const { return d_tlist[i]; }
  Expr getExpr(int i) const { return d_elist[i]; }
};

}

#endif
