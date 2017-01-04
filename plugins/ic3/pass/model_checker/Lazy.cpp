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
#include "pred/Predicate.h"
#include "pred/PredSet.h"
#include "lang/Model.h"
#include "dp/SMT.h"

#include "model_checker/ActionGraph.h"
#include "model_checker/GameGraph.h"

#include "model_checker/Lazy.h"


#include <cmath>

namespace model_checker {


Region::Region() {

}

void Lazy::getPivot(Pivot& pivot, bool min) {
	/** select state with maximal relative deviation */
	MSG(0,"Lazy::getPivot\n");

	double max (-1);
	foreach(State state, graph.states) {
		if( out_degree(state, graph.graph) == 0 ) continue;

		double val_diff(graph.getProbability(state, false) - graph.getProbability(state, true));
		bool   sch_diff (graph.getChosenDistribution(state,false) != graph.getChosenDistribution(state,true));

		if( sch_diff && max < val_diff ) {
			max = val_diff;
			pivot.state = state;
		}
	}

	assert(max != -1);

	assert(graph.isState(pivot.state));
	assert(0 != out_degree(pivot.state, graph.graph));
	pivot.c = target(graph.getChoice(pivot.state, min), graph.graph);
	assert(graph.isChoiceSet(pivot.c));
	assert(0 != out_degree(pivot.c, graph.graph));
	pivot.d = target(graph.getChoice(pivot.c,false), graph.graph);
	assert(graph.isDistribution(pivot.d));

	graph.pivotState = pivot.state;
	graph.pivotDistr = pivot.d;

}

bool Lazy::isDone() {
	int precision;
	frexp(util::Database::term_crit_param,&precision);

	static double u(1.0), l(0.0);
	double cl = graph.getMinResult();
	double cu = graph.getMaxResult();
	if( cl < l || cu > u  ) {
		MSG(0,"diff %E %E \n",l-cl, u - cu);
	}

	l = cl;
	u = cu;
	return approxEquality(cl,cu,precision);
}

CVC3::Expr Lazy::getWP(Distribution d) {

	Action action(graph[d].p2Prop.action);
	const lang::Commands& commands (model.getCommands());
	assert(action < commands.size());
	const lang::Command& c(*commands[action]);
	const lang::Alternatives & alt(c.getAlternatives ());
	assert(alt.size()==out_degree(d,graph.graph));

	std::vector<State> states;
	graph.getSuccessors(d,states);

	std::vector<CVC3::Expr> conj(states.size());

	std::vector<CVC3::Expr> wp_succ;
	for(unsigned i=0; i<states.size();++i) {
		foreach(CVC3::Expr& e, rm[states[i]].p) {
			wp_succ.push_back(lang::vc.simplify((*alt[i])(e)));
		}
	}

	wp_succ.push_back(c.getGuard());

	CVC3::Expr result( lang::vc.simplify ( lang::ExprManager::Conjunction(wp_succ) ));
	MSG(0,"Lazy::getWP " +  result.toString()+" \n");

	if(result.isFalse()) {
		MSG(0,"Lazy::getWP: FALSE : " + c.toString() + "\n")

		foreach(CVC3::Expr& e, wp_succ) MSG(0,e.toString()+ "\n");

	}


	return result;
}

void Lazy::Refine(Pivot& pivot) {
	MSG(0,"Lazy::Refine\n");

	/* check if WP conforms with region */
	CVC3::Expr wp(getWP(pivot.d));
	CVC3::Expr region(lang::ExprManager::Conjunction(rm[pivot.state].p));

	if(lang::ExprManager::IsFalse(lang::vc.andExpr(region,wp))) {
		MSG(0,"Lazy::Refine: disjoint case\n");
		graph.removeDistribution(pivot.state,pivot.c,pivot.d);


	} else {
		MSG(0,"Lazy::Refine: non-disjoint case\n");

		if(wp.isFalse()) {
			assert(false);
		}
		/* perform split */
		State fresh = graph.doSplit(pivot);

		/* update region map */
		rm[fresh] = rm[pivot.state];
		rm[pivot.state].p.push_back(lang::vc.notExpr(wp));
		rm[fresh].p.push_back(wp);


		CVC3::Expr region1(lang::vc.andExpr(region,wp));
		CVC3::Expr region2(lang::vc.andExpr(region,lang::vc.notExpr(wp)));
		graph.setAnnotation(fresh,region1.toString());
		graph.setAnnotation(pivot.state,region2.toString());

		if(graph[pivot.state].stateProp.isInit()) {
			StateKind sk1 = lang::ExprManager::DisjointWith(region1,model.getInitial()) ? intermediate : init;
			StateKind sk2 = lang::ExprManager::DisjointWith(region2,model.getInitial()) ? intermediate : init;

			graph[fresh].stateProp.setKind(sk1);
			graph[pivot.state].stateProp.setKind(sk2);
		}
	}
	MSG(0,"Lazy::Refine: done\n");
}


void Lazy::run (bool min) {


	Pivot pivot;

	for(unsigned int i=0; ; ++i) {


		graph.until(min);

		if(isDone()) break;

		getPivot(pivot,min);

		if(i<10) {
			std::string filename("game_graph"+util::intToString(i)+".gdl");
			std::ofstream game_graph_file(filename.c_str());
			graph.printAiSee(game_graph_file);
		}
		Refine(pivot);
	}
	std::string filename("game_graph_final.gdl");
	std::ofstream game_graph_file(filename.c_str());
	graph.printAiSee(game_graph_file);

	MSG(0,"GameGraph::Lazy: done\n");
}

}
