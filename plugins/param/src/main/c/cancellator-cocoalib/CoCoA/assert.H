#ifndef CoCoA_assert_H
#define CoCoA_assert_H

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

  void AssertionFailed(const char* const cond, const char* const file, unsigned long line);

} // end of namespace CoCoA


// The CoCoA_ASSERT macro checks conditions only if compilation is with the
// CoCoA_DEBUG flag set; if the flag is not set, the macro does nothing
// whatsoever (not even evaluating its argument).  After compiling with
// CoCoA_DEBUG set, the executable checks that the condition is true;
// if not, it calls CoCoA::AssertionFailed which prints out a useful message.
#ifndef CoCoA_DEBUG
#define CoCoA_ASSERT(cond) static_cast<void>(0)
#else
#define CoCoA_ASSERT(cond) do { if (!(cond)) CoCoA::AssertionFailed(#cond, __FILE__, __LINE__); } while(0)
#endif


// RCS header/log in the next few lines.
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/assert.H,v 1.2 2007/10/30 17:14:11 abbott Exp $
// $Log: assert.H,v $
// Revision 1.2  2007/10/30 17:14:11  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.2  2006/12/06 17:17:44  cocoa
// -- removed #include "config.H"
//
// Revision 1.1  2006/10/06 14:04:57  cocoa
// The new assert header and implementation files.
// A new test.
//
//

#endif
