/*****************************************************************************/
/*!
 * \file expr_map.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Dec 11 01:22:49 GMT 2002
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
// CLASS: ExprMap<Data>
//
// AUTHOR: Sergey Berezin, 12/10/2002
//
// Abstract:
//
// An abstract interface mapping Expr values to Data.  The
// implementation is a hash table.
//
// Subclassing is NOT allowed; this would lose the iterators (can we
// fix it?  Maybe not worth the trouble.)
//
// Functions follow the style of STL 'map' container.
// 
// ExprMap<Data>()    [Default constructor] Creates an empty map
// int count(Expr e)  Counts the number of elements mapped from e.
//                    Normally, returns 0 or 1.
// Data& operator[](e)  Returns Data associated with e.  If e is not mapped,
//                    insert new Data() into ExprMap.
//                    Can be used to populate ExprMap as follows:
//          ExprMap map;
//          map[e1] = data1; map[e2] = data2; ...
//          Caveat: Data must have a default constructor and be assignable.
// void erase(Expr e)  Erase e->data mapping from ExprMap.
// void insert(Expr e, Data d)  Insert e->d mapping.
// iterator begin()   Return simple "input" iterators for ExprMap
// iterator end()     (as defined in STL)
// size_t size()      Return size of the map
// bool empty()       Check for emptiness     
///////////////////////////////////////////////////////////////////////////////
#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__expr_map_h_
#define _cvc3__expr_map_h_

#include "expr_hash.h"

namespace CVC3 {

  template<class Data>
  class ExprMap {
  private:

    typedef std::map<Expr, Data> ExprMapType;
    // Private members
    ExprMapType d_map;

  public:

    //////////////////////////////////////////////////////////////////////////
    // Class: ExprMap::iterator
    // Author: Sergey Berezin
    // Created: Tue Dec 10 16:25:19 2002
    // Description: 
    //////////////////////////////////////////////////////////////////////////

    class const_iterator: public std::iterator<std::input_iterator_tag, std::pair<Expr,Data>,std::ptrdiff_t> {
      friend class ExprMap;
    private:
      typename ExprMapType::const_iterator d_it;
      // Private constructor
      const_iterator(const typename ExprMapType::const_iterator& it)
	: d_it(it) { }
    public:
      // Default constructor
      const_iterator() { }
      // (Dis)equality
      bool operator==(const const_iterator& i) const { return d_it == i.d_it; }
      bool operator!=(const const_iterator& i) const { return d_it != i.d_it; }
      // Dereference operators.
      const std::pair<const Expr,Data>& operator*() const { return *d_it; }
      const std::pair<const Expr,Data>* operator->() const {
	return d_it.operator->();
      }
      // Prefix increment
      const_iterator& operator++() { ++d_it; return *this; }
      // Postfix increment: requires a Proxy object to hold the
      // intermediate value for dereferencing
      class Proxy {
	const std::pair<const Expr,Data>& d_pair;
      public:
	Proxy(const std::pair<Expr,Data>& pair) : d_pair(pair) { }
	std::pair<const Expr,Data> operator*() { return d_pair; }
      }; // end of class Proxy
      // Actual postfix increment: returns Proxy with the old value.
      // Now, an expression like *i++ will return the current *i, and
      // then advance the iterator.  However, don't try to use Proxy for
      // anything else.
      Proxy operator++(int) {
	Proxy tmp(*(*this));
	++(*this);
	return tmp;
      }
      // Prefix decrement
      const_iterator& operator--() { --d_it; return *this; }
    }; // end of class const_iterator

    class iterator: public std::iterator<std::input_iterator_tag, std::pair<Expr,Data>,std::ptrdiff_t> {
      friend class ExprMap;
    private:
      typename ExprMapType::iterator d_it;
      // Private constructor
      iterator(const typename ExprMapType::iterator& it)
	: d_it(it) { }
    public:
      // Default constructor
      iterator() { }
      // (Dis)equality
      bool operator==(const iterator& i) const { return d_it == i.d_it; }
      bool operator!=(const iterator& i) const { return d_it != i.d_it; }
      // Dereference operators.
      std::pair<const Expr,Data>& operator*() const { return *d_it; }
      std::pair<const Expr,Data>* operator->() const {
	return d_it.operator->();
      }
      // Prefix increment
      iterator& operator++() { ++d_it; return *this; }
      // Postfix increment: requires a Proxy object to hold the
      // intermediate value for dereferencing
      class Proxy {
	std::pair<const Expr,Data>& d_pair;
      public:
	Proxy(std::pair<const Expr,Data>& pair) : d_pair(pair) { }
	std::pair<const Expr,Data> operator*() { return d_pair; }
      }; // end of class Proxy
      // Actual postfix increment: returns Proxy with the old value.
      // Now, an expression like *i++ will return the current *i, and
      // then advance the iterator.  However, don't try to use Proxy for
      // anything else.
      Proxy operator++(int) {
	Proxy tmp(*(*this));
	++(*this);
	return tmp;
      }
      // Prefix decrement
      iterator& operator--() { --d_it; return *this; }
    }; // end of class iterator

    //////////////////////////////////////////////////////////////////////////
    // Public methods
    //////////////////////////////////////////////////////////////////////////

    // Default constructor
    ExprMap() { }
    // Copy constructor
    ExprMap(const ExprMap& map): d_map(map.d_map) { }

    // Other methods
    bool empty() const { return d_map.empty(); }
    size_t size() const { return d_map.size(); }

    size_t count(const Expr& e) const { return d_map.count(e); }
    Data& operator[](const Expr& e) { return d_map[e]; }
    void clear() { d_map.clear(); }

    void insert(const Expr& e, const Data& d) { d_map[e] = d; }
    void erase(const Expr& e) { d_map.erase(e); }

    template<class InputIterator>
      void insert(InputIterator l, InputIterator r) { d_map.insert(l,r); }

    template<class InputIterator>
      void erase(InputIterator l, InputIterator r) {
      for(; l!=r; ++l) {
	d_map.erase((*l).first);
      }
    }

    iterator begin() { return iterator(d_map.begin()); }
    iterator end() { return iterator(d_map.end()); }
    const_iterator begin() const { return const_iterator(d_map.begin()); }
    const_iterator end() const { return const_iterator(d_map.end()); }
    iterator find(const Expr& e) { return iterator(d_map.find(e)); }
    const_iterator find(const Expr& e) const { return const_iterator(d_map.find(e)); }

    friend bool operator==(const ExprMap& m1, const ExprMap& m2) {
      return m1.d_map == m2.d_map;
    }
    friend bool operator!=(const ExprMap& m1, const ExprMap& m2) {
      return !(m1 == m2);
    }
  }; // end of class ExprMap

  template<class Data>
  class ExprHashMap {
  private:

    typedef std::hash_map<Expr, Data> ExprHashMapType;
    // Private members
    ExprHashMapType d_map;

  public:

    class const_iterator: public std::iterator<std::input_iterator_tag, std::pair<Expr,Data>,std::ptrdiff_t> {
      friend class ExprHashMap;
    private:
      typename ExprHashMapType::const_iterator d_it;
      // Private constructor
      const_iterator(const typename ExprHashMapType::const_iterator& it)
	: d_it(it) { }
    public:
      // Default constructor
      const_iterator() { }
      // (Dis)equality
      bool operator==(const const_iterator& i) const { return d_it == i.d_it; }
      bool operator!=(const const_iterator& i) const { return d_it != i.d_it; }
      // Dereference operators.
      const std::pair<const Expr,Data>& operator*() const { return *d_it; }
      const std::pair<const Expr,Data>* operator->() const {
	return d_it.operator->();
      }
      // Prefix increment
      const_iterator& operator++() { ++d_it; return *this; }
      // Postfix increment: requires a Proxy object to hold the
      // intermediate value for dereferencing
      class Proxy {
	const std::pair<const Expr,Data>& d_pair;
      public:
	Proxy(const std::pair<Expr,Data>& pair) : d_pair(pair) { }
	std::pair<const Expr,Data> operator*() { return d_pair; }
      }; // end of class Proxy
      // Actual postfix increment: returns Proxy with the old value.
      // Now, an expression like *i++ will return the current *i, and
      // then advance the iterator.  However, don't try to use Proxy for
      // anything else.
      Proxy operator++(int) {
	Proxy tmp(*(*this));
	++(*this);
	return tmp;
      }
    }; // end of class const_iterator

    class iterator: public std::iterator<std::input_iterator_tag, std::pair<Expr,Data>,std::ptrdiff_t> {
      friend class ExprHashMap;
    private:
      typename ExprHashMapType::iterator d_it;
      // Private constructor
      iterator(const typename ExprHashMapType::iterator& it)
	: d_it(it) { }
    public:
      // Default constructor
      iterator() { }
      // (Dis)equality
      bool operator==(const iterator& i) const { return d_it == i.d_it; }
      bool operator!=(const iterator& i) const { return d_it != i.d_it; }
      // Dereference operators.
      std::pair<const Expr,Data>& operator*() const { return *d_it; }
      std::pair<const Expr,Data>* operator->() const {
	return d_it.operator->();
      }
      // Prefix increment
      iterator& operator++() { ++d_it; return *this; }
      // Postfix increment: requires a Proxy object to hold the
      // intermediate value for dereferencing
      class Proxy {
	std::pair<const Expr,Data>& d_pair;
      public:
	Proxy(std::pair<const Expr,Data>& pair) : d_pair(pair) { }
	std::pair<const Expr,Data> operator*() { return d_pair; }
      }; // end of class Proxy
      // Actual postfix increment: returns Proxy with the old value.
      // Now, an expression like *i++ will return the current *i, and
      // then advance the iterator.  However, don't try to use Proxy for
      // anything else.
      Proxy operator++(int) {
	Proxy tmp(*(*this));
	++(*this);
	return tmp;
      }
    }; // end of class iterator

    //////////////////////////////////////////////////////////////////////////
    // Public methods
    //////////////////////////////////////////////////////////////////////////

    //! Default constructor
    ExprHashMap() { }
    //! Constructor specifying the initial number of buckets
    ExprHashMap(size_t n): d_map(n) { }
    // Copy constructor
    ExprHashMap(const ExprHashMap& map): d_map(map.d_map) { }

    // Other methods
    bool empty() const { return d_map.empty(); }
    size_t size() const { return d_map.size(); }

    size_t count(const Expr& e) const { return d_map.count(e); }
    Data& operator[](const Expr& e) { return d_map[e]; }
    void clear() { d_map.clear(); }

    void insert(const Expr& e, const Data& d) { d_map[e] = d; }
    void erase(const Expr& e) { d_map.erase(e); }

    template<class InputIterator>
      void insert(InputIterator l, InputIterator r) { d_map.insert(l,r); }

    template<class InputIterator>
      void erase(InputIterator l, InputIterator r) {
      for(; l!=r; ++l) {
	d_map.erase((*l).first);
      }
    }

    iterator begin() { return iterator(d_map.begin()); }
    iterator end() { return iterator(d_map.end()); }
    const_iterator begin() const { return const_iterator(d_map.begin()); }
    const_iterator end() const { return const_iterator(d_map.end()); }
    iterator find(const Expr& e) { return iterator(d_map.find(e)); }
    const_iterator find(const Expr& e) const { return const_iterator(d_map.find(e)); }

    // These aren't implemented
//     friend bool operator==(const ExprHashMap& m1, const ExprHashMap& m2) {
//       return m1.d_map == m2.d_map;
//     }
//     friend bool operator!=(const ExprHashMap& m1, const ExprHashMap& m2) {
//       return !(m1 == m2);
//     }
  }; // end of class ExprHashMap

} // end of namespace CVC3

#endif
