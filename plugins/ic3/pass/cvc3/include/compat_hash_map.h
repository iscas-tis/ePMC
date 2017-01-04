/*****************************************************************************/
/*!
 * \file compat_hash_map.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Jan 31 02:23:26 GMT 2003
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
 * Compatibility header file for STL extension "hash_map".  Any other
 * source file that needs to use hash_map should include this instead.
 * 
 * If hash_map is not defined in namespace std, we bring it in there.
 * It turns out that different versions of gcc use different
 * namespaces for STL extensions (std, __gnu_cxx, and God knows
 * what'll be next).
 * 
 * This header assumes that only one of HAVE_*_HASH_MAP symbols is
 * defined.
 * 
 */
/*****************************************************************************/
#ifndef _cvc3__include__compat_hash_map_h_
#define _cvc3__include__compat_hash_map_h_

#include "hash_map.h"
namespace std {
  using namespace Hash;
}

#endif
