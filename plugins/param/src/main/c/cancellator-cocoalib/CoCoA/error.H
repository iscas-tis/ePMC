#ifndef CoCoA_error_H
#define CoCoA_error_H

//   Copyright (c)  2005-2010  John Abbott

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



// All errors should be thrown by calling the function "error"
// as this makes debugging easier and doesn't adversely affect
// run-time performance.  The function "error" will print a brief
// message if CoCoA_DEBUG is set, and it will throw the error object.


#include <exception>
//using std::exception;
#include <string>
//using std::string;
#include <iosfwd>
//using std::ostream;

// UGLY WORKAROUND FOR SOLARIS BUG!!!
// (some header file defines the preprocessor symbol ERR to be 13)
#if defined(ERR)
#  undef ERR
#endif


namespace CoCoA
{

  namespace ERR
  {
    struct ID
    {
      ID(const char* const ErrName, const char* const DefaultMesg): myName(ErrName), myDefaultMesg(DefaultMesg) {};
      bool operator<(const ID& rhs) const;
      bool operator==(const ID& rhs) const;
      bool operator!=(const ID& rhs) const;
      const char* const myName;
      const char* const myDefaultMesg;
    };

    extern const ID
            LANGUAGE,          // Not really an error message, just the name of the language
            nonstandard,       // To create a nonstandard error message
            UNKNOWN,           // To signal an error of unknown type -- can this ever be used???
            ArgTooBig,         // Argument too large in a numerical function
            AssertFail,        // An assertion failed
            BadArg,            // The arg(s) given are unsuitable
            BadArraySize,      // Unsuitable array size (incompatible with other arguments)
            BadCodomain,       // Supplied RingHom has wrong codomain
            BadColIndex,       // Column index too large in a matrix
            BadCompose,        // Attempt to compose maps with differing domain and codomain
            BadComptIndex,     // Index too large accessing component of ModuleElem
            BadConvert,        // Unable to convert value
            BadDegIndex,       // Index too large accessing degree component
            BadDomain,         // Supplied RingHom has wrong domain
            BadGlobalSettings, // ambiguous, duplicate or incompatible global settings
            BadIndetIndex,     // Indeterminate index too large in a polynomial ring
            BadIndex,          // Index out of range
            BadIndetNames,     // Duplicate indet names or varied number of indices for a single name
            BadInducingHom,    // Inducing hom has wrong domain
            BadInducingHom2,   // Inducing hom has wrong codomain
            BadInducingHomKer, // Inducing hom has unsuitable kernel
            BadMatrixSetEntry, // Attempt to set a matrix entry where this is forbidden
            BadMatrixSize,     // Matrix of the wrong size for the requested operation
            BadModulus,        // modulus must be at least 2 and fit into a long
            BadNumBase,        // Base for number conversion must be between 2 and 36 (incl)
            BadNumIndets,      // Wrong number of indet names when creating a PPMonoid or PolyRing
            BadOpenMath,       // OpenMath input did not contain the expected tag
            BadPPMonoid,       // PPMonoid supplied is not of the right type
            BadPPMonoidHomArg, // Argument given to PPMonoidHom is not in the domain
            BadProbability,    // Probability P must satisfy  0 <= P <= 1
            BadPwrZero,        // Attempt to compute 0^N where N is not strictly positive
            BadPolyRingHomImages,// Unsuitable indet images for creating PolyRing hom (wrong number or wrong ring)
            BadQuot,           // Attempt to compute a quotient which does not exist in a given ring/monoid
            BadQuotRing,       // Attempt to quotient by ideal(1)
            BadRing,           // Used ring does not have required properties
            BadRingHomArg,     // Argument given to a RingHom is in the wrong ring
            BadRingHomArg2,    // Argument given to a partial RingHom is outside the domain
            BadRowIndex,       // Row index too large in a matrix
            BadSmallFpChar,    // Characteristic (for small finite field) too large or not prime
            BadSymbolHead,     // Invalid symbolic name head (with illegal characters)
            BadSymbolSubscript,    // Symbolic name: subscript too large or name does not have that many subscripts
            BadSymbolRange,    // Incompatible names given to range
            BLASFailed,        // A BLAS function failed
            CanonicalHomFail,  // Unable to construct canonical homomorphism
            ConstMatEntry,     // Cannot assign to constant matrix entry
            DeadMemPool,       // Attempt to use a MemPool after it has been destroyed
            DivByZero,         // Division by zero or a zero-divisor
            Empty,             // Empty list or vector
            EmbedBigRatFailed, // Cannot embed rational number into ring
            ExpTooBig,         // Exponent is too large
            GlobalManager1,    // No GlobalManager object has been created
            GlobalManager2,    // May not create more than one GlobalManager object
            IdealNotInRing,    // Incompatible ring and ideal
            IncompatDims,      // Incompatible dimenions
            InputFail,         // An input operation failed
            InsuffPrec,        // Insufficient precision when using RingTwinFloat values
    ////???            IntDivByNeg,       // Integer division/remainder by negative divisor
            InvertibleRingElem,// Non-invertible RingElem required
            IterEnded,         // Attempt to advance an iter which has already reached the end
            LapackFailed,      // A Lapack driver failed
            LogZero,           // Cannot compute log of zero
            MemPoolZero,       // Cannot use MemPool to manage blocks of zero size
            MissNumLibs,       // Numerical libraries not configured in
            MixedCoeffRings,   // Arithmetic operation between polys with different coeff rings
            MixedDegrees,      // Arithmetic operation between incompatible degrees
            MixedModules,      // Arithmetic operation between elements of different modules
            MixedPolyIters,    // Comparison between iterators over different polys
            MixedPPMs,         // Arithmetic operation between elements of different PPMonoids
            MixedRings,        // Arithmetic operation between elements of different rings
            MixedSizes,        // Operation between objects with different size
            ModulusLT2,        // Modulus must be >= 2
            NegExp,            // Negative exponent
            NoGlobalMgr,       // GlobalManager must be created before using CoCoALib
            NotCommutative,    // Ring must be commutative
            NotDenseUPolyRing, // Ring must be a dense univariate polynomial ring, but is not
            NotElemFrF,        // Operation makes sense only for elements of a fraction field
            NotElemGCDDomain,  // Operation makes sense only for elements of a GCD domain
            NotElemPolyRing,   // Operation makes sense only for elements of a polynomial ring
            NotElemQuotientRing,// Operation makes sense only for elements of a quotient ring
            NotElemSparsePolyRing,// Operation makes sense only for elements of a sparse polynomial ring
            NotFGModule,       // Module must be Finitely Generated, but is not
            NotField,          // Ring must be a field, but is not
            NotFracField,      // Ring must be a FractionField, but is not
            NotFreeModule,     // Module must be free, but is not
            NotFullRank,       // Matrix must be full rank, but is not
            NotModuleSpPR,     // Module must be on a SparsePolyRing, but is not
            NotIndet,          // Expected an indeterminate
            NotIntegralDomain, // Ring must be an integral domain, but is not
            NotInvMatrix,      // Matrix is not invertible
            NotMonomialGens,   // Ideal must be generated by monomials
            NotNonNegative,    // Value must be non-negative
            NotNonNegativeGrading,// Grading is not non-negative
            NotNonZero,        // Value must be non-zero
            NotOrdDom,         // Ring must be ordered, but is not
            NotPolyRing,       // Ring must be a polynomial ring, but is not
            NotPositive,       // Value is not positive
            NotPositiveGrading,// Grading is not positive
            NotQuotientRing,   // Ring must be a quotient ring, but is not
            NotRingTwinFloat,  // Operation valid only over RingTwinFloat
            NotSparsePolyRing, // Ring must be a sparse polynomial ring, but is not
            NotSquareMatrix,   // Matrix must be square, but is not
            NotTermOrdering,   // Ordering is not a term-ordering
            NotTrueGCDDomain,  // Ring must be a true GCD domain (and not a field), but is not
            NotUnit,           // Operation is valid only for invertible elements
            NYI,               // NOT YET IMPLEMENTED
            PolyIterEnded,     // Attempt to use an off-the-end iterator
            PPOrder,           // PP is not in the right order
            ZeroModulus,       // Numerical operation specified a zero modulus
            ZeroGradingDim,    // GradingDim (grading dimension) is 0
            ZeroRingElem,      // Non-zero RingElem required
            SERIOUS;           // Execution should never have reached here
  }

  /*-----------------------------------------------------------------*/
  /** \include error.txt  */
  /*-----------------------------------------------------------------*/
  class ErrorInfo: public std::exception
  {
  public:
    ErrorInfo(const std::string& mesg, const std::string& func);
    ErrorInfo(const std::string& mesg, const std::string& func, const char* file, unsigned long line);
    ErrorInfo(ERR::ID id, const std::string& func);
    ErrorInfo(ERR::ID id, const std::string& func, const char* file, unsigned long line);
    ~ErrorInfo() throw() {}
///???    const ERR::ID& id() const { return myID; }
    const char* what() const throw() { return myMessage.c_str(); }
    bool operator==(const ERR::ID& id) const { return myID == id; }
    bool operator!=(const ERR::ID& id) const { return myID != id; }
    void myOutputSelf(std::ostream& out) const;
  private: // Data members
    friend void ANNOUNCE(std::ostream& out, const ErrorInfo& err);
    const ERR::ID myID;
    const std::string myMessage;
    const std::string myContext;
    const std::string myFile;
    const unsigned long myLine;
  };

  std::ostream& operator<<(std::ostream& out, const ErrorInfo& err);
  void ANNOUNCE(std::ostream& out, const ErrorInfo& err);
  void ThrowError(const ErrorInfo& err);


  namespace ErrorLanguage
  {
    void english(); // Set english as the language for error messages.
    void italian(); // Set italian as the language for error messages.
  }


} // end of namespace CoCoA

// Macro: using this allows the file and line number to be reported in the error message.
#define CoCoA_ERROR(ErrID, FnName) ::CoCoA::ThrowError(::CoCoA::ErrorInfo(ErrID, FnName, __FILE__, __LINE__))



// RCS header/log in the next few lines.
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/error.H,v 1.42 2014/07/10 14:33:42 abbott Exp $
// $Log: error.H,v $
// Revision 1.42  2014/07/10 14:33:42  abbott
// Summary: Removed unused error code NotRingZ (or NotRingZZ, there was a typo!)
// Author: JAA
//
// Revision 1.41  2014/01/29 17:01:22  abbott
// Summary: Removed rather pointless "(no message)" default string for message associated to an error code
// Author: JAA
//
// Revision 1.40  2013/05/27 17:07:00  bigatti
// -- new name for error of FreeModule over SparsePolyRing
//
// Revision 1.39  2013/05/20 15:58:17  abbott
// Added new error code NotPositive.
//
// Revision 1.38  2013/02/21 16:56:49  bigatti
// -- added ERR:Empty
//
// Revision 1.37  2013/02/13 09:08:58  bigatti
// -- added ZeroGradingDim
//
// Revision 1.36  2013/01/21 13:37:29  abbott
// Added new error IncompatDims.
//
// Revision 1.35  2012/05/24 14:53:35  bigatti
// -- changed symbol "index" into "subscripts"
//
// Revision 1.34  2012/05/22 10:02:37  abbott
// Removed IsGCDDomain; substituted by IsTrueGCDDomain.
// Added IsFractionFieldOfGCDDomain.
//
// Revision 1.33  2012/03/26 11:51:35  abbott
// Rephrased error message associated with ERR::NotIndet.
//
// Revision 1.32  2012/03/16 15:51:35  abbott
// Added new error NotNonZero.
//
// Revision 1.31  2011/08/24 10:22:45  bigatti
// -- renamed QQ --> BigRat
//
// Revision 1.30  2011/06/23 16:02:26  abbott
// Minor changes to two error message strings.
//
// Revision 1.29  2011/05/03 09:54:44  abbott
// Changed macro arg name (to improve readability).
//
// Revision 1.28  2011/03/16 13:22:43  abbott
// Added new error "BadModulus".
//
// Revision 1.27  2011/01/19 16:13:06  bigatti
// -- added ERR::ModulusLT2
//
// Revision 1.26  2010/12/17 16:09:15  abbott
// Changed interface to ANNOUNCE function: now it requires the ostream to passed in.
// A few other minor changes.
//
// Revision 1.25  2010/10/22 14:03:04  abbott
// Major change to GMPAllocator -- it is now set/activated by the GlobalManager.
// This is a Friday afternoon check-in... hope to check in cleaner code in the
// next few days.
//
// Revision 1.24  2010/07/09 17:05:01  abbott
// Added new error for PPMonoid homs.
//
// Revision 1.23  2010/04/27 16:07:50  bigatti
// -- added 2 error messages for DynamicBitset
//
// Revision 1.22  2010/03/18 13:54:42  abbott
// Added new error for OpenMath input problems.
//
// Revision 1.21  2010/02/03 12:02:44  bigatti
// -- added NotMonomialGens
//
// Revision 1.20  2009/12/23 18:53:52  abbott
// Major change to conversion functions:
//   convert(..) is now a procedure instead of a function
//   IsConvertible(..) replaces the former convert(..) function
//   Added new NumericCast conversion function (placeholder for BOOST feature)
//   Consequent changes in code which uses these features.
//
// Revision 1.19  2009/12/11 11:46:32  abbott
// Changed fn  convert  into  IsConvertible.
// Added template procedure  convert.
// New version because change is not backward compatible.
//
// Revision 1.18  2009/12/03 17:26:35  abbott
// Renamed EFGModule to FGModule.
// Renamed ModuleBase member fns  myInit -> myNew, myKill -> myDelete.
// Removed some cruft (old code that was not used by anyone).
//
// Revision 1.17  2009/10/29 18:47:58  abbott
// Added IterEnded error code.
//
// Revision 1.16  2009/07/02 16:32:11  abbott
// Consequential changes stemming from new class QQ, and modified interface to the member
// function RingBase::myIsRational.  Also some new conversion functions.
//
// Revision 1.15  2009/05/20 14:25:30  abbott
// Added new error for InputFailCheck.
//
// Revision 1.14  2008/12/16 21:11:43  abbott
// Improved error message for bad symbol index -- there are two possible causes.
//
// Revision 1.13  2008/10/08 13:54:05  abbott
// New minor version to reflect the backward-incompatible changes to CoCoA errors.
//
// Revision 1.12  2008/10/08 09:48:19  abbott
// Final(?) minor refinement to CoCoA::ERR:ID internal layout.
//
// Revision 1.11  2008/10/07 15:45:22  abbott
// Changed ErrorInfo objects so they include the name of their own error ID.
// Changed catch statements to catch const objects.
// Removed calls to the member fn which accessed the error ID member of an
// ErrorInfo; now you simply compare directly with the error ID (makes the
// code easier to read).
//
// Revision 1.10  2008/06/30 17:21:08  abbott
// Added ugly workaround for Solaris ERR bug.
//
// Revision 1.9  2008/04/21 12:51:53  abbott
// Added some new error codes.
//
// Revision 1.8  2008/03/12 16:35:18  bigatti
// -- changed: IsHomogeneous --> IsHomog
// -- changed: ERR:ZeroPoly --> ERR::ZeroRingElem
//
// Revision 1.7  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.6  2007/10/05 14:35:16  bigatti
// -- added error: NotDenseUPolyRing
//
// Revision 1.5  2007/09/24 14:18:07  abbott
// Changed layout of comments.
//
// Revision 1.4  2007/06/21 21:29:47  abbott
// Changed name of RingFloat into RingTwinFloat.
//
// Revision 1.3  2007/05/21 12:57:28  abbott
// New class for passing machine integers as args; includes some simple
// operations on machine integers (cmp, gcd, IsNegative,...).  Operations
// between ZZ and machine integers modified to use the new class.  Inexact
// integer division (of a ZZ) by a negative value now triggers an error;
// new error for reporting inexact integer division by a negative value.
//
// Revision 1.2  2007/03/28 10:06:13  abbott
// Now gives error when you use RingZ() or RingQ() without creating GlobalManager.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.13  2007/03/08 11:07:12  cocoa
// Made pseudo ctors for polynomial rings more uniform.  This allowed me to
// remove an include of CoCoA/symbol.H  from the RingDistrM*.H files, but then
// I had to put the include in several .C files.
//
// Revision 1.12  2007/03/05 21:34:36  cocoa
// Ooops, forgot to check these in a few mins ago.  Revised names for hom pseudo-ctors; new error
// for CanonicalHom.
//
// Revision 1.11  2007/03/05 16:17:11  bigatti
// -- clenup for numerical code (and 3 new error codes)
//
// Revision 1.10  2007/03/03 14:07:23  bigatti
// -- "foundations" renamed into "GlobalManager"
//
// Revision 1.9  2007/03/02 10:47:53  cocoa
// First stage of RingZ modifications -- tests do not compile currently, Anna will fix this.
//
// Revision 1.8  2007/02/12 17:40:19  bigatti
// -- added MissNumLibs
//
// Revision 1.7  2007/01/13 14:14:34  cocoa
// Overhaul of RingHom code: it nows uses SmartPtrIRC, and printing is more logical.
// Have not yet updated the documentation.
//
// Revision 1.6  2006/12/21 13:46:54  cocoa
// Improved layout; added one new error code.
//
// Revision 1.5  2006/12/06 17:17:44  cocoa
// -- removed #include "config.H"
//
// Revision 1.4  2006/10/06 10:10:15  cocoa
// Corrected a comment.  Corrected position of #ifndef etc.
//
// Revision 1.3  2006/08/07 21:23:25  cocoa
// Removed almost all publicly visible references to SmallExponent_t;
// changed to long in all PPMonoid functions and SparsePolyRing functions.
// DivMask remains to sorted out.
//
// Revision 1.2  2006/07/17 19:16:53  cocoa
// New, better errors :-)
//
// Revision 1.1.1.1  2006/05/30 11:39:36  cocoa
// Imported files
//
// Revision 1.6  2006/04/14 13:51:40  cocoa
// Added a class for generating random bits (RandomBitStream in the file random.H).
//
// Revision 1.5  2006/04/07 16:44:52  cocoa
// Considerably updated MemPool design -- it works, and I'm about to test
// its efficiency against the old one.
//
// Revision 1.4  2006/04/04 13:01:02  cocoa
// -- added: NotNonNegativeGrading, NotPositiveGrading
//
// Revision 1.3  2006/03/27 12:21:26  cocoa
// Minor silly changes to reduce number of complaints from some compiler or other.
//
// Revision 1.2  2005/11/24 16:09:38  cocoa
// -- added operator[] for ModuleElem
//
// Revision 1.1.1.1  2005/10/17 10:46:54  cocoa
// Imported files
//
// Revision 1.8  2005/09/28 11:50:35  cocoa
// -- new code for graded modules
//
// Revision 1.7  2005/08/08 16:36:32  cocoa
// Just checking in before going on holiday.
// Don't really recall what changes have been made.
// Added IsIndet function for RingElem, PPMonoidElem,
// and a member function of OrdvArith.
// Improved the way failed assertions are handled.
//
// Revision 1.6  2005/07/19 15:30:20  cocoa
// A first attempt at iterators over sparse polynomials.
// Main additions are to SparsePolyRing, DistrMPoly*.
// Some consequential changes to PPMonoid*.
//
// Revision 1.5  2005/07/15 16:34:33  cocoa
// Added iterators for sparse polynomials.
// The code compiles (and the old tests still run).
// It'd Friday evening -- I'm going home before
// getting any ideas about making the iterator code run.
//
// Revision 1.4  2005/07/08 15:09:29  cocoa
// Added new symbol class (to represent names of indets).
// Integrated the new class into concrete polynomial rings
// and PPMonoid -- many consequential changes.
// Change ctors for the "inline" sparse poly rings: they no
// longer expect a PPMonoid, but build their own instead
// (has to be a PPMonoidOv).
//
// Revision 1.3  2005/07/01 16:08:16  cocoa
// Friday check-in.  Major change to structure under PolyRing:
// now SparsePolyRing and DUPolyRing are separated (in preparation
// for implementing iterators).
//
// A number of other relatively minor changes had to be chased through
// (e.g. IndetPower).
//
// Revision 1.2  2005/06/27 14:55:24  cocoa
// Cleaned up some more PPMonoid code.
//
// Revision 1.1.1.1  2005/05/03 15:47:30  cocoa
// Imported files
//
// Revision 1.8  2005/04/29 15:42:02  cocoa
// Improved documentation for GMPAllocator.
// Added example program for GMPAllocator.
// Added example program for simple ops on polynomials.
// Added two new ctors for (principal) ideals (from long, and from ZZ).
// Added (crude) printing for PPMonoids.
// Updated library.H (#included GMPAllocator.H).
//
// Revision 1.7  2005/04/27 16:14:56  cocoa
// Cleaned up example programs -- added "free use" permit.
// Changed a couple of ErrorInfo object names, and added
// ERR::NotTrueGCDDomain.
//
// Revision 1.6  2005/04/20 15:40:48  cocoa
// Major change: modified the standard way errors are to be signalled
// (now via a macro which records filename and line number).  Updated
// documentation in error.txt accordingly.
//
// Improved the documentation in matrix.txt (still more work to be done).
//
// Revision 1.5  2005/04/19 14:06:04  cocoa
// Added GPL and GFDL licence stuff.
//
// Revision 1.4  2005/03/11 16:44:18  cocoa
// New abstract class structure for matrices.
// New types of special matrix.
//
// Revision 1.3  2005/03/02 18:46:41  cocoa
// Added new types ConstRefMatrix, and RefMatrix following along
// the lines of ConstRefRingElem and RefRingElem.  The semantics
// should be a bit clearer now.
//
// Revision 1.2  2005/02/22 12:50:40  cocoa
// -- added: NotFullRank, NotIntDom, NotTermOrdering
//
// Revision 1.1.1.1  2005/01/27 15:12:13  cocoa
// Imported files
//
// Revision 1.19  2004/11/25 16:14:21  cocoa
// (1) Fixed definition of specialization of std::swap template function
//     so that it compiles with gcc 3.4.3
// (2) Implemented monomial function for polynomial rings.
// (3) Added one(PPM) and PPM->myOne() functions.
//
// Revision 1.18  2004/11/19 17:43:50  cocoa
// -- added matrix related errors
//
// Revision 1.17  2004/11/19 16:15:51  cocoa
// (a) Removed unused error message about degree of zero;
//     replaced it by a more general message about needing a
//     non-zero polynomial (for various ops such as LC, LPP).
// (b) Added some missing arg checking in LC, LPP and deg
//     (for elems of a PolyRing).
// (c) Updated some commented out code in GPair and GPoly.
//
// Revision 1.16  2004/11/19 15:14:09  cocoa
// (a) Added new check to MemPool so that it can signal an
//     error if one tries to use a MemPool after it has been
//     destroyed.
// (b) Improved makefile in TEST/ so that it checks output,
//     and prints useful messages if the test fails.
// (c) Tidied ring.txt a bit (still more to do).
//
// Revision 1.15  2004/11/12 15:49:29  cocoa
// Tidying prior to 0.90 release.
// (a) documentation improved (or marked as poor)
// (b) sundry minor improvements to the code
//
// Revision 1.14  2004/11/11 14:16:37  cocoa
// -- added \include *.txt for doxygen
//
// Revision 1.13  2004/11/11 11:56:09  cocoa
// (1) Tidied makefiles, and introduced common.mki
// (2) Improved several tests, and cleaned them so that they
//     handle sanely any otherwise unhandled exceptions.
//
// Revision 1.12  2004/11/09 15:53:56  cocoa
// -- added BadQuotRing("Attempt to quotient by ideal(1)")
//
// Revision 1.11  2004/11/05 15:37:56  cocoa
// Added a couple of new errors.
//
// Revision 1.10  2004/11/03 18:01:55  cocoa
// -- clarified behaviour of error()
// -- added ANNOUNCE function
// -- minor tidying
//
// Revision 1.8  2004/07/27 16:03:38  cocoa
// Added IsCommutative test and IamCommutative member function
// to all rings.  Tidied geobuckets a little.
//
// Revision 1.7  2004/07/20 15:04:06  cocoa
// The next step in the new "ring element" conversion process:
// handling the case of creating a "const RefRingElem" object
// (since C++ refuses to do this properly itself).
//
// Revision 1.6  2004/07/14 16:40:42  cocoa
// Separated RingFpLog from its implementation which now resides in
// a new class: SmallFpLogImpl.  This is analogous to the change made
// to RingFp yesterday.
//
// Some tidying and other sundry minor changes.
//
// Revision 1.5  2004/07/13 16:32:26  cocoa
// First stage of major revamp of ring elements.
// Implementation of RingFp has been split into "ring interface"
// and "algorithms plus data structures".
//
// Revision 1.4  2004/06/29 17:10:22  cocoa
// Partially tidied use of "protected" and "private" in various
// base classes.  Checking in at the end of the day -- it works,
// and I wouldn't want it to be lost next time point's disk
// misbehaves.
//
// Revision 1.3  2004/05/27 16:14:02  cocoa
// Minor revision for new coding conventions.
//
// Revision 1.2  2004/05/24 15:52:13  cocoa
// Major update:
//   new error mechanism
//   many fixes
//   RingHoms almost work now
//   RingFloat much improved
//
// Revision 1.1.1.1  2003/09/24 12:55:43  cocoa
// Imported files
//
// Revision 1.5  2003/06/23 16:15:05  abbott
// Minor cleaning prior to public release.
//
// Revision 1.4  2002/11/13 15:37:49  abbott
// Added a second field to CoCoAError to allow the location of an
// error to be indicated.  Also a new accessor function "where" to
// read the new field, and a query function saying whether the new
// field has a non-trivial value (HasLocation).  The old constructor
// remains, and is joined by a new one allowing the location field
// to be specified.
//
// Revision 1.3  2002/01/25 15:16:59  abbott
// Added class CoCoAError as a temporary solution; the standard library
// exception classes do not seem to offer what we need.
//
// Revision 1.2  2001/12/07 15:56:48  abbott
// error function now takes an exception as an argument, instead of a string.
//
// Revision 1.1  2001/11/26 19:13:24  abbott
// Initial revision
//

#endif
