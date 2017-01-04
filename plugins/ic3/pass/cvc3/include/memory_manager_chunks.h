/*****************************************************************************/
/*!
 * \file memory_manager_chunks.h
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
 * Class MemoryManager: allocates/deallocates memory for objects of a
 * fixed size (the size is a parameter to the constructor).  The
 * actual memory is allocated in big chunks, which (at the moment) are
 * never released back.  However, the deallocated blocks are later reused.
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

#ifndef _cvc3__memory_manager_chunks_h
#define _cvc3__memory_manager_chunks_h

#include <vector>
#include "memory_manager.h"

namespace CVC3 {

class MemoryManagerChunks: public MemoryManager {
 private:
  unsigned d_dataSize; // #bytes in each data element
  unsigned d_chunkSize; // number of data elements
  unsigned d_chunkSizeBytes; // #bytes in each chunk
  std::vector<char*> d_freeList;
  std::vector<char*> d_chunkList; // Pointers to the beginning of each chunk
  // Pointer to the next free block of memory in the current chunk
  char* d_nextFree;
  // End of current chunk (1 byte off the end)
  char* d_endChunk;

  // Private methods
  void newChunk() { // Allocate new chunk
    d_nextFree = (char*)malloc(d_chunkSizeBytes);
    FatalAssert(d_nextFree != NULL, "Out of memory");
    d_endChunk = d_nextFree + d_chunkSizeBytes;
    d_chunkList.push_back(d_nextFree);
  }

 public:
  // Constructor
  MemoryManagerChunks(unsigned dataSize, unsigned chunkSize = 1024)
    : d_dataSize(dataSize), d_chunkSize(chunkSize),
      d_chunkSizeBytes(dataSize*chunkSize),
      d_nextFree(NULL), d_endChunk(NULL) { }
  // Destructor
  ~MemoryManagerChunks() {
    while(d_chunkList.size() > 0) {
      free(d_chunkList.back());
      d_chunkList.pop_back();
    }
  }

  void* newData(size_t size) {
    DebugAssert(size == d_dataSize,
		"MemoryManager::newData: the data size doesn't match");
    void* res;
    // Check the free list first
    if(d_freeList.size() > 0) {
      res = (void*)d_freeList.back();
      d_freeList.pop_back();
      return res;
    }
    if(d_nextFree == NULL || d_nextFree == d_endChunk)
      newChunk();
    res = (void*)d_nextFree;
    d_nextFree += d_dataSize;
    return res;
  }

  void deleteData(void* d) {
    d_freeList.push_back((char*)d);
  }
}; // end of class MemoryManager

}

#endif
