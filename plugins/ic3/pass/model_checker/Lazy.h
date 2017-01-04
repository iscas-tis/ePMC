/*
 * Lazy.h
 *
 *  Created on: Mar 19, 2009
 *      Author: bwachter
 */

#ifndef LAZY_H_
#define LAZY_H_

namespace model_checker {


struct Region {
	Region();

	std::vector<CVC3::Expr> p;
};

typedef std::unordered_map<State,Region> RegionMap;

class Lazy {
public:

	Lazy(lang::Model& __model, GameGraph& __graph, RegionMap& __rm) : model(__model), graph(__graph) , rm(__rm) {}

	lang::Model& model;
	GameGraph& graph;
	RegionMap& rm;


	CVC3::Expr getWP(Distribution d);
	void Refine(Pivot& pivot);

	void getPivot(Pivot& pivot, bool min);
	void run(bool min);
	bool isDone();
};
}

#endif /* LAZY_H_ */
