/*****************************************************************************/
/*!
 * \file eval_exception.h
 *
 * Author: Sergey Berezin
 *
 * Created: Tue Feb 25 14:58:57 2003
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
 * An exception thrown on an error while evaluating a command.  Use it
 * only when the error does not fall under any of the standard cases
 * like typecheck or parse errors.
 */
/*****************************************************************************/

#ifndef _cvc3__eval_exception_h_
#define _cvc3__eval_exception_h_

#include "exception.h"

namespace CVC3 {
class EvalException: public Exception {
public:
  // Constructors
  EvalException() { }
  EvalException(const std::string& msg): Exception(msg) { }
  EvalException(const char* msg): Exception(msg) { }
  // Destructor
  virtual ~EvalException() { }
  // Printing the message
  virtual std::string toString() const {
    return "Error while evaluating a command:\n  " + d_msg;
  }
};

class ResetException: public Exception {
public:
  // Constructors
  ResetException(): Exception("Reset Exception") { }
  // Destructor
  virtual ~ResetException() { }
};

}

#endif
