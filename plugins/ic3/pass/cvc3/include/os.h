/*****************************************************************************/
/*!
 * \file os.h
 * \brief Abstraction over different operating systems.
 *
 * Author: Alexander Fuchs
 *
 * Created: Fri Feb 16 12:00:00 2007
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

#ifndef _cvc3__windows_h_
#define _cvc3__windows_h_


// define if cvc3lib built as a dll, comment if cvc3lib is linked statically
// #define CVC_DLL_LINKAGE
// library export of C++ symbols for C++ windows interface

// for dynamic binding dll export needed
#ifdef CVC_DLL_LINKAGE
 #ifdef CVC_DLL_EXPORT
 #define CVC_DLL __declspec(dllexport)
 #elif CVC_DLL_IMPORT
 #define CVC_DLL __declspec(dllimport)
 #else
 #define CVC_DLL
 #endif

// for static binding dll export not needed
#else
#define CVC_DLL

#endif

#ifndef _LINUX_WINDOWS_CROSS_COMPILE
/// MS C++ specific settings
#ifdef _MSC_VER

// CLR specific settings
//  #ifdef _MANAGED

// if lex files are created with cygwin they require isatty,
// which in MS VS C++ requires using _isatty
#include <io.h>
#define isatty _isatty

// C99 stdint data types
typedef signed __int8        int8_t;
typedef signed __int16       int16_t;
typedef signed __int32       int32_t;
typedef signed __int64       int64_t;
typedef unsigned __int8      uint8_t;
typedef unsigned __int16     uint16_t;
typedef unsigned __int32     uint32_t;
typedef unsigned __int64     uint64_t;

// unix specific settings
#else

// C99 data types
// (should) provide:
// int8_t, int16_t, int32_t, int64_t, uint8_t, uint16_t, uint32_t, uint64_tm
// intptr_t, uintptr_t
#include <stdint.h>

#endif

#else
// Cross-compile include the same as for unix
#include <stdint.h>
#endif



#endif
