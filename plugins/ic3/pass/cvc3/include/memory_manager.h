/*****************************************************************************/
/*!
 * \file memory_manager.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Thu Apr  3 16:47:14 2003
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
 * Class MemoryManager: allocates/deallocates memory for objects of a
 * requested size.  Some instanced of this class may be specialized to
 * a specific object size, and the actual memory may be allocated in
 * big chunks, for efficiency.
 *
 * Typical use of this class is to create 
 * MemoryManager* mm = new MemoryManagerChunks(sizeof(YourClass)); 
 * where YourClass has operators new and delete redefined:
 * void* YourClass::operator new(size_t size, MemoryManager* mm)
 * { return mm->newData(size); }
 * void YourClass::delete(void*) { } // do not deallocate memory here
 * Then, create objects with obj = new(mm) YourClass(), and destroy them with
 * delete obj; mm->deleteData(obj);
 */
/*****************************************************************************/

#ifndef _cvc3__memory_manager_h
#define _cvc3__memory_manager_h

namespace CVC3 {

class MemoryManager {
 public:
  // Destructor
  virtual ~MemoryManager() { }

  virtual void* newData(size_t size) = 0;

  virtual void deleteData(void* d) = 0;
}; // end of class MemoryManager

}

#endif
