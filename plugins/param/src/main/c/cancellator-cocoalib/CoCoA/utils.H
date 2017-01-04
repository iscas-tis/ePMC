#ifndef CoCoA_utils_H
#define CoCoA_utils_H

//   Copyright (c)  2006-2009,2011  John Abbott

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

#include "CoCoA/convert.H"

#include <limits>
// using std::numeric_limits
#include <cmath>
// using std::floor;
// using std::sqrt;
#include <vector>
// using std::vector

#include<iostream>

namespace CoCoA
{

  // I think these 3 fns should really be in the C++ standard library.
  // Round to nearest integer (rounding up in case of a tie).
  inline double round(double x)
  {
    return std::floor(x+0.5);
  }

  // The square of x -- useful if x is a big expression.
  inline double square(double x)
  {
    return x*x;
  }


  // Computes hi - lo; assumes hi >= lo.
  inline unsigned long ULongDiff(long hi, long lo)
  {
    CoCoA_ASSERT(lo <= hi);
    return static_cast<unsigned long>(hi) - lo; // lo is silently converted to ulong
  }


  inline std::vector<long> LongRange(long lo, long hi) // lo & hi both included
  {
    if (lo > hi) return std::vector<long>();
    const unsigned long n = ULongDiff(hi, lo);
    if (n+1 == 0) CoCoA_ERROR(ERR::ArgTooBig, "LongRange");
    std::vector<long> ans(n+1);
    ans[n] = hi; // this way it works even when hi=LONG_MAX
    std::vector<long>::iterator ptr = ans.begin();
    for (long val=lo; val < hi; ++val)
      *ptr++ = val;
    return ans;
  }


  // This template fn should be used only for integral types T.
  // MaxSquarableInteger<T>() gives largest value whose square fits in T.
  template<typename T>
  T MaxSquarableInteger()
  {
    CoCoA_ASSERT(std::numeric_limits<T>::is_integer);
    CoCoA_ASSERT(std::numeric_limits<T>::is_bounded);
    const static T SqrtMax = isqrt(std::numeric_limits<T>::max());
    return SqrtMax;
  }


  template <typename T> class NumericCast; // fwd decl -- defined in convert.H

  // len is the same as .size() except that result is long rather than size_t
  template<typename T>
  long len(const T& container)
  {
#if CoCoA_DEBUG
    return NumericCast<long>(container.size());  // NumericCast throws in case of overflow
#else
    return container.size(); // automatically cast to long without checking
#endif
  }


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/utils.H,v 1.22 2014/07/14 15:05:35 abbott Exp $
// $Log: utils.H,v $
// Revision 1.22  2014/07/14 15:05:35  abbott
// Summary: Removed LexCmp3 (moved into UtilsTemplate.H)
// Author: JAA
//
// Revision 1.21  2014/04/30 16:03:08  abbott
// Summary: Modified len to enable range check only when CoCoA_DEBUG is defined
// Author: JAA
//
// Revision 1.20  2014/04/11 13:36:19  abbott
// Summary: Added missing std:: prefix in two assertions
// Author: JAA
//
// Revision 1.19  2014/04/04 10:14:44  abbott
// Summary: New shorter impl of MaxSquarableInteger
// Author: JAA
//
// Revision 1.18  2014/04/03 15:52:15  abbott
// Summary: Improved commented out impl of BiggestSqBelow2Power
// Author: JAA
//
// Revision 1.17  2014/04/03 15:32:35  abbott
// Summary: New impls for LongRange, ULongDiff, MaxSquarableInteger
// Author: JAA
//
// Revision 1.16  2013/02/14 17:35:14  bigatti
// -- added LongRange -- useful for submat calls
//
// Revision 1.15  2011/12/05 16:54:40  bigatti
// -- changed: MachineInteger --> MachineInt (just in comment)
//
// Revision 1.14  2011/09/06 13:37:50  abbott
// Moved ULong2Long into separate file (ULong2Long.H)
//
// Revision 1.13  2011/08/27 21:48:11  abbott
// Commented out template fn "cmp" -- because it preempts cmp fn for MachineInt (qv.)
//
// Revision 1.12  2011/08/27 20:49:20  abbott
// Improved defn of ULong2Long -- needs more work.
//
// Revision 1.11  2011/03/14 10:25:52  abbott
// Added ULong2Long: an inline fn to convert unsigned long into long
// (i.e. invert the automatic conversion long to unsigned long).
//
// Revision 1.10  2011/03/09 17:21:21  bigatti
// -- added #include "CoCoA/convert.H"
//
// Revision 1.9  2011/03/09 15:52:52  abbott
// Added fwd decl for NumericCast.
//
// Revision 1.8  2011/03/09 14:56:19  abbott
// Added template fn "len" (v. similar to mem fn size).
//
// Revision 1.7  2009/10/29 18:30:40  abbott
// Corrected copyright years.
//
// Revision 1.6  2009/06/04 14:39:29  abbott
// Added template class NumericLimits (similar to std::numeric_limits) so that
// one can easily find the largest integer whose square fits into a given type.
//
// Revision 1.5  2008/11/06 12:50:44  abbott
// Moved definitions of square and round to utils.H from ApproxPts.H
//
// Revision 1.4  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.3  2007/05/21 12:53:08  abbott
// No real change in the end.
//
// Revision 1.2  2007/03/23 18:38:43  abbott
// Separated the "convert" functions (and CheckedCast) into their own files.
// Many consequential changes.  Also corrected conversion to doubles.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.5  2007/03/08 18:42:05  cocoa
// Cleaned up whitespace.
//
// Revision 1.4  2006/12/06 17:18:40  cocoa
// -- removed #include "config.H"
//
// Revision 1.3  2006/11/27 13:06:23  cocoa
// Anna and Michael made me check without writing a proper message.
//
// Revision 1.2  2006/10/06 14:04:15  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//
// Revision 1.1  2006/08/07 21:23:25  cocoa
// Removed almost all publicly visible references to SmallExponent_t;
// changed to long in all PPMonoid functions and SparsePolyRing functions.
// DivMask remains to sorted out.
//

#endif
