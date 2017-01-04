/*****************************************************************************/
/*!
 *\file cvc_util.h
 *\brief basic helper utilities
 *
 * Author: Clark Barrett
 *
 * Created: Thu Dec  1 16:35:52 2005
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

#ifndef _cvc3__debug_h
#include "debug.h"
#endif

#ifndef _cvc3__cvc_util_h
#define _cvc3__cvc_util_h

namespace CVC3 {

inline std::string to_upper(const std::string & src){
  std::string nameup; 
  for(std::string::const_iterator i=src.begin(), iend = src.end(); i!=iend ; i++){
    nameup.push_back(toupper(*i));
  }
  return nameup;
}

inline std::string to_lower(const std::string & src){
  std::string nameup; 
  for(std::string::const_iterator i=src.begin(), iend = src.end(); i!=iend ; i++){
    nameup.push_back(tolower(*i));
  }
  return nameup;
}

inline std::string int2string(int n) {
  std::ostringstream ss;
  ss << n;
  return ss.str();
}

template<class T>
T abs(T t) { return t < 0 ? -t : t; }

template<class T>
T max(T a, T b) { return a > b ? a : b; }

struct ltstr{
  bool operator()(const std::string& s1, const std::string& s2) const{
    return s1.compare(s2) < 0;
  }
};

template<class T>
class StrPairLess {
public:
  bool operator()(const std::pair<std::string,T>& p1,
		  const std::pair<std::string,T>& p2) const {
    return p1.first < p2.first;
  }
};

template<class T>
std::pair<std::string,T> strPair(const std::string& f, const T& t) {
  return std::pair<std::string,T>(f, t);
}

typedef std::pair<std::string,std::string> StrPair;

//! Sort two vectors based on the first vector
template<class T>
void sort2(std::vector<std::string>& keys, std::vector<T>& vals) {
  DebugAssert(keys.size()==vals.size(), "sort2()");
  // Create std::vector of pairs
  std::vector<std::pair<std::string,T> > pairs;
  for(size_t i=0, iend=keys.size(); i<iend; ++i)
    pairs.push_back(strPair(keys[i], vals[i]));
  // Sort pairs
  StrPairLess<T> comp;
  sort(pairs.begin(), pairs.end(), comp);
  DebugAssert(pairs.size() == keys.size(), "sort2()");
  // Split the pairs back into the original vectors
  for(size_t i=0, iend=pairs.size(); i<iend; ++i) {
    keys[i] = pairs[i].first;
    vals[i] = pairs[i].second;
  }
}

/*! @brief A class which sets a boolean value to true when created,
 * and resets to false when deleted.
 *
 * Useful for tracking when the control is within a certain method or
 * not.  For example, TheoryCore::addFact() uses d_inAddFact to check
 * that certain other methods are only called from within addFact().
 * However, when an exception is thrown, this variable is not reset.
 * The watcher class will reset the variable even in those cases.
 */
class ScopeWatcher {
 private:
  bool *d_flag;
public:
  ScopeWatcher(bool *flag): d_flag(flag) { *d_flag = true; }
  ~ScopeWatcher() { *d_flag = false; }
};


// For memory calculations
class MemoryTracker {
public:
  static void print(std::string name, int verbosity,
                    unsigned long memSelf, unsigned long mem)
  {
    if (verbosity > 0) {
      std::cout << name << ": " << memSelf << std::endl;
      std::cout << "  Children: " << mem << std::endl;
      std::cout << "  Total: " << mem+memSelf << std::endl;
    }
  }

  template <typename T>
  static unsigned long getVec(int verbosity, const std::vector<T>& v)
  {
    unsigned long memSelf = sizeof(std::vector<T>);
    unsigned long mem = 0;
    print("vector", verbosity, memSelf, mem);
    return memSelf + mem;
  }

  template <typename T>
  static unsigned long getVecAndData(int verbosity, const std::vector<T>& v)
  {
    unsigned long memSelf = sizeof(std::vector<T>);
    unsigned long mem = 0;
    for (unsigned i = 0; i < v.size(); ++i) {
      mem += v[i].getMemory(verbosity - 1);
    }
    print("vector+data", verbosity, memSelf, mem);
    return memSelf + mem;
  }

  template <typename T>
  static unsigned long getVecAndDataP(int verbosity, const std::vector<T>& v)
  {
    unsigned long memSelf = sizeof(std::vector<T>);
    unsigned long mem = 0;
    for (unsigned i = 0; i < v.size(); ++i) {
      mem += v[i]->getMemory(verbosity - 1);
    }
    print("vector+data(p)", verbosity, memSelf, mem);
    return memSelf + mem;
  }

  static unsigned long getString(int verbosity, const std::string& s)
  {
    unsigned long memSelf = sizeof(std::string);
    unsigned long mem = s.capacity() * sizeof(char);
    print("string", verbosity, memSelf, mem);
    return memSelf + mem;
  }

//   template <class _Key, class _Value,
// 	    class _HashFcn, class _EqualKey, class _ExtractKey>
//     unsigned long get(int verbosity, const hash_table<_Key, _Value, _HashFcn, 
//         unsigned long memSelf = sizeof(BucketNode);
//         unsigned long mem = 0;
//         BucketNode* node = this;
//         do {
//           if (getMemoryData) {
//             mem += d_value.getMemory(verbosity
//           node = node->d_next;
//         } while (node != NULL)          
//         unsigned long mem = 0;

//         mem += getMemoryVec(verbosity - 1, d_data, false, true);
//         printMemory("hash_table", verbosity, memSelf, mem);
//         return mem+memSelf;
//       }

//   unsigned long getMemory(int verbosity, hash_table) {
//       unsigned long memSelf = sizeof(hash_table);
//       unsigned long mem = 0;
//       mem += d_hash.getmemory(verbosity - 1) - sizeof(hasher);
//       mem += d_equal.getmemory(verbosity - 1) - sizeof(key_equal);
//       mem += d_extractKey.getmemory(verbosity - 1) - sizeof(_ExtractKey);

//       // handle data
//       mem += sizeof(Data);
//       mem += sizeof(Bucket*)*d_data.capacity();
//       for (unsigned i = 0; i < d_data.size(); ++i) {
//         mem += d_data[i]->getMemory(verbosity - 1, getMemoryData, getMemoryDataP);
//       }

//       printMemory("hash_table", verbosity, memSelf, mem);
//       return mem+memSelf;
//     }
  
//     unsigned long getMemory(int verbosity, hash_map) const {
//       unsigned long memSelf = sizeof(hash_map);
//       unsigned long mem = 0;
//       mem += d_table.getMemory(verbosity - 1) - sizeof(_hash_table);
//       MemoryTracker::print("hash_map", verbosity, memSelf, mem);
//       return mem+memSelf;
//     }


}; // End of MemoryTracker
  
}

#endif
