/**************************** CPPHeaderFile ***************************

* FileName [Timer.h]

* PackageName [util]

* Synopsis [Header file for Timer class.]

* Description [This class encapsulates a timer used for making time
* measurements.]

* SeeAlso [Timer.cpp]

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2007 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#ifndef __TIMER_H__
#define __TIMER_H__

namespace util {

/*! \class Timer The class allows measurment of time 
    \ingroup util
*/
class Timer
{
 private:
  double start;
  double time;

 public:
  Timer() { start = -1; time = 0; }
  Timer(const Timer &rhs);  
  const Timer &operator = (const Timer &rhs);

  bool Running() const;
  void Start();
  void Stop();
  void Forward(const Timer &arg);
  double Read() const;
};

} //namespace util

#endif //__TIMER_H__

/*********************************************************************/
//end of Timer.h
/*********************************************************************/
