// $Id: meddly_expert.hh 618 2015-04-21 17:54:39Z junaidbabar $

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

/*! \file meddly_expert.hh

 Implementation details for interface in meddly_expert.h.
 */

#ifndef MEDDLY_EXPERT_HH
#define MEDDLY_EXPERT_HH

inline MEDDLY::expert_variable*
MEDDLY::expert_domain::getExpertVar(int lev) const
{
  return (expert_variable*) vars[lev];
}

inline const MEDDLY::expert_variable*
MEDDLY::expert_domain::readExpertVar(int lev) const
{
  return (expert_variable*) vars[lev];
}

inline void
MEDDLY::expert_domain::createVariable(int below, int &vh)
{
  createVariable(below);
  vh = below + 1;
}
inline void
MEDDLY::expert_domain::destroyVariable(int vh)
{
  removeVariableAtLevel(vh);
}

inline int
MEDDLY::expert_domain::getVariableHeight(int vh) const
{
  return vh;
}

inline int
MEDDLY::expert_domain::getVariableWithHeight(int ht) const
{
  return ht;
}

inline void
MEDDLY::expert_domain::enlargeVariableBound(int vh, bool prime, int b)
{
  getExpertVar(vh)->enlargeBound(prime, b);
}

inline void
MEDDLY::expert_domain::shrinkVariableBound(int vh, int b, bool force)
{
  getExpertVar(vh)->shrinkBound(b, force);
}

// ****************************************************************************

inline const void*
MEDDLY::node_reader::HHptr() const
{
  return extra_hashed;
}

inline int
MEDDLY::node_reader::HHbytes() const
{
  return ext_size;
}

inline MEDDLY::node_handle
MEDDLY::node_reader::d(int n) const
{
  MEDDLY_DCASSERT(down);
  MEDDLY_CHECK_RANGE(0, n, (is_full ? size : nnzs));
  return down[n];
}

inline int
MEDDLY::node_reader::i(int n) const
{
  MEDDLY_DCASSERT(index);
  MEDDLY_DCASSERT(!is_full);
  MEDDLY_CHECK_RANGE(0, n, nnzs);
  return index[n];
}

inline const void*
MEDDLY::node_reader::eptr(int i) const
{
  MEDDLY_DCASSERT(edge);
  MEDDLY_CHECK_RANGE(0, i, (is_full ? size : nnzs));
  return ((char*) edge) + i * edge_bytes;
}

inline void
MEDDLY::node_reader::getEdge(int n, int &val) const
{
  MEDDLY_DCASSERT(sizeof(int) == edge_bytes);
  MEDDLY::expert_forest::int_EVencoder::readValue(eptr(n), val);
}

inline void
MEDDLY::node_reader::getEdge(int n, float &val) const
{
  MEDDLY_DCASSERT(sizeof(float) == edge_bytes);
  MEDDLY::expert_forest::float_EVencoder::readValue(eptr(n), val);
}

inline int
MEDDLY::node_reader::ei(int i) const
{
  int ev;
  getEdge(i, ev);
  return ev;
}

inline float
MEDDLY::node_reader::ef(int i) const
{
  float ev;
  getEdge(i, ev);
  return ev;
}

inline int
MEDDLY::node_reader::getLevel() const
{
  return level;
}

inline int
MEDDLY::node_reader::getSize() const
{
  MEDDLY_DCASSERT(is_full);
  return size;
}

inline int
MEDDLY::node_reader::getNNZs() const
{
  MEDDLY_DCASSERT(!is_full);
  return nnzs;
}

inline bool
MEDDLY::node_reader::isSparse() const
{
  return !is_full;
}

inline bool
MEDDLY::node_reader::isFull() const
{
  return is_full;
}

inline bool
MEDDLY::node_reader::hasEdges() const
{
  return edge_bytes;
}
inline int
MEDDLY::node_reader::edgeBytes() const
{
  return edge_bytes;
}

inline unsigned
MEDDLY::node_reader::hash() const
{
#ifdef DEVELOPMENT_CODE
  MEDDLY_DCASSERT(has_hash);
#endif
  return h;
}
inline void
MEDDLY::node_reader::setHash(unsigned H)
{
#ifdef DEVELOPMENT_CODE
  MEDDLY_DCASSERT(!has_hash);
  has_hash = true;
#endif
  h = H;
}

inline MEDDLY::node_reader*
MEDDLY::node_reader::useReader()
{
  node_reader* nr;
  if (freeList) {
    nr = freeList;
    freeList = nr->next;
  }
  else {
    nr = new node_reader;
  }
#ifdef DEVELOPMENT_CODE
  nr->has_hash = false;
#endif
  return nr;
}

inline void
MEDDLY::node_reader::recycle(MEDDLY::node_reader* r)
{
  if (r) {
    r->next = freeList;
    freeList = r;
  }
}

inline void
MEDDLY::node_reader::freeRecycled()
{
  while (freeList) {
    MEDDLY::node_reader* n = freeList->next;
    delete freeList;
    freeList = n;
  }
}

// ****************************************************************************

inline void*
MEDDLY::node_builder::raw_hh() const
{
  MEDDLY_DCASSERT(extra_hashed);
  return extra_hashed;
}

inline void*
MEDDLY::node_builder::raw_uh() const
{
  MEDDLY_DCASSERT(extra_unhashed);
  return extra_unhashed;
}

inline MEDDLY::node_handle&
MEDDLY::node_builder::raw_d(int i) const
{
  MEDDLY_DCASSERT(down);
  MEDDLY_CHECK_RANGE(0, i, size);
  return down[i];
}

inline int&
MEDDLY::node_builder::raw_i(int i) const
{
  MEDDLY_DCASSERT(indexes);
  MEDDLY_DCASSERT(is_sparse);
  MEDDLY_CHECK_RANGE(0, i, size);
  return indexes[i];
}

inline bool
MEDDLY::node_builder::hasEdges() const
{
  return edge_bytes > 0;
}

inline int
MEDDLY::node_builder::edgeBytes() const
{
  return edge_bytes;
}

inline void
MEDDLY::node_builder::resize(int s)
{
  is_sparse = false;
#ifdef DEVELOPMENT_CODE
  has_hash = false;
#endif
  size = s;
  if (size > alloc)
    enlarge();
}

inline void
MEDDLY::node_builder::resparse(int s)
{
  is_sparse = true;
#ifdef DEVELOPMENT_CODE
  has_hash = false;
#endif
  size = s;
  if (size > alloc)
    enlarge();
}

inline void
MEDDLY::node_builder::shrinkSparse(int ns)
{
  MEDDLY_DCASSERT(is_sparse);
  MEDDLY_CHECK_RANGE(0, ns, size + 1);
  size = ns;
}

inline bool
MEDDLY::node_builder::isSparse() const
{
  return is_sparse;
}

inline bool
MEDDLY::node_builder::isFull() const
{
  return !is_sparse;
}

inline int
MEDDLY::node_builder::rawSize() const
{
  return size;
}

inline int
MEDDLY::node_builder::getSize() const
{
  MEDDLY_DCASSERT(!is_sparse);
  return size;
}

inline int
MEDDLY::node_builder::getNNZs() const
{
  MEDDLY_DCASSERT(is_sparse);
  return size;
}

inline int
MEDDLY::node_builder::getLevel() const
{
  return level;
}

inline MEDDLY::node_handle&
MEDDLY::node_builder::d(int i)
{
  return raw_d(i);
}
inline MEDDLY::node_handle
MEDDLY::node_builder::d(int i) const
{
  return raw_d(i);
}
inline int&
MEDDLY::node_builder::i(int i)
{
  return raw_i(i);
}
inline int
MEDDLY::node_builder::i(int i) const
{
  return raw_i(i);
}

inline int
MEDDLY::node_builder::ei(int i) const
{
  int ev;
  getEdge(i, ev);
  return ev;
}

inline float
MEDDLY::node_builder::ef(int i) const
{
  float ev;
  getEdge(i, ev);
  return ev;
}

inline void*
MEDDLY::node_builder::eptr(int i)
{
  MEDDLY_DCASSERT(edge);
  MEDDLY_CHECK_RANGE(0, i, size);
  return ((char*) edge) + i * edge_bytes;
}

inline const void*
MEDDLY::node_builder::eptr(int i) const
{
  MEDDLY_DCASSERT(edge);
  MEDDLY_CHECK_RANGE(0, i, size);
  return ((char*) edge) + i * edge_bytes;
}

inline void*
MEDDLY::node_builder::UHptr()
{
  return extra_unhashed;
}

inline const void*
MEDDLY::node_builder::HHptr() const
{
  return extra_hashed;
}

inline void*
MEDDLY::node_builder::HHptr()
{
  return extra_hashed;
}

inline int
MEDDLY::node_builder::HHbytes() const
{
  return hhbytes;
}

inline unsigned
MEDDLY::node_builder::hash() const
{
#ifdef DEVELOPMENT_CODE
  MEDDLY_DCASSERT(has_hash);
#endif
  return h;
}

inline void
MEDDLY::node_builder::setUH(const void* x)
{
  memcpy(raw_uh(), x, parent->unhashedHeaderBytes());
}

inline void
MEDDLY::node_builder::getUH(void* x) const
{
  memcpy(x, raw_uh(), parent->unhashedHeaderBytes());
}

inline void
MEDDLY::node_builder::setHH(const void* x)
{
  memcpy(raw_hh(), x, parent->hashedHeaderBytes());
}

inline void
MEDDLY::node_builder::getHH(void* x) const
{
  memcpy(x, raw_hh(), parent->hashedHeaderBytes());
}

inline void
MEDDLY::node_builder::getEdge(int n, int &val) const
{
  MEDDLY_DCASSERT(sizeof(int) == edge_bytes);
  MEDDLY::expert_forest::int_EVencoder::readValue(eptr(n), val);
}

inline void
MEDDLY::node_builder::getEdge(int n, float &val) const
{
  MEDDLY_DCASSERT(sizeof(float) == edge_bytes);
  MEDDLY::expert_forest::float_EVencoder::readValue(eptr(n), val);
}

inline void
MEDDLY::node_builder::setEdge(int n, int ev)
{
  MEDDLY_DCASSERT(sizeof(int) == edge_bytes);
  MEDDLY::expert_forest::int_EVencoder::writeValue(eptr(n), ev);
}

inline void
MEDDLY::node_builder::setEdge(int n, float ev)
{
  MEDDLY_DCASSERT(sizeof(float) == edge_bytes);
  MEDDLY::expert_forest::float_EVencoder::writeValue(eptr(n), ev);
}

// ****************************************************************************

inline bool
MEDDLY::node_header::isActive() const
{
  return offset > 0;
}
inline bool
MEDDLY::node_header::isZombie() const
{
  return cache_count < 0;
}
inline bool
MEDDLY::node_header::isDeleted() const
{
  return 0 == level;
}
inline void
MEDDLY::node_header::setDeleted()
{
  level = 0;
}
inline void
MEDDLY::node_header::setNotDeleted()
{
  level = 1;
}
inline int
MEDDLY::node_header::getNextDeleted() const
{
  return -offset;
}
inline void
MEDDLY::node_header::setNextDeleted(int n)
{
  offset = -n;
}
inline void
MEDDLY::node_header::makeZombie()
{
  MEDDLY_DCASSERT(cache_count > 0);
  cache_count *= -1;
  offset = 0;
}

// ****************************************************************************

inline MEDDLY::node_handle
MEDDLY::node_storage::getCountOf(node_address addr) const
{
  MEDDLY_DCASSERT(counts);
  MEDDLY_DCASSERT(addr > 0);
  return counts[addr];
}

inline void
MEDDLY::node_storage::setCountOf(node_address addr, MEDDLY::node_handle c)
{
  MEDDLY_DCASSERT(counts);
  MEDDLY_DCASSERT(addr > 0);
  counts[addr] = c;
}

inline MEDDLY::node_handle
MEDDLY::node_storage::incCountOf(node_address addr)
{
  MEDDLY_DCASSERT(counts);
  MEDDLY_DCASSERT(addr > 0);
  return ++counts[addr];
}
;

inline MEDDLY::node_handle
MEDDLY::node_storage::decCountOf(node_address addr)
{
  MEDDLY_DCASSERT(counts);
  MEDDLY_DCASSERT(addr > 0);
  return --counts[addr];
}

inline MEDDLY::node_handle
MEDDLY::node_storage::getNextOf(node_address addr) const
{
  MEDDLY_DCASSERT(nexts);
  MEDDLY_DCASSERT(addr > 0);
  return nexts[addr];
}

inline void
MEDDLY::node_storage::setNextOf(node_address addr, MEDDLY::node_handle n)
{
  MEDDLY_DCASSERT(nexts);
  MEDDLY_DCASSERT(addr > 0);
  nexts[addr] = n;
}

inline void
MEDDLY::node_storage::resize_header(MEDDLY::node_reader& nr, int extra_slots)
{
  nr.resize_header(extra_slots);
}

inline void*
MEDDLY::node_storage::extra_hashed(MEDDLY::node_reader& nr)
{
  return nr.extra_hashed;
}

inline MEDDLY::node_handle*
MEDDLY::node_storage::down_of(MEDDLY::node_reader& nr)
{
  return nr.down;
}

inline int*
MEDDLY::node_storage::index_of(MEDDLY::node_reader& nr)
{
  return nr.index;
}

inline char*
MEDDLY::node_storage::edge_of(MEDDLY::node_reader& nr)
{
  return (char*) nr.edge;
}

inline int&
MEDDLY::node_storage::nnzs_of(MEDDLY::node_reader& nr)
{
  return nr.nnzs;
}

inline const MEDDLY::expert_forest*
MEDDLY::node_storage::getParent() const
{
  MEDDLY_DCASSERT(parent);
  return parent;
}

inline MEDDLY::expert_forest*
MEDDLY::node_storage::getParent()
{
  MEDDLY_DCASSERT(parent);
  return parent;
}

inline void
MEDDLY::node_storage::incMemUsed(long delta)
{
  if (stats)
    stats->incMemUsed(delta);
}

inline void
MEDDLY::node_storage::decMemUsed(long delta)
{
  if (stats)
    stats->decMemUsed(delta);
}

inline void
MEDDLY::node_storage::incMemAlloc(long delta)
{
  if (stats)
    stats->incMemAlloc(delta);
}

inline void
MEDDLY::node_storage::decMemAlloc(long delta)
{
  if (stats)
    stats->decMemAlloc(delta);
}

inline void
MEDDLY::node_storage::incCompactions()
{
  if (stats)
    stats->num_compactions++;
}

inline void
MEDDLY::node_storage::updateCountArray(MEDDLY::node_handle* cptr)
{
  counts = cptr;
}

inline void
MEDDLY::node_storage::updateNextArray(MEDDLY::node_handle* nptr)
{
  nexts = nptr;
}

inline void
MEDDLY::node_storage::moveNodeOffset(MEDDLY::node_handle node, node_address old_addr,
    node_address new_addr)
{
  MEDDLY_DCASSERT(parent);
  parent->moveNodeOffset(node, old_addr, new_addr);
}

// ****************************************************************************

inline MEDDLY::node_handle
MEDDLY::expert_forest::bool_Tencoder::value2handle(bool v)
{
  return v ? -1 : 0;
}
inline bool
MEDDLY::expert_forest::bool_Tencoder::handle2value(MEDDLY::node_handle h)
{
  if (-1 == h)
    return true;
  if (0 == h)
    return false;
  throw error(error::MISCELLANEOUS);
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::int_Tencoder::value2handle(int v)
{
  MEDDLY_DCASSERT(4 == sizeof(MEDDLY::node_handle));
  if (v < -1073741824 || v > 1073741823) {
    // Can't fit in 31 bits (signed)
    throw error(error::OVERFLOW);
  }
  if (v)
    v |= 0x80000000; // sets the sign bit
  return v;
}

inline int
MEDDLY::expert_forest::int_Tencoder::handle2value(MEDDLY::node_handle h)
{
  // << 1 kills the sign bit
  // >> 1 puts us back, and extends the (new) sign bit
  return (h << 1) >> 1;
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::float_Tencoder::value2handle(float v)
{
  MEDDLY_DCASSERT(4 == sizeof(MEDDLY::node_handle));
  MEDDLY_DCASSERT(sizeof(float) <= sizeof(MEDDLY::node_handle));
  if (0.0 == v)
    return 0;
  intfloat x;
  x.real = v;
  // strip lsb in fraction, and add sign bit
  return (x.integer >> 1) | 0x80000000;
}

inline float
MEDDLY::expert_forest::float_Tencoder::handle2value(MEDDLY::node_handle h)
{
  MEDDLY_DCASSERT(4 == sizeof(MEDDLY::node_handle));
  MEDDLY_DCASSERT(sizeof(float) <= sizeof(MEDDLY::node_handle));
  if (0 == h)
    return 0.0;
  intfloat x;
  x.integer = (h << 1); // remove sign bit
  return x.real;
}

inline size_t
MEDDLY::expert_forest::int_EVencoder::edgeBytes()
{
  return sizeof(int);
}
inline void
MEDDLY::expert_forest::int_EVencoder::writeValue(void* ptr, int val)
{
  memcpy(ptr, &val, sizeof(int));
}
inline void
MEDDLY::expert_forest::int_EVencoder::readValue(const void* ptr, int &val)
{
  memcpy(&val, ptr, sizeof(int));
}

inline size_t
MEDDLY::expert_forest::float_EVencoder::edgeBytes()
{
  return sizeof(float);
}
inline void
MEDDLY::expert_forest::float_EVencoder::writeValue(void* ptr, float val)
{
  memcpy(ptr, &val, sizeof(float));
}
inline void
MEDDLY::expert_forest::float_EVencoder::readValue(const void* ptr, float &val)
{
  memcpy(&val, ptr, sizeof(float));
}

template<typename T>
  inline MEDDLY::node_handle
  MEDDLY::expert_forest::handleForValue(T v) const
  {
    switch (getRangeType()) {
      case BOOLEAN:
        return bool_Tencoder::value2handle(v);
      case INTEGER:
        return int_Tencoder::value2handle(v);
      case REAL:
        return float_Tencoder::value2handle(v);
      default:
        throw error(error::MISCELLANEOUS);
    }
  }

template<typename T>
  inline void
  MEDDLY::expert_forest::getValueFromHandle(MEDDLY::node_handle n, T& v) const
  {
    MEDDLY_DCASSERT(isTerminalNode(n));
    switch (getRangeType()) {
      case BOOLEAN:
        v = bool_Tencoder::handle2value(n);
        return;
      case INTEGER:
        v = int_Tencoder::handle2value(n);
        return;
      case REAL:
        v = float_Tencoder::handle2value(n);
        return;
      default:
        throw error(error::MISCELLANEOUS);
    }
  }

inline bool
MEDDLY::expert_forest::getBooleanFromHandle(MEDDLY::node_handle n) const
{
  MEDDLY_DCASSERT(isTerminalNode(n));
  switch (getRangeType()) {
    case BOOLEAN:
      return bool_Tencoder::handle2value(n);
    case INTEGER:
      return int_Tencoder::handle2value(n);
    case REAL:
      return float_Tencoder::handle2value(n);
    default:
      throw error(error::MISCELLANEOUS);
  }
}

inline int
MEDDLY::expert_forest::getIntegerFromHandle(MEDDLY::node_handle n) const
{
  MEDDLY_DCASSERT(isTerminalNode(n));
  switch (getRangeType()) {
    case BOOLEAN:
      return bool_Tencoder::handle2value(n);
    case INTEGER:
      return int_Tencoder::handle2value(n);
    case REAL:
      return float_Tencoder::handle2value(n);
    default:
      throw error(error::MISCELLANEOUS);
  }
}

inline float
MEDDLY::expert_forest::getRealFromHandle(MEDDLY::node_handle n) const
{
  MEDDLY_DCASSERT(isTerminalNode(n));
  switch (getRangeType()) {
    case BOOLEAN:
      return bool_Tencoder::handle2value(n);
    case INTEGER:
      return int_Tencoder::handle2value(n);
    case REAL:
      return float_Tencoder::handle2value(n);
    default:
      throw error(error::MISCELLANEOUS);
  }
}

inline MEDDLY::expert_forest::statset&
MEDDLY::expert_forest::changeStats()
{
  return stats;
}

inline char
MEDDLY::expert_forest::edgeBytes() const
{
  return edge_bytes;
}

inline bool
MEDDLY::expert_forest::areEdgeValuesHashed() const
{
  return hash_edge_values;
}

inline char
MEDDLY::expert_forest::unhashedHeaderBytes() const
{
  return unhashed_bytes;
}

inline char
MEDDLY::expert_forest::hashedHeaderBytes() const
{
  return hashed_bytes;
}

inline const MEDDLY::expert_domain*
MEDDLY::expert_forest::getExpertDomain() const
{
  return (MEDDLY::expert_domain*) getDomain();
}
inline MEDDLY::expert_domain*
MEDDLY::expert_forest::useExpertDomain()
{
  return (MEDDLY::expert_domain*) useDomain();
}

inline int
MEDDLY::expert_forest::getNumVariables() const
{
  return getDomain()->getNumVariables();
}

inline int
MEDDLY::expert_forest::getMinLevelIndex() const
{
  return isForRelations() ? -getNumVariables() : 0;
}

inline bool
MEDDLY::expert_forest::isValidLevel(int k) const
{
  return (k >= getMinLevelIndex()) && (k <= getNumVariables());
}

inline int
MEDDLY::expert_forest::getLevelSize(int lh) const
{
  MEDDLY_DCASSERT(isValidLevel(lh));
  if (lh < 0) {
    return getDomain()->getVariableBound(-lh, true);
  }
  else {
    return getDomain()->getVariableBound(lh, false);
  }
}

inline bool
MEDDLY::expert_forest::isTerminalNode(MEDDLY::node_handle p)
{
  return (p < 1);
}

inline bool
MEDDLY::expert_forest::isValidNonterminalIndex(MEDDLY::node_handle node) const
{
  return (node > 0) && (node <= a_last);
}

inline bool
MEDDLY::expert_forest::isValidNodeIndex(MEDDLY::node_handle node) const
{
  return node <= a_last;
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::getLastNode() const
{
  return a_last;
}

inline const MEDDLY::node_header&
MEDDLY::expert_forest::getNode(MEDDLY::node_handle p) const
{
  MEDDLY_DCASSERT(address);
  MEDDLY_CHECK_RANGE(1, p, 1 + a_last);
  return address[p];
}

inline int
MEDDLY::expert_forest::getNodeLevel(MEDDLY::node_handle p) const
{
  if (isTerminalNode(p))
    return 0;
  MEDDLY_DCASSERT(address);
  MEDDLY_CHECK_RANGE(1, p, 1 + a_last);
  return address[p].level;
}

inline bool
MEDDLY::expert_forest::isPrimedNode(MEDDLY::node_handle p) const
{
  return getNodeLevel(p) < 0;
}

inline bool
MEDDLY::expert_forest::isUnprimedNode(MEDDLY::node_handle p) const
{
  return getNodeLevel(p) > 0;
}

inline int
MEDDLY::expert_forest::getIndexSetCardinality(MEDDLY::node_handle node) const
{
  MEDDLY_DCASSERT(isIndexSet());
  if (isTerminalNode(node))
    return (node != 0) ? 1 : 0;
  // yes iff the unhashed extra header is non-zero.
  const MEDDLY::node_header& nd = getNode(node);
  const int* uhh = (const int*) nodeMan->getUnhashedHeaderOf(nd.offset);
  MEDDLY_DCASSERT(*uhh > 0);
  return *uhh;
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::getNext(MEDDLY::node_handle p) const
{
  MEDDLY_DCASSERT(address);
  MEDDLY_DCASSERT(isValidNonterminalIndex(p));
  return nodeMan->getNextOf(address[p].offset);
}

inline void
MEDDLY::expert_forest::setNext(MEDDLY::node_handle p, MEDDLY::node_handle n)
{
  MEDDLY_DCASSERT(address);
  MEDDLY_DCASSERT(isValidNonterminalIndex(p));
  nodeMan->setNextOf(address[p].offset, n);
}

inline unsigned
MEDDLY::expert_forest::hash(MEDDLY::node_handle p) const
{
  MEDDLY_DCASSERT(address);
  MEDDLY_DCASSERT(isValidNonterminalIndex(p));
#ifdef SAVE_HASHES
  return address[p].hash;
#else
  return hashNode(p);
#endif
}

inline long
MEDDLY::expert_forest::readInCount(MEDDLY::node_handle p) const
{
  return nodeMan->getCountOf(getNode(p).offset);
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::linkNode(MEDDLY::node_handle p)
{
  MEDDLY_DCASSERT(isActiveNode(p));
  if (isTerminalNode(p))
    return p;
  MEDDLY_DCASSERT(!isPessimistic() || !isZombieNode(p));

  long count = incInCount(p);
  if (1 == count) {
    // Reclaim an orphan node
    stats.reclaimed_nodes++;
    stats.orphan_nodes--;
  }
#ifdef TRACK_DELETIONS
  fprintf(stdout, "\t+Node %d count now %ld\n", p, count);
  fflush(stdout);
#endif
  return p;
}

inline void
MEDDLY::expert_forest::unlinkNode(MEDDLY::node_handle p)
{
  MEDDLY_DCASSERT(isActiveNode(p));
  if (isTerminalNode(p))
    return;
  MEDDLY_DCASSERT(!isPessimistic() || !isZombieNode(p));
  MEDDLY_DCASSERT(getInCount(p) > 0);

  long count = decInCount(p);

#ifdef TRACK_DELETIONS
  fprintf(stdout, "\t-Node %d count now %ld\n", p, count);
  fflush(stdout);
#endif
  if (count)
    return;

  handleNewOrphanNode(p);
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::cacheNode(MEDDLY::node_handle p)
{
  MEDDLY_DCASSERT(isActiveNode(p));
  if (isTerminalNode(p))
    return p;
  cacheCount(p)++;
#ifdef TRACK_CACHECOUNT
  fprintf(stdout, "\t+Node %d is in %d caches\n", p, getCacheCount(p));
  fflush(stdout);
#endif
  return p;
}

inline void
MEDDLY::expert_forest::uncacheNode(MEDDLY::node_handle p)
{
  if (isTerminalNode(p))
    return;
  MEDDLY_DCASSERT(isValidNonterminalIndex(p));
  MEDDLY_DCASSERT(
      isActiveNode(p) || (!isActiveNode(p) && isPessimistic()
          && isZombieNode(p)));
  int& cc = cacheCount(p);
  if (isPessimistic() && isZombieNode(p)) {
    // special case: we store the negative of the count.
    MEDDLY_DCASSERT(cc < 0);
    cc++;
    if (0 == cc) {
      stats.zombie_nodes--;
      recycleNodeHandle(p);
    }
    return;
  }
  // we store the actual count.
  MEDDLY_DCASSERT(cc > 0);
  cc--;
#ifdef TRACK_CACHECOUNT
  fprintf(stdout, "\t-Node %d is in %d caches\n", p, cc);
  fflush(stdout);
#endif

  if (cc == 0 && readInCount(p) == 0) {
    MEDDLY_DCASSERT(!isPessimistic());
    stats.orphan_nodes--;
    deleteNode(p);
  }
}

inline bool
MEDDLY::expert_forest::isStale(MEDDLY::node_handle node) const
{
  return isMarkedForDeletion() || (isTerminalNode(node) ? terminalNodesAreStale
      : isPessimistic() ? isZombieNode(node) : (readInCount(node) == 0));
}

inline unsigned
MEDDLY::expert_forest::hashNode(MEDDLY::node_handle p) const
{
  return nodeMan->hashNode(getNode(p));
}

inline int
MEDDLY::expert_forest::getSingletonIndex(MEDDLY::node_handle p, MEDDLY::node_handle &down) const
{
  return nodeMan->getSingletonIndex(getNode(p).offset, down);
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::getSingletonDown(MEDDLY::node_handle node, int index) const
{
  MEDDLY::node_handle down;
  if (getSingletonIndex(node, down) == index)
    return down;
  return 0;
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::getDownPtr(MEDDLY::node_handle p, int index) const
{
  return nodeMan->getDownPtr(getNode(p).offset, index);
}

inline void
MEDDLY::expert_forest::getDownPtr(MEDDLY::node_handle p, int index, int& ev,
    MEDDLY::node_handle& dn) const
{
  nodeMan->getDownPtr(getNode(p).offset, index, ev, dn);
}

inline void
MEDDLY::expert_forest::getDownPtr(MEDDLY::node_handle p, int index, float& ev,
    MEDDLY::node_handle& dn) const
{
  nodeMan->getDownPtr(getNode(p).offset, index, ev, dn);
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::getTransparentNode() const
{
  return transparent;
}

inline void
MEDDLY::expert_forest::initNodeReader(MEDDLY::node_reader &nr, MEDDLY::node_handle node,
    bool full) const
{
  const MEDDLY::node_header &n = getNode(node);
  nr.resize(n.level, getLevelSize(n.level), edgeBytes(), full);
  nodeMan->fillReader(n.offset, nr);
}

inline MEDDLY::node_reader*
MEDDLY::expert_forest::initNodeReader(MEDDLY::node_handle node, bool full) const
{
  MEDDLY::node_reader* nr = MEDDLY::node_reader::useReader();
  MEDDLY_DCASSERT(nr);
  initNodeReader(*nr, node, full);
  return nr;
}

inline MEDDLY::node_reader*
MEDDLY::expert_forest::initRedundantReader(int k, MEDDLY::node_handle node, bool full) const
{
  MEDDLY::node_reader* nr = MEDDLY::node_reader::useReader();
  MEDDLY_DCASSERT(nr);
  initRedundantReader(*nr, k, node, full);
  return nr;
}

inline MEDDLY::node_reader*
MEDDLY::expert_forest::initRedundantReader(int k, int ev, MEDDLY::node_handle nd,
    bool full) const
{
  MEDDLY::node_reader* nr = MEDDLY::node_reader::useReader();
  MEDDLY_DCASSERT(nr);
  initRedundantReader(*nr, k, ev, nd, full);
  return nr;
}

inline MEDDLY::node_reader*
MEDDLY::expert_forest::initRedundantReader(int k, float ev, MEDDLY::node_handle nd,
    bool full) const
{
  MEDDLY::node_reader* nr = MEDDLY::node_reader::useReader();
  MEDDLY_DCASSERT(nr);
  initRedundantReader(*nr, k, ev, nd, full);
  return nr;
}

inline MEDDLY::node_reader*
MEDDLY::expert_forest::initIdentityReader(int k, int i, MEDDLY::node_handle node,
    bool full) const
{
  MEDDLY::node_reader* nr = MEDDLY::node_reader::useReader();
  MEDDLY_DCASSERT(nr);
  initIdentityReader(*nr, k, i, node, full);
  return nr;
}

inline MEDDLY::node_reader*
MEDDLY::expert_forest::initIdentityReader(int k, int i, int ev, MEDDLY::node_handle nd,
    bool full) const
{
  MEDDLY::node_reader* nr = MEDDLY::node_reader::useReader();
  MEDDLY_DCASSERT(nr);
  initIdentityReader(*nr, k, i, ev, nd, full);
  return nr;
}

inline MEDDLY::node_reader*
MEDDLY::expert_forest::initIdentityReader(int k, int i, float ev,
    MEDDLY::node_handle nd, bool full) const
{
  MEDDLY::node_reader* nr = MEDDLY::node_reader::useReader();
  MEDDLY_DCASSERT(nr);
  initIdentityReader(*nr, k, i, ev, nd, full);
  return nr;
}

inline MEDDLY::node_builder&
MEDDLY::expert_forest::useNodeBuilder(int level, int tsz)
{
  MEDDLY_DCASSERT(isValidLevel(level));
  MEDDLY_DCASSERT(!builders[level].lock);
  builders[level].resize(tsz);
  builders[level].lock = true;
#ifdef DEBUG_NODE_BUILDERS
  fprintf(stderr, "using node builder at level %d\n", level);
#endif
  return builders[level];
}

inline MEDDLY::node_builder&
MEDDLY::expert_forest::useSparseBuilder(int level, int nnz)
{
  MEDDLY_DCASSERT(isValidLevel(level));
  MEDDLY_DCASSERT(!builders[level].lock);
  builders[level].resparse(nnz);
  builders[level].lock = true;
#ifdef DEBUG_NODE_BUILDERS
  fprintf(stderr, "using sparse builder at level %d\n", level);
#endif
  return builders[level];
}

inline void
MEDDLY::expert_forest::doneNodeBuilder(MEDDLY::node_builder& nb)
{
  MEDDLY_DCASSERT(nb.lock);
  nb.lock = false;
#ifdef DEBUG_NODE_BUILDERS
  fprintf(stderr, "releasing builder at level %d\n", nb.getLevel());
#endif
}

inline MEDDLY::node_handle
MEDDLY::expert_forest::createReducedNode(int in, MEDDLY::node_builder& nb)
{
  nb.computeHash();
  MEDDLY::node_handle q = createReducedHelper(in, nb);
  MEDDLY_DCASSERT(nb.lock);
  nb.lock = false;
#ifdef TRACK_DELETIONS
  printf("Created node %d\n", q);
#endif
#ifdef DEBUG_NODE_BUILDERS
  fprintf(stderr, "releasing builder at level %d\n", nb.getLevel());
#endif
  return q;
}

template<class T>
  inline void
  MEDDLY::expert_forest::createReducedNode(int in, MEDDLY::node_builder& nb, T& ev,
      MEDDLY::node_handle& node)
  {
    normalize(nb, ev);
    nb.computeHash();
    node = createReducedHelper(in, nb);
    MEDDLY_DCASSERT(nb.lock);
    nb.lock = false;
#ifdef TRACK_DELETIONS
    printf("Created node %d\n", node);
#endif
#ifdef DEBUG_NODE_BUILDERS
    fprintf(stderr, "releasing builder at level %d\n", nb.getLevel());
#endif
  }

inline bool
MEDDLY::expert_forest::areDuplicates(MEDDLY::node_handle node, const MEDDLY::node_builder &nb) const
{
  MEDDLY_DCASSERT(node > 0);
  MEDDLY_DCASSERT(address);
  MEDDLY_CHECK_RANGE(1, node, 1 + a_last);
  if (address[node].level != nb.getLevel())
    return false;
  return nodeMan->areDuplicates(address[node].offset, nb);
}

inline bool
MEDDLY::expert_forest::areDuplicates(MEDDLY::node_handle node, const MEDDLY::node_reader &nr) const
{
  MEDDLY_DCASSERT(node > 0);
  MEDDLY_DCASSERT(address);
  MEDDLY_CHECK_RANGE(1, node, 1 + a_last);
  if (address[node].level != nr.getLevel())
    return false;
  return nodeMan->areDuplicates(address[node].offset, nr);
}

inline void
MEDDLY::expert_forest::setEdgeSize(char ebytes, bool hashed)
{
  MEDDLY_DCASSERT(0 == edge_bytes);
  edge_bytes = ebytes;
  hash_edge_values = hashed;
}
;
inline void
MEDDLY::expert_forest::setUnhashedSize(char ubytes)
{
  MEDDLY_DCASSERT(0 == unhashed_bytes);
  unhashed_bytes = ubytes;
}
inline void
MEDDLY::expert_forest::setHashedSize(char hbytes)
{
  MEDDLY_DCASSERT(0 == hashed_bytes);
  hashed_bytes = hbytes;
}

inline bool
MEDDLY::expert_forest::isZombieNode(long p) const
{
  MEDDLY_DCASSERT(isValidNodeIndex(p));
  MEDDLY_DCASSERT(!isTerminalNode(p));
  return (getCacheCount(p) < 0);
}

inline bool
MEDDLY::expert_forest::isActiveNode(long p) const
{
  return (isValidNodeIndex(p) && (isTerminalNode(p) || getNode(p).offset > 0));
}

inline bool
MEDDLY::expert_forest::isDeletedNode(long p) const
{
  MEDDLY_DCASSERT(isValidNonterminalIndex(p));
  return !(isActiveNode(p) || isZombieNode(p));
}

inline bool
MEDDLY::expert_forest::isTimeToGc() const
{
  return isPessimistic() ? (stats.zombie_nodes > deflt.zombieTrigger)
      : (stats.orphan_nodes > deflt.orphanTrigger);
}

inline long
MEDDLY::expert_forest::getInCount(MEDDLY::node_handle p)
{
  return nodeMan->getCountOf(getNode(p).offset);
}

inline long
MEDDLY::expert_forest::incInCount(MEDDLY::node_handle p)
{
  return nodeMan->incCountOf(getNode(p).offset);
}

inline long
MEDDLY::expert_forest::decInCount(MEDDLY::node_handle p)
{
  return nodeMan->decCountOf(getNode(p).offset);
}

inline int&
MEDDLY::expert_forest::cacheCount(MEDDLY::node_handle p)
{
  MEDDLY_DCASSERT(isValidNodeIndex(p));
  MEDDLY_DCASSERT(!isTerminalNode(p));
  return address[p].cache_count;
}

inline int
MEDDLY::expert_forest::getCacheCount(MEDDLY::node_handle p) const
{
  MEDDLY_DCASSERT(isValidNodeIndex(p));
  MEDDLY_DCASSERT(!isTerminalNode(p));
  return address[p].cache_count;
}

inline void
MEDDLY::expert_forest::moveNodeOffset(MEDDLY::node_handle node, node_address old_addr,
    node_address new_addr)
{
  MEDDLY_DCASSERT(address);
  MEDDLY_DCASSERT(old_addr == address[node].offset);
  address[node].offset = new_addr;
}

// ****************************************************************************

inline int
MEDDLY::opname::getIndex() const
{
  return index;
}

inline const char*
MEDDLY::opname::getName() const
{
  return name;
}

// ****************************************************************************

inline void
MEDDLY::specialized_opname::arguments::setAutoDestroy(bool destroy)
{
  destroyWhenDone = destroy;
}

inline bool
MEDDLY::specialized_opname::arguments::autoDestroy() const
{
  return destroyWhenDone;
}

// ****************************************************************************

inline MEDDLY::specialized_operation*
MEDDLY::numerical_opname::buildOperation(const dd_edge &x_ind,
    const dd_edge &A, const dd_edge &y_ind) const
{
  numerical_args na(x_ind, A, y_ind);
  na.setAutoDestroy(false); // na will be destroyed when we return
  return buildOperation(&na);
}

// ****************************************************************************


inline bool
MEDDLY::satpregen_opname::pregen_relation::isFinalized() const
{
  return 0 == next;
}

inline MEDDLY::node_handle*
MEDDLY::satpregen_opname::pregen_relation::arrayForLevel(int k) const
{
  MEDDLY_DCASSERT(isFinalized());
  MEDDLY_CHECK_RANGE(1, k, K + 1);
  if (level_index) {
    // "by events"
    if (level_index[k - 1] > level_index[k]) {
      return events + level_index[k];
    }
    else {
      // empty list
      return 0;
    }
  }
  else {
    // "by levels"
    if (events[k])
      return events + k;
    else
      return 0;
  }
}

inline int
MEDDLY::satpregen_opname::pregen_relation::lengthForLevel(int k) const
{
  MEDDLY_DCASSERT(isFinalized());
  MEDDLY_CHECK_RANGE(1, k, K + 1);
  if (level_index) {
    // "by events"
    return level_index[k - 1] - level_index[k];
  }
  else {
    // "by levels"
    return events[k] ? 1 : 0;
  }
}

inline MEDDLY::forest*
MEDDLY::satpregen_opname::pregen_relation::getInForest() const
{
  return insetF;
}

inline MEDDLY::forest*
MEDDLY::satpregen_opname::pregen_relation::getRelForest() const
{
  return mxdF;
}

inline MEDDLY::forest*
MEDDLY::satpregen_opname::pregen_relation::getOutForest() const
{
  return outsetF;
}

// ****************************************************************************

inline MEDDLY::operation*
MEDDLY::compute_table::search_key::getOp() const
{
  return op;
}

inline void
MEDDLY::compute_table::search_result::setValid()
{
  is_valid = true;
}
inline void
MEDDLY::compute_table::search_result::setInvalid()
{
  is_valid = false;
}
inline
MEDDLY::compute_table::search_result::operator bool() const
{
  return is_valid;
}

// convenience methods, for grabbing edge values
inline void
MEDDLY::compute_table::readEV(const MEDDLY::node_handle* p, int &ev)
{
  ev = p[0];
}
inline void
MEDDLY::compute_table::readEV(const MEDDLY::node_handle* p, float &ev)
{
  float* f = (float*) p;
  ev = f[0];
}

inline const MEDDLY::compute_table::stats&
MEDDLY::compute_table::getStats()
{
  return perf;
}

// ****************************************************************************


inline void
MEDDLY::operation::setAnswerForest(const MEDDLY::expert_forest* f)
{
  discardStaleHits = f ? f->getNodeDeletion()
      == MEDDLY::forest::policies::PESSIMISTIC_DELETION : false; // shouldn't be possible, so we'll do what's fastest.
}

inline void
MEDDLY::operation::registerInForest(MEDDLY::forest* f)
{
  if (f)
    f->registerOperation(this);
}

inline void
MEDDLY::operation::unregisterInForest(MEDDLY::forest* f)
{
  if (f)
    f->unregisterOperation(this);
}

inline MEDDLY::compute_table::search_key*
MEDDLY::operation::useCTkey()
{
  MEDDLY_DCASSERT(CT);
  compute_table::search_key* ans;
  if (CT_free_keys) {
    ans = CT_free_keys;
    CT_free_keys = ans->next;
  }
  else {
    ans = CT->initializeSearchKey(this);
  }
  return ans;
}

inline bool
MEDDLY::operation::isMarkedForDeletion() const
{
  return is_marked_for_deletion;
}

inline void
MEDDLY::operation::setNext(operation* n)
{
  next = n;
}

inline MEDDLY::operation*
MEDDLY::operation::getNext()
{
  return next;
}

inline bool
MEDDLY::operation::usesMonolithicComputeTable()
{
  return Monolithic_CT;
}

inline int
MEDDLY::operation::getIndex() const
{
  return oplist_index;
}

inline MEDDLY::operation*
MEDDLY::operation::getOpWithIndex(int i)
{
  return op_list[i];
}

inline int
MEDDLY::operation::getOpListSize()
{
  return list_size;
}

inline const char*
MEDDLY::operation::getName() const
{
  return theOpName->getName();
}

inline const MEDDLY::opname*
MEDDLY::operation::getOpName() const
{
  return theOpName;
}

inline int
MEDDLY::operation::getKeyLength() const
{
  return key_length;
}

inline int
MEDDLY::operation::getAnsLength() const
{
  return ans_length;
}

inline int
MEDDLY::operation::getCacheEntryLength() const
{
  return key_length + ans_length;
}

inline bool
MEDDLY::operation::isEntryStale(const MEDDLY::node_handle* data)
{
  return (is_marked_for_deletion || isStaleEntry(data));
}

inline void
MEDDLY::operation::doneCTkey(compute_table::search_key* K)
{
  MEDDLY_DCASSERT(K);
  K->next = CT_free_keys;
  CT_free_keys = K;
}

inline bool
MEDDLY::operation::shouldStaleCacheHitsBeDiscarded() const
{
  return discardStaleHits;
}

// ****************************************************************************

inline bool
MEDDLY::unary_operation::matches(const MEDDLY::expert_forest* arg,
    const MEDDLY::expert_forest* res) const
{
  return (arg == argF && res == resF);
}

inline bool
MEDDLY::unary_operation::matches(const MEDDLY::expert_forest* arg, opnd_type res) const
{
  return (arg == argF && resultType == res);
}

// ****************************************************************************

inline bool
MEDDLY::binary_operation::matches(const MEDDLY::expert_forest* arg1,
    const MEDDLY::expert_forest* arg2, const MEDDLY::expert_forest* res) const
{
  return (arg1 == arg1F && arg2 == arg2F && res == resF);
}

inline void
MEDDLY::binary_operation::operationCommutes()
{
  can_commute = (arg1F == arg2F);
}

// ****************************************************************************

inline void
MEDDLY::op_initializer::recycle(op_initializer *I)
{
  if (0 == I)
    return;
  MEDDLY_DCASSERT(I->refcount);
  I->refcount--;
  if (0 == I->refcount)
    delete I;
}

inline MEDDLY::op_initializer*
MEDDLY::op_initializer::copy(op_initializer *I)
{
  if (I)
    I->refcount++;
  return I;
}

// ****************************************************************************

inline MEDDLY::unary_operation*
MEDDLY::getOperation(const unary_opname* code, const dd_edge& arg,
    const dd_edge& res)
{
  return getOperation(code, (MEDDLY::expert_forest*) arg.getForest(),
      (MEDDLY::expert_forest*) res.getForest());
}

inline MEDDLY::unary_operation*
MEDDLY::getOperation(const unary_opname* code, const dd_edge& arg,
    opnd_type res)
{
  return getOperation(code, (MEDDLY::expert_forest*) arg.getForest(), res);
}

inline MEDDLY::binary_operation*
MEDDLY::getOperation(const binary_opname* code, const dd_edge& arg1,
    const dd_edge& arg2, const dd_edge& res)
{
  return getOperation(code, (MEDDLY::expert_forest*) arg1.getForest(),
      (MEDDLY::expert_forest*) arg2.getForest(), (MEDDLY::expert_forest*) res.getForest());
}

#endif
