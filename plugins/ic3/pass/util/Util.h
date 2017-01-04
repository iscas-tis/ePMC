/**************************** CPPHeaderFile ***************************

* FileName [Util.h]

* PackageName [util]

* Synopsis [Header file for various utility routines.]

* SeeAlso []

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2007 by Saarland University.  All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#ifndef __UTIL_H__
#define __UTIL_H__

#include <cmath>
#include <algorithm>
#include <iostream>
#include <fstream>
#include <cstdio>
#include <cassert>
#include <string>
#include <list>
#include <set>
#include <map>
#include <vector>
#include <stack>
#include <typeinfo>
#include <valarray>

#include <boost/shared_ptr.hpp>
#include <boost/foreach.hpp>
/*modified by Li Yong -- 2015/4/17*/
#include <boost/version.hpp>
namespace boost {
    #if BOOST_VERSION != 104900
    namespace BOOST_FOREACH = foreach;
    #endif
}
/*modified by Li Yong -- 2015/4/17*/

#define foreach BOOST_FOREACH

#include <tr1/unordered_map>
#include <tr1/unordered_set>

namespace std {
template<class Key, class Entry>
    class unordered_map : public std::tr1::unordered_map<Key,Entry> {};
template<class Entry>
    class unordered_set : public std::tr1::unordered_set<Entry> {};

    namespace tr1 {

    template<> struct hash< std::pair<int,int> >
        {
            size_t operator()( const std::pair<int,int>& p ) const
            {
                size_t result = 0;
                result = p.first + (result << 6) + (result << 16) - result;
                result = p.second + (result << 6) + (result << 16) - result;
                return result;
            }
        };
    }
}


#ifndef WIN32
#include <sys/resource.h>
#endif //WIN32

#include "Timer.h"


#define MSG(level,args...) { if(util::Database::VERBOSITY_LEVEL>=(level)) util::Message(args); }

#define DBG(arg) if(util::Database::DEBUG) { arg; }



/*! \typedef true, false and don't care */
typedef enum { l_false=-1, l_undef, l_true } lbool;

/*!
\defgroup util Utilities: String conversions, stats and settings
\remarks defines useful things like string to int conversion,
error messages, a timer class Timer, a statistics class Statistics, and a database Database for storing settings.
\namespace util Utilities: String conversions, stats and settings
*/
namespace util {
    /*! \brief print an error message and quit
    \ingroup util
    */
    __attribute__ ((noreturn)) void Error(const char *format,...);
    /*! \brief print an error message and quit
    \ingroup util
    */
    __attribute__ ((noreturn)) void Error(std::string message);
    /*! \brief print a message
    \ingroup util
    */
    void Message(const char *format,...);

    /*! \brief print a message
    \ingroup util
    */
    void Message(std::string message);
    /*! \brief trim a string - remove intial and trailing whitespace
    \ingroup util
    */
    std::string TrimString(const std::string &s);
    /*! \brief convert an integer to a string
    \ingroup util
    */
    std::string intToString(int i);

    /*! \brief convert a string to an int
        \ingroup util
    */
    int stringToInt(const std::string& s);

    /*! \brief convert a string to a double
        \ingroup util
    */
    double stringToDouble(const std::string& s);


    /*! \brief convert a float to a string
    \ingroup util
    */
    std::string floatToString(float f);
    /*! \brief convert a float to a string
    \ingroup util
    */
    std::string doubleToString(double d);
    /*! \brief create a new temporary variable
    \ingroup util
    */
    std::string NewTempVar();
    /*! \brief check if sth is a temporary variable
    \ingroup util
    */
    bool IsTempVar(const std::string &);

    /*! \brief remove file extension from file name */
    std::string RemoveExtension(std::string s);
} //namespace util


bool approxEquality(double a, double b, double precision) ;

template <class C>
struct Signature {
    typedef std::vector<bool> boolVector;
    boolVector s;
    typedef std::set<C> CSet;
    CSet cset;

    static void Disjunction(boolVector& arg1, const boolVector& arg2) {
        for(unsigned i=0; i<arg1.size(); ++i) {
            arg1[i] = arg1[i] || arg2[i];
        }
    }
    static bool Implies(const boolVector& arg1, const boolVector& arg2) {
        bool result (true);
        for(unsigned i=0; result && i<arg1.size(); ++i) {
            result = result && (!arg1[i] || arg2[i]);
        }
        return result;
    }


    Signature(unsigned size) : s(size, false) { }
    Signature(const Signature<C>& sig) { *this = sig; }
    Signature<C>& operator=(const Signature<C>& sig) { s = sig.s; cset = sig.cset; return *this;}

    Signature(const boolVector& __s) : s(__s) {}

    static void partitionRefinement2(std::vector<Signature<C> >& partition,
                                     std::vector<Signature<C> >& new_partition ) {
        if(partition.size()==0) return;

        typedef std::list<boolVector*> List;

        List characteristic;

        unsigned N = partition[0].s.size();

        foreach(Signature<C>& sig, partition) {
            assert(sig.s.size() == N);
            characteristic.push_back(&sig.s);
        }


        for(unsigned i = 0; i<N; ++i) {
            List::iterator first (characteristic.end());
            for(List::iterator it = characteristic.begin() ; it!=characteristic.end() ; ) {
                assert((*it)->size() == N);
                if((**it)[i]) {
                    if(first == characteristic.end() ) {
                        first = it;
                        ++it;
                    } else {
                        List::iterator dit = it;
                        boolVector v(**first);
                        Disjunction(**first,**dit);
                        ++it;
                        characteristic.erase(dit);
                    }
                } else {
                    ++it;
                }
            }

        }
        new_partition.resize(characteristic.size(),N);

        unsigned i=0;
        foreach(boolVector* vec, characteristic) {
            new_partition[i].s = *vec;
            new_partition[i].cset.clear();
            ++i;
        }

        foreach(const Signature<C>& sig1, partition) {
            assert(sig1.cset.size()== 1);
            bool result = false;
            foreach(Signature<C>& sig2, new_partition) {
                assert(sig1.s.size() == N);
                assert(sig2.s.size() == N);
                if(Implies(sig1.s,sig2.s)) {
                    sig2.cset.insert(sig1.cset.begin(),sig1.cset.end());
                    result = true;
                    break;
                }
            }
            assert(result);
        }
    }

    inline void insert(const C& c) { cset.insert(c); }
/*
    static void partitionRefinement(std::vector<Signature<C>* >& partition) {
        if(partition.size()==0) return;

        unsigned N = partition[0]->s.size();
        for(unsigned i = 0; i<N; ++i) {
            std::vector<unsigned> positions;
            for(unsigned j = 0; j<partition.size(); ++j) {
                if((*partition[j]).s[i]) {
                    positions.push_back(j);
                }
            }
            merge(partition,positions);
        }
    }
*/

};

#endif //__UTIL_H__

/*********************************************************************/
//end of Util.h
/*********************************************************************/
