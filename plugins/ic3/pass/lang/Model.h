#ifndef Model_H
#define Model_H

#include "Node.h"
#include "Property.h"

namespace lang
{


  class Module;
  class Command;
  class Alternative;

/*! container for set of Alternatives */
  class Alternatives : public std::vector < boost::shared_ptr < Alternative > >{};
/*! container for set of Commands */
  class Commands : public std::vector < boost::shared_ptr < Command > >{};
/*! container for set of synchronized Commands */
  class SynchronizedCommands : public std::map < std::string, Commands >{};
/*! container for set of properties */
  class Properties : public std::vector < boost::shared_ptr < Property > > {};
/*! \typedef action label */
  typedef std::string Action;
/*! \typedef container for set of action labels */
  typedef std::set < std::string > Actions;
/*! \typedef container for set of modules */
  typedef std::vector < boost::shared_ptr < Module > >Modules;

  typedef std::unordered_map < std::string, CVC3::Expr> Variables;

  enum ModelType
  {
    DTMC, MDP, CTMC, CTMDP
  };

/*! \brief a model

          The getter functions are the most interesting part for
	  people who want to use the class.
	  The other functions are mainly used for model construction
	  which is done automatically by the parser.
 */
  struct Model:Node
  {
	/*************************/
    /* The getter functions  */
	/*************************/
    /*! \brief Get module by name
       \return pointer to module if found, NULL otherwise
     */
    Module *getModule (const std::string &) const;

    /*! \brief Getter for initial condition */
    inline const CVC3::Expr & getInitial () const
    {
      return init;
    }

    /*! \brief setter for initial default value */
    inline const CVC3::Expr & getDefaultInitialValue (const CVC3::Expr & var) const
    {
      CVC3::ExprHashMap < CVC3::Expr >::const_iterator it =
	default_initial_value.find (var);
      if (it != default_initial_value.end ())
	{
	  return it->second;
	}
      else
	{
	  static CVC3::Expr dummy;
	    return dummy;
	}
    }

    /*! \brief Getter for guarded transitions */
    inline const Commands & getCommands () const
    {
      return guarded_transitions;
    }

    /*! \brief Getter for user-provided predicates */
    inline const std::vector < CVC3::Expr > &getUserPreds () const
    {
      return user_predicates;
    }

    /*! \brief Getter for properties */
    inline const Properties & getProperties () const
    {
      return properties;
    }

    /*! \brief Getter for invariants */
    inline const std::vector < CVC3::Expr > &getInvar () const
    {
      return invariants;
    }

    /*! \brief Maximal number of alternatives in assignment
       \note  For example [a] x>0 -> 0.5 : (x'=1) + 0.5 : (x'=2) has 2 alternatives
       while [a] x>0 -> 0.5 : (x'=1) + 0.25 : (x'=2) + 0.25: (x'=3) has three.
     */
    inline unsigned getMaxNrOfAlt () const
    {
      return maxNrOfAlt;
    }

    /*! \brief Variable is a free input (doesn't remain constant under transition relation) */
    inline bool isInputVariable (const CVC3::Expr & var) const
    {
      return input_variables.find (var) != input_variables.end ();
    }

    inline bool isParameterVariable (const CVC3::Expr & var) const
    {
      return parameter_variables.find (var) != parameter_variables.end ();
    }


    /*! \brief get the kind of model i.e. deterministic vs. non-deterministic, continuous vs. discrete-time */
    ModelType getModelType () const
    {
      return model_type;
    }


	/*****************************************************************/
    /* The following part is only interesting for model construction */
	/*****************************************************************/

    /*! \brief setter for initial default value */
    inline void setDefaultInitialValue (const CVC3::Expr & var,
					const CVC3::Expr & val)
    {
      default_initial_value[var] = val;
    }

    /*! \brief get the kind of model i.e. deterministic vs. non-deterministic, continuous vs. discrete-time */
    void setModelType (ModelType mt)
    {
      model_type = mt;
    }

    /*! \brief Flatten multiplies out all synchronized guarded transitions
       \pre The model is complete, i.e. contains all desired modules
       \post The guarded transitions of the flattened model are in field Model::guarded_transitions
     */
    void Flatten ();

    /*! brief Convert CTMC to CTMDP i.e. single active command per state
     */
    void CTMC2CTMDP ();

    /* Model construction functions invoked by parser */
    /*! \brief add a module */
    void addModule (Module *);

    /*! \brief register an action */
    void addAction (const Action &);

    /*! \brief Define the initial condition */
    void setInitial (const CVC3::Expr & __init)
    {
      init = __init;
    }

    /*! \brief add a variable */
    void addVariable (const std::string &, const CVC3::Expr &, bool input =
		      false);

    void addVariable (const std::string & id, const CVC3::Expr & var,
		      const CVC3::Expr & lower, const CVC3::Expr & upper,
		      bool input = false);

    /*! \brief add another predicate */
    void addPredicate (const CVC3::Expr & __expr)
    {
      user_predicates.push_back (__expr);
    }

    /*! \brief add another property */
    void addProperty (Property * p)
    {
      assert (p);
      boost::shared_ptr < Property > ptr (p);
      properties.push_back (ptr);
    }

    /*! \brief add invariant */
    void addInvariant (const CVC3::Expr & __expr)
    {
      invariants.push_back (vc.simplify(__expr));
    }

    /*! \brief add guarded state reward */
    void addStateReward
      (const CVC3::Expr & __guard, const CVC3::Expr & __reward)
    {
      state_rewards.push_back (std::pair < CVC3::Expr,
			       CVC3::Expr > (__guard, __reward));
    };

    /*! get guarded state rewards */
    std::vector < std::pair < CVC3::Expr, CVC3::Expr > >&getStateRewards ()
    {
      return state_rewards;
    };

    inline void clearStateRewards() {
      state_rewards.clear();
    }

    inline void clearTransRewards() {
      trans_rewards.clear();
    }

    /*! \brief add guarded transition reward */
    void addTransReward
      (const CVC3::Expr & __guard, const CVC3::Expr & __reward)
    {
      trans_rewards.
	push_back (std::make_pair (std::make_pair ("", __guard), __reward));
    };

    /*! \brief add guarded transition reward */
    void addTransReward
      (const Action & __action, const CVC3::Expr & __guard,
       const CVC3::Expr & __reward)
    {
      trans_rewards.
	push_back (std::
		   make_pair (std::make_pair (__action, __guard), __reward));
    };


    /*! get guarded transition rewards */
    std::vector < std::pair < std::pair < Action, CVC3::Expr >,
      CVC3::Expr > >&getTransRewards ()
    {
      return trans_rewards;
    };

    Model ();
    ~Model ();

    /*! \brief symbol table of all variables */
    std::tr1::unordered_map < std::string, CVC3::Expr > variables;
    std::hash_set < CVC3::Expr > input_variables;
    std::hash_set < CVC3::Expr > parameter_variables;

    /* Node functions */
    virtual std::string toString () const;
    virtual void Apply (DFSAdapter & a) const;

    Model (const Model & m)
    {
      *this = m;
    }
    Model & operator= (const Model &);

    /*! brief check if the model only uses constant rates */
    bool usesOnlyConstantRates ();

	/**     \brief compute implicit default initial values of variables
		\note PRISM language implicitly assumes that the lower bound of the variable range is initial value
		      if no explicit initial expression is given
		\post the initial condition is initialized to default expression
	*/
    void computeDefaultInitialValue ();

    bool hasBounds (const CVC3::Expr & var) const
    {
      return variable_bounds.find (var) != variable_bounds.end ();
    }
    std::pair < CVC3::Expr, CVC3::Expr > getBounds (const CVC3::Expr & var)
    {
      return variable_bounds[var];
    }

  private:
    ModelType model_type;	//! DTMC, DTMDP, CTMC, CMTDP,...
    CVC3::Expr init;		//! initial states
    Commands guarded_transitions;	//! guarded transitions
    Properties properties;	//! properties
    Modules modules;		//! modules
    std::vector < CVC3::Expr > user_predicates;	//! user-added predicates
    std::vector < CVC3::Expr > invariants;	//! invariants
    CVC3::ExprHashMap < std::pair < CVC3::Expr, CVC3::Expr > >variable_bounds;	//! bound on a variable
    CVC3::ExprHashMap < CVC3::Expr > default_initial_value;	//! default initial value if no explicit initial state given
    std::vector < std::string > var_names;	//! variables of global namespace
    Actions actions;		//! set of actions
    unsigned maxNrOfAlt;	//! maximal number of alternatives in assignment
    std::vector < std::pair < CVC3::Expr, CVC3::Expr > >state_rewards;
    std::vector < std::pair < std::pair < Action, CVC3::Expr >,
      CVC3::Expr > >trans_rewards;
  };

/*! \brief A model is typically the parallel composition of different modules */
  struct Module:Node
  {
	/*****************************************************************/
    /* The following part is only interesting for model construction */
	/*****************************************************************/
    /*! \brief constructor */
    Module (std::string __name):name (__name)
    {
    }
    /*! \brief dummy constructor */
    Module ()
    {
    }
    /*! \brief Instantiate with existing module
       \par existing module in which to substitute
       \par lhs left-hand sides of substitution
       \par rhs right-hand sides of substitution
       \par al  mapping of action labels
     */
    Module (const std::string & name,
	    const Module & existing,
	    const CVC3::ExprHashMap < CVC3::Expr > &repl,
	    const HashMap < std::string, std::string > &al);


    /*! \brief add a command */
    void addCommand (Command *);

    /* Getters */
    const std::string & getName () const
    {
      return name;
    }

    /* Node functions */
    virtual std::string toString () const;
    virtual void Apply (DFSAdapter & a) const;

  private:
    friend class Model;

    SynchronizedCommands sync_guarded_transitions;	//! synchronized guarded transtions
    Commands guarded_transitions;	//! interleaved guarded transitions
    std::tr1::unordered_map < std::string, CVC3::Expr > local_vars;	//! local variables
    std::string name;		//! name of the module
  };

  class Alternative : Node
  {
   public:
  	Alternative() : is_rate_double(true), p_double(1)  {}

  	//! \brief create copy of guarded transition with substitution[lhs/rhs]
  	//Assignment ( const Assignment&, const std::vector<Expr>& lhs, const std::vector<Expr>& rhs);
  	//! \brief create copy of guarded transition with substitution[lhs/rhs]
  	Alternative ( const Alternative&, const CVC3::ExprHashMap<CVC3::Expr>& repl);

  	void setWeight(const CVC3::Expr&);
	inline const CVC3::Expr &getWeight() {
	  return p;
	}


  	//! \brief return rate as double (if possible!!)
  	//! \brief returns whether rate is a double
  	bool isRateDouble();
  	double getRateAsDouble() const;

  	/*! \brief does any right-hand side share an identifier with given expr */
  	bool LvalueCompatible( const CVC3::Expr& ) const;

  	/* Node functions */
  	virtual std::string toString() const;
  	virtual void Apply(DFSAdapter& a) const;
  	virtual Alternative* Clone() const;
  	virtual void Cleanup();
  	//void Assign(BasicExpr* left, BasicExpr* right);

  	void Assign(const CVC3::Expr& left, const CVC3::Expr& right);

  	/*! \brief multiply with another assignment */
  	Alternative* operator*(const Alternative& ass) const;
  	/*! \brief access to lhs given a right-hand side */
  	//Expr operator[](const Expr&) const;

  	CVC3::Expr operator[](const CVC3::Expr&) const;

  	// Apply an assignment to an expression
  	CVC3::Expr operator()(const CVC3::Expr& e) const;

  	// Apply an assignment to a state
  	void getSuccessor(CVC3::ExprHashMap<CVC3::Expr>& state, CVC3::ExprHashMap<CVC3::Expr>& successor) const;

  	void CollectLhs(std::set<CVC3::Expr>& collection) const;

  	typedef CVC3::ExprHashMap<CVC3::Expr> Map;

  	inline const Map& getMap() const { return map; }

  	const std::set<CVC3::Expr>& getSupport() const { return support; }

  	private:
  		CVC3::Expr p; //! weight of the assignment
  		bool is_rate_double;
  	  	double p_double; //! weight of assignment in case it's constant
  		Map map;
  		Map wp_cache;
  		std::set<CVC3::Expr> support;
  };

/*! \brief A command
    \note A command consists of an action label, a guard, and different weighted alternatives
 */
  struct Command : public Node
  {
	/*************************/
    /* The getter functions  */
	/*************************/
    /*! \brief getter for Command::guard */
    inline const CVC3::Expr & getGuard () const
    {
      return g;
    }
    /*! \brief getter for Command::action */
    inline const std::string & getAction () const
    {
      return action;
    }
    /*! \brief getter for Alternatives */
    inline const Alternatives & getAlternatives () const
    {
      return alternatives;
    }

    /*! \brief does any right-hand side share an identifier with given expr */
    bool LvalueCompatible (const CVC3::Expr &) const;

    inline unsigned getNrOfAlt () const
    {
      return alternatives.size ();
    }
	/*****************************************************************/
    /* The following part is only interesting for model construction */
	/*****************************************************************/
    /*! \brief setter for Command::guard */
    void setGuard (const CVC3::Expr & __g);

    /*! \brief setter for Command::action */
    void setAction (const std::string & __action)
    {
      action = __action;
    }
    /*! \brief add another Assignment */
    void addAlternative (Alternative *);
    /*! \brief Perform consistency check on guarded transition (probabilities sum up to 1) */
    void checkSanity ();

    //! \brief plain constructor
    Command ();
    /*! \brief create copy of guarded transition with some variables replaced
       \par lhs left-hand sides of substitution
       \par rhs right-hand sides of substitution
       \par al  mapping of action labels
     */
    Command (const Command & gt,
	     const CVC3::ExprHashMap < CVC3::Expr > &repl);

    /* Node functionality */
    virtual std::string toString () const;
    virtual void Apply (DFSAdapter & a) const;

    const Alternative& operator[](unsigned choice) const { assert(choice < alternatives.size()); return *alternatives[choice]; }

    /*! \brief compute sum of two commands
       \note ([] guard_1 -> D_1) + ([] guard_2 -> D_2) = [] guard_1 & guard_2 -> D_1 + D_2 */
    Command *operator+ (const Command &) const;

    /*! \brief compute product for synchronous parallel composition
       \note ([] guard_1 -> D_1) * ([] guard_2 -> D_2) = [] guard_1 & guard_2 -> D_1 * D_2
       @see Model::Flatten()
     */
    Command *operator* (const Command &) const;

    /*!
     * get the updates in which the alternatives
     * of the command differ
     * Example:
     * for command
     * [] x > 2 -> 0.1: (x'=1) & (y'=1) + 0.8: (x'=2) & (y'=1) + 0.1: (x'=3) & (y'=1)
     * prob_choices 0, 2
     * => ( base = [y'=1] , alt = { [x -> 1], [x -> 3] } )
     */
    void factorize(
    		const std::vector<int>& prob_choices,
    		Alternative::Map& base,
    		std::vector<Alternative::Map>& alt
    ) const;

    const std::set<CVC3::Expr>& getSupport() const {return support; }


    CVC3::Expr WP(const CVC3::Expr& e, unsigned i) const;
  private:
    Action action;		//! action on which transition is triggered
    CVC3::Expr g;		//! guard
    Alternatives alternatives;	//! weighted alternatives
    std::set<CVC3::Expr> support;


    friend class Model;
    friend class AbsModel;
  };

}				// namespace lang


#endif
