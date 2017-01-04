#ifndef CoCoA_BuildInfo_H
#define CoCoA_BuildInfo_H

//   Copyright (c)  2005,2007  John Abbott

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


#include<iosfwd>
//using std::ostream;
#include <string>

namespace CoCoA
{
  // String constants containing the build information.
  namespace BuildInfo
  {
    const std::string& version();       ///< Which version of CoCoALib is this?
    const std::string& compiler();
    const std::string& CompilationFlags();
    const std::string& CompilationDefines();

    void PrintAll(std::ostream& out);       ///< Print all the build information on stream out.
  }

} // end of namespace CoCoA



// RCS header/log in the next few lines.
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/BuildInfo.H,v 1.5 2012/10/02 11:45:09 bigatti Exp $
// $Log: BuildInfo.H,v $
// Revision 1.5  2012/10/02 11:45:09  bigatti
// -- added CompilationDefines
//
// Revision 1.4  2012/10/02 10:36:12  abbott
// Revised interface to BuildInfo information strings.
// Several consequential changes.
//
// Revision 1.3  2012/06/20 12:26:26  bigatti
// -- now exporting also compiler and compilation flags
//
// Revision 1.2  2007/10/30 17:14:12  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.4  2007/02/08 22:34:22  cocoa
// Changed BuildInfo: only BuildInfo version is publicly visible.
// Only BuildInfo needs complicated compile-time flags, so several changes
// to Makefiles etc.  Added a new example: ex-BuildInfo.C
//
// Revision 1.3  2006/12/06 17:09:37  cocoa
// -- removed #include "config.H"
//
// Revision 1.2  2006/10/06 14:04:16  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//
// Revision 1.1.1.1  2006/05/30 11:39:36  cocoa
// Imported files
//
// Revision 1.1.1.1  2005/10/17 10:46:54  cocoa
// Imported files
//
// Revision 1.1  2005/10/06 16:36:42  cocoa
// Added the capability find out build information at run-time.
// The Makefiles should be a little tidier too.
//

#endif
