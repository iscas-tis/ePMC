#ifndef CoCoA_time_H
#define CoCoA_time_H

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

  /*-----------------------------------------------------------------*/
  /** \include time.txt  */
  /*-----------------------------------------------------------------*/
  double CpuTime();  ///< Returns user CPU usage in seconds since start of program.
  double RealTime(); ///< Returns number of seconds elapsed since some fixed point in the past.
  void DateTime(long& date, long& time); ///< yyyymmdd, hhmmss

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/time.H,v 1.3 2011/07/06 15:49:41 bigatti Exp $
// $Log: time.H,v $
// Revision 1.3  2011/07/06 15:49:41  bigatti
// -- added DateTime
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
// Revision 1.2  2006/01/20 17:44:20  cocoa
// Friday afternoon check-in: you have been warned!
// Fixed ref counting bug in SmartPtrIRC.
// Added sum/product functions in tmp.H.
// Minor tidying here and there.
//
// Revision 1.1  2005/11/15 12:24:35  cocoa
// New timing functions.
//

#endif
