
// $Id: unique_table.h 612 2015-04-15 21:33:17Z junaidbabar $

/*
    Meddly: Multi-terminal and Edge-valued Decision Diagram LibrarY.
    Copyright (C) 2009, Iowa State University Research Foundation, Inc.

    This library is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this library.  If not, see <http://www.gnu.org/licenses/>.
*/


#ifndef UNIQUE_TABLE_H
#define UNIQUE_TABLE_H

#include "defines.h"

namespace MEDDLY {
  class unique_table;
};

/** Unique table for discovering duplicate nodes.

    This is now a stand-alone, non-template class
    designed specifically for expert_forests.
*/
class MEDDLY::unique_table {
  public:
    unique_table(expert_forest *ef);
    ~unique_table();

    inline unsigned getSize() const         { return size; }
    inline unsigned getNumEntries() const   { return num_entries; }
    inline unsigned getMemUsed() const      { return size * sizeof(node_handle); }

    void reportStats(FILE* s, const char* pad, unsigned flags) const;

    /// For debugging
    void show(FILE *s) const;

    /** If table contains key, move it to the front of the list.
        Otherwise, do nothing.
        Returns index of the item if found, 0 otherwise.

        Class T must have the following methods:
          unsigned hash():    return the hash value for this item.
          bool equals(int p): return true iff this item equals node p.
     */
    template <class T>
    inline node_handle find(const T &key) {
      unsigned h = key.hash() % size;
      MEDDLY_DCASSERT(h < size);
      node_handle prev = 0;
      for (node_handle ptr = table[h]; ptr; ptr = parent->getNext(ptr)) {
        if (parent->areDuplicates(ptr, key)) { // key.equals(ptr)) {
          // MATCH
          if (ptr != table[h]) {
            // Move to front
            MEDDLY_DCASSERT(prev);
            parent->setNext(prev, parent->getNext(ptr));
            parent->setNext(ptr, table[h]);
            table[h] = ptr;
          }
          MEDDLY_DCASSERT(table[h] == ptr);
          return ptr;
        } // if
        prev = ptr;
      } // for ptr
      // no match
      return 0;
    }

    /** Add to the front of the list.
        Used when we KNOW that the item is not in the unique table already.
    */
    inline void add(unsigned h, node_handle item) {
      num_entries++;
      if (num_entries > next_expand) expand();
      MEDDLY_DCASSERT(item>0);
      h %= size;
      parent->setNext(item, table[h]);
      table[h] = item;
    }

    /** If table contains key, remove it and return it.
      I.e., the exact key.
      Otherwise, return 0.
    */
    inline node_handle remove(unsigned h, node_handle key) {
      h %= size;
      MEDDLY_DCASSERT(h < size);
      node_handle prev = 0;
      node_handle ptr = table[h];
      for ( ; ptr; ptr = parent->getNext(ptr)) {
        if (key == ptr) {
          if (ptr != table[h]) {
            // remove from middle
            parent->setNext(prev, parent->getNext(ptr));
          } else {
            // remove from head
            table[h] = parent->getNext(ptr);
          }
          num_entries--;
          if (num_entries < next_shrink) shrink();
          return ptr;
        }
        prev = ptr;
      }
      return 0;
    }

  private:  // helper methods
    /// Empty the hash table into a list; returns the list.
    node_handle convertToList();
    /// A series of inserts; doesn't check for duplicates or expand.
    void buildFromList(node_handle front);
    /// Expand the hash table (if possible)
    void expand();
    /// Shrink the hash table
    void shrink();

  private:
    expert_forest* parent;
    unsigned size;
    unsigned num_entries;
    unsigned next_expand;
    unsigned next_shrink;
    node_handle* table;

    static const unsigned maxSize = 1073741824;
    static const unsigned minSize = 8;
};

#endif
