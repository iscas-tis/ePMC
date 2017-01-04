/*****************************************************************************/
/*!
 * \file cdo.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Wed Feb 12 17:27:43 2003
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

#ifndef _cvc3__include__cdo_h_
#define _cvc3__include__cdo_h_

#include "context.h"

namespace CVC3 {

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: CDO (Context Dependent Object)				     //
// Author: Clark Barrett                                                     //
// Created: Wed Feb 12 17:28:25 2003					     //
// Description: Generic templated class for an object which must be saved    //
//              and restored as contexts are pushed and popped.  Requires    //
//              that operator= be defined for the data class.                //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////
template <class T>
class CDO :public ContextObj {
  T d_data;

  virtual ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDO<T>(*this); }
  virtual void restoreData(ContextObj* data) {
    d_data = ((CDO<T>*)data)->d_data;
  }
  virtual void setNull(void) { d_data = T(); }

  // Disable copy constructor and operator=
  // If you need these, use smartcdo instead
  CDO(const CDO<T>& cdo): ContextObj(cdo), d_data(cdo.d_data) { }
  CDO<T>& operator=(const CDO<T>& cdo) {}

public:
  CDO(Context* context) : ContextObj(context)
    { IF_DEBUG(setName("CDO");) }
  CDO(Context* context, const T& data, int scope = -1)
    : ContextObj(context) {
    IF_DEBUG(setName("CDO"));   ; 
    set(data, scope);
  }
  ~CDO() {}
  void set(const T& data, int scope=-1) { makeCurrent(scope); d_data = data; }
  const T& get() const { return d_data; }
  operator T() { return get(); }
  CDO<T>& operator=(const T& data) { set(data); return *this; }
  
};

}

#endif
