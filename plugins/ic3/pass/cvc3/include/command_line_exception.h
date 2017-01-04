/*****************************************************************************/
/*!
 * \file command_line_exception.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Fri May 30 14:59:51 2003
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
 * An exception thrown on an error while processing a command line
 * argument.
 */
/*****************************************************************************/

#ifndef _cvc3__command_line_exception_h_
#define _cvc3__command_line_exception_h_

#include "exception.h"

namespace CVC3 {
class CLException: public Exception {
public:
  // Constructors
  CLException() { }
  CLException(const std::string& msg): Exception(msg) { }
  CLException(const char* msg): Exception(msg) { }
  // Destructor
  virtual ~CLException() { }
  // Printing the message
  virtual std::string toString() const {
    return "Error while processing a command line option:\n  " + d_msg;
  }
};

}

#endif
