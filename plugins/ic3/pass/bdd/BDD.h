
/**************************** CPPHeaderFile ***************************

* FileName [BDD.h]

* PackageName [parser]

* Synopsis [interface to CUDD]

* Description []

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
#ifndef __BDD_H__
#define __BDD_H__


#include <vector>
#include "util/Cube.h"

extern "C" {
#include "bdd/cudd/include/util.h"
#include "bdd/cudd/include/cudd.h"
}

namespace bdd {

class BDD;
class MTBDD;

/*! \brief wrapper for DD manager */
class DDManager {
public:
	//! \brief constructor */
	DDManager(size_t nvars = 100);
	//! \brief destructor */
	~DDManager();
	//! \brief true */
	BDD True() const;
	//! \brief false */
	BDD False() const;
	//! \brief create a variable */
	BDD Variable(int i) const;
	MTBDD MTBDDVariable(int i) const;
	MTBDD Constant(double value) const;
	MTBDD BddToMtbdd(const BDD &) const;
	MTBDD Equals(const MTBDD &, double);
	MTBDD Interval(const MTBDD &, double, double);

	/*! \brief conjunction of a vector of BDDs */
	BDD And(const std::vector<BDD>&);


	MTBDD Encode(const std::vector<MTBDD>&,
			     long, double);
	BDD Encode(const std::vector<BDD>&, long);

	//! \brief nr of variables */
	unsigned getNrOfVariables() const;

	/* for interfacing with low-level code */
	/*! \brief return ddmgr */
	DdManager* getDdManager() const { return ddmgr; }

	//! \brief identity /*
	MTBDD MtbddIdentity(std::vector<MTBDD>, std::vector<MTBDD>);
private:
	friend class DD;
	friend class BDD;
	friend class MTBDD;
	DdManager* ddmgr;                  //! CUDD DD manager
};

class CubeVisitor {
public:
	virtual void operator()(const Cube&) {}
	virtual ~CubeVisitor() {}
};

/*! \brief BDD and MTBDD base class */
class DD {
public:
	/* auxiliary */
	/*! \brief constant function */
	bool IsConstant() const;
	/*! \brief support */
	BDD getSupport() const;

	/*! \brief get size of support */
	int getSupportSize() const;

	/*! \brief index of a node (variable index) */
	inline int getIndex() const { assert(f); return Cudd_NodeReadIndex(f); }

	/* statistics */
	/*! \brief nr of minterms */
	double CountMinterm(int num_vars) const;
	/*! \brief nr of cubes */
	size_t CountCubes() const;
	/*! \brief nr of nodes in BDD */
	size_t size() const;
	/*! \brief print the minterms */
	void PrintMinterm() const;
	/*! \brief print the minterms */
	void PrintMinterm(std::ostream&) const;
	/*! \brief print the minterms into string */
	void PrintMinterm(std::string&) const;

	/*! \brief print the minterms (providing variables names) */
	void PrintMinterm(const std::vector<std::string>&) const;


	/*! \brief is initialized */
	bool isNull() const { return f==0; }
	/*! \brief set to null */
	void setNull() { f = 0; }


	/*! \brief run a cube visitor on this DD */
	void Visit(CubeVisitor& v);

	/*! \brief assume that BDD is a cube, make it explicit */
	void getCube(Cube&) const;

	/* code to co-operate with low-level C */
	/*! \brief return manager */
	inline DdManager* getDdManager() const { return ddmgr; }
	/*! \brief return function */
	inline DdNode*    getDdNode   () const { return f;     }
protected:
	friend class DDManager; //! manager should be able to manipulate BDD
	friend class BDD;
	DdManager* ddmgr;        //! actuall DD manager
	DdNode* f;               //! hook to DD node
};

/*! \brief BDD wrapper */
class BDD : public DD {
public:
	/*! \brief copy constructor */
	BDD(const BDD&);
	/*! \brief construct from cube */
	BDD (DDManager&, const Cube&);

	/*! \brief construct from cube */
	BDD (DDManager&, const Cube&, const std::vector<BDD>&);

	/*! \brief dummy constructor used by containers */
	BDD ();
	/*! destructor */
	~BDD();

	/*! convert to MTBDD */
	MTBDD toMTBDD() const;

	/* comparison operators */
	/*! \brief equal */
	bool operator==(const BDD&) const;
	/*! \brief unequal */
	bool operator!=(const BDD&) const;
	/*! \brief less equal compares bdds */
	bool operator<=(const BDD& bdd) const;
    	/*! \brief comparison between underlying BDD pointers
	 *  /warning: compares pointers not logical function
	 */
	bool operator<(const BDD&) const;

	/* information about BDD */
	/*! \brief true */
	bool isTrue()     const;
	/*! \brief false */
	bool isFalse()    const;

	/* manipulating BDDs */
	/*! \brief disjunction */
	BDD operator|(const BDD&) const;
	/*! \brief conjunction */
	BDD operator&(const BDD&) const;
	/*! \brief exclusive or */
	BDD operator^(const BDD&) const;
	/*! \brief assign */
	BDD& operator=(const BDD&) ;
	/*! \brief or assign */
	BDD& operator|=(const BDD&) ;
	/*! \brief and assign */
	BDD& operator&=(const BDD&) ;
	/*! \brief negation */
	BDD operator!() const;
	/*! \brief if then else */
	BDD Ite(const BDD&,const BDD&) const;

	/*! \brief maximum */
	double FindMax() ;

	/*! \brief cofactor */
	BDD Cofactor(const BDD&) const;
	/*! \brief don't care mini (method 1 = restrict, 2 = constrain, 3 = LICompaction) */
	BDD Simplify(const BDD&,short = 2) const;
	/*! \brief get the projection of the BDD w.r.t. to cube */
	BDD Projection(const BDD& cube) const;


	/* quantification */
	/*! \brief exist */
	BDD Exist(const BDD& X) const;
	/*! \brief exist */
	BDD Exist(const std::vector<BDD>& vars) const;
	/*! \brief exist */
	BDD Exist(const std::vector<MTBDD>& vars) const;

	/*! \brief forall X.f */
	BDD Forall(const BDD& X) const;
	/*! \brief exist X.f \wedge g */
	BDD ExistAnd(const BDD& g, const BDD& support) const;

	BDD ExistAnd(const BDD& g, const std::vector<BDD>& vars) const;
	/*! \brief simplify assuming that certain variables are disjoint */
	BDD SimplifyDisj(const std::vector<std::pair<BDD,BDD> >&, unsigned = 0);

	BDD SwapVariables(const std::vector<BDD>& old_vars, const std::vector<BDD>& new_vars) const;
	BDD PermuteVariables(const std::vector<BDD>& old_vars, const std::vector<BDD>& new_vars) const;

	BDD ShiftVariables(const std::vector<BDD>& vars, int distance) const;

	/* state traversal */
	/*! \brief symbolic state traversal */
	static BDD StateTraversal(
			DDManager& ddmgr,
			const std::vector<BDD>& X0,
			const std::vector<BDD>& X1,
			const BDD& init,
			const BDD& transition_relation,
			bool forward = true);
	/*! \brief image computation for state traversal */
	static BDD Post(
		       DDManager& ddmgr,
		       const BDD& X0_cube,
	           const std::vector<BDD>& X1,
	       	   const BDD& current,
		       const BDD& transition_relation);

	static BDD Pre(
		       DDManager& ddmgr,
		       const std::vector<BDD>& X0,
		       const BDD& X1_cube,
	       	   const BDD& current,
		       const BDD& transition_relation);

	/* prime implicants */
	/*! \brief extend the cube to a prime implicant of this BDD */
	BDD getPrimeImplicant(const BDD& cube) const;


	/*! \brief enumerate the prime implicant cover using variables names in 2nd arg */
	void PrintPrimes(std::ostream&,const std::vector<std::string>&) const;
	/*! \brief nr of prime implicants */
	size_t CountPrimes() const;

	/* read and put out content of BDD */
	/*! \brief enumerate the encoded cubes into a vector */
	void EnumerateCubes(std::vector<Cube>&) const;



	/*! \brief enumerate the prime implicant cover */
	void EnumeratePrimes(std::vector<Cube>&,unsigned offset=0,unsigned stride=1) const;
	/*! \brief enumerate prime implicants restricted to a support set */
	//void PrintPrimes(std::ostream&,const std::vector<std::string>&,const BDD& support) const;
	/*! \brief enumerate update cube and equip it with probabilities given in 3rd argument */
	void PrintUpdate(std::ostream&,const std::vector<std::string>&, const std::vector<double>&) const;
	/*! \brief print cubes */
	void Print(std::ostream& o,const std::vector<std::string>& b) const;
	/*! \brief print cubes */
	void Print(std::ostream& o,const std::vector<std::string>& b,DdNode*,bool = false) const;
	/*! \brief print cube cover */
	void PrintCover() const;
	/* for internal use & interfacing with low-level C code */
	/*! internal constructor */
	BDD (DdManager* __ddmgr, DdNode* __f);
	friend class MTBDD;

	void aiSee(std::ostream &stream) const;
	static void aiSee(std::ostream &stream, DdManager* ddmgr, DdNode* dd) ;
};

/*! \brief MTBDD wrapper */
class MTBDD : public DD {
public:
	/*! \brief copy constructor */
	MTBDD(const MTBDD&);
	/*! \brief dummy constructor used by containers */
	MTBDD ();
	/*! destructor */
	~MTBDD();

	/* comparison operators */
	/*! \brief equal */
	bool operator==(const MTBDD&) const;
	/*! \brief unequal */
	bool operator!=(const MTBDD&) const;

	/* information about MTBDD */
	/*! \brief true */
	bool IsOne()     const;
	/*! \brief false */
	bool IsZero()    const;

	/* manipulating BDDs */
	/*! \brief disjunction */
	MTBDD operator|(const MTBDD&) const;
	/*! \brief conjunction */
	MTBDD operator&(const MTBDD&) const;
	/*! \brief times */
	MTBDD operator*(const MTBDD&) const;
	/*! \brief plus */
	MTBDD operator+(const MTBDD&) const;
	/*! \brief assign */
	MTBDD& operator=(const MTBDD&) ;
	/*! \brief or assign */
	MTBDD& operator|=(const MTBDD&) ;
	/*! \brief plus assign */
	MTBDD& operator+=(const MTBDD&) ;
	/*! \brief and assign */
	MTBDD& operator&=(const MTBDD&) ;
	/*! \brief times assign */
	MTBDD& operator*=(const MTBDD&) ;
	double FindMax() ;
	/*! \brief negation */
	MTBDD operator!() const;
	/*! \brief cofactor */
	MTBDD Cofactor(const MTBDD&) const;
	MTBDD Cofactor(const BDD&) const;

	/*! \brief don't care mini (method 1 = restrict, 2 = constrain, 3 = LICompaction) */
	MTBDD Simplify(const MTBDD&,short = 2) const;


	/* quantification */
	/*! \brief quantify variables combining with disjunction */
	MTBDD OrAbstract(const std::vector<MTBDD>& vars) const;
	/*! \brief quantify variables combining with conjunction */
	MTBDD AndAbstract(const std::vector<MTBDD>& vars) const;

	/*! \brief quantify variables taking maximum */
	MTBDD MaxAbstract(const MTBDD& add) const;
	/*! \brief quantify variables taking maximum */
	MTBDD MaxAbstract(const std::vector<MTBDD>& vars) const;
	/*! \brief quantify variables taking sum */
	MTBDD Exist(const std::vector<MTBDD>& vars) const;
	/*! \brief quantify variables taking sum */
	MTBDD Exist(const std::vector<BDD>& vars) const;

	static MTBDD Prob0A
	(const MTBDD& trans01, const MTBDD& all,
	 const std::vector<MTBDD>& rvars,
	 const std::vector<MTBDD>& cvars,
 	 const std::vector<MTBDD>& ndvars,
 	 const MTBDD& b1,
 	 const MTBDD& b2);




	/*! \brief exist */
	MTBDD Exist(const MTBDD& add) const;
	/*! \brief for all */
	MTBDD ForAll(const MTBDD& add) const;

	/*! \brief if then else */
	MTBDD Ite(const MTBDD&,const MTBDD&) const;

	/* variables */
	/*! \brief replace old_vars with new_vars */
	MTBDD PermuteVariables(const std::vector<MTBDD>& old_vars, const std::vector<MTBDD>& new_vars) const;
	/*! \brief replace old_vars with new_vars */
	MTBDD SwapVariables(const std::vector<MTBDD>& old_vars, const std::vector<MTBDD>& new_vars) const;

	/* read and put out content of BDD */
	/*! \brief enumerate the encoded cubes into a vector */
	void EnumerateCubes(std::vector<Cube>&) const;
	/*! \brief enumerate the prime implicant cover */
	void EnumeratePrimes(std::vector<Cube>&) const;
	/*! \brief enumerate update cube and equip it with probabilities given in 3rd argument */
	void PrintUpdate(std::ostream&,const std::vector<std::string>&, const std::vector<double>&) const;
	/*! \brief print cubes */
	void PrintCubes(std::ostream& o,const std::vector<std::string>& b) const;
	/*! \brief print cubes */
	void Print(std::ostream& o,const std::vector<std::string>& b) const;


	/*! \brief print cubes */
	void Print(std::ostream& o,const std::vector<std::string>& b,DdNode*,bool = false) const;

	/*! \brief print cube cover */
	void PrintCover() const;
	/*! \brief is initialized */
	bool isNull() const { return f==0; }

	BDD GreaterThan(double) const;

	BDD Interval(double lower, double upper) const;


	/* for internal use & interfacing with low-level C code */
	/*! internal constructor */
	MTBDD (DdManager* __ddmgr, DdNode* __f);

	friend class BDD;
};

} // end of namespace bdd


/** BEGIN FIX **/
namespace std
{
namespace tr1 {
  template<> struct hash< bdd::BDD >
  {
    size_t operator()( const bdd::BDD& x ) const
    {
      return hash< const char* >()( (const char*) x.getDdNode());
    }
  };
}
}
/** END FIX **/

#endif
