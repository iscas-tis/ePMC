/*****************************************************************************/
/*!
 *\file smartcdo.h
 *\brief Smart context-dependent object wrapper
 *
 * Author: Clark Barrett
 *
 * Created: Fri Nov 12 17:28:58 2004
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

#ifndef _cvc3__include__smartcdo_h_
#define _cvc3__include__smartcdo_h_

#include "cdo.h"

namespace CVC3 {

/*****************************************************************************/
/*!
 *\class SmartCDO
 *\brief SmartCDO
 *
 * Author: Clark Barrett
 *
 * Created: Fri Nov 12 17:33:31 2004
 *
 * Wrapper for CDO which automatically allocates and deletes a pointer to a
 * CDO.  This allows the copy constructor and operator= to be defined which are
 * especially useful for storing CDO's in vectors.  All operations are const to
 * enable use as a member of CDLists.
 *
 * Be careful not to delete RefCDO during pop(), since this messes up
 * the backtracking data structures.  We delay the deletion by
 * registering each RefCDO to be notified before and after each pop().
 * This makes the use of SmartCDO somewhat more expensive, so use it
 * with care.
 * 
 */
/*****************************************************************************/
template <class T>
class SmartCDO {

  template <class U>
  class RefCDO {
    friend class SmartCDO;
    unsigned d_refCount;
    CDO<U> d_cdo;
    bool d_delay; //!< Whether to delay our own deletion

    class RefNotifyObj : public ContextNotifyObj {
      friend class RefCDO;
      RefCDO<U>* d_ref;
      //! Constructor
      RefNotifyObj(RefCDO<U>* ref, Context* context)
	: ContextNotifyObj(context), d_ref(ref) { }
      void notifyPre() { d_ref->d_delay = true; }
      void notify() {
	d_ref->d_delay = false;
	d_ref->kill();
      }
    };

    RefNotifyObj* d_notifyObj;

    friend class RefNotifyObj;

    RefCDO(Context* context): d_refCount(0), d_cdo(context), d_delay(false),
      d_notifyObj(new RefNotifyObj(this, context)) {}

    RefCDO(Context* context, const U& cdo, int scope = -1)
      : d_refCount(0), d_cdo(context, cdo, scope), d_delay(false),
      d_notifyObj(new RefNotifyObj(this, context)) {}

    ~RefCDO() { delete d_notifyObj; }
    //! Delete itself, unless delayed (then we'll be called again later)
    void kill() { if(d_refCount==0 && !d_delay) delete this; }
  };

  RefCDO<T>* d_data;

public:
  //! Check if the SmartCDO object is Null
  bool isNull() const { return (d_data==NULL); }
  //! Default constructor: create a Null SmartCDO object
  SmartCDO(): d_data(NULL) { }
  //! Create and initialize SmartCDO object at the current scope
  SmartCDO(Context* context)
    { d_data = new RefCDO<T>(context); d_data->d_refCount++; }
  //! Create and initialize SmartCDO object at the given scope
  SmartCDO(Context* context, const T& data, int scope = -1)
    { d_data = new RefCDO<T>(context, data, scope); d_data->d_refCount++; }
  //! Delete 
  ~SmartCDO()
    { if (isNull()) return;
      if (--d_data->d_refCount == 0) d_data->kill(); }

  SmartCDO(const SmartCDO<T>& cdo) : d_data(cdo.d_data)
    { if (!isNull()) d_data->d_refCount++; }

  SmartCDO<T>& operator=(const SmartCDO<T>& cdo)
  {
    if (this == &cdo) return *this;
    if (!isNull() && --(d_data->d_refCount)) d_data->kill();
    d_data = cdo.d_data;
    if (!isNull()) ++(d_data->d_refCount);
    return *this;
  }

  void set(const T& data, int scope=-1) const {
    DebugAssert(!isNull(), "SmartCDO::set: we are Null");
    d_data->d_cdo.set(data, scope);
  }
  const T& get() const {
    DebugAssert(!isNull(), "SmartCDO::get: we are Null");
    return d_data->d_cdo.get();
  }
  operator T() const { return get(); }
  const SmartCDO<T>& operator=(const T& data) const {set(data); return *this;}
};

}

#endif
