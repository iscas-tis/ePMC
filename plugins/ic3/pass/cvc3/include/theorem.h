/*****************************************************************************/
/*!
 * \file theorem.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Dec 10 00:37:49 GMT 2002
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
// CLASS: Theorem
//
// AUTHOR: Sergey Berezin, 07/05/02
//
// Abstract:
//
// A class representing a proven fact in CVC.  It stores the theorem
// as a CVC expression, and in the proof prodicing mode also its
// proof.
//
// The idea is to allow only a few trusted classes to create values of
// this class.  If all the critical computations in the decision
// procedures are done through the use of Theorems, then soundness of
// these decision procedures will rely only on the soundness of the
// methods in the trusted classes (the inference rules).
//
// Thus, proof checking can effectively be done at run-time on the
// fly.  Or the soundness may be potentially proven by static analysis
// and many run-time checks can then be optimized away.
//
// This theorem.h file should be used by the decision procedures that
// use Theorem.
//
////////////////////////////////////////////////////////////////////////

// expr.h Has to be included outside of #ifndef, since it sources us
// recursively (read comments in expr_value.h).
#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__theorem_h_
#define _cvc3__theorem_h_

#include "os.h"
#include "proof.h"

namespace CVC3 {

  // Declare the data holding classes but hide the definitions
  class TheoremManager;
  class TheoremValue;
  class Assumptions;

  // Theorem is basically a wrapper around a pointer to a
  // TheoremValue, so that we can pass this class around by value.
  // All the constructors of this class are private, so do not inherit
  // from it and do not try to create a value directly.  Only
  // TheoremProducer can create new Theorem instances.
  //
  // Theorems, unlike expressions, are NOT made unique, and it is
  // possible to have the same theorem in different scopes with
  // different assumptions and proofs.  It is a deliberate feature,
  // since natural deduction sometimes requires proving the same
  // conclusion from different assumptions independently, e.g. in
  // disjunction elimination rule.
  class CVC_DLL Theorem {
  private:
    // Make a theorem producing class our friend.  No definition is
    // exposed here.
    friend class TheoremProducer;
    // Also allow our 3-valued cousin to create us
    friend class Theorem3;
    // Also TheoremValue classes for assumptions
    friend class RegTheoremValue;
    friend class RWTheoremValue;

    // Optimization: reflexivity theorems just store the exprValue pointer
    // directly.  Also, the lowest bit is set to 1 to indicate that its
    // a reflexivity theorem.  This really helps performance!
    union {
      intptr_t d_thm;
      ExprValue* d_expr;
    };

    //! Compare Theorems by their expressions.  Return -1, 0, or 1.
    friend int compare(const Theorem& t1, const Theorem& t2);
    //! Compare a Theorem with an Expr (as if Expr is a Theorem)
    friend int compare(const Theorem& t1, const Expr& e2);
    //! Compare Theorems by TheoremValue pointers.  Return -1, 0, or 1.
    friend int compareByPtr(const Theorem& t1, const Theorem& t2);
    //! Equality is w.r.t. compare()
    friend bool operator==(const Theorem& t1, const Theorem& t2) 
      { return (compare(t1, t2)==0); }
    //! Disequality is w.r.t. compare()
    friend bool operator!=(const Theorem& t1, const Theorem& t2)
      { return (compare(t1, t2) != 0); }

    //! Constructor only used by TheoremValue for assumptions
    Theorem(TheoremValue* thm) :d_thm(((intptr_t)thm) | 0x1) {}

    //! Constructor for a new theorem 
    Theorem(TheoremManager* tm, const Expr &thm, const Assumptions& assump,
            const Proof& pf, bool isAssump = false, int scope = -1);

    //! Constructor for rewrite theorems.
    /*! These use a special efficient subclass of TheoremValue for
     * theorems which represent rewrites: A |- t = t' or A |- phi<=>phi'
     */
    Theorem(TheoremManager* tm, const Expr& lhs, const Expr& rhs,
	    const Assumptions& assump, const Proof& pf, bool isAssump = false,
            int scope = -1);

    //! Constructor for a reflexivity theorem: |-t=t or |-phi<=>phi
    Theorem(const Expr& e);

    void recursivePrint(int& i) const;
    void getAssumptionsRec(std::set<Expr>& assumptions) const;
    void getAssumptionsAndCongRec(std::set<Expr>& assumptions,
                                  std::vector<Expr>& congruences) const;
    void GetSatAssumptionsRec(std::vector<Theorem>& assumptions) const;

    ExprValue* exprValue() const { return d_expr; }
    TheoremValue* thm() const { return (TheoremValue*)(d_thm & (~(0x1))); }

  public:
    // recusive function to print theorems and all assumptions recursively
    // important: this function will corrupt all flags!! so exercise 
    // caution when using in any graph traversals 
    // (probably more useful to call it only before a crash)
    void printDebug() const { 
      clearAllFlags();
      setCachedValue(0);
      setFlag();
      int i = 1; 
      recursivePrint(i);
    }

    // Default constructor creates Null theorem to allow untrusted
    // code declare local vars without initialization (vector<Theorem>
    // may need that, for instance).
    Theorem(): d_thm(0) { }
    // Copy constructor
    Theorem(const Theorem &th);
    // Assignment operator
    Theorem& operator=(const Theorem &th);

    // Destructor
    ~Theorem();

    // Test if we are running in a proof production mode and with assumptions
    bool withProof() const;
    bool withAssumptions() const;

    bool isNull() const { return d_thm == 0; }

    // True if theorem is of the form t=t' or phi iff phi'
    bool isRewrite() const;
    // True if theorem was created using assumpRule
    bool isAssump() const;
    // True if reflexivity theorem
    bool isRefl() const { return d_thm && !(d_thm & 0x1); }
    
    // Return the theorem value as an Expr
    Expr getExpr() const;
    const Expr& getLHS() const;
    const Expr& getRHS() const;

    void GetSatAssumptions(std::vector<Theorem>& assumptions) const;


    // Return the assumptions.  a should be empty and uninitialized
    //    void getAssumptions(Assumptions& a) const;
    // Recurse to get actual assumptions
    
    void getLeafAssumptions(std::vector<Expr>& assumptions,
                            bool negate = false) const;
    // Same as above but also collects congruences in the proof tree
    void getAssumptionsAndCong(std::vector<Expr>& assumptions,
                               std::vector<Expr>& congruences,
                               bool negate = false) const;
    const Assumptions& getAssumptionsRef() const;
    // Return the proof of the theorem.  If running without proofs,
    // return the Null proof.
    Proof getProof() const;
    // Return the lowest scope level at which this theorem is valid.
    // Value -1 means no information is available.
    int getScope() const;
    //! Return quantification level for this theorem
    unsigned getQuantLevel() const;

    unsigned getQuantLevelDebug() const;

    //! Set the quantification level for this theorem
    void setQuantLevel(unsigned level);

    // hash
    size_t hash() const;

    // Printing
    std::string toString() const;

    // For debugging
    void printx() const;
    void printxnodag() const;
    void pprintx() const;
    void pprintxnodag() const;
    
    void print() const;

    /*! \name Methods for Theorem Attributes
     *
     * Several attributes used in conflict analysis and assumptions
     * graph traversals.
     * @{
     */
    //! Check if the flag attribute is set
    bool isFlagged() const;
    //! Clear the flag attribute in all the theorems
    void clearAllFlags() const;
    //! Set the flag attribute
    void setFlag() const;

    //! Set flag stating that theorem is an instance of substitution
    void setSubst() const;
    //! Is theorem an instance of substitution
    bool isSubst() const;
    //! Set the "expand" attribute
    void setExpandFlag(bool val) const;
    //! Check the "expand" attribute
    bool getExpandFlag() const;
    //! Set the "literal" attribute
    /*! The expression of this theorem will be added as a conflict
     * clause literal */
    void setLitFlag(bool val) const;
    //! Check the "literal" attribute
    /*! The expression of this theorem will be added as a conflict
     * clause literal */
    bool getLitFlag() const;
    //! Check if the theorem is a literal
    bool isAbsLiteral() const;

    bool refutes(const Expr& e) const
    {
      return
	(e.isNot() && e[0] == getExpr()) ||
	(getExpr().isNot() && getExpr()[0] == e);
    }

    bool proves(const Expr& e) const
    {
      return getExpr() == e;
    }

    bool matches(const Expr& e) const
    {
      return proves(e) || refutes(e);
    }

    void setCachedValue(int value) const;
    int getCachedValue() const;
    
    /*!@}*/ // End of Attribute methods

    //! Printing a theorem to a stream, calling it "name".
    std::ostream& print(std::ostream& os, const std::string& name) const;
    
    friend std::ostream& operator<<(std::ostream& os, const Theorem& t) {
      return t.print(os, "Theorem");
    }

    static bool TheoremEq(const Theorem& t1, const Theorem& t2) 
    { 
      DebugAssert(!t1.isNull() && !t2.isNull(), 
                  "AssumptionsValue() Null Theorem passed to constructor");
      return t1 == t2;
    }
  };  // End of Theorem

/*****************************************************************************/
/*!
 *\class Theorem3
 *\brief Theorem3
 *
 * Author: Sergey Berezin
 *
 * Created: Tue Nov  4 17:57:07 2003
 *
 * Implements the 3-valued theorem used for the user assertions and
 * the result of query.  It is simply a wrapper around class Theorem,
 * but has a different semantic meaning: the formula may have partial
 * functions and has the Kleene's 3-valued interpretation.  The fact
 * that a Theorem3 value is derived means that the TCCs for the
 * formula and all of its assumptions are valid in the current
 * context, and the proofs of TCCs contribute to the set of
 * assumptions.
 */
/*****************************************************************************/
  class Theorem3 {
  private:
    // Make a theorem producing class our friend.  No definition is
    // exposed here.
    friend class TheoremProducer;

    Theorem d_thm;

    friend bool operator==(const Theorem3& t1, const Theorem3& t2) {
      return t1.d_thm == t2.d_thm;
    }
    friend bool operator!=(const Theorem3& t1, const Theorem3& t2) {
      return t1.d_thm != t2.d_thm;
    }


    // Private constructors for a new theorem 
    Theorem3(TheoremManager* tm, const Expr &thm, const Assumptions& assump,
             const Proof& pf, bool isAssump = false, int scope = -1)
      : d_thm(tm, thm, assump, pf, isAssump, scope) { }

    // Constructors for rewrite theorems.  These use a special efficient
    // subclass of TheoremValue for theorems which represent rewrites:
    // A |- t = t' or A |- phi iff phi'
    Theorem3(TheoremManager* tm, const Expr& lhs, const Expr& rhs,
	     const Assumptions& assump, const Proof& pf)
      : d_thm(tm, lhs, rhs, assump, pf) { }

  public:
    // recusive function to print theorems and all assumptions recursively
    // important: this function will corrupt all flags!! so exercise 
    // caution when using in any graph traversals 
    // (probably more useful to call it only before a crash)
    void printDebug() const { d_thm.printDebug(); }

    // Default constructor creates Null theorem to allow untrusted
    // code declare local vars without initialization (vector<Theorem>
    // may need that, for instance).
    Theorem3() { }

    // Destructor
    virtual ~Theorem3() { }

    bool isNull() const { return d_thm.isNull(); }

    // True if theorem is of the form t=t' or phi iff phi'
    bool isRewrite() const { return d_thm.isRewrite(); }
    bool isAssump() const { return d_thm.isAssump(); }
    
    // Return the theorem value as an Expr
    Expr getExpr() const { return d_thm.getExpr(); }
    const Expr& getLHS() const { return d_thm.getLHS(); }
    const Expr& getRHS() const { return d_thm.getRHS(); }

    // Return the assumptions.
    // It's an error if called while running without assumptions.
    //    void getAssumptions(Assumptions& a) const { d_thm.getAssumptions(a); }
    const Assumptions& getAssumptionsRef() const {
      return d_thm.getAssumptionsRef();
    }
    // Return the proof of the theorem.  If running without proofs,
    // return the Null proof.
    Proof getProof() const { return d_thm.getProof(); }

    // Return the lowest scope level at which this theorem is valid.
    // Value -1 means no information is available.
    int getScope() const { return d_thm.getScope(); }

    // Test if we are running in a proof production mode and with assumptions
    bool withProof() const { return d_thm.withProof(); }
    bool withAssumptions() const { return d_thm.withAssumptions(); }

    // Printing
    std::string toString() const;

    // For debugging
    void printx() const { d_thm.printx(); }
    void print() const { d_thm.print(); }

    //! Check if the theorem is a literal
    bool isAbsLiteral() const { return d_thm.isAbsLiteral(); }

    friend std::ostream& operator<<(std::ostream& os, const Theorem3& t) {
      return t.d_thm.print(os, "Theorem3");
    }
  };  // End of Theorem3

  //! "Less" comparator for theorems by TheoremValue pointers
  class TheoremLess {
  public:
    bool operator()(const Theorem& t1, const Theorem& t2) const {
      return (compareByPtr(t1, t2) < 0);
    }
  };
  typedef std::map<Theorem,bool, TheoremLess> TheoremMap;

  inline std::string Theorem::toString() const {
    std::ostringstream ss;
    ss << (*this);
    return ss.str();
  }

  inline std::string Theorem3::toString() const {
    std::ostringstream ss;
    ss << (*this);
    return ss.str();
  }

  // Merge assumptions from different theorems
//   inline Assumptions merge(const Theorem& t1, const Theorem& t2) {
//     return Assumptions(t1, t2);
//   }
//   inline void merge(Assumptions& a, const Theorem& t) {
//     a.add(t);
//   }
//   inline Assumptions merge(const std::vector<Theorem>& t) {
//     return Assumptions(t);
//   }

  inline bool operator<(const Theorem& t1, const Theorem& t2)
    { return compare(t1, t2) < 0; }
  inline bool operator<=(const Theorem& t1, const Theorem& t2)
    { return compare(t1, t2) <= 0; }
  inline bool operator>(const Theorem& t1, const Theorem& t2)
    { return compare(t1, t2) > 0; }
  inline bool operator>=(const Theorem& t1, const Theorem& t2)
    { return compare(t1, t2) >= 0; }

} // end of namespace CVC3

#include "hash_fun.h"
namespace Hash
{

template<> struct hash<CVC3::Theorem>
{
  size_t operator()(const CVC3::Theorem& e) const { return e.hash(); }
};

}

#endif
