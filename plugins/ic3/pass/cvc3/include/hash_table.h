/*****************************************************************************/
/*!
 *\file hash_table.h
 *\brief hash table implementation
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
// http://www.sgi.com/tech/stl/stl_hashtable.h

#ifndef _cvc3__hash__hash_table_h_
#define _cvc3__hash__hash_table_h_

#include <vector>
#include <string>
#include <functional>
#include <algorithm>
#include "hash_fun.h"
#include "os.h"

// For some reason, including debug.h doesn't work--so redeclare macros here

#ifdef _CVC3_DEBUG_MODE
#define DebugAssert(cond, str) if(!(cond)) \
 CVC3::debugError(__FILE__, __LINE__, #cond, str)
namespace CVC3 {
extern CVC_DLL void debugError(const std::string& file, int line,
                       const std::string& cond, const std::string& msg);
}
#else
#define DebugAssert(cond, str)
#endif

namespace Hash {
  // get size_t from hash_fun, which gets it from cstddef
  typedef size_t size_type;

    /// primes for increasing the hash table size

  // Note: assumes size_type is unsigned and at least 32 bits.
  const size_type num_primes = 28;
  
  static const size_type prime_list[num_primes] = {
    53ul,         97ul,         193ul,       389ul,       769ul,
    1543ul,       3079ul,       6151ul,      12289ul,     24593ul,
    49157ul,      98317ul,      196613ul,    393241ul,    786433ul,
    1572869ul,    3145739ul,    6291469ul,   12582917ul,  25165843ul,
    50331653ul,   100663319ul,  201326611ul, 402653189ul, 805306457ul, 
    1610612741ul, 3221225473ul, 4294967291ul
  };
  
  inline size_type next_prime(size_type n)
  {
    const size_type* first = prime_list;
    const size_type* last = prime_list + (size_type)num_primes;
    const size_type* pos = std::lower_bound(first, last, n);
    return pos == last ? *(last - 1) : *pos;
  }


  /*! template to instante to hash map and hash set

  based on the sgi implementation:
  http://www.sgi.com/tech/stl/HashedAssociativeContainer.html

    _Key: hash key type
    _Data: key + value data to store
    _HashFcn: functional class providing a hash function: int(_Key)
      Note: in some STL implementations hash is already part of
      some extension an in namespace std or stdext, in some it is not.
      So we assume that it is not available. :TODO:
    _EqualKey: functional class providing a comparison function: bool(_Key, _Key)
      returns true iff two keys are considered to be equal
    _ExtractKey: extracts key from _Data: _Key(_Data)
  */
  //
  // can't use debug.h as debug.h uses hash maps...
  //
  // implemented as an array of lists (buckets)
  template <class _Key, class _Value,
	    class _HashFcn, class _EqualKey, class _ExtractKey>
  class hash_table {

  /// types

  public:
    // interface typedefs
    typedef Hash::size_type size_type;
    typedef _Key key_type;
    typedef _Value value_type;
    typedef _HashFcn hasher;
    typedef _EqualKey key_equal;

  protected:
    // a bucket is a list of values
    // using an STL list makes it more complicated to have a nice end iterator,
    // as iterators of different lists can not be compared
    // (at least in the MS STL).
    // so instead we implement our own single-linked list here,
    // where NULL is the end iterator for all lists.
    struct BucketNode {
      BucketNode(BucketNode* next, const value_type& value)
	: d_next(next), d_value(value)
      { };
      BucketNode* d_next;
      value_type d_value;
    };
    typedef BucketNode Bucket;
    
    // the buckets are kept in an array
    typedef std::vector<Bucket*> Data;
    typedef typename Data::iterator data_iter;
    typedef typename Data::const_iterator data_const_iter;


  public:
    // iterators
    class iterator;
    friend class iterator;
    class const_iterator;
    friend class const_iterator;



  /// variables

  protected:
    /// template parameters

    // the hash function for a key
    hasher d_hash;

    // the equality function between keys
    key_equal d_equal;

    // extraction of key from data
    _ExtractKey d_extractKey;
	      

    // the current number of elements - stored for efficiency
    size_type d_size;

    // the hash table - an array of buckets
    Data d_data;


  /// methods

  protected:

    /// template parameters
    

    // the hash function for a key
    size_type hash(const key_type& key) const {
      return d_hash(key);
    }

    // equality between keys
    bool equal(const key_type& key1, const key_type& key2) const {
      return d_equal(key1, key2);
    }

    // extraction of a key
    const key_type& extractKey(const value_type& value) const {
      return d_extractKey(value);
    }


    /// bucket retrieval

    // returns the index in the array which contains the bucket
    // with the keys mapping to the same hash value as key
    size_type getBucketIndex(const key_type& key) const {
      return (hash(key) % d_data.size());
    }

    Bucket* getBucketByKey(const key_type& key) {
      return getBucketByIndex(getBucketIndex(key));
    }
    
    const Bucket* getBucketByKey(const key_type& key) const {
      return getBucketByIndex(getBucketIndex(key));
    }

    Bucket* getBucketByIndex(const size_type index) {
      DebugAssert(index < d_data.size(),"hash_table::getBucketByIndex");
      return d_data.at(index);
    }

    const Bucket* getBucketByIndex(const size_type index) const {
      DebugAssert(index < d_data.size(),"hash_table::getBucketByIndex (const)");
      return d_data.at(index);
    }



    /// resize

    // increase the size of the table, typically if the load factor is too high
    void resize() {
      if (load_factor() > 1) {
	// create new table with doubled size size
	//size_type size = 2 * d_data.size();
	// this is simple, but might not be efficient for bad hash functions,
	// which for example mainly hash to values which contain 2 as a factor.
	// thus, instead we go from a prime to a prime of more or less double size.
	size_type size = next_prime(d_data.size() + 1);
	Data copy(size, NULL);
	
	// move entries to new table
	for (size_type i = 0; i < d_data.size(); ++i) {
	  // head of current bucket to move
	  BucketNode* bucket = d_data[i];
	  while (bucket != NULL) {
	    // move head of old bucket
	    BucketNode* current = bucket;
	    bucket = bucket->d_next;

	    // move old head to new bucket
	    size_type new_index = hash(extractKey(current->d_value)) % size;
	    BucketNode* new_bucket = copy[new_index];
	    current->d_next = new_bucket;
	    copy[new_index] = current;
	  }
	  d_data[i] = NULL;
	}

	d_data.swap(copy);
      }
    }


  public:
    /// constructors

    // default size is 16 buckets
    hash_table() :
      d_hash(_HashFcn()), d_equal(_EqualKey()), d_extractKey(_ExtractKey()),
      d_size(0), d_data(16)
    {
      init();
    };
    
    // specifiy initial number of buckets - must be positive
    hash_table(size_type initial_capacity) : 
      d_hash(_HashFcn()), d_equal(_EqualKey()), d_extractKey(_ExtractKey()),
      d_size(0), d_data(initial_capacity)
    {
      init();
    };

    // specifiy initial number of buckets and hash function
    hash_table(size_type initial_capacity, const _HashFcn& hash) : 
      d_hash(hash), d_equal(_EqualKey()), d_extractKey(_ExtractKey()),
      d_size(0), d_data(initial_capacity)
    {
      init();
    };

    // specifiy initial number of buckets, hash and equal function
    hash_table(size_type initial_capacity,
	     const _HashFcn& hash, const _EqualKey& equal) : 
      d_hash(hash), d_equal(equal), d_extractKey(_ExtractKey()),
      d_size(0), d_data(initial_capacity)
    {
      init();
    };

    // copy hash table
    hash_table(const hash_table& other) :
      d_hash(other.d_hash), d_equal(other.d_equal), d_extractKey(other.d_extractKey),
      d_size(other.d_size), d_data(0)
    {
      assignTable(other.d_data);
    };

    ~hash_table() {
      clear();
    }

    // assign hash table
    hash_table& operator=(const hash_table& other) {
      if (this != &other) {
	clear();

	d_hash = other.d_hash;
	d_equal = other.d_equal;
	d_extractKey = other.d_extractKey;
	d_size = other.d_size;
	assignTable(other.d_data);
      }

      return *this;
    }
    
    
    // replaces the current hash table with the given one
    void assignTable(const Data& data) {
      // copy elements:
      // default assignment operator does not work if value_type contains
      // constants, which should be the case as the key should be constant.
      // so not even shrinking a vector is possible,
      // so create a new table instead and swap with the current one.
      Data tmp(data.size());
      d_data.swap(tmp);
      
      // for each bucket ...
      for (size_type i = 0; i < data.size(); ++i) {
	// .. copy each element
	DebugAssert(i < d_data.size(),"hash_table::operator=");
	
	  // copy bucket if not empty
	Bucket* source = data[i];
	if (source != NULL) {
	  // set first element
	  Bucket* target = new BucketNode(NULL, source->d_value);
	  d_data[i] = target;
	  source = source->d_next;	
	  
	  // copy remaining nodes
	  while (source != NULL) {
	    target->d_next = new BucketNode(NULL, source->d_value);
	    target = target->d_next;
	    source = source->d_next;	
	  }
	}
      }
    }

    void swap(hash_table& other) {
      std::swap(d_hash, other.d_hash);
      std::swap(d_equal, other.d_equal);
      std::swap(d_extractKey, other.d_extractKey);
      std::swap(d_size, other.d_size);
      d_data.swap(other.d_data);
    }

    // sets all buckets to NULL
    void init() {
      for (size_type i = 0; i < d_data.size(); ++i) {
	d_data[i] = NULL;
      }
    }

    // empty all buckets
    void clear() {
      d_size = 0;
    
      for (size_type i = 0; i < d_data.size(); ++i) {
	BucketNode* head = d_data[i];
	while (head != NULL) {
	  BucketNode* next = head->d_next;
	  delete head;
	  head = next;
	}
	d_data[i] = NULL;
      }
    }



    /// operations

    
    // returns end iterator if key was not bound
    iterator find(const key_type& key) {
      for (BucketNode* node = getBucketByKey(key); node != NULL; node = node->d_next) {
	if (equal(extractKey(node->d_value), key)) {
	  return iterator(this, node);
	}
      }
      return end();      
    }

    // const version of find
    const_iterator find(const key_type& key) const {
      for (const BucketNode* node = getBucketByKey(key); node != NULL; node = node->d_next) {
	if (equal(extractKey(node->d_value), key)) {
	  return const_iterator(this, node);
	}
      }
      return end();      
    }


    // adds the mapping from key to data, if key is still unbound
    // otherwise returns false
    std::pair<iterator, bool> insert(const value_type& value) {
      // resize in case we insert
      resize();

      const key_type& key = extractKey(value);
      size_type index = getBucketIndex(key);

      // check if key is already bound
      for (BucketNode* node = d_data[index]; node != NULL; node = node->d_next) {
	if (equal(extractKey(node->d_value), key)) {
	  return std::make_pair(end(), false);
	}
      }

      // insert new value
      ++d_size;
      d_data[index] = new BucketNode(d_data[index], value);
      return std::make_pair(iterator(this, d_data[index]), true);
    }

    // if key in value is already bound,
    // returns that bindings,
    // otherwise inserts value and returns it.
    value_type& find_or_insert(const value_type& value) {
      // resize in case we insert
      resize();

      const key_type& key = extractKey(value);
      size_type index = getBucketIndex(key);

      // check if key is already bound
      for (BucketNode* node = d_data[index]; node != NULL; node = node->d_next) {
	if (equal(extractKey(node->d_value), key)) {
	  return node->d_value;
	}
      }

      // insert new value
      ++d_size;
      d_data[index] = new BucketNode(d_data[index], value);
      return d_data[index]->d_value;
    }


    // removes binding of key
    // returns number of keys removed,
    // i.e. 1 if key was bound, 0 if key was not bound.
    size_type erase(const key_type& key) {
      size_type index = getBucketIndex(key);

      // keep track of the node previous to the current one
      BucketNode* prev = NULL;
      for (BucketNode* node = d_data[index]; node != NULL; node = node->d_next) {
	if (equal(extractKey(node->d_value), key)) {
	  --d_size;
	  
	  // remove the bucket's head
	  if (prev == NULL) {
	    d_data[index] = node->d_next;
	  }
	  // remove within the list;
	  else {
	    prev->d_next = node->d_next;
	  }
	  delete node;
	  return 1;
	}

	prev = node;
      }
      
      return 0;
    }

    // removes element pointed to by iter,
    // returns element after iter.
    const_iterator erase(const const_iterator& iter) {
      const_iterator next(iter);
      ++next;

      const key_type& key = extractKey(*iter);
      size_type index = getBucketIndex(key);

      // keep track of the node previous to the current one
      BucketNode* prev = NULL;
      for (BucketNode* node = d_data[index]; node != NULL; node = node->d_next) {
	if (equal(extractKey(node->d_value), key)) {
	  --d_size;
	  
	  // remove the bucket's head
	  if (prev == NULL) {
	    d_data[index] = node->d_next;
	  }
	  // remove within the list;
	  else {
	    prev->d_next = node->d_next;
	  }
	  delete node;
	  return next;
	}

	prev = node;
      }
      
      return next;
    }


    /// status

    // is the key bound?
    bool contains(const key_type& key) const {
      return (find(key) != end());
    }

    // returns the number of times a key is bound,
    // i.e. 0 or 1
    size_type count(const _Key& key) const {
      if (contains(key)) {
	return 1;
      }
      else {
	return 0;
      }
    }
  
    // is the hash table empty?
    bool empty() const {
      return (d_size == 0);
    }

    // the number of elements in the hash table
    size_type size() const {
      return d_size;
    }

    // the number of buckets in the hash table
    size_type bucket_count() const {
      return d_data.size();
    }

    // returns the average number of elements per bucket
    float load_factor() const {
      return (float(d_size) / float(d_data.size()));
    }



    /// iterators

    // returns forward iterator to iterate over all key/data pairs
    iterator begin() {
      if (d_size > 0) {
	// find first non-empty bucket
	size_type index = 0;
	while (d_data[index] == NULL) {
	  ++index;
	}
	
	return iterator(this, d_data[index]);
      }
      else {
	return end();
      }
    }

    // const version of begin
    const_iterator begin() const {
      if (d_size > 0) {
	// find first non-empty bucket
	size_type index = 0;
	while (d_data[index] == NULL) {
	  ++index;
	}
	
	return const_iterator(this, d_data[index]);
      }
      else {
	return end();
      }
    }


    // returns end iterator
    iterator end() {
      return iterator(this, NULL);
    }
    
    // const version of end
    const_iterator end() const {
      return const_iterator(this, NULL);
    }




    /// inner classes




    // iterator over hashtable elements

    // modifying the hash table leaves iterator intact,
    // unless the key in the value it points to is modified/deleted
    class iterator {
      friend class hash_table;
      friend class const_iterator;

      /// variables
      
    protected:
      // the hash table of this iterator
      hash_table* d_hash_table;
      // iterator points to current element in some bucket
      BucketNode* d_node;


      /// methods
    protected:
      // used by hash_table to create an iterator
      iterator(hash_table* hash_table, BucketNode* node)
	: d_hash_table(hash_table), d_node(node)
      { }

    public:
      // public default constructor,
      // leaves the iterator in undefined state.
      iterator()
	: d_hash_table(NULL), d_node(NULL)
      { }

      // copy constructor
      iterator(const iterator& other)
	: d_hash_table(other.d_hash_table), d_node(other.d_node)
      { }

      // assignment
      iterator& operator=(const iterator& other) {
	if (this != &other) {
	  d_hash_table = other.d_hash_table;
	  d_node = other.d_node;
	}

	return *this;
      }

      // go to next data - pre-increment
      iterator& operator++() {
	// must not be the end iterator
	DebugAssert(d_node != NULL, "hash operator++");

	// get current bucket index
	size_type index = d_hash_table->getBucketIndex(d_hash_table->extractKey(d_node->d_value));
	
	// go to next entry in bucket
	d_node = d_node->d_next;

	// while current bucket empty
	while (d_node == NULL) {
	  // go to next bucket
	  ++index;

	  // all buckets exhausted
	  if (index == d_hash_table->d_data.size()) {
	    // unfortunately this does not work, as end() returns a tmp value
	    // return d_hash_table->end();
	    *this = d_hash_table->end();
	    return *this;
	  }
	  DebugAssert(index < d_hash_table->d_data.size(),
                      "hash operator++ 2");

	  d_node = d_hash_table->getBucketByIndex(index);
	}
	
	return *this;
      };

      // go to next data - post-increment
      iterator operator++(int) {
	iterator tmp = *this;
	++(*this);
	return tmp;
      }

      value_type& operator*() const {
	return d_node->d_value;
      }

      value_type* operator->() const {
	return &(operator*());
      }

      // are two iterator identical?
      bool operator==(const iterator& other) const {
	// if the same bucket iterator, then it must be the same hash table
	DebugAssert(d_node == NULL || d_node != other.d_node || d_hash_table == other.d_hash_table, "hash operator==");
	return (d_node == other.d_node);
      }

      // negation of ==
      bool operator!=(const iterator& other) const {
	return !(*this == other);
      }
    };




    // const iterator over hashtable elements

    // modifying the hash table leaves iterator intact,
    // unless the key in the value it points to is modified/deleted
    class const_iterator {
      friend class hash_table;

      /// variables
      
    protected:
      // the hash table of this iterator
      const hash_table* d_hash_table;
      // iterator points to current element in some bucket
      const BucketNode* d_node;


      /// methods
    protected:
      // used by hash_table to create an iterator
      const_iterator(hash_table const* hash_table, const BucketNode* node)
	: d_hash_table(hash_table), d_node(node)
      { }

    public:
      // public default constructor,
      // leaves the iterator in undefined state.
      const_iterator()
	: d_hash_table(NULL), d_node(NULL)
      { }

      // copy constructor
      const_iterator(const const_iterator& other)
	: d_hash_table(other.d_hash_table), d_node(other.d_node)
      { }

      // conversion constructor from non-const iterator
      const_iterator(const iterator& other)
	: d_hash_table(other.d_hash_table), d_node(other.d_node)
      { }

      // assignment
      const_iterator& operator=(const const_iterator& other) {
	if (this != &other) {
	  d_hash_table = other.d_hash_table;
	  d_node = other.d_node;
	}

	return *this;
      }

      // go to next data - pre-increment
      const_iterator& operator++() {
	// must not be the end iterator
	DebugAssert(d_node != NULL, "");

	// get current bucket index
	size_type index = d_hash_table->getBucketIndex(d_hash_table->extractKey(d_node->d_value));
	
	// go to next entry in bucket
	d_node = d_node->d_next;

	// while current bucket empty
	while (d_node == NULL) {
	  // go to next bucket
	  ++index;

	  // all buckets exhausted
	  if (index == d_hash_table->d_data.size()) {
	    // unfortunately this does not work, as end() returns a tmp value
	    // return d_hash_table->end();
	    *this = d_hash_table->end();
	    return *this;
	  }
	  DebugAssert(index < d_hash_table->d_data.size(),"");

	  d_node = d_hash_table->getBucketByIndex(index);
	}
	
	return *this;
      };

      // go to next data - post-increment
      const_iterator operator++(int) {
	const_iterator tmp = *this;
	++(*this);
	return tmp;
      }

      const value_type& operator*() const {
	return d_node->d_value;
      }

      const value_type* operator->() const {
	return &(operator*());
      }

      // are two iterator identical?
      bool operator==(const const_iterator& other) const {
	// if the same bucket iterator, then it must be the same hash table
	DebugAssert(d_node == NULL || d_node != other.d_node || d_hash_table == other.d_hash_table,"");
	return (d_node == other.d_node);
      }

      // negation of ==
      bool operator!=(const const_iterator& other) const {
	return !(*this == other);
      }
    };
  };

}

#endif
