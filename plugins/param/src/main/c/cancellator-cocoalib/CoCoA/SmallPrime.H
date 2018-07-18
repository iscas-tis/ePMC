#ifndef CoCoA_SmallPrime_H
#define CoCoA_SmallPrime_H

//   Copyright (c)  2005  John Abbott

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


namespace CoCoA
{

  bool IsSmallPrime(long n);
  long NextSmallPrime(long n);
  long PrevSmallPrime(long n);
  long FindPrimRoot(long p);


  /*-----------------------------------------------------------------*/
  /** \include SmallPrime.txt  */
  /*-----------------------------------------------------------------*/
  class SmallPrimeSource
  {
  public:
    SmallPrimeSource(long StartingFrom = 0);
    ~SmallPrimeSource() {};
    SmallPrimeSource& operator++();
    friend long CurrentPrime(const SmallPrimeSource& ps);
  private:
    long myCurrentPrime;
  };


  inline long CurrentPrime(const SmallPrimeSource& ps)
  {
    return ps.myCurrentPrime;
  }

}  // end of namespace CoCoA




// RCS header/log
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/SmallPrime.H,v 1.3 2011/03/14 10:28:48 abbott Exp $
// $Log: SmallPrime.H,v $
// Revision 1.3  2011/03/14 10:28:48  abbott
// Changed unsigned long into long.
//
// Revision 1.2  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.3  2006/12/06 17:25:43  cocoa
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
// Revision 1.1.1.1  2005/10/17 10:46:54  cocoa
// Imported files
//
// Revision 1.1.1.1  2005/05/03 15:47:31  cocoa
// Imported files
//
// Revision 1.2  2005/04/19 14:06:04  cocoa
// Added GPL and GFDL licence stuff.
//
// Revision 1.1.1.1  2005/01/27 15:12:13  cocoa
// Imported files
//
// Revision 1.3  2004/11/12 15:49:29  cocoa
// Tidying prior to 0.90 release.
// (a) documentation improved (or marked as poor)
// (b) sundry minor improvements to the code
//
// Revision 1.1.1.1  2003/09/24 12:55:43  cocoa
// Imported files
//
// Revision 1.2  2003/06/23 16:10:37  abbott
// Minor cleaning prior to public release.
//
// Revision 1.1  2002/01/30 18:06:59  abbott
// Initial revision
//

#endif
