/*****************************************************************************/
/*!
 * \file cdmap_ordered.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Thu May 15 15:55:09 2003
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

#ifndef _cvc3__include__cdmap_ordered_h_
#define _cvc3__include__cdmap_ordered_h_

#include <map>
#include <iterator>
#include "context.h"

namespace CVC3 {

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: CDMapOrdered (Context Dependent Map)				     //
// Author: Sergey Berezin                                                    //
// Created: Thu May 15 15:55:09 2003					     //
// Description: Generic templated class for a map which must be saved        //
//              and restored as contexts are pushed and popped.  Requires    //
//              that operator= be defined for the data class, and            //
//              operator< for the key class.                                 //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////

// Auxiliary class: almost the same as CDO (see cdo.h), but on
// setNull() call it erases itself from the map.

template <class Key, class Data> class CDMapOrdered;

template <class Key, class Data>
class CDOmapOrdered :public ContextObj {
  Key d_key;
  Data d_data;
  bool d_inMap; // whether the data must be in the map
  CDMapOrdered<Key, Data>* d_cdmap;

  // Doubly-linked list for keeping track of elements in order of insertion
  CDOmapOrdered<Key,Data>* d_prev;
  CDOmapOrdered<Key,Data>* d_next;

  virtual ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDOmapOrdered<Key,Data>(*this); }

  virtual void restoreData(ContextObj* data) {
    CDOmapOrdered<Key,Data>* p((CDOmapOrdered<Key,Data>*)data);
    if(p->d_inMap) { d_data = p->d_data; d_inMap = true; }
    else setNull();
  }
  virtual void setNull(void) {
    // Erase itself from the map and put itself into trash.  We cannot
    // "delete this" here, because it will break context operations in
    // a non-trivial way.
    if(d_cdmap->d_map.count(d_key) > 0) {
      d_cdmap->d_map.erase(d_key);
      d_cdmap->d_trash.push_back(this);
    }
    d_prev->d_next = d_next;
    d_next->d_prev = d_prev;
    if (d_cdmap->d_first == this) {
      d_cdmap->d_first = d_next;
      if (d_next == this) {
        d_cdmap->d_first = NULL;
      }
    }
  }

public:
  CDOmapOrdered(Context* context, CDMapOrdered<Key, Data>* cdmap,
	 const Key& key, const Data& data, int scope = -1)
    : ContextObj(context, true /* use bottom scope */),
    d_key(key), d_inMap(false), d_cdmap(cdmap) {
    set(data, scope);
    IF_DEBUG(setName("CDOmapOrdered");)
    CDOmapOrdered<Key, Data>*& first = d_cdmap->d_first;
    if (first == NULL) {
      first = d_next = d_prev = this;
    }
    else {
      d_prev = first->d_prev;
      d_next = first;
      d_prev->d_next = first->d_prev = this;
    }
  }
  ~CDOmapOrdered() {}
  void set(const Data& data, int scope=-1) {
    makeCurrent(scope); d_data = data; d_inMap = true;
  }
  const Key& getKey() const { return d_key; }
  const Data& get() const { return d_data; }
  operator Data() { return get(); }
  CDOmapOrdered<Key,Data>& operator=(const Data& data) { set(data); return *this; }
  CDOmapOrdered<Key,Data>* next() const {
    if (d_next == d_cdmap->d_first) return NULL;
    else return d_next;
  }
}; // end of class CDOmapOrdered

// Dummy subclass of ContextObj to serve as our data class
class CDMapOrderedData : public ContextObj {
  ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDMapOrderedData(*this); }
  void restoreData(ContextObj* data) { }
  void setNull(void) { }
 public:
  CDMapOrderedData(Context* context): ContextObj(context) { }
  CDMapOrderedData(const ContextObj& co): ContextObj(co) { }
};

// The actual class CDMapOrdered
template <class Key, class Data>
class CDMapOrdered: public ContextObj {
  friend class CDOmapOrdered<Key, Data>;
 private:
  std::map<Key,CDOmapOrdered<Key,Data>*> d_map;
  // The vector of CDOmapOrdered objects to be destroyed
  std::vector<CDOmapOrdered<Key,Data>*> d_trash;
  CDOmapOrdered<Key,Data>* d_first;
  Context* d_context;
  
  // Nothing to save; the elements take care of themselves
  virtual ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDMapOrderedData(*this); }
  // Similarly, nothing to restore
  virtual void restoreData(ContextObj* data) { }
  
  // Destroy stale CDOmapOrdered objects from trash
  void emptyTrash() {
    for(typename std::vector<CDOmapOrdered<Key,Data>*>::iterator
	  i=d_trash.begin(), iend=d_trash.end(); i!=iend; ++i)
      delete *i;
    d_trash.clear();
  }

  virtual void setNull(void) {
    // Delete all the elements and clear the map
    for(typename std::map<Key,CDOmapOrdered<Key,Data>*>::iterator
	  i=d_map.begin(), iend=d_map.end();
	i!=iend; ++i) delete (*i).second;
    d_map.clear();
    emptyTrash();
  }
public:
  CDMapOrdered(Context* context, int scope = -1)
    : ContextObj(context), d_first(NULL), d_context(context) {
    IF_DEBUG(setName("CDMapOrdered"));   ; 
  }
  ~CDMapOrdered() { setNull(); }
  // The usual operators of map
  size_t size() const { return d_map.size(); }
  size_t count(const Key& k) const { return d_map.count(k); }

  // If a key is not present, a new object is created and inserted
  CDOmapOrdered<Key,Data>& operator[](const Key& k) {
    emptyTrash();
    typename std::map<Key,CDOmapOrdered<Key,Data>*>::iterator i(d_map.find(k));
    CDOmapOrdered<Key,Data>* obj;
    if(i == d_map.end()) { // Create new object
      obj = new CDOmapOrdered<Key,Data>(d_context, this, k, Data());
      d_map[k] = obj;
    } else {
      obj = (*i).second;
    }
    return *obj;
  }

  void insert(const Key& k, const Data& d, int scope = -1) {
    emptyTrash();
    typename std::map<Key,CDOmapOrdered<Key,Data>*>::iterator i(d_map.find(k));
    if(i == d_map.end()) { // Create new object
      CDOmapOrdered<Key,Data>*
	obj(new CDOmapOrdered<Key,Data>(d_context, this, k, d, scope));
      d_map[k] = obj;
    } else {
      (*i).second->set(d, scope);
    }
  }
  // FIXME: no erase(), too much hassle to implement efficiently...

  // Iterator for CDMapOrdered: points to pair<const Key, CDOMap<Key,Data>&>;
  // in most cases, this will be functionally similar to pair<const Key,Data>.
  class iterator : public std::iterator<std::input_iterator_tag,std::pair<const Key, Data>,std::ptrdiff_t> {
    private:
      // Private members
      typename std::map<Key,CDOmapOrdered<Key,Data>*>::const_iterator d_it;
    public:
      // Constructor from std::map
      iterator(const typename std::map<Key,CDOmapOrdered<Key,Data>*>::const_iterator& i)
      : d_it(i) { }
      // Copy constructor
      iterator(const iterator& i): d_it(i.d_it) { }
      // Default constructor
      iterator() { }
      // (Dis)equality
      bool operator==(const iterator& i) const {
	return d_it == i.d_it;
      }
      bool operator!=(const iterator& i) const {
	return d_it != i.d_it;
      }
      // Dereference operators.
      std::pair<const Key, Data> operator*() const {
	const std::pair<const Key, CDOmapOrdered<Key,Data>*>& p(*d_it);
	return std::pair<const Key, Data>(p.first, *p.second);
      }
      // Who needs an operator->() for maps anyway?...
      // It'd be nice, but not possible by design.
      //std::pair<const Key,Data>* operator->() const {
      //  return &(operator*());
      //}
      

      // Prefix and postfix increment
      iterator& operator++() { ++d_it; return *this; }
      // Postfix increment: requires a Proxy object to hold the
      // intermediate value for dereferencing
      class Proxy {
	const std::pair<const Key, Data>* d_pair;
      public:
	Proxy(const std::pair<const Key, Data>& p): d_pair(&p) { }
	std::pair<const Key, Data>& operator*() { return *d_pair; }
      };
      // Actual postfix increment: returns Proxy with the old value.
      // Now, an expression like *i++ will return the current *i, and
      // then advance the iterator.  However, don't try to use Proxy for
      // anything else.
      Proxy operator++(int) {
	Proxy e(*(*this));
	++(*this);
	return e;
      }
  };

  iterator begin() const { return iterator(d_map.begin()); }
  iterator end() const { return iterator(d_map.end()); }

  class orderedIterator {
      const CDOmapOrdered<Key,Data>* d_it;
    public:
      orderedIterator(const CDOmapOrdered<Key,Data>* p): d_it(p) {}
      orderedIterator(const orderedIterator& i): d_it(i.d_it) { }
      // Default constructor
      orderedIterator() { }
      // (Dis)equality
      bool operator==(const orderedIterator& i) const {
	return d_it == i.d_it;
      }
      bool operator!=(const orderedIterator& i) const {
	return d_it != i.d_it;
      }
      // Dereference operators.
      std::pair<const Key, Data> operator*() const {
	return std::pair<const Key, Data>(d_it->getKey(), d_it->get());
      }

      // Prefix and postfix increment
      orderedIterator& operator++() { d_it = d_it->next(); return *this; }
      // Postfix increment: requires a Proxy object to hold the
      // intermediate value for dereferencing
      class Proxy {
	const std::pair<const Key, Data>* d_pair;
      public:
	Proxy(const std::pair<const Key, Data>& p): d_pair(&p) { }
	std::pair<const Key, Data>& operator*() { return *d_pair; }
      };
      // Actual postfix increment: returns Proxy with the old value.
      // Now, an expression like *i++ will return the current *i, and
      // then advance the orderedIterator.  However, don't try to use Proxy for
      // anything else.
      Proxy operator++(int) {
	Proxy e(*(*this));
	++(*this);
	return e;
      }
  };

  orderedIterator orderedBegin() const { return orderedIterator(d_first); }
  orderedIterator orderedEnd() const { return orderedIterator(NULL); }

  iterator find(const Key& k) const { return iterator(d_map.find(k)); }

}; // end of class CDMapOrdered


}

#endif
