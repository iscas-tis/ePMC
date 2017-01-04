/*****************************************************************************/
/*!
 * \file smtlib_exception.h
 * \brief An exception to be thrown by the smtlib translator.
 * 
 * Author: Clark Barrett
 * 
 * Created: Thu Feb 24 18:22:18 2005
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

#ifndef _cvc3__smtlib_exception_h_
#define _cvc3__smtlib_exception_h_

#include <string>
#include <iostream>
#include "exception.h"

namespace CVC3 {

  class SmtlibException: public Exception {
  public:
    // Constructors
    SmtlibException() { }
    SmtlibException(const std::string& msg): Exception(msg) { }
    SmtlibException(const char* msg): Exception(msg) { }
    // Destructor
    virtual ~SmtlibException() { }
    virtual std::string toString() const {
      return "SMTLIB translation error: " + d_msg;
    }
  }; // end of class SmtlibException
} // end of namespace CVC3 

#endif
