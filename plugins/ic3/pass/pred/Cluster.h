

namespace pred {

/*! \brief One cluster is a set of co-dependent predicates */
class Cluster {
	public:

	CVC3::Expr guard;
	PredSet mod;                       //! modified predicates (group G_2 in Qest'07 paper)
	PredSet rel;                       //! relevant but unmodified predicates (group G_1 in Qest'07 paper)
	std::vector<CVC3::Expr> invar;          //! assertions needed to check the command
	CVC3::Expr learned_constraint;
	std::vector<CVC3::Expr> transition_constraints;

    dp::YicesSMT smt;

	Cluster();
	Cluster(const Cluster& c) { *this = c; }
	Cluster(const CVC3::Expr& __guard,
		const PredSet&    __mod,
		const PredSet&    __rel,
		const std::vector<CVC3::Expr>& __invar);

	Cluster& operator=(const Cluster& f);

	bool operator==(const Cluster& c) const;

	std::string toString() const;

	static void computeClusters(const std::vector<CVC3::Expr>&,
			      const lang::Command&,
			      const PredSet&,
			      std::vector<Cluster>&,
			      CVC3::Expr& e,
			      bool CartesianAbstraction);
	size_t hash() const;

	private:
	static bool isModified(const lang::Command&, const Predicate&) ;

	void Set(const CVC3::Expr& __guard,
		const PredSet&    __mod,
		const PredSet&    __rel,
		const std::vector<CVC3::Expr>& __invar);

	static void computeClusters(
		const std::vector<CVC3::Expr>& invariants, // invariants of the command
		const lang::Command& gt,    // the command
		const PredSet& preds,           // set of predicates
		const PredSet& mod_preds,
		std::vector<Cluster>& result,
		CVC3::Expr& e);
};

}

namespace std
{
namespace tr1 {
	template<> struct hash< pred::Cluster >
	{
		size_t operator()( const pred::Cluster& x ) const
		{
			return x.hash();
		}
	};
}
}

