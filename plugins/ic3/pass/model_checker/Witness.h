/*
 * Witness.h
 *
 *  Created on: Oct 13, 2008
 *      Author: bwachter
 */

#ifndef WITNESS_H_
#define WITNESS_H_

namespace model_checker {

struct Witness {
	/* state to be refined */
	unsigned state;
	/* command inducing distribution 1 */
	unsigned lower_action;
	/* command inducing distribution 2 */
	unsigned upper_action;
	/* distribution 1 */
	std::vector<unsigned> lower_states;
	/* distribution 2 */
	std::vector<unsigned> upper_states;
};
}

#endif /* WITNESS_H_ */
