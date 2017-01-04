/*****************************************************************************/
/*!
 * \file compat_hash_set.h
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
 * Compatibility header file for STL extension "hash_set".  Any other
 * source file that needs to use hash_set should include this instead.
 * 
 * If hash_set and hash are not defined in namespace std, we bring
 * them in there.  It turns out that different versions of gcc use
 * different namespaces for STL extensions (std, __gnu_cxx, and God
 * knows what'll be next).
 * 
 * This header assumes that only one of HAVE_*_HASH_SET symbols is
 * defined.
 * 
 * 
 */
/*****************************************************************************/
#ifndef _core_utilities_compat_hash_set_h_
#define _core_utilities_compat_hash_set_h_

#include "hash_set.h"
namespace std {
  using namespace Hash;
}

#endif
