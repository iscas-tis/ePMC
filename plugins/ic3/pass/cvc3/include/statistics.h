/*****************************************************************************/
/*!
 * \file statistics.h
 * \brief Description: Counters and flags for collecting run-time statistics.
 * 
 * Author: Sergey Berezin
 * 
 * Created: Thu Jun  5 17:38:13 2003
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

#ifndef _cvc3__statistics_h
#define _cvc3__statistics_h

#include <string>
#include <iostream>
#include <sstream>
#include <map>

namespace CVC3 {

  class Statistics; // The main class, defined below

  // First, wrapper classes for flags and counters.  Later, we
  // overload some operators like '=', '++', etc. for those classes.

  // Boolean flag (can only be true or false)
  class StatFlag {
  private:
    bool* d_flag; // We don't own the pointer
  public:
    // Constructor: takes the pointer to the actual flag, normally
    // stored in class Statistics below.
    StatFlag(bool& flag) : d_flag(&flag) { }
    // Destructor
    ~StatFlag() { }
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
    StatFlag& operator=(bool x) { *d_flag=(x!=false); return *this; }
    // Comparisons
    friend bool operator==(const StatFlag& f1, const StatFlag& f2);
    friend bool operator!=(const StatFlag& f1, const StatFlag& f2);
    // Printing
    friend std::ostream& operator<<(std::ostream& os, const StatFlag& f);
  }; // end of class StatFlag

  inline bool operator==(const StatFlag& f1, const StatFlag& f2) {
    return (*f1.d_flag) == (*f2.d_flag);
  }
  inline bool operator!=(const StatFlag& f1, const StatFlag& f2) {
    return (*f1.d_flag) != (*f2.d_flag);
  }
  inline std::ostream& operator<<(std::ostream& os, const StatFlag& f) {
    if(*f.d_flag) return(os << "true");
    else return(os << "false");
  }

  // Integer counter.  Intended use is to count events (e.g. number of
  // function calls), but can be used to store any integer value
  // (e.g. size of some data structure)
  class StatCounter {
  private:
    int* d_counter; // We don't own the pointer
  public:
    // Constructor: takes the pointer to the actual counter, normally
    // stored in class Statistics below.
    StatCounter(int& c) : d_counter(&c) { }
    // Destructor
    ~StatCounter() { }
    // Auto-cast to int.  In particular, arithmetic comparisons like
    // <, >, <=, >= will work because of this.

    operator int() { return *d_counter; }

    // Auto-increment operators
    // Prefix versions:
    int operator--() { return --(*d_counter); }
    int operator++() { return ++(*d_counter); }
    // Postfix versions:
    int operator--(int) { return (*d_counter)--; }
    int operator++(int) { return (*d_counter)++; }
    // Can be assigned an integer or the value of another StatCounter
    StatCounter& operator=(int x) { *d_counter=x; return *this; }
    StatCounter& operator+=(int x) { *d_counter+=x; return *this; }
    StatCounter& operator-=(int x) { *d_counter-=x; return *this; }
    StatCounter& operator=(const StatCounter& x)
      { *d_counter=*x.d_counter; return *this; }
    StatCounter& operator-=(const StatCounter& x)
      { *d_counter-=*x.d_counter; return *this; }
    StatCounter& operator+=(const StatCounter& x)
      { *d_counter+=*x.d_counter; return *this; }
    // Comparisons to integers and other StatCounters
    friend bool operator==(const StatCounter& c1, const StatCounter& c2);
    friend bool operator!=(const StatCounter& c1, const StatCounter& c2);
    friend bool operator==(int c1, const StatCounter& c2);
    friend bool operator!=(int c1, const StatCounter& c2);
    friend bool operator==(const StatCounter& c1, int c2);
    friend bool operator!=(const StatCounter& c1, int c2);
    // Printing
    friend std::ostream& operator<<(std::ostream& os, const StatCounter& f);
  }; // end of class StatCounter

  inline bool operator==(const StatCounter& c1, const StatCounter& c2) {
    return (*c1.d_counter) == (*c2.d_counter);
  }
  inline bool operator!=(const StatCounter& c1, const StatCounter& c2) {
    return (*c1.d_counter) != (*c2.d_counter);
  }
  inline bool operator==(int c1, const StatCounter& c2) {
    return c1 == (*c2.d_counter);
  }
  inline bool operator!=(int c1, const StatCounter& c2) {
    return c1 != (*c2.d_counter);
  }
  inline bool operator==(const StatCounter& c1, int c2) {
    return (*c1.d_counter) == c2;
  }
  inline bool operator!=(const StatCounter& c1, int c2) {
    return (*c1.d_counter) != c2;
  }
  inline std::ostream& operator<<(std::ostream& os, const StatCounter& c) {
    return (os << *c.d_counter);
  }

  // class Statistics: the storage for all flags and counters

  class Statistics {
  private:
    // Output control
    std::ostream* d_os;
    typedef std::map<std::string, bool> StatFlagMap;
    typedef std::map<std::string, int> StatCounterMap;
    StatFlagMap d_flags;
    StatCounterMap d_counters;
  public:
    // Constructor
    Statistics() { }
    // Destructor (must destroy objects it d_timers)
    ~Statistics() { }
    // Accessing flags, counters, and timers by name.  If an object
    // doesn't exist, it is created and initialized to false or 0.
    StatFlag flag(const std::string& name)
      { return StatFlag(d_flags[name]); }
    StatCounter counter(const std::string& name)
      { return StatCounter(d_counters[name]); }

    // Print all the collected data
    std::ostream& printAll(std::ostream& os) const;
    friend std::ostream& operator<<(std::ostream& os,
				    const Statistics& stats) {
      return stats.printAll(os);
    }
  }; // end of class Statistics

} // end of namespace CVC3

#endif
