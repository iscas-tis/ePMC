#ifndef CoCoA_OpenMathXML_H
#define CoCoA_OpenMathXML_H

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


#include "CoCoA/OpenMath.H"
#include "CoCoA/BigInt.H"

#include <iostream>
// using std::ostream;
#include <stack>
// using std::stack;
#include <string>
// using std::string


namespace CoCoA
{

  class OpenMathOutputXML: public OpenMathOutputBase
  {
  public:
    OpenMathOutputXML(std::ostream& out);
    ~OpenMathOutputXML();
    virtual void mySend(const MachineInt& n);
    virtual void mySend(const BigInt& N);  // outputs an integer
    virtual void mySend(const OpenMathSymbol& s);
    virtual void mySendApplyStart();
    virtual void mySendApplyEnd();
    virtual void mySendObjectStart();
    virtual void mySendObjectEnd();
    //void OutputAttribute();//??????
  private:
    std::ostream& myOut;  // UGLY, RISKY!!???!!
    long myLevel;  // nesting level
    std::stack<OpenMathTag> myTagStack;  // only really needed for debugging
    const char* myIndent();
  };


  class OpenMathInputXML: public OpenMathInputBase
  {
  public:
    OpenMathInputXML(std::istream& in); // just sets ref count to zero.
    virtual ~OpenMathInputXML();

    virtual void advance();
    virtual OpenMathTag myCurrTag();
    virtual long NumDescendants() const;
    virtual bool myRecv(long & n); // SHORT INTEGER
//????    virtual bool myRecv(unsigned long & n); // UNSIGNED SHORT INTEGER
    virtual bool myRecv(BigInt& N);    // LONG INTEGER
    virtual bool myRecv(OpenMathSymbol& s);
  private: // data members
    enum status_t { NotYetRead, AlreadyRead };
    status_t myStatus;
    OpenMathTag myCurrTagType;
    BigInt myIntValue;
    OpenMathSymbol mySymbol;
    std::istream& myIn;  // UGLY, RISKY!!???!!
    long myLevel;  // nesting level
    std::stack<OpenMathTag> myTagStack;  // only really needed for debugging
  private:
    char ReadChar();
    char SkipWSReadChar();
    void ReadNextNode();
    bool SkipMatch(const std::string& expected);
    bool ReadDecimalString(std::string& DecimalDigits);
    bool ReadStringInQuotes(std::string& QuotedString);
  };


} // end of namespace CoCoA


// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/OpenMathXML.H,v 1.8 2011/11/09 13:47:56 bigatti Exp $
// $Log: OpenMathXML.H,v $
// Revision 1.8  2011/11/09 13:47:56  bigatti
// -- renamed MachineInteger --> MachineInt
//
// Revision 1.7  2011/08/14 15:52:18  abbott
// Changed ZZ into BigInt (phase 1: just the library sources).
//
// Revision 1.6  2011/08/12 15:49:28  abbott
// Added mem fns for BigInt; also new data member.
//
// Revision 1.5  2011/03/11 14:49:08  abbott
// Changed size_t into long.
//
// Revision 1.4  2010/03/18 13:53:48  abbott
// Minor rationalization to OpenMath implementation: moved op<<(OMOut,ZZ) to ZZ files.
//
// Revision 1.3  2008/12/16 21:10:32  abbott
// Replaced the various output fns for different sort of machine integers by a
// single one for MachineInt.
//
// Revision 1.2  2007/10/30 17:14:12  abbott
// Changed licence from GPL-2 only to GPL-3 or later.
// New version for such an important change.
//
// Revision 1.1.1.1  2007/03/09 15:16:11  abbott
// Imported files
//
// Revision 1.7  2006/12/06 17:14:12  cocoa
// -- removed #include "config.H"
//
// Revision 1.6  2006/11/27 14:22:04  cocoa
// -- ooops, missing semicolon
//
// Revision 1.5  2006/11/27 13:53:10  cocoa
// changed mySend(std::size_t n)  into  mySend(unsigned long n)
//
// Revision 1.4  2006/11/27 13:04:35  cocoa
// Added explicit OpenMath output operators for all integer types.
//
// Revision 1.3  2006/11/02 13:25:44  cocoa
// Simplification of header files: the OpenMath classes have been renamed.
// Many minor consequential changes.
//
// Revision 1.2  2006/10/06 14:04:15  cocoa
// Corrected position of #ifndef in header files.
// Separated CoCoA_ASSERT into assert.H from config.H;
// many minor consequential changes (have to #include assert.H).
// A little tidying of #include directives (esp. in Max's code).
//

#endif
