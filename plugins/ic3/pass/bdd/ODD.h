#ifndef __ODD_HH
#define __ODD_HH

namespace bdd {

typedef std::vector<bool>   BoolVector;
typedef std::vector<double> DoubleVector;

struct OddNode
{
	OddNode() ;
	OddNode(DdNode *__dd, OddNode *__e, OddNode *__t);
	DdNode *dd;
	OddNode *e, *t;
	long eoff, toff;
};


class StateVisitor {
public:
	virtual void operator()(int,double) {}
	virtual ~StateVisitor() {}
};


/**
	\brief offset-labeled decision diagram (ODD) class
	\note ODDs are used to convert between decision diagrams and vectors,
	      i.e. between symbolic and explicit-state encoding of states and distributions
*/
class ODD {
public:
	ODD();
	~ODD();
	ODD(const MTBDD& reach, const std::vector<MTBDD>& vars);

	void enumerateStates(const MTBDD& dd, StateVisitor& visitor) const;

	void toDoubleVector(const MTBDD& dd, DoubleVector& result) const;
	MTBDD toMTBDD(const DoubleVector& vec) const;
	void toCube(int nr, Cube& result) const;

	inline int getNumOfStates() const { return num_of_states; }
	OddNode* getOddNode() const { return odd; }

	void rebuild(const MTBDD& reach, const std::vector<MTBDD>& vars);

	std::string toString() const;

	const std::vector<int>& getVarIndices() const { return var_indices; }
	const std::vector<MTBDD>& getVariables() const { return vars; }

	void aiSee(std::ostream &stream) const;
	void aiSee(std::ostream &stream, DdManager* ddman, OddNode* odd) const;

private:
	typedef std::map<DdNode*,OddNode*> DdNodeMap;
	typedef std::vector<DdNodeMap> DdNodeMapTable;

	std::string toStringRec(OddNode* node, int level) const;

	void buildOdd(const MTBDD& reach, const std::vector<MTBDD>& vars);
	void destroyOdd();

	OddNode *buildOddRec(DdNode*, int, DdNodeMapTable& );

	long addOffsets(OddNode *odd, int level, int num_vars);

	void enumerateStatesRec(DdNode*, int, OddNode*, long, StateVisitor& visitor) const;

	MTBDD toMTBDDRec(const DoubleVector&,int,OddNode*, long) const;

	DdManager* ddman;
	OddNode* odd;
	int num_odd_nodes;
	int num_of_states;
	int num_vars;
	std::vector<MTBDD> vars;
	std::vector<int> var_indices;
	std::vector<OddNode*> odd_nodes;
};


class StateVisitor2 {
public:
	virtual void operator()(int) {}
	virtual ~StateVisitor2() {}
};


class ODD2 {
public:
	ODD2();
	~ODD2();
	ODD2(const BDD& reach, const std::vector<int>& vars);
	void rebuild(const BDD& reach, const std::vector<int>& vars);

	void enumerateStates(const BDD& dd, StateVisitor2& visitor) const;

	void toBoolVector(const BDD& dd, BoolVector& result) const;
	BDD toBDD(const BoolVector& vec) const;
	BDD toBDD(int nr) const;
	void toCube(int nr, Cube& result) const;

	inline const BDD& getReach() const { return reach; }
	inline int getNumOfStates() const { return num_of_states; }
	OddNode* getOddNode() const { return odd; }



	std::string toString() const;

	const std::vector<int>& getVarIndices() const { return var_indices; }
	const std::vector<BDD>& getVariables() const { return vars; }


	void aiSee(std::ostream &stream) const;
	void aiSee(std::ostream &stream, DdManager* ddman, OddNode* odd) const;

private:
	typedef std::map<DdNode*,OddNode*> DdNodeMap;
	typedef std::vector<DdNodeMap> DdNodeMapTable;

	std::string toStringRec(OddNode* node, int level) const;

	void buildOdd(const BDD& reach, const std::vector<int>& var_indices);
	void destroyOdd();

	OddNode *buildOddRec(DdNode*, int, DdNodeMapTable& );

	long addOffsets(OddNode *odd, int level, int num_vars);

	void enumerateStatesRec(DdNode*, int, OddNode*, long, StateVisitor2& visitor) const;

	BDD toBDDRec(const BoolVector&,int,OddNode*, long) const;
	DdManager* ddman;
	OddNode* odd;

	BDD reach;

	int num_odd_nodes;
	int num_of_states;
	int num_vars;
	std::vector<BDD> vars;
	std::vector<int> var_indices;
	std::vector<OddNode*> odd_nodes;
};


}


#endif
