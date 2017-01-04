/*****************************************************************************/
/*!
 * \file command_line_flags.h
 *
 * Author: Sergey Berezin
 *
 * Created: Mon Feb 10 16:22:00 2003
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

#ifndef _cvc3__command_line_flags_h_
#define _cvc3__command_line_flags_h_

#include <sstream>
#include <cstring>
#include <vector>
#include <map>
#include "command_line_exception.h"
#include "debug.h"

namespace CVC3 {

  //! Different types of command line flags
  typedef enum {
    CLFLAG_NULL,
    CLFLAG_BOOL,
    CLFLAG_INT,
    CLFLAG_STRING,
    CLFLAG_STRVEC //!< Vector of pair<string, bool>
  } CLFlagType;

  /*!
    Class CLFlag (for Command Line Flag)

    Author: Sergey Berezin

    Date: Fri May 30 14:10:48 2003

    This class implements a data structure to hold a value of a single
    command line flag.
  */

class CLFlag {
 private:
  //! Type of the argument
  CLFlagType d_tp;
  //! The argument
  union {
    bool b;
    int i;
    std::string* s;
    std::vector<std::pair<std::string,bool> >* sv;
  } d_data;
  //! This tag is set to true when the flag is assigned a new value
  bool d_modified;
  //! Help string
  std::string d_help;
  //! Whether to display this flag when user invokes cvc3 -h
  bool d_display;
 public:
  //! Constructor for a boolean flag
  CLFlag(bool b, const std::string& help, bool display = true)
    : d_tp(CLFLAG_BOOL), d_modified(0), d_help(help), d_display(display)
    { d_data.b = b; }
  //! Constructor for an integer flag
  CLFlag(int i, const std::string& help, bool display = true)
    : d_tp(CLFLAG_INT), d_modified(0), d_help(help), d_display(display)
    { d_data.i = i; }
  //! Constructor for a string flag
  CLFlag(const std::string& s, const std::string& help, bool display = true)
    : d_tp(CLFLAG_STRING), d_modified(0), d_help(help), d_display(display) {
    d_data.s = new std::string(s);
  }
  //! Constructor for a string flag from char*
  CLFlag(const char* s, const std::string& help, bool display = true)
    : d_tp(CLFLAG_STRING), d_modified(0), d_help(help), d_display(display) {
    d_data.s = new std::string((char*)s);
  }
  //! Constructor for a vector flag
  CLFlag(const std::vector<std::pair<std::string,bool> >& sv,
	 const std::string& help, bool display = true)
    : d_tp(CLFLAG_STRVEC), d_modified(0), d_help(help), d_display(display) {
    d_data.sv = new std::vector<std::pair<std::string,bool> >(sv);
  }
  //! Default constructor
  CLFlag(): d_tp(CLFLAG_NULL), d_modified(0), d_help("Undefined flag"), d_display(false) { }
  //! Copy constructor
  CLFlag(const CLFlag& f)
    : d_tp(f.d_tp), d_modified(f.d_modified), d_help(f.d_help), d_display(f.d_display) {
    switch(d_tp) {
    case CLFLAG_STRING:
      d_data.s = new std::string(*f.d_data.s); break;
    case CLFLAG_STRVEC:
      d_data.sv = new std::vector<std::pair<std::string,bool> >(*f.d_data.sv); break;
    default: d_data = f.d_data;
    }
  }
  //! Destructor
  ~CLFlag() {
    switch(d_tp) {
    case CLFLAG_STRING: delete d_data.s; break;
    case CLFLAG_STRVEC: delete d_data.sv; break;
    default: break;// Nothing to do
    }
  }
  //! Assignment from another flag
  CLFlag& operator=(const CLFlag& f) {
    if(this == &f) return *this; // Self-assignment
    // Try to preserve the existing heap objects if possible
    if(d_tp == f.d_tp) {
      switch(d_tp) {
      case CLFLAG_STRING: *d_data.s = *f.d_data.s; break;
      case CLFLAG_STRVEC: *d_data.sv = *f.d_data.sv; break;
      default: d_data = f.d_data;
      }
    } else {
      switch(d_tp) {
      case CLFLAG_STRING: delete d_data.s; break;
      case CLFLAG_STRVEC: delete d_data.sv; break;
      default: break;
      }
      switch(f.d_tp) {
      case CLFLAG_STRING: d_data.s = new std::string(*f.d_data.s); break;
      case CLFLAG_STRVEC:
	d_data.sv=new std::vector<std::pair<std::string,bool> >(*f.d_data.sv);
	break;
      default: d_data = f.d_data;
      }
    }
    d_tp = f.d_tp;
    d_modified = f.d_modified;
    d_help = f.d_help;
    d_display = f.d_display;
    return *this;
  }
  //! Assignment of a boolean value
  /*! The flag must already have the right type */
  CLFlag& operator=(bool b) {
    DebugAssert(d_tp == CLFLAG_BOOL, "");
    d_data.b = b;
    d_modified = true;
    return *this;
  }
  //! Assignment of an integer value
  /*! The flag must already have the right type */
  CLFlag& operator=(int i) {
    DebugAssert(d_tp == CLFLAG_INT, "");
    d_data.i = i;
    d_modified = true;
    return *this;
  }
  //! Assignment of a string value
  /*! The flag must already have a string type. */
  CLFlag& operator=(const std::string& s) {
    DebugAssert(d_tp == CLFLAG_STRING, "");
    *d_data.s = s;
    d_modified = true;
    return *this;
  }
  //! Assignment of an string value from char*
  /*! The flag must already have a string type. */
  CLFlag& operator=(const char* s) {
    DebugAssert(d_tp == CLFLAG_STRING, "");
    *d_data.s = s;
    d_modified = true;
    return *this;
  }
  //! Assignment of a string value with a boolean tag to a vector flag
  /*! The flag must already have a vector type.  The pair of
    <string,bool> will be appended to the vector. */
  CLFlag& operator=(const std::pair<std::string,bool>& p) {
    DebugAssert(d_tp == CLFLAG_STRVEC, "");
    d_data.sv->push_back(p);
    d_modified = true;
    return *this;
  }
  //! Assignment of a vector value
  /*! The flag must already have a vector type. */
  CLFlag& operator=(const std::vector<std::pair<std::string,bool> >& sv) {
    DebugAssert(d_tp == CLFLAG_STRVEC, "");
    *d_data.sv = sv;
    d_modified = true;
    return *this;
  }
  // Accessor methods
  //! Return the type of the flag
  CLFlagType getType() const { return d_tp; }
  /*! @brief Return true if the flag was modified from the default
    value (e.g. set on the command line) */
  bool modified() const { return d_modified; }
  //! Return true if flag should be displayed in regular help
  bool display() const { return d_display; }

  // The value accessors return a reference.  For the system-wide
  // flags, this reference will remain valid throughout the run of the
  // program, even if the flag's value changes.  So, the reference can
  // be cached, and the value can be checked directly (which is more
  // efficient).
  const bool& getBool() const {
    DebugAssert(d_tp == CLFLAG_BOOL, "CLFlag::getBool: not a boolean flag");
    return d_data.b;
  }

  const int& getInt() const {
    DebugAssert(d_tp == CLFLAG_INT, "CLFlag::getInt: not an integer flag");
    return d_data.i;
  }

  const std::string& getString() const {
    DebugAssert(d_tp == CLFLAG_STRING,
		"CLFlag::getString: not a string flag");
    return *d_data.s;
  }

  const std::vector<std::pair<std::string,bool> >& getStrVec() const {
    DebugAssert(d_tp == CLFLAG_STRVEC,
		"CLFlag::getStrVec: not a string vector flag");
    return *d_data.sv;
  }

  const std::string& getHelp() const {
    return d_help;
  }

}; // end of class CLFlag

///////////////////////////////////////////////////////////////////////
// Class CLFlag (for Command Line Flag)
//
// Author: Sergey Berezin
// Date: Fri May 30 14:10:48 2003
//
// Database of command line flags.
///////////////////////////////////////////////////////////////////////

class CLFlags {
 private:
  typedef std::map<std::string, CLFlag> CharMap;
  CharMap d_map;

  // Private methods

  // Retrieve an existing flag for modification.  The 'name' must be a
  // full name of an existing flag.
  CLFlag& getFlag0(const std::string& name) {
    DebugAssert(d_map.count(name) > 0,
		"getFlag0("+name+"): there are no flags with this name");
    return (*d_map.find(name)).second;
  }
 public:
  // Public methods
  // Add a new flag.  The name must be a complete flag name.
  void addFlag(const std::string& name, const CLFlag& f) {
    d_map[name] = f;
  }
  // Count how many flags match the name prefix
  size_t countFlags(const std::string& name) const {
    size_t res(0), len(name.size());
    for(CharMap::const_iterator i=d_map.begin(), iend=d_map.end();
	i!=iend; ++i) {
      if(std::strncmp(name.c_str(), (*i).first.c_str(), len) == 0) res++;
    }
    return res;
  }
  // Match the name prefix and add all the matching names to the vector
  size_t countFlags(const std::string& name,
		    std::vector<std::string>& names) const {
    size_t res(0), len(name.size());
    for(CharMap::const_iterator i=d_map.begin(), iend=d_map.end();
	i!=iend; ++i) {
      if(std::strncmp(name.c_str(), (*i).first.c_str(), len) == 0) {
	names.push_back((*i).first);
	res++;
      }
    }
    return res;
  }
  // Retrieve an existing flag.  The 'name' must be a full name of an
  // existing flag.
  const CLFlag& getFlag(const std::string& name) const {
    DebugAssert(d_map.count(name) > 0,
		"getFlag("+name+"): there are no flags with this name");
    return (*d_map.find(name)).second;
  }

  const CLFlag& operator[](const std::string& name) const {
    return getFlag(name);
  }

  // Setting the flag to a new value, but preserving the help string.
  // The 'name' prefix must uniquely resolve to an existing flag.
  void setFlag(const std::string& name, const CLFlag& f) {
    CLFlag& oldF(getFlag0(name));
    DebugAssert(oldF.getType() == f.getType(),
		"setFlag("+name+"): flag type doesn't match");
    oldF = f;
  }

  // Variants of setFlag for all the types
  void setFlag(const std::string& name, bool b) { getFlag0(name) = b; }
  void setFlag(const std::string& name, int i) { getFlag0(name) = i; }
  void setFlag(const std::string& name, const std::string& s)
    { getFlag0(name) = s; }
  void setFlag(const std::string& name, const char* s)
    { getFlag0(name) = s; }
  void setFlag(const std::string& name, const std::pair<std::string,bool>& p)
    { getFlag0(name) = p; }
  void setFlag(const std::string& name,
	       const std::vector<std::pair<std::string,bool> >& sv)
    { getFlag0(name) = sv; }

}; // end of class CLFlags

} // end of namespace CVC3

#endif
