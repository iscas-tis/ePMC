/*****************************************************************************/
/*!
 *\file memory_manager_context.h
 *\brief Stack-based memory manager
 *
 * Author: Clark Barrett
 *
 * Created: Thu Aug  3 21:39:07 2006
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 *
 * <hr>
 */
/*****************************************************************************/

#ifndef _cvc3__include__memory_manager_context_h
#define _cvc3__include__memory_manager_context_h

#include <cstdlib>
#include <vector>
#include "memory_manager.h"

namespace CVC3 {

  const unsigned chunkSizeBytes = 16384; // #bytes in each chunk

/*****************************************************************************/
/*!
 *\class ContextMemoryManager
 *\brief ContextMemoryManager
 *
 * Author: Clark Barrett
 *
 * Created: Thu Aug  3 16:41:35 2006
 *
 * Stack-based memory manager
 */
/*****************************************************************************/
class ContextMemoryManager :public MemoryManager {
  static std::vector<char*> s_freePages;
  std::vector<char*> d_chunkList; // Pointers to the beginning of each chunk

  // Pointers to the next free block of memory in the current chunk
  char* d_nextFree;
  // Pointer to end of current chunk (1 byte off the end)
  char* d_endChunk;
  // Index into chunk vector
  unsigned d_indexChunkList;

  // Stack of pointers to the next free block of memory in the current chunk
  std::vector<char*> d_nextFreeStack;
  // Stack of pointers to end of current chunk (1 byte off the end)
  std::vector<char*> d_endChunkStack;
  // Stack of indices into chunk vector
  std::vector<unsigned> d_indexChunkListStack;

  // Private methods
  void newChunk() { // Allocate new chunk
    DebugAssert(d_chunkList.size() > 0, "expected unempty list");
    ++d_indexChunkList;
    DebugAssert(d_chunkList.size() == d_indexChunkList, "invariant violated");
    if (s_freePages.empty()) {
      d_chunkList.push_back((char*)malloc(chunkSizeBytes));
    }
    else {
      d_chunkList.push_back(s_freePages.back());
      s_freePages.pop_back();
    }
    d_nextFree = d_chunkList.back();
    FatalAssert(d_nextFree != NULL, "Out of memory");
    d_endChunk = d_nextFree + chunkSizeBytes;
  }

 public:
  // Constructor
  ContextMemoryManager()
    : d_indexChunkList(0)
  {
    if (s_freePages.empty()) {
      d_chunkList.push_back((char*)malloc(chunkSizeBytes));
    }
    else {
      d_chunkList.push_back(s_freePages.back());
      s_freePages.pop_back();
    }      
    d_nextFree = d_chunkList.back();
    FatalAssert(d_nextFree != NULL, "Out of memory");
    d_endChunk = d_nextFree + chunkSizeBytes;
  }

  // Destructor
  ~ContextMemoryManager() {
    while(!d_chunkList.empty()) {
      s_freePages.push_back(d_chunkList.back());
      d_chunkList.pop_back();
    }
  }

  void* newData(size_t size) {
    void* res = (void*)d_nextFree;
    d_nextFree += size;
    if (d_nextFree > d_endChunk) {
      newChunk();
      res = (void*)d_nextFree;
      d_nextFree += size;
      DebugAssert(d_nextFree <= d_endChunk, "chunk not big enough");
    }
    return res;
  }

  void deleteData(void* d) { }

  void push() {
    d_nextFreeStack.push_back(d_nextFree);
    d_endChunkStack.push_back(d_endChunk);
    d_indexChunkListStack.push_back(d_indexChunkList);
  }

  void pop() {
    d_nextFree = d_nextFreeStack.back();
    d_nextFreeStack.pop_back();
    d_endChunk = d_endChunkStack.back();
    d_endChunkStack.pop_back();
    while (d_indexChunkList > d_indexChunkListStack.back()) {
      s_freePages.push_back(d_chunkList.back());
      d_chunkList.pop_back();
      --d_indexChunkList;
    }
    d_indexChunkListStack.pop_back();
  }

  static void garbageCollect(void) {
    while (!s_freePages.empty()) {
      free(s_freePages.back());
      s_freePages.pop_back();
    }
  }

  unsigned getMemory(int verbosity) {
    unsigned long memSelf = sizeof(ContextMemoryManager);
    unsigned long mem = 0;

    mem += MemoryTracker::getVec(verbosity - 1, d_chunkList);
    mem += MemoryTracker::getVec(verbosity - 1, d_nextFreeStack);
    mem += MemoryTracker::getVec(verbosity - 1, d_endChunkStack);
    mem += MemoryTracker::getVec(verbosity - 1, d_indexChunkListStack);

    mem += d_chunkList.size() * chunkSizeBytes;

    MemoryTracker::print("ContextMemoryManager", verbosity, memSelf, mem);

    return mem + memSelf;
  }

  static unsigned getStaticMemory(int verbosity) {
    unsigned mem = 0;
    mem += MemoryTracker::getVec(verbosity - 1, s_freePages);
    mem += s_freePages.size() * chunkSizeBytes;
    MemoryTracker::print("ContextMemoryManager Static", verbosity, 0, mem);
    return mem;
  }

}; // end of class ContextMemoryManager

}

#endif
