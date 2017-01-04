/*****************************************************************************/
/*!
 * \file pretty_printer.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Mon Jun 16 12:31:08 2003
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
 * Defines an abstract class PrettyPrinter which connects the
 * theory-specific pretty-printers with ExprManager.
 * 
 */
/*****************************************************************************/

#ifndef _cvc3__pretty_printer_h_
#define _cvc3__pretty_printer_h_

namespace CVC3 {

  class Expr;
  class ExprStream;
  //! Abstract API to a pretty-printer for Expr
  /*! \ingroup PrettyPrinting */
  class PrettyPrinter {
  public:
    //! Default constructor
    PrettyPrinter() { }
    //! Virtual destructor
    virtual ~PrettyPrinter() { }
    //! The pretty-printer which subclasses must implement
    virtual ExprStream& print(ExprStream& os, const Expr& e) = 0;
  };

}

#endif
