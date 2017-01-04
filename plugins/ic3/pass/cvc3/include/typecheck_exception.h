/*****************************************************************************/
/*!
 * \file typecheck_exception.h
 * \brief An exception to be thrown at typecheck error.
 *
 * Author: Sergey Berezin
 *
 * Created: Fri Feb 14 18:44:15 2003
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

#ifndef _cvc3__typecheck_exception_h_
#define _cvc3__typecheck_exception_h_

#include <string>
#include <iostream>
#include "exception.h"

namespace CVC3 {

  class TypecheckException: public Exception {
  public:
    // Constructors
    TypecheckException() { }
    TypecheckException(const std::string& msg): Exception(msg) { }
    TypecheckException(const char* msg): Exception(msg) { }
    // Destructor
    virtual ~TypecheckException() { }
    virtual std::string toString() const {
      return "Type Checking error: " + d_msg;
    }
  }; // end of class TypecheckException
} // end of namespace CVC3

#endif
