#ifndef CoCoA_ThreadsafeCounter_H
#define CoCoA_ThreadsafeCounter_H

//   Copyright (c)  2012  John Abbott

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
//   -------------------------------------------------------------------------

#include <iosfwd>
//using std::ostream;

#ifdef CoCoA_WITH_BOOST
#include <boost/thread/mutex.hpp>
// using boost::mutex;
#endif

namespace CoCoA
{

  // This class was taken from BOOST's mutex example.
  class ThreadsafeCounter
  {
  public:
    ThreadsafeCounter(): myCount(0) {}
    long myAdvance(long n);  // returns previous value of counter
    friend std::ostream& operator<<(std::ostream& out, const ThreadsafeCounter& c);
  private: // data members
#ifdef CoCoA_WITH_BOOST
    boost::mutex myMutex;
#endif
    long myCount;
  };

  std::ostream& operator<<(std::ostream& out, const ThreadsafeCounter& c);

} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/ThreadsafeCounter.H,v 1.2 2012/07/19 17:12:48 abbott Exp $
// $Log: ThreadsafeCounter.H,v $
// Revision 1.2  2012/07/19 17:12:48  abbott
// Added default **NON-THREADSAFE** version if BOOST is not available.
//
// Revision 1.1  2012/05/29 14:54:00  abbott
// Made ThreadsafeCounter separate from symbol.
//
//

#endif
