#ifndef CoCoA_VectorOperations_H
#define CoCoA_VectorOperations_H

//   Copyright (c)  2005,2006,2009  John Abbott

//   This file is part of the source of CoCoALib, the CoCoA Library.

//   CoCoALib is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.

//   CoCoALib is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.

//   You should have received a copy of the GNU General Public License
//   along with CoCoALib.  If not, see <http://www.gnu.org/licenses/>.


// This contains sundry things which may eventually become stable parts of
// the CoCoA library.


#include "CoCoA/error.H"

#include <algorithm>
//using std::for_each;
//using std::find_if;
#include <iostream>
#include <list>
// using std::list
#include <vector>
// using std::vector;

namespace CoCoA
{

  ///////////////////////////////////////////////////////
  // Functions for printing vectors/lists/etc.

  // LLVM 6.0 (MacOS 10.10) needs this fwd decl
  template <typename FwdIterator_t>
  void OutputRange(std::ostream& out, FwdIterator_t start, const FwdIterator_t& end);


  // Use cout << v syntax to print out a std::vector<T> of printable values.
  template <typename T>
  inline std::ostream& operator<<(std::ostream& out, const std::vector<T>& v)
  {
    OutputRange(out, v.begin(), v.end());
    return out;
  }

  // Use cout << l syntax to print out a std::list<T> of printable values.
  template <typename T>
  inline std::ostream& operator<<(std::ostream& out, const std::list<T>& v)
  {
    OutputRange(out, v.begin(), v.end());
    return out;
  }

  // This prints out the elements (of a list or vector) in a given range.
  // The empty list appears as []; otherwise ",  " separated elements
  // between "[" and "]".
  // NOTE: This template fn is not intended for public use!
  template <typename FwdIterator_t>
  void OutputRange(std::ostream& out, FwdIterator_t start, const FwdIterator_t& end)
  {
    if (start == end) { out << "[]"; return; }
    out << "[" << *start;
    while (++start != end)
      out << ",  " << *start;
    out << "]";
  }


  ////////////////////////////////////////////////////////////
  // Functions for computing products of lists and vectors.

  // This class is simply to permit use of std::for_each.
  template <typename arith_t>
  class SeqProd
  {
  public:
    SeqProd(const arith_t& InitialValue): myProdSoFar(InitialValue) {}
    // Default copy ctor, assignment, and dtor are all OK.
    void operator()(const arith_t& NextValue) { myProdSoFar *= NextValue; }
    const arith_t& myCurrentValue() const { return myProdSoFar; }
  private:
    arith_t myProdSoFar;
  };

  template <typename InputIterator, typename arith_t>
  arith_t product(InputIterator begin, InputIterator end, const arith_t& InitialValue)
  {
    return std::for_each(begin, end, SeqProd<arith_t>(InitialValue)).myCurrentValue();
  }

  template <typename ListOrVector>
  typename ListOrVector::value_type product(const ListOrVector& V)
  {
    if (V.empty()) CoCoA_ERROR(ERR::Empty, "product(L)");
    return product(++V.begin(), V.end(), V.front());
  }


  ////////////////////////////////////////////////////////////
  // Functions for computing sums of lists and vectors.

  // This class is simply to permit use of for_each.
  template <typename arith_t>
  class SeqSum
  {
  public:
    SeqSum(const arith_t& InitialValue): mySumSoFar(InitialValue) {}
    // Default copy ctor, assignment, and dtor are all OK.
    void operator()(const arith_t& NextValue) { mySumSoFar += NextValue; }
    const arith_t& myCurrentValue() const { return mySumSoFar; }
  private:
    arith_t mySumSoFar;
  };

  template <typename InputIterator, typename arith_t>
  arith_t sum(InputIterator begin, InputIterator end, const arith_t& InitialValue)
  {
    return std::for_each(begin, end, SeqSum<arith_t>(InitialValue)).myCurrentValue();
  }

  template <typename ListOrVector>
  typename ListOrVector::value_type sum(const ListOrVector& V)
  {
    if (V.empty()) CoCoA_ERROR(ERR::Empty, "sum(L)");
    return sum(++V.begin(), V.end(), V.front());
  }


////////////////////////////////////////////////////////////
// Functions for checking all elements have the same owner

// This class is simply to permit use of find_if.
  template <class elem_t>
  class HasDifferentOwner
  {
  public:
    HasDifferentOwner(const elem_t& x): myValue(x) {};
    // Default copy ctor, assignment, and dtor are all OK.
    bool operator() (const elem_t& x) const { return owner(x) != owner(myValue); }
  private:
    const elem_t& myValue;
  };

  template <typename container>
  bool HasUniqueOwner(const container& v)
  {
    typedef typename container::value_type elem_t;
    if (v.empty()) return true;
    return v.end() ==
      find_if(++v.begin(), v.end(), HasDifferentOwner<elem_t>(v.front()));
  }


//////////////////////////////////////////////////////////////////
//  3-way comparison

//   // Generic 3-way comparison function: assumes operator< is defined.
//   // Result is <0 =0 or >0 according as a<b, a==b, a>b.
//   template <typename T1, typename T2>
//   inline int cmp(const T1& a, const T2& b)
//   {
//     if (a < b) return -1;
//     if (b < a) return 1;
//     return 0;
//   }

  // LexCmp3 3-way lex comparison.  I think this ought to be in the STL.
  // Result is <0, =0, or >0 according as begin1..end1 < = > begin2..end2
  // Assumes existence of fn cmp for single elements.
  template <class InputIterator1, class InputIterator2>
  inline int LexCmp3(InputIterator1 begin1, InputIterator1 end1,
                     InputIterator2 begin2, InputIterator2 end2)
  {
    while (begin1 != end1 && begin2 != end2)
    {
      const int sign = cmp(*begin1, *begin2);
      if (sign != 0) return sign;
      ++begin1;
      ++begin2;
    }
    if (begin1 != end1) return 1;
    if (begin2 != end2) return -1;
    return 0;
  }


  // // This function object class taken from Meyers's "Effective STL", page 38 (first ed.)
  // // It can be used to delete all the elements in an STL container of pointers.
  // struct DeleteObject
  // {
  //   template<typename T> void operator()(const T* ptr) const
  //   {
  //     delete ptr;
  //   }
  // };


} // end of namespace CoCoA



// RCS header/log in the next few lines.
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/VectorOperations.H,v 1.3 2014/09/26 14:47:36 abbott Exp $
// $Log: VectorOperations.H,v $
// Revision 1.3  2014/09/26 14:47:36  abbott
// Summary: Moved defn of OutputRange; added fwd decl, to keep LLVM compiler quiet
// Author: JAA
//
// Revision 1.2  2014/07/31 14:47:28  abbott
// Summary: Corrected copyright date
// Author: JAA
//
// Revision 1.1  2014/07/31 14:43:43  abbott
// Summary: Merged io.H and UtilsTemplate.H into this file
// Author: JAA
//
// Revision 1.5  2014/07/31 13:03:31  abbott
// Summary: Removed needless include of <vector>
// Author: JAA
//
// Revision 1.4  2014/07/15 16:50:29  abbott
// Summary: Commented out DeleteObject -- never used by anyone
// Author: JAA
//
// Revision 1.3  2014/07/14 16:04:43  abbott
// Summary: HasUniqueOwner should now work for any container
// Author: JAA
//
// Revision 1.2  2014/07/14 15:04:46  abbott
// Summary: Moved LexCmp3 here from utils.H; minor cleaning
// Author: JAA
//
// Revision 1.1  2014/07/14 14:22:07  abbott
// Summary: Renamed from tmp.H
// Author: JAA
//
// Revision 1.6  2011/02/10 15:25:39  bigatti
// -- fixed HasUniqueOwner for empty lists
//
// Revision 1.5  2009/11/20 15:52:43  bigatti
// -- fixed string
//
// Revision 1.4  2009/07/24 15:15:58  bigatti
// -- unified sum (and product) for C++ lists and vectors
// -- added: HasUniqueOwner for vector of elements with function "owner"
//
// Revision 1.3  2009/05/20 14:26:19  abbott
// CVS says something has changed, but I cannot see what (maybe whitespace?).
//
// Revision 1.2  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.3  2006/12/06 17:18:40  cocoa
// -- removed #include "config.H"
//
// Revision 1.2  2006/10/06 14:04:15  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//
// Revision 1.1.1.1  2006/05/30 11:39:37  cocoa
// Imported files
//
// Revision 1.3  2006/03/27 16:15:39  cocoa
// Checking in functions for printing lists and vectors of things,
// so that GPoly.C will compile.  Will reorganize things another day.
//
// Revision 1.2  2006/03/27 12:21:25  cocoa
// Minor silly changes to reduce number of complaints from some compiler or other.
//
// Revision 1.1  2006/01/20 17:44:20  cocoa
// Friday afternoon check-in: you have been warned!
// Fixed ref counting bug in SmartPtrIRC.
// Added sum/product functions in tmp.H.
// Minor tidying here and there.
//

#endif
