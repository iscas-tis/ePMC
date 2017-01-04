/**************************** CPPHeaderFile ***************************

* FileName [Node.h]

* PackageName [lang]

* Synopsis [Header file for Node class.]

* Description [This class encapsulates a node in the AST.]

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

#ifndef __NODE_H__
#define __NODE_H__

#include <string>

namespace lang {

//other classes needed
class DFSAdapter;

class Node
{
 public:
  //virtual destructor
  virtual ~Node() {}

  //get a string representation
  virtual std::string toString() const = 0;

  //used to walk a AST
  virtual void Apply(DFSAdapter &a) const = 0;

};

 inline std::ostream &operator<<(std::ostream &os, const Node &node) {
   os << node.toString();
   return os;
 }
 
} //namespace lang

/*********************************************************************/
//include headers for concrete ast classes
/*********************************************************************/
//#include "Stmt.h"
//#include "StdcAst.h"

#endif //__NODE_H__

/*********************************************************************/
//end of Node.h
/*********************************************************************/
