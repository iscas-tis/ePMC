/*****************************************************************************/
/*!
 * \file sound_exception.h
 * \brief An exception to be thrown when unsoundness is detected in a proof rule
 * 
 * Author: Sergey Berezin
 * 
 * Created: Fri Jun  6 10:48:38 2003
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 * 
 * <hr>
 * 
 */
/*****************************************************************************/

#ifndef _cvc3__sound_exception_h_
#define _cvc3__sound_exception_h_

#include <string>
#include <iostream>
#include "exception.h"

namespace CVC3 {

  class SoundException: public Exception {
  public:
    // Constructors
    SoundException() { }
    SoundException(const std::string& msg): Exception(msg) { }
    SoundException(const char* msg): Exception(msg) { }
    // Destructor
    virtual ~SoundException() { }
    virtual std::string toString() const {
      return "Soundness error: " + d_msg;
    }
  }; // end of class SoundException
} // end of namespace CVC3 

#endif
