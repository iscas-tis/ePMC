#include "util/Util.h"
#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Cube.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"
#include "dp/SMT.h"

#include "bdd/BDD.h"
#include "bdd/ODD.h"

#include <cmath>
#include "EncodingManager.h"


using namespace lang;
using namespace dp;
using namespace bdd;

namespace pred {

bdd::DDManager dummy_dd_mgr;
EncodingManager dummy_em (dummy_dd_mgr,1,1);
Predicate dummy_pred;

const double inverse_of_ln2 = 1.0 / log(2.0);


ChoiceRange::ChoiceRange() : em (0) {

}

ChoiceRange& ChoiceRange::operator=(const ChoiceRange& cr) {
	em          = cr.em;
	index       = cr.index;
	lower       = cr.lower;
	upper       = cr.upper;
	choice_vars = cr.choice_vars;
	support     = cr.support;
	return *this;
}

ChoiceRange::ChoiceRange(const ChoiceRange& cr)
{
	(*this) = cr;
}

bdd::BDD EncodingManager::getBDDVariable(int bdd_variable_index) {
	return dd_mgr.Variable(bdd_variable_index);
}


bdd::BDD ChoiceRange::getBDD(int value) const {
	assert(em);
	return em->dd_mgr.Encode(choice_vars,value);
}


ChoiceRange::ChoiceRange(EncodingManager& __em,
			 int __index,
			 int __lower,
			 int __upper) :
	em    (&__em),
	index (__index),
	lower (__lower),
	upper (__upper)
{
	assert(em);
	int size = upper - lower + 1;

	if(size>=0) choice_vars.resize(size);

	support = __em.getDDManager().True();

	for(int i = size-1; i>=0; --i) {
		choice_vars[i] = em->getBDDVariable(i+lower);
		support &= choice_vars[i];
	}
}

std::string ChoiceRange::toString() const {
	return "ChoiceRange["+util::intToString(index) + "](lower=" + util::intToString(lower) +
		                                         ", upper=" + util::intToString(upper) + ")";
}


StateVar::StateVar() : em (dummy_em), predicate(dummy_pred) {
}

StateVar::StateVar(const StateVar& sv) :
	em        (sv.em),
	index     (sv.index),
	predicate (sv.predicate),
	name      (sv.name),
	present   (sv.present),
	next      (sv.next)
{
}

std::string StateVar::toString() const {
	return "["+util::intToString(index) + "] offset="+util::intToString(offset)+", "+ name;
}


bdd::BDD StateVar::getBDD(int instance) const {
	BDD result;
	assert(instance >= 0 && instance < 2);
	if(instance == 0) {
		result = present;
	} else if(instance == 1) {
		result = next;
	}
	return result;// present state variable = dd_mgr.Variable(offset)
}

int StateVar::getBDDVariableIndex  ( int instance ) const {
	assert(instance >= 0 && instance < 2);
	return offset+instance;
}

const Predicate& StateVar::getPredicate() const {
	return predicate;
}


StateVar::StateVar(EncodingManager& __em,
		   const Predicate& __predicate,
		   int __index,
		   int __offset) :
	em        (__em),
	index     (__index),
	offset    (__offset),
	predicate (__predicate)
{
	name    = predicate.toString();
	present = em.getBDDVariable(offset);
	next    = em.getBDDVariable(offset+1);
}

EncodingManager::EncodingManager () : dd_mgr(dummy_dd_mgr){
}

EncodingManager::EncodingManager (const EncodingManager& em) : dd_mgr(em.dd_mgr){
	operator=(em);
}

EncodingManager& EncodingManager::operator=(const EncodingManager& em) {
	dd_mgr              = em.dd_mgr;
	first_nondet_choice = em.first_nondet_choice;
	first_prob_choice   = em.first_prob_choice;
	first_state         = em.first_state;
	nr_of_time_frames   = em.nr_of_time_frames;
	prob_choice_range   = em.prob_choice_range;
	max_nondet_var      = em.max_nondet_var;

	int counter = 0;
	for(std::vector<ChoiceRange*>::const_iterator i = em.nondet_choice_ranges.begin();i!=em.nondet_choice_ranges.end();++i, ++counter) {
		const ChoiceRange& cr(**i);
		createNondetChoiceRange(counter,cr.lower,cr.upper);
	}

	for(std::vector<StateVar*>::const_iterator i = em.state_variables.begin();i!=em.state_variables.end();++i) {
		const StateVar& var (**i);
		createStateVar      (var.getPredicate());
	}

	return *this;
}



EncodingManager::EncodingManager (bdd::DDManager& __dd_mgr,
				  int nr_of_reserved_nondet_variables,
				  int nr_of_reserved_prob_choice_variables,
				  int __nr_of_time_frames) :
	dd_mgr		    ( __dd_mgr                                                                                  )
      , first_nondet_choice ( 0                                                                                         )
      , first_prob_choice   ( first_nondet_choice + nr_of_reserved_nondet_variables                                     )
      , first_state         ( first_prob_choice + nr_of_reserved_prob_choice_variables                                  )
      , nr_of_time_frames   ( __nr_of_time_frames								        )
      , max_nondet_var      ( first_nondet_choice							                )
      , prob_choice_range   ( *this, 0, first_prob_choice, first_prob_choice + nr_of_reserved_prob_choice_variables - 1 )
{
	assert(first_nondet_choice < first_prob_choice);
	assert(first_prob_choice   <= first_state);
}

EncodingManager::~EncodingManager() {
	for(std::vector<ChoiceRange*>::iterator i = nondet_choice_ranges.begin();i!=nondet_choice_ranges.end();++i) {
		delete *i;
	}
	resizeNondetChoiceRanges(0);
}

void EncodingManager::computeVariableRange(int offset,
					int nr_of_choices,
					int &lower,
					int &upper) {
	if(nr_of_choices == 0) {
		lower = offset;
		upper = offset - 1;
	} else {
		long need = getChoiceVarNeed(nr_of_choices);
		lower = offset;
		upper = lower + need - 1;
	}
}

ChoiceRange& EncodingManager::createNondetChoiceRange (int nr_of_choices) {
	const int ncrs = nondet_choice_ranges.size();
	int lower, upper;
	int offset = ncrs == 0 ? 0 : nondet_choice_ranges[ncrs-1]->upper+1;

	computeVariableRange(offset,nr_of_choices,lower,upper);

	// check if enough space
	if( ! (upper < first_prob_choice) ) {
		std::cout<<" nr_of_choices : "<< nr_of_choices <<"upper : " << upper<< " first_prob_choice : "<< first_prob_choice << std::endl;

		throw util::RuntimeError("EncodingManager::createNondetChoiceRange: too many nondet choices! (upper= "
		+util::intToString(upper)+" >= first_prob_choice " + util::intToString(first_prob_choice)+ " )\n");
	}
	// we have enough space
	return createNondetChoiceRange (ncrs, lower, upper);
}

ChoiceRange& EncodingManager::createNondetChoiceRange (int ncrs,int lower, int upper) {
	// we have enough space
	ChoiceRange* cr_ptr = new ChoiceRange(*this,ncrs,lower,upper);
	nondet_choice_ranges.push_back(cr_ptr);
	max_nondet_var = std::max(upper,max_nondet_var);
	return *cr_ptr;
}


void EncodingManager::popNondetChoiceRange (int nr) {
	//range check of index
	if( ! ( 0 <= nondet_choice_ranges.size() - nr) )
		throw util::RuntimeError("EncodingManager::popNondetChoiceRanges: choice ranges stack empty!\n");

	resizeNondetChoiceRanges(nondet_choice_ranges.size()-nr);
}

ChoiceRange& EncodingManager::getNondetChoiceRange    (int index) const {
	//range check of index
	if( ! ( index < (int)nondet_choice_ranges.size() ) )
		throw util::RuntimeError("EncodingManager::getNondetChoiceRange: index out of range!\n");

	return *nondet_choice_ranges[index];
}

StateVar& EncodingManager::createStateVar             (const Predicate& pred) {

	assert(!existStateVar(pred));

	//get the right index
	int index = state_variables.size();

	//create the state variable
	StateVar* sv_ptr = new StateVar(*this,pred,index,first_state + 2 * index);

	//store it
	state_variables.push_back(sv_ptr);
	pred_exprs.push_back(sv_ptr->getPredicate().getExpr());
	state_variable_map[pred] = sv_ptr;

	return *sv_ptr;
}

StateVar& EncodingManager::getStateVar                (int index) const {
	//range check of index
	if( ! ( index < (int)state_variables.size() ) )
		throw util::RuntimeError("EncodingManager::getNondetChoiceRange: index out of range!\n");

	//return corresponding variable
	return *state_variables[index];
}

StateVar& EncodingManager::getStateVarFromBDDIndex (int bdd_index) const {
	int index = (bdd_index - first_state) / 2;

	//range check of index
	if( index < 0 ||  ! ( index < (int)state_variables.size() ) )
		throw util::RuntimeError("EncodingManager::getNondetChoiceRange: index out of range!\n");

	//return corresponding variable
	return *state_variables[index];
}

bool EncodingManager::existStateVar       (const Predicate& pred) const {
	return state_variable_map.find(pred)!=state_variable_map.end();
}


StateVar& EncodingManager::getStateVar		     (const Predicate& pred) const {
	//find predicate in map
	std::map<Predicate,StateVar*>::const_iterator i = state_variable_map.find(pred);
	//complain if not in there
	if(i==state_variable_map.end())
		throw util::RuntimeError("EncodingManager::getStateVar: corresponding predicate not found!\n");

	//return the corresponding state variable
	return *(i->second);

}

void EncodingManager::resizeNondetChoiceRanges    (int new_size) {
	//erase entries and resize the choice range vector
	for(int i = new_size; i < (int)nondet_choice_ranges.size();++i) {
		delete nondet_choice_ranges[i];
	}
	nondet_choice_ranges.resize(new_size);
}

const ChoiceRange& EncodingManager::getProbChoiceRange      () const {
	return prob_choice_range;
}

std::string EncodingManager::toString() const {
	std::string result;

	result+="EncodingManager { \n";

	result += " first_nondet_choice="+util::intToString(first_nondet_choice)+",\n";
	result += " first_prob_choice="+util::intToString(first_prob_choice)+",\n";
	result += " first_state="+ util::intToString(first_state)+",\n";
	result += " nr_of_time_frames="+util::intToString(nr_of_time_frames)+",\n";


	result += " ----------- Nondet Choice -----------:\n";
	// forall choice ranges
	for(std::vector<ChoiceRange*>::const_iterator i = nondet_choice_ranges.begin();i!=nondet_choice_ranges.end();++i) {
		result += " " + (*i)->toString()+"\n";
	}
	result += " ----------- Probab Choice -----------:\n";
	result += " "+ prob_choice_range.toString()+"\n";

	result += " ----------- State Variables -----------:\n";
	// for all state variables
	for(std::vector<StateVar*>::const_iterator i = state_variables.begin();i!=state_variables.end();++i) {
		result += " " + (*i)->toString()+"\n";
	}

	result+="} // EncodingManager\n";
	return result;
}

int EncodingManager::getChoiceVarNeed(int nr_of_choices) {

	double result = ceil(log(nr_of_choices) * inverse_of_ln2);
	return (int) result;

}


/*!
	\brief compute symbolic representation (expression) from boolean combination of boolean variables
	\note  Let b1 and b2 be boolean variables for predicates p1 and p2 respectively
*/
CVC3::Expr EncodingManager::getExpr(const bdd::BDD& f) const {
	/*!
	   Compute the expression in 3 steps:
	   (1) enumerate the prime implicant cover of the BDD
	   (2) convert each element in the prime implicant cover into expression
	   (3) compute the disjunction of these expressions and return it
	*/

	/* step (1) */
	std::vector<Cube> prime_implicants;

	f.EnumeratePrimes(prime_implicants,first_state,2); /* set offset (first_state) and stride (2) suitably */

	/* step (2) */
	std::vector<CVC3::Expr> exprs;
	exprs.reserve(prime_implicants.size());

	for(std::vector<Cube>::const_iterator it=prime_implicants.begin();it!=prime_implicants.end();++it) {
		const Cube& c(*it);
		CVC3::Expr expr(ExprManager::getExprCube(c,pred_exprs));
		//MSG(0,"EncodingManager::getExpr: "+expr.toString()+"\n");
		exprs.push_back(expr);
	}

	/* step (3) */
	CVC3::Expr result( ExprManager::Disjunction(exprs) );
	return result;
}



CVC3::Expr EncodingManager::getExpr(const bdd::BDD& f, const DecodingMap& dmap) const {
	CVC3::Expr e(getExprRecur(f.getDdNode(), 0, dmap));
	return vc.simplify(e);
}


CVC3::Expr EncodingManager::getExprRecur(DdNode* node,
					 int instance,
		  		     	 const DecodingMap& dmap) const {
	CVC3::Expr result;
	if(Cudd_IsConstant(node)) {
		result = vc.trueExpr();
	} else {
		DdNode* reg(Cudd_Regular(node));
		DdNode* left(Cudd_T(reg));
		DdNode* right(Cudd_E(reg));

		if(reg->index >= first_prob_choice && reg->index < first_state) {
			CVC3::Expr left_expr(getExprRecur(left, 2 * instance + 1,dmap));
			CVC3::Expr right_expr(getExprRecur(right,2 * instance,dmap));
			if(left_expr.isTrue())   result = right_expr;
			else if(right_expr.isTrue()) result = left_expr;
			else result = vc.andExpr(left_expr,right_expr);
		} else if(reg->index >= first_state){
			CVC3::Expr left_expr(getExprRecur(left,instance,dmap));
			CVC3::Expr right_expr(getExprRecur(right,instance,dmap));
			int inst ( (reg->index - first_state) % 2 == 0 ? 0 : instance + 1);
			DecodingMap::const_iterator it (dmap.find(std::pair<int,int>(inst,reg->index)));
			if(dmap.end() == it) {
				result = vc.orExpr(left_expr,right_expr);
			} else {
				result = getIte(left,right,it->second,left_expr,right_expr);
			}

		} else {
			CVC3::Expr left_expr(getExprRecur(left,instance,dmap));
			CVC3::Expr right_expr(getExprRecur(right,instance,dmap));
			result = vc.orExpr(left_expr,right_expr);
		}
	}
	return Cudd_IsComplement(node) ? vc.notExpr(result) : result;;
}


CVC3::Expr EncodingManager::EncodingManager::getExpr2(const bdd::BDD& f) const {
	return getExprRecur(f.getDdNode());
}


CVC3::Expr EncodingManager::getExprRecur(DdNode* node) const {
	CVC3::Expr result;
	if(Cudd_IsConstant(node)) {
		result = Cudd_IsComplement(node) ? vc.falseExpr() : vc.trueExpr();
	} else {
		DdNode* reg(Cudd_Regular(node));
		DdNode* left(Cudd_T(reg));
		DdNode* right(Cudd_E(reg));
		int index((reg->index - first_state) / 2);

		CVC3::Expr reg_expr;

		reg_expr = getIte(left,right,pred_exprs[index],getExprRecur(left),getExprRecur(right));

		result = Cudd_IsComplement(node) ? vc.notExpr(reg_expr) : reg_expr;
	}
	return result;
}


CVC3::Expr EncodingManager::getIte(DdNode* left,
								   DdNode* right,
								   CVC3::Expr c,
								   CVC3::Expr le,
								   CVC3::Expr ri) const {
	CVC3::Expr result;
	if(Cudd_IsConstant(left)) {
		if(left == Cudd_ReadLogicZero(dd_mgr.getDdManager())) {
			// reg = c ? false : right
			result = vc.andExpr(vc.notExpr(c),ri);
		} else {
			// reg = c ? true : right
			result = vc.orExpr(c,ri);
		}
	} else if(Cudd_IsConstant(right)) {
		if(right == Cudd_ReadLogicZero(dd_mgr.getDdManager())) {
			// reg = c ? left : false
			result = vc.andExpr(c,le);
		} else {
			// reg = c ? left : true
			result = vc.orExpr(vc.notExpr(c),le);
		}
	} else {
		result = vc.iteExpr(c,le,ri);
	}
	return result;
}

/*************** Members of CubeEncoder ******************/

CubeEncoder::CubeEncoder() :
	em (0) {}

CubeEncoder::CubeEncoder(EncodingManager& __em, int __max_instance) :
	  em(&__em)
	, max_instance(__max_instance)
	, support(em->dd_mgr.True())
{
	const std::vector<bdd::BDD>& prob_choice (em->getProbChoiceRange().getChoiceVars());
	for(std::vector<bdd::BDD>::const_iterator i=prob_choice.begin();i!=prob_choice.end();++i) {
		support &= (*i);
	}
}

CubeEncoder::CubeEncoder(const CubeEncoder& ce) {
	*this = ce;
}

/**
	\param c       cube to be encoded
	\param present sort of the guard of the cube (constraint on present state variables)
	\param bdd     the complete encoding of the cube
	\post  variables present, bdd, trans01 are updated
	*/
void CubeEncoder::encodeTransition(const Cube& c, bdd::BDD& present, bdd::BDD& bdd) const {
	assert(c.size() == inter.size());


	Cube cube(c.size(),l_undef);

	bdd = em->dd_mgr.False();

	for(int inst=0;inst<=max_instance;++inst) {
		for(int i=0;i<(int)inter.size();++i) {
			int instance = inter[i].second;
			cube[i] = (inst == instance) ? c[i] : l_undef;
		}

		bdd::BDD time(em->dd_mgr,cube,inter_bdd);
		if( inst > 0 )
			bdd           |= em->getProbChoiceRange().getBDD(inst-1) & time;
		else {
			present = time;
		}
	}
	bdd &= present;

	assert(!present.isNull());
	assert(!bdd.isNull());
}

void CubeEncoder::encodeState(const Cube& c, bdd::BDD& bdd) {
	assert(c.size() == inter.size());

	bdd::BDD cube(em->getDDManager(),c,inter_bdd);
	bdd = cube;
}

void CubeEncoder::push(StateVar* v, int instance, const CVC3::Expr& e) {
	std::pair<StateVar*,int> p(v,instance);
	inter.push_back(p);
	bdd::BDD state_var_bdd(v->getBDD(instance == 0 ? 0 : 1));
	inter_bdd.push_back(state_var_bdd);
	max_instance = std::max(instance,max_instance);
	support &= state_var_bdd;
	dmap[std::pair<int,int>(instance,state_var_bdd.getIndex())] = e;
}

std::string CubeEncoder::toString() const {
	std::string result;
	result = "CubeEncoder {max_instance = "+ util::intToString(max_instance) + " \n";
	for(Interpretation::const_iterator i=inter.begin(); i!=inter.end();++i) {
		std::pair<StateVar*,int> p(*i);
		StateVar* var(p.first);
		int instance (p.second);
		result += " " +  var->toString() + "$" + util::intToString(instance)+ "\n";
	}
	result +="}\n";
	return result;
}
bool CubeEncoder::operator==(const CubeEncoder& ce) const {
	return em == ce.em
	     &&	inter_bdd == ce.inter_bdd;
}

CubeEncoder& CubeEncoder::operator=(const CubeEncoder& ce) {
	em = ce.em;
	inter = ce.inter;
	inter_bdd = ce.inter_bdd;
	max_instance = ce.max_instance;
	if(!ce.support.isNull())
		support = ce.support;
	return *this;
}

/*************** Members of SymbolicCluster ******************/

SymbolicCluster::SymbolicCluster() :
	  em(0)
	, top(false)
{
}


void SymbolicCluster::makeTop() {
	top = true;
	trans01 = em->dd_mgr.False();
	for(int i=1;i<=ce.getMaxInstance();++i) {
		trans01 |= em->getProbChoiceRange().getBDD(i-1);
	}
}

SymbolicCluster& SymbolicCluster::operator=(const SymbolicCluster& rhs) {
	em = rhs.em;
	ce = rhs.ce;
	top = rhs.top;
	sc = rhs.sc;
	trans01 = rhs.trans01;
	return *this;
}

bool SymbolicCluster::operator==(const SymbolicCluster& rhs) const {
	bool result ( em == rhs.em && top == rhs.top );
	result = result && sc.size() == rhs.sc.size();
	for(Collection::const_iterator it = sc.begin(); result && it!=sc.end(); ++it) {
		Collection::const_iterator it2 (rhs.sc.find(it->first));
		foreach(bdd::BDD bdd,  it2->second) {
			result = result && it->second.count(bdd)>0;
		}
	}
	return result;
}

SymbolicCluster::SymbolicCluster(const CubeEncoder& __ce) :
	  em(__ce.em)
	, ce(__ce)
	, trans01(em->dd_mgr.False())
	, top(false)
{}

SymbolicCluster::SymbolicCluster(std::pair<EncodingManager&,int> p) :
	  em(&p.first)
	, ce(p.first,p.second)
	, trans01(em->dd_mgr.False())
	, top(false)
{}

bdd::BDD SymbolicCluster::computeBDD(EncodingManager& em, const ChoiceRange& cr)
{
	bdd::BDD result = combineTransitions(em, cr);
	return result;
}


void SymbolicCluster::addBDD(const bdd::BDD& prefix, const bdd::BDD& t) {
	assert(!prefix.isNull());
	assert(!t.isNull());
	trans01 |= t;
	Collection::iterator it = sc.find(prefix);

	if(it==sc.end()) {
		std::pair<bdd::BDD,std::unordered_set<bdd::BDD> > p;
		p.first = prefix;
		p.second.insert(t);
		sc.insert(p);
	}
	else
		(it->second).insert(t);
}

int SymbolicCluster::getMaximalSetSize() const {
	int result = 0;
	for(Collection::const_iterator it = sc.begin(); it!=sc.end();++it) {
		result = std::max(result,(int)(it->second).size());
	}
	return result;
}

bdd::BDD SymbolicCluster::Simplify(const bdd::BDD& f) const {
	/*!
		We quantify out the variables that are NOT in the support of the symbolic cluster
		(1) compute the quantification variables
		(2) quantify them out
	 */
	/* step (1) */
	bdd::BDD f_support(f.getSupport());
	bdd::BDD quant(f_support.Simplify(ce.support));
	quant = quant.Simplify(em->getProbChoiceRange().getSupport());
	/* step (2) */
	return f.Exist(quant);
}


bdd::BDD SymbolicCluster::encodeSet(const std::unordered_set<bdd::BDD>& s, EncodingManager& em, const ChoiceRange& cr) const {
	bdd::BDD result = em.dd_mgr.False();

	int choice_nr = 0;
	foreach(const bdd::BDD& factor, s) {
		assert(!factor.isNull());
		bdd::BDD choice_bdd    = cr.getBDD(choice_nr);
		assert(!choice_bdd.isNull());
		result |= choice_bdd & factor;
		++choice_nr;
	}

	return result;
}

/**
	\warning This function has side effects on em
	\post    SymbolicCluster::computeBDD creates a ChoiceRange in the EncodingManager
*/
bdd::BDD SymbolicCluster::combineTransitions(EncodingManager& em, const ChoiceRange& cr) {
	bdd::BDD result  = em.dd_mgr.False();

	if(top) {
		// conjoin possible probabilistic choices
		// with unconstrained update
		result = em.dd_mgr.True();
	} else {
		// go through the different sets
		for(Collection::const_iterator it = sc.begin(); it!=sc.end();++it) {
			result  |= encodeSet(it->second, em, cr);
		}
	}
	/** \note we do not pop the choice range here
			to prevent interference with other SymbolicClusters
		-> we have to make sure that fresh choice variables are enumerated
	*/

	return result;
}

void SymbolicCluster::addTransition(const Cube& c) {
	bdd::BDD present, bdd;
	ce.encodeTransition(c,present,bdd);
	addBDD(present,bdd);
}

std::string SymbolicCluster::toString(const Collection& coll) const {
	std::string result;
	for(Collection::const_iterator i = coll.begin(); i!=coll.end(); ++i) {
		(i->first).PrintMinterm(result);
		result +=" -> \n";
		result +="{\n";
		foreach(const bdd::BDD& bdd, (i->second)) {
			bdd.PrintMinterm(result);
			result +=",\n";
		}
		result +="}\n";
	}
	return result;

}

std::string SymbolicCluster::toString() const {
	std::string result;
	result += toString(sc);
	return result;
}

/*************** Members of SymbolicAbstractCommand ******************/

SymbolicAbstractCommand::Signature dummy_sig(1);

SymbolicAbstractCommand::SymbolicAbstractCommand() :
	  em(&dummy_em)
	, lower_nondet (10000000)
	, upper_nondet(-1)
{
}

SymbolicAbstractCommand::SymbolicAbstractCommand(EncodingManager& __em) :
	  em(&__em)
	, lower_nondet (10000000)
	, upper_nondet (-1)
{
}

SymbolicAbstractCommand::SymbolicAbstractCommand(const SymbolicAbstractCommand& sac) { *this = sac; }

SymbolicAbstractCommand::SymbolicAbstractCommand(EncodingManager& __em, const Signature& __sig) :
	  em(&__em)
	, sig(__sig)
	, lower_nondet (10000000)
	, upper_nondet (-1)
{
}

SymbolicAbstractCommand& SymbolicAbstractCommand::operator=(const SymbolicAbstractCommand& sac) {
	em = sac.em;
	sig = sac.sig;
	//if(!sac.guard.isNull())
	guard = sac.guard;
	//if(!sac.transitions.isNull())
	transitions = sac.transitions;
	transitions_mtbdd = sac.transitions_mtbdd;
	care              = sac.care;
	lower_nondet = sac.lower_nondet;
	upper_nondet = sac.upper_nondet;
	trans01      = sac.trans01;
	vars         = sac.vars;
	state_vars   = sac.state_vars;
	return *this;
}



/*
 * Enumerate the used choices explicitly
 */
void NondetMatrix::splitBDD(
	DdManager* ddman,
	DdNode *dd,
	const std::vector<bdd::BDD>& ndvars,
	int num_ndvars,
	std::vector<bdd::BDD>& matrices,
	int& count,
	int level
	)
{
	// base case - empty matrix
	if (dd == Cudd_ReadLogicZero(ddman)) return;

	// base case - nonempty matrix
	if (level == num_ndvars) {
		matrices[count++] = bdd::BDD(ddman,dd);
		return;
	}

	DdNode *e, *t;

	// recurse
	if (Cudd_NodeReadIndex(dd) > ndvars[level].getIndex()) {
		e = t = dd;
	}
	else {
		e = Cudd_NotCond(Cudd_E(dd),Cudd_IsComplement(dd));
		t = Cudd_NotCond(Cudd_T(dd),Cudd_IsComplement(dd));
	}

	splitBDD(ddman, e, ndvars, num_ndvars, matrices, count, level+1);
	splitBDD(ddman, t, ndvars, num_ndvars, matrices, count, level+1);
}


/* the int signifies the BDD index
 * and the bool whether it is a present (true) or next-state (false) variable */
struct supportPair {
	int index;
	bool present;
	std::string toString() const {
		return "(" + util::intToString(index)+ "," + (present ? "present" : "future") + ")";
	}
	supportPair() : index(0), present(true) {}
	supportPair(const supportPair& p) : index(p.index), present(p.present) {}
	supportPair& operator=(const supportPair& p) { index = p.index; present = p.present; return *this; }

	inline bool operator<(const supportPair& p) const { return index < p.index;	}
	inline bool operator==(const supportPair& p) const { return index == p.index && present == p.present; }
};

typedef std::vector<supportPair> supportVector;

void computeSupportVector(const std::vector<bdd::BDD>& rvars,
						  const std::vector<bdd::BDD>& cvars,
						  supportVector& vars) {
	/*
	 * we assume that rvars and cvars are disjoint
	 * we assume that rvars and cvars are sorted
	 */
	vars.resize(rvars.size()+ cvars.size());


	std::vector<supportPair> rvec(rvars.size());
	for(unsigned i = 0 ; i < rvars.size(); ++i) {
		rvec[i].index = rvars[i].getIndex();
		rvec[i].present = true;
	}

	std::vector<supportPair> cvec(cvars.size());
	for(unsigned i = 0 ; i < cvars.size(); ++i) {
		cvec[i].index = cvars[i].getIndex();
		cvec[i].present = false;
	}

	std::merge(rvec.begin(), rvec.end(), cvec.begin(), cvec.end(), vars.begin());
}

void traverseMatrix (
	NondetMatrix& matrix,
	DdManager *ddman,
	DdNode *dd,
	const supportVector& vars,
	int num_vars,
	int level,
	bdd::OddNode *row,
	bdd::OddNode *col,
	int r,
	int c,
	int branch) {


	// base case - zero terminal
	if (dd == Cudd_ReadLogicZero(ddman)) return;

	assert(row->dd != Cudd_ReadLogicZero(ddman) && col->dd != Cudd_ReadLogicZero(ddman));

	// base case - non zero terminal
	if (level == num_vars) {
		matrix.succ[matrix.starts[r] + branch] = c;
		return;
	}

	DdNode *e, *t;

	// recurse
	if (Cudd_NodeReadIndex(dd) > vars[level].index) {
		e = t = dd;
	}
	else {
		e = Cudd_NotCond(Cudd_E(dd),Cudd_IsComplement(dd));
		t = Cudd_NotCond(Cudd_T(dd),Cudd_IsComplement(dd));
	}

	/* present state variable */
	if(vars[level].present) {
		traverseMatrix(matrix, ddman, e, vars, num_vars, level+1,
		    		    row->e, col, r, c, branch);
		traverseMatrix(matrix, ddman, t, vars, num_vars, level+1,
					    row->t, col, r + row->eoff, c, branch);
	} else {
		traverseMatrix(matrix, ddman, e, vars, num_vars, level+1,
		    		   row, col->e, r, c, branch);
		traverseMatrix(matrix, ddman, t, vars, num_vars, level+1,
					   row, col->t, r, c+col->eoff, branch);
	}
}


void NondetMatrix::traverseRow (
	DdManager *ddman,
	DdNode *dd,
	const std::vector<bdd::BDD>& vars,
	int num_vars,
	int level,
	bdd::OddNode *odd,
	int i,
	int code)
{
	// base case - zero terminal
	if (dd == Cudd_ReadLogicZero(ddman)) return;

	assert(odd->dd != Cudd_ReadLogicZero(ddman));

	// base case - non zero terminal
	if (level == num_vars) {
		switch (code) {

		// mdp - first pass
		case 1:
			starts[i+1] += nr_of_branches;
			break;
		// mdp - second pass
		case 2:
			starts[i] += nr_of_branches;
			break;
		}
		return;
	}

	DdNode *e, *t;

	// recurse
	if (Cudd_NodeReadIndex(dd) > vars[level].getIndex()) {
		e = t = dd;
	}
	else {
		e = Cudd_NotCond(Cudd_E(dd),Cudd_IsComplement(dd));
		t = Cudd_NotCond(Cudd_T(dd),Cudd_IsComplement(dd));
	}
	traverseRow(ddman, e, vars, num_vars, level+1, odd->e, i, code);
	traverseRow(ddman, t, vars, num_vars, level+1, odd->t, i+odd->eoff, code);
}



void SymbolicAbstractCommand::computeMatrixStats(const std::vector<bdd::ODD2*>& odd_vec,
												 int& n,
												 int& nc,
												 int& nnz,
												 int& nm) const {
	/* number of row entries */
	n = odd_vec[0]->getNumOfStates();

	const ChoiceRange& cr(em->getProbChoiceRange());

	DdManager* ddman(em->dd_mgr.getDdManager());

	bdd::BDD next_vars(em->dd_mgr.True());
	for(unsigned i=0; i<sig.size();++i) {
		next_vars &= em->dd_mgr.And(vars[i+1]);
	}

	/* find out the dimensions of the matrix */
	BDD tmp(transitions.Exist(cr.getChoiceVars()).Exist(next_vars));
	nc = tmp.CountMinterm(vars[0].size() + ndvars.size());         // number of probability distributions
	nnz = nc * sig.size();                                           // number of non-zero matrix entries
	tmp = tmp.Exist(vars[0]);
	nm = tmp.CountMinterm(ndvars.size());                            // nondeterministic branching
}


void SymbolicAbstractCommand::toMatrix(
					   const std::vector<bdd::ODD2*>& odd_vec,
					   NondetMatrix& matrix) const {




	assert(odd_vec.size() == vars.size());

	for(unsigned i=0; i<odd_vec.size(); ++i) {
		assert(odd_vec[i]);
	}

	assert(transitions <= odd_vec[0]->getReach());

	MSG(1,"=============== SymbolicAbstractCommand::toMatrix\n");
	matrix.nr_of_branches = sig.size();
	matrix.distr = sig;

	const ChoiceRange& cr(em->getProbChoiceRange());

	DdManager* ddman(em->dd_mgr.getDdManager());

	bdd::BDD next_vars(em->dd_mgr.True());
	for(unsigned i=0; i<sig.size();++i) {
		next_vars &= em->dd_mgr.And(vars[i+1]);
	}


	/* number of row entries */
	int n, nc, nnz, nm;
	computeMatrixStats(odd_vec, n, nc, nnz, nm);

	MSG(1,"SymbolicAbstractCommand::toMatrix vars[0].size %d ndvars.size() %d\n",vars[0].size(),ndvars.size());
	MSG(1,"SymbolicAbstractCommand::toMatrix blocks n %d nc %d nnz %d nm %d branches %d\n",n,nc,nnz,nm,sig.size());

	/* decompose BDD into several deterministic BDDs */
	int count = 0;
	std::vector<BDD> matrices(nm);
	matrix.splitBDD(ddman, transitions.getDdNode(), ndvars, ndvars.size(), matrices, count, 0);

	/* ... and for each store which rows/choices are non-empty */
	std::vector<BDD> matrices_ne(nm);

#if 0
	std::set<BDD> matrices_set;
#endif
	for(int i=0;i<nm;++i) {
		matrices_ne[i] = matrices[i].Exist(cr.getChoiceVars()).Exist(next_vars);
#if 0
		matrices_set.insert(matrices[i]);
#endif
	}
#if 0
	assert(matrices.size()==nm);
#endif

	matrix.starts.resize(n+1);
	matrix.succ.resize(nnz,100000);

	/* traverse BDD to compute how many choices there are for each row ... */
	for(int i=0;i<n+1;++i) {
		matrix.starts[i] = 0;
	}

	for(int i=0;i<nm;++i) {
		matrix.traverseRow(
			 ddman,
			 matrices_ne[i].getDdNode(),
			 vars[0],
			 vars[0].size(),
			 0,
			 odd_vec[0]->getOddNode(),
			 0,
			 1);
	}

	/* ... and use this to compute starts information */
	for(int i=1; i<n+1; ++i) {
		matrix.starts[i] += matrix.starts[i-1];
	}

	std::vector<bdd::BDD> prob_choice(sig.size());
	for(int j=0; j<sig.size(); ++j) {
		prob_choice[j] = cr.getBDD(j);
	}

	std::vector<supportVector> support_vec(sig.size());
	for(int j=0; j<sig.size(); ++j) {
		computeSupportVector(vars[0],vars[j+1],support_vec[j]);
	}

	/* traverse BDD to compute matrix entries */
	for(int i=0; i<nm; ++i) {
		for(int j=0; j<sig.size(); ++j) {
			bdd::BDD cofactor_matrix(matrices[i].Cofactor(prob_choice[j]));

			traverseMatrix(
				  matrix,
				  ddman,
 				  cofactor_matrix.getDdNode(),
			      support_vec[j],
			      support_vec[j].size(),
			      0,
			      odd_vec[0]->getOddNode(),
			      odd_vec[j+1]->getOddNode(),
			      0,
			      0,
			      j);
		}
		matrix.traverseRow(
			 ddman,
			 matrices_ne[i].getDdNode(),
			 vars[0],
			 vars[0].size(),
			 0,
			 odd_vec[0]->getOddNode(),
			 0,
			 2);
	}

	/* restore starts information */
	for (int i = n; i > 0; --i) {
		matrix.starts[i] = matrix.starts[i-1];
	}
	matrix.starts[0] = 0;

#if 0
	bool broke(false);

	for(int i=0; i< n; ++i) {
		std::set<std::string> succs;
		for(unsigned j=matrix.starts[i]; j<matrix.starts[i+1]; j+= matrix.nr_of_branches) {
			std::string s;
			for(unsigned k = 0; k< matrix.nr_of_branches; ++k) {
				s += util::intToString(matrix.succ[j + k]) + ".";
			}
			succs.insert(s);
			// MSG(0,"distr : %s\n",s.c_str());
		}

		if(succs.size() != ((matrix.starts[i+1] - matrix.starts[i])  / matrix.nr_of_branches) ) {
			broke = true;
			MSG(0,"block %d succs.size() %d matrix.starts[i] %d matrix.starts[i+1] %d nr_of_branches %d\n",i,succs.size(),matrix.starts[i],matrix.starts[i+1],matrix.nr_of_branches);
			for(unsigned j=matrix.starts[i]; j<matrix.starts[i+1]; j+= matrix.nr_of_branches) {
				std::string s;
				for(unsigned k = 0; k< matrix.nr_of_branches; ++k) {
					s += util::intToString(matrix.succ[j + k]) + ".";
				}
				MSG(0,"SymbolicAbstractCommand::toMatrix : %s\n",s.c_str());
			}

		}
	}
	if(broke)
			assert(false);
#endif
}


void SymbolicAbstractCommand::clear() {
	lower_nondet = 10000000;
	upper_nondet = -1;
	care.clear();
	transitions_mtbdd.setNull();
	transitions.setNull();
	trans01.setNull();
	ndvars.clear();
	vars.clear();
	state_vars.clear();
}



void SymbolicAbstractCommand::registerCubeEncoder(const CubeEncoder& cube_encoder) {
	const CubeEncoder::Interpretation& new_inter(cube_encoder.getInterpretation());
	for(CubeEncoder::Interpretation::const_iterator i=new_inter.begin();i!=new_inter.end();++i) {
		std::pair<StateVar*,int> p (*i);
		StateVar* v = p.first;
		int instance = p.second;
		trackVariableInstance(v,instance);
	}
}

/*! \brief compute BDD variables in support of the transitions
 *  \pre assumes correct signature sig
 *  \post vars contains BDD variables in support of the transitions BDD
 */
void SymbolicAbstractCommand::computeVars() {
	vars.clear();
	vars.resize(sig.size()+1);

	state_vars.clear();
	state_vars.resize(sig.size()+1);

	foreach(VariableInstancePair& p, care) {
		StateVar* const sv (p.first);
		std::set<int>&  instances(p.second);
		foreach(int instance, instances) {
			assert(instance < vars.size());
			vars[instance].push_back(sv->getBDD(instance > 0 ? 1 : 0 ));
			state_vars[instance].push_back(sv);
		}
	}


#if 0
	/* debugging: ensure that vars is sorted and contains unique entries */
	for(int instance=0; instance<sig.size()+1;++instance) {
		std::vector<BDD> vari(vars[instance]);
		std::sort(vari.begin(),vari.end());

		bool equal = true;
		for(unsigned j=0;j<vars[instance].size();++j) {
			equal = equal && vars[instance][j]==vari[j];
		}


		if(!equal) {
			for(unsigned j=0;j<vars[instance].size();++j) {
				MSG(0,"SymbolicAbstractCommand::computeVars: %d %d %d %d\n",
						vars[instance][j].getIndex(),
						vars[instance][j].getDdNode(),
						vari[j].getIndex(),
						vari[j].getDdNode());
				std::string s(state_vars[instance][j]->toString());
				MSG(0,"SymbolicAbstractCommand::computeVars: %s \n",s.c_str() );
			}
			assert(false);
		}


		std::unique(vari.begin(),vari.end());
		assert(vars[instance].size()==vari.size());
	}
#endif
}


void SymbolicAbstractCommand::trackVariableInstance     (StateVar* v, int instance) {
	// update care
	care[v].insert(instance);
}


/**
  * /post "ndvars" contains non-determinism variables
  * /return BDD encoding of transitions
  */
bdd::BDD SymbolicAbstractCommand::combineClusters(std::vector<SymbolicCluster>& scc) {
	assert(em);
	bdd::BDD result;

	ndvars.clear();

	trans_with_prob = em->dd_mgr.True();

	if(scc.size() == 0) {
		trans_with_prob = result = em->dd_mgr.False();
	} else {

		result = em->dd_mgr.True();

		foreach(SymbolicCluster& sc, scc) {
			int maxsize = sc.getMaximalSetSize();
			const ChoiceRange& cr  = em->createNondetChoiceRange(maxsize);

			ndvars.insert(ndvars.end(),cr.getChoiceVars().begin(),cr.getChoiceVars().end());

			bdd::BDD cluster_bdd (sc.computeBDD(*em,cr));
			result &= cluster_bdd;
			lower_nondet = std::min(lower_nondet,cr.getLower());
			upper_nondet = std::max(upper_nondet,cr.getUpper());
			trans_with_prob &= sc.getTrans01();
		}
	}
	assert(scc.size() <= em->nondet_choice_ranges.size());

	// undo the effect on the EncodingManager
	em->popNondetChoiceRange(scc.size());
	return result;
}

bdd::BDD SymbolicAbstractCommand::computeTrans01(const std::vector<SymbolicCluster>& scc) {
	assert(em);
	bdd::BDD result(em->dd_mgr.True());
	foreach(const SymbolicCluster& sc, scc) {
		result &= sc.getTrans01();
	}

	// quantify out probabilistic choice variables
	result = trans01 = guard & result.ExistAnd(getIdentityUpdates(), em->getProbChoiceRange().getChoiceVars());

	return result;
}

bdd::BDD SymbolicAbstractCommand::computeBDD(std::vector<SymbolicCluster>& scc, bool constrain) {
	computeVars();
	bdd::BDD result = combineClusters(scc);


	// complete the matrix with identity alternatives
	if(constrain) {
		result &= getIdentityUpdates(); // switch identity updates off if local predicates are used
		result &= guard;	
	}
	transitions = result;
	return result;
}

bdd::BDD SymbolicAbstractCommand::getTransitionsOfBranch(int branch) const {
	assert(branch<sig.size());
	const ChoiceRange& cr(em->getProbChoiceRange());
	assert(!transitions.isNull());
	return transitions.Exist(ndvars).Cofactor(cr.getBDD(branch));
}

unsigned SymbolicAbstractCommand::getNumberOfDistributions            () const {
	bdd::BDD next_vars(em->dd_mgr.True());
	for(unsigned i=1; i<sig.size();++i) {
		next_vars &= em->dd_mgr.And(vars[i]);
	}

	/* find out the dimensions of the matrix */
	BDD tmp(transitions.Exist(em->getProbChoiceRange().getChoiceVars()).Exist(next_vars));
	unsigned nc = tmp.CountMinterm(next_vars.size() + ndvars.size());         // number of probability distributions
	return nc;
}


bdd::BDD   SymbolicAbstractCommand::getPost(const bdd::BDD& in, int branch) const {
	return BDD::Post(em->getDDManager(),
					 em->getDDManager().And(vars[0]),
					 vars[branch+1],
					 in,
					 getTransitionsOfBranch(branch));
}

bdd::BDD   SymbolicAbstractCommand::getPre(const bdd::BDD& in, int branch) const {
	return BDD::Pre(em->getDDManager(),
					 vars[0],
					 em->getDDManager().And(vars[branch+1]),
					 in,
					 getTransitionsOfBranch(branch));
}


bdd::MTBDD SymbolicAbstractCommand::getTransitionsMTBDD(bool quantify_out_prob_choice) const {
	if(transitions_mtbdd.isNull())
		throw util::RuntimeError("SymbolicAbstractCommand::getTransitionsMTBDD(): transitions have yet to be computed!\n");

	bdd::MTBDD result;

	if(quantify_out_prob_choice) {
		const ChoiceRange& cr (em->getProbChoiceRange());
		result = transitions_mtbdd.Exist(cr.getChoiceVars());
	}
	else
		result = transitions_mtbdd;

	return result;
}


bdd::MTBDD SymbolicAbstractCommand::computeTransitionsMTBDD() {
	if(transitions.isNull())
		throw util::RuntimeError("SymbolicAbstractCommand::getTransitions(): transitions have yet to be computed!\n");

	const ChoiceRange& cr (em->getProbChoiceRange());

	int max_instance = sig.size();
	assert(sig.size()>0);
	bdd::MTBDD mask = em->dd_mgr.Constant(0.0);
	for(int inst=0;inst<max_instance;++inst) {
		bdd::BDD prob_choice_bdd(cr.getBDD(inst));
		bdd::MTBDD prob_choice_mtbdd(prob_choice_bdd.toMTBDD());
		mask += prob_choice_mtbdd * em->dd_mgr.Constant(sig[inst]);
	}
	bdd::MTBDD result = (transitions.toMTBDD() * mask);
	transitions_mtbdd = result;

// 	MSG(0,"Mask distribution size %d\n",sig.size());
// 	mask.PrintMinterm();
// 	MSG(0,"Transition BDD\n");
// 	transitions.PrintMinterm();
//
// 	MSG(0,"Transition MTBDD\n");
// 	result.PrintMinterm();

	return result;
}

bdd::BDD SymbolicAbstractCommand::getTransitions() const {
	return transitions;
}

/** \todo adjust this to case where different time frames can have different support
 */

bdd::BDD SymbolicAbstractCommand::getIdentityUpdates () const {
	/*
		We go through all the variables instances V whose next value is not affected by the command.
		For each of them we have to make sure that its value is preserved by the command.
		We can determine V by inspecting the signatures of the SymbolicClusters that
		are used to build the transitions of the command.
		The signature tells us which variable instances are affected.
		The EncodingManager keeps track of all state variables.
		With respect to these, we take the complement of the affected variable instances.
	*/
	bdd::BDD result (em->dd_mgr.True());

	const std::vector<StateVar*>& state_variables = em->getStateVarVector();
	int max_instance = sig.size();

	foreach(StateVar* i, state_variables) {
		const StateVar& state_var (*i);
		VariableInstances::const_iterator ci = care.find(i);
		std::set<int> empty_set;
		const std::set<int>& instances (ci == care.end() ? empty_set : ci->second );
		bdd::BDD present (state_var.getBDD());
		bdd::BDD next    (state_var.getBDD(1));
		bdd::BDD identity(present.Ite(next,!next));
		bdd::BDD current (em->dd_mgr.True());
		for(int inst=0;inst<max_instance;++inst) {
			// \todo: only do this if
			if( instances.find(inst+1) == instances.end() ) {
				bdd::BDD prob_choice_bdd(em->getProbChoiceRange().getBDD(inst));
				current &= prob_choice_bdd.Ite(identity,em->dd_mgr.True());
			}
		}
		result &= current;
	}

	return result;
}

bdd::BDD SymbolicAbstractCommand::getGuard      () const { return guard; }

void SymbolicAbstractCommand::forceUnusedChoiceVarsToZero(int global_max) {

	if(!transitions.isNull())
		transitions &= forceUnusedChoiceVarsToZeroBDD(global_max);

	if(!transitions_mtbdd.isNull()) {
		transitions_mtbdd *= forceUnusedChoiceVarsToZeroMTBDD(global_max);
	}
}


bdd::BDD SymbolicAbstractCommand::forceUnusedChoiceVarsToZeroBDD(int global_max) const {
	bdd::BDD result = em->dd_mgr.True();
	if(upper_nondet == -1)
		return result;

	for(int i = upper_nondet+1;i<=global_max;++i) {
		bdd::BDD var (em->dd_mgr.Variable(i));
		result &= !var;
	}
	return result;
}

bdd::MTBDD SymbolicAbstractCommand::forceUnusedChoiceVarsToZeroMTBDD(int global_max) const {
	bdd::MTBDD result = em->dd_mgr.Constant(1.0);
	if(upper_nondet == -1)
		return result;

	for(int i = upper_nondet+1;i<=global_max;++i) {
		bdd::MTBDD var ((em->dd_mgr.Variable(i)).toMTBDD());
		result *= var.Ite(em->dd_mgr.Constant(0.0),em->dd_mgr.Constant(1.0));
	}
	return result;
}



/*************** Members of SymbolicAbstractModel ******************/
SymbolicAbstractModel::SymbolicAbstractModel(EncodingManager& __em, unsigned __nr_of_commands) :
		em(&__em)
	, nr_of_commands(__nr_of_commands)
	, interleaving(em->createNondetChoiceRange(nr_of_commands))
{
}

SymbolicAbstractModel::~SymbolicAbstractModel() {
	em->popNondetChoiceRange();
}

int SymbolicAbstractModel::getMaxNondetVar(const Collection& commands) const {
	// maximal nondeterminism variable
	int max_nondet_var = -1;
	for(Collection::const_iterator it=commands.begin();it!=commands.end();++it) {
		assert(*it);
		const SymbolicAbstractCommand& sac = **it;
		max_nondet_var = std::max(sac.getMaxNondetVar(),max_nondet_var);
	}
	return max_nondet_var;
}

void SymbolicAbstractModel::computeNondetVars(int k) {
	nondet_vars.resize(k);
	for(int i=0;i<k;++i) {
		nondet_vars[i] = em->dd_mgr.Variable(i);
	}
}

void SymbolicAbstractModel::preprocessCommands(Collection& commands) {

	int max_nondet_var = getMaxNondetVar(commands);
	max_nondet_var = std::max(max_nondet_var,em->getCurrentMaxNondetVar());

	computeNondetVars(max_nondet_var+1);
	for(Collection::iterator it=commands.begin();it!=commands.end();++it) {
		assert(*it);
		SymbolicAbstractCommand& sac = **it;
		sac.forceUnusedChoiceVarsToZero(max_nondet_var);
	}
}

bdd::BDD SymbolicAbstractModel::computeTransitionMatrixBDD(Collection& commands) {
	bdd::BDD result = em->dd_mgr.False();
	assert( nr_of_commands == (int)commands.size());

	preprocessCommands(commands);

	int command_nr = 0;
	for(Collection::const_iterator it=commands.begin();it!=commands.end();++it) {
		assert(*it);
		const SymbolicAbstractCommand& sac = **it;
		bdd::BDD factor = sac.getTransitions();
		bdd::BDD choice = interleaving.getBDD(command_nr);
		result         |= choice & factor;
		++command_nr;
	}
	return result;
}


bdd::MTBDD SymbolicAbstractModel::computeTransitionMatrixMTBDD(Collection& commands, bool quantify_out_prob_choice) {
	bdd::MTBDD result = em->dd_mgr.Constant(0.0);
	assert( nr_of_commands == (int)commands.size());

	preprocessCommands(commands);

	nr_of_vars = 2 * em->getStateVarVector().size() + nondet_vars.size() +
		         (quantify_out_prob_choice ? 0 : em->getProbChoiceRange().getChoiceVars().size() );
        ;
	double nr_of_minterms = 0;

	int command_nr = 0;
	for(Collection::const_iterator it=commands.begin();it!=commands.end();++it) {
		assert(*it);
		const SymbolicAbstractCommand& sac = **it;
		bdd::MTBDD factor = sac.getTransitionsMTBDD(quantify_out_prob_choice);
		bdd::MTBDD choice = (interleaving.getBDD(command_nr)).toMTBDD();
		result          = choice.Ite(factor,result);
		double nr_of_factor_minterms = factor.CountMinterm(nr_of_vars);
		nr_of_minterms += nr_of_factor_minterms;

	//	summands.push_back(summand);


		MSG(1,"%d SymbolicAbstractModel::computeTransitionMatrixMTBDD %d %E %E \n", command_nr, factor == em->dd_mgr.Constant(0.0),nr_of_factor_minterms,
		choice.CountMinterm(interleaving.getChoiceVars().size()));
		++command_nr;
	}

	MSG(1,"SymbolicAbstractModel::computeTransitionMatrixMTBDD %E  %E\n",result.CountMinterm(nr_of_vars),nr_of_minterms);
	//result.PrintMinterm();
	return result;
}



}
