/*
 * AbsExpression.h
 *
 *  Created on: Sep 7, 2009
 *      Author: bwachter
 */

#ifndef ABSEXPRESSION_H_
#define ABSEXPRESSION_H_

namespace pred {

class AbsExpression {
public:
	static bdd::BDD abstractCover(const CVC3::Expr& e,
					  const EncodingManager& em,
				      const PredSet& preds,
                      const bdd::BDD& care_set,
                      const std::vector<CVC3::Expr>& invar);

	static bdd::BDD abstractCoverDecomp(const CVC3::Expr& e,
						 const EncodingManager& em,
					     const PredSet& preds,
					     const bdd::BDD& care_set,
					     const std::vector<CVC3::Expr>& invar);

};
}


#endif /* ABSEXPRESSION_H_ */
