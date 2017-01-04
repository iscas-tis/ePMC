#include "util/Error.h"
#include "util/Util.h"
#include "util/Cube.h"
#include "util/Database.h"
#include <limits>
using namespace util;

#include "AST.h"

#include "vcl.h"
#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"


#include "SymbolTable.h"
#include "ExprManager.h"
#include "Node.h"

#include "pred/Predicate.h"
#include "pred/PredSet.h"
#include "Property.h"
#include "Model.h"

#include "PRISMParser.h"
using namespace lang;

extern void PRISMparse();
// TODO fix this such that it also runs on CIP
//extern int PRISMlex_destroy (void );

namespace PRISM {

AST::Model PRISMParser::astModel;

AST::Substitution constants;



typedef std::unordered_map<std::string, CVC3::Expr> VarTable;

void translateModel(AST::Model& am, Model& m, VarTable& vt);

PRISMParser::PRISMParser() {
	table = new VarTable();
}

PRISMParser::~PRISMParser() {
	((VarTable*)table)->clear();
	delete (VarTable*) table;
}

  void PRISMParser::run(const std::string& file, lang::Model &model)
    {
    	line_number = 1;
		try {
			if(!freopen(file.c_str(),"r",stdin)) {
				throw RuntimeError("File "+file+" not found\n");
			}

    		PRISMparse();

    		if(util::Database::PrettyPrintModel) {
    			std::ofstream ast_file("out.ast");
    			ast_file << PRISMParser::astModel.toString() << std::endl;
    		}

    		translateModel(PRISMParser::astModel,model,*(VarTable*)table);
    		PRISMParser::astModel.clear(); // avoid double insertions into ::model
		} catch ( ParseError& e) {
			std::cout<<e<<std::endl;
			exit(10);
		} catch ( TypeError& e) {
			std::cout<<e<<std::endl;
			exit(10);
		} catch ( RuntimeError& e) {
			std::cout<<e<<std::endl;
			exit(10);
		}
		fclose(stdin);
		//        PRISMlex_destroy();
    }

    CVC3::Expr translateExpr(boost::shared_ptr<AST::Expr> ae, const VarTable& vt)
    {

    	   CVC3::Expr result;
    	   assert(ae.get());
    	   AST::Expr e(*ae.get());

    	   std::vector < CVC3::Expr > children (e.children.size ());
    	   	for (unsigned i = 0; i < e.children.size (); ++i)
    	   	{
    	   	  children[i] = translateExpr(e.children[i],vt);
    	   	}

		   switch(e.kind) {
			case AST::Null:
				break;
			case AST::Var:
			{
				VarTable::const_iterator i(vt.find(e.getIdentifier()));
				if(i!=vt.end())
					result = i->second;
				else {
					MSG(0,"Table size %d\n",vt.size())
					for(VarTable::const_iterator i=vt.begin(); i!=vt.end();++i) {
						MSG(0,"Table contents: "+i->first+"->"+i->second.toString()+ "\n");
					}
					throw util::ParseError("translateExpr: unknown variable " + e.toString());
				}
			}
				break;
			case AST::Bool:
				result = e.getBool() ? vc.trueExpr() : vc.falseExpr();
				break;
			case AST::Int:
				result = vc.ratExpr(e.getInt(),1);
				break;
			case AST::Double: {
					std::string repr(util::floatToString(e.getDouble()));
					result = vc.ratExpr(repr,10);
				}
				break;

			case AST::Not:
				result = vc.notExpr(children[0]);
				break;
			case AST::And:
				result = vc.andExpr(children);
				break;
			case AST::Or:
				result = vc.orExpr(children);
				break;
			case AST::Eq:
				if(children[0].getType().isBool()) {
					result = vc.iffExpr(children[0],children[1]);
				} else {
					result = vc.eqExpr(children[0],children[1]);
				}
				break;
			case AST::Neq:
				if(children[0].getType().isBool()) {
					result = vc.notExpr(vc.iffExpr(children[0],children[1]));
				} else {
					result = vc.notExpr(vc.eqExpr(children[0],children[1]));
				}
				break;
			case AST::Lt:
				result = vc.ltExpr(children[0],children[1]);
				break;
			case AST::Gt:
				result = vc.gtExpr(children[0],children[1]);
				break;
			case AST::Le:
				result = vc.leExpr(children[0],children[1]);
				break;
			case AST::Ge:
				result = vc.geExpr(children[0],children[1]);
				break;

			case AST::Plus:
				result = vc.plusExpr(children[0],children[1]);
				break;
			case AST::Minus:
				result = vc.minusExpr(children[0],children[1]);
				break;
			case AST::Uminus:
				result = vc.uminusExpr(children[0]);
				break;
			case AST::Mult:
				result = vc.multExpr(children[0],children[1]);
				break;
			case AST::Div:
				result = vc.divideExpr(children[0],children[1]);
				break;
			case AST::Mod:

				break;

			case AST::Ite:
				result = vc.iteExpr(children[0],children[1],children[2]);
				break;
			case AST::Min:
				result = vc.iteExpr(vc.leExpr(children[0],children[1]),children[0],children[1]);
				break;
			case AST::Max:
				result = vc.iteExpr(vc.geExpr(children[0],children[1]),children[0],children[1]);
				break;

			case AST::Apply:

				break;
			default:
				break;
    	   }
		   if(result.isNull()) {
			   throw util::ParseError("translateExpr: could not translate " + e.toString());
		   }
		   return result;
    }

  /**
   * Transforms AST bound expression to Bound of concrete syntax tree.
   *
   * @param bound_expr abstract expression to be transformed
   * @param minimize true iff result should be for minimizing probabilities
   */
  Bound boundFromAST(const AST::Expr& bound_expr, bool minimize) {
    Bound::Kind k;
    double bound(0.0);
    switch(bound_expr.kind) {
    case AST::Gt:
      k = Bound::GR;  // >  bound ... greater
      assert(bound_expr.children[1]->isDouble());
      bound = bound_expr.children[1]->getDouble();
      break;
    case AST::Ge:
      k = Bound::GEQ; // >= bound ... greater or equal
      assert(bound_expr.children[1]->isDouble());
      bound = bound_expr.children[1]->getDouble();
      break;
    case AST::Lt:
      k = Bound::LE;  // <  bound ... strictly less
      assert(bound_expr.children[1]->isDouble());
      bound = bound_expr.children[1]->getDouble();
      break;
    case AST::Le:
      k = Bound::LEQ; // <= bound ... less or equal
      assert(bound_expr.children[1]->isDouble());
      bound = bound_expr.children[1]->getDouble();
      break;
    case AST::Eq:
      k = Bound::EQ; // =  bound ... equal
      assert(bound_expr.children[1]->isDouble());
      bound = bound_expr.children[1]->getDouble();
      break;
    default:
      k = Bound::DK;   // = ?      ... value to be computed
      break;
    }

    Bound b(k, bound, minimize);

    return b;
  }


    Property* translateProperty(boost::shared_ptr<AST::Expr> ae, const VarTable& vt) {
    	Property* result(0);
    	assert(ae.get());
    	AST::Expr e(*ae.get());

	   switch(e.kind) {
		case AST::Next:
			if(e.children.size()==1)
				result = new Next(translateProperty(e.children[0],vt));
			else if(e.children.size()==3) {
				double a(-1), b (-1);
				Time::Kind k;
				switch(e.children[1]->kind) {
					case AST::Null:
						break;
					case AST::Int:
						a = e.children[1]->getInt();
						break;
					case AST::Double:
						a = e.children[1]->getDouble();
						break;
					default:
						break;
				}

				switch(e.children[2]->kind) {
					case AST::Null:
						break;
					case AST::Int:
						b = e.children[2]->getInt();
						break;
					case AST::Double:
						b = e.children[2]->getDouble();
						break;
					default:
						break;
				}

				if(a!=-1.0 && b!=-1.0) {
					k = Time::INTERVAL;
				} else if(b != -1.0) {
					a = 0;
					k = Time::LE;
				} else if(a !=-1.0) {
					k = Time::GE;
					b = std::numeric_limits<double>::max();
				} else {
					throw util::ParseError("Bad time bound");
				}
				Time t(k,a,b);
				result = new Next(t,translateProperty(e.children[0],vt));

			}

			break;
		case AST::Until:
		{
			// TODO: support bounded until
			// Until(const Time&,Property*,Property*);
			// Until(Time::Kind,double,double,Property*,Property*);
			Property* p1(translateProperty(e.children[0],vt));
			Property* p2(translateProperty(e.children[1],vt));

			if(e.children.size()==2)
				result = new Until(p1,p2);
			else if(e.children.size()==4){
				double a(-1.0), b (-1.0);
				Time::Kind k;
				switch(e.children[2]->kind) {
					case AST::Null:
						break;
					case AST::Int:
						a = e.children[2]->getInt();
						break;
					case AST::Double:
						a = e.children[2]->getDouble();
						break;
					default:
						break;
				}

				switch(e.children[3]->kind) {
					case AST::Null:
						break;
					case AST::Int:
						b = e.children[3]->getInt();
						break;
					case AST::Double:
						b = e.children[3]->getDouble();
						break;
					default:
						break;
				}

				if(a!=-1.0 && b!=-1.0) {
					k = Time::INTERVAL;
				} else if(b != -1.0) {
					a = 0;
					k = Time::LE;
				} else if(a !=-1.0) {
					b = std::numeric_limits<double>::max();
					k = Time::GE;
				} else {
					throw util::ParseError("Bad time bound");
				}
				Time t(k,a,b);
				result = new Until(t,p1,p2);
			}
		}
			break;
		case AST::P:
		case AST::Pmin:
		case AST::Pmax:
        case AST::Steady:
        case AST::SteadyMax:
        case AST::SteadyMin: {
			const AST::Expr& bound_expr(*e.children[0].get());
            Bound b(boundFromAST(bound_expr, (e.kind == AST::Pmin) || (e.kind == AST::SteadyMin)));
            if ((AST::Steady == e.kind)
                || (AST::SteadyMax == e.kind)
                || (AST::SteadyMin == e.kind)) {
              result = new SteadyState(b, translateProperty(e.children[1],vt));;
            } else {
              result = new Quant(b, translateProperty(e.children[1],vt));;
            }
			break;
		}
			break;
		case AST::ReachabilityReward:
		{
			Property* p(translateProperty(e.children[0],vt));
			result = new ReachabilityReward(p);
		}
			break;
		case AST::CumulativeReward:
		{
			assert(e.children[0]->isDouble());
			double d(e.children[0]->getDouble());
			result = new CumulativeReward(d);
		}
			break;
		case AST::InstantaneousReward:
		{
			assert(e.children[0]->isDouble());
			double d(e.children[0]->getDouble());
			result = new InstantaneousReward(d);
		}
			break;
       case AST::SteadyStateReward:
       case AST::SteadyStateRewardMax:
       case AST::SteadyStateRewardMin:
         {
         const AST::Expr& bound_expr(*e.children[0].get());
         Bound b(boundFromAST(bound_expr, (e.kind == AST::SteadyStateRewardMin)));
         result = new SteadyStateReward(b);
         break;
         }
		case AST::PropNot:
			result = new PropNeg(translateProperty(e.children[0],vt));
			break;
		case AST::PropAnd:
			result = new PropBinary(PropBinary::AND,translateProperty(e.children[0],vt),
								translateProperty(e.children[1],vt));
			break;
		case AST::PropOr:
			result = new PropBinary(PropBinary::OR,translateProperty(e.children[0],vt),
   							       translateProperty(e.children[1],vt));
			break;

		default:
		{
			CVC3::Expr nested_expr ( translateExpr(ae,vt) );
			result = new PropExpr(nested_expr);
			break;
		}
	   }
       return result;
    }

    Alternative* translateAlternative(boost::shared_ptr<AST::Alternative> aa, const VarTable& vt)
    {
    	const AST::Alternative& alternative(*aa.get());
    	const AST::Update& update (alternative.update);
    	Alternative* result(new Alternative());

    	for(AST::Assignment::const_iterator i=update.assignment.begin();i!=update.assignment.end();++i)
    	{
    		CVC3::Expr lhs (translateExpr(i->first,vt));
    		CVC3::Expr rhs (translateExpr(i->second,vt));
    		result->Assign(lhs,rhs);
    	}
    	CVC3::Expr weight(translateExpr(alternative.weight,vt));
    	result->setWeight(weight);
		return result;
    }

    Command* translateCommand(boost::shared_ptr<AST::Command> ac, const VarTable& vt)
    {
    	std::string label;
    	boost::shared_ptr < AST::Expr > guard;
    	AST::Alternatives alternatives;

    	const AST::Command& command (*ac.get());
    	Command* result(new Command());



        for (AST::Alternatives::const_iterator i (command.alternatives.begin ());
    	 i != command.alternatives.end (); ++i)
          {
		try {
	    		result->addAlternative (translateAlternative(*i,vt));
	        } catch(util::ParseError& p) {
			throw util::ParseError("Alternative of command "+(*i)->toString() + "\n"
                                      + " Reason: " +  p.toString() + "\n");
		}
	}

	try {
	        result->setGuard(translateExpr(command.guard,vt));
        } catch(util::ParseError& p) {
		throw util::ParseError("Guard "+command.guard->toString() + "\n"
                                      + " Reason: " +  p.toString() + "\n");
	}

        result->setAction(command.label);
        return result;
    }

    Module* translateModule(boost::shared_ptr<AST::Module> am, const VarTable& vt)
    {
    	const AST::Module& module(*am.get());
    	Module* result(new Module(module.name));
        for (AST::Commands::const_iterator i (module.commands.begin ());
    	 i != module.commands.end (); ++i)
          {
    		result->addCommand(translateCommand(*i,vt)) ;
          }

    	return result;
    }

    void translateVariables(const AST::Variables& vars, Model& model, VarTable& vt) {
    	   for (AST::Variables::const_iterator i (vars.begin ());
    		 i != vars.end (); ++i)
    	      {
    	      	const AST::Variable& var(*i->second.get());


    	      	CVC3::Expr var_expr;

    	    	switch(var.type->kind) {
    	    	case AST::Type::Boolean:
    	    		{
    	    			var_expr = (vc.varExpr(i->first,vc.boolType()));
    	    			model.addVariable(i->first,var_expr);
    	    			model.setDefaultInitialValue (var_expr, var.init.get() ? translateExpr(var.init,vt) : vc.falseExpr());
    	    		}
    	    		break;
    	    	case AST::Type::Integer:
    	    		{
    	    			var_expr = (vc.varExpr(i->first,vc.intType()));
    	    			model.addVariable(i->first,var_expr);
    	    			model.setDefaultInitialValue (var_expr, var.init.get() ? translateExpr(var.init,vt) : vc.ratExpr(0,1));
    	    		}
    	    		break;
    	    	case AST::Type::Double: {
						var_expr = (vc.varExpr(i->first,vc.realType()));
						model.addVariable(i->first,var_expr);
						model.setDefaultInitialValue (var_expr, var.init.get() ? translateExpr(var.init,vt) : vc.ratExpr(0,1) );
					}
    	    		break;
    	    	case AST::Type::Bitvector: {
						var_expr = (vc.varExpr(i->first,vc.bitvecType(var.type->bitvector_data.width)));
						model.addVariable(i->first,var_expr);
					}
    	    		break;
    	    	case AST::Type::Range:
					{
						CVC3::Expr upper, lower;
						try {

							lower = translateExpr(var.type->range_data.lower,vt);
							upper = translateExpr(var.type->range_data.upper,vt);
						} catch ( util::ParseError& p) {
							throw util::ParseError("Range of variable "+ var.toString() + "\n"
                                      + " Reason: " +  p.toString() + "\n");
						}
						var_expr = (vc.varExpr(i->first,vc.intType()));
						model.addVariable(i->first,var_expr,lower,upper);

						model.setDefaultInitialValue (var_expr, var.init.get() ? translateExpr(var.init,vt) : lower);
					}
    	    		break;
    	    	}

    	      	vt.insert(std::pair<std::string,CVC3::Expr>(i->first, var_expr));
    	      	if(var.is_parameter)
    	      		model.parameter_variables.insert(var_expr);

    	      }

    }

    void translateModel(AST::Model& am, Model& model, VarTable& vt)
    {

    	switch(am.model_type) {
    	case AST::DTMC:
    		model.setModelType(DTMC);
    		break;
    	case AST::MDP:
    		model.setModelType(MDP);
    		break;
    	case AST::CTMC:
    		model.setModelType(CTMC);
    		break;
    	case AST::CTMDP:
    		model.setModelType(CTMDP);
    		break;
    	case AST::Unspecified:
    		model.setModelType(MDP);
    		break;
    	}

    	/* 1) Variable table
    	 *
    	 * build the variable table by traversing the model
    	 * collecting variables from each module */

    	/* global variables */
    	translateVariables(am.globals,model,vt);

    	/* local module variables */
        for (AST::Modules::const_iterator i (am.modules.begin ());
			 i != am.modules.end (); ++i)
        {
        	translateVariables(i->second->locals,model,vt);
        }

    	/* 2) translate modules and add them to the model */
    	for (AST::Modules::const_iterator i (am.modules.begin ()); i != am.modules.end (); ++i)
    	{
    	    model.addModule(translateModule(i->second,vt));
    	}

    	/* 3) translate the rest */

    	// boost::shared_ptr < Expr > initial
	try {
		if(am.initial.get()) {
    			CVC3::Expr e(translateExpr(am.initial,vt));
    			model.setInitial(e);
	    	}

        } catch(util::ParseError& p) {
		throw util::ParseError("Initial condition "+ am.initial->toString() + "\n"
                                      + " Reason: " +  p.toString() + "\n");
	}

    	// Exprs invariants
    	for(AST::Exprs::const_iterator i=am.invariants.begin();i!=am.invariants.end();++i) {
    		CVC3::Expr e(translateExpr(*i,vt));
    		model.addInvariant(e);
    	}

        // Actions actions;
    	for(AST::Actions::const_iterator i=am.actions.begin();i!=am.actions.end();++i) {
    		model.addAction(*i);
    	}

        // Exprs predicates;
    	for(AST::Exprs::const_iterator i=am.predicates.begin();i!=am.predicates.end();++i) {
    	   	CVC3::Expr e(translateExpr(*i,vt));
    	   	model.addPredicate(e);
    	}

    	// Exprs invariants
    	for(AST::Exprs::const_iterator i=am.properties.begin();i!=am.properties.end();++i) {
    		Property* p(translateProperty(*i,vt));
    	    model.addProperty(p);
    	}

        // StateRewards state_rewards;
    	for(AST::StateRewards::const_iterator i=am.state_rewards.begin();i!=am.state_rewards.end();++i) {
    		CVC3::Expr guard (translateExpr(i->first,vt));
    		CVC3::Expr reward(translateExpr(i->second,vt));
    		model.addStateReward(guard, reward);
    	}

        // TransitionRewards transition_rewards;
    	for(AST::TransitionRewards::const_iterator i=am.transition_rewards.begin();i!=am.transition_rewards.end();++i) {
    		Action a(i->first);
    		CVC3::Expr guard (translateExpr(i->second.first,vt));
    	    CVC3::Expr reward (translateExpr(i->second.second,vt));

    		if(a=="")
    			model.addTransReward(guard, reward);
    		else
    			model.addTransReward(a,guard, reward);
    	}
    }
}

