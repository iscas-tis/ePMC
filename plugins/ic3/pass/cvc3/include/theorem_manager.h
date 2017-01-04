/*****************************************************************************/
/*!
 * \file theorem_manager.h
 * 
 * Author: Sergey Berezin, Tue Feb  4 14:29:25 2003
 * 
 * Created: Feb 05 18:29:37 GMT 2003
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
 * CLASS: TheoremManager
 * 
 * 
 * Holds the shared data for the class Theorem
 */
/*****************************************************************************/

#ifndef _cvc3__theorem_manager_h_
#define _cvc3__theorem_manager_h_

#include "debug.h"
#include "compat_hash_map.h"

namespace CVC3 {

  class ContextManager;
  class ExprManager;
  class CLFlags;
  class MemoryManager;
  class CommonProofRules;
  
  class TheoremManager {
  private:
    ContextManager* d_cm;
    ExprManager* d_em;
    const CLFlags& d_flags;
    MemoryManager* d_mm;
    MemoryManager* d_rwmm;
    bool d_withProof;
    bool d_withAssump;
    unsigned d_flag; // used for setting flags in Theorems
    bool d_active; //!< Whether TheoremManager is active.  See also clear()
    CommonProofRules* d_rules;

    std::hash_map<long, bool> d_reflFlags;
    std::hash_map<long, int> d_cachedValues;
    std::hash_map<long, bool> d_expandFlags;
    std::hash_map<long, bool> d_litFlags;

    CommonProofRules* createProofRules();

  public:
    //! Constructor
    TheoremManager(ContextManager* cm,
                   ExprManager* em,
                   const CLFlags& flags);
    //! Destructor
    ~TheoremManager();
    //! Deactivate TheoremManager
    /*! No more Theorems can be created after this call, only deleted.
     * The purpose of this call is to dis-entangle the mutual
     * dependency of ExprManager and TheoremManager during destruction time.
     */
    void clear();
    //! Test whether the TheoremManager is still active
    bool isActive() { return d_active; }

    ContextManager* getCM() const { return d_cm; }
    ExprManager* getEM() const { return d_em; }
    const CLFlags& getFlags() const { return d_flags; }
    MemoryManager* getMM() const { return d_mm; }
    MemoryManager* getRWMM() const { return d_rwmm; }
    CommonProofRules* getRules() const { return d_rules; }

    unsigned getFlag() const {
      return d_flag;
    }
    
    void clearAllFlags() {
      d_reflFlags.clear();
      FatalAssert(++d_flag, "Theorem flag overflow.");
    }

    bool withProof() {
      return d_withProof;
    }
    bool withAssumptions() {
      return d_withAssump;
    }

    // For Refl theorems
    void setFlag(long ptr) { d_reflFlags[ptr] = true; }
    bool isFlagged(long ptr) { return d_reflFlags.count(ptr) > 0; }
    void setCachedValue(long ptr, int value) { d_cachedValues[ptr] = value; }
    int getCachedValue(long ptr) {
      std::hash_map<long, int>::const_iterator i = d_cachedValues.find(ptr);
      if (i != d_cachedValues.end()) return (*i).second;
      else return 0;
    }
    void setExpandFlag(long ptr, bool value) { d_expandFlags[ptr] = value; }
    bool getExpandFlag(long ptr) {
      std::hash_map<long, bool>::const_iterator i = d_expandFlags.find(ptr);
      if (i != d_expandFlags.end()) return (*i).second;
      else return false;
    }
    void setLitFlag(long ptr, bool value) { d_litFlags[ptr] = value; }
    bool getLitFlag(long ptr) {
      std::hash_map<long, bool>::const_iterator i = d_litFlags.find(ptr);
      if (i != d_litFlags.end()) return (*i).second;
      else return false;
    }
    

  }; // end of class TheoremManager

} // end of namespace CVC3

#endif
