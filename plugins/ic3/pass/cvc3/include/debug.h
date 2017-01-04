/*****************************************************************************/
/*!
 * \file debug.h
 * \brief Description: Collection of debugging macros and functions.
 *
 * Author: Sergey Berezin
 *
 * Created: Thu Dec  5 13:12:59 2002
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

#ifndef _cvc3__debug_h
#define _cvc3__debug_h

#include <string>
#include <iostream>
#include <sstream>
#include <vector>
#include "os.h"
#include "exception.h"

/*! @brief If something goes horribly wrong, print a message and abort
  immediately with exit(1). */
/*! This macro stays even in the non-debug build, so the end users can
  send us meaningful bug reports. */

#define FatalAssert(cond, msg) if(!(cond)) \
 CVC3::fatalError(__FILE__, __LINE__, #cond, msg)

namespace CVC3 {
  //! Function for fatal exit.
  /*! It just exits with code 1, but is provided here for the debugger
   to set a breakpoint to.  For this reason, it is not inlined. */
  extern CVC_DLL void fatalError(const std::string& file, int line,
			 const std::string& cond, const std::string& msg);
}

#ifdef _CVC3_DEBUG_MODE

#include "compat_hash_map.h"
#include "compat_hash_set.h"

//! Any debugging code must be within IF_DEBUG(...)
#define IF_DEBUG(code) code

//! Print a value conditionally.
/*!  If 'cond' is true, print 'pre', then 'v', then 'post'.  The type
 of v must have overloaded operator<<.  It expects a ';' after it. */
#define DBG_PRINT(cond, pre, v, post) if(cond) CVC3::debugger.getOS() \
  << (pre) << (v) << (post) << std::endl

//! Print a message conditionally
#define DBG_PRINT_MSG(cond, msg) \
  if(cond) CVC3::debugger.getOS() << (msg) << std::endl

/*! @brief Same as DBG_PRINT, only takes a flag name instead of a
  general condition */
#define TRACE(flag, pre, v, post) \
  DBG_PRINT(CVC3::debugger.trace(flag), pre, v, post)

//! Same as TRACE, but for a simple message
#define TRACE_MSG(flag, msg) \
  DBG_PRINT_MSG(CVC3::debugger.trace(flag), msg)

//! Sanity check for debug build.  It disappears in the production code.
#define DebugAssert(cond, str) if(!(cond)) \
 CVC3::debugError(__FILE__, __LINE__, #cond, str)


namespace CVC3 {

  class Expr;
  //! Our exception to throw
  class DebugException: public Exception {
  public:
    // Constructor
    DebugException(const std::string& msg): Exception(msg) { }
    // Printing
    virtual std::string toString() const {
      return "Assertion violation " + d_msg;
    }
  }; // end of class DebugException

  //! Similar to fatalError to raise an exception when DebugAssert fires.
  /*! This does not necessarily cause the program to quit. */
  extern CVC_DLL void debugError(const std::string& file, int line,
			 const std::string& cond, const std::string& msg);

  // First, wrapper classes for flags, counters, and timers.  Later,
  // we overload some operators like '=', '++', etc. for those
  // classes.
  //! Boolean flag (can only be true or false)
  class DebugFlag {
  private:
    bool* d_flag; // We don't own the pointer
  public:
    // Constructor: takes the pointer to the actual flag, normally
    // stored in class Debug below.
    DebugFlag(bool& flag) : d_flag(&flag) { }
    // Destructor
    ~DebugFlag() { }
    // Auto-cast to boolean
    operator bool() { return *d_flag; }

    // Setting and resetting by ++ and --
    // Prefix versions:
    bool operator--() { *d_flag = false; return false; }
    bool operator++() { *d_flag = true; return true; }
    // Postfix versions:
    bool operator--(int) { bool x=*d_flag; *d_flag=false; return x; }
    bool operator++(int) { bool x=*d_flag; *d_flag=true; return x; }
    // Can be assigned only a boolean value
    DebugFlag& operator=(bool x) { *d_flag=(x!=false); return *this; }
    // Comparisons
    friend bool operator==(const DebugFlag& f1, const DebugFlag& f2);
    friend bool operator!=(const DebugFlag& f1, const DebugFlag& f2);
    // Printing
    friend std::ostream& operator<<(std::ostream& os, const DebugFlag& f);
  }; // end of class DebugFlag

  //! Checks if the *values* of the flags are equal
  inline bool operator==(const DebugFlag& f1, const DebugFlag& f2) {
    return (*f1.d_flag) == (*f2.d_flag);
  }
  //! Checks if the *values* of the flags are disequal
  inline bool operator!=(const DebugFlag& f1, const DebugFlag& f2) {
    return (*f1.d_flag) != (*f2.d_flag);
  }
  //! Printing flags
  inline std::ostream& operator<<(std::ostream& os, const DebugFlag& f) {
    if(*f.d_flag) return(os << "true");
    else return(os << "false");
  }

  //! Integer counter for debugging purposes.
  /*! Intended use is to count events (e.g. number of function calls),
    but can be used to store any integer value (e.g. size of some data
    structure) */
  class DebugCounter {
  private:
    int* d_counter; //!< We don't own the pointer
  public:
    //! Constructor
    /*!  Takes the pointer to the actual counter, normally stored in
      class Debug below. */
    DebugCounter(int& c) : d_counter(&c) { }
    //! Destructor
    ~DebugCounter() { }
    //! Auto-cast to int.
    /*! In particular, arithmetic comparisons like <, >, <=, >= will
      work because of this. */
    operator int() { return *d_counter; }

    // Auto-increment operators

    //! Prefix auto-decrement
    int operator--() { return --(*d_counter); }
    //! Prefix auto-increment
    int operator++() { return ++(*d_counter); }
    //! Postfix auto-decrement
    int operator--(int) { return (*d_counter)--; }
    //! Postfix auto-increment
    int operator++(int) { return (*d_counter)++; }
    //! Value assignment.
    DebugCounter& operator=(int x) { *d_counter=x; return *this; }
    DebugCounter& operator+=(int x) { *d_counter+=x; return *this; }
    DebugCounter& operator-=(int x) { *d_counter-=x; return *this; }
    //! Assignment from another counter.
    /*! It copies the value, not the pointer */
    DebugCounter& operator=(const DebugCounter& x)
      { *d_counter=*x.d_counter; return *this; }
    /*! It copies the value, not the pointer */
    DebugCounter& operator-=(const DebugCounter& x)
      { *d_counter-=*x.d_counter; return *this; }
    /*! It copies the value, not the pointer */
    DebugCounter& operator+=(const DebugCounter& x)
      { *d_counter+=*x.d_counter; return *this; }
    // Comparisons to integers and other DebugCounters
    friend bool operator==(const DebugCounter& c1, const DebugCounter& c2);
    friend bool operator!=(const DebugCounter& c1, const DebugCounter& c2);
    friend bool operator==(int c1, const DebugCounter& c2);
    friend bool operator!=(int c1, const DebugCounter& c2);
    friend bool operator==(const DebugCounter& c1, int c2);
    friend bool operator!=(const DebugCounter& c1, int c2);
    //! Printing counters
    friend std::ostream& operator<<(std::ostream& os, const DebugCounter& f);
  }; // end of class DebugCounter

  inline bool operator==(const DebugCounter& c1, const DebugCounter& c2) {
    return (*c1.d_counter) == (*c2.d_counter);
  }
  inline bool operator!=(const DebugCounter& c1, const DebugCounter& c2) {
    return (*c1.d_counter) != (*c2.d_counter);
  }
  inline bool operator==(int c1, const DebugCounter& c2) {
    return c1 == (*c2.d_counter);
  }
  inline bool operator!=(int c1, const DebugCounter& c2) {
    return c1 != (*c2.d_counter);
  }
  inline bool operator==(const DebugCounter& c1, int c2) {
    return (*c1.d_counter) == c2;
  }
  inline bool operator!=(const DebugCounter& c1, int c2) {
    return (*c1.d_counter) != c2;
  }
  inline std::ostream& operator<<(std::ostream& os, const DebugCounter& c) {
    return (os << *c.d_counter);
  }

  //! A class holding the time value.
  /*! What exactly is time is not exposed.  It can be the system's
    struct timeval, or it can be the (subset of the) user/system/real
    time tuple. */
  class DebugTime;

  //! Time counter.
  /*! Intended use is to store time intervals or accumulated time for
    multiple events (e.g. time spent to execute certain lines of code,
    or accumulated time spent in a particular function). */
  class CVC_DLL DebugTimer {
  private:
    DebugTime* d_time; //!< The time value
    bool d_clean_time; //!< Set if we own *d_time
  public:
    //! Constructor: takes the pointer to the actual time value.
    /*! It is either stored in class Debug below (then the timer is
      "public"), or we own it, making the timer "private". */
    DebugTimer(DebugTime* time, bool take_time = false)
      : d_time(time), d_clean_time(take_time) { }
    /*! @brief Copy constructor: copy the *pointer* from public
      timers, and *value* from private.  */
    /*! The reason for different behavior for public and private time
      is the following.  When you modify a public timer, you want the
      changes to show in the central database and everywhere else,
      whereas private timers are used as independent temporary
      variables holding intermediate time values. */
    DebugTimer(const DebugTimer& timer);
    //! Assignment: same logistics as for the copy constructor
    DebugTimer& operator=(const DebugTimer& timer);

    //! Destructor
    ~DebugTimer();

    // Operators
    //! Set time to zero
    void reset();
    DebugTimer& operator+=(const DebugTimer& timer);
    DebugTimer& operator-=(const DebugTimer& timer);
    //! Produces new "private" timer
    DebugTimer operator+(const DebugTimer& timer);
    //! Produces new "private" timer
    DebugTimer operator-(const DebugTimer& timer);

    // Our friends
    friend class Debug;
    // Comparisons
    friend bool operator==(const DebugTimer& t1, const DebugTimer& t2);
    friend bool operator!=(const DebugTimer& t1, const DebugTimer& t2);
    friend bool operator<(const DebugTimer& t1, const DebugTimer& t2);
    friend bool operator>(const DebugTimer& t1, const DebugTimer& t2);
    friend bool operator<=(const DebugTimer& t1, const DebugTimer& t2);
    friend bool operator>=(const DebugTimer& t1, const DebugTimer& t2);

    //! Print the timer's value
    friend std::ostream& operator<<(std::ostream& os, const DebugTimer& timer);
  }; // end of class DebugTimer

  //! The heart of the Bug Extermination Kingdom.
  /*! This class exposes many important components of the entire
    CVC-lite system for use in debugging, keeps all the flags,
    counters, and timers in the central database, and provides timing
    and printing functions. */

  class CVC_DLL Debug {
  private:
    //! Command line options for tracing; these override the TRACE command
    const std::vector<std::pair<std::string,bool> >* d_traceOptions;
    //! name of dump file
    const std::string* d_dumpName;
    // Output control
    std::ostream* d_os;
    // Stream for dumping trace to file ("dump-trace" option)
    std::ostream* d_osDumpTrace;
    //! Private hasher class for strings
    class stringHash {
    public:
      size_t operator()(const std::string& s) const {
	std::hash<char*> h;
	return h(s.c_str());
      }
    }; // end of stringHash
    // Hash tables for storing flags, counters, and timers
    typedef std::hash_map<std::string, bool, stringHash> FlagMap;
    typedef std::hash_map<std::string, int, stringHash> CounterMap;
    typedef std::hash_map<std::string, DebugTime*, stringHash> TimerMap;
    FlagMap d_flags;       //!< Set of flags
    FlagMap d_traceFlags;  //!< Set of trace flags
    CounterMap d_counters; //!< Set of counters
    /*! Note, that the d_timers map does *not* own the pointers; so
      the objects in d_timers must be destroyed explicitly in our
      destructor. */
    TimerMap d_timers;     //!< Set of timers

  public:
    //! Constructor
    Debug(): d_traceOptions(NULL), d_os(&std::cerr), d_osDumpTrace(NULL) { }
    //! Destructor (must destroy objects it d_timers)
    ~Debug();
    //! Must be called before Debug class can be safely used
    void init(const std::vector<std::pair<std::string,bool> >* traceOptions,
              const std::string* dumpName);
    //! Must be called before arguments supplied to init are deallocated
    void finalize();
    //! Accessing flags by name.
    /*! If a flag doesn't exist, it is created and initialized to
      false. */
    DebugFlag flag(const std::string& name)
      { return DebugFlag(d_flags[name]); }
    //! Accessing tracing flags by name.
    /*! If a flag doesn't exist, it is created and initialized to
      false. */
    DebugFlag traceFlag(const std::string& name)
      { return DebugFlag(d_traceFlags[name]); }
    //! Accessing tracing flag by char* name (mostly for GDB)
    DebugFlag traceFlag(const char* name);
    //! Set tracing of everything on (1) and off (0) [for use in GDB]
    void traceAll(bool enable = true);
    //! Accessing counters by name.
    /*! If a counter doesn't exist, it is created and initialized to 0. */
    DebugCounter counter(const std::string& name)
      { return DebugCounter(d_counters[name]); }
    //! Accessing timers by name.
    /*! If a timer doesn't exist, it is created and initialized to 0. */
    DebugTimer timer(const std::string& name);

    //! Check whether to print trace info for a particular flag.
    /*! Trace flags are the same DebugFlag objects, but live in a
      different namespace from the normal debug flags */
    bool trace(const std::string& name);

    // Timer functions

    //! Create a new "private" timer, initially set to 0.
    /*! The new timer will not be added to the set of timers, will not
     have a name, and will not be printed by 'printAll()'.  It is
     intended to be used to measure time intervals which are later
     added or assigned to the named timers. */
    static DebugTimer newTimer();

    //! Set the timer to the current time (whatever that means)
    void setCurrentTime(DebugTimer& timer);
    void setCurrentTime(const std::string& name) {
      DebugTimer t(timer(name));
      setCurrentTime(t);
    }
    /*! @brief Set the timer to the difference between current time
      and the time stored in the timer: timer = currentTime -
      timer. */
    /*! Intended to obtain the time interval since the last call to
      setCurrentTime() with that timer. */
    void setElapsed(DebugTimer& timer);

    //! Return the ostream used for debugging output
    std::ostream& getOS() { return *d_os; }
    //! Return the ostream for dumping trace
    std::ostream& getOSDumpTrace();

    //! Print an entry to the dump file
    void dumpTrace(const std::string& title,
		   const std::vector<std::pair<std::string,std::string> >&
		   fields);
    //! Set the debugging ostream
    void setOS(std::ostream& os) { d_os = &os; }

    //! Print all the collected data if "DEBUG" flag is set to 'os'
    void printAll(std::ostream& os);
    /*! @brief Print all the collected data if "DEBUG" flag is set to
      the default debug stream */
    void printAll() { printAll(*d_os); }

    // Generally useful functions
    //! Get the current scope level
    int scopeLevel();

  }; // end of class Debug

  extern CVC_DLL Debug debugger;

} // end of namespace CVC3

#else  // if _CVC3_DEBUG_MODE is not defined

// All debugging macros are empty here

#define IF_DEBUG(code)

#define DebugAssert(cond, str)

#define DBG_PRINT(cond, pre, v, post)
#define TRACE(cond, pre, v, post)

#define DBG_PRINT_MSG(cond, msg)
#define TRACE_MSG(flag, msg)

// to make the CLI wrapper happy
namespace CVC3 {
class DebugException: public Exception { };
}

#endif // _CVC3_DEBUG_MODE

#include "cvc_util.h"

#endif // _cvc3__debug_h
