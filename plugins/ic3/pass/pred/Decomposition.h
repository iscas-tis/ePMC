

namespace pred {

class Decomposition {
	public:

	CVC3::Expr guard;
	std::vector<PredSet> mod_vec;                   //! modified predicates (group G_2 in Qest'07 paper)
	PredSet rel;                                    //! relevant but unmodified predicates (group G_1 in Qest'07 paper)
	std::vector<CVC3::Expr> invar;                  //! assertions needed to check the command
	CVC3::Expr learned_constraint;
	std::vector<CVC3::Expr> transition_constraints;

	Decomposition();
	Decomposition(const Decomposition& c) ;
	Decomposition(const CVC3::Expr&,const std::vector<PredSet>&,const PredSet&,const std::vector<CVC3::Expr>&);
	Decomposition& operator=(const Decomposition& f);
	bool operator==(const Decomposition& c) const;
	std::string toString() const;
	static void computeDecompositions(
		const std::vector<CVC3::Expr>&,
		const lang::Command&,
		const std::vector<const PredSet*>&,
		std::vector<Decomposition>&,
		CVC3::Expr& e);
	size_t hash() const;

	unsigned getNrOfPredicates() const;

	private:

	static void getSupportVec(const CVC3::Expr&  e,
				   const std::vector<CVC3::Expr>& table,
				   Signature<unsigned>::boolVector& support);
	static void vecUnion(std::vector<bool>& a, const std::vector<bool>& b);
	static void vecUnion(std::vector<bool>& a, const std::vector<std::vector<bool> >& bs);
	static bool isModified(const lang::Command&, const Predicate&) ;
	void Set(const CVC3::Expr&,const std::vector<PredSet>&, const PredSet&,const std::vector<CVC3::Expr>&);
};

}

namespace std
{
namespace tr1 {
	template<> struct hash< pred::Decomposition >
	{
		size_t operator()( const pred::Decomposition& x ) const
		{
			return x.hash();
		}
	};
}
}

