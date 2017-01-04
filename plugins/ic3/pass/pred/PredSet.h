/*! \file PredSet.h
 *  \brief Header file for Predicate class.
 * \note This class encapsulates a set of predicates.
 * @see Predicate.h
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

#ifndef __PREDSET_H__
#define __PREDSET_H__

namespace pred {

class Predicate;

/*! \brief this class encapsulates a set of predicates */
class PredSet
{
public:
	typedef CVC3::ExprHashMap<int> Table;

	/*! \brief constructor */
	PredSet();
	/*! \brief copy constructor */
	PredSet(const PredSet &rhs) { *this = rhs; }

	/*! \brief comparison */
	bool operator == (const PredSet &rhs) const;

	/*! \brief hash value */
	size_t hash() const;

	/*! \brief assignment */
	PredSet &operator = (const PredSet &rhs);
	/*! \brief string output */
	virtual std::string toString() const;

	enum ReturnCode {
		TRIVIAL,   // trivial, i.e. true or false, predicate
		CONTAINED, // exact same predicate is already contained
		COVERED,   // predicate is covered by existing predicates
		ADDED      // new predicate added
	};

	ReturnCode classifyExpr(const CVC3::Expr& e, bool expensive_checks = false) const;

	/*! \brief performs redundancy checks (redundancy = equivalence to boolean combination of existing pred) */
	virtual ReturnCode Add(const Predicate &p, bool expensive_checks = false);

	bool isRedundant(const CVC3::Expr &p, bool expensive_checks = false) const;

	/*! \brief return the index of a predicate within a predset
	 * \return if predicate found the index otherwise -1
	 */
	inline int find(const CVC3::Expr &p) const;
	/*! \brief return the index of a predicate within a predset
	 * \return if predicate found the index otherwise -1
	 */
	inline int find(const Predicate &p) const;


	/*! \brief access predicates within a PredSet by index
	 *  \pre The index exists. Can be checked with find
	*/
	inline const Predicate& operator[] (unsigned i) const { return preds[i]; };
	/*! \brief number of predicates in PredSet */
	unsigned size() const { return preds.size(); }
	/*! \brief emptiness check */
	bool empty() const { return preds.empty(); }
	/*! \brief return the vector Predicates in PredSet */
	const std::vector<Predicate> &getPreds() const { return preds; }
	/*! \brief merge together two PredSets avoiding redundancies (or not)
	 * @param rhs PredSet to merge with
	 * @param lazy do redundancy checks or just union
         */
	void Merge(const PredSet &rhs, bool lazy = false);
	/*! \brief are two PredSets disjoint ? */
	bool DisjointWith(const PredSet &lhs) const;
	/*! \brief empty a PredSet */
	virtual void clear();
	/*! destructor */
	virtual ~PredSet() {}

protected:
	std::vector<Predicate> preds;  //! the vector of predicates
	Table table; //! tabulated predicates for faster lookup

};

/*! \brief check if predicate is contained in PredSet
 * \return the position if successful otherwise return negative value
 */
inline int PredSet::find(const CVC3::Expr &e) const {
  Table::const_iterator i = table.find(e);
	if(i!=table.end()) return i->second;
	return -1;
}

/*! \brief check if predicate is contained in PredSet
 * \return the position if successful otherwise return negative value
 */
inline int PredSet::find(const Predicate &p) const {
	Table::const_iterator i = table.find(p.getExpr());
	if(i!=table.end()) return i->second;
	return -1;
}

}


#endif //__PREDSET_H__

/*********************************************************************/
//end of PredSet.h
/*********************************************************************/
