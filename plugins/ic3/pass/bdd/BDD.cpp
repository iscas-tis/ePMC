#include <vector>
#include <map>

#include <set>
using namespace std;
using namespace __gnu_cxx;

#include "util/Util.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Database.h"
#include "util/Cube.h"
#include "util/aiSee.h"
#include "BDD.h"


using namespace util;

namespace bdd {

//CUDD uses a special number to denote don't care cube entries ... and this is it:
const int CUDD_UNDEF_CONST = 2;

DDManager::DDManager(size_t nvars) {
	ddmgr = Cudd_Init(nvars,0,CUDD_UNIQUE_SLOTS,CUDD_CACHE_SLOTS,0);
	//Cudd_AutodynEnable(ddmgr,CUDD_REORDER_SIFT);
}

DDManager::~DDManager() {
	Cudd_Quit(ddmgr);
}

BDD DDManager::True() const {
	return BDD(ddmgr,Cudd_ReadOne(ddmgr));
}

BDD DDManager::False() const {
	return BDD(ddmgr,Cudd_ReadLogicZero(ddmgr));
}

BDD DDManager::Variable(int i) const {
	return BDD(ddmgr,Cudd_bddIthVar(ddmgr,i));
}

MTBDD DDManager::MTBDDVariable(int i) const {
	return MTBDD(ddmgr,Cudd_addIthVar(ddmgr,i));
}

MTBDD DDManager::Constant(double value) const {
  return MTBDD(ddmgr, Cudd_addConst(ddmgr, value));
}

MTBDD DDManager::Equals(const MTBDD &mtbdd, double value) {
  return Interval(mtbdd, value, value);
}

MTBDD DDManager::Interval(const MTBDD &bdd, double lower, double upper) {
  	DdNode *tmp, *tmp2;
	tmp = Cudd_addBddInterval(ddmgr, bdd.f, lower, upper);
	assert(tmp);
	Cudd_Ref(tmp);
	tmp2 = Cudd_BddToAdd(ddmgr, tmp);
	Cudd_RecursiveDeref(ddmgr, tmp);
	return MTBDD(ddmgr, tmp2);
}


BDD DDManager::And(const std::vector<BDD>& bdds) {
	DdNode      *result = Cudd_ReadOne(ddmgr);
	DdNode      *fn = Cudd_ReadLogicZero(ddmgr);
	Cudd_Ref(result);
	for (int i = bdds.size() - 1; i >= 0; --i) {
		fn = Cudd_bddAnd(ddmgr,bdds[i].f,result);
		Cudd_Ref(fn);
		Cudd_RecursiveDeref(ddmgr,result);
		result = fn;
	}

	return BDD(ddmgr,result);
}



MTBDD DDManager::BddToMtbdd(const BDD &bdd) const {
  DdNode * result = Cudd_BddToAdd(ddmgr, bdd.f);
  assert(result);
  return MTBDD(ddmgr, result);
}

MTBDD BDD::toMTBDD () const {
	assert(ddmgr && f);
	DdNode * result = Cudd_BddToAdd(ddmgr, f);
	assert(result);
	return MTBDD(ddmgr, result);
}


BDD BDD::SwapVariables(const std::vector<BDD>& old_vars, const std::vector<BDD>& new_vars) const {
	assert(ddmgr && f);
	assert(old_vars.size() == new_vars.size());
	size_t n(old_vars.size());
	DdNode *result;
	DdNode *x[n], *y[n];
	for (size_t i = 0; i < n; ++i) {
		x[i] = old_vars[i].f;
		y[i] = new_vars[i].f;
	}
	result = Cudd_bddSwapVariables(ddmgr, f, x, y, n);
	return BDD(ddmgr,result);

}

MTBDD DDManager::Encode(const vector<bdd::MTBDD>& vars,
			long index,
			double value) {
	DdNode *tmp, *tmp2, *f, *tmp_f, *g, *res;
	int i;
	int num_vars = vars.size();

	// build a 0-1 ADD to store position of element of the vector
	f = Cudd_addConst(ddmgr, 1.0);
	for (i = 0; i < num_vars; i++) {
		Cudd_Ref(tmp = vars[i].f);
		if ((index & (1l<<(num_vars-i-1))) == 0) {
			tmp2 = Cudd_addCmpl(ddmgr, tmp);
			Cudd_Ref(tmp2);
			Cudd_RecursiveDeref(ddmgr, tmp);
			tmp = tmp2;
		}
		tmp_f = Cudd_addApply(ddmgr, Cudd_addTimes, tmp, f);
		Cudd_Ref(tmp_f);
		Cudd_RecursiveDeref(ddmgr, tmp);
		Cudd_RecursiveDeref(ddmgr, f);
		f = tmp_f;
	}

	g = Cudd_addConst(ddmgr, value);
	Cudd_Ref(g);

	// compute new vector
	res = Cudd_addIte(ddmgr, f, g, Cudd_ReadZero(ddmgr));
	//  Cudd_RecursiveDeref(ddmgr, f);
	//  Cudd_RecursiveDeref(ddmgr, g);
	//  Cudd_RecursiveDeref(ddmgr, bdd.f);

	/** \todo memory issues? */

	return MTBDD(ddmgr, res);
}

BDD DDManager::Encode(const vector<bdd::BDD>& vars, long index) {
	DdNode *tmp, *tmp2, *f, *tmp_f;
	int i;
	int num_vars = vars.size();

	// build a 0-1 ADD to store position of element of the vector
	f = Cudd_ReadOne(ddmgr);
	Cudd_Ref(f);
	for (i = 0; i < num_vars; i++) {
		Cudd_Ref(tmp = vars[i].f);
		if ((index & (1l<<(num_vars-i-1))) == 0) {
			tmp2 = Cudd_Not(tmp);
			Cudd_Ref(tmp2);
			Cudd_RecursiveDeref(ddmgr, tmp);
			tmp = tmp2;
		}
		tmp_f = Cudd_bddAnd(ddmgr, tmp, f);
		Cudd_Ref(tmp_f);
		Cudd_RecursiveDeref(ddmgr, tmp);
		Cudd_RecursiveDeref(ddmgr, f);
		f = tmp_f;
	}
	return BDD(ddmgr, f);
}


unsigned DDManager::getNrOfVariables() const {
	return Cudd_ReadSize(ddmgr);
}

MTBDD DDManager::MtbddIdentity(vector<MTBDD> a, vector<MTBDD> b) {
  assert((a.size() == b.size()));
  MTBDD result = Constant(1);

  for (unsigned i = 0; i < a.size(); i++) {
    result *= a[i].Ite(b[i], !b[i]);
  }

  return result;
}

double DD::CountMinterm(int num_vars) const {
	assert(ddmgr && f);

	double nr_of_minterms ( Cudd_CountMinterm(ddmgr, f, num_vars) );

	assert( num_vars <= Cudd_ReadSize(ddmgr) );
	assert( nr_of_minterms != CUDD_OUT_OF_MEM);
	assert( nr_of_minterms >= 0);

	return nr_of_minterms;
}

void DD::PrintMinterm() const {
	assert(ddmgr && f);
	Cudd_PrintMinterm(ddmgr, f);
}

void DD::PrintMinterm(std::ostream& o) const {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		for(int i=0;i<nvars;++i) {
			switch(cube[i]) {
				case 0: o << "0"; break;
				case 1: o << "1";  break;
				case CUDD_UNDEF_CONST: o<<"-"; break;
				default: assert(false);
			}
		}
	}
}

void DD::PrintMinterm(std::string& s) const {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		for(int i=0;i<nvars;++i) {
			switch(cube[i]) {
				case 0: s+="0"; break;
				case 1: s+="1";  break;
				case CUDD_UNDEF_CONST: s+="-"; break;
				default: assert(false);
			}
		}
		s+="\n";
	}
}

size_t DD::CountCubes() const {
   size_t counter = 0;
   DdGen *gen;
   int* cube;
   CUDD_VALUE_TYPE value;
   Cudd_ForeachCube(ddmgr, f, gen, cube, value )
	   ++counter;
   return counter;

}

size_t BDD::CountPrimes() const {
   size_t counter = 0;
   DdGen *gen;
   int* cube;
   Cudd_ForeachPrime(ddmgr, f, f, gen, cube )
	   ++counter;
   return counter;
}

size_t DD::size() const {
	return Cudd_DagSize(f);
}


BDD::BDD (DdManager* __ddmgr, DdNode* __f) {
	ddmgr = __ddmgr;
	f     = __f;
	assert(ddmgr && f);
	Cudd_Ref(f);
}

BDD::BDD() {
	DD::ddmgr = 0; DD::f = 0;
}

BDD::BDD(const BDD& bdd) {
	ddmgr = bdd.ddmgr;
	f = bdd.f;
	if(f)
		Cudd_Ref(f);
}

BDD::~BDD() {
	if(ddmgr && f)
		Cudd_RecursiveDeref(ddmgr,f);
}

BDD::BDD (DDManager& cm, const Cube& c) {
	ddmgr = cm.ddmgr;
	assert(ddmgr);
	DdNode      *cube = Cudd_ReadOne(ddmgr);
	DdNode      *fn = Cudd_ReadLogicZero(ddmgr);
	Cudd_Ref(cube);
	for (int i = c.size() - 1; i >= 0; --i) {
		switch(c[i]) {
			case l_true:  fn = Cudd_bddAnd(ddmgr,Cudd_bddIthVar(ddmgr,i),cube); break;
			case l_false: fn = Cudd_bddAnd(ddmgr,Cudd_Not(Cudd_bddIthVar(ddmgr,i)),cube); break;
			default: continue; break;
		}
		Cudd_Ref(fn);
		Cudd_RecursiveDeref(ddmgr,cube);
		cube = fn;
	}
	f = cube;
	Cudd_Ref(f);
}

/*! \brief construct from cube */
BDD::BDD (DDManager& cm, const Cube& c, const std::vector<BDD>& x) {
	ddmgr = cm.ddmgr;
	assert(ddmgr);
	f = Cudd_ReadOne(cm.ddmgr);
	Cudd_Ref(f);
	DdNode      *fn = Cudd_ReadLogicZero(ddmgr);
	for (int i = c.size() - 1; i >= 0; --i) {
		switch(c[i]) {
			case l_true:  fn = Cudd_bddAnd(ddmgr,x[i].f,f); break;
			case l_false: fn = Cudd_bddAnd(ddmgr,Cudd_Not(x[i].f),f); break;
			default: continue; break;
		}
		Cudd_Ref(fn);
		Cudd_RecursiveDeref(ddmgr,f);
		f = fn;
	}
	Cudd_Ref(f);
}


bool BDD::operator==(const BDD& bdd) const {
	return f == bdd.f;
}

bool BDD::operator!=(const BDD& bdd) const {
	return f != bdd.f;
}

bool BDD::operator<=(const BDD& bdd) const {
	assert(f);
	assert(bdd.f);
	return Cudd_bddLeq(ddmgr,f,bdd.f);
}

bool BDD::operator<(const BDD& bdd) const {
	assert(f);
	assert(bdd.f);
	return f < bdd.f;
}


BDD& BDD::operator=(const BDD& bdd) {
	assert(bdd.ddmgr && bdd.f);
	if(ddmgr && f) {
		Cudd_RecursiveDeref(ddmgr,f);
	}
	ddmgr = bdd.ddmgr;
	f = bdd.f;
	Cudd_Ref(f);
	return *this;
}

BDD BDD::operator|(const BDD& bdd) const {
	assert(ddmgr == bdd.ddmgr);
	DdNode* result = Cudd_bddOr(ddmgr,f,bdd.f);
	return BDD(ddmgr,result);
}


BDD BDD::operator&(const BDD& bdd) const {
	assert(ddmgr == bdd.ddmgr);
	DdNode* result = Cudd_bddAnd(ddmgr,f,bdd.f);
	return BDD(ddmgr,result);
}


BDD BDD::operator^(const BDD& bdd) const {
	assert(ddmgr == bdd.ddmgr);
	DdNode* result = Cudd_bddXor(ddmgr,f,bdd.f);
	return BDD(ddmgr,result);
}

BDD BDD::operator!() const {
	assert(ddmgr);
	DdNode* result = Cudd_Not(f);
	return BDD(ddmgr,result);
}

BDD& BDD::operator|=(const BDD& bdd) {
    	assert(ddmgr==bdd.ddmgr);
	DdNode *result = Cudd_bddOr(ddmgr,f,bdd.f);
    	assert(result);
    	Cudd_Ref(result);
    	Cudd_RecursiveDeref(ddmgr,f);
    	f = result;
    	return *this;
}

BDD& BDD::operator&=(const BDD& bdd) {
	assert(ddmgr==bdd.ddmgr);
	DdNode *result = Cudd_bddAnd(ddmgr,f,bdd.f);
    	assert(result);
    	Cudd_Ref(result);
    	Cudd_RecursiveDeref(ddmgr,f);
    	f = result;
    	return *this;
}

double BDD::FindMax() {
  return Cudd_V(Cudd_addFindMax(ddmgr, f));
}

BDD BDD::Ite(const BDD& a,const BDD& b) const {
	assert(ddmgr==a.ddmgr && ddmgr == b.ddmgr && ddmgr && f && a.f && b.f);
	DdNode* result = Cudd_bddIte(ddmgr,f,a.f,b.f);
	return BDD(ddmgr,result);
}

BDD BDD::Cofactor(const BDD& bdd) const {
	assert(ddmgr==bdd.ddmgr && ddmgr && f && bdd.f);
	DdNode* result = Cudd_Cofactor(ddmgr,f,bdd.f);
	return BDD(ddmgr,result);
}

BDD BDD::Simplify(const BDD& bdd, short method) const {
	assert(ddmgr==bdd.ddmgr && ddmgr && f && bdd.f);
	DdNode* result;
	switch(method) {
		case 1: result = Cudd_bddRestrict(ddmgr,f,bdd.f);  break;
		case 2: result = Cudd_bddConstrain(ddmgr,f,bdd.f); break;
		case 3: result = Cudd_bddLICompaction(ddmgr,f,bdd.f); break;
		case 4: result = Cudd_bddMinimize(ddmgr,f,bdd.f); break;
		default: result = f; break;
	}
	return BDD(ddmgr,result);
}

BDD BDD::Projection(const BDD& cube) const {
	assert(ddmgr==cube.ddmgr && ddmgr && f && cube.f);
	DdNode* result = Cudd_CProjection(ddmgr,f,cube.f);
	return BDD(ddmgr,result);
}


BDD BDD::Exist(const BDD& bdd) const {
	assert(ddmgr==bdd.ddmgr && ddmgr && f && bdd.f);
	DdNode* result = Cudd_bddExistAbstract(ddmgr,f,bdd.f);
	return BDD(ddmgr,result);
}

/* exist quantification */
BDD BDD::Exist(const std::vector<MTBDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = Cudd_bddIthVar(ddmgr,Cudd_NodeReadIndex(vars[i].f));
		Cudd_Ref(var_array[i]);
	}
	DdNode *cube = Cudd_bddComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_bddExistAbstract(ddmgr, f, cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return BDD(ddmgr,res);
}

/* exist quantification */
BDD BDD::Exist(const std::vector<BDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = vars[i].f;
	}
	DdNode *cube = Cudd_bddComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_bddExistAbstract(ddmgr, f, cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return BDD(ddmgr,res);
}



BDD BDD::Forall(const BDD& bdd) const {
	assert(ddmgr==bdd.ddmgr && ddmgr && f && bdd.f);
	DdNode* result = Cudd_bddUnivAbstract(ddmgr,f,bdd.f);
	return BDD(ddmgr,result);
}

BDD BDD::ExistAnd(const BDD& g, const BDD& support) const {
	assert(ddmgr==g.ddmgr && ddmgr==support.ddmgr && ddmgr && f && g.f && support.f);
	DdNode* result = Cudd_bddAndAbstract(ddmgr,f,g.f,support.f);
	return BDD(ddmgr,result);
}

/* exist quantification */
BDD BDD::ExistAnd(const BDD& g, const std::vector<BDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = vars[i].f;
	}
	DdNode *cube = Cudd_bddComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_bddAndAbstract(ddmgr, f, g.f,cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return BDD(ddmgr,res);
}


BDD BDD::SimplifyDisj(const vector<pair<BDD,BDD> >& v, unsigned i) {
start:
	if(i>=v.size()) return *this;
	BDD x   = v[i].first;
	BDD fx  = Cofactor(x);
	BDD fnx = Cofactor(!x);
	if(fx == fnx) {++i; goto start; }
	if(fx.IsConstant()) {++i; goto start; }
	BDD care = v[i].second;
	fx = fx.Cofactor(care);
	fnx = fnx.SimplifyDisj(v,i+1);
	return x.Ite(fx,fnx);
}

/* information about BDD */
//true
bool BDD::isTrue()     const {
	assert(f && ddmgr);
	return f == Cudd_ReadOne(ddmgr);
}

//false
bool BDD::isFalse()    const {
	assert(f && ddmgr);
	return f == Cudd_ReadLogicZero(ddmgr);
}

void DD::getCube(Cube& c) const {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		c.resize(nvars);
		for(int i=0;i<nvars;++i) {
			lbool value = l_undef;
			switch(cube[i]) {
				case 0: value = l_false; break;
				case 1: value = l_true;  break;
				case CUDD_UNDEF_CONST: value = l_undef; break;
				default: assert(false);
			}
			c[i] = value;
		}
	}
}


//enumerate the encoded cubes into a vector
void BDD::EnumerateCubes(vector<Cube>& cv) const {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	CUDD_VALUE_TYPE value;
	int counter = 0;
	//find out how much space is needed
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		++counter;
	}
	//allocate the vectors
	Cube dk(nvars,l_undef);
	cv.resize(0);
	cv.resize(counter,dk);
	counter = 0;
	//fill in the values
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		Cube& c(cv[counter]);
		for(int i=0;i<nvars;++i) {
			switch(cube[i]) {
				case 0: c[i] = l_false; break;
				case 1: c[i] = l_true;  break;
				case CUDD_UNDEF_CONST:  break;
			}
		}
		++counter;
	}
}

/*! \brief run a cube visitor on this DD */
void DD::Visit(CubeVisitor& v) {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		Cube c;
		for(int i=0;i<nvars;++i) {
			lbool value = l_undef;
			switch(cube[i]) {
				case 0: value = l_false; break;
				case 1: value = l_true;  break;
				case CUDD_UNDEF_CONST: value = l_undef; break;
				default: assert(false);
			}
			c.push_back( value );
		}
		v(c);
	}
}



BDD BDD::getPrimeImplicant(const BDD& cube) const {
	assert(ddmgr && f && cube.f);
	DdNode* pi = Cudd_bddMakePrime(ddmgr, cube.f, f);
	assert(pi);
	return BDD(ddmgr,pi);
}

//enumerate the prime implicant cover
void BDD::EnumeratePrimes(vector<Cube>& cv,unsigned offset,unsigned stride) const {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	//fill in the values
	unsigned length ( (nvars - offset + 1) / stride);

	Cudd_ForeachPrime(ddmgr, f, f, gen, cube ) {
		cv.resize(cv.size()+1);
		Cube& c(cv.back());
		c.resize(length,l_undef);
		for(int i=offset, j=0;i<nvars;i+=stride, ++j) {
			switch(cube[i]) {
				case 0: c[j] = l_false; break;
				case 1: c[j] = l_true;  break;
				default: break;
			}
		}
	}
}

//enumerate the prime implicant cover
//o ... is a file stream
//b ... are the names of the variables in the cubes/primes
void BDD::PrintPrimes(ostream& o,const vector<string>& b) const {
	int counter = 0;
	DdGen *gen;
	unsigned nvars = Cudd_ReadSize(ddmgr);
	int* cube;
	bool all_undef = true;
	Cudd_ForeachPrime(ddmgr, f, f, gen, cube ) {
		if(counter) o<<" | ";
		for(unsigned i=0;i<nvars;++i)
			if(cube[i]!=CUDD_UNDEF_CONST)  {
				if( i<b.size()) {
					o<< (!all_undef?"&": "")<<(cube[i] ? "":"!")<<b[i];
					all_undef = false;
				}
			}
		//empty cube is true
		if(all_undef==true) o<<"true";
		all_undef = true;
		++counter;
	}
	//empty set of cubes
	if(counter==0) {
		o<<"false";
	}
}

BDD MTBDD::Interval(double lower, double upper) const {
	assert( ddmgr && f);
	DdNode* result = Cudd_addBddInterval(ddmgr,f,lower,upper);
	return BDD(ddmgr,result);
}

BDD MTBDD::GreaterThan(double constant) const {
  DdNode *result;

  result = Cudd_addBddStrictThreshold(ddmgr, f, constant);

  return BDD(ddmgr, result);
}

//enumerate the prime implicant cover
//o ... is a file stream
//b ... are the names of the variables
void BDD::Print(ostream& o,const vector<string>& b) const {
	Print(o,b,f);
}

void BDD::PrintCover() const {
	assert(ddmgr);
	assert(f);
	Cudd_bddPrintCover(ddmgr,f,f);
}


void BDD::Print(ostream& o,const vector<string>& b,DdNode* curr, bool neg) const {
	if(Cudd_IsConstant(curr)) {
		o<<(Cudd_IsComplement(curr) ? "false" : "true");
		return;
	}
	if(Cudd_IsComplement(curr))
		neg = false;
	int i = Cudd_Regular(curr)->index;
	DdNode* left = Cudd_T(curr);
	DdNode* right = Cudd_E(curr);

	DdNode* one = Cudd_ReadOne(ddmgr);
	DdNode* zero = Cudd_ReadLogicZero(ddmgr);
	short casus = 0;
	if( left == zero );
	else if(left ==one) casus = 10;
	else casus = 20;

	if( right == zero );
	else if(right ==one) casus += 1;
	else casus += 2;

	// c ? a : b = c & a | !c & b
	// switch(lcase, rcase)
	// 00: error
	// 01: !c
	// 02: !c & b
	// 10: c
	// 11: error
	// 12: c | b
	// 20: c & a
	// 21: c | a
	// 22: c ? a : b
	const string& var = b[i];
	switch(casus) {
		case 1:
			if(neg)
				o<<var;
			else
				o<<"!"<<var;
			break;
		case 2:
			if(neg) {
				o<<var<<" | "; Print(o,b,right,true);
			} else {
				o<<"!"<<var<<" & "; Print(o,b,right);
			}
			break;
		case 10:
			if(neg)
				o<<"!"<<var;
			else
				o<<var;
			break;
		case 12:
			if(neg) {
				o<<"!"<<var<<" & "; Print(o,b,right,true);
			} else {
				o<<"("<<var<<" | "; Print(o,b,right); o<<")";
			}
			break;
		case 20:
			if(neg) {
				o<<"("<<var<<" | "; Print(o,b,left,true); o<<")";
			} else {
				o<<var<<" & "; Print(o,b,left);
			}
			break;
		case 21:
			if(neg) {
				o<<"!"<<var<<" & "; Print(o,b,left,true);
			}
			else {
				o<<"("<<var<<" | "; Print(o,b,left); o<<" )";
			}
			break;
		case 22:
			o<<"( "<<var<<" ? ";
			Print(o,b,left,neg); o<<" : ";
			Print(o,b,right,neg);
			o<<" )";
			break;
		default: assert(false); break;
	}
}

//enumerate the prime implicant cover
//o ... is a file stream
//b ... are the names of the variables in the cubes/primes
//
void BDD::PrintUpdate(ostream& o,const vector<string>& b,const vector<double>& p) const {
	int cube_size = b.size();

	if(cube_size==0) { o<<"1;\n"; return; }

	DdGen *gen;
	int nvars = Cudd_ReadSize(ddmgr);
	int* cube;
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value) {
		bool all_undef = true;
		for(int i=0;i<nvars;++i) {
			int index = i % cube_size;
			if(index == 0) {
				size_t p_index = i/cube_size;
				if(i && all_undef ) o<<"1";
				if( p_index >= p.size()) break;
				all_undef = true;
				if(i)
					o<<"+ ";
				o<<p[p_index]<<":";
			}
			if(cube[i]!=CUDD_UNDEF_CONST)  {
				o<< (!all_undef? "&": "")<<"("<<b[index]<<"'="<< (cube[i]==l_true ? "true" : "false") <<")";
				all_undef = false;
			}
		}

		o<<";\n";
	}
}


void BDD::aiSee(std::ostream &stream, DdManager* ddmgr, DdNode* dd) {
	std::string id(util::intToString(reinterpret_cast<long>(Cudd_Regular(dd))));
	if(!Cudd_IsConstant(dd)) {
		util::aiSeeNode(stream,id,util::intToString(Cudd_NodeReadIndex(dd)));
		DdNode *e(Cudd_E(dd)), *t(Cudd_T(dd));
		std::string estr(util::intToString((long)Cudd_Regular(e)));
		std::string tstr(util::intToString((long)Cudd_Regular(t)));
		util::aiSeeEdge(stream,id,estr,"",1,"blue",Cudd_IsComplement(e) ? "line" : "solid");
		util::aiSeeEdge(stream,id,tstr,"",1,"red",Cudd_IsComplement(t) ? "line" : "solid");
	}

}


void BDD::aiSee(std::ostream &stream) const {
	stream << "graph: {\n";
	DdGen *gen;
	DdNode *node;
	util::aiSeeNode(stream,util::intToString((long)Cudd_ReadOne(ddmgr)),"1");

	Cudd_ForeachNode(
	   ddmgr,
	   f,
	   gen,
	   node
	) {
		BDD::aiSee(stream,ddmgr,node);
	}

	stream << "}\n";
}

MTBDD MTBDD::Prob0A
(
 const MTBDD& trans01,
 const MTBDD& all,
 const std::vector<MTBDD>& rvars,
 const std::vector<MTBDD>& cvars,
 const std::vector<MTBDD>& ndvars,
 const MTBDD& b1,
 const MTBDD& b2
)
{
	MSG(1,"MTBDD::Prob0A\n");
  MTBDD reach, sol, tmp;
  bool done;
  int iters;

  // timing stuff
  long start1;
  double time_taken, time_for_setup, time_for_iters;

  // start clock
  start1 = util_cpu_time();

  // reachability fixpoint loop
  reach = b2;
  done = false;
  iters = 0;
  while (!done) {
	MSG(1,"reach %E\n",reach.CountMinterm(cvars.size()));
    iters++;
    tmp = reach.PermuteVariables(rvars, cvars);
    tmp = tmp * trans01;
    tmp = tmp.Exist(cvars);
    tmp = tmp.Exist(ndvars);
    tmp = b1 * tmp;
    tmp = tmp | b2;

    if (tmp == reach) {
      done = true;
    }
    reach = tmp;
  }
  reach = reach.PermuteVariables(cvars, rvars);

  // actual answer is states NOT reachable
  sol = all * !reach;

  // stop clock
  time_taken = (double)(util_cpu_time() - start1)/1000;
  time_for_setup = 0;
  time_for_iters = time_taken;

  // print iterations/timing info
  MSG(1,"MTBD::Prob0A: %d iterations in %.2f seconds (average %.6f, setup %.2f)\n", iters, time_taken, time_for_iters/iters, time_for_setup);

  return sol;
}


/**
	\brief BDD-based state traversal
	\param X0 present state variables
	\param X1 next state variables
	\param init initial states (support X0)
	\param transition_relation partitioned transition relation (support union of X0 and X1 )
	\return reachable set of states	(support X0)
	\see BDD BDD::Image()
*/
BDD BDD::StateTraversal(DDManager& ddmgr,
			const vector<BDD>& X0,
			const vector<BDD>& X1,
			const BDD& start,
			const BDD& transition_relation,
			bool forward) {
	BDD previous(ddmgr.False());
	BDD current (start);

	assert(X0.size()==X1.size());



	BDD onion = start;

	if(forward) {

		BDD X0cube(ddmgr.True());
		for(int i = (int) X0.size()-1; i>-1;--i) {
			X0cube &= X0[i];
		}
		while(current!=previous) {
			previous = current;
			onion = Post(ddmgr,X0cube,X1,onion,transition_relation) & !current;
			current |= onion;
		}
	}
	else {
		BDD X1cube(ddmgr.True());
		for(int i = (int) X1.size()-1; i>-1;--i) {
			X1cube &= X1[i];
		}
		while(previous!=current) {
			previous = current;
			onion = Pre(ddmgr,X0,X1cube,onion,transition_relation) & !current;
			current |= onion;
		}
	}
	return current;
}

/**
	\brief image computation (single step of BDD-based state traversal)
	\param X0 present state variables
	\param X1 next state variables
	\param init initial states (support X0)
	\param transition_relation partitioned transition relation (support union of X0 and X1 )
	\return reachable set of states	(support X0)
*/
BDD BDD::Post(DDManager& ddmgr,
	       const BDD& X0_cube,
	       const vector<BDD>& X1,
	       const BDD& current,
	       const BDD& R) {
	BDD  post_local = current.ExistAnd(R,X0_cube);
	return post_local.ShiftVariables(X1, -1);
}

/**
	\brief image computation (single step of BDD-based state traversal)
	\param X0 present state variables
	\param X1 next state variables
	\param init initial states (support X0)
	\param transition_relation partitioned transition relation (support union of X0 and X1 )
	\return reachable set of states	(support X0)
*/
BDD BDD::Pre(DDManager& ddmgr,
	       const vector<BDD>& X0,
	       const BDD& X1_cube,
	       const BDD& current,
	       const BDD& R) {
	BDD future = current.ShiftVariables(X0, 1);
	return future.ExistAnd(R,X1_cube);
}


/************************* MTBDD section *******************************/



MTBDD MTBDD::Cofactor(const MTBDD& bdd) const {
	assert(ddmgr==bdd.ddmgr && ddmgr && f && bdd.f);
	DdNode* result = Cudd_Cofactor(ddmgr,f,bdd.f);
	return MTBDD(ddmgr,result);
}


MTBDD MTBDD::Cofactor(const BDD& bdd) const {
	assert(ddmgr==bdd.ddmgr && ddmgr && f && bdd.f);
	DdNode* result = Cudd_Cofactor(ddmgr,f,bdd.f);
	return MTBDD(ddmgr,result);
}

MTBDD MTBDD::Simplify(const MTBDD& bdd, short method) const {
	assert(ddmgr==bdd.ddmgr && ddmgr && f && bdd.f);
	DdNode* result;
	switch(method) {
		case 1: result = Cudd_addRestrict(ddmgr,f,bdd.f);  break;
		case 2: result = Cudd_addConstrain(ddmgr,f,bdd.f); break;
		default: result = f; break;
	}
	return MTBDD(ddmgr,result);
}

MTBDD::MTBDD (DdManager* __ddmgr, DdNode* __f) {
	ddmgr = __ddmgr;
	f = __f;
	assert(ddmgr && f);
	Cudd_Ref(f);
}

MTBDD MTBDD::MaxAbstract(const MTBDD& add) const {
	assert(ddmgr==add.ddmgr && ddmgr && f && add.f);
	DdNode* result = Cudd_addUnivAbstract(ddmgr,f,add.f);
	return MTBDD(ddmgr,result);
}

MTBDD MTBDD::Exist(const MTBDD& add) const {
	assert(ddmgr==add.ddmgr && ddmgr && f && add.f);
	DdNode* result = Cudd_addExistAbstract(ddmgr,f,add.f);
	return MTBDD(ddmgr,result);
}

MTBDD MTBDD::ForAll(const MTBDD& add) const {
	assert(ddmgr==add.ddmgr && ddmgr && f && add.f);
	DdNode* result = Cudd_addUnivAbstract(ddmgr,f,add.f);
	return MTBDD(ddmgr,result);
}


MTBDD MTBDD::Ite(const MTBDD& a,const MTBDD& b) const {
	assert(ddmgr==a.ddmgr && ddmgr == b.ddmgr && ddmgr && f && a.f && b.f);
	DdNode* result = Cudd_addIte(ddmgr,f,a.f,b.f);
	return MTBDD(ddmgr,result);
}

MTBDD::MTBDD() {
	ddmgr = 0;
	f = 0;
}

MTBDD::MTBDD(const MTBDD& bdd) {
	ddmgr = bdd.ddmgr;
	f = bdd.f;
	if(f)
		Cudd_Ref(f);
}

MTBDD::~MTBDD() {
	if(ddmgr && f)
		Cudd_RecursiveDeref(ddmgr,f);
}

bool MTBDD::operator==(const MTBDD& bdd) const {
	return f == bdd.f;
}

bool MTBDD::operator!=(const MTBDD& bdd) const {
	return f != bdd.f;
}

/* exist quantification */
MTBDD MTBDD::OrAbstract(const std::vector<MTBDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = vars[i].f;
	}
	DdNode *cube = Cudd_addComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_addOrAbstract(ddmgr, f, cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return MTBDD(ddmgr,res);
}

/* exist quantification */
MTBDD MTBDD::MaxAbstract(const std::vector<MTBDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = vars[i].f;
	}
	DdNode *cube = Cudd_addComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_addMaxAbstract(ddmgr, f, cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return MTBDD(ddmgr,res);
}


/* exist quantification */
MTBDD MTBDD::Exist(const std::vector<BDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = Cudd_addIthVar(ddmgr,Cudd_NodeReadIndex(vars[i].f));
		Cudd_Ref(var_array[i]);
	}
	DdNode *cube = Cudd_addComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_addExistAbstract(ddmgr, f, cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return MTBDD(ddmgr,res);
}

/* exist quantification */
MTBDD MTBDD::Exist(const std::vector<MTBDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = vars[i].f;
	}
	DdNode *cube = Cudd_addComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_addExistAbstract(ddmgr, f, cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return MTBDD(ddmgr,res);
}

/* forall quantification */
MTBDD MTBDD::AndAbstract(const std::vector<MTBDD>& vars) const {
	assert(ddmgr && f);
	unsigned num_vars = vars.size();
	DdNode* var_array[num_vars];
	for(unsigned i = 0; i<num_vars; ++i) {
		var_array[i] = vars[i].f;
	}
	DdNode *cube = Cudd_addComputeCube(ddmgr, var_array, NULL, num_vars);
        Cudd_Ref(cube);
        DdNode* res = Cudd_addUnivAbstract(ddmgr, f, cube);
        Cudd_RecursiveDeref(ddmgr,cube);

        return MTBDD(ddmgr,res);
}

MTBDD MTBDD::PermuteVariables(const std::vector<MTBDD>& old_vars, const std::vector<MTBDD>& new_vars) const {
	assert(ddmgr && f);
	assert(old_vars.size() == new_vars.size());
	DdNode *result;

	int permut[Cudd_ReadSize(ddmgr)];

	for (int i = 0; i < Cudd_ReadSize(ddmgr); ++i) {
		permut[i] = i;
	}
	for (unsigned i = 0; i < old_vars.size(); ++i) {
		permut[old_vars[i].f->index] = new_vars[i].f->index;
	}
	result = Cudd_addPermute(ddmgr, f, permut);
	return MTBDD(ddmgr,result);

}

BDD BDD::PermuteVariables(const std::vector<BDD>& old_vars, const std::vector<BDD>& new_vars) const {
	assert(ddmgr && f);
	assert(old_vars.size() == new_vars.size());
	DdNode *result;

	int permut[Cudd_ReadSize(ddmgr)];

	for (int i = 0; i < Cudd_ReadSize(ddmgr); ++i) {
		permut[i] = i;
	}
	for (unsigned i = 0; i < old_vars.size(); ++i) {
		permut[old_vars[i].f->index] = new_vars[i].f->index;
	}
	result = Cudd_bddPermute(ddmgr, f, permut);
	return BDD(ddmgr,result);

}

BDD BDD::ShiftVariables(const std::vector<BDD>& vars, int distance) const {
	assert(ddmgr && f);
	DdNode *result;

	int permut[Cudd_ReadSize(ddmgr)];

	for (int i = 0; i < Cudd_ReadSize(ddmgr); ++i) {
		permut[i] = i;
	}
	for (unsigned i = 0; i < vars.size(); ++i) {
		permut[vars[i].f->index] = vars[i].f->index + distance;
	}
	result = Cudd_bddPermute(ddmgr, f, permut);
	return BDD(ddmgr,result);

}


MTBDD MTBDD::SwapVariables(const std::vector<MTBDD>& old_vars, const std::vector<MTBDD>& new_vars) const {
	assert(ddmgr && f);
	assert(old_vars.size() == new_vars.size());
	size_t n(old_vars.size());
	DdNode *result;
	DdNode *x[n], *y[n];
	for (size_t i = 0; i < n; ++i) {
		x[i] = old_vars[i].f;
		y[i] = new_vars[i].f;
	}
	result = Cudd_addSwapVariables(ddmgr, f, x, y, n);
	return MTBDD(ddmgr,result);

}

MTBDD& MTBDD::operator=(const MTBDD& bdd) {
	assert(bdd.ddmgr && bdd.f);
	if(ddmgr && f) {
		Cudd_RecursiveDeref(ddmgr,f);
	}
	ddmgr = bdd.ddmgr;
	f = bdd.f;
	Cudd_Ref(f);
	return *this;
}

MTBDD MTBDD::operator|(const MTBDD& bdd) const {
	assert(ddmgr == bdd.ddmgr);
	DdNode *result = Cudd_addApply(ddmgr, Cudd_addOr, f, bdd.f);
	return MTBDD(ddmgr,result);
}


MTBDD MTBDD::operator&(const MTBDD& bdd) const {
	assert(ddmgr == bdd.ddmgr);

	return !(!(*this) | !bdd);
}

MTBDD MTBDD::operator*(const MTBDD& bdd) const {
	assert(ddmgr == bdd.ddmgr);
	DdNode* result = Cudd_addApply(ddmgr, Cudd_addTimes, f,bdd.f);
	return MTBDD(ddmgr,result);
}

MTBDD MTBDD::operator+(const MTBDD& bdd) const {
	assert(ddmgr == bdd.ddmgr);
	DdNode* result = Cudd_addApply(ddmgr, Cudd_addPlus, f,bdd.f);
	return MTBDD(ddmgr,result);
}

MTBDD MTBDD::operator!() const {
	assert(ddmgr);
	DdNode *result = Cudd_addCmpl(ddmgr, f);
	return MTBDD(ddmgr,result);
}

MTBDD& MTBDD::operator|=(const MTBDD& bdd) {
	assert(ddmgr==bdd.ddmgr);
	DdNode *result = Cudd_addApply(ddmgr, Cudd_addOr, f,bdd.f);
    	assert(result);
    	Cudd_Ref(result);
    	Cudd_RecursiveDeref(ddmgr,f);
    	f = result;
    	return *this;
}

MTBDD& MTBDD::operator+=(const MTBDD& bdd) {
    	assert(ddmgr==bdd.ddmgr);
	DdNode *result = Cudd_addApply(ddmgr, Cudd_addPlus, f,bdd.f);
    	assert(result);
    	Cudd_Ref(result);
    	Cudd_RecursiveDeref(ddmgr,f);
    	f = result;
    	return *this;
}

  /* this doesn't work */
MTBDD& MTBDD::operator&=(const MTBDD& bdd) {
	assert(ddmgr==bdd.ddmgr);
    	return *this = (*this) & bdd;
}

MTBDD& MTBDD::operator*=(const MTBDD& bdd) {
	assert(ddmgr==bdd.ddmgr);
	DdNode *result = Cudd_addApply(ddmgr, Cudd_addTimes, f,bdd.f);
	//	DdNode *result = Cudd_bddAnd(ddmgr,f,bdd.f);
    	assert(result);
    	Cudd_Ref(result);
    	Cudd_RecursiveDeref(ddmgr,f);
    	f = result;
    	return *this;
}

double MTBDD::FindMax() {
  return Cudd_V(Cudd_addFindMax(ddmgr, f));
}

/* information about MTBDD */
//1
bool MTBDD::IsOne()     const {
	assert(f && ddmgr);
	return f == Cudd_ReadOne(ddmgr);
}

//0
bool MTBDD::IsZero()    const {
	assert(f && ddmgr);
	return f == Cudd_ReadZero(ddmgr);
}

//1 or 0
bool DD::IsConstant() const {
	assert(f && ddmgr);
	return Cudd_IsConstant(f);
}

BDD DD::getSupport() const {
	assert(ddmgr && f);
	DdNode* result = Cudd_Support(ddmgr,f);
	return BDD(ddmgr,result);
}

int DD::getSupportSize() const {
	assert(ddmgr && f);
	int result = Cudd_SupportSize(ddmgr,f);
	return result;
}

//enumerate the encoded cubes into a vector
void MTBDD::EnumerateCubes(vector<Cube>& cv) const {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		Cube c;
		for(int i=0;i<nvars;++i) {
			lbool value = l_undef;
			switch(cube[i]) {
				case 0: value = l_false; break;
				case 1: value = l_true;  break;
				case CUDD_UNDEF_CONST: value = l_undef; break;
				default: assert(false);
			}
			c.push_back( value );
		}
		cv.push_back(c);
	}
}

// void MTBDD::PrintPrimes() const {
//   printf("\n--\n");
//   Cudd_PrintMinterm(ddmgr, f);
//   printf("\n--\n");
// }

//enumerate the prime implicant cover
void MTBDD::EnumeratePrimes(vector<Cube>& cv) const {
	//used to determine maximal cube length
	DdGen *gen;
	int nvars = Cudd_ReadSize(ddmgr);
	int* cube;
	Cudd_ForeachPrime(ddmgr, f, f, gen, cube ) {
		Cube c;
		for(int i=0;i<nvars;++i) {
			lbool value = l_undef;
			switch(cube[i]) {
				case 0: value = l_false; break;
				case 1: value = l_true;  break;
				case CUDD_UNDEF_CONST: value = l_undef; break;
				default: assert(false);
			}
			c.push_back( value );
		}
		cv.push_back(c);
	}
}

//enumerate the prime implicant cover
//o ... is a file stream
//b ... are the names of the variables
void MTBDD::Print(ostream& o,const vector<string>& b) const {
	Print(o,b,f);
}

void MTBDD::PrintCover() const {
	assert(ddmgr);
	assert(f);
	//	Cudd_bddPrintCover(ddmgr,f,f);
}

//enumerate the encoded cubes into a vector
void MTBDD::PrintCubes(ostream& o,const vector<string>& b) const {
	DdGen *gen;
	int* cube;
	int nvars = Cudd_ReadSize(ddmgr);
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value ) {
		for(int i=0;i<nvars && i<(int)b.size();++i) {

			switch(cube[i]) {
				case 0: if(i>0) o<<" & ";
					if(b[i]=="") o<<"!V"+util::intToString(i);
					else
					o<<"!"<<b[i]; break;
				case 1: if(i>0) o<<" & ";
					if(b[i]=="") o<<"V"+util::intToString(i);
					else
					o<<b[i];  break;
				case CUDD_UNDEF_CONST: break;
				default: assert(false);
			}

		}
		o<<" : "<<value<<"\n";
	}
}

void MTBDD::Print(ostream& o,const vector<string>& b,DdNode* curr, bool neg) const {
	if(Cudd_IsConstant(curr)) {
		o<<Cudd_V(curr);
		return;
	}
	if(Cudd_IsComplement(curr))
		neg = false;
	int i = Cudd_Regular(curr)->index;
	DdNode* left = Cudd_T(curr);
	DdNode* right = Cudd_E(curr);

	DdNode* one = Cudd_ReadOne(ddmgr);
	DdNode* zero = Cudd_ReadLogicZero(ddmgr);
	short casus = 0;
	if( left == zero );
	else if(left ==one) casus = 10;
	else casus = 20;

	if( right == zero );
	else if(right ==one) casus += 1;
	else casus += 2;

	// c ? a : b = c & a | !c & b
	// switch(lcase, rcase)
	// 00: error
	// 01: !c
	// 02: !c & b
	// 10: c
	// 11: error
	// 12: c | b
	// 20: c & a
	// 21: c | a
	// 22: c ? a : b
	const string& var = b[i];
	switch(casus) {
		case 1:
			if(neg)
				o<<var;
			else
				o<<"!"<<var;
			break;
		case 2:
			if(neg) {
				o<<var<<" | "; Print(o,b,right,true);
			} else {
				o<<"!"<<var<<" & "; Print(o,b,right);
			}
			break;
		case 10:
			if(neg)
				o<<"!"<<var;
			else
				o<<var;
			break;
		case 12:
			if(neg) {
				o<<"!"<<var<<" & "; Print(o,b,right,true);
			} else {
				o<<"("<<var<<" | "; Print(o,b,right); o<<")";
			}
			break;
		case 20:
			if(neg) {
				o<<"("<<var<<" | "; Print(o,b,left,true); o<<")";
			} else {
				o<<var<<" & "; Print(o,b,left);
			}
			break;
		case 21:
			if(neg) {
				o<<"!"<<var<<" & "; Print(o,b,left,true);
			}
			else {
				o<<"("<<var<<" | "; Print(o,b,left); o<<" )";
			}
			break;
		case 22:
			o<<"( "<<var<<" ? ";
			Print(o,b,left,neg); o<<" : ";
			Print(o,b,right,neg);
			o<<" )";
			break;
		default: assert(false); break;
	}
}

//enumerate the prime implicant cover
//o ... is a file stream
//b ... are the names of the variables in the cubes/primes
//
void MTBDD::PrintUpdate(ostream& o,const vector<string>& b,const vector<double>& p) const {
	int cube_size = b.size();

	if(cube_size==0) { o<<"1;\n"; return; }

	DdGen *gen;
	int nvars = Cudd_ReadSize(ddmgr);
	int* cube;
	CUDD_VALUE_TYPE value;
	Cudd_ForeachCube(ddmgr, f, gen, cube, value) {
		bool all_undef = true;
		for(int i=0;i<nvars;++i) {
			int index = i % cube_size;
			if(index == 0) {
				size_t p_index = i/cube_size;
				if(i && all_undef ) o<<"1";
				if( p_index >= p.size()) break;
				all_undef = true;
				if(i)
					o<<"+ ";
				o<<p[p_index]<<":";
			}
			if(cube[i]!=CUDD_UNDEF_CONST)  {
				o<< (!all_undef? "&": "")<<"("<<b[index]<<"'="<< (cube[i]==l_true ? "true" : "false") <<")";
				all_undef = false;
			}
		}

		o<<";\n";
	}
}

}
