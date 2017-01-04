/*****************************************************************************/
/*!
 * \file expr_hash.h
 * \brief Definition of the API to expression package.  See class Expr for details.
 * 
 * Author: Clark Barrett
 * 
 * Created: Tue Nov 26 00:27:40 2002
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
 * Define std::hash<Expr> and std::hash<std::string> for hash_map and
 * hash_set over Expr class.
 */
/*****************************************************************************/

#ifndef _cvc3__expr_h_
#include "expr.h"
#endif

#ifndef _cvc3__include__expr_hash_h_
#define _cvc3__include__expr_hash_h_

#include "hash_fun.h"
namespace Hash
{

template<> struct hash<CVC3::Expr>
{
  size_t operator()(const CVC3::Expr& e) const { return e.hash(); }
};

template<> class hash<std::string> {
 private:  
  hash<const char*> h;
 public:
  size_t operator()(const std::string& s) const {
    return h(s.c_str());
  }
};

}

#endif
