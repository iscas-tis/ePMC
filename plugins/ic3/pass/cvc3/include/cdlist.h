/*****************************************************************************/
/*!
 * \file cdlist.h
 * 
 * Author: Clark Barrett
 * 
 * Created: Wed Feb 12 18:45:26 2003
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

#ifndef _cvc3__include__cdlist_h_
#define _cvc3__include__cdlist_h_

#include "context.h"
#include <deque>

namespace CVC3 {

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: CDList (Context Dependent List)				     //
// Author: Clark Barrett                                                     //
// Created: Wed Feb 12 17:28:25 2003					     //
// Description: Generic templated class for list which grows monotonically   //
//              over time (if the context is not popped) but must also be    //
//              saved and restored as contexts are pushed and popped.        //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////
// TODO: more efficient implementation
template <class T>
class CDList :public ContextObj {
  //! The actual data.  
  /*! Use deque because it doesn't create/destroy data on resize.
    This pointer is only non-NULL in the master copy. */
  std::deque<T>* d_list; // 
  unsigned d_size;

  virtual ContextObj* makeCopy(ContextMemoryManager* cmm) { return new(cmm) CDList<T>(*this); }
  virtual void restoreData(ContextObj* data)
    { d_size = ((CDList<T>*)data)->d_size;
      while (d_list->size() > d_size) d_list->pop_back(); }
  virtual void setNull(void)
    { while (d_list->size()) d_list->pop_back(); d_size = 0; }

  // Copy constructor (private).  Do NOT copy d_list.  It's not used
  // in restore, and it will be deleted in destructor.
  CDList(const CDList<T>& l): ContextObj(l), d_list(NULL), d_size(l.d_size) { }
public:
  CDList(Context* context) : ContextObj(context), d_size(0) {
    d_list = new std::deque<T>();
    IF_DEBUG(setName("CDList");)
  }
  virtual ~CDList() { if(d_list != NULL) delete d_list; }
  unsigned size() const { return d_size; }
  bool empty() const { return d_size == 0; }
  T& push_back(const T& data, int scope = -1)
   { makeCurrent(scope); d_list->push_back(data); ++d_size; return d_list->back(); }
  void pop_back()
  { DebugAssert(isCurrent() && getRestore() &&
                d_size > ((CDList<T>*)getRestore())->d_size, "pop_back precond violated");
    d_list->pop_back(); --d_size; }
  const T& operator[](unsigned i) const {
    DebugAssert(i < size(),
		"CDList["+int2string(i)+"]: i < size="+int2string(size()));
    return (*d_list)[i];
  }
  const T& at(unsigned i) const {
    DebugAssert(i < size(),
		"CDList["+int2string(i)+"]: i < size="+int2string(size()));
    return (*d_list)[i];
  }
  const T& back() const {
    DebugAssert(size() > 0,
		"CDList::back(): size="+int2string(size()));
    return d_list->back();
  }
  typedef typename std::deque<T>::const_iterator const_iterator;
  const_iterator begin() const {
    return d_list->begin();
  }
  const_iterator end() const {
    return begin() + d_size;
  }
};

}

#endif
