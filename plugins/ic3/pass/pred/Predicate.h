/*! \file Predicate.h
 *  \brief Header file for Predicate class.
 * \note This class encapsulates a predicate. A predicate is a
 * collection of mutually disjoint pure C expressions.
 * @see Predicate.cpp
 *
 * \author Bjoern Wachter
 * \remarks Copyright (c) 2007 by Saarland University.  All
 * Rights Reserved. This software is for educational purposes only.
 * Permission is given to academic institutions to use, copy, and
 * modify this software and its documentation provided that this
 * introductory message is not removed, that this software and its
 * documentation is used for the institutions' internal research and
 * educational purposes, and that no monies are exchanged. No guarantee
 * is expressed or implied by the distribution of this code. Send
 * bug-reports and/or questions to: bwachter@cs.uni-sb.de.
 */

#ifndef __PREDICATE_H__
#define __PREDICATE_H__

namespace pred {

/*! \brief A predicate encapsulates a boolean expression and some dedicated checks to avoid redundancy */
class Predicate
{
public:
	static const int NONE;       //! not required to avoid CE
	static const int REQUIRED;   //! required
	static const int TENTATIVE;  //! right now all are tentative
	/*! \brief empty constructor */
	Predicate() { type = NONE; }
	/*! \brief constructor */
	Predicate(const CVC3::Expr &e,int t = TENTATIVE);
	/*! \brief copy constructor */
	Predicate(const Predicate &rhs);
	/*! \brief assignment operator */
	const Predicate &operator = (const Predicate &rhs);
	/*! \brief hash value */
	inline size_t hash() const { return e.hash(); }
	/*! \brief equality operator */
	inline bool operator == (const Predicate &rhs) const { return e == rhs.e; }
	inline bool operator != (const Predicate &rhs) const { return e != rhs.e; }
	/*! \brief less than operator */
	inline bool operator <  (const Predicate &rhs) const { return e < rhs.e; }
	/*! \brief negation operator */
	inline Predicate operator!() const { return Predicate(!e); }
	/*! \brief output predicate as a string */
	std::string toString() const;
	/*! \brief return the underlying boolean expression */
	inline const CVC3::Expr &getExpr() const { return e; }
	/*! \brief check if a given predicate rhs is already covered by this predicate
	 *
	 * This is a check to avoid redundancies like having a predicate and its negation in a predicate set.
	 */
	bool Merge(const Predicate &rhs) const;
	/*! \brief Check if a predicate contains a certain set of Lvalues  */
	bool ContainsLvalue(const std::vector<CVC3::Expr> &arg) const;
	/*! \brief return the reason why the predicate was added */
	int getType() const { return type; }
	/*! \brief set the type of a predicate in retrospect */
	void SetType(int t) { type = t; }
private:
	CVC3::Expr e;  //! the defining expression belonging to the predicate
	int type;      //! the type of the predicate - required or tentative
};

} //namespace pred

#endif //__PREDICATE_H__

/*********************************************************************/
//end of Predicate.h
/*********************************************************************/
