/*****************************************************************************/
/*!
 * \file cdmap.h
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

#ifndef _cvc3__include__cdmap_h_
#define _cvc3__include__cdmap_h_

#include <iterator>
#include "context.h"

namespace CVC3 {

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// Class: CDMap (Context Dependent Map)					     //
// Author: Sergey Berezin                                                    //
// Created: Thu May 15 15:55:09 2003					     //
// Description: Generic templated class for a map which must be saved        //
//              and restored as contexts are pushed and popped.  Requires    //
//              that operator= be defined for the data class, and            //
//              operator== for the key class.   In addition, a hash<Key>     //
//              template specialization must be defined, or a hash class     //
//              explicitly provided in the template.                         //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////

// Auxiliary class: almost the same as CDO (see cdo.h), but on
// setNull() call it erases itself from the map.

template <class Key, class Data, class HashFcn = std::hash<Key> > class CDMap;

template <class Key, class Data, class HashFcn = std::hash<Key> >
class CDOmap :public ContextObj {
  Key d_key;
  Data d_data;
  bool d_inMap; // whether the data must be in the map
  CDMap<Key, Data, HashFcn>* d_cdmap;

  // Doubly-linked list for keeping track of elements in order of insertion
  CDOmap<Key, Data, HashFcn>* d_prev;
  CDOmap<Key, Data, HashFcn>* d_next;

  virtual ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDOmap<Key, Data, HashFcn>(*this); }

  virtual void restoreData(ContextObj* data) {
    CDOmap<Key, Data, HashFcn>* p((CDOmap<Key, Data, HashFcn>*)data);
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
  CDOmap(Context* context, CDMap<Key, Data, HashFcn>* cdmap,
	 const Key& key, const Data& data, int scope = -1)
    : ContextObj(context), d_key(key), d_inMap(false), d_cdmap(cdmap) {
    set(data, scope);
    IF_DEBUG(setName("CDOmap");)
    CDOmap<Key, Data, HashFcn>*& first = d_cdmap->d_first;
    if (first == NULL) {
      first = d_next = d_prev = this;
    }
    else {
      d_prev = first->d_prev;
      d_next = first;
      d_prev->d_next = first->d_prev = this;
    }
  }
  ~CDOmap() {}
  void set(const Data& data, int scope=-1) {
    makeCurrent(scope); d_data = data; d_inMap = true;
  }
  const Key& getKey() const { return d_key; }
  const Data& get() const { return d_data; }
  operator Data() { return get(); }
  CDOmap<Key, Data, HashFcn>& operator=(const Data& data) { set(data); return *this; }
  CDOmap<Key, Data, HashFcn>* next() const {
    if (d_next == d_cdmap->d_first) return NULL;
    else return d_next;
  }
}; // end of class CDOmap

// Dummy subclass of ContextObj to serve as our data class
class CDMapData : public ContextObj {
  ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDMapData(*this); }
  void restoreData(ContextObj* data) { }
  void setNull(void) { }
 public:
  CDMapData(Context* context): ContextObj(context) { }
  CDMapData(const ContextObj& co): ContextObj(co) { }
};

// The actual class CDMap
template <class Key, class Data, class HashFcn>
class CDMap: public ContextObj {
  friend class CDOmap<Key, Data, HashFcn>;
 private:
  std::hash_map<Key,CDOmap<Key, Data, HashFcn>*,HashFcn> d_map;
  // The vector of CDOmap objects to be destroyed
  std::vector<CDOmap<Key, Data, HashFcn>*> d_trash;
  CDOmap<Key, Data, HashFcn>* d_first;
  Context* d_context;

  // Nothing to save; the elements take care of themselves
  virtual ContextObj* makeCopy(ContextMemoryManager* cmm)
    { return new(cmm) CDMapData(*this); }
  // Similarly, nothing to restore
  virtual void restoreData(ContextObj* data) { }

  // Destroy stale CDOmap objects from trash
  void emptyTrash() {
    for(typename std::vector<CDOmap<Key, Data, HashFcn>*>::iterator
	  i=d_trash.begin(), iend=d_trash.end(); i!=iend; ++i) {
      delete *i;
      free(*i);
    }
    d_trash.clear();
  }

  virtual void setNull(void) {
    // Delete all the elements and clear the map
    for(typename std::hash_map<Key,CDOmap<Key, Data, HashFcn>*,HashFcn>::iterator
	  i=d_map.begin(), iend=d_map.end();
	i!=iend; ++i) {
      delete (*i).second;
      free((*i).second);
    }
    d_map.clear();
    emptyTrash();
  }
public:
  CDMap(Context* context, int scope = -1)
    : ContextObj(context), d_first(NULL), d_context(context) {
    IF_DEBUG(setName("CDMap"));   ;
  }
  ~CDMap() { setNull(); }
  // The usual operators of map
  size_t size() const { return d_map.size(); }
  size_t count(const Key& k) const { return d_map.count(k); }

  typedef CDOmap<Key, Data, HashFcn>& ElementReference;

  // If a key is not present, a new object is created and inserted
  CDOmap<Key, Data, HashFcn>& operator[](const Key& k) {
    emptyTrash();
    typename std::hash_map<Key,CDOmap<Key, Data, HashFcn>*,HashFcn>::iterator i(d_map.find(k));
    CDOmap<Key, Data, HashFcn>* obj;
    if(i == d_map.end()) { // Create new object
      obj = new(true) CDOmap<Key, Data, HashFcn>(d_context, this, k, Data());
      d_map[k] = obj;
    } else {
      obj = (*i).second;
    }
    return *obj;
  }

  void insert(const Key& k, const Data& d, int scope = -1) {
    emptyTrash();
    typename std::hash_map<Key,CDOmap<Key, Data, HashFcn>*,HashFcn>::iterator i(d_map.find(k));
    if(i == d_map.end()) { // Create new object
      CDOmap<Key, Data, HashFcn>*
	obj(new(true) CDOmap<Key, Data, HashFcn>(d_context, this, k, d, scope));
      d_map[k] = obj;
    } else {
      (*i).second->set(d, scope);
    }
  }
  // FIXME: no erase(), too much hassle to implement efficiently...

  // Iterator for CDMap: points to pair<const Key, CDOMap<Key, Data, HashFcn>&>;
  // in most cases, this will be functionally similar to pair<const Key,Data>.
  class iterator : public std::iterator<std::input_iterator_tag,std::pair<const Key, Data>,std::ptrdiff_t> {
    private:
      // Private members
      typename std::hash_map<Key,CDOmap<Key, Data, HashFcn>*,HashFcn>::const_iterator d_it;
    public:
      // Constructor from std::hash_map
      iterator(const typename std::hash_map<Key,CDOmap<Key, Data, HashFcn>*,HashFcn>::const_iterator& i)
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
	const std::pair<const Key, CDOmap<Key, Data, HashFcn>*>& p(*d_it);
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
      const CDOmap<Key, Data, HashFcn>* d_it;
    public:
      orderedIterator(const CDOmap<Key, Data, HashFcn>* p): d_it(p) {}
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

}; // end of class CDMap


}

#endif
