/*****************************************************************************/
/*!
 *\file hash_set.h
 *\brief hash map implementation
 *
 * Author: Alexander Fuchs
 *
 * Created: Thu Oct 19 11:04:00 2006
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 * 
 * <hr>
 */
/*****************************************************************************/

/*
 * Copyright (c) 1996,1997
 * Silicon Graphics Computer Systems, Inc.
 *
 * Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and
 * that both that copyright notice and this permission notice appear
 * in supporting documentation.  Silicon Graphics makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 *
 *
 * Copyright (c) 1994
 * Hewlett-Packard Company
 *
 * Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and
 * that both that copyright notice and this permission notice appear
 * in supporting documentation.  Hewlett-Packard Company makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 *
 */

// this implementation is in essence a subset of the SGI implementation:
// http://www.sgi.com/tech/stl/stl_hash_set.h

#ifndef _cvc3__hash__hash_set_h_
#define _cvc3__hash__hash_set_h_


#include "hash_fun.h"
#include "hash_table.h"

namespace Hash {

  // identity is an extension taken from the SGI
  // implementation of the STL file functional:
  // http://www.sgi.com/tech/stl/stl_function.h
  template <class _Tp>
  struct _Identity : public std::unary_function<_Tp,_Tp> {
    const _Tp& operator()(const _Tp& __x) const { return __x; }
  };


  /*! hash set implementation based on the sgi interface:
    http://www.sgi.com/tech/stl/hash_set.html

    _Key: hash key type
    _HashFcn: functional class providing a hash function: size_type (_Key)
    _EqualKey: functional class providing a comparison function: bool(_Key, _Key)
      returns true iff two keys are considered to be equal
  */
  template <class _Key, class _HashFcn = hash<_Key>,
	    class _EqualKey = std::equal_to<_Key> >
  class hash_set {

  /// types
  protected:
  typedef hash_table<_Key, _Key, _HashFcn, _EqualKey, _Identity<_Key> > _hash_table;

  public:
    // typedefs as custom for other implementations
    typedef typename _hash_table::size_type size_type;
    typedef typename _hash_table::key_type key_type;
    typedef typename _hash_table::value_type value_type;
    typedef typename _hash_table::hasher hasher;
    typedef typename _hash_table::key_equal key_equal;

  public:
    // iterators
    typedef typename _hash_table::iterator iterator;
    typedef typename _hash_table::const_iterator const_iterator;



  /// variables

  protected:
    // the hash table
    _hash_table d_table;


  /// methods

  public:
    /// constructors

    // default size is 16 buckets
    hash_set() :
      d_table()
    { };
    
    // specifiy initial number of buckets - must be positive
    hash_set(size_type initial_capacity) : 
      d_table(initial_capacity)
    { };

    // specifiy initial number of buckets and hash function
    hash_set(size_type initial_capacity, const _HashFcn& hash) : 
      d_table(initial_capacity, hash)
    { };

    // specifiy initial number of buckets, hash and equal function
    hash_set(size_type initial_capacity,
	     const _HashFcn& hash, const _EqualKey& equal) : 
      d_table(initial_capacity, hash, equal)
    { };

    // copy hash map.
    hash_set(const hash_set& other) :
      d_table(other.d_table)
    { };
    
    // assign hash map
    hash_set& operator=(const hash_set& other) {
      if (this != &other) {
	d_table = other.d_table;
      }

      return *this;
    }

    void swap(hash_set& other) {
      d_table.swap(other.d_table);
    }

    // removes all entries, number of buckets is not reduced.
    void clear() {
      d_table.clear();
    };



    /// operations

    
    // returns end iterator if key was not bound
    iterator find(const key_type& key) {
      return d_table.find(key);
    }
    
    // const version of find
    const_iterator find(const key_type& key) const {
      return d_table.find(key);
    }


    // adds the mapping from key to data, if key is still unbound
    // otherwise returns false
    std::pair<iterator, bool> insert(const value_type& entry) {
      return d_table.insert(entry);
    }

    // removes binding of key
    // returns number of keys removed,
    // i.e. 1 if key was bound, 0 if key was not bound.
    size_type erase(const key_type& key) {
      return d_table.erase(key);
    }



    /// status

    // is the key bound?
    bool contains(const key_type& key) const {
      return d_table.contains(key);
    }

    // returns the number of times a key is bound,
    // i.e. 0 or 1
    size_type count(const _Key& key) const {
      return d_table.count(key);
    }
  
    // is the hash map empty?
    bool empty() const {
      return d_table.empty();
    }

    // the number of elements in the hash map
    size_type size() const {
      return d_table.size();
    }

    // the number of buckets in the hash map
    size_type bucket_count() const {
      return d_table.bucket_count();
    }

    // returns the average number of elements per bucket
    float load_factor() const {
      return d_table.load_factor();
    }



    /// iterators

    // returns forward iterator to iterate over all key/data pairs
    iterator begin() {
      return d_table.begin();
    }

    // const version of begin
    const_iterator begin() const {
      return d_table.begin();
    }


    // returns end iterator
    iterator end() {
      return d_table.end();
    }
    
    // const version of end
    const_iterator end() const {
      return d_table.end();
    }
  };

}

#endif
