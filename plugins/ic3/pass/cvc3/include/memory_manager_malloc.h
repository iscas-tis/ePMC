/*****************************************************************************/
/*!
 * \file memory_manager_malloc.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Tue Apr 19 14:30:36 2005
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
 * Class MemoryManagerMalloc: default implementation of MemoryManager
 * using malloc().
 * 
 * Typical use of this class is to create 
 * MemoryManager* mm = new MemoryManager(sizeof(YourClass)); 
 * where YourClass has operators new and delete redefined:
 * void* YourClass::operator new(size_t, MemoryManager* mm)
 * { return mm->newData(); }
 * void YourClass::delete(void*) { } // do not deallocate memory here
 * Then, create objects with obj = new(mm) YourClass(), and destroy them with
 * delete obj; mm->deleteData(obj);
 */
/*****************************************************************************/

#ifndef _cvc3__memory_manager_malloc_h
#define _cvc3__memory_manager_malloc_h

#include "memory_manager.h"

namespace CVC3 {

class MemoryManagerMalloc: public MemoryManager {
 public:
  // Constructor
  MemoryManagerMalloc() { }
  // Destructor
  ~MemoryManagerMalloc() { }

  void* newData(size_t size) {
    return malloc(size);
  }

  void deleteData(void* d) {
    free(d);
  }
}; // end of class MemoryManager

}

#endif
