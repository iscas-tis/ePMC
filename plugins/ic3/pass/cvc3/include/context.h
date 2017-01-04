/*****************************************************************************/
/*!
 * \file context.h
 *
 * Author: Clark Barrett
 *
 * Created: Tue Dec 31 19:07:38 2002
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


#ifndef _cvc3__include__context_h_
#define _cvc3__include__context_h_

#include <string>
#include <vector>
#include <stdlib.h>
#include "debug.h"
#include "memory_manager_context.h"
#include "os.h"

namespace CVC3 {

/****************************************************************************/
/*! \defgroup Context Context Management
 *  \ingroup BuildingBlocks
 * Infrastructure for backtrackable data structures.
 * @{
 */
/****************************************************************************/

class Context;
class ContextManager;
class ContextNotifyObj;
class ContextObj;
class ContextObjChain;

/****************************************************************************/
/*!
 * Author: Clark Barrett
 *
 * Created: Thu Feb 13 00:19:15 2003
 *
 * A scope encapsulates the portion of a context which has changed
 * since the last call to push().  Thus, when pop() is called,
 * everything in this scope is restored to its previous state.
 */
 /****************************************************************************/

class Scope {
  friend class ContextObj;
  friend class ContextObjChain;
  friend class CDFlags;
  //! Context that created this scope
  Context* d_context;

  //! Memory manager for this scope
  ContextMemoryManager* d_cmm;

  //! Previous scope in this context
  Scope* d_prevScope;

  //! Scope level
  int d_level;

  /*! @brief Linked list of objects which are "current" in this scope,
    and thus need to be restored when the scope is deleted */
  ContextObjChain* d_restoreChain;

  //! Called by ContextObj when created
  void addToChain(ContextObjChain* obj);

public:
  //! Constructor
  Scope(Context* context, ContextMemoryManager* cmm,
        Scope* prevScope = NULL)
    : d_context(context), d_cmm(cmm), d_prevScope(prevScope),
      d_restoreChain(NULL)
    { if (prevScope) d_level = prevScope->level() + 1; else d_level = 0; }
  //! Destructor
  ~Scope() {}

  //! Access functions
  Scope* prevScope() const { return d_prevScope; }
  int level(void) const { return d_level; }
  bool isCurrent(void) const;
  Scope* topScope() const;
  Context* getContext() const { return d_context; }
  ContextMemoryManager* getCMM() const { return d_cmm; }

  void* operator new(size_t size, MemoryManager* mm)
    { return mm->newData(size); }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }
  void operator delete(void*) { }

  //! Restore all the values
  void restore(void);

  //! Called by ~ContextManager
  void finalize(void);

  //! Check for memory leaks
  void check(void);

  //! Compute memory used
  unsigned long getMemory(int verbosity);
};

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: ContextObjChain						     //
// Author: Sergey Berezin                                                    //
// Created: Wed Mar 12 11:25:22 2003					     //

/*! Description: An element of a doubly linked list holding a copy of
 * ContextObj in a scope.  It is made separate from ContextObj to keep
 * the list pointers valid in all scopes at all times, so that the
 * object can be easily removed from the list when the master
 * ContextObj is destroyed. */
///////////////////////////////////////////////////////////////////////////////
class ContextObjChain {
friend class Scope;
friend class ContextObj;
friend class CDFlags;
private:
  //! Next link in chain
  ContextObjChain* d_restoreChainNext;

  /*! @brief Pointer to the pointer of the previous object which
  points to us.  This makes a doubly-linked list for easy element
  deletion */
  ContextObjChain** d_restoreChainPrev;

  //! Pointer to the previous copy which belongs to the same master
  ContextObjChain* d_restore;

  //! Pointer to copy of master to be restored when restore() is called
  ContextObj* d_data;

  //! Pointer to the master object
  ContextObj* d_master;

  //! Private constructor (only friends can use it)
  ContextObjChain(ContextObj* data, ContextObj* master,
		  ContextObjChain* restore)
    : d_restoreChainNext(NULL), d_restoreChainPrev(NULL),
      d_restore(restore), d_data(data), d_master(master)
  { }

  //! Restore from d_data to d_master
  ContextObjChain* restore(void);
public:
  //! Destructor
  ~ContextObjChain() {}

  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }

  void operator delete(void*) { }

  // If you use this operator, you have to call free yourself when the memory
  // is freed.
  void* operator new(size_t size, bool b) {
    return malloc(size);
  }
  void operator delete(void* pMem, bool b) {
    free(pMem);
  }

  IF_DEBUG(std::string name() const;)
};

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: ContextObj							     //
// Author: Clark Barrett                                                     //
// Created: Thu Feb 13 00:21:13 2003					     //

/*!  Description: This is a generic class from which all objects that
 * are context-dependent should inherit.  Subclasses need to implement
 * makeCopy, restoreData, and setNull.
 */
///////////////////////////////////////////////////////////////////////////////
class CVC_DLL ContextObj {
friend class Scope;
friend class ContextObjChain;
friend class CDFlags;
private:
  //! Last scope in which this object was modified.
  Scope* d_scope;

  /*! @brief The list of values on previous scopes; our destructor
   *  should clean up those. */
  ContextObjChain* d_restore;

  IF_DEBUG(std::string d_name;)
  IF_DEBUG(bool d_active;)

  //! Update on the given scope, on the current scope if 'scope' == -1
  void update(int scope = -1);

protected:
  //! Copy constructor (defined mainly for debugging purposes)
  ContextObj(const ContextObj& co)
    : d_scope(co.d_scope), d_restore(co.d_restore) {
    IF_DEBUG(d_name=co.d_name;)
    DebugAssert(co.d_active, "ContextObj["+co.name()+"] copy constructor");
    IF_DEBUG(d_active = co.d_active;)
    //    TRACE("context verbose", "ContextObj()[", this, "]: copy constructor");
  }

  //! Assignment operator (defined mainly for debugging purposes)
  ContextObj& operator=(const ContextObj& co) {
    DebugAssert(false, "ContextObj::operator=(): shouldn't be called");
    return *this;
  }

  /*! @brief Make a copy of the current object so it can be restored
   * to its current state */
  virtual ContextObj* makeCopy(ContextMemoryManager* cmm) = 0;

  //! Restore the current object from the given data
  virtual void restoreData(ContextObj* data) {
    FatalAssert(false,
                "ContextObj::restoreData(): call in the base abstract class");
  }

  const ContextObj* getRestore() {
    return d_restore ? d_restore->d_data : NULL;
  }

  //! Set the current object to be invalid
  virtual void setNull(void) = 0;

  //! Return our name (for debugging)
  IF_DEBUG(virtual std::string name() const { return d_name; })

  //! Get context memory manager
  ContextMemoryManager* getCMM() { return d_scope->getCMM(); }

public:
  //! Create a new ContextObj.
  /*!
   * The initial scope is set to the bottom scope by default, to
   * reduce the work of pop() (otherwise, if the object is defined
   * only on a very high scope, its scope will be moved down with each
   * pop).  If 'atBottomScope' == false, the scope is set to the
   * current scope.
   */
  ContextObj(Context* context);
  virtual ~ContextObj();

  int level() const { return (d_scope==NULL)? 0 : d_scope->level(); }
  bool isCurrent(int scope = -1) const {
    if(scope >= 0) return d_scope->level() == scope;
    else return d_scope->isCurrent();
  }
  void makeCurrent(int scope = -1) { if (!isCurrent(scope)) update(scope); }
  IF_DEBUG(void setName(const std::string& name) { d_name=name; })

  void* operator new(size_t size, MemoryManager* mm) {
    return mm->newData(size);
  }
  void operator delete(void* pMem, MemoryManager* mm) {
    mm->deleteData(pMem);
  }

  // If you use this operator, you have to call free yourself when the memory
  // is freed.
  void* operator new(size_t size, bool b) {
    return malloc(size);
  }
  void operator delete(void* pMem, bool b) {
    free(pMem);
  }

  void operator delete(void*) { }
};

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: Context							     //
// Author: Clark Barrett                                                     //
// Created: Thu Feb 13 00:24:59 2003					     //
/*!
 * Encapsulates the general notion of stack-based saving and restoring
 * of a database.
 */
///////////////////////////////////////////////////////////////////////////////
class CVC_DLL Context {
  //! Context Manager
  ContextManager* d_cm;

  //! Name of context
  std::string d_name;

  //! Context ID
  int d_id;

  //! Pointer to top and bottom scopes of context
  Scope* d_topScope;
  Scope* d_bottomScope;

  //! List of objects to notify with every pop
  std::vector<ContextNotifyObj*> d_notifyObjList;

  //! Stack of free ContextMemoryManager's
  std::vector<ContextMemoryManager*> d_cmmStack;

public:
  Context(ContextManager* cm, const std::string& name, int id);
  ~Context();

  //! Access methods
  ContextManager* getCM() const { return d_cm; }
  const std::string& name() const { return d_name; }
  int id() const { return d_id; }
  Scope* topScope() const { return d_topScope; }
  Scope* bottomScope() const { return d_bottomScope; }
  int level() const { return d_topScope->level(); }

  void push();
  void pop();
  void popto(int toLevel);
  void addNotifyObj(ContextNotifyObj* obj) { d_notifyObjList.push_back(obj); }
  void deleteNotifyObj(ContextNotifyObj* obj);
  unsigned long getMemory(int verbosity);
};

// Have to define after Context class
inline bool Scope::isCurrent(void) const
  { return this == d_context->topScope(); }

inline void Scope::addToChain(ContextObjChain* obj) {
  if(d_restoreChain != NULL)
    d_restoreChain->d_restoreChainPrev = &(obj->d_restoreChainNext);
  obj->d_restoreChainNext = d_restoreChain;
  obj->d_restoreChainPrev = &d_restoreChain;
  d_restoreChain = obj;
}

inline Scope* Scope::topScope() const { return d_context->topScope(); }

inline void Scope::restore(void) {
  //  TRACE_MSG("context verbose", "Scope::restore() {");
  while (d_restoreChain != NULL) d_restoreChain = d_restoreChain->restore();
  //  TRACE_MSG("context verbose", "Scope::restore() }");
}

// Create a new ContextObj.  The initial scope is set to the bottom
// scope by default, to reduce the work of pop() (otherwise, if the
// object is defined only on a very high scope, its scope will be
// moved down with each pop).  If 'atBottomScope' == false, the
// scope is set to the current scope.
inline ContextObj::ContextObj(Context* context)
  IF_DEBUG(: d_name("ContextObj"))
{
  IF_DEBUG(d_active=true;)
  DebugAssert(context != NULL, "NULL context pointer");
  d_scope = context->bottomScope();
  d_restore = new(true) ContextObjChain(NULL, this, NULL);
  d_scope->addToChain(d_restore);
  //  if (atBottomScope) d_scope->addSpecialObject(d_restore);
  //  TRACE("context verbose", "ContextObj()[", this, "]");
}


/****************************************************************************/
//! Manager for multiple contexts.  Also holds current context.
/*!
 * Author: Clark Barrett
 *
 * Created: Thu Feb 13 00:26:29 2003
 */
/****************************************************************************/

class ContextManager {
  Context* d_curContext;
  std::vector<Context*> d_contexts;

public:
  ContextManager();
  ~ContextManager();

  void push() { d_curContext->push(); }
  void pop() { d_curContext->pop(); }
  void popto(int toLevel) { d_curContext->popto(toLevel); }
  int scopeLevel() { return d_curContext->level(); }
  Context* createContext(const std::string& name="");
  Context* getCurrentContext() { return d_curContext; }
  Context* switchContext(Context* context);
  unsigned long getMemory(int verbosity);
};

/****************************************************************************/
/*! Author: Clark Barrett
 *
 * Created: Sat Feb 22 16:21:47 2003
 *
 * Lightweight version of ContextObj: objects are simply notified
 * every time there's a pop. notifyPre() is called right before the
 * context is restored, and notify() is called after the context is
 * restored.
 */
/****************************************************************************/

class ContextNotifyObj {
  friend class Context;
protected:
  Context* d_context;
public:
  ContextNotifyObj(Context* context): d_context(context)
    { context->addNotifyObj(this); }
  virtual ~ContextNotifyObj() {
    // If we are being deleted before the context, remove ourselves
    // from the notify list.  However, if the context is deleted
    // before we are, then our d_context will be cleared from ~Context()
    if(d_context!=NULL) d_context->deleteNotifyObj(this);
  }
  virtual void notifyPre(void) {}
  virtual void notify(void) {}
  virtual unsigned long getMemory(int verbosity) { return sizeof(ContextNotifyObj); }
};

/*@}*/  // end of group Context

}

#endif
