/*****************************************************************************/
/*!
 *\file cdflags.h
 *\brief Context Dependent Vector of Flags
 *
 * Author: Clark Barrett
 *
 * Created: Thu Jan 26 16:37:46 2006
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

#ifndef _cvc3__include__cdflags_h_
#define _cvc3__include__cdflags_h_

#include "context.h"
#include "os.h"

namespace CVC3 {

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: CDFlags (Context Dependent Vector of Flags)			     //
// Author: Clark Barrett                                                     //
// Created: Thu Jan 26 16:37:46 2006					     //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////
class CVC_DLL CDFlags :public ContextObj {
  unsigned d_flags;

  virtual ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDFlags(*this); }
  virtual void restoreData(ContextObj* data)
    { d_flags = ((CDFlags*)data)->d_flags; }
  virtual void setNull(void) { FatalAssert(false, "Should never be called"); }

  void update(unsigned mask, int scope, bool setMask);

  // Disable copy constructor and operator=
  // If you need these, use smartcdo instead
  CDFlags(const CDFlags& cdflags): ContextObj(cdflags), d_flags(cdflags.d_flags) { }
  CDFlags& operator=(const CDFlags& cdflags) { return *this; }

public:
  CDFlags(Context* context) : ContextObj(context), d_flags(0)
    { IF_DEBUG(setName("CDFlags");) }
  ~CDFlags() {}
  void set(unsigned mask, int scope=-1) { update(mask, scope, true); }
  void clear(unsigned mask, int scope=-1) { update(mask, scope, false); }
  bool get(unsigned mask) const { return (d_flags & mask) != 0; }
};

}

#endif
