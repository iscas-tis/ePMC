#ifndef CoCoA_ProgressReporter_H
#define CoCoA_ProgressReporter_H

//   Copyright (c)  2014  John Abbott

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

namespace CoCoA
{

  class ProgressReporter
  {
  public:
    explicit ProgressReporter(double interval);
    void operator()() { if (++myIntervalCount == myCheckCount && myIsTimeToPrint()) myPrintReport(); }
    void operator()(long arg1) { if (++myIntervalCount == myCheckCount && myIsTimeToPrint()) myPrintReport(arg1); }
    void operator()(long arg1, long arg2) { if (++myIntervalCount == myCheckCount && myIsTimeToPrint()) myPrintReport(arg1, arg2); }

  private:
    long myIntervalCount;
    long myCheckCount;
    long myTotalCount;
    double myLastCheckTime;
    double myTargetInterval;
    double myNextPrintTime;
    long myLastPrintCount;  // used only for computing the rate
    double myLastPrintTime; // (ditto)
    friend std::ostream& operator<<(std::ostream& out, const ProgressReporter& PR);
  private:
    double myRate(); // called only by the printing fns; auto-updates myLastPrintCount & myLastPrintTime
    void myPrintReport();
    void myPrintReport(long arg1);
    void myPrintReport(long arg1, long arg2);
    bool myIsTimeToPrint();
  };

  std::ostream& operator<<(std::ostream& out, const ProgressReporter& PR);

  void increase125(long& n);
  void decrease125(long& n);


} // end of namespace CoCoA



// RCS header/log in the next few lines
// $Header: /Volumes/Home_1/cocoa/cvs-repository/CoCoALib-0.99/include/CoCoA/ProgressReporter.H,v 1.2 2014/10/13 18:05:43 abbott Exp $
// $Log: ProgressReporter.H,v $
// Revision 1.2  2014/10/13 18:05:43  abbott
// Summary: Added code to print out measured rate in each progress report
// Author: JAA
//
// Revision 1.1  2014/04/28 16:48:41  abbott
// Summary: Header file for ProgressReporter
// Author: JAA
//
//

#endif
