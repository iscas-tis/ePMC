/**************************** CPPHeaderFile ***************************

* FileName [Error.h]

* PackageName [util]

* Synopsis [Exceptions that are invoked on error conditions.]

* Description []

* SeeAlso []

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2006 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#ifndef ERROR_H
#define ERROR_H

#include <string>
#include <iostream>

namespace util {

/*! \class Error Error messages
    \ingroup util
*/
class Error {
 public:
    Error()  {}
    Error( const std::string& s) : msg( s ) {}
    inline std::ostream& message(std::ostream& o) const {
        return o << msg << std::endl;
    }
    const std::string& toString() const { return msg; }
private:
  const std::string msg;
};

inline std::ostream& operator<<(std::ostream &o, const class Error& e) { return e.message( o ); }

/*! \class ParseError parse error
    \ingroup util
*/
struct ParseError : public Error {
    ParseError( const std::string& s ) : Error( "parse error: " + s) {}
};

/*! \class InternalError internal error
    \ingroup util
*/
struct InternalError: public Error {
    InternalError( const std::string& s ) : Error( "internal error: " + s ) {}
};

/*! \class RuntimeError runtime error
    \ingroup util
*/
struct RuntimeError: public Error {
   RuntimeError( const std::string& s ) : Error(s) {}
   RuntimeError() : Error( "runtime error: ") {}
};

/*! \class TypeError failed type check
    \ingroup util
*/
struct TypeError : public Error {
  TypeError( const std::string& s ) : Error(s ) {}
  TypeError() : Error("type error: ") {}
};

/*! \class Warning Warn
    \ingroup util
*/
struct Warning {
    const std::string msg;
    Warning( const std::string &msg ) : msg(msg) {}
};

} //end of namespace util

#endif
