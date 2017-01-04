#include "util/Util.h"
#include "util/Database.h"
#include "util/aiSee.h"
#include "BDD.h"
#include "ODD.h"


namespace bdd {

OddNode::OddNode() : dd(0), e(0), t(0), eoff(-1), toff(-1) {}
OddNode::OddNode(DdNode *__dd,
		OddNode *__e,
		OddNode *__t) :
	dd(__dd), e(__e), t(__t), eoff(-1), toff(-1) {}

ODD::ODD() {
	num_odd_nodes = 0;
	num_of_states = 0;
	odd = NULL;
}

ODD::ODD(const MTBDD& reach, const std::vector<MTBDD>& vars) {
	buildOdd(reach,vars);
}

ODD::~ODD() {
	destroyOdd();
}


void ODD::rebuild(const MTBDD& reach, const std::vector<MTBDD>& vars) {
	destroyOdd();
	buildOdd(reach,vars);
}

void ODD::buildOdd(const MTBDD& reach, const std::vector<MTBDD>& __vars) {
	ddman = reach.getDdManager();
	vars = __vars;
	num_vars = vars.size();

	// build tables to store odd nodes
	DdNodeMapTable ddtables(num_vars+1);

	// reset node counter
	num_odd_nodes = 0;

	var_indices.resize(num_vars);

	for(int i=0; i<num_vars;++i) {
		var_indices[i] = vars[i].getIndex();
	}


	// call recursive bit
	odd = buildOddRec(reach.getDdNode(), 0, ddtables);

	// add offsets to odd
	addOffsets(odd, 0, num_vars);

	num_of_states = odd->eoff + odd->toff;
}

void ODD::destroyOdd() {
	for(std::vector<OddNode*>::iterator i=odd_nodes.begin();i!=odd_nodes.end();++i)
		delete *i;
	odd_nodes.clear();
	num_odd_nodes = 0;
	num_of_states = 0;
	vars.clear();
	var_indices.clear();
}

OddNode* ODD::buildOddRec(DdNode *dd, int level,
		          DdNodeMapTable& ddtables)
{
	OddNode *ptr;

	// see if we already have odd in the tables
	DdNodeMap::const_iterator it (ddtables[level].find(dd));
	if(it!=ddtables[level].end()) {
		ptr = it->second;
		assert(ptr);
	} else {
		num_odd_nodes++;
		ddtables[level][dd] = ptr = new OddNode(dd,0,0);
		odd_nodes.push_back(ptr);
		// and recurse...
		if (level == num_vars) {}
		else if (var_indices[level] < dd->index) {
			ptr->e = buildOddRec(dd, level+1, ddtables);
			ptr->t = ptr->e;
		} else {
			ptr->e = buildOddRec(Cudd_E(dd), level+1, ddtables);
			ptr->t = buildOddRec(Cudd_T(dd), level+1, ddtables);
		}
	}
	return ptr;
}

//------------------------------------------------------------------------------

long ODD::addOffsets(OddNode *node, int level, int num_vars)
{
	if ((node->eoff == -1) || (node->toff == -1)) {
		if (level == num_vars) {
			if (node->dd == Cudd_ReadZero(ddman)) {
				node->eoff = 0;
				node->toff = 0;
			}
			else {
				node->eoff = 0;
				node->toff = 1;
			}
		}
		else {
			node->eoff = addOffsets(node->e, level+1, num_vars);
			node->toff = addOffsets(node->t, level+1, num_vars);
		}
	}

	return node->eoff + node->toff;
}

//------------------------------------------------------------------------------

void ODD::enumerateStates(const MTBDD& dd, StateVisitor& visitor) const {
	assert(odd);
	enumerateStatesRec(dd.getDdNode(),0,odd,0,visitor);
}

void ODD::enumerateStatesRec(DdNode *dd,
				 int level,
				 OddNode* odd,
				 long o,
				 StateVisitor& visitor) const
{
	DdNode *e, *t;

	if (dd == Cudd_ReadZero(ddman)) return;

	if (level == num_vars) {
		visitor(o,Cudd_V(dd));
		return;
	}
	else if (dd->index > var_indices[level]) {
		e = t = dd;
	}
	else {
		e = Cudd_E(dd);
		t = Cudd_T(dd);
	}

	enumerateStatesRec(e, level+1, odd->e, o, visitor);
	enumerateStatesRec(t, level+1, odd->t, o+odd->eoff, visitor);
}


std::vector<double> dummy;

struct VectorVisitor : StateVisitor {

	VectorVisitor(unsigned int n, std::vector<double>& __vec) : vec(__vec) {
		vec.resize(n);
		foreach(double& entry, vec)
			entry = 0;
	}

	virtual void operator()(int i,double d) {
		vec[i] = d;
	}
	std::vector<double>& vec;
};

void ODD::toDoubleVector(const MTBDD& dd, DoubleVector& result) const {
	assert(odd);
	int n = odd->eoff + odd->toff;

	VectorVisitor vec_visitor(n,result);

	enumerateStates(dd, vec_visitor);
}


void ODD::toCube(int nr, Cube& result) const {
	result.resize(num_vars,l_undef);
	OddNode *current = odd;
	int diff;
	for(int level = 0; level < num_vars; ++level) {
		diff = nr - current->eoff;
		if ( diff < 0 ) {
			current = current->e;
			result[level] = l_false;
		}
		else if ( diff < current->toff ) {
			nr = diff;
			current = current->t;
			result[level] = l_true;
		} else {
			// value out of range
			assert(false);
		}
	}
}

MTBDD ODD::toMTBDDRec(const DoubleVector& vec, int level, OddNode *odd, long o)
const {
	MTBDD result, e, t;

	if (level == num_vars) {
		result = MTBDD(ddman, Cudd_addConst(ddman,vec[o]));
	}
	else {
		if (odd->eoff > 0) {
			e = toMTBDDRec(vec, level+1, odd->e, o);
		}
		else {
			e = MTBDD(ddman, Cudd_addConst(ddman,0.0));
		}
		if (odd->toff > 0) {
			t = toMTBDDRec(vec, level+1, odd->t, o+odd->eoff);
		}
		else {
			t = MTBDD(ddman, Cudd_addConst(ddman,0.0));
		}
		if (e == t) {
			result = e;
		}
		else {
			result = vars[level].Ite(t, e);
		}
	}
	return result;
}

MTBDD ODD::toMTBDD(const DoubleVector& vec) const {
	assert(odd);
	return toMTBDDRec(vec,0,odd,0);
}

std::string ODD::toString() const {
	std::string result = toStringRec(odd,0);
	return result;
}

std::string ODD::toStringRec(OddNode* node, int level) const {
	std::string result;
	std::string indent;

	for(int i=0;i<level;++i) {
		indent +=" ";
	}

	if(level == num_vars)
		return "";

	if(node->e==node->t) {
		result += indent + util::intToString(node->eoff) +"\n";
		result += indent + toStringRec(node->e,level+1);
	} else {
		result += indent + util::intToString(node->eoff) +"\n";
		result += indent + toStringRec(node->e,level+1);
		result += indent + util::intToString(node->toff) + "\n";
		result += indent + toStringRec(node->t,level+1);
	}
	return result;
}

void ODD::aiSee(std::ostream &stream) const {
	stream << "graph: {\n"
	       << "display_edge_labels: yes\n";

	foreach(OddNode* odd, odd_nodes) {
		std::string label;
		if(odd->dd == Cudd_ReadZero(ddman)) {
			label = "0";
		}
		util::aiSeeNode(stream,util::intToString((long)odd),label);
		aiSee(stream,ddman,odd);
	}


	stream << "}\n";
}
void ODD::aiSee(std::ostream &stream, DdManager* ddman, OddNode* odd) const {
	std::string id(util::intToString((long)odd));
	if(odd->e) {
		std::string target(util::intToString((long)odd->e));
		std::string label(util::intToString(odd->eoff));
		util::aiSeeEdge(stream, id, target, label, 1,"blue");
	}

	if(odd->t) {
		std::string target(util::intToString((long)odd->t));
		std::string label(util::intToString(odd->toff));
		util::aiSeeEdge(stream, id, target, label, 1,"red");
	}
}


/**** ODD with BDD ****/

ODD2::ODD2() {
	num_odd_nodes = 0;
	num_of_states = 0;
	odd = NULL;
}

ODD2::ODD2(const BDD& __reach, const std::vector<int>& vars) : reach(__reach) {
	buildOdd(reach,vars);
}

ODD2::~ODD2() {
	destroyOdd();
}


void ODD2::rebuild(const BDD& reach, const std::vector<int>& var_indices) {
	destroyOdd();
	buildOdd(reach,var_indices);
}

void ODD2::buildOdd(const BDD& __reach, const std::vector<int>& __var_indices) {

	reach = __reach;

	ddman = reach.getDdManager();

	var_indices = __var_indices;
	num_vars = var_indices.size();

	// build tables to store odd nodes
	DdNodeMapTable ddtables(num_vars+1);

	// reset node counter
	num_odd_nodes = 0;

	vars.resize(num_vars);

	for(int i=0; i<num_vars;++i) {
		vars[i] = BDD(ddman,Cudd_bddIthVar(ddman,var_indices[i]));
	}

	/* debugging: make sure everything is sorted */

		std::vector<int> vari(var_indices);
		std::sort(vari.begin(),vari.end());

		bool equal = true;
		for(unsigned j=0;j<var_indices.size();++j) {
			equal = equal && var_indices[j]==vari[j];
		}

		if(!equal) {
			assert(false);
		}



	// call recursive bit
	odd = buildOddRec(reach.getDdNode(), 0, ddtables);

	// add offsets to odd
	addOffsets(odd, 0, num_vars);

	num_of_states = odd->eoff + odd->toff;
}


void ODD2::destroyOdd() {
	for(std::vector<OddNode*>::iterator i=odd_nodes.begin();i!=odd_nodes.end();++i)
		delete *i;
	odd_nodes.clear();
	num_odd_nodes = 0;
	num_of_states = 0;
	vars.clear();
	var_indices.clear();
}

OddNode* ODD2::buildOddRec(DdNode *dd, int level,
		          DdNodeMapTable& ddtables)
{
	OddNode *ptr;

	// see if we already have odd in the tables
	DdNodeMap::const_iterator it (ddtables[level].find(dd));
	if(it!=ddtables[level].end()) {
		ptr = it->second;
		assert(ptr);
	} else {
		num_odd_nodes++;
		ddtables[level][dd] = ptr = new OddNode(dd,0,0);
		odd_nodes.push_back(ptr);
		// and recurse...
		if (level == num_vars) {
		}
		else if (var_indices[level] < Cudd_NodeReadIndex(dd)) {
			ptr->e = buildOddRec(dd, level+1, ddtables);
			ptr->t = ptr->e;
		} else {
			DdNode* e(Cudd_NotCond(Cudd_E(dd),Cudd_IsComplement(dd)));
			DdNode* t(Cudd_NotCond(Cudd_T(dd),Cudd_IsComplement(dd)));

			ptr->e = buildOddRec(e, level+1, ddtables);
			ptr->t = buildOddRec(t, level+1, ddtables);
		}
	}
	return ptr;
}

//------------------------------------------------------------------------------

long ODD2::addOffsets(OddNode *node, int level, int num_vars)
{
	if ((node->eoff == -1) || (node->toff == -1)) {
		if (level == num_vars) {
			if (node->dd == Cudd_ReadLogicZero(ddman)) {
				node->eoff = 0;
				node->toff = 0;
			}
			else {
				node->eoff = 0;
				node->toff = 1;
			}
		}
		else {
			node->eoff = addOffsets(node->e, level+1, num_vars);
			node->toff = addOffsets(node->t, level+1, num_vars);
		}
	}

	return node->eoff + node->toff;
}

//------------------------------------------------------------------------------

void ODD2::enumerateStates(const BDD& dd, StateVisitor2& visitor) const {
	assert(odd);
	enumerateStatesRec(dd.getDdNode(),0,odd,0,visitor);
}

void ODD2::enumerateStatesRec(DdNode *dd,
				 int level,
				 OddNode* odd,
				 long o,
				 StateVisitor2& visitor) const
{
	DdNode *e, *t;

	if (dd == Cudd_ReadLogicZero(ddman)) return;

	if (level == num_vars) {
		visitor(o);
		return;
	}
	else if (Cudd_NodeReadIndex(dd) > var_indices[level]) {
		e = t = dd;
	}
	else {
		e = Cudd_NotCond(Cudd_E(dd),Cudd_IsComplement(dd));
		t = Cudd_NotCond(Cudd_T(dd),Cudd_IsComplement(dd));
	}

	enumerateStatesRec(e, level+1, odd->e, o, visitor);
	enumerateStatesRec(t, level+1, odd->t, o+odd->eoff, visitor);
}

struct VectorVisitor2 : StateVisitor2 {

	VectorVisitor2(unsigned int n, std::vector<bool>& __vec) : vec(__vec) {
		vec.clear();
		vec.resize(n,false);
	}

	virtual void operator()(int i) {
		vec[i] = true;
	}
	std::vector<bool>& vec;
};

void ODD2::toBoolVector(const BDD& dd, BoolVector& result) const {
	assert(odd);
	int n = odd->eoff + odd->toff;

	VectorVisitor2 vec_visitor(n,result);

	enumerateStates(dd, vec_visitor);
}


void ODD2::toCube(int nr, Cube& result) const {
	result.resize(num_vars,l_undef);
	OddNode *current = odd;
	int diff;
	for(int level = 0; level < num_vars; ++level) {
		diff = nr - current->eoff;
		if ( diff < 0 ) {
			current = current->e;
			result[level] = l_false;
		}
		else if ( diff < current->toff ) {
			nr = diff;
			current = current->t;
			result[level] = l_true;
		} else {
			// value out of range
			assert(false);
		}
	}
}

BDD ODD2::toBDDRec(const BoolVector& vec, int level, OddNode *odd, long o)
const {
	BDD result, e, t;

	if (level == num_vars) {
		result = BDD(ddman,Cudd_NotCond(Cudd_ReadOne(ddman),!vec[0]));
	}
	else {
		if (odd->eoff > 0) {
			e = toBDDRec(vec, level+1, odd->e, o);
		}
		else {
			e = BDD(ddman,Cudd_ReadLogicZero(ddman));
		}
		if (odd->toff > 0) {
			t = toBDDRec(vec, level+1, odd->t, o+odd->eoff);
		}
		else {
			t = BDD(ddman,Cudd_ReadLogicZero(ddman));
		}
		if (e == t) {
			result = e;
		}
		else {
			result = vars[level].Ite(t, e);
		}
	}
	return result;
}

BDD ODD2::toBDD(const BoolVector& vec) const {
	assert(odd);
	return toBDDRec(vec,0,odd,0);
}

std::string ODD2::toString() const {
	std::string result;
	for(unsigned i=0; i<vars.size(); ++i) {
		result += "var["+ util::intToString(i)+"] = "+ util::intToString(vars[i].getIndex()) + "\n";

	}

	result += toStringRec(odd,0);

	return result;
}

std::string ODD2::toStringRec(OddNode* node, int level) const {
	std::string result;
	std::string indent;

	for(int i=0;i<level;++i) {
		indent +=" ";
	}

	if(level == num_vars)
		return "";

	if(node->e==node->t) {
		result += indent + util::intToString(node->eoff) +"\n";
		result += indent + toStringRec(node->e,level+1);
	} else {
		result += indent + util::intToString(node->eoff) +"\n";
		result += indent + toStringRec(node->e,level+1);
		result += indent + util::intToString(node->toff) + "\n";
		result += indent + toStringRec(node->t,level+1);
	}
	return result;
}

void ODD2::aiSee(std::ostream &stream) const {
	stream << "graph: {\n"
	       << "display_edge_labels: yes\n";

	foreach(OddNode* odd, odd_nodes) {
		std::string label;
		if(odd->dd == Cudd_ReadLogicZero(ddman)) {
			label = "F";
		} else if(odd->dd == Cudd_ReadOne(ddman)) {
			label = "T";
		} else {
			label = util::intToString(Cudd_NodeReadIndex(odd->dd));
		}

		util::aiSeeNode(stream,util::intToString((long)odd),label);
		aiSee(stream,ddman,odd);
	}


	stream << "}\n";
}
void ODD2::aiSee(std::ostream &stream, DdManager* ddman, OddNode* odd) const {
	std::string id(util::intToString((long)odd));
	if(odd->e) {
		std::string target(util::intToString((long)odd->e));
		std::string label(util::intToString(odd->eoff));
		util::aiSeeEdge(stream, id, target, label, 1,"blue");
	}

	if(odd->t) {
		std::string target(util::intToString((long)odd->t));
		std::string label(util::intToString(odd->toff));
		util::aiSeeEdge(stream, id, target, label, 1,"red");
	}
}
}
