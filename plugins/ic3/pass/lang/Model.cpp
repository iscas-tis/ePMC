/******************************** CPPFile *****************************

* FileName [Model.cpp]

* PackageName [main]

* Synopsis [The concrete model:
            a program in guarded transition language.]

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2006 by Saarland University. All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#include "vcl.h"

#include "util/Util.h"
#include "util/Cube.h"
#include "util/Statistics.h"
#include "util/Error.h"
#include "util/Database.h"

#include "SymbolTable.h"
#include "ExprManager.h"
#include "Node.h"
#include "Property.h"
#include "Model.h"

#include "dp/SMT.h"

namespace lang
{

  Model::Model ():Node (), model_type (MDP), maxNrOfAlt (0)
  {
  }

/*********************************************************************/
// Create String from Model.
/*********************************************************************/
  std::string Model::toString () const
  {
    std::string s;

    //go through the variables

    s += "module M\n";
    for (std::vector < std::string >::const_iterator i = var_names.begin ();
	 i != var_names.end (); ++i)
      {
	const std::string & v = *i;
	  std::tr1::unordered_map<std::string,CVC3::Expr>::const_iterator vit =
	  variables.find (*i);
	  assert (vit != variables.end ());
	  s += v + " : " + ((*vit).second.getType ().toString ()) + ";\n";
      }
    unsigned i=0;
    foreach (boost::shared_ptr<Command> c, guarded_transitions) {
    	assert (c);
    	s += "Nr. " + util::intToString(i) + " : " + c->toString ();

	++i;
      }
    s += "endmodule\n";
    s += "init\n" + init.toString () + "\nendinit" + "\n";
    foreach (boost::shared_ptr<Property> p, properties)
      {
    	assert (p);
    	s += p->toString () + "\n";
      }
    return s;
  }

/*********************************************************************/
//get a module by name.
/*********************************************************************/
  Module *Model::getModule (const std::string & module_name) const
  {
    // go through the modules to find suitable one
    for (Modules::const_iterator i = modules.begin (); i != modules.end ();
	 ++i)
      if ((*i)->getName () == module_name)
	return (*i).get ();
    return NULL;

  }

/*********************************************************************/
//Instantiate a given module
/*********************************************************************/
  Module::Module (const std::string & __name,
		  const Module & existing,
		  const CVC3::ExprHashMap < CVC3::Expr > &repl,
		  const HashMap < std::string,
		  std::string > &al):name (__name)
  {

    // recursively descend into synchronized commands and do replacements
    for (SynchronizedCommands::const_iterator i =
	 existing.sync_guarded_transitions.begin ();
	 i != existing.sync_guarded_transitions.end (); ++i)
      {
	std::string action = (*i).first;
	HashMap < std::string, std::string >::const_iterator ait =
	  al.find (action);
	if (ait != al.end ())
	  {
	    action = (*ait).second;
	  }
	const Commands & gts = (*i).second;
	for (Commands::const_iterator i = gts.begin (); i != gts.end (); ++i)
	  {
	    Command *gt = new Command (**i, repl);
	    gt->setAction (action);
	    addCommand (gt);
	  }

    }

    // recursively descend into commands and do replacements
    for (Commands::const_iterator i = existing.guarded_transitions.begin ();
	 i != existing.guarded_transitions.end (); ++i)
      addCommand (new Command (**i, repl));

    for (HashMap < std::string, CVC3::Expr >::const_iterator vit =
	 existing.local_vars.begin (); vit != existing.local_vars.end ();
	 ++vit)
      {
	CVC3::Expr expr = (*vit).second.substExpr (repl);
	local_vars.insert(std::pair<std::string,CVC3::Expr>(expr.getName (), expr));
      }

  }

/*********************************************************************/
//insert a variable.
/*********************************************************************/
  void Model::addVariable (const std::string & id, const CVC3::Expr & var,
			   bool input)
  {
    if (variables.find (id) != variables.end ())
      {
	// maybe check if the type is compatible: if yes we're content
	return;
      }
    var_names.push_back (id);
    variables.insert(std::pair<std::string,CVC3::Expr>(id, var));
    if (input)
      input_variables.insert (var);
  }

/*********************************************************************/
//insert a variable.
/*********************************************************************/
  void Model::addVariable (const std::string & id,
			   const CVC3::Expr & var,
			   const CVC3::Expr & lower,
			   const CVC3::Expr & upper, bool input)
  {

    if (variables.find (id) != variables.end ())
      {
	// maybe check if the type is compatible: if yes we're content
	return;
      }
    var_names.push_back (id);
    variables.insert(std::pair<std::string,CVC3::Expr>(id, var));
    variable_bounds[var] =
      std::pair < CVC3::Expr, CVC3::Expr > (lower, upper);
    if (input)
      input_variables.insert (var);
  }


  void Model::computeDefaultInitialValue ()
  {
    init = vc.trueExpr ();
    std::vector < CVC3::Expr > init_vec;
    for (CVC3::ExprHashMap < CVC3::Expr >::iterator it =
	 default_initial_value.begin (); it != default_initial_value.end ();
	 ++it)
      {
	// for bools CVC3 does not allow equality only equivalence (iff)
	if ((it->second).isBoolConst ())
	  {
	    if ((it->second).isTrue ())
	      {
		init_vec.push_back (it->first);
	      }
	    else
	      {
		init_vec.push_back (vc.notExpr (it->first));
	      }
	  }
	else
	  {
	    init_vec.push_back (vc.eqExpr (it->first, it->second));
	  }
      }
    init = ExprManager::Conjunction (init_vec);
  }

/*********************************************************************/
// Apply AST walker.
/*********************************************************************/
  void Model::Apply (DFSAdapter & a) const
  {
/* initial.getExpr()->Apply(a);
 for(Commands::const_iterator it = guarded_transitions.begin();
     it!=guarded_transitions.end(); ++it)
  (*it)->Apply(a);*/
  }


/*********************************************************************/
//add an action :-)
/*********************************************************************/
  void Model::addAction (const Action & a)
  {
    assert (a != "");
    actions.insert (a);
  }

/*********************************************************************/
// Model destructor
/*********************************************************************/
  Model::~Model ()
  {
  }

  void Model::addModule (Module * module)
  {
    assert (module);
    boost::shared_ptr < Module > mod_ptr (module);
    modules.push_back (mod_ptr);
  }

  void Model::CTMC2CTMDP ()
  {
    dp::SMT &smt(dp::SMT::getSMT());

    /* Build model to get all guard combinations that may be valid at
     * the same time */
    std::vector < CVC3::Expr > variables;
    std::vector < std::string > variable_names;
    CVC3::Expr guard_combinations = vc.trueExpr ();
    CVC3::Expr not_all_guards_off = vc.falseExpr ();
    for (unsigned i = 0; i != guarded_transitions.size (); ++i)
      {
	const Command & gt = *guarded_transitions[i];
	const CVC3::Expr & guard (gt.getGuard ());
	std::ostringstream o;
	o << i;
	const std::string variable_name ("G" + o.str ());
	variable_names.push_back (variable_name);
	const CVC3::Expr guard_var (vc.
				    varExpr (variable_name, vc.boolType ()));
	variables.push_back (guard_var);
	const CVC3::Expr guard_clause (vc.iffExpr (guard, guard_var));
	guard_combinations = guard_combinations && guard_clause;
	not_all_guards_off = not_all_guards_off || guard_var;
      }

    for (unsigned i = 0; i < invariants.size (); i++)
      {
	guard_combinations = guard_combinations && invariants[i];
      }

    guard_combinations = guard_combinations && not_all_guards_off;
    guard_combinations = vc.simplify (guard_combinations);
    std::cout << std::endl << guard_combinations << std::endl;

    /* now enumerate all possible guard combinations and build new commands */
    smt.pushContext ();
    smt.Assert (guard_combinations);
    Commands collection;
    while (true)
      {
	/* get next guard combination */
	lbool smt_result = smt.Solve ();
	if (smt_result != l_true)
	  break;
	std::cout << "COMBINATION" << std::endl;
	CVC3::ExprHashMap < CVC3::Expr > exprMap;
	smt.getModel (variables, exprMap);
	smt.blockModel (exprMap);

	/* build new command */
	Command *new_command = new Command ();
	new_command->setGuard (vc.trueExpr ());
	new_command->setAction ("");

	for (unsigned i = 0; i != guarded_transitions.size (); ++i)
	  {
	    CVC3::ExprHashMap < CVC3::Expr > bla;
	    const Command *gt =
	      new Command (*guarded_transitions[i].get (), bla);
	    const CVC3::Expr & variable (variables[i]);
	    const CVC3::Expr & value = exprMap[variable];
	    /* if old command in current combination, add all possible
	     * alternatives */
	    if (value == vc.trueExpr ())
	      {
		/* @bjoern: is this a leak?

		   @moritz: not if you use shared pointers: shared_ptr implements Garbage Collection
		 */
		new_command = *gt + *new_command;
	      }
	  }
	boost::shared_ptr < Command > gt_ptr (new_command);
	collection.push_back (gt_ptr);
      }
    smt.popContext ();

    guarded_transitions.clear ();
    //  guarded_transitions = collection;
    maxNrOfAlt = 0;
#if 1
    //  guarded_transitions.clear();
    //finally add the collected transitions
    for (Commands::const_iterator git = collection.begin ();
	 git != collection.end (); ++git)
      {
	boost::shared_ptr < Command > gt = *git;
	assert (gt.get ());
	guarded_transitions.push_back (gt);
	unsigned NrOfAlt = gt->getNrOfAlt ();
	maxNrOfAlt = maxNrOfAlt > NrOfAlt ? maxNrOfAlt : NrOfAlt;
      }
#endif

    std::cout << toString () << std::endl;
    std::cout << "DONE ENUM" << std::endl;
  }

/*********************************************************************/
//merge all modules into the model
//   1. collect the guarded transitions
//   2. multiply out synchronized guarded transitions
/*********************************************************************/
  void Model::Flatten() {
	util::Statistics::flattenTimer.Start();
	maxNrOfAlt = 0;

	MSG(1,"Model::Flatten %d\n",modules.size());

	//   1. collect the guarded transitions
	for (Modules::const_iterator mit = modules.begin(); mit != modules.end(); ++mit) {
		const Module & module = **mit;
		const Commands & gts = module.guarded_transitions;
		for (Commands::const_iterator git = gts.begin(); git != gts.end(); ++git) {
			boost::shared_ptr < Command> gt_ptr(*git);
			guarded_transitions.push_back(gt_ptr);
			unsigned NrOfAlt = gt_ptr->getNrOfAlt();
			maxNrOfAlt = maxNrOfAlt > NrOfAlt ? maxNrOfAlt : NrOfAlt;
		}
	}
	//   2. multiply out synchronized guarded transitions
	for (Actions::const_iterator ait = actions.begin(); ait != actions.end(); ++ait) {
		Commands collection;
		const Action & action(*ait);
		MSG(1,"Model::Flatten: considering synchronization on " + action + " \n")
		for (Modules::const_iterator mit = modules.begin(); mit
				!= modules.end(); ++mit) {

			const Module & module = **mit;
			const SynchronizedCommands & sgts = module.sync_guarded_transitions;
			SynchronizedCommands::const_iterator sit = sgts.find(action);
			if (sit == sgts.end())
				continue;
			MSG(1,"Model::Flatten: module " + module.getName() + "\n");

			const Commands & gts = (sit->second);
			//initialize if necessary
			if (collection.empty()) {
				collection = gts;
				continue;
			}

			Commands new_collection;
			//multiply all guarded transitions
			for (Commands::iterator git1 = collection.begin(); git1
					!= collection.end(); ++git1) {
				for (Commands::const_iterator git2 = gts.begin(); git2
						!= gts.end(); ++git2) {
					assert (*git1);
					assert (*git2);
					Command *gt = (**git1) * (**git2);
					//if the guards are disjoint there may be no such guarded transition
					if (gt) {
						boost::shared_ptr < Command> gt_ptr(gt);
						new_collection.push_back(gt_ptr);
					}
				}

			}
			collection = new_collection;
			MSG(1,"Model::Flatten: collection size %d\n",collection.size());
		}
		//finally add the collected transitions
		foreach (boost::shared_ptr < Command>& gt, collection) {
			assert (gt.get ());
			guarded_transitions.push_back(gt);
			unsigned NrOfAlt = gt->getNrOfAlt();
			maxNrOfAlt = maxNrOfAlt > NrOfAlt ? maxNrOfAlt : NrOfAlt;
		}

	}

	if (init.isNull()) {
		computeDefaultInitialValue();
	}
	MSG (1, "nr of variables %d\n", variables.size ());
	MSG (1, "Maximal number of alternatives in assignment: %d\n",
			getMaxNrOfAlt ());

	util::Statistics::flattenTimer.Stop();

}

  bool Model::usesOnlyConstantRates ()
  {
    for (unsigned gt_nr = 0; gt_nr < guarded_transitions.size (); gt_nr++)
      {
	Command & gt = *guarded_transitions[gt_nr];
	const Alternatives & asss = gt.getAlternatives ();
	for (unsigned ass_nr = 0; ass_nr < asss.size (); ass_nr++)
	  {
	    Alternative & ass = *asss[ass_nr];
	    if (!ass.isRateDouble())
	      {
	    	return false;
	      }
	  }
      }

    return true;
  }

  Model & Model::operator= (const Model & m)
  {
    init = m.init;
    guarded_transitions = m.guarded_transitions;
    properties = m.properties;
    modules = m.modules;
    user_predicates = m.user_predicates;
    invariants = m.invariants;
    var_names = m.var_names;
    input_variables = m.input_variables;
    variables = m.variables;
    actions = m.actions;
    maxNrOfAlt = m.maxNrOfAlt;
    default_initial_value = m.default_initial_value;
    variable_bounds = m.variable_bounds;
    state_rewards = m.state_rewards;
    return *this;
  }


//! \brief create copy of guarded transition with expressions replaced (given by [lhs/rhs])
  Command::Command (const Command & existing,
		    const CVC3::ExprHashMap < CVC3::Expr > &repl)
  {
    action = existing.action;
    g = existing.g.substExpr (repl);

    // do replacements in alternatives
    for (Alternatives::const_iterator i = existing.alternatives.begin ();
	 i != existing.alternatives.end (); ++i)
      {
	addAlternative (new Alternative (**i, repl));
      }
  }

  void Command::setGuard (const CVC3::Expr & __g)
  {
    std::vector < CVC3::Expr > conj;
    ExprManager::getTopLevelConjuncts (__g, conj);
    g = ExprManager::Conjunction (conj);
  }

  bool Command::LvalueCompatible (const CVC3::Expr & e) const
  {
    //if(ExprManager::LvalueCompatible(e,g)) return true;

    for (Alternatives::const_iterator i = alternatives.begin ();
	 i != alternatives.end (); ++i)
      {
	if ((*i)->LvalueCompatible (e))
	  return true;
      }
    return false;
  }


/*********************************************************************/
//convert to string
/*********************************************************************/
  std::string Module::toString ()const
  {
    std::string s;
    for (SynchronizedCommands::const_iterator it =
	 sync_guarded_transitions.begin ();
	 it != sync_guarded_transitions.end (); ++it)
      {
	const Commands & gts = it->second;
	for (Commands::const_iterator it = gts.begin ();
	     it != gts.end (); ++it)
	    s += (*it)->toString ();
      }
    s += "\n";
    return s;

  }

/*********************************************************************/
//apply DFS Walker
/*********************************************************************/
  void Module::Apply (DFSAdapter & a) const
  {
    for (SynchronizedCommands::const_iterator it =
	 sync_guarded_transitions.begin ();
	 it != sync_guarded_transitions.end (); ++it)
      {
	const Commands & gts = it->second;
	for (Commands::const_iterator it = gts.begin ();
	     it != gts.end (); ++it)
	    (*it)->Apply (a);
      }
    for (Commands::const_iterator it = guarded_transitions.begin ();
	 it != guarded_transitions.end (); ++it)
      (*it)->Apply (a);
  }

/*********************************************************************/
//add a guarded transition
/*********************************************************************/
  void Module::addCommand (Command * c)
  {
    boost::shared_ptr < Command > c_ptr (c);
    std::string a(c->getAction());
    if (a == "")
      {
	guarded_transitions.push_back (c_ptr);
      }
    else
      {
	sync_guarded_transitions[a].push_back (c_ptr);
      }

  }

  Alternative::Alternative( const Alternative& existing, const CVC3::ExprHashMap<CVC3::Expr>& repl ) {
  	for(Map::const_iterator i = existing.map.begin(); i!=existing.map.end(); ++i) {
  		Assign((i->first).substExpr(repl),(i->second).substExpr(repl));
  	}
  	p = existing.p.substExpr(repl);
  	is_rate_double = existing.is_rate_double;
  	p_double = existing.p_double;
  }

  bool Alternative::LvalueCompatible( const CVC3::Expr& e) const {
  	for(Map::const_iterator i=map.begin();i!=map.end();++i)
  		if(ExprManager::LvalueCompatible(e,i->second))
  			return true;
  	return false;
  }

  void Alternative::Assign(const CVC3::Expr& left, const CVC3::Expr& right) {
  	if(left.isNull() || right.isNull()) {
  		throw util::RuntimeError("Assignment::Assign: assignment to Null "+left.toString()+" -> "+right.toString());
  	}
  	if(map.count(left)>0) {
		throw util::RuntimeError("Assignment::Assign: Conflicting assignment"
		+left.toString()+" -> { "+right.toString() + ","+map[left].toString() + "}");
	}
  	map.insert(left,right);
  	support.insert(left);
  }


  std::string Alternative::toString() const {
  	std::string s;
  	s+= p.toString() + " : ";

  	for(Map::const_iterator i = map.begin(); i!=map.end(); ++i) {
  		if(i!=map.begin())
  			s+="& ";
  		s+=" ( "+(i->first).toString()+"' = "+(i->second).toString()+" ) ";
  	}

  	return s;
  }

  void Alternative::Apply(DFSAdapter& a) const {
  }

  double Alternative::getRateAsDouble() const {
  	if(!is_rate_double) {
  		throw util::RuntimeError("Alternative::getRateAsDouble: rate is non-constant " + p.toString() + "\n");
  	}
  	return p_double;
  }

  bool Alternative::isRateDouble() {
    return is_rate_double;
  }

  Alternative* Alternative::Clone() const {
  	Alternative* a = new Alternative();
  	a->p = p;
  	a->is_rate_double = is_rate_double;
  	a->p_double = p_double;
  	a->map = map;
  	a->support = support;
  	return a;
  }

  void Alternative::Cleanup() {
  }

  /*********************************************************************/
  //return a pointer to an assignment
  //containing
  /*********************************************************************/
  Alternative* Alternative::operator*(const Alternative& ass) const {
  	Alternative* a = Clone();
  	a->p_double *= ass.p_double;
  	a->is_rate_double &= ass.is_rate_double;
  	if(a->is_rate_double) {
  		std::string nr(util::floatToString(a->p_double));
  		a->p = vc.ratExpr(nr,10);

  	} else
  		a->p = vc.multExpr(a->p, ass.p);

  	a->map = map;
  	for(Map::const_iterator i=ass.map.begin(); i!=ass.map.end();++i)
  		a->Assign(i->first,i->second);

  	return a;
  }

  void Alternative::CollectLhs(std::set<CVC3::Expr>& collection ) const {
  	collection = support;
  }

  CVC3::Expr Alternative::operator[](const CVC3::Expr& var) const {
  	Map::const_iterator i = map.find(var);
  	if(i==map.end())
  		return i->second;
  	else
  		return CVC3::Expr();
  }

  CVC3::Expr Alternative::operator()(const CVC3::Expr& e) const {
  	return e.substExpr(map);
  }

  void Alternative::getSuccessor(CVC3::ExprHashMap<CVC3::Expr>& state, CVC3::ExprHashMap<CVC3::Expr>& successor) const {
  	/* generate successors of all variable values for which we have an entry */
  	for(Map::const_iterator i=map.begin();i!=map.end();++i) {
  		successor[i->first] = vc.simplify((i->second).substExpr(state));
  	}

  	/* copy other variables values to successor table */
  	for(CVC3::ExprHashMap<CVC3::Expr>::iterator i=state.begin();i!=state.end();++i) {
  	  if (0 == successor.count(i->first)) {
  	    successor[i->first] = i->second;
  	  }
  	}
  }

  void Alternative::setWeight(const CVC3::Expr& weight) {
	  p = vc.simplify(weight);
	  switch(p.getKind()) {
	  		case CVC3::RATIONAL_EXPR:
	  			{
	  				const CVC3::Rational& rat (p.getRational());
	  				double numerator ((double)rat.getNumerator().getInt());
	  				double denominator ((double)rat.getDenominator().getInt());
	  				p_double = numerator / denominator;
	  				if(p_double < 0) {
	  					throw util::ParseError("Negative probability "
	  					+ util::floatToString(p_double) + "\n");
	  				}
	  				is_rate_double = true;
	  			}
	  		break;
	  		default:
	  			p_double = -1.0;
	  			is_rate_double = false;
	  		break;
	  	}
  }

/*********************************************************************/
// Constructor
/*********************************************************************/
  Command::Command ()
  {
  }

void Command::factorize(
	const std::vector<int>& prob_choices,
	Alternative::Map& base,
	std::vector<Alternative::Map>& alt
) const {
	alt.clear();
	alt.resize(prob_choices.size());
	foreach(CVC3::Expr v,support) {
		std::vector<CVC3::Expr> rhs;
		std::set<CVC3::Expr> s;
		rhs.reserve(prob_choices.size());
		foreach(int choice, prob_choices) {
			assert(choice < alternatives.size());
			const Alternative& alt(*alternatives[choice]);
			rhs.push_back(alt(v));
			s.insert(rhs.back());
		}
		if(s.size()>1) {
			for(unsigned i=0; i<alt.size();++i)
				alt[i][v] = rhs[i];
		} else if(s.size()==1) {
			base[v] = rhs[0];
		}
	}
}


/** \brief compute a constraint on the present states based on constraint on present and next states
 *  \pre v.size == nr of branches of command + 1
 */
CVC3::Expr Command::WP(const CVC3::Expr& e, unsigned i) const {
	assert(i<alternatives.size());
	const lang::Alternative& a = *alternatives[i];
	return a(e);
}




/*********************************************************************/
// check if the probabilities within a guarded transition sum up to 1.
/*********************************************************************/
  void Command::checkSanity ()
  {
    for (Alternatives::iterator it = alternatives.begin ();
	 it != alternatives.end (); ++it)
      {
	// we can only check constant rates
	if (((*it)->isRateDouble ()) && ((*it)->getRateAsDouble () < 0))
	  {
	    MSG (1, "Warning negative probabilities are dangerous!!\n");
	  }
      }
  }

/*********************************************************************/
// add an Assignment to the guarded transition.
/*********************************************************************/
  void Command::addAlternative (Alternative * a)
  {
    assert (a);
    boost::shared_ptr < Alternative > a_ptr (a);
    /* check if probability makes sense */
    alternatives.push_back (a_ptr);
    support.insert(a->getSupport().begin(),a->getSupport().end());
  }


/*********************************************************************/
// Write the guarded transition to a string.
/*********************************************************************/
  std::string Command::toString ()const
  {
    std::string s;
    s += "[" + action + "] ";
    s += g.toString () + " -> ";
    for (Alternatives::const_iterator it = alternatives.begin ();
	 it != alternatives.end (); ++it)
      {
	assert (*it);
	if (it != alternatives.begin ())
	  s += "\n + ";
	s += (*it)->toString ();
      }
    s += ";\n";
    return s;
  }

/*********************************************************************/
// Apply the AST walker.
/*********************************************************************/
  void Command::Apply (DFSAdapter & a) const
  {
//   g.getExpr()->Apply(a);
//   for(Alternatives::const_iterator it = alternatives.begin(); it!=alternatives.end();++it)
//     (*it)->Apply(a);
  }

/*********************************************************************/
//Warning: may also return 0
/*********************************************************************/
  Command *Command::operator* (const Command & gt) const
  {
    //go through all alternatives, compose them and multiply their probabilities

    //return 0 if guards are disjoint
    if ( /*ExprManager::LvalueCompatible(g,gt.g) &&  */ ExprManager::
	DisjointWith (g, gt.g))
      return 0;
    Command *ng = new Command ();
      ng->g = vc.andExpr (g, gt.g);

      ng->action = action;
    for (Alternatives::const_iterator it1 = alternatives.begin ();
	 it1 != alternatives.end (); ++it1)
      {
	for (Alternatives::const_iterator it2 = gt.alternatives.begin ();
	     it2 != gt.alternatives.end (); ++it2)
	  {
	    assert (*it1);
	    assert (*it2);
	    ng->addAlternative ((**it1) * (**it2));
	  }
      }
    return ng;
  }

/*********************************************************************/
//Warning: may also return 0
/*********************************************************************/
  Command *Command::operator+ (const Command & gt) const
  {
    //go through all alternatives, compose them and multiply their probabilities

    //return 0 if guards are disjoint
    if ( /*ExprManager::LvalueCompatible(g,gt.g) &&  */ ExprManager::
	DisjointWith (g, gt.g))
      return 0;
    Command *ng = new Command ();
      ng->g = vc.andExpr (g, gt.g);

      ng->action = action;
    for (Alternatives::const_iterator it1 = alternatives.begin ();
	 it1 != alternatives.end (); ++it1)
      {
	assert (*it1);
	ng->addAlternative ((*it1).get ());
      }
    for (Alternatives::const_iterator it2 = gt.alternatives.begin ();
	 it2 != gt.alternatives.end (); ++it2)
      {
	assert (*it2);
	ng->addAlternative ((*it2).get ());
      }
    return ng;
  }

}				//end of lang
