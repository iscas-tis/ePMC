/*
 * CFGaiSee.cpp
 *
 *  Created on: Aug 22, 2009
 *      Author: bwachter
 */

#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/aiSee.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"

#include "TransitionConstraint.h"

#include "util/Cube.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"

#include "dp/SMT.h"

#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"

#include "EncodingManager.h"

#include "Decomposition.h"
#include "CFGCommand.h"
#include "CFG.h"


namespace pred {



std::string getString(vertex_descriptor v, unsigned block_nr) {
	return "v" + util::intToString(v)+ "b" + util::intToString(block_nr);
}

std::string getString(vertex_descriptor v, unsigned block_nr, unsigned distr_nr) {
	return "v" + util::intToString(v)+ "b" + util::intToString(block_nr) + "d"+ util::intToString(distr_nr);
}

std::string getIntervalString(double lo, double up) {
	std::string interval;
	if(lo == up)
		interval = util::floatToString(lo);
	else
		interval = "[" + util::floatToString(lo) + "," + util::floatToString(up) + "]";
	return interval;
}




void CFG::aiSee(std::ostream &stream) const {
	stream << "graph: {\n";


	foreach(Location loc, locs ) {

		const CFGNodeProp& prop(graph[loc]);
		const ValIterResult& vlo(prop.lb);
		const ValIterResult& vup(prop.ub);
		unsigned nr_of_blocks(vlo.val.size());

		std::string label;


		const CFGLocation& cfg_loc(getCFGLocation(loc));


		foreach(const CVC3::Expr& expr, cfg_loc.e ) {
			label += expr.toString()+"\n" ;
		}
		stream << "/* Location ";
		stream <<label<<" */\n";

		std::string color;

		double max_diff(0.0);
		for(unsigned i=0; i<vlo.val.size();++i) {
			double diff(vup.val[i] - vlo.val[i]);
			max_diff = max_diff < diff ? diff : max_diff;
		}

		std::string deviation;

		if(init.count(loc)>0)
			color = "lightgreen";
		else if(loc == goal)
			color = "lightred";
		else {
			if(max_diff>0)
				color = "yellow";
			else
				color = "white";
		}
		if(max_diff > 0)
			deviation = "diff.: " + util::floatToString(max_diff);

		stream << "graph: {title: \""<<loc<<" "<<deviation<<" " << prop.time << "\"\n"
			   << "status: clustered\n"
			   << "info1: \""<< label <<"\"\n"
			   << "info2: \""<< cfg_loc.b.preds.toString() <<"\"\n"
			   << "color: "<< color << "\n";

		for(unsigned block = 0; block<nr_of_blocks; ++block) {
			std::string block_str(getString(loc,block));
			std::string interval;

			double lo(vlo.val[block]);
			double up(vup.val[block]);
			interval = getIntervalString(lo,up);

			Block b;
			b.loc = loc;
			b.block_nr = block;

			CVC3::Expr characteristic(getExprWithinLocation(b));

			std::string c(characteristic.toString());



			util::aiSeeNode(stream, block_str, "", interval, c, 3, 3, "circle", "darkgrey",1);

			if(loc==goal) continue;


			for (out_edge_iter_pair it(out_edges(loc, graph)); it.first!= it.second; ++it.first) {
				Action action(target(*it.first, graph));
				const CFGNodeProp& action_prop(graph[action]);

				std::string color;

				if(vlo.str[block] == action && vlo.str[block] == vup.str[block]) {
					color = "green";
				} else if(vlo.str[block] == action) {
					color = "blue";
				} else if(vup.str[block] == action) {
					color = "red";
				} else
					color = "black";
				util::aiSeeEdge(stream, block_str,
							   getString(action,block),
								"" /* label */, 1 /* width */, color);

			}
		}



		// evaluate distributions, and actions
		for (out_edge_iter_pair itc(out_edges(loc, graph)); itc.first!= itc.second; ++itc.first) {
			vertex_descriptor action (target(*itc.first, graph));

			const CFGNodeProp& prop_action(graph[action]);
			const ValIterResult& valo(prop_action.lb);
			const ValIterResult& vaup(prop_action.ub);

			stream << "/* Action of location "<< loc <<" */\n";

			/***********
			 * actions *
			 ***********/
			for(unsigned block = 0; block<nr_of_blocks; ++block) {
				std::string block_str(getString(action,block));

				std::string interval;

				double lo(valo.val[block]);
				double up(vaup.val[block]);
				interval = getIntervalString(lo,up);



				util::aiSeeNode(stream, block_str  , "",
						interval,util::intToString(action), 2, 2, "circle", "darkgrey",2);

				for (out_edge_iter_pair itd(out_edges(action, graph)); itd.first!= itd.second; ++itd.first) {
					vertex_descriptor distr(target(*itd.first, graph));
					const CFGNodeProp& prop_distr(graph[distr]);

					std::string color;
					if(valo.str[block] == distr && valo.str[block] == vaup.str[block]) {
						color = "green";
					} else if (valo.str[block] == distr) {
						color = "blue";
					} else if (vaup.str[block] == distr) {
						color = "red";
					} else
						color = "black";

					util::aiSeeEdge(stream, block_str, getString(distr,block), "" /* label */, 1 /* width */, color);
				}
			}


			/******************
			 * distributions  *
			 ******************/
			for (out_edge_iter_pair itd(out_edges(action, graph)); itd.first!= itd.second; ++itd.first) {
				vertex_descriptor distr(target(*itd.first, graph));
				const CFGNodeProp& prop_distr(graph[distr]);

				const NondetMatrix& matrix(matrices[prop_distr.id]);
				const ValIterResult& vdlo(prop_distr.lb);
				const ValIterResult& vdup(prop_distr.ub);


				stream << "/* Distribution of location "<< loc <<" and action "<< action <<" */\n";

				assert(matrix.nr_of_branches==out_degree(distr,graph));
				unsigned stride (out_degree(distr,graph));

				for(unsigned block = 0; block<nr_of_blocks; ++block) {
					std::string block_str(getString(distr,block));

					std::string interval;

					double lo(vdlo.val[block]);
					double up(vdup.val[block]);
					interval = getIntervalString(lo,up);


					util::aiSeeNode(stream, block_str  , "",
							interval,util::intToString(distr), 2,
							3, "box", "darkgrey",3);


					unsigned l1(matrix.starts[block]),
							 h1(matrix.starts[block+1]);
					for(unsigned choice = l1; choice<h1; choice+=stride) {
						assert(choice < matrix.succ.size());

						std::string distr_str(getString(distr,block,choice));


						std::string color;

						if(choice == vdlo.str[block] && vdlo.str[block] == vdup.str[block]) {
							color = "green";
						} else if(choice == vdlo.str[block]) {
							color = "blue";
						} else if(choice == vdup.str[block])  {
							color = "red";
						} else {
							color = "black";
						}

						util::aiSeeEdge(stream, block_str,
								distr_str,
								"" /* label */, 1 /* width */,
								color);


						unsigned j=0;
						double dlo(0.0);
						double dup(0.0);

						for (out_edge_iter_pair it(out_edges(distr, graph)); it.first!= it.second; ++j, ++it.first) {
							vertex_descriptor succ(target(*it.first, graph));

							std::string succ_str(getString(succ,matrix.succ[choice + j]));

							const Valuation& succ_val_lo(graph[succ].lb.val);
							const Valuation& succ_val_up(graph[succ].ub.val);
							dlo += matrix.distr[j] * (succ_val_lo)[matrix.succ[choice + j]];
							dup += matrix.distr[j] * (succ_val_up)[matrix.succ[choice + j]];
							util::aiSeeEdge(stream, distr_str,succ_str, util::floatToString(matrix.distr[j]) /* label */, 1 ,"black");
						}
						util::aiSeeNode(stream, distr_str  , "",
								getIntervalString(dlo,dup), "", 3,
														3, "trapeze", "darkgrey",4);

					}
				}
			}
		}
		stream << "}\n";

	}

	stream << "\n}"<<std::endl;
}

}
