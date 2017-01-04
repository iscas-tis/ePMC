
// $Id: meddly_expert.h 618 2015-04-21 17:54:39Z junaidbabar $

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


/*! \file meddly_expert.h

    Low-level MDD library interface.

    This interface is for "expert" users who want to define new
    operations, or for library developers to define the built-in operations.
    Casual users probably only need the interface provided by "meddly.h".

    The first part of the interface describes the expert interface and the
    second part contains implementations of virtual functions in the interface.

    IMPORTANT: meddly.h must be included before including this file.
    TODO: Operations are not thread-safe.
*/

#ifndef MEDDLY_EXPERT_H
#define MEDDLY_EXPERT_H

#include <string.h>

// Flags for development version only. Significant reduction in performance.
#ifdef DEVELOPMENT_CODE
#define RANGE_CHECK_ON
#define DCASSERTS_ON
#endif

// #define DEBUG_NODE_BUILDERS
// #define DEBUG_MDD_H
// #define TRACK_DELETIONS
// #define TRACK_CACHECOUNT

// Use this for assertions that will fail only when your
// code is wrong.  Handy for debugging.
#ifdef DCASSERTS_ON
#define MEDDLY_DCASSERT(X) assert(X)
#else
#define MEDDLY_DCASSERT(X)
#endif

// Use this for range checking assertions that should succeed.
#ifdef RANGE_CHECK_ON
#define MEDDLY_CHECK_RANGE(MIN, VALUE, MAX) { assert(VALUE < MAX); assert(VALUE >= MIN); }
#else
#define MEDDLY_CHECK_RANGE(MIN, VALUE, MAX)
#endif

//
// Design decision: should we remember the hashes for a reduced node?
// Tests so far indicate: doesn't save much, if any, time; and has
// a noticeable increase in memory.
//
// #define SAVE_HASHES

namespace MEDDLY {

  // classes defined here
  struct settings;
  class expert_variable;
  class expert_domain;

  // wrappers for nodes
  class node_reader;
  class node_builder;

  // Actual node storage
  struct node_header;
  class node_storage;

  class expert_forest;

  class opname;
  class unary_opname;
  class binary_opname;
  class specialized_opname;
  class numerical_opname;
  class satpregen_opname;
  class satotf_opname;

  class compute_table_style;
  class compute_table;

  class operation;
  class unary_operation;
  class binary_operation;
  class specialized_operation;

  class op_initializer;

  class cleanup_procedure;

  // classes defined elsewhere
  class base_table;
  class unique_table;

  // ******************************************************************
  // *                                                                *
  // *                   Named numerical operations                   *
  // *                                                                *
  // ******************************************************************

  /** Computes y = y + xA.
      x and y are vectors, stored explicitly, and A is a matrix.
      x_ind and y_ind specify how minterms are mapped to indexes
      for vectors x and y, respectively.
  */
  extern const numerical_opname* EXPLVECT_MATR_MULT;
  // extern const numerical_opname* VECT_MATR_MULT; // renamed!

  /** Computes y = y + Ax.
      x and y are vectors, stored explicitly, and A is a matrix.
      x_ind and y_ind specify how minterms are mapped to indexes
      for vectors x and y, respectively.
  */
  extern const numerical_opname* MATR_EXPLVECT_MULT;
  // extern const numerical_opname* MATR_VECT_MULT; // renamed!

  // ******************************************************************
  // *                                                                *
  // *                  Named saturation operations                   *
  // *                                                                *
  // ******************************************************************

  /** Forward reachability using saturation.
      Transition relation is already known.
  */
  extern const satpregen_opname* SATURATION_FORWARD;

  /** Backward reachability using saturation.
      Transition relation is already known.
  */
  extern const satpregen_opname* SATURATION_BACKWARD;

  /** Forward reachability using saturation.
      Transition relation is not completely known,
      will be built along with reachability set.
  */
  extern const satotf_opname* SATURATION_OTF;

  // ******************************************************************
  // *                                                                *
  // *                      Operation management                      *
  // *                                                                *
  // ******************************************************************

  /// Remove an existing operation from the operation cache.
  void removeOperationFromCache(operation* );

  /** Find, or build if necessary, a unary operation.
        @param  code    Operation we want
        @param  arg     Argument forest
        @param  res     Result forest
        @return         The matching operation, if it already exists;
                        a new operation, otherwise.
  */
  unary_operation* getOperation(const unary_opname* code,
    expert_forest* arg, expert_forest* res);

  /** Find, or build if necessary, a unary operation.
        @param  code    Operation we want
        @param  arg     Argument forest from this dd_edge
        @param  res     Result forest from this dd_edge
        @return         The matching operation, if it already exists;
                        a new operation, otherwise.
  */
  unary_operation* getOperation(const unary_opname* code,
    const dd_edge& arg, const dd_edge& res);

  /** Find, or build if necessary, a unary operation.
        @param  code    Operation we want
        @param  arg     Argument forest
        @param  res     Result type
        @return         The matching operation, if it already exists;
                        a new operation, otherwise.
  */
  unary_operation* getOperation(const unary_opname* code,
    expert_forest* arg, opnd_type result);

  /** Find, or build if necessary, a unary operation.
        @param  code    Operation we want
        @param  arg     Argument forest from this dd_edge
        @param  res     Result type
        @return         The matching operation, if it already exists;
                        a new operation, otherwise.
  */
  unary_operation* getOperation(const unary_opname* code,
    const dd_edge& arg, opnd_type result);


  /** Find, or build if necessary, a binary operation.
        @param  code    Operation we want
        @param  arg1    Argument 1 forest
        @param  arg2    Argument 2 forest
        @param  res     Result forest
        @return         The matching operation, if it already exists;
                        a new operation, otherwise.
  */
  binary_operation* getOperation(const binary_opname* code,
    expert_forest* arg1, expert_forest* arg2, expert_forest* res);

  /** Find, or build if necessary, a binary operation.
        @param  code    Operation we want
        @param  arg1    Argument 1 forest taken from this dd_edge
        @param  arg2    Argument 2 forest taken from this dd_edge
        @param  res     Result forest taken from this dd_edge
        @return         The matching operation, if it already exists;
                        a new operation, otherwise.
  */
  binary_operation* getOperation(const binary_opname* code,
    const dd_edge& arg1, const dd_edge& arg2, const dd_edge& res);

  /** Safely destroy the given unary operation. 
      It should be unnecessary to call this directly.
  */
  void destroyOperation(unary_operation* &op);

  /** Safely destroy the given binary operation. 
      It should be unnecessary to call this directly.
  */
  void destroyOperation(binary_operation* &op);

  /// Safely destroy the given numerical operation.
  void destroyOperation(specialized_operation* &op);

  /// Should not be called directly.
  void destroyOpInternal(operation* op);

  /// Builds an initializer for MEDDLY's builtin operations.
  op_initializer* makeBuiltinInitializer();

  // ******************************************************************
  // *                                                                *
  // *                      Compute table styles                      *
  // *                                                                *
  // ******************************************************************

  /// One huge hash table that uses chaining.
  extern const compute_table_style* MonolithicChainedHash;

  /// One huge hash table that does not use chaining.
  extern const compute_table_style* MonolithicUnchainedHash;

  /// A hash table (with chaining) for each operation.
  extern const compute_table_style* OperationChainedHash;

  /// A hash table (no chaining) for each operation.
  extern const compute_table_style* OperationUnchainedHash;

  /// A STL "map" for each operation.
  extern const compute_table_style* OperationMap;

}; // namespace MEDDLY


// ******************************************************************
// *                                                                *
// *                         settings  class                        *
// *                                                                *
// ******************************************************************

/** "Global" settings for MEDDLY.
    These settings DO NOT CHANGE once the library is initialized.

    The compute cache by default uses a hash table with chaining (i.e. a
    list of entries at each hash location). This can be changed to a
    hash-table without chaining. The maximum size of the hash table can
    also be fixed (to limit the amount of memory it uses).
*/
struct MEDDLY::settings {
  public:

    struct computeTableSettings {
      public:
        enum staleRemovalOption {
          /// Whenever we see a stale entry, remove it.
          Aggressive,
          /// Only remove stales when we need to expand the table
          Moderate,
          /// Only remove stales during Garbage Collection.
          Lazy
        };

        /// The type of compute table(s) that should be used.
        const compute_table_style* style;
        /// Maximum compute table size.
        unsigned maxSize;
        /// How aggressively should we try to eliminate stale entries.
        staleRemovalOption staleRemoval;

        /// Constructor, to set defaults.
        computeTableSettings();
    };

    /// Settings for the compute table(s)
    computeTableSettings ctSettings;
    /// Default forest policies for "sets"
    forest::policies mddDefaults;
    /// Default forest policies for "relations"
    forest::policies mxdDefaults;
    /// Initializer for operations
    op_initializer* operationBuilder;

    /// Constructor, to set defaults.
    settings();
    /// Copy constructor.
    settings(const settings &s);
    /// Destructor
    ~settings();
    void operator=(const settings &s);

  private:
    void init(const settings &s);
    void clear();
};

// ******************************************************************
// *                                                                *
// *                     expert_variable  class                     *
// *                                                                *
// ******************************************************************

class MEDDLY::expert_variable : public variable {
  public:
    expert_variable(int b, char* n);

    /// Update our list of domains: add \a d.
    void addToList(domain* d);
    /// Update our list of domains: remove \a d.
    void removeFromList(const domain* d);

    /** Enlarge the possible values for a variable.
      This could modify all nodes in all forests, depending on the
      choice of reduction rule.
      @param  prime   If prime is true, enlarge the bound for
                      the primed variable only, otherwise both
                      the primed and unprimed are enlarged.
      @param  b       New bound, if less than the current bound
                      an error is thrown.
    */
    void enlargeBound(bool prime, int b);

    /** Shrink the possible values for a variable.
      This could modify all nodes in all forests, depending on the
      choice of reduction rule.
      @param  b       New bound, if more than the current bound
                      an error is thrown.
      @param  force   If \a b is too small, and information will be lost,
                      proceed anyway if \a force is true, otherwise
                      return an error code.
    */
    void shrinkBound(int b, bool force);

  private:
    domain** domlist;
    int dl_alloc;
    int dl_used;

    virtual ~expert_variable();
};


// ******************************************************************
// *                                                                *
// *                      expert_domain  class                      *
// *                                                                *
// ******************************************************************

class MEDDLY::expert_domain : public domain {
  public:
    expert_domain(variable**, int);

    virtual void createVariablesBottomUp(const int* bounds, int N);

    /** Create all variables at once, from the top down.
      Requires the domain to be "empty" (containing no variables or forests).
      @param  bounds  Current variable bounds.
                      bounds[0] gives the bound for the top-most variable,
                      and bounds[N-1] gives the bound for the bottom-most
                      variable.
      @param  N       Number of variables.
    */
    void createVariablesTopDown(const int* bounds, int N);

    /** Insert a new variable.
          @param  lev   Level to insert above; use 0 for a
                        new bottom-most level.
          @param  v     Variable to insert.
    */
    void insertVariableAboveLevel(int lev, variable* v);

    /** Remove a variable at the specified level.
        An error is thrown if the variable size is not 1.
        Use shrinkVariableBound() to make the bound 1.
        All forests are modified as appropriate.
          @param  lev   Level number.
    */
    void removeVariableAtLevel(int lev);

    /** Find the level of a given variable.
          @param  v   Variable to search for.
          @return 0, if the variable was not found;
                  i, with getVar(i) == v, otherwise.
    */
    int findLevelOfVariable(const variable* v) const;

    expert_variable* getExpertVar(int lev) const;
    const expert_variable* readExpertVar(int lev) const;

    /** Add a new variable with bound 1.
      Can be used when the domain already has forests, in which case
      all forests are modified as appropriate.
      @param  below   Placement information: the new variable will appear
                      immediately above the level \a below.
    */
    void createVariable(int below);

    /** Add a new variable with bound 1.
      Deprecated as of version 0.5; use one paramater version instead.
    */
#ifdef _MSC_VER
    __declspec(deprecated)
#endif
#ifdef __GNUC__
    __attribute__ ((deprecated))
#endif
    void createVariable(int below, int &vh);

    /** Destroy a variable with bound 1.
        Deprecated as of version 0.5; use removeVariableAtLevel instead.
    */
#ifdef _MSC_VER
    __declspec(deprecated)
#endif
#ifdef __GNUC__
    __attribute__ ((deprecated))
#endif
    void destroyVariable(int vh);

    /** Get the position of the variable in this domain's variable-order.
      \a TERMINALS are considered to be at height 0.
      be at height 0.
      Deprecated as of version 0.5: level height always equals level handle.
      @param  vh      Any variable handle.
      @return         The variable at this height. 0 for \a TERMINALS.
                      If \a vh is not a valid level handle, return -1.
    */
#ifdef _MSC_VER
    __declspec(deprecated)
#endif
#ifdef __GNUC__
    __attribute__ ((deprecated))
#endif
    int getVariableHeight(int vh) const;

    /** Get the variable with height \a ht in this domain's variable-order.
      \a TERMINALS are considered to be at height 0.
      Deprecated as of version 0.5: level height always equals level handle.
      @param  ht      Height of the variable.
      @return         The variable with this height. If the height is not in
                      [0, height of top variable], returns -1.
    */
#ifdef _MSC_VER
    __declspec(deprecated)
#endif
#ifdef __GNUC__
    __attribute__ ((deprecated))
#endif
    int getVariableWithHeight(int ht) const;
    /** Swap the locations of variables in forests.
      I.e., changes the variable ordering of all forests with this domain.
      @param  lev1    Level of first variable.
      @param  lev2    Level of second variable.
    */
    void swapOrderOfVariables(int lev1, int lev2);

    /** Find the actual bound of a variable.
      @param  vh      Variable handle.
      @return         The smallest shrinkable bound before information loss
                      for variable \a vh. If \a vh is invalid, or TERMINALS,
                      returns 0.
    */
    int findVariableBound(int vh) const;

    /** Enlarge the possible values for a variable.
      This could modify all nodes in all forests, depending on the
      choice of reduction rule.
      @param  vh     Variable handle.
      @param  prime   If prime is true, enlarge the bound for
                      the primed variable only, otherwise both
                      the primed and unprimed are enlarged.
      @param  b       New bound, if less than the current bound
                      an error code is returned.
    */
    void enlargeVariableBound(int vh, bool prime, int b);

    /** Shrink the possible values for a variable.
      This could modify all nodes in all forests, depending on the
      choice of reduction rule.
      @param  lev     Variable handle.
      @param  b       New bound, if more than the current bound
                      an error code is returned.
      @param  force   If \a b is too small, and information will be lost,
                      proceed anyway if \a force is true, otherwise
                      return an error code.
    */
    void shrinkVariableBound(int vh, int b, bool force);

    virtual void write(FILE* s) const;
    virtual void read(FILE* s);

  protected:
    ~expert_domain();
};


// ******************************************************************
// *                                                                *
// *                       node_reader  class                       *
// *                                                                *
// ******************************************************************

/** Class for reading nodes.
    Ideally - used anywhere we want to read node data.
    Backend implementation may change :^)
    Implemented in node_wrappers.cc.
    Readers may be "full" or "sparse",
    regardless of how the actual node is stored.

    Currently, a node is:
      array of node_handles, for the downward pointers
      array of integers, for indexes (sparse only)
      "compact" chunk of memory for edge values
*/
class MEDDLY::node_reader {
  public:
    /** Constructor.
     The class must be "filled" by a forest before
     it can be used, however.
     */
    node_reader();

    /// Destructor.
    ~node_reader();

    /// Free memory, but don't delete.
    void clear();

    /// Display this node
    void show(FILE* s, const expert_forest* parent, bool verb) const;

    /// Get a pointer to the hashed header data.
    const void* HHptr() const;

    /// Get the number of bytes of hashed header data.
    int HHbytes() const;

    /** Get a downward pointer.
     @param  n   Which pointer.
     @return     If this is a full reader,
     return pointer with index n.
     If this is a sparse reader,
     return the nth non-zero pointer.
     */
    node_handle d(int n) const;

    /** Get the index of the nth non-zero pointer.
     Use only for sparse readers.
     */
    int i(int n) const;

    /// Get a pointer to an edge
    const void* eptr(int i) const;

    /// Get the edge value, as an integer.
    void getEdge(int i, int& ev) const;

    /// Get the edge value, as a float.
    void getEdge(int i, float& ev) const;

    /// Get the edge value, as an integer.
    int ei(int i) const;

    /// Get the edge value, as a float.
    float ef(int i) const;

    /// Get the level number of this node.
    int getLevel() const;

    /// Get the size of this node (full readers only).
    int getSize() const;

    /// Get the number of nonzeroes of this node (sparse readers only).
    int getNNZs() const;

    /// Is this a sparse reader?
    bool isSparse() const;

    /// Is this a full reader?
    bool isFull() const;

    /// Does this node have edge values?
    bool hasEdges() const;

    /// Number of bytes per edge
    int edgeBytes() const;

    // For debugging unique table.
    unsigned hash() const;

    void setHash(unsigned H);

    void computeHash(bool hashEdgeValues, node_handle tv);

    // Centralized recycling
    static node_reader* useReader();
    static void recycle(node_reader* r);

  private:
    static node_reader* freeList;
    node_reader* next; // for recycled list
    /*
     Extra info that is hashed
     */
    void* extra_hashed;
    int ext_alloc;
    int ext_size;
    /*
     Down pointers, indexes, edge values.
     */
    node_handle* down;
    int* index;
    void* edge;
    int alloc;
    int ealloc;
    int size;
    int nnzs;
    int level;
    unsigned h;
    char edge_bytes; // number of bytes for an edge value.
    bool is_full;
#ifdef DEVELOPMENT_CODE
    bool has_hash;
#endif

    static void freeRecycled();

    /// Called within expert_forest to allocate space.
    ///   @param  p     Parent.
    ///   @param  k     Level number.
    ///   @param  ns    Size of node.
    ///   @param  eb    Bytes for each edge.
    ///   @param  full  If true, we'll be filling a full reader.
    ///                 Otherwise it is a sparse one.
    void resize(int k, int ns, char eb, bool full);

    /// Allocate space for extra hashed info
    void resize_header(int extra_bytes);

    friend class expert_forest;
    friend class node_storage;
    friend void cleanup();
};


// ******************************************************************
// *                                                                *
// *                       node_builder class                       *
// *                                                                *
// ******************************************************************

/** Class for building nodes.
    Effectively, a reserved chunk of memory for storing down pointers
    and edge values.
    Implemented in node_wrappers.cc.
*/
class MEDDLY::node_builder {

  public:
    bool lock;

    node_builder();
    ~node_builder();
    void init(int k, const expert_forest* p);
    void show(FILE* s, bool verb) const;
    bool hasEdges() const;
    int edgeBytes() const;
    void resize(int s);
    void resparse(int s);
    // used when we don't know exactly the sparse size
    void shrinkSparse(int ns);
    bool isSparse() const;
    bool isFull() const;
    int rawSize() const;
    int getSize() const;
    int getNNZs() const;
    int getLevel() const;
    void setUH(const void* x);
    void getUH(void* x) const;
    void setHH(const void* x);
    void getHH(void* x) const;
    node_handle& d(int i);
    node_handle d(int i) const;
    int& i(int i);
    int i(int i) const;
    // edge getting
    void getEdge(int i, int& ev) const;
    void getEdge(int i, float& ev) const;
    int ei(int i) const;
    float ef(int i) const;
    // edge setting
    void setEdge(int i, int ev);
    void setEdge(int i, float ev);
    // raw edge info
    void* eptr(int i);
    const void* eptr(int i) const;
    /// Get a pointer to the unhashed header data.
    void* UHptr();
    /// Get a pointer to the hashed header data.
    const void* HHptr() const;
    void* HHptr();
    /// Get the number of bytes of hashed header data.
    int HHbytes() const;
    // for unique table
    unsigned hash() const;
    void computeHash();

  protected:
    void enlarge();

  private:
    const expert_forest* parent;
    void* extra_hashed;
    void* extra_unhashed;
    node_handle* down;
    int* indexes;
    void* edge;
    int hhbytes; // number of bytes in hashed header
    int level; // level of this node.
    int size;
    int alloc;
    unsigned h;
    char edge_bytes;
#ifdef DEVELOPMENT_CODE
    bool has_hash;
#endif
    bool is_sparse;

    // These cheat; let's see if any compiler complains.
    void* raw_hh() const;
    void* raw_uh() const;
    node_handle& raw_d(int i) const;
    int& raw_i(int i) const;

}; // end of node_builder class




// ******************************************************************
// *                                                                *
// *                       node_header  class                       *
// *                                                                *
// ******************************************************************

/** Header information for each node.
    The node handles (integers) need to keep some
    information about the node they point to,
    both for addressing and for bookkeeping purposes.
    This struct holds that information.
*/
struct MEDDLY::node_header {
    /** Offset to node's data in the corresponding node storage structure.
        If the node is active, this is the offset (>0) in the data array.
        If the node is deleted, this is -next deleted node
        (part of the unused address list).
    */
    node_address offset;

    /** Node level
        If the node is active, this indicates node level.
    */
    int level;

    /** Cache count
        The number of cache entries that refer to this node (excl. unique
        table). If this node is a zombie, cache_count is negative.
    */
    int cache_count;

#ifdef SAVE_HASHES
    /// Remember the hash for speed.
    unsigned hash;
#endif

    // Handy functions, in case our internal storage changes.

    bool isActive() const;
    bool isZombie() const;

    bool isDeleted() const;
    void setDeleted();
    void setNotDeleted();

    int getNextDeleted() const;
    void setNextDeleted(int n);

    void makeZombie();
};



// ******************************************************************
// *                                                                *
// *                       node_storage class                       *
// *                                                                *
// ******************************************************************

/** Abstract base class for node storage.

    The base class is implemented in node_wrappers.cc;
    various backends are implemented in directory storage/,
    or you may implement your own scheme :^)

    Nodes are represented by an address, which for a valid
    node, will be greater than 0.  The first valid address
    must be 1.

    Whatever scheme is used to store nodes internally,
    it must be possible to set pointers \a count and \a next
    such that count[addr] and next[addr] give the incoming
    count and next pointer for the given node addr.
    Derived classes are responsible for setting up
    and maintaining these pointers.
*/
class MEDDLY::node_storage {
  public:
    node_storage();
    virtual ~node_storage();

    /** Build a new mechanism, bound to the given forest.

        Any instance serves also as a factory to build new instances.
        However, an instance is not initialized until it is bound
        to a given forest.

          @param  f   Forest to bind to

          @return     A pointer to a node storage class,
                      initialized for forest f.
                      Most likely, we are returning the same
                      instance as ourself, but this is not required.
    */
    virtual node_storage* createForForest(expert_forest* f) const = 0;

    /** Should be called by createForForest in derived classes.
        Initializes the base class for a given forest.

          @param  f   Forest to bind to
    */
    void initForForest(expert_forest* f);

    /** Go through and collect any garbage.

          @param  shrink    If true, we will shrink data structures
                            as we free space; otherwise, we won't.
    */
    virtual void collectGarbage(bool shrink) = 0;

    /** Show various stats.
          @param  s       Output stream to write to
          @param  pad     Padding string, written at the start of
                          each output line.
          @param  flags   Controls what is displayed.
    */
    virtual void reportStats(FILE* s, const char* pad, unsigned flags) const = 0;

    /** Write a node in human-readable format.

        Ideally, the output format for each node is the same
        regardless of how it is stored.

        @param  s       Output stream.
        @param  addr    Address of the node we care about.
        @param  verb    Verbose output or not.
    */
    virtual void showNode(FILE* s, node_address addr, bool verb) const = 0;

    /** Write a node in machine-readable format.

        Ideally, the output format for each node is the same
        regardless of how it is stored.

        @param  s       Output stream.
        @param  addr    Address of the node we care about.
        @param  map     Translation to use on node handles.
                        Allows us to renumber nodes as we write them.
    */
    virtual void
        writeNode(FILE* s, node_address addr, const node_handle* map) const;

    /** Dump the internal storage details.
        Primarily used for debugging.

          @param  s       Output stream to use
          @param  flags   What to show.
                            0x01  Show active memory
                            0x02  Show memory "holes"
    */
    void dumpInternal(FILE* s, unsigned flags) const;

    /** Allocate space for, and store, a node.
        I.e., create a new node that is a copy of the given one.
        The node might be "compressed" in various ways to reduce
        storage requirements.  (Indeed, that is the whole point
        of the node_storage class.)
            @param  p     Node handle number, in case it is used
            @param  nb    Node data is copied from here.
            @param  opt   Ways we can store the node.
            @return       The "address" of the new node.
    */
    virtual node_address makeNode(node_handle p, const node_builder &nb,
                                  node_storage_flags opt) = 0;

    /** Destroy a node.
        Unlink the downward pointers, and recycle the memory
        used by the node.
            @param  addr    Address of the node.
    */
    virtual void unlinkDownAndRecycle(node_address addr) = 0;


    // various ways to read a node

    /** Check for duplicates.
          @param  addr    Node address in this structure
          @param  nb      Node to compare against

          @return true    iff the nodes are duplicates
    */
    virtual bool
        areDuplicates(node_address addr, const node_builder &nb) const = 0;

    /** Check for duplicates.
          @param  addr    Node address in this structure
          @param  nr      Node to compare against

          @return true    iff the nodes are duplicates
    */
    virtual bool
        areDuplicates(node_address addr, const node_reader &nr) const = 0;

    /** Fill a node reader.
        Useful if we want to read an entire node.
          @param  addr    Node address in this structure.
          @param  nr      Node reader to fill, has already
                          been "resized".  Full or sparse
                          will be decided from \a nr settings.
    */
    virtual void fillReader(node_address addr, node_reader &nr) const = 0;

    /** Compute the hash value for a node.
        This requires an actual "node", not just an address,
        because the hash contains the node's level.
        Should give the same answer as filling a node_reader
        and computing the hash on the node_reader.

          @param  p     Node of interest.
    */
    virtual unsigned hashNode(const node_header& p) const = 0;


    /** Determine if this is a singleton node.
        Used for identity reductions.
          @param  addr    Address of the node we care about
          @param  down    Output:
                          The singleton downward pointer, or undefined.

          @return   If the node has only one non-zero downward pointer,
                    then return the index for that pointer.
                    Otherwise, return a negative value.
    */
    virtual int
        getSingletonIndex(node_address addr, node_handle &down) const = 0;


    /** Get the specified downward pointer for a node.
        Fast if we just want one.
          @param  addr    Address of the node we care about
          @param  index   Index of downward pointer
          @return         Desired pointer
          @throw          INVALID_VARIABLE, if index is negative.
    */
    virtual node_handle getDownPtr(node_address addr, int index) const = 0;

    /** Get the specified outgoing edge for a node.
        Fast if we just want one.

          @param  addr    Address of the node we care about
          @param  ind     Index of the pointer we want.
          @param  ev      Output: edge value at that index.
          @param  dn      Output: downward pointer at that index.
    */
    virtual void getDownPtr(node_address addr, int ind, int& ev,
                            node_handle& dn) const = 0;

    /** Get the specified outgoing edge for a node.
        Fast if we just want one.

          @param  addr    Address of the node we care about
          @param  ind     Index of the pointer we want.
          @param  ev      Output: edge value at that index.
          @param  dn      Output: downward pointer at that index.
    */
    virtual void getDownPtr(node_address addr, int ind, float& ev,
                            node_handle& dn) const = 0;


    /** Read the unhashed header portion of a node.

          @param  addr    Address of the node we care about
    */
    virtual const void* getUnhashedHeaderOf(node_address addr) const = 0;

    /** Read the hashed header portion of a node.

          @param  addr    Address of the node we care about
    */
    virtual const void* getHashedHeaderOf(node_address addr) const = 0;

    // --------------------------------------------------
    // incoming count data
    // --------------------------------------------------

    /// Get the number of incoming pointers to a node.
    node_handle getCountOf(node_address addr) const;
    /// Set the number of incoming pointers to a node.
    void setCountOf(node_address addr, node_handle c);
    /// Increment (and return) the number of incoming pointers to a node.
    node_handle incCountOf(node_address addr);
    /// Decrement (and return) the number of incoming pointers to a node.
    node_handle decCountOf(node_address addr);

    // --------------------------------------------------
    // next pointer data
    // --------------------------------------------------

    node_handle getNextOf(node_address addr) const;
    void setNextOf(node_address addr, node_handle n);

  protected:
    /// Dump information not related to individual nodes.
    virtual void dumpInternalInfo(FILE*) const = 0;

    /** Dump the node/hole information at the given address.
          @param  s       Output stream to use
          @param  addr    Address
          @param  flags   What chunks should be displayed

          @return   Next interesting address.
    */
    virtual node_address dumpInternalNode(FILE* s, node_address addr,
                                          unsigned flags) const = 0;

    /// Dump final info (after node info)
    virtual void dumpInternalTail(FILE*) const = 0;

    // Hooks from other classes, so we don't need to make
    // all the derived classes "friends".

    void moveNodeOffset(node_handle node, node_address old_addr,
                        node_address new_addr);

    static void resize_header(node_reader& nr, int extra_slots);
    static void* extra_hashed(node_reader& nr);
    static node_handle* down_of(node_reader& nr);
    static int* index_of(node_reader& nr);
    static char* edge_of(node_reader& nr);
    static int& nnzs_of(node_reader& nr);

    //
    // Methods for derived classes to deal with
    // members owned by the base class
    //

    const expert_forest* getParent() const;
    expert_forest* getParent();

    void incMemUsed(long delta);
    void decMemUsed(long delta);
    void incMemAlloc(long delta);
    void decMemAlloc(long delta);
    void incCompactions();
    void updateCountArray(node_handle* cptr);
    void updateNextArray(node_handle* nptr);

    /** Initialization hook for derived classes.
        Allowes derived classes to initialize themselves
        for a particular forest.
        Called by initForForest().
        Default behavior is to do nothing.
    */
    virtual void localInitForForest(const expert_forest* f);

    //
    // Hooks for hole managers
    //

    // Change the data array
    virtual void updateData(node_handle* data) = 0;

    // How small can a node be?
    virtual int smallestNode() const = 0;

    friend class holeman;

  private:
    /// Parent forest.
    expert_forest* parent;

    /// Memory stats
    forest::statset* stats;

    /// Count array, so that counts[addr] gives the count for node at addr.
    node_handle* counts;

    /// Next array, so that nexts[addr] gives the next value for node at addr.
    node_handle* nexts;
};


// ******************************************************************
// *                                                                *
// *                                                                *
// *                      expert_forest  class                      *
// *                                                                *
// *                                                                *
// ******************************************************************

class MEDDLY::expert_forest: public forest
{
    // flags for reporting; DO NOT rely on specific values
  public:
    /// Should memory be reported in a human readable format
    static const unsigned HUMAN_READABLE_MEMORY;
    /// Basic forest stats
    static const unsigned BASIC_STATS;
    static const unsigned EXTRA_STATS;
    /// Specific forest stats, dependent on forest type
    static const unsigned FOREST_STATS;
    /// Stats specific to the node storage mechanism.
    static const unsigned STORAGE_STATS;
    /// Detailed stats for the node storage mechanism.
    static const unsigned STORAGE_DETAILED;
    /// Stats specific to the unique table.
    static const unsigned UNIQUE_TABLE_STATS;
    /// Stats specific to the unique table.
    static const unsigned UNIQUE_TABLE_DETAILED;
    /// Stats specific to the hole manager.
    static const unsigned HOLE_MANAGER_STATS;
    /// Stats specific to the hole manager.
    static const unsigned HOLE_MANAGER_DETAILED;

    // preferred way to encode and decode terminal values
    // (classes so we can use them in template functions)

    /** Encoding for booleans into (terminal) node handles */
    class bool_Tencoder {
        // -1 true
        //  0 false
      public:
        static node_handle value2handle(bool v);
        static bool handle2value(node_handle h);
        static void show(FILE* s, node_handle h);
        static void write(FILE* s, node_handle h);
        static node_handle read(FILE* s);
    };
    /** Encoding for integers into (terminal) node handles */
    class int_Tencoder {
      public:
        static node_handle value2handle(int v);
        static int handle2value(node_handle h);
        static void show(FILE* s, node_handle h);
        static void write(FILE* s, node_handle h);
        static node_handle read(FILE* s);
    };
    /** Encoding for floats into (terminal) node handles */
    class float_Tencoder {
        union intfloat {
            float real;
            int integer;
        };
      public:
        static node_handle value2handle(float v);
        static float handle2value(node_handle h);
        static void show(FILE* s, node_handle h);
        static void write(FILE* s, node_handle h);
        static node_handle read(FILE* s);
    };
    // preferred way to encode and decode edge values
    // (classes so we can use them in template functions)
    /** Encoding for ints into (edge value) node handles */
    class int_EVencoder {
      public:
        static size_t edgeBytes();
        static void writeValue(void* ptr, int val);
        static void readValue(const void* ptr, int &val);
        static void show(FILE* s, const void* ptr);
        static void write(FILE* s, const void* ptr);
        static void read(FILE* s, void* ptr);
    };
    /** Encoding for floats into (edge value) node handles */
    class float_EVencoder {
      public:
        static size_t edgeBytes();
        static void writeValue(void* ptr, float val);
        static void readValue(const void* ptr, float &val);
        static void show(FILE* s, const void* ptr);
        static void write(FILE* s, const void* ptr);
        static void read(FILE* s, void* ptr);
    };
    /** Constructor.
      @param  dslot   slot used to store the forest, in the domain
      @param  d       domain to which this forest belongs to.
      @param  rel     does this forest represent a relation.
      @param  t       the range of the functions represented in this forest.
      @param  ev      edge annotation.
      @param  p       Polcies for reduction, storage, deletion.
    */
    expert_forest(int dslot, domain *d, bool rel, range_type t,
                  edge_labeling ev, const policies &p);

  // ------------------------------------------------------------
  // inlined helpers.

    /**
        Convenience function.
        Based on the forest type, convert the desired value
        into a terminal node handle.
          @param  v   Value to encode
          @return     Handle for terminal node
    */
    template <typename T>
    node_handle handleForValue(T v) const;

    /**
        Convenience function.
        Based on the forest type, convert the terminal node handle
        into its encoded value.
          @param  n   Node handle
          @param  v   Output: encoded value
    */
    template <typename T>
    void getValueFromHandle(node_handle n, T& v) const;

    /**
        Convenience function.
        Based on the forest type, convert the terminal node handle
        into its encoded boolean value.
          @param  n   Node handle
    */
    bool getBooleanFromHandle(node_handle n) const;

    /**
        Convenience function.
        Based on the forest type, convert the terminal node handle
        into its encoded integer value.
          @param  n   Node handle
    */
    int getIntegerFromHandle(node_handle n) const;

    /**
        Convenience function.
        Based on the forest type, convert the terminal node handle
        into its encoded real (float) value.
          @param  n   Node handle
    */
    float getRealFromHandle(node_handle n) const;

    statset& changeStats();
    /// Number of bytes for an edge value.
    char edgeBytes() const;
    /// Are edge values included when computing the hash.
    bool areEdgeValuesHashed() const;
    /// Extra bytes per node, not hashed.
    char unhashedHeaderBytes() const;
    /// Extra bytes per node, hashed.
    char hashedHeaderBytes() const;

    const expert_domain* getExpertDomain() const;
    expert_domain* useExpertDomain();
    /// Ignores prime/unprime.
    int getNumVariables() const;
    int getMinLevelIndex() const;
    bool isValidLevel(int k) const;

    /// The maximum size (number of indices) a node at this level can have
    int getLevelSize(int lh) const;
    /// Is this a terminal node?
    static bool isTerminalNode(node_handle p);
    /// Sanity check: is this a valid nonterminal node index.
    bool isValidNonterminalIndex(node_handle node) const;
    /// Sanity check: is this a valid node index.
    bool isValidNodeIndex(node_handle node) const;
    node_handle getLastNode() const;
    /// Get data for a given nonterminal node.
    const node_header& getNode(node_handle p) const;
    /** Get the node's level as an integer.
        Negative values are used for primed levels.
    */
    int getNodeLevel(node_handle p) const;
    bool isPrimedNode(node_handle p) const;
    bool isUnprimedNode(node_handle p) const;

    /// Get the cardinality of an Index Set.
    int getIndexSetCardinality(node_handle node) const;

    // --------------------------------------------------
    // Used by the unique table
    // --------------------------------------------------
    node_handle getNext(node_handle p) const;
    void setNext(node_handle p, node_handle n);
    unsigned hash(node_handle p) const;

    // --------------------------------------------------
    // Managing reference counts
    // --------------------------------------------------

    /// Returns the in-count for a node.
    long readInCount(node_handle p) const;

    /** Increase the link count to this node. Call this when another node is
        made to point to this node.
          @return p, for convenience.
    */
    node_handle linkNode(node_handle p);

    /** Decrease the link count to this node. If link count reduces to 0, this
        node may get marked for deletion. Call this when another node releases
        its connection to this node.
    */
    void unlinkNode(node_handle p);

    // --------------------------------------------------
    // Managing cache entries
    // --------------------------------------------------

    /** Increase the cache count for this node. Call this whenever this node
        is added to a cache.
          @param  p     Node we care about.
          @return p, for convenience.
    */
    node_handle cacheNode(node_handle p);

    /** Increase the cache count for this node. Call this whenever this node
        is added to a cache.
          @param  p     Node we care about.
    */
    void uncacheNode(node_handle p);

    /// A node can be discarded once it goes stale. Whether a node is
    /// considered stale depends on the forest's deletion policy.
    /// Optimistic deletion: A node is said to be stale only when both the
    ///   in-count and cache-count are zero.
    /// Pessimistic deletion: A node is said to be stale when the in-count
    ///  is zero regardless of the cache-count.
    bool isStale(node_handle node) const;

  // ------------------------------------------------------------
  // non-virtual, handy methods for debugging or logging.

    void dump(FILE *s) const;
    void dumpInternal(FILE *s) const;
    void dumpUniqueTable(FILE *s) const;
    void validateIncounts(bool exact);
    void countNodesByLevel(long* active) const;


  // ------------------------------------------------------------
  // non-virtual, handy methods.

    /** Build a list of nodes in the subgraph below the given node.
        This for example is used to determine which nodes must
        be printed to display a subgraph.
        Terminal nodes are NOT included.

          @param  roots   Array of root nodes in the forest.
                          Each root node will be included in the list,
                          except for terminal nodes.

          @param  N       Dimension of \a roots array.

          @param  sort    If true, the list will be in increasing order.
                          Otherwise, the list will be in some convenient order
                          (currently, it is the order that nodes
                          are discovered).

          @return   A malloc'd array of non-terminal nodes, terminated by 0.
                    Or, a null pointer, if the list is empty.
    */
    node_handle*
    markNodesInSubgraph(const node_handle* roots, int N, bool sort) const;

    /** Count and return the number of non-terminal nodes
        in the subgraph below the given node.
    */
    long getNodeCount(node_handle node) const;

    /** Count and return the number of edges
        in the subgraph below the given node.
    */
    long getEdgeCount(node_handle node, bool countZeroes) const;

    /// Display the contents of a single node.
    void showNode(FILE* s, node_handle node, int verbose = 0) const;

    /// Show all the nodes in the subgraph below the given nodes.
    void showNodeGraph(FILE* s, const node_handle* node, int n) const;


    /** Show various stats for this forest.
          @param  s       Output stream to write to
          @param  pad     Padding string, written at the start of
                          each output line.
          @param  flags   Which stats to display, as "flags";
                          use bitwise or to combine values.
                          For example, BASIC_STATS | FOREST_STATS.
    */
    void reportStats(FILE* s, const char* pad, unsigned flags) const;


    /// Compute a hash for a node.
    unsigned hashNode(node_handle p) const;

    /** Check and find the index of a single downward pointer.

          @param  node    Node we care about
          @param  down    Output:
                          The singleton downward pointer, or undefined.

          @return   If the node has only one non-zero downward pointer,
                    then return the index for that pointer.
                    Otherwise, return a negative value.
    */
    int getSingletonIndex(node_handle p, node_handle &down) const;

    /** Check and get a single downward pointer.

          @param  node    Node we care about
          @param  index   Index we're trying to match

          @return   If the only non-zero downward pointer for
                    this node happens at \a index, then return the pointer.
                    Otherwise, return 0.
    */
    node_handle getSingletonDown(node_handle node, int index) const;

    /** For a given node, get a specified downward pointer.

        This is designed to be used for one or two indexes only.
        For reading all or several downward pointers, a
        node_reader should be used instead.

          @param  p       Node to look at
          @param  index   Index of the pointer we want.

          @return         The downward pointer at that index.
    */
    node_handle getDownPtr(node_handle p, int index) const;

    /** For a given node, get a specified downward pointer.

        This is designed to be used for one or two indexes only.
        For reading all or several downward pointers, a
        node_reader should be used instead.

          @param  p       Node to look at
          @param  index   Index of the pointer we want.

          @param  ev      Output: edge value at that index.
          @param  dn      Output: downward pointer at that index.
    */
    void getDownPtr(node_handle p, int index, int& ev, node_handle& dn) const;

    /** For a given node, get a specified downward pointer.

        This is designed to be used for one or two indexes only.
        For reading all or several downward pointers, a
        node_reader should be used instead.

          @param  p       Node to look at
          @param  index   Index of the pointer we want.

          @param  ev      Output: edge value at that index.
          @param  dn      Output: downward pointer at that index.
    */
    void getDownPtr(node_handle p, int index, float& ev, node_handle& dn) const;

    node_handle getTransparentNode() const;

  // ------------------------------------------------------------
  // Preferred mechanism for reading nodes

    /** Initialize a node reader.
          @param  nr      Node reader to fill.
          @param  node    The node to use.
          @param  full    true:   Use a full reader.
                          false:  Use a sparse reader.
    */
    void initNodeReader(node_reader &nr, node_handle node, bool full) const;

    /// Allocate and initialize a node reader.
    node_reader* initNodeReader(node_handle node, bool full) const;

    /** Initialize a redundant node reader.
        Use for multi-terminal forests.
        For convenience.
          @param  nr      Node reader to fill.
          @param  k       Level that was skipped.
          @param  node    Downward pointer to use.
          @param  full    Use a full reader or sparse.
    */
    void initRedundantReader(node_reader &nr, int k, node_handle node, bool full) const;

    /// Allocate and initialize a redundant node reader.
    node_reader* initRedundantReader(int k, node_handle node, bool full) const;

    /** Initialize a redundant node reader.
        Use for edge-valued forests, whose edge values
        require a single integer slot.
        For convenience.
          @param  nr      Node reader to fill.
          @param  k       Level that was skipped.
          @param  ev      Edge value to use.
          @param  node    Downward pointer to use.
          @param  full    Use a full reader or sparse.
    */
    void initRedundantReader(node_reader &nr, int k, int ev, node_handle node,
      bool full) const;

    /// Allocate and initialize a redundant node reader.
    node_reader*
    initRedundantReader(int k, int ev, node_handle nd, bool full) const;

    /** Initialize a redundant node reader.
        Use for edge-valued forests, whose edge values
        require a single float slot.
        For convenience.
          @param  nr      Node reader to fill.
          @param  k       Level that was skipped.
          @param  ev      Edge value to use.
          @param  node    Downward pointer to use.
          @param  full    Use a full reader or sparse.
    */
    void initRedundantReader(node_reader &nr, int k, float ev,
      node_handle node, bool full) const;

    /// Allocate and initialize a redundant node reader.
    node_reader* initRedundantReader(int k, float ev,
      node_handle nd, bool full) const;

    /** Initialize an identity node reader.
        Use for multi-terminal forests.
        For convenience.
          @param  nr      Node reader to fill.
          @param  k       Level that was skipped.
          @param  i       Index of identity reduction
          @param  n       Downward pointer to use.
          @param  f       Use a full reader or sparse.
    */
    void initIdentityReader(node_reader &nr, int k, int i, node_handle n, bool f) const;

    /** Initialize an identity node reader.
        Use for edge-valued forests, whose edge values
        require a single integer slot.
        For convenience.
          @param  nr      Node reader to fill.
          @param  k       Level that was skipped.
          @param  i       Index of identity reduction
          @param  ev      Edge value.
          @param  n       Downward pointer to use.
          @param  f       Use a full reader or sparse.
    */
    void initIdentityReader(node_reader &nr, int k, int i, int ev, node_handle n, bool f) const;

    /// Allocate and initialize an identity node reader.
    node_reader* initIdentityReader(int k, int i, node_handle node,
      bool full) const;

    /// Allocate and initialize an identity node reader.
    node_reader* initIdentityReader(int k, int i, int ev, node_handle nd,
      bool full) const;

    /** Initialize an identity node reader.
        Use for edge-valued forests, whose edge values
        require a single float slot.
        For convenience.
          @param  nr      Node reader to fill.
          @param  k       Level that was skipped.
          @param  i       Index of identity reduction
          @param  ev      Edge value.
          @param  n       Downward pointer to use.
          @param  f       Use a full reader or sparse.
    */
    void initIdentityReader(node_reader &nr, int k, int i, float ev,
      node_handle n, bool f) const;

    /// Allocate and initialize an identity node reader.
    node_reader* initIdentityReader(int k, int i, float ev,
      node_handle nd, bool full) const;

  // ------------------------------------------------------------
  // Preferred mechanism for building nodes

    node_builder& useNodeBuilder(int level, int tsz);
    node_builder& useSparseBuilder(int level, int nnz);
    void doneNodeBuilder(node_builder& nb);

    /** Return a forest node equal to the one given.
        The node is constructed as necessary.
        This version should be used only for
        multi terminal forests.
        The node builder nb is recycled.
          @param  in    Incoming pointer index;
                        used for identity reductions.
          @param  nb    Constructed node.

          @return       A node handle equivalent
                        to nb, taking into account
                        the forest reduction rules
                        and if a duplicate node exists.
    */
    node_handle createReducedNode(int in, node_builder& nb);

    /** Return a forest node equal to the one given.
        The node is constructed as necessary.
        This version should be used only for
        edge valuded forests.
        The node builder nb is recycled.
          @param  in    Incoming pointer index;
                        used for identity reductions.
          @param  nb    Constructed node.
          @param  ev    Output: edge value
          @param  node  Output: node handle.
                        On exit, the edge value and the node
                        handle together are equivalent to nb;
                        taking into account the forest reduction rules
                        and if a duplicate node exists.
    */
    template <class T>
    void createReducedNode(int in, node_builder& nb, T& ev, node_handle& node);

  // ------------------------------------------------------------
  // virtual in the base class, but implemented here.
  // See meddly.h for descriptions of these methods.

    virtual void writeEdges(FILE* s, const dd_edge* E, int n) const;
    virtual void readEdges(FILE* s, dd_edge* E, int n);
    virtual void garbageCollect();
    virtual void compactMemory();
    virtual void showInfo(FILE* strm, int verbosity);

  // ------------------------------------------------------------
  // abstract virtual, must be overridden.
  //

    /** Are the given edge values "duplicates".
        I.e., when determining if two nodes are duplicates,
        do the given edge values qualify as duplicate values.
          @param  eva     Pointer to the first edge value
          @param  evb     Pointer to the second edge value

          @return     true, iff the edge values are "equal".
          @throws     A TYPE_MISMATCH error if the forest
                      does not store edge values.
    */
    virtual bool areEdgeValuesEqual(const void* eva, const void* evb) const;

    /** Discover duplicate nodes.
        Required for the unique table.
          @param  node    Handle to a node.
          @param  nb      Node we've just built.

          @return   true, iff the nodes are duplicates.
    */
    bool areDuplicates(node_handle node, const node_builder &nb) const;

    /** Discover duplicate nodes.
        Right now, used for sanity checks only.
          @param  node    Handle to a node.
          @param  nr      Some other node.

          @return   true, iff the nodes are duplicates.
    */
    bool areDuplicates(node_handle node, const node_reader &nr) const;

    /** Is this a redundant node that can be eliminated?
        Must be implemented in derived forests
        to deal with the default edge value.
          @param  nb    Node we're trying to build.

          @return   True, if nr is a redundant node
                          AND it should be eliminated.
    */
    virtual bool isRedundant(const node_builder &nb) const = 0;

    /** Is the specified edge an identity edge, that can be eliminated?
        Must be implemented in derived forests
        to deal with the default edge value.

          @param  nr    Node we're trying to build.
                        We know there is a single non-zero downward pointer.

          @param  i     Candidate edge (or edge index for sparse nodes).

          @return True, if nr[i] is an identity edge, and the
                        identity node should be eliminated.
    */
    virtual bool isIdentityEdge(const node_builder &nb, int i) const = 0;


    /**
        Build an iterator.
        Used by class enumerator.
    */
    virtual enumerator::iterator* makeFullIter() const = 0;

    /**
        Build an iterator with a fixed row.
        Default behavior - throw an "INVALID_FOREST" error.
    */
    virtual enumerator::iterator* makeFixedRowIter() const;

    /**
        Build an iterator with a fixed column.
        Default behavior - throw an "INVALID_FOREST" error.
    */
    virtual enumerator::iterator* makeFixedColumnIter() const;

    /** Show a terminal node.
          @param  s       Stream to write to.
          @param  tnode   Handle to a terminal node.
    */
    virtual void showTerminal(FILE* s, node_handle tnode) const;

    /** Write a terminal node in machine-readable format.
          @param  s       Stream to write to.
          @param  tnode   Handle to a terminal node.
    */
    virtual void writeTerminal(FILE* s, node_handle tnode) const;

    /** Read a terminal node in machine-readable format.
          @param  s       Stream to read from.
          @return         Handle to a terminal node.
          @throws         An invalid file exception if the stream does not
                          contain a valid terminal node.
    */
    virtual node_handle readTerminal(FILE* s);

    /** Show an edge value.
          @param  s       Stream to write to.
          @param  edge    Pointer to edge value chunk
    */
    virtual void showEdgeValue(FILE* s, const void* edge) const;

    /** Write an edge value in machine-readable format.
          @param  s       Stream to write to.
          @param  edge    Pointer to edge value chunk
    */
    virtual void writeEdgeValue(FILE* s, const void* edge) const;

    /** Read an edge value in machine-readable format.
          @param  s       Stream to read from.
          @param  edge    Pointer to edge value chunk
    */
    virtual void readEdgeValue(FILE* s, void* edge);

    /** Show the hashed header values.
          @param  s       Stream to write to.
          @param  hh      Pointer to hashed header data.
    */
    virtual void showHashedHeader(FILE* s, const void* hh) const;

    /** Write the hashed header in machine-readable format.
          @param  s       Stream to write to.
          @param  hh      Pointer to hashed header data.
    */
    virtual void writeHashedHeader(FILE* s, const void* hh) const;

    /** Read the hashed header in machine-readable format.
          @param  s       Stream to write to.
          @param  nb      Node we're building.
    */
    virtual void readHashedHeader(FILE* s, node_builder &nb) const;

    /** Show the unhashed header values.
          @param  s       Stream to write to.
          @param  uh      Array of all unhashed header values.
    */
    virtual void showUnhashedHeader(FILE* s, const void* uh) const;

    /** Write the unhashed header in machine-readable format.
          @param  s       Stream to write to.
          @param  hh      Pointer to unhashed header data.
    */
    virtual void writeUnhashedHeader(FILE* s, const void* uh) const;

    /** Read the unhashed header in machine-readable format.
          @param  s       Stream to write to.
          @param  nb      Node we're building.
    */
    virtual void readUnhashedHeader(FILE* s, node_builder &nb) const;



  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // |                                                                |
  // |                       protected  methods                       |
  // |                                                                |
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  protected:

    /// Destructor.
    virtual ~expert_forest();

    /** Initialize data.
        Should be called in the child class constructors.
        Allows us to use class properties to initialize the data.
    */
    void initializeForest();


  // ------------------------------------------------------------
  // inlined setters for derived classes to use.

    void setEdgeSize(char ebytes, bool hashed);
    void setUnhashedSize(char ubytes);
    void setHashedSize(char hbytes);

  // ------------------------------------------------------------
  // inlined helpers.

    bool isZombieNode(long p) const;
    bool isActiveNode(long p) const;
    bool isDeletedNode(long p) const;

  // ------------------------------------------------------------
  // virtual, with default implementation.
  // Should be overridden in appropriate derived classes.

    /// Character sequence used when writing forests to files.
    virtual const char* codeChars() const;

    /** Normalize a node.
        Used only for "edge valued" DDs with range type: integer.
        Different forest types will have different normalization rules,
        so the default behavior given here (throw an error) will need
        to be overridden by all edge-valued forests.

          @param  nb    Array of downward pointers and edge values;
                        may be modified.
          @param  ev    The incoming edge value, may be modified
                        as appropriate to normalize the node.
    */
    virtual void normalize(node_builder &nb, int& ev) const;

    /** Normalize a node.
        Used only for "edge valued" DDs with range type: real.
        Different forest types will have different normalization rules,
        so the default behavior given here (throw an error) will need
        to be overridden by all edge-valued forests.

          @param  nb    Array of downward pointers and edge values;
                        may be modified.
          @param  ev    The incoming edge value, may be modified
                        as appropriate to normalize the node.
    */
    virtual void normalize(node_builder &nb, float& ev) const;

    /** Show forest-specific stats.
          @param  s     Output stream to use
          @param  pad   String to display at the beginning of each line.
    */
    virtual void reportForestStats(FILE* s, const char* pad) const;



  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // |                                                                |
  // |                        private  methods                        |
  // |                                                                |
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  private:

  // ------------------------------------------------------------
  // inlined helpers for this class

    bool isTimeToGc() const;
    /// Returns the in-count for a node.
    long getInCount(node_handle p);
    /// Increment and return the in-count for a node
    long incInCount(node_handle p);
    /// Decrement and return the in-count for a node
    long decInCount(node_handle p);
    /// Returns the (modifiable) cache-count for a node
    int& cacheCount(node_handle p);
    /// Returns the cache-count for a node
    int getCacheCount(node_handle p) const;

    /** Change the location of a node.
        Used by node_storage during compaction.
        Should not be called by anything else.
          @param  node        Node we're moving
          @param  old_addr    Current address of node, for sanity check
          @param  new_addr    Where we're moving the node to
    */
    void moveNodeOffset(node_handle node, node_address old_addr, node_address new_addr);
    friend class MEDDLY::node_storage;


  // ------------------------------------------------------------
  // helpers for this class

    void handleNewOrphanNode(node_handle node);
    void deleteNode(node_handle p);
    void zombifyNode(node_handle p);

    /// Determine a node handle that we can use.
    node_handle getFreeNodeHandle();

    /// Release a node handle back to the free pool.
    void recycleNodeHandle(node_handle p);

    /** Apply reduction rule to the temporary node and finalize it. 
        Once a node is reduced, its contents cannot be modified.
          @param  in    Incoming index, used only for identity reduction;
                        Or -1.
          @param  nb    Array of downward pointers.
          @return       Handle to a node that encodes the same thing.
    */
    node_handle createReducedHelper(int in, const node_builder &nb);

    // Sanity check; used in development code.
    void validateDownPointers(const node_builder &nb) const;

    /// Increase the number of node handles.
    void expandHandleList();

    /// Decrease the number of node handles.
    void shrinkHandleList();

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // |                                                                |
  // |                              Data                              |
  // |                                                                |
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  protected:
    /// uniqueness table, still used by derived classes.
    unique_table* unique;

    /// Should a terminal node be considered a stale entry in the compute table.
    /// per-forest policy, derived classes may change as appropriate.
    bool terminalNodesAreStale;

    /// Class that stores nodes.
    node_storage* nodeMan;

    /// Transparent value
    node_handle transparent;

  private:
    // Keep a node_builder for each level.
    node_builder* raw_builders;
    node_builder* builders;

    // Garbage collection in progress
    bool performing_gc;

    // memory for validating incounts
    node_handle* in_validate;
    int  in_val_size;

    /// address info for nodes
    node_header *address;
    /// Size of address/next array.
    node_handle a_size;
    /// Last used address.
    node_handle a_last;
    /// Pointer to unused address lists, based on size
    node_handle a_unused[8];  // number of bytes per handle
    /// Lowest non-empty address list
    char a_lowest_index;
    /// Next time we shink the address list.
    node_handle a_next_shrink;


    /// Number of bytes for an edge
    char edge_bytes;
    /// Are edge values hashed?
    bool hash_edge_values;
    /// Number of bytes of unhashed header
    char unhashed_bytes;
    /// Number of bytes of hashed header
    char hashed_bytes;

    class nodecounter;
};
// end of expert_forest class.


// ******************************************************************
// *                                                                *
// *                expert_forest::nodecounter class                *
// *                                                                *
// ******************************************************************

class MEDDLY::expert_forest::nodecounter: public edge_visitor {
    expert_forest* parent;
    int* counts;
  public:
    nodecounter(expert_forest*p, int* c);
    virtual ~nodecounter();
    virtual void visit(dd_edge &e);
};


// ******************************************************************
// *                                                                *
// *                          opname class                          *
// *                                                                *
// ******************************************************************

/// Class for names of operations.
class MEDDLY::opname {
    const char* name;
    int index;
    static int next_index;

    friend void MEDDLY::initialize(const settings &);
    friend void MEDDLY::cleanup();
  public:
    opname(const char* n);
    virtual ~opname();

    int getIndex() const;
    const char* getName() const;
};

// ******************************************************************
// *                                                                *
// *                       unary_opname class                       *
// *                                                                *
// ******************************************************************

/// Unary operation names.
class MEDDLY::unary_opname : public opname {
  public:
    unary_opname(const char* n);
    virtual ~unary_opname();

    virtual unary_operation*
      buildOperation(expert_forest* arg, expert_forest* res) const;

    virtual unary_operation*
      buildOperation(expert_forest* arg, opnd_type res) const;
};

// ******************************************************************
// *                                                                *
// *                      binary_opname  class                      *
// *                                                                *
// ******************************************************************

/// Binary operation names.
class MEDDLY::binary_opname : public opname {
  public:
    binary_opname(const char* n);
    virtual ~binary_opname();

    virtual binary_operation* buildOperation(expert_forest* arg1,
      expert_forest* arg2, expert_forest* res) const = 0;
};


// ******************************************************************
// *                                                                *
// *                    specialized_opname class                    *
// *                                                                *
// ******************************************************************

/// Specialized operation names.
class MEDDLY::specialized_opname : public opname {
  public:
    /**
      Abstract base class for arguments to buildOperation().
      Derived operation names must provide derived classes for arguments.
    */
    class arguments {
      public:
        arguments();
        virtual ~arguments();

        /**
            Specify if arguments should be destroyed or not.
            If yes (the default), the operation will destroy
            the arguments once they are no longer needed.
        */
        void setAutoDestroy(bool destroy);
        bool autoDestroy() const;

      private:
        bool destroyWhenDone;
    };

    specialized_opname(const char* n);
    virtual ~specialized_opname();

    /** Note - unlike the more general binary and unary ops,
        a specialized operation might be crafted to the specific
        arguments (passed as an abstract class).
        Examples are:
          - operations that will be called several times with
            several of the arguments unchanged, and we want
            to do some preprocessing on the unchanged arguments.

          - operations with very bizarre and/or user-defined
            parameters (like on-the-fly saturation).

        @param  a   Arguments.  Will be destroyed when we are finished,
                    if autoDestroy() is set for the arguments.
    */
    virtual specialized_operation* buildOperation(arguments* a) const = 0;
};

// ******************************************************************
// *                                                                *
// *                     numerical_opname class                     *
// *                                                                *
// ******************************************************************

/// Numerical operation names.
class MEDDLY::numerical_opname : public specialized_opname {
  public:
    class numerical_args : public specialized_opname::arguments {
      public:
        const dd_edge &x_ind;
        const dd_edge &A;
        const dd_edge &y_ind;

        numerical_args(const dd_edge &xi, const dd_edge &a, const dd_edge &yi);
        virtual ~numerical_args();
    };

    numerical_opname(const char* n);
    virtual ~numerical_opname();
    virtual specialized_operation* buildOperation(arguments* a) const = 0;

    /// For convenience, and backward compatability :^)
    specialized_operation* buildOperation(const dd_edge &x_ind,
      const dd_edge &A, const dd_edge &y_ind) const;
};


// ******************************************************************
// *                                                                *
// *                     satpregen_opname class                     *
// *                                                                *
// ******************************************************************

/// Saturation, with already generated transition relations, operation names.
class MEDDLY::satpregen_opname : public specialized_opname {
  public:
    satpregen_opname(const char* n);
    virtual ~satpregen_opname();

    /// Arguments should have type "pregen_relation".
    virtual specialized_operation* buildOperation(arguments* a) const = 0;

    /** Class for a partitioned transition relation, already known
        The relation can be partitioned "by events" or "by levels".
        In the case of "by events", we can have more than one relation
        per level; otherwise, there is at most one relation per level.
    */
    class pregen_relation : public specialized_opname::arguments {
      public:
        /** Constructor, by events
              @param  inmdd       MDD forest containing initial states
              @param  mxd         MxD forest containing relations
              @param  outmdd      MDD forest containing result
              @param  num_events  Number of events; specifies the maximum
                                  number of calls to addToRelation().
        */
        pregen_relation(forest* inmdd, forest* mxd, forest* outmdd,
          int num_events);

        /** Constructor, by levels
              @param  inmdd       MDD forest containing initial states
              @param  mxd         MxD forest containing relations
              @param  outmdd      MDD forest containing result
        */
        pregen_relation(forest* inmdd, forest* mxd, forest* outmdd);

        virtual ~pregen_relation();
        void addToRelation(const dd_edge &r);

        // Options for controlling the amount of processing performed by
        // \a finalize(splittingOption).
        enum splittingOption {
          // None.
          None,
          // Transitions from level K that do not effect level K,
          // are moved to a lower level.
          SplitOnly,
          // SplitOnly + duplicated transitions between adjacent levels
          // are removed from the higher level.
          SplitSubtract,
          // SplitOnly + all duplicate transitions are removed.
          SplitSubtractAll,
          // Same as SplitSubtractAll, but using an algorithm that
          // first combines all transitions before splitting it up per level.
          MonolithicSplit
        };

        /** To be called after all events have been added to
            the transition relation.
            This method modifies the decision diagrams stored at different
            levels, to reduce duplicated transitions.
              @param  split       This parameter only applies to "by levels",
                                  and it controls the amount of processing
                                  that is performed.
                                  Please refer to splittingOption for details.
        */
        void finalize(splittingOption split = SplitSubtract);

        bool isFinalized() const;

        forest* getInForest() const;
        forest* getRelForest() const;
        forest* getOutForest() const;

        // the following methods assume the relation has been finalized.
        node_handle* arrayForLevel(int k) const;
        int lengthForLevel(int k) const;

      private:
        // helper for constructors
        void setForests(forest* inf, forest* mxd, forest* outf);
        // helper for finalize,
        // find intersection of diagonals of events[k],
        // subtracts the intersection of events[k] and adds it to events[k-1].
        void splitMxd(splittingOption split);
        // helper for finalize
        // adds all event[k]; sets all event[k] to 0;
        // sets events[level(sum)] = sum
        void unionLevels();

        forest* insetF;
        expert_forest* mxdF;
        forest* outsetF;
        int K;
        // array of sub-relations
        node_handle* events;
        // next pointers, unless we're finalized
        int* next;
        // size of events array
        int num_events;
        // last used element of events array
        int last_event;

        // If null, then we are "by levels".  Otherwise, we are "by events",
        // and before we're finalized, level_index[k] points to a linked-list
        // of sub-relations that affect level k.
        // after we're finalized, the events array is sorted, so
        // level_index[k] is the index of the first event affecting level k.
        // Dimension is number of variables + 1.
        int* level_index;
    };

};



// ******************************************************************
// *                                                                *
// *                      satotf_opname  class                      *
// *                                                                *
// ******************************************************************

/// Saturation, transition relations built on the fly, operation names.
class MEDDLY::satotf_opname : public specialized_opname {
  public:
    satotf_opname(const char* n);
    virtual ~satotf_opname();

    /// Arguments should have type "otf_relation", below
    virtual specialized_operation* buildOperation(arguments* a) const = 0;

  public:
    class otf_relation;

    // ============================================================

    /**
        Part of an enabling or updating function.
        It knows what variables it depends on, and how to build itself
        (provided by the user).
    */
    class subfunc {
      private:
        int* vars;
        int num_vars;
        dd_edge root;
      public:
        /// Constructor, specify variables that this function depends on.
        subfunc(int* v, int nv);
        virtual ~subfunc();

        /// Get number of variables we depend on
        inline int getNumVars() const {
          return num_vars;
        }

        /// Get array of variables we depend on
        inline const int* getVars() const {
          return vars;
        }

        /// Get the DD encoding of the current sub-function
        inline const dd_edge& getRoot() const {
          return root;
        }

        /**
          Update the sub-function, as local states are confirmed.
          User MUST provide this method, which should use setRoot()
          to update the DD encoding.
        */
        virtual void update(otf_relation &rel) = 0;

      protected:
        inline void setRoot(dd_edge &r) {
          root = r;
        }

    };

    // ============================================================

    /**
        An "event".
        Produces part of the transition relation, from its sub-functions.

        TBD - do we need to split the enabling and updating sub-functions,
        or will one giant list work fine?
    */
    class event {
      private:
        subfunc** pieces;
        int num_pieces;

        // TBD - put a list of events that have priority over this one

        // TBD - list of levels that we depend on - built from pieces

      public:
        event(subfunc** p, int np);
        virtual ~event();

        /// Get number of pieces
        inline int getNumPieces() const {
          return num_pieces;
        }

        /// Get array of subfuncs
        inline subfunc** getPieces() const {
          return pieces;
        }

        /**
            Rebuild the relation due to this event, from its sub-functions.
            Default assumes that we take the conjunction of all the parts;
            but users can override this behavior.

              @param  e   Relation is written to this edge.
        */
        virtual void rebuild(dd_edge &e);


        // TBD - for priority - when is this event enabled?

    };

    // ============================================================

    /**
        Overall relation.
        This includes all events, and keeping track of which local
        variables are confirmed.

        TBD.
    */
    class otf_relation {
      private:
        forest* insetF;
        forest* mxdF;
        forest* outsetF;
        int K;

        event** events;
        int num_events;

        //
        // For each level, list of pieces to update
        // if that level changes.
        subfunc** pieces;
        int* piecesForLevel;

        // TBD - for each level, list of events to update

      public:
        /** Constructor.
              @param  inmdd       MDD forest containing initial states
              @param  mxd         MxD forest containing relations
              @param  outmdd      MDD forest containing result
              @param  E           List of events
              @param  nE          Number of events
        */
        otf_relation(forest* inmdd, forest* mxd, forest* outmdd,
          event** E, int ne);

        virtual ~otf_relation();
    };

};

// ******************************************************************
// *                                                                *
// *                         ct_object class                        *
// *                                                                *
// ******************************************************************

/** Generic objects in compute tables.
    Used for things other than dd_edges and simple types.
    Defined in ops.cc
*/
class MEDDLY::ct_object {
  public:
    ct_object();
    virtual ~ct_object();
    virtual opnd_type getType() = 0;
};

// ******************************************************************
// *                                                                *
// *                    compute_table_style class                   *
// *                                                                *
// ******************************************************************

/** Interface for building compute tables.
*/
class MEDDLY::compute_table_style {
  protected:
    compute_table_style();
    virtual ~compute_table_style();

  public:
    /** Build a new, monolithic table.
        Monolithic means that the table stores entries for several
        (ideally, all) operations.

        Default throws an error.
    */
    virtual compute_table* create(const settings::computeTableSettings &s)
      const;


    /**
        Build a new table for a single operation.
        Default throws an error.
    */
    virtual compute_table* create(const settings::computeTableSettings &s,
      operation* op) const;


    /**
        Does this style build monolithic CTs?
    */
    virtual bool usesMonolithic() const = 0;
};

// ******************************************************************
// *                                                                *
// *                      compute_table  class                      *
// *                                                                *
// ******************************************************************

/** Interface for compute tables.
    Anyone implementing an operation (see below) will
    probably want to use this.
*/
class MEDDLY::compute_table {
    public:
      /// The maximum size of the hash table.
      unsigned maxSize;
      /// Do we try to eliminate stales during a "find" operation
      bool checkStalesOnFind;
      /// Do we try to eliminate stales during a "resize" operation
      bool checkStalesOnResize;

      struct stats {
        unsigned numEntries;
        unsigned hits;
        unsigned pings;
        static const int searchHistogramSize = 256;
        long searchHistogram[searchHistogramSize];
        long numLargeSearches;
        int maxSearchLength;
      };

      //
      // Something to search for in the CT.
      // This is an interface now!
      //
      class search_key {
          operation* op;

        protected:
          search_key(operation* op);

        public:
          /// Used for linked-list of recycled search keys in an operation.
          search_key* next;

          operation* getOp() const;
          virtual ~search_key();

          // interface, for operations
          virtual void reset() = 0;
          virtual void writeNH(node_handle nh) = 0;
          virtual void write(int i) = 0;
          virtual void write(float f) = 0;
      };

      //
      // Result of a search
      //
      class search_result {
          bool is_valid;

        protected:
          search_result();
          virtual ~search_result();

        public:
          void setValid();
          void setInvalid();
          operator bool() const;

          virtual node_handle readNH() = 0;
          virtual void read(int &i) = 0;
          virtual void read(float &f) = 0;
          virtual void read(long &l) = 0;
          virtual void read(double &d) = 0;
          virtual void read(void* &ptr) = 0;
      };

      //
      // Building a new CT entry.
      // This is an interface now!
      //
      class entry_builder {
        protected:
          entry_builder();
          virtual ~entry_builder();

        public:
          virtual void writeResultNH(node_handle) = 0;
          virtual void writeResult(int) = 0;
          virtual void writeResult(float) = 0;
          virtual void writeResult(long) = 0;
          virtual void writeResult(double) = 0;
          virtual void writeResult(void*) = 0;
      };

      // convenience methods, for grabbing edge values
      static void readEV(const node_handle* p, int &ev);
      static void readEV(const node_handle* p, float &ev);

      /// Constructor
      compute_table(const settings::computeTableSettings &s);

      /** Destructor.
          Does NOT properly discard all table entries;
          use \a removeAll() for this.
      */
      virtual ~compute_table();

      /// Is this a per-operation compute table?
      virtual bool isOperationTable() const = 0;

      /// Initialize a search key for a given operation.
      virtual search_key* initializeSearchKey(operation* op) = 0;

      /** Find an entry in the compute table based on the key provided.
          @param  key   Key to search for.
          @return       An appropriate search_result.
      */
      virtual search_result& find(search_key *key) = 0;

      /** Start a new compute table entry.
          The operation should "fill in" the values for the entry,
          then call \a addEntry().
      */
      virtual entry_builder& startNewEntry(search_key* k) = 0;

      /** Add the "current" new entry to the compute table.
          The entry may be specified by filling in the values
          for the struct returned by \a startNewEntry().
      */
      virtual void addEntry() = 0;

      /** Remove all stale entries.
          Scans the table for entries that are no longer valid (i.e. they are
          stale, according to operation::isEntryStale) and removes them. This
          can be a time-consuming process (proportional to the number of cached
          entries).
      */
      virtual void removeStales() = 0;

      /** Removes all entries.
      */
      virtual void removeAll() = 0;

      /// Get performance stats for the table.
      const stats& getStats();

      /// For debugging.
      virtual void show(FILE *s, int verbLevel = 0) = 0;

    protected:
      stats perf;
};

// ******************************************************************
// *                                                                *
// *                        operation  class                        *
// *                                                                *
// ******************************************************************

/** Generic operation.
    Operations are tied to specific forests.
    Necessary for compute table entries.
*/
class MEDDLY::operation {
    const opname* theOpName;
    bool is_marked_for_deletion;
    int oplist_index;
    int key_length;
    int ans_length;
    /// List of free search_keys
    compute_table::search_key* CT_free_keys;

    // declared and initialized in meddly.cc
    static compute_table* Monolithic_CT;
    // declared and initialized in meddly.cc
    static operation** op_list;
    // declared and initialized in meddly.cc
    static int* op_holes;
    // declared and initialized in meddly.cc
    static int list_size;
    // declared and initialized in meddly.cc
    static int list_alloc;
    // declared and initialized in meddly.cc
    static int free_list;

    // should ONLY be called during library cleanup.
    static void destroyAllOps();

  protected:
    /// Compute table to use, if any.
    compute_table* CT;
    /// Struct for CT searches.
    // compute_table::search_key* CTsrch;
    // for cache of operations.
    operation* next;
    // must stale compute table hits be discarded.
    // if the result forest is using pessimistic deletion, then true.
    // otherwise, false.  MUST BE SET BY DERIVED CLASSES.
    bool discardStaleHits;

    virtual ~operation();
    void setAnswerForest(const expert_forest* f);
    void markForDeletion();
    void registerInForest(forest* f);
    void unregisterInForest(forest* f);
    virtual bool isStaleEntry(const node_handle* entry) = 0;
    compute_table::search_key* useCTkey();
    void allocEntryForests(int nf);
    void addEntryForest(int index, expert_forest* f);
    void allocEntryObjects(int no);
    void addEntryObject(int index);

    friend class forest;
    friend void MEDDLY::initialize(const settings &);
    friend void MEDDLY::destroyOpInternal(operation* op);
    friend void MEDDLY::cleanup();

  public:
    /// New constructor.
    /// @param  n   Operation "name"
    /// @param  kl  Key length of compute table entries.
    ///             Use 0 if this operation does not use the compute table.
    /// @param  al  Answer length of compute table entries.
    ///             Use 0 if this operation does not use the compute table.
    operation(const opname* n, int kl, int al);

    bool isMarkedForDeletion() const;
    void setNext(operation* n);
    operation* getNext();

    static bool usesMonolithicComputeTable();
    static void removeStalesFromMonolithic();

    /// Remove stale compute table entries for this operation.
    void removeStaleComputeTableEntries();

    /// Remove all compute table entries for this operation.
    void removeAllComputeTableEntries();

    // for compute tables.

    int getIndex() const;
    static operation* getOpWithIndex(int i);
    static int getOpListSize();

    // for debugging:

    static void showMonolithicComputeTable(FILE*, int verbLevel);
    static void showAllComputeTables(FILE*, int verbLevel);
    void showComputeTable(FILE*, int verbLevel) const;

    // handy
    const char* getName() const;
    const opname* getOpName() const;

    /// Number of ints that make up the key (usually the operands).
    int getKeyLength() const;

    /// Number of ints that make up the answer (usually the results).
    int getAnsLength() const;

    /// Number of ints that make up the entire record (key + answer)
    int getCacheEntryLength() const;

    /// Checks if the cache entry (in entryData[]) is stale.
    bool isEntryStale(const node_handle* data);

    void doneCTkey(compute_table::search_key* K);

    /// Removes the cache entry (in entryData[]) by informing the
    /// applicable forests that the nodes in this entry are being removed
    /// from the cache
    virtual void discardEntry(const node_handle* entryData) = 0;

    /// Prints a string representation of this cache entry on strm (stream).
    virtual void showEntry(FILE* strm, const node_handle *entryData) const = 0;

    bool shouldStaleCacheHitsBeDiscarded() const;
};

// ******************************************************************
// *                                                                *
// *                     unary_operation  class                     *
// *                                                                *
// ******************************************************************

/** Mechanism to apply a unary operation in a specific forest.
    Specific operations will be derived from this class.
*/
class MEDDLY::unary_operation : public operation {
  protected:
    expert_forest* argF;
    expert_forest* resF;
    opnd_type resultType;

    virtual ~unary_operation();

  public:
    unary_operation(const unary_opname* code, int kl, int al,
      expert_forest* arg, expert_forest* res);

    unary_operation(const unary_opname* code, int kl, int al,
      expert_forest* arg, opnd_type res);

    bool matches(const expert_forest* arg, const expert_forest* res)
      const;

    bool matches(const expert_forest* arg, opnd_type res) const;

    // high-level front-ends
    virtual void compute(const dd_edge &arg, dd_edge &res);
    virtual void compute(const dd_edge &arg, long &res);
    virtual void compute(const dd_edge &arg, double &res);
    virtual void compute(const dd_edge &arg, ct_object &c);

    // TBD: low-level front-ends?
    // e.g.,
    // virtual int computeDD(int k, int p);
    // virtual void computeEvDD(int k, int v, int p, int &w, int &q);
};

// ******************************************************************
// *                                                                *
// *                     binary_operation class                     *
// *                                                                *
// ******************************************************************

/** Mechanism to apply a binary operation in a specific forest.
    Specific operations will be derived from this class.
*/
class MEDDLY::binary_operation : public operation {
  protected:
    bool can_commute;
    expert_forest* arg1F;
    expert_forest* arg2F;
    expert_forest* resF;
    opnd_type resultType;

    virtual ~binary_operation();
    void operationCommutes();

  public:
    binary_operation(const binary_opname* code, int kl, int al,
      expert_forest* arg1, expert_forest* arg2, expert_forest* res);

    bool matches(const expert_forest* arg1, const expert_forest* arg2,
      const expert_forest* res) const;

    // high-level front-end
    virtual void compute(const dd_edge &ar1, const dd_edge &ar2, dd_edge &res)
      = 0;

    // low-level front ends

    /// Low-level compute on nodes a and b, return result.
    virtual node_handle compute(node_handle a, node_handle b);
    /// Low-level compute at level k on nodes a and b, return result.
    virtual node_handle compute(int k, node_handle a, node_handle b);

    /// Low-level compute on EV edges (av, ap) and (bv, bp), return result.
    virtual void compute(int av, node_handle ap, int bv, node_handle bp,
      int &cv, node_handle &cp);

    /// Low-level compute on EV edges (av, ap) and (bv, bp), return result.
    virtual void compute(float av, node_handle ap, float bv, node_handle bp,
      float &cv, node_handle &cp);

};

// ******************************************************************
// *                                                                *
// *                  specialized_operation  class                  *
// *                                                                *
// ******************************************************************

/** Mechanism to apply specialized operations.
*/
class MEDDLY::specialized_operation : public operation {
  public:
    specialized_operation(const specialized_opname* code, int kl, int al);
  protected:
    virtual ~specialized_operation();
  public:

    /** For unary (like) operations.
        Note that there could be other "built in" operands.
        Default behavior is to throw an exception.
    */
    virtual void compute(const dd_edge &arg, dd_edge &res);

    /** For binary (like) operations.
        Note that there could be other "built in" operands.
        Default behavior is to throw an exception.
    */
    virtual void compute(const dd_edge &ar1, const dd_edge &ar2, dd_edge &res);

    /** For numerical operations.
        compute y += some function of x, depending on the operation.
        Default behavior is to throw an exception.
    */
    virtual void compute(double* y, const double* x);
};

// ******************************************************************
// *                                                                *
// *                      op_initializer class                      *
// *                                                                *
// ******************************************************************

/** Preferred mechanism for users to initialize their own operations.
    Derive a class from this one, provide the \a execute method.
    Implementation in ops.cc
*/
class MEDDLY::op_initializer {
  unsigned refcount;
  op_initializer* before;

public:
  /// Constructor.
  ///   @param  bef   initializer(s) to execute before this one.
  op_initializer(op_initializer* bef);

  void initChain(const settings &s);
  void cleanupChain();

  static void recycle(op_initializer *I);
  static op_initializer* copy(op_initializer *I);

protected:
  virtual ~op_initializer();
  virtual void init(const settings &s) = 0;
  virtual void cleanup() = 0;
};


// ******************************************************************
// *                                                                *
// *                    cleanup_procedure  class                    *
// *                                                                *
// ******************************************************************

/** Mechanism for registering actions to occur at library cleanup time.
    This allows us to free certain memory when the library is closed.
    Derive a class from this one, and provide the execute() method.
    Implementation in meddly.cc
*/
class MEDDLY::cleanup_procedure {
    static cleanup_procedure* list;
    cleanup_procedure* next;
  public:
    cleanup_procedure();
  protected:
    virtual ~cleanup_procedure();
  public:
    virtual void execute() = 0;

    static void Initialize();
    static void ExecuteAll();
    static void DeleteAll();
};


#include "meddly_expert.hh"
#endif
