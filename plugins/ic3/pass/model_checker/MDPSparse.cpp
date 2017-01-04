#include <limits>
#include "util/Util.h"
#include "util/Error.h"
#include "util/Database.h"
#include "util/Statistics.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"
#include "FoxGlynn.h"

#include "ActionGraph.h"
#include "MDPSparse.h"

using namespace bdd;

namespace model_checker {

MDPSparse::MDPSparse(int __max_iters, int __term_crit, double __term_crit_param)
	:
	  max_iters       (__max_iters)
	, term_crit       (__term_crit)
	, term_crit_param (__term_crit_param)
{
}

void MDPSparse::Until (
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	bool min,
	std::vector<double>& soln,
	std::vector<int> &chosen) const
{
	// misc
	int iters = 0;
	//soln = yes_vec;

	MSG(0,"Value iteration for %s probability ... ",min ? "minimal" : "maximal");
	for (bool done = false ;!done && iters < max_iters; ++iters) {
		done = true;
		//traverse states
		for (unsigned i = 0; i < n; ++i) {
			const int l1 = row_starts[i];
			const int h1 = row_starts[i+1];

			if( h1 <= l1 || soln[i] == 1.0) {
				continue;
			}

			double d1 = min ? 2.0 : -1.0;
			int opt_j = -1;
			// traverse distributions
			for (int j = l1; j < h1; ++j) {
				const int l2 = choice_starts[j];
				const int h2 = choice_starts[j+1];
				double d2 = 0.0;
				// traverse successors
				for (int k = l2; k < h2; ++k) d2 += non_zeros[k] * soln[cols[k]];
				// check for optimum
				if ( min ? d2 < d1 : d2 > d1 ) { d1 = d2; opt_j = j; }
			}

			// check convergence & set scheduler
			if ( !approxEquality(soln[i],d1,term_crit_param))
				{ done = false; chosen[i] = opt_j; }
			// update result
			soln[i] = d1;
		}


	}
	MSG(0,"iterations: %d\n",iters);
}

void MDPSparse::Until (
	const std::vector<unsigned>& order,
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	bool min,
	std::vector<double>& soln,
	std::vector<int> &chosen) const
{
	// misc
	int iters = 0;
	//soln = yes_vec;

	MSG(0,"Value iteration for %s probability ... ",min ? "minimal" : "maximal");
	for (bool done = false ;!done && iters < max_iters; ++iters) {
		done = true;
		//traverse states
		foreach(unsigned i, order) {
			const unsigned l1 = row_starts[i];
			const unsigned h1 = row_starts[i+1];

			if( h1 <= l1 ) {
				continue;
			}

			double d1 = min ? 2.0 : -1.0;
			int opt_j = -1;

			int old_choice (chosen[i]);
			double old_choice_val (soln[i]);

			// traverse distributions
			for (unsigned j = l1; j < h1; ++j) {
				const int l2 = choice_starts[j];
				const int h2 = choice_starts[j+1];
				double d2 = 0.0;
				// traverse successors
				for (int k = l2; k < h2; ++k) d2 += non_zeros[k] * soln[cols[k]];
				// check for optimum
				if ( min ? d2 < d1 : d2 > d1 ) { d1 = d2; opt_j = j; }

				if(j == old_choice)
					old_choice_val = d2;
			}

			// check convergence & set scheduler
			bool done2(approxEquality(soln[i],d1,term_crit_param));

			done = done && done2;

			// update result
			soln[i] = !done2 ? d1 : soln[i];


			if(!approxEquality(d1,old_choice_val,term_crit_param)) {
				chosen[i] = opt_j;
				done = false;
			}


		}


	}
	MSG(0,"iterations: %d\n",iters);
}

void MDPSparse::UntilCheck (
	const std::vector<unsigned>& order,
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	bool min,
	std::vector<double>& soln,
	std::vector<int> &chosen) const
{
	// misc
	int iters = 0;
	//soln = yes_vec;

	MSG(0,"Value iteration for %s probability ... ",min ? "minimal" : "maximal");
	for (bool done = false ;!done && iters < max_iters; ++iters) {
		done = true;
		//traverse states
		foreach(unsigned i, order) {
			const unsigned l1 = row_starts[i];
			const unsigned h1 = row_starts[i+1];

			if( h1 <= l1 ) {
				continue;
			}

			double d1 = min ? 2.0 : -1.0;
			int opt_j = -1;


			// traverse distributions
			int j = chosen[i];

			if(j == -1) continue;

			const int l2 = choice_starts[j];
			const int h2 = choice_starts[j+1];
			double d2 = 0.0;
			// traverse successors
			for (int k = l2; k < h2; ++k) d2 += non_zeros[k] * soln[cols[k]];
			// check for optimum
			if ( min ? d2 < d1 : d2 > d1 ) { d1 = d2; opt_j = j; }

			// check convergence & set scheduler
			if(!approxEquality(soln[i],d1,term_crit_param) ) {
				done = false;
				chosen[i] = opt_j;
			}

			// update result
			soln[i] = d1;
		}
	}

	MSG(0,"iterations: %d\n",iters);
	for(unsigned i=0; i<init_vec.size(); ++i) {
		if(init_vec[i] == 1.0) {
			MSG(0,"MDPSpare::UntilCheck %d %E\n",i,soln[i]);
		}
	}
}

void MDPSparse::UntilCheck (
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	bool min,
	std::vector<double>& soln,
	std::vector<int> &chosen) const
{
	// misc
	int iters = 0;
	//soln = yes_vec;

	MSG(0,"Value iteration for %s probability ... ",min ? "minimal" : "maximal");
	for (bool done = false ;!done && iters < max_iters; ++iters) {
		done = true;
		//traverse states
		for(unsigned i=0; i<n; ++i) {
			const unsigned l1 = row_starts[i];
			const unsigned h1 = row_starts[i+1];

			if( h1 <= l1 ) {
				continue;
			}

			double d1 = min ? 2.0 : -1.0;
			int opt_j = -1;


			// traverse distributions
			int j = chosen[i];

			if(j == -1) continue;

			const int l2 = choice_starts[j];
			const int h2 = choice_starts[j+1];
			double d2 = 0.0;
			// traverse successors
			for (int k = l2; k < h2; ++k) d2 += non_zeros[k] * soln[cols[k]];
			// check for optimum
			if ( min ? d2 < d1 : d2 > d1 ) { d1 = d2; opt_j = j; }

			// check convergence & set scheduler
			if(!approxEquality(soln[i],d1,term_crit_param) ) {
				done = false;
				chosen[i] = opt_j;
			}

			// update result
			soln[i] = d1;
		}
	}

	MSG(0,"iterations: %d\n",iters);
	for(unsigned i=0; i<init_vec.size(); ++i) {
		if(init_vec[i] == 1.0) {
			MSG(0,"MDPSpare::UntilCheck %d %E\n",i,soln[i]);
		}
	}
}



/**
	Game-based Transformer
	\pre Transitions with identical actions are stored consecutively
*/

void MDPSparse::Until (
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	bool min1,
	bool min2,
	std::vector<double>& soln,
	std::vector<int> &chosen) const
{
	MSG(0,"Value iteration for %s bound on %s probability ... ",(min1 ? "lower" : "upper"), (min2 ? "minimal" : "maximal"));
	// misc
	int iters = 0;
	//soln = yes_vec;


	for (bool done = false ;!done && iters < max_iters; ++iters) {
		done = true;
		//traverse states
		for (unsigned i = 0; i < n; ++i) {
			const int l1 = row_starts[i];
			const int h1 = row_starts[i+1];

			if( h1 <= l1 || soln[i] == 1.0) {
				continue;
			}

			double di = min2 ? 2.0 : -1.0;
			double da = min1 ? 2.0 : -1.0;

			int decision_a = -1;
			int decision_i = -1;

			double old_choice_val (0.0);
			int old_choice(chosen[i]);
			
			// traverse distributions
			for (int j = l1; j < h1; ++j) {
				assert(j<(int)choice_starts.size());
				const int l2 = choice_starts[j];
				const int h2 = choice_starts[j+1];
				double dj = 0.0;
				long current_action(actions[j]);

				// traverse successors
				for (int k = l2; k < h2; ++k) dj += non_zeros[k] * soln[cols[k]];
				// check for optimum

				if(j == old_choice ) old_choice_val = dj;

				if ( min1 ? dj < da : dj > da ) { da = dj; decision_a = j; }

				if ( j == h1 - 1  || actions[j+1]!= current_action) {
					if ( min2 ? da < di : da > di ) { di = da; decision_i = decision_a; }
					da = min1 ? 2.0 : -1.0;
				}
			}


			// check convergence & set scheduler

			bool done_strategy = approxEquality(old_choice_val,di,term_crit_param);
			if( !done_strategy )
				chosen[i] = decision_i;
			bool done_soln = approxEquality(soln[i],di,term_crit_param);

			if ( !done_strategy || !done_soln )
				{ done = false; }

			// update result
			soln[i] = di;
		}
	}
	MSG(0,"iterations: %d\n",iters);
}


void MDPSparse::Until (
	const std::vector<unsigned>& order,
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	bool min1,
	bool min2,
	std::vector<double>& soln,
	std::vector<int> &chosen) const
{
	MSG(0,"Value iteration for %s bound on %s probability ... ",(min1 ? "lower" : "upper"), (min2 ? "minimal" : "maximal"));
	// misc
	int iters = 0;
	//soln = yes_vec;


	for (bool done = false ;!done && iters < max_iters; ++iters) {
		done = true;
		//traverse states
		foreach(unsigned i, order) {
			const int l1 = row_starts[i];
			const int h1 = row_starts[i+1];

			double di = min2 ? 2.0 : -1.0;
			double da = min1 ? 2.0 : -1.0;

			int decision_a = -1;
			int decision_i = -1;

			int old_choice (chosen[i]);
			double old_choice_val (soln[i]);


			// traverse distributions
			for (int j = l1; j < h1; ++j) {
				const int l2 = choice_starts[j];
				const int h2 = choice_starts[j+1];
				double dj = 0.0;
				long current_action(actions[j]);

				// traverse successors
				for (int k = l2; k < h2; ++k) dj += non_zeros[k] * soln[cols[k]];
				// check for optimum

				
				if (j == old_choice ) old_choice_val = dj;

				if ( min1 ? dj < da : dj > da ) { da = dj; decision_a = j; }

				if ( j == h1 - 1 || actions[j+1]!= current_action ) {
					if ( min2 ? da < di : da > di ) { di = da; decision_i = decision_a; }
					da = min1 ? 2.0 : -1.0;
				}
			}

			/* update strategy if necessary */
			if(!approxEquality(di,old_choice_val,term_crit_param) ) { // Can the strategy be improved?
				chosen[i] = decision_i;
			}

			// check convergence
			done = done && approxEquality(soln[i],di,term_crit_param);

			// update result
			soln[i] = di;
		}
	}
	MSG(0,"iterations: %d\n",iters);
}


void MDPSparse::IntervalUntil (
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	bool min,
	std::vector<double>& lower_soln,
	std::vector<double>& upper_soln,
	std::vector<int>& lower_chosen,
	std::vector<int>& upper_chosen) {

	std::vector<std::vector<unsigned> > back_set;

	std::vector<unsigned > order;

	util::Timer precompTimer;

	if(util::Database::valiter_order == util::Database::rdfs) {
		precompTimer.Start();
		computePre(back_set);
		computeReverseDFSOrder(back_set,yes_vec,order);
		rdfs_order = order;
		precompTimer.Stop();
	}

	for(unsigned i=0;i<lower_chosen.size(); ++i) {
		unsigned l1(row_starts[i]);
		unsigned h1(row_starts[i+1]);
		lower_chosen[i] = l1 == h1 ? -1 : row_starts[i];
	}


	switch(util::Database::valiter_order) {
		case util::Database::rdfs:
			if(min) {
				Until(order,init_vec,yes_vec,true, true, lower_soln, lower_chosen);
				upper_chosen = lower_chosen;
				upper_soln   = lower_soln;
				Until(order,init_vec,yes_vec,false,true, upper_soln, upper_chosen);
			} else {
				Until(order,init_vec,yes_vec,true,false, lower_soln, lower_chosen);
				upper_chosen = lower_chosen;
				upper_soln   = lower_soln;
				Until(order,init_vec,yes_vec,false, upper_soln, upper_chosen);
			}
			break;
		case util::Database::random:
			MSG(0,"IntervalUntil random update order");				
			if(min) {
				Until(init_vec,yes_vec,true, true, lower_soln, lower_chosen);
				upper_chosen = lower_chosen;
				upper_soln   = lower_soln;
				Until(init_vec,yes_vec,false,true, upper_soln, upper_chosen);
			} else {
				Until(init_vec,yes_vec,true,false, lower_soln, lower_chosen);

				upper_chosen = lower_chosen;
				upper_soln   = lower_soln;
				Until(init_vec,yes_vec,false, false, upper_soln, upper_chosen);
			}

			/*	
			Until(init_vec,yes_vec,!min,min,min ? upper_soln : lower_soln,min ? upper_chosen : lower_chosen);
			Until(init_vec,yes_vec,min,min ? lower_soln : upper_soln, min ? lower_chosen : upper_chosen);
			*/				
			break;
		case util::Database::topo:
			assert(false); // not implemented
			break;
	}	
}



void MDPSparse::computePre(
		std::vector<std::vector<unsigned> >& back_set) const {
	back_set.clear();
	back_set.resize(n);
	for (unsigned state = 0; state < n; ++state) {
		assert(state<row_starts.size());
		unsigned l1 = row_starts[state];
		unsigned h1 = row_starts[state+1];
		for (unsigned j = l1; j < h1; ++j) {
			assert(j<choice_starts.size());
			unsigned l2 = choice_starts[j];
			unsigned h2 = choice_starts[j+1];
			for (unsigned k = l2; k < h2; ++k) {
				assert(k<cols.size());
				assert(cols[k]<back_set.size());
				back_set[cols[k]].push_back(state);
			}
		}
	}
}

void MDPSparse::computeReverseDFSOrder
				   (const std::vector<std::vector<unsigned> >& back_set,
					const std::vector<double>& yes_vec,
					std::vector<unsigned>& order) {

	std::set<int> frontier;

	order.clear();
	order.reserve(n);

	/* discovery time of a state */
	std::vector<int> time(n,-1);

	for(unsigned i=0; i<yes_vec.size(); ++i) {
		if(yes_vec[i] == 1.0)
			frontier.insert(i);
	}

	int discovery_counter (0);

	while(!frontier.empty()) {
		int current(*frontier.begin());

		frontier.erase(frontier.begin());

		if(time[current] != -1) continue;

		// newly discovered
		time[current] = discovery_counter++;
		if(!yes_vec[current] == 1.0)
			order.push_back(current);

		const std::vector<unsigned>& preds(back_set[current]);

		foreach(unsigned pred, preds) {
			frontier.insert(pred);
		}
	}
	MSG(0,"MDPSparse::computeReverseDFSOrder: backwards reachable vertices %d\n",order.size());
}




void MDPSparse::calculate_P_s_alpha_B (const std::vector<double> &yes_vec,
                                       std::vector<double> &P_s_alpha_B,
                                       const std::vector<double> &unif_non_zeros) const {

  P_s_alpha_B.clear();

  /* allocate and fill P(s,alpha,B) */
  for (unsigned state_nr = 0; state_nr < n; state_nr++) {
    if (1.0 != yes_vec[state_nr]) {
      unsigned state_start = row_starts[state_nr];
      unsigned state_end = row_starts[state_nr + 1];
      unsigned choice_nr;
      for (choice_nr = state_start; choice_nr < state_end; choice_nr++) {
	unsigned i_start = choice_starts[choice_nr];
	unsigned i_end = choice_starts[choice_nr + 1];
	double value = 0.0;
	for (unsigned i = i_start; i < i_end; i++) {
	  if (1.0 == yes_vec[cols[i]]) {
	    value += unif_non_zeros[i];
	  }
	}
	P_s_alpha_B.push_back(value);
      }
    }
  }
}

double MDPSparse::CTBoundedUntil (
	const std::vector<double>& init_vec,
	std::vector<double>& yes_vec,
	bool min,
	std::vector<double>& soln,
	std::vector<int> &chosen,
	double time
	)
{
	// misc
	soln.clear();
	soln.resize(yes_vec.size(), 0.0);
	std::vector<double> soln2 = soln;

	std::vector<double> unif_non_zeros = non_zeros;

	// find rate
	double rate = 0.0;
	for (unsigned i = 0; i < n; ++i) {
	  const int l1 = row_starts[i];
	  const int h1 = row_starts[i+1];

	  // traverse distributions
	  for (int j = l1; j < h1; ++j) {
	    assert(j<(int)choice_starts.size());
	    const int l2 = choice_starts[j];
	    const int h2 = choice_starts[j+1];
	    // traverse successors
	    double val = 0.0;
	    for (int k = l2; k < h2; ++k) val += non_zeros[k];
	    if (val > rate) {
	      rate = val;
	    }
	  }
	}

	// get Poisson distribution
	FoxGlynn fg(time * rate, 1.0E-6);

	// divide by rate
	for (unsigned i = 0; i < n; ++i) {
	  const int l1 = row_starts[i];
	  const int h1 = row_starts[i+1];

	  // traverse distributions
	  for (int j = l1; j < h1; ++j) {
	    assert(j<(int)choice_starts.size());
	    const int l2 = choice_starts[j];
	    const int h2 = choice_starts[j+1];
	    // traverse successors
	    for (int k = l2; k < h2; ++k) unif_non_zeros[k] /= rate;
	  }
	}

	// get P(s, alpha, B)
	std::vector<double> P_s_alpha_B;
	calculate_P_s_alpha_B(yes_vec, P_s_alpha_B, unif_non_zeros);

	// do iterations
	unsigned left = fg.getLeft();
	unsigned right = fg.getRight();
	const std::vector<double> &weights = fg.getWeights();
	double total_weight = fg.getTotalWeight();
	MSG(0,"\nStarting iterations... \n");
	for (unsigned iters = right; iters > 0; iters--) {
		double psi = 0.0;
		unsigned ps_index = 0;
		if ((unsigned) iters >= left) {
		  psi = weights[iters - left] / total_weight;
		}

		//traverse states
		for (unsigned i = 0; i < n; ++i) {
		  if (yes_vec[i]) {
		    soln2[i] = soln[i] + psi;
		  } else {
			const int l1 = row_starts[i];
			const int h1 = row_starts[i+1];

			if( h1 <= l1) {
				soln[i] = yes_vec[i];
				continue;
			}

			double d1 = min ? 2.0 : -1.0;

			int opt_j = -1;
			// traverse distributions
			for (int j = l1; j < h1; ++j) {
				assert(j<(int)choice_starts.size());
				const int l2 = choice_starts[j];
				const int h2 = choice_starts[j+1];
				double d2 = 0.0;
				// traverse successors
				double rate_used = 0.0;
				for (int k = l2; k < h2; ++k) {
				  d2 += unif_non_zeros[k] * soln[cols[k]];
				  rate_used += unif_non_zeros[k];
				}
				// self-loop with remaining rate part
				// (should be overapprox for CTMDPs!!)
				d2 += (1 - rate_used) * soln[i];

				if (iters >= left) {
				  d2 += psi * P_s_alpha_B[ps_index];
				}

				// check for optimum
				if ( min ? d2 < d1 : d2 > d1 ) { d1 = d2; opt_j = j; }
				ps_index++;
			}
			//			cout << "=> " << opt_j << endl;
			if ( !approxEquality(soln[i],d1,term_crit_param))
				{ chosen[i] = opt_j; }
			//			cout << chosen[i] << " ";
			// update result
			soln2[i] = d1;
		  }
		}
		//cout << endl;
		soln.swap(soln2);
		//		cout << "===" << endl;
	}


	double optimum = min ? 2.0 : -1.0;
	for (unsigned i = 0; i < n; ++i) {
		if(init_vec[i] == 1.0) {
			double val (soln[i]);
			if( min ? val < optimum : optimum < val )
				optimum = val;
		}
		if (1.0 == yes_vec[i]) {
		  soln[i] = 1.0;
		}
	}

	MSG(0,"RESULT");

//	for (unsigned i = 0; i < n; ++i) {
//	  cout << "> " << yes_vec[i] << " " << init_vec[i] << endl;
//	  cout << "==> " << chosen[i] << endl;
//	}

	// compute embedded DTMC
	for (unsigned i = 0; i < n; ++i) {
	  const int l1 = row_starts[i];
	  const int h1 = row_starts[i+1];

	  // traverse distributions
	  for (int j = l1; j < h1; ++j) {
	    assert(j<(int)choice_starts.size());
	    const int l2 = choice_starts[j];
	    const int h2 = choice_starts[j+1];
	    double rate = 0.0;
	    for (int k = l2; k < h2; ++k) rate += non_zeros[k];
	    for (int k = l2; k < h2; ++k) non_zeros[k] /= rate;
	  }
	}

	std::vector<double> dummy_soln(soln.size());
	Until(init_vec, yes_vec, min, dummy_soln, chosen);

	return optimum;
}



/* PRISM uses case 2: case 1 is fast but imprecise
	if (fabs(soln2 - soln[i]) > term_crit_param)
		{ done = false; chosen[i] = opt_j; }
	break;
	case 2:
	if (fabs(soln2 - soln[i])/soln2 > term_crit_param)
		{ done = false; chosen[i] = opt_j; }
*/

bool MDPSparse::sanityCheck(const bdd::ODD& odd) const {
	std::vector<double> vec(odd.getNumOfStates(),0.0);

	unsigned l1(0), h1(0), l2(0), h2(0), i(0), j(0);
	double d2;
	for (i = 0; i < n; ++i) {
		if (!use_counts) { l1 = row_starts[i]; h1 = row_starts[i+1]; }
		else { l1 = h1; h1 += row_counts[i]; }

		// make sure that identical actions are consecutive
		std::set<long> action_set;
		long prev_action(-1);

		// traverse distributions
		for (j = l1; j < h1; ++j) {
			if (!use_counts) { l2 = choice_starts[j]; h2 = choice_starts[j+1]; }
			else { l2 = h2; h2 += choice_counts[j]; }

			long action(actions[j]);

			if(prev_action != action) {
				assert(action_set.count(action)==0);
				action_set.insert(action);
			}

			prev_action = action;

			d2 = 0.0;
			// traverse successors
			for (unsigned k = l2; k < h2; ++k) d2 += non_zeros[k];
			if(d2 > 1.0) {
				MSG(0,"MDPSparse::sanityCheck(): State %d Too high probability %E in command %d\n",i,d2, actions[j]);

				MSG(0,"Superstochastic: ");
				for (unsigned k = l2; k < h2; ++k) MSG(0,"%E ",non_zeros[k]);
				MSG(0,"\n");


				vec[i] = 1.0;
				bdd::MTBDD state_mtbdd = odd.toMTBDD(vec);
				state_mtbdd.PrintMinterm();

				return false;
			}
		}
	}
	return true;
}






struct BFSData {
	int state;
	LabeledTransition lt;
	double prob;
	int level;
	BFSData(int s) : state(s), prob(0.0), level(0) {}
	BFSData(int s, double p, int l) : state(s), prob(p), level(l) {}
};

/*!
	\param smart do not add states that don't reach bad state
*/
void MDPSparse::computeGraph (
	bool min,
	const std::vector<double>& init_vec,
	const std::vector<double>& yes_vec,
	const std::vector<double>& lower_soln,
	const std::vector<double>& upper_soln,
	const std::vector<int>& lower_chosen,
	const std::vector<int>& upper_chosen,
	ActionGraph& graph,
	bool smart)
{

	const std::vector<int>& chosen ( min ? lower_chosen : upper_chosen);
	double highest_diff = -1.0;

	int opt_ini = -1;
	// find optimal initial state
	for (unsigned i = 0; i < n; ++i) {
		if(init_vec[i]==0) continue;
		double diff ( upper_soln[i] - lower_soln[i]);
		if(diff > highest_diff ) {
			highest_diff = diff;
			opt_ini = i;
		}
	}

	bool success = false;

	include.clear();
	include.resize(n,false);


	// reserve
	// 0 : bad
	// 1 : initial
	// 2 ... m : other states


	std::vector<BFSData> st_stack;


	std::vector<int> back_map(n,-1);

	include[opt_ini] = true;
	back_map[opt_ini] = 1;

	int bad_new = 0;

	st_stack.push_back(-1);

	int level = 0;

	BFSData ini(opt_ini,highest_diff,level);
	st_stack.push_back(ini);

	int s1(1), s2(st_stack.size());


	std::vector<int> frontier;

	switch(util::Database::strategy) {
		case util::Database::optDiff:
		case util::Database::optDiffPath:
		case util::Database::strongestEvidence:
		 {
				// reachability analysis
			while(s1<s2) {


				for(int i=s1; i<s2; ++i) {
					int from = st_stack[i].state;

					double diff_from(upper_soln[from]-lower_soln[from]);
					int j = chosen[from];
					if(j == -1) {
						if( yes_vec[i]==1.0 ) {

							success = true;
							back_map[from] = bad_new;
							st_stack[0] = from;
						}
					} else if( j >= row_starts[from] && j < row_starts[from+1]) {
						long l2 = choice_starts[j];
						long h2 = choice_starts[j+1];

						for (int k = l2; k < h2; ++k) {
							int to = cols[k];
							double diff(upper_soln[to]-lower_soln[to]);
							if(diff == 0) {
								frontier.push_back(from);
							}

							if(!include[to]) {
								if( util::Database::strongestEvidence != util::Database::strategy ?
									(chosen[to] == -1 || diff == 0.0) : yes_vec[to]==1.0) {
									success = true;
									back_map[to] = bad_new;
									if(st_stack[0].state  == -1) {
										 // remember first bad state and transition to it
										st_stack[0].state = to;
									}

								} else  {

									if(util::Database::strongestEvidence == util::Database::strategy
									  && chosen[to] == -1) // do not add no-states
										continue;
									BFSData new_entry(to,diff_from,level);
									st_stack.push_back(new_entry);
									include[to]=true;
									back_map[to] = st_stack.size()-1;
								}
								/*
								 *  for each explored state,
								 *  store the tree edge that lead to the state
								 */
								assert(actions[j]!=-1);
								LabeledTransition lt(back_map[from], actions[j] , back_map[to]);
								lt.setState(from);
								lt.Add(non_zeros[k],updates[k]);
								lt.setDiff(diff);
								st_stack[back_map[to]].lt = lt;
							}
							else {

								BFSData& existing_entry(st_stack[back_map[to]]);
								if(back_map[to]!=opt_ini) {						
									LabeledTransition& lt(existing_entry.lt);
									if((int)lt.getFrom()==back_map[from])
										lt.Add(non_zeros[k],updates[k]);
								}
								
							}
						}
					}
					++level;
				}
				s1 = s2;
				s2 = st_stack.size();
			}


		}
		break;
		case util::Database::anyDiff:
		case util::Database::onlyScheduler: {
			// reachability analysis
			while(s1<s2) {

				for(int i=s1; i<s2; ++i) {
					int from = st_stack[i].state;

					double diff_from(upper_soln[from]-lower_soln[from]);

					int upper_action(actions[upper_chosen[from]]);
					int lower_action(actions[upper_chosen[from]]);

					bool skip_actions = upper_action == lower_action;
					int action(upper_action);

					int l1(row_starts[from]);
					int h1(row_starts[from+1]);
					/*
					* do reachability for both schedulers
					*
					*/
					for( int j = l1; j<h1 ; ++j) {
						if(j == -1) {
							success = true;
							back_map[from] = bad_new;
							st_stack[0] = from;
						} else if( j >= 0 && j < (int)nc) {

							if(skip_actions) {
								if(actions[j] != action) {
									continue;
								}
							}

							long l2 = choice_starts[j];
							long h2 = choice_starts[j+1];
							for (int k = l2; k < h2; ++k) {
								int to = cols[k];
								double diff(upper_soln[to]-lower_soln[to]);
								if(!include[to]) {
									if( diff == 0.0) {
										success = true;
										back_map[to] = bad_new;
										if(st_stack[0].state  == -1) // remember first bad state
											st_stack[0] = to;
									} else  {
										BFSData new_entry(to,diff_from,level);
										st_stack.push_back(new_entry);
										back_map[to] = st_stack.size()-1;

										/*
										*  for each explored state,
										*  store the tree edge that lead to the state
										*/
										LabeledTransition lt(back_map[from], actions[j] , back_map[to]);
										lt.setState(from);
										lt.Add(non_zeros[k],updates[k]);
										lt.setDiff(diff);
										st_stack[back_map[to]].lt = lt;
									}


									include[to] = true;
								}
								else {

									BFSData& existing_entry(st_stack[back_map[to]]);
									LabeledTransition& lt(existing_entry.lt);


									if((int)lt.getFrom()==back_map[from]) {
										lt.Add(non_zeros[k],updates[k]);

									}
								}

							}
						} else {

							MSG(0,"What the heck ? \n");
							exit(10);
						}
					}
					++level;
				}
				s1 = s2;
				s2 = st_stack.size();
			}
		}
		break;
	}


	graph.resize(st_stack.size());
	graph.setInit(1);
	graph.setBad(bad_new);

	MSG(0,"MDPSparse: size of induced MC %d\n",st_stack.size());

	if(util::Database::strategy == util::Database::strongestEvidence) {

		assert(success);

		for(unsigned i = 1; i<st_stack.size(); ++i) {
			unsigned state(st_stack[i].state);
			StateLabel label(upper_soln[state]-lower_soln[state]);
			graph.addLabel(state,label);


			int l1(row_starts[state]);
			int h1(row_starts[state+1]);

			int choice(chosen[state]);
			if(choice == -1 || l1 == h1) continue;
			
			assert(l1 <= choice);
			assert(choice < h1);

			// neighboring choices either belong to different state or different action
			bool precise ( ( choice - 1 < l1 || actions[choice - 1] != actions[choice] ) 
				    && ( choice + 1 >= h1 || actions[choice + 1] != actions[choice] ) );


			long l2 = choice_starts[choice];
			long h2 = choice_starts[choice+1];
			for(unsigned k = l2 ; k < h2  ; ++k) {
				assert(k<cols.size());
				unsigned to(cols[k]);
				assert(to < include.size());
				if(!include[to] && back_map[to] != bad_new) continue;
				assert(choice < actions.size());
				LabeledTransition lt(back_map[state], actions[choice] , back_map[to]);
				lt.setState(state);
				lt.Add(non_zeros[k],updates[k]);
				lt.setPrecise( precise );
				graph.addTransition(lt);
			}
		}
	} else {


	 	int pivot(-1);
		double max_deviation(0.0);
		// identify a pivot state

		int level = -1;

		if(rdfs_order.size()>0) {
			foreach(unsigned i, rdfs_order) {
				if(!include[i]) continue;

				BFSData& entry(st_stack[back_map[i]]);



				unsigned state(i);

				double val(  (upper_soln[state] - lower_soln[state]));

				if( entry.level > level && val > max_deviation) {
					std::vector<Witness> wit;
					computeWitnesses(
									state,
									lower_chosen[state],
									upper_chosen[state],
									lower_soln,
									upper_soln,
									wit);
					if(wit.size()>0 ) {
						level = entry.level;
						pivot = state;
						max_deviation = val;
					}
				}
			}
		} else
		for(unsigned i = 0; i<st_stack.size(); ++i) {
			unsigned state(st_stack[i].state);
			double val(  (upper_soln[state] - lower_soln[state]));
			std::vector<Witness> wit;
			computeWitnesses(
							state,
							lower_chosen[state],
							upper_chosen[state],
							lower_soln,
							upper_soln,
							wit);
			if( val > max_deviation && wit.size()>0 ) {
				pivot = state;
				max_deviation = val;
			}
		}

		pivot_state = pivot;

		if(pivot==-1) {
			MSG(0,"MDPSparse::computeGraph: No pivot block found\n");
			return;
		} else {

			MSG(0,"MDPSparse::computeGraph: pivot block %d %E\n",pivot_state,max_deviation);

			LabeledTransition lt(back_map[pivot],-1,bad_new);
			lt.Add(1.0,0);
			lt.setState(pivot);
			lt.setDiff(max_deviation);
			lt.setPrecise(true);

			computeWitnesses(
						pivot,
						lower_chosen[pivot],
						upper_chosen[pivot],
						lower_soln,
						upper_soln,
						lt.w);

			graph.addTransition(lt);

			// compute the tree edges by traversing from pivot backward to the initial state
			int from = pivot;
			while(from != opt_ini) {
				assert(back_map[from]!=-1);
				LabeledTransition& lt(st_stack[back_map[from]].lt);

				int state = from;
				int choice = chosen[state];			
				int l1(row_starts[state]);
				int h1(row_starts[state+1]);
				bool precise ( ( choice - 1 < l1 || actions[choice - 1] != actions[choice] ) 
						  && ( choice + 1 >= h1 || actions[choice + 1] != actions[choice] ) );

				lt.setPrecise(precise);
				lt.setState(from);
				assert((int)lt.getTo() == back_map[from] );
				graph.addTransition(lt);

				from = st_stack[lt.getFrom()].state;
			}
		}
	}
	graph.ComputeBackSets();

}



void MDPSparse::traverse_mtbdd_matr_rec (
	DdManager *ddman,
	DdNode *dd,
	const std::vector<bdd::MTBDD>& rvars,
	const std::vector<bdd::MTBDD>& cvars,
	int num_vars,
	int level,
	bdd::OddNode *row,
	bdd::OddNode *col,
	int r,
	int c,
	int code,
	bool transpose,
	long action,
	int  branch )
{
	// base case - zero terminal
	if (dd == Cudd_ReadZero(ddman)) return;

	// base case - non zero terminal
	if (level == num_vars) {
		switch (code) {

		// row major - first pass
		case 1:
			assert(false);
			row_starts[(transpose?c:r)+1]++;
			break;

		// column major - first pass
		case 3:
			assert(false);
			row_starts[(transpose?r:c)+1]++;
			break;

		// compact modified sparse row - first pass
		case 6:
			assert(false);
			++row_starts[(transpose?c:r)+1];
			break;

		// compact modified sparse column - first pass
		case 8:
			assert(false);
			++row_starts[(transpose?r:c)+1];
			break;

		// mdp - first pass
		case 10:
		        actions[row_starts[(transpose?c:r)]] = action;
			++choice_starts[row_starts[(transpose?c:r)]+1];
			break;

		// mdp - second pass
		case 11:{
			static long choice;
			static long index;
			choice = row_starts[(transpose?c:r)];
			index = choice_starts[choice];
			non_zeros[index] = Cudd_V(dd);
			cols[index] = (transpose?r:c);
			updates[index] = branch;
			++choice_starts[choice];
			break;
			}
		case 12:
			++nnz;		
			break;

		default:
			assert(false);
			break;
		}
		return;
	}

	DdNode *e, *t, *ee, *et, *te, *tt;

	// recurse
	if (dd->index > cvars[level].getIndex()) {
		ee = et = te = tt = dd;
	}
	else if (dd->index > rvars[level].getIndex()) {
		ee = te = Cudd_E(dd);
		et = tt = Cudd_T(dd);
	}
	else {
		e = Cudd_E(dd);
		if (e->index > cvars[level].getIndex()) {
			ee = et = e;
		}
		else {
			ee = Cudd_E(e);
			et = Cudd_T(e);
		}
		t = Cudd_T(dd);
		if (t->index > cvars[level].getIndex()) {
			te = tt = t;
		}
		else {
			te = Cudd_E(t);
			tt = Cudd_T(t);
		}
	}

	traverse_mtbdd_matr_rec(ddman, ee, rvars, cvars, num_vars, level+1,
				row->e, col->e, r, c, code,
				transpose, action, branch);
	traverse_mtbdd_matr_rec(ddman, et, rvars, cvars, num_vars, level+1,
				row->e, col->t, r, c+col->eoff, code,
				transpose, action, branch);
	traverse_mtbdd_matr_rec(ddman, te, rvars, cvars, num_vars, level+1,
				row->t, col->e, r+row->eoff, c, code,
				transpose, action, branch);
	traverse_mtbdd_matr_rec(ddman, tt, rvars, cvars, num_vars, level+1,
				row->t, col->t, r+row->eoff, c+col->eoff, code,
				transpose, action, branch);
}



void MDPSparse::traverse_mtbdd_vect_rec (
	DdManager *ddman,
	DdNode *dd,
	const std::vector<bdd::MTBDD>& vars,
	int num_vars,
	int level,
	bdd::OddNode *odd,
	int i,
	int code)
{
	// base case - zero terminal
	if (dd == Cudd_ReadZero(ddman)) return;

	// base case - non zero terminal
	if (level == num_vars) {
		switch (code) {

		// mdp - first pass
		case 1:
			++row_starts[i+1];
			break;

		// mdp - second pass
		case 2:
			++row_starts[i];
			break;
		}
		return;
	}

	DdNode *e, *t;

	// recurse
	if (dd->index > vars[level].getIndex()) {
		e = t = dd;
	}
	else {
		e = Cudd_E(dd);
		t = Cudd_T(dd);
	}
	traverse_mtbdd_vect_rec(ddman, e, vars, num_vars, level+1, odd->e, i, code);
	traverse_mtbdd_vect_rec(ddman, t, vars, num_vars, level+1, odd->t, i+odd->eoff, code);
}

/*
 * Enumerate the used choices explicitly
 */
void MDPSparse::split_mdp_rec(
	DdManager* ddman,
	DdNode *dd,
	const std::vector<bdd::MTBDD>& ndvars,
	int num_ndvars,
	std::vector<bdd::MTBDD>& matrices,
	int& count,
	int level
	)
{
	// base case - empty matrix
	if (dd == Cudd_ReadZero(ddman)) return;

	// base case - nonempty matrix
	if (level == num_ndvars) {
		matrices[count++] = MTBDD(ddman,dd);
		return;
	}

	DdNode *e, *t;

	// recurse
	if (dd->index > ndvars[level].getIndex()) {
		e = t = dd;
	}
	else {
		e = Cudd_E(dd);
		t = Cudd_T(dd);
	}

	split_mdp_rec(ddman, e, ndvars, num_ndvars, matrices, count, level+1);
	split_mdp_rec(ddman, t, ndvars, num_ndvars, matrices, count, level+1);
}

void MDPSparse::compute_nondet_actions (
	DdManager *ddman,
	DdNode *dd,
	const std::vector<bdd::MTBDD>& ndvars,
	int num_ndvars,
	std::vector<long>& choices,
	int& count,
	int highest_level,  // explore up to that level
	long choice,
	long interleaving_choice,
	int level)
{
	// base case - empty matrix
	if (dd == Cudd_ReadZero(ddman)) return;


	if(level == highest_level) {
		interleaving_choice = choice;
	}

	// base case - nonempty matrix
	if (level == num_ndvars) {
		choices[count] = interleaving_choice;
		++count;
		return;
	}

	DdNode *e, *t;

	// recurse
	if (dd->index > ndvars[level].getIndex()) {
		e = t = dd;
	} else {
		e = Cudd_E(dd);
		t = Cudd_T(dd);
	}

	long e_choice = choice;
	long t_choice = choice | (1 << (highest_level - level - 1));

	compute_nondet_actions(ddman, e, ndvars, num_ndvars, choices, count, highest_level, e_choice, interleaving_choice, level + 1);
	compute_nondet_actions(ddman, t, ndvars, num_ndvars, choices, count, highest_level, t_choice, interleaving_choice, level + 1);
}

MDPSparse::MDPSparse (
	DDManager& dd_mgr,
	const bdd::MTBDD& matrix,
	const std::vector<bdd::MTBDD>& rvars,
	const std::vector<bdd::MTBDD>& cvars,
	const std::vector<bdd::MTBDD>& ndvars,
	const std::vector<bdd::MTBDD>& pvars,
	int num_interleaving_choice_vars,
	const bdd::ODD& odd,
	int __max_iters, int __term_crit, double __term_crit_param) :
	  max_iters       (__max_iters)
	, term_crit       (__term_crit)
	, term_crit_param (__term_crit_param)
{

	util::Timer construction_t;
	construction_t.Start();

	//Cudd_DebugCheck(dd_mgr.getDdManager());

	DdManager* ddman(matrix.getDdManager());
	int choice = 0;
	unsigned num_vars = rvars.size();
	unsigned num_ndvars = ndvars.size();

	unsigned max, max2;
	// create new data structure

	n = odd.getNumOfStates();
	// get num of choices (prob. distributions)
	BDD tmp ((matrix.GreaterThan(0.0)).Exist(pvars).Exist(cvars));

	nc = (int)tmp.CountMinterm(rvars.size() + ndvars.size());

	//Cudd_DebugCheck(dd_mgr.getDdManager());


	// break the mdp mtbdd into several (nm) mtbdds
	tmp = tmp.Exist(rvars);
	nm = (int)tmp.CountMinterm(ndvars.size());


	std::vector<bdd::MTBDD> matrices (nm);

	int count = 0;

	split_mdp_rec(
		ddman,
		matrix.getDdNode(),
		ndvars,
		num_ndvars,
		matrices,
		count,
		0);

	// and for each one create a bdd storing which rows/choices are non-empty
	std::vector<bdd::MTBDD> matrices_bdds(nm);

	std::vector<int> nr_of_prob_choices (nm);
	int max_prob_choice = 0;

	for (unsigned i = 0; i < nm; ++i) {
		MTBDD tmp2 ((matrices[i].GreaterThan(0)).toMTBDD().Exist(cvars));
		nr_of_prob_choices[i] = (int)tmp2.Exist(rvars).CountMinterm(pvars.size());
		max_prob_choice = std::max(nr_of_prob_choices[i],max_prob_choice);
		matrices_bdds[i] = tmp2.Exist(pvars);
	}
	std::vector<MTBDD> prob_choice(max_prob_choice+1);
	for (int i = 0; i < max_prob_choice; ++i) {
		prob_choice[i] = dd_mgr.Encode(pvars, (long) i, 1);
	}

	// create arrays
	row_starts.resize(n+1);
	choice_starts.resize(nc+1);


	// first traverse mtbdds to compute how many choices are in each row
	for (unsigned i = 0; i < n+1; ++i) row_starts[i] = 0;
	for (unsigned i = 0; i < nm; ++i) {
		traverse_mtbdd_vect_rec(
		ddman,
		matrices_bdds[i].getDdNode(),
		rvars,
		num_vars,
		0,
		odd.getOddNode(),
		0,
		1);

	}
	// and use this to compute the starts information
	// (and at same time, compute max num choices in a state)
	max = 0;
	for (unsigned i = 1 ; i < n+1; ++i) {
		if (row_starts[i] > max) max = row_starts[i];
		row_starts[i] += row_starts[i-1];

	}
	k = max;

	// Added by EMH: Save for each nondeterministic choice of each state
	// what the original action was. This is necessary to later match
	// a choice with an action.

	std::vector<long> choices(nm);
	choice = 0;
	actions.resize(nc + 1,-1);

	count = 0;
	compute_nondet_actions(
		ddman,
		matrix.getDdNode(),
		ndvars,
		num_ndvars,
		choices,
		count,
		num_interleaving_choice_vars);

	std::vector<std::vector<MTBDD> > cofactor_matrix(nm);


	// now traverse mtbdds to compute how many transitions in each choice
	for (unsigned i = 0; i < nc+1; ++i) choice_starts[i] = 0;
	for (unsigned i = 0; i < nm; ++i) {
		int action = choices[i];

		count = 0;
		cofactor_matrix[i].resize(nr_of_prob_choices[i]);
		split_mdp_rec(
			ddman,
			matrices[i].getDdNode(),
			pvars,
			pvars.size(),
			cofactor_matrix[i],
			count,
			0);

		// traverse the different cofactors
		for (int j = 0; j < nr_of_prob_choices[i]; ++j) {
			traverse_mtbdd_matr_rec (
				ddman,
				cofactor_matrix[i][j].getDdNode(),
				rvars,
				cvars,
				num_vars,
				0,
				odd.getOddNode(),
				odd.getOddNode(),
				0,
				0,
				10,
				false,
				action,
				0 );
		}

		traverse_mtbdd_vect_rec(
			ddman,
			matrices_bdds[i].getDdNode(),
			rvars,
			num_vars,
			0,
			odd.getOddNode(),
			0,
			2);
	}
	// and use this to compute the starts2 information
	// (and at same time, compute max num transitions in a choice)
	max2 = 0;
	for (unsigned i = 1; i < nc+1; ++i) {
		if (choice_starts[i] > max2) max2 = choice_starts[i];
		choice_starts[i] += choice_starts[i-1];

	}
	// recompute starts (because we altered them during last traversal)
	for (unsigned i = n; i > 0; --i) {
		row_starts[i] = row_starts[i-1];
	}
	row_starts[0] = 0;

	// max num choices/transitions determines whether we store counts or starts:
	use_counts = (max < (long)(1 << (8*sizeof(unsigned char))));
	use_counts &= (max2 < (long)(1 << (8*sizeof(unsigned char))));
	/* added by EMH: we can't use this because of extend_yes_vector */
	use_counts = 0;


	nnz = (int)matrix.CountMinterm(2 * rvars.size() + ndvars.size() + pvars.size());
	non_zeros.resize(nnz);
	updates.resize(nnz);
	cols.resize(nnz);
	
	MSG(0,"MDP::Sparse: rvars.size() %d ndvars.size() %d pvars.size() %d nm %d nc %d nnz %d\n",
		rvars.size(),ndvars.size(),pvars.size(), nm, nc, nnz);

	// now traverse the mtbdd again to get the actual matrix entries
	for (unsigned i = 0; i < nm; ++i) {
		int action = choices[i];
		// traverse the different cofactors
		for (int j = 0; j < nr_of_prob_choices[i]; ++j) {
			traverse_mtbdd_matr_rec (
				ddman,
				cofactor_matrix[i][j].getDdNode(),
				rvars,
				cvars,
				num_vars,
				0,
				odd.getOddNode(),
				odd.getOddNode(),
				0,
				0,
				11,
				false,
				action,
				j );
		}

		traverse_mtbdd_vect_rec(
			ddman,
			matrices_bdds[i].getDdNode(),
			rvars,
			num_vars,
			0,
			odd.getOddNode(),
			0,
			2);
	}
	// recompute starts (because we altered them during last traversal)
	for (unsigned i = n; i > 0; --i) {
		row_starts[i] = row_starts[i-1];
	}
	row_starts[0] = 0;
	// recompute starts2 (likewise)
	for (unsigned i = nc; i > 0; --i) {
		choice_starts[i] = choice_starts[i-1];
	}
	choice_starts[0] = 0;

	// if it's safe to do so, replace starts/starts2 with (smaller) arrays of counts
	if (use_counts) {
		row_counts.resize(n);
		for (unsigned i = 0; i < n; ++i) row_counts[i] = (unsigned char)(row_starts[i+1] - row_starts[i]);
		row_starts.clear();
		choice_counts.resize(nc);
		for (unsigned i = 0; i < nc; ++i) choice_counts[i] = (unsigned char)(choice_starts[i+1] - choice_starts[i]);
		choice_starts.clear();
		mem = (nnz * (sizeof(double) + sizeof(unsigned int)) + (n+nc) * sizeof(unsigned char)) / 1024.0;
	} else {
		mem = (nnz * (sizeof(double) + sizeof(unsigned int)) + (n+nc) * sizeof(int)) / 1024.0;
	}

	construction_t.Stop();
	MSG(0,"transition matrix n=%d nnz=%d nc=%d nm=%d (time: %5.2f)\n",n,nnz,nc,nm,construction_t.Read()*1000);
	//assert(sm.sanityCheck(odd));
}

void MDPSparse::UnboundedUntil (
		bdd::DDManager& dd_mgr,
		bdd::ODD& odd,
		bdd::MTBDD &init,
		bdd::MTBDD &yes,
		bdd::MTBDD &maybe,
		bool min,		// min or max probabilities (true = min, false = max)
		ActionGraph &graph,
		Witnesses& w,
		double& lower,
		double& upper) {
	BoundedUntil (
		dd_mgr,
		odd,
		init,
		yes,
		maybe,
		min,		// min or max probabilities (true = min, false = max)
		graph,
		w,
		lower,
		upper,
		util::Database::max_iters);
}

void    MDPSparse::BoundedUntil (
	bdd::DDManager& dd_mgr,
	bdd::ODD& odd,
	MTBDD &init,
	MTBDD &yes,
	MTBDD &maybe,
	bool min,		// min or max probabilities (true = min, false = max)
	ActionGraph &graph,
	Witnesses& w,
	double& lower,
	double& upper,
	unsigned bound)
{
	int n = odd.getNumOfStates();

	if(yes == dd_mgr.Constant(0)) {
		lower = upper = 0.0;
		return;
	}


	util::Statistics::valueiterTimer.Start();

	// model checking
	std::vector<double> yes_vec;
	odd.toDoubleVector(yes, yes_vec);
	std::vector<double> original_yes(yes_vec);
	std::vector<int> lower_chosen(n,-1);
	std::vector<int> upper_chosen(n,-1);
	std::vector<double> lower_soln(n,0.0);
	std::vector<double> upper_soln(n,0.0);

	odd.toDoubleVector(init,init_vec);

	bool bailout = false;

	double result;
	lower_soln = yes_vec;
	upper_soln = yes_vec;
	for(int i=0; i<n; ++i) {
		if(init_vec[i] == 1.0 && yes_vec[i]==1.0) {
			MSG(0,"MDPSparse::BoundedUntil: Warning initial state is bad state\n");

			bailout = true;
			result = 1.0;
			break;
		}
	}

	util::Timer valiter_t;
	valiter_t.Start();

	IntervalUntil(init_vec,yes_vec,min,lower_soln,upper_soln,lower_chosen,upper_chosen);
	valiter_t.Stop();

	bool strategiesAgree ( true );
	lower = 2.0;
	upper = -1.0;
	
	for(unsigned i=0; i<n; ++i) {
		if(init_vec[i]==1.0) {
			MSG(0,"interval [%E,%E] (time: %5.2f)\n",lower_soln[i],upper_soln[i],valiter_t.Read()*1000);
			lower = lower_soln[i] < lower ? lower_soln[i] : lower;
			upper = upper_soln[i] > upper ? upper_soln[i] : upper;
		}
	}

	for(unsigned i=0; i<n; ++i) 	
		strategiesAgree = strategiesAgree && upper_chosen[i]!=lower_chosen[i];			
	if(!approxEquality(upper,lower,util::Database::epsilon) && !strategiesAgree) {
		computeGraph(min,init_vec,original_yes,lower_soln,upper_soln, lower_chosen, upper_chosen,graph);
	} else {
		lower = upper;
	}

	util::Statistics::valueiterTimer.Stop();


	if(util::Database::displayCEX) {
		std::string filename("cex_"+util::intToString(util::Statistics::nr_of_refine)+ ".gdl");
		std::ofstream file (filename.c_str());
		file << graph.toString() + "\n";
	}

	


	if(util::Database::DEBUG || util::Database::displayMarkovChain) {
		std::string filename("game_"+util::intToString(util::Statistics::nr_of_refine)+ ".gdl");

		std::ofstream file (filename.c_str());
		aiSee(file,
				   odd,
				   init_vec,
				   yes_vec,
				   lower_soln,
				   upper_soln,
				   lower_chosen,
				   upper_chosen);
	}
}



double MDPSparse::CTBoundedUntil (
	DDManager& dd_mgr,
	ODD& odd,
	std::vector<MTBDD> &rvars,
	std::vector<MTBDD> &cvars,
	std::vector<MTBDD> &ndvars,
	std::vector<MTBDD> &pvars,
	int num_interleaving_choice_vars,
	MTBDD &init,
	MTBDD &trans,
	MTBDD &yes,
	MTBDD &maybe,
	bool min,
	double time,
	ActionGraph &graph)
{
	int n = odd.getNumOfStates();

	if(yes == dd_mgr.Constant(0)) {
		return 0.0;
	}

	util::Statistics::valueiterTimer.Start();
	// filter out rows
	MTBDD a (trans * maybe);

	// build sparse matrix
	MSG(1,"\nBuilding sparse matrix... ");
	MDPSparse ndsm(dd_mgr, a, rvars, cvars, ndvars, pvars, num_interleaving_choice_vars, odd,
			util::Database::max_iters, util::Database::term_crit, util::Database::term_crit_param);

	// model checking
	std::vector<double> yes_vec;
	odd.toDoubleVector(yes, yes_vec);
	std::vector<double> original_yes(yes_vec);
	std::vector<int> chosen(n,-1);
	std::vector<double> soln(n,0.0);

	odd.toDoubleVector(init,ndsm.init_vec);

	bool bailout = false;

	double result;
	soln = yes_vec;
	for(int i=0; i<n; ++i) {
		if(!min && ndsm.init_vec[i] == 1.0 && soln[i]==1.0) {
			MSG(0,"MDPSparse::UnboundedUntil: Warning initial state is bad state\n");

			bailout = true;
			result = 1.0;
			break;
		}
	}


	//if(!bailout)
	result = ndsm.CTBoundedUntil(ndsm.init_vec,yes_vec,min,soln,chosen,time);
	std::ofstream mdp_file ("mdp.dt");
	ndsm.toDOT(mdp_file, odd, ndsm.init_vec, original_yes);


	// get counterexample graph
	ndsm.computeGraph(min,ndsm.init_vec,original_yes,original_yes,soln,chosen,chosen,graph);

	util::Statistics::valueiterTimer.Stop();

	return result;
}

void MDPSparse::toDOT(std::ostream& stream, bdd::ODD& odd, std::vector<double>& init_vec, std::vector<double>& yes_vec) const {

	stream << "digraph MDP {\n" ;

	std::vector<double> vec(odd.getNumOfStates(),0.0);
	std::vector<double> zero(odd.getNumOfStates(),0.0);

	unsigned l1(0), h1(0), l2(0), h2(0), i(0), j(0);
	// states
	for (i = 0; i < n; ++i) {
		std::string fillcolor;

		vec = zero;
		vec[i] = 1.0;
		bdd::MTBDD state_mtbdd = odd.toMTBDD(vec);


		if(yes_vec[i] == 1.0) {
			fillcolor = "red";
		} else if(init_vec[i] == 1.0) {
			fillcolor = "green";
		} else {
			fillcolor = "gray";
		}

		stream << "  S_" << (int) i << " [";
		stream << "width=\"0.7\" ";
		stream << "shape=\"circle\", style=\"filled\", fillcolor=\""<< fillcolor<<"\" ";

// 		stream << ", label=\"";
// 		state_mtbdd.PrintMinterm(stream);
		stream <<"]\n";
	}
	// distributions
	for (i = 0; i < n; ++i) {

		if (!use_counts) { l1 = row_starts[i]; h1 = row_starts[i+1]; }
		else { l1 = h1; h1 += row_counts[i]; }
		for (j = l1; j < h1; ++j) {
			stream << "  S_" << i << " -> P1_"<<j;
			stream << " [label=\"" << actions[j] << "\" ]\n";
			stream << "  P1_" << j << " [";
			stream << "label=\"\", width=\"0.1\", shape=\"circle\", ";
			stream << "style=\"filled\", fillcolor=\"black\"";
			stream << "];\n";
		}
	}
	// matrix entries
	for (i = 0; i < n; ++i) {

		if (!use_counts) { l1 = row_starts[i]; h1 = row_starts[i+1]; }
		else { l1 = h1; h1 += row_counts[i]; }


		// traverse distributions
		for (j = l1; j < h1; ++j) {
			if (!use_counts) { l2 = choice_starts[j]; h2 = choice_starts[j+1]; }
			else { l2 = h2; h2 += choice_counts[j]; }

			// traverse successors
			for (unsigned k = l2; k < h2; ++k) {
				stream << "  P1_" << j << " ->  S_"<< cols[k] ;
				stream << "[label=\""<< non_zeros[k] << "\"] \n" ;
			}
		}
	}
	stream << "}" << std::endl;



}

  /**
 * Print graph node with given attributes.
 */
inline
void printAiSeeNode(std::ostream &stream, int id, const std::string& label,
		const std::string& infobox, const std::string& infobox2, int width,
		int height,
		int horizontal_order,
		int vertical_order,
		const std::string& shape, const std::string& fill) {
	stream << "node: { " << "\n" << "  title: \"" << id << "\"\n"
			<< "  width: " << width << "\n" << "  height: " << height << "\n"
			<< "  horizontal_order: " << horizontal_order << "\n"
			<< "  vertical_order: " << vertical_order << "\n"
			<< "  shape: " << shape << "\n";
	if ("" != infobox) {
		stream << "  info1: \"" << infobox << "\"\n";
	}
	if ("" != infobox2) {
		stream << "  info2: \"" << infobox2 << "\"\n";
	}
	if ("" != fill) {
		stream << "  color: " << fill << "\n";
	}
	stream << "  label: \"" << label << "\"\n" << "}\n";
}

inline
void printAiSeeNode(std::ostream &stream, int id, const std::string& label,
		const std::string& infobox, const std::string& infobox2, int width,
		int height,
		const std::string& shape,
		const std::string& fill,
		const std::string textcolor = "",
		int borderwidth = -1) {
	stream << "node: { " << "\n" << "  title: \"" << id << "\"\n"
			<< "  width: " << width << "\n" << "  height: " << height << "\n"
			<< "  shape: " << shape << "\n";
	if ("" != infobox) {
		stream << "  info1: \"" << infobox << "\"\n";
	}
	if ("" != infobox2) {
		stream << "  info2: \"" << infobox2 << "\"\n";
	}
	if ("" != fill) {
		stream << "  color: " << fill << "\n";
	}
	if ("" != textcolor) {
		stream << "  textcolor: " << textcolor << "\n";
	}


	if(-1 != borderwidth) {
		stream << "  borderwidth: " << borderwidth << "\n";
	}

	stream << "  label: \"" << label << "\"\n" << "}\n";
}
/**
 * Prints graph edge with given attributes.
 */
inline
void printAiSeeEdge(std::ostream &stream, int source, int target,
		const std::string& label, int width,
		// "line", "dashed", "dotted"
		const std::string& style,
		//"delta", "standard", "diamond", "short", "white_delta", "white_diamond", or "none"
		const std::string& sourceArrow,
		//"delta", "standard", "diamond", "short", "white_delta", "white_diamond", or "none"
		const std::string& targetArrow, const std::string& fill) {
	stream << "edge: {\n " << "  sourcename: \"" << source << "\"\n"
			<< "  targetname: \"" << target << "\"\n" << "  thickness: "
			<< width << "\n";
	if ("" != style) {
		stream << "  style: " << style << "\n";
	}
	if ("" != fill) {
		stream << "  color: " << fill << "\n";
	}
	stream << "  backarrowstyle: " << sourceArrow << "\n" << "  arrowstyle: "
			<< targetArrow << "\n" << "  label: \"" << label << "\"\n"
			<< "  class: 1}\n";
}


void MDPSparse::aiSee(std::ostream& stream,
		      bdd::ODD& odd,
		      std::vector<double>& init_vec,
		      std::vector<double>& yes_vec,
		      const std::vector<double>& lower_soln,
		      const std::vector<double>& upper_soln,
		      const std::vector<int>& lower_chosen,
		      const std::vector<int>& upper_chosen) const {

	MSG(0,"MDPSpase::aiSee\n")

	stream << "graph: {" << std::endl << std::endl
		<< "colorentry 1: 153 255 0" << std::endl
		<< "colorentry 2: 255 255 0" << std::endl
		<< "colorentry 3: 0 0 0" << std::endl // black
		<< "colorentry 4: 255 255 221" << std::endl
		<< "colorentry 5: 128 0 128" << std::endl // purple
		<< "colorentry 6: 0 0 255" << std::endl // blue
		<< "colorentry 7: 200 0 0" << std::endl << std::endl // red
		<< "colorentry 8: 200 200 200" << std::endl // lightgray
		<< "colorentry 9: 255 255 255" << std::endl; // white

	// shapes
	const std::string box("box");
	const std::string rhomb("rhomb");
	const std::string hexagon("hexagon");
	const std::string triangle("triangle");
	const std::string ellipse("ellipse");

	// colors
	const std::string green("1");
	const std::string yellow("2");
	const std::string black("3");
	const std::string gray("4");
	const std::string purple("5");
	const std::string blue("6");
	const std::string red("7");
	const std::string lightgray("8");
	const std::string white("9");

	std::vector<double> vec(odd.getNumOfStates(),0.0);
	std::vector<double> zero(odd.getNumOfStates(),0.0);

	int width, height;
	int horizontal_order, vertical_order;

	unsigned l1(0), h1(0), l2(0), h2(0), i(0), j(0);

	std::string label;
	std::string sourceArrow = "none";
	std::string targetArrow = "solid";
	std::string style;
	std::string shape;
	std::string fill = black;

	std::string infobox1, infobox2;

	// states


	// box the SCCs states
#if 0
	if(false && strong_components.size()>0)
	for(unsigned i=0; i<strong_components.size(); ++i) {
		stream << "graph: { title: \"SCC " << i <<"\"\n"
		       << "  state : clustered \n";
			for(unsigned j=0; j<strong_components[i].size(); ++j) {

					std::string fillcolor;
			unsigned state(strong_components[i][j]);

			vec = zero;
			vec[state] = 1.0;
			//bdd::MTBDD state_mtbdd = odd.toMTBDD(vec);

			float diff(upper_soln[state] - lower_soln[state]);
			label = "[" + util::floatToString(lower_soln[state]) +
			        "," + util::floatToString(upper_soln[state]) + "]";

			if(yes_vec[state] == 1.0) {
				fillcolor = red;
				vertical_order = 2;
				horizontal_order = 0;
				shape = box;
			} else if(init_vec[state] == 1.0) {
				fillcolor = green;
				width = 160;
				height = 80;
				vertical_order = 0;
				horizontal_order = 0;
				shape = hexagon;
			} else {
				fillcolor = (diff == 0 || lower_chosen[state] == upper_chosen[state]) ? gray : yellow;
				width = -1;
				height = -1;
				vertical_order = 1;
				horizontal_order = 0;
				shape = box;
			}

			printAiSeeNode(stream,state, label,
				infobox1, // infobox1
				infobox2, // infobox2
				width, // width
				height, // height
				/* horizontal_order,
				vertical_order, */
				shape,
				fillcolor
				);

			if (!use_counts) { l1 = row_starts[state]; h1 = row_starts[state+1]; }
			else { l1 = h1; h1 += row_counts[state]; }

			// traverse distributions
			for (unsigned j = l1; j < h1; ++j) {


				width = 2;
				fill  = "3";

				label = util::intToString(actions[j]);

				printAiSeeNode(stream,
						   n + j,
						   label,
						   "",
						   "",
						   -1,
						   -1,
						   /* 0,
						   1, */
						   triangle,
						   gray);

				bool lower(lower_chosen[state] == (int)j);
				bool upper(upper_chosen[state] == (int)j);

				if( lower && upper) {
					fill = purple;
				} else if( lower) {
					fill = blue;
				} else if( upper) {
					fill = red;
				} else {
					fill = black;
				}

				printAiSeeEdge(stream, state, n + j, "", width, style,
						"none", "none", fill);


				l2 = choice_starts[j];
				h2 = choice_starts[j+1];
				// traverse successors
				for (unsigned k = l2; k < h2; ++k) {
					label = util::floatToString(non_zeros[k]);
					width = 1;
					fill = "3";

					printAiSeeEdge(stream, n + j, (int) cols[k], label, width, style,
						sourceArrow, targetArrow, fill);
				}
			}

		}
		stream << "}\n";
	}
	else
#endif


	for (i = 0; i < n; ++i) {

		if(util::Database::displayMarkovChain && !include[i] && yes_vec[i] != 1.0) continue;

		std::string fillcolor;
		std::string textcolor(black);
		int borderwidth = -1;

		vec = zero;
		vec[i] = 1.0;
		//bdd::MTBDD state_mtbdd = odd.toMTBDD(vec);

		float diff(upper_soln[i] - lower_soln[i]);
		label = " B" + util::intToString(i) + " " + // block number, i.e., B1 = block nr. 1
			( diff == 0 ? util::floatToString(upper_soln[i]) :
			"[" + util::floatToString(lower_soln[i])
		      + "," + util::floatToString(upper_soln[i]) + "]") + "\n";

		if(yes_vec[i] == 1.0) {
			fillcolor = red;
			textcolor = white;
			vertical_order = n;
			horizontal_order = 0;
			shape = box;
			width = -1;
			height = -1;
		} else if(init_vec[i] == 1.0) {
			fillcolor = green;
			width = -1;
			height = -1;
			vertical_order = 0;
			horizontal_order = 0;
		} else {
			fillcolor = (diff == 0 || lower_chosen[i] == upper_chosen[i]) ? gray : yellow;
			width = -1;
			height = -1;
			shape = box;
		}

		if( i < state_info.size()) {
			label += state_info[i];
		}


		if( i < include.size()) {
			if(include[i]) {
				borderwidth = 3;
			} else {

				if(yes_vec[i] == 1.0) {
					fillcolor = red;
					textcolor  = white;
					borderwidth = 0;
				} else {

					if(lower_soln[i] == upper_soln[i]) {
						fillcolor = white;
						borderwidth = 1;
					} else {
						fillcolor = lightgray;
					}
				}
			}
		}

		if( pivot_state != -1 && i == (unsigned) pivot_state) {
			borderwidth = 10;
		}

		printAiSeeNode(stream, i, label,
			infobox1, // infobox1
			infobox2, // infobox2
			width, // width
			height, // height
			shape,
			fillcolor,
			textcolor,
			borderwidth
			);

		if (!use_counts) { l1 = row_starts[i]; h1 = row_starts[i+1]; }
		else { l1 = h1; h1 += row_counts[i]; }

		// traverse distributions
		for (j = l1; j < h1; ++j) {


			width = 2;
			fill  = "3";

			label = util::intToString(actions[j]);

			if( i < state_info.size()) {
						infobox1 = action_info[actions[j]];
					}


			printAiSeeNode(stream,
						   n + j,
						   label,
						   infobox1,
						   infobox2,
						   -1,
						   -1,
						   /* 0,
						   1, */
						   triangle,
						   gray);
			bool lower(lower_chosen[i] == (int)j);
			bool upper(upper_chosen[i] == (int)j);

			if( lower && upper) {
				fill = purple;
			} else if( lower) {
				fill = blue;
			} else if( upper) {
				fill = red;
			} else {
				fill = black;
			}

			printAiSeeEdge(stream, i, n + j, "", width, style,
					"none", "none", fill);

			l2 = choice_starts[j];
			h2 = choice_starts[j+1];
			// traverse successors
			for (unsigned k = l2; k < h2; ++k) {
				label = util::floatToString(non_zeros[k]);
				width = 1;
				fill = "3";

				int succ((int) cols[k]);
				if(util::Database::displayMarkovChain && !include[succ] && yes_vec[succ] != 1.0) continue;

				printAiSeeEdge(stream, n + j, succ, label, width, style,
					sourceArrow, targetArrow, fill);
			}

		}
	}

	// end (box the goal states)
	stream << "}" << std::endl;
}

struct SuccessorVisitor {
public:
	SuccessorVisitor(const MDPSparse& __mdp) : mdp(__mdp) {}

	inline void getSuccessors(unsigned state, std::set<unsigned>& result) {
		std::vector<unsigned> succ;
		for (unsigned i = 0; i < mdp.n; ++i) {
			const unsigned l1 = mdp.row_starts[i];
			const unsigned h1 = mdp.row_starts[i+1];

			for (unsigned j = l1; j < h1; ++j) {
				const unsigned l2 = mdp.choice_starts[j];
				const unsigned h2 = mdp.choice_starts[j+1];
				// traverse successors
				for (unsigned k = l2; k < h2; ++k) result.insert(mdp.cols[k]);
			}
		}

	}
private:
	const MDPSparse& mdp;
};

struct DFSVisitor {
public:
	void discover_vertex(unsigned state, const MDPSparse& graph) ;
	void finish_vertex(unsigned state, const MDPSparse& graph) ;
};


struct DFS {


};


struct TarjanVisitor : DFSVisitor {
protected:

	unsigned min_discover_time(unsigned u, unsigned v) {
		return graph.dfs_map[u].discover_time < graph.dfs_map[v].discover_time ? u : v;
	}
	const MDPSparse& graph;
	unsigned& c;
	std::vector<unsigned>& s;
	unsigned& dfs_time;
	DFSMap& dfs_map;
public:
	TarjanVisitor(
		const MDPSparse& __graph,
		unsigned& __c,
		std::vector<unsigned>& __s,
		unsigned& __dfs_time,
		DFSMap& __dfs_map) :
		   graph(__graph)
		,  c (__c)
		,  s (__s)
		,  dfs_time(__dfs_time)
		,  dfs_map(__dfs_map) {}

	void discover_vertex(unsigned v) {
		DFSEntry entry(dfs_map[v]);
	        entry.root = v;
	        entry.comp = (std::numeric_limits<unsigned>::max)();
        	entry.discover_time = dfs_time++;
        	s.push_back(v);
	}

	void finish_vertex(unsigned v) {
		DFSEntry entry(dfs_map[v]);
		unsigned w;
		for (unsigned i = 0; i < graph.n; ++i) {
			const unsigned l1 = graph.row_starts[i];
			const unsigned h1 = graph.row_starts[i+1];

			for (unsigned j = l1; j < h1; ++j) {
				const unsigned l2 = graph.choice_starts[j];
				const unsigned h2 = graph.choice_starts[j+1];
				// traverse successors
				for (unsigned k = l2; k < h2; ++k) {
					w = graph.cols[k];
          				if (dfs_map[w].comp == (std::numeric_limits<unsigned>::max)())
            				entry.root = min_discover_time(entry.root, dfs_map[w].root);
				}
			}
		}

        	if (entry.root == v) {
          		do {
            			w = s.back(); s.pop_back();
            			dfs_map[w].comp = c;
          		} while (w != v);
          		++c;
        	}
        }

};


/**
\brief Tarjan's SCC algorithm
\note
Input: Graph G = (V, E), Start node v0

index = 0                       // DFS node number counter
S = empty                       // An empty stack of nodes
tarjan(v0)                      // Start a DFS at the start node

procedure tarjan(v)
  v.index = index               // Set the depth index for v
  v.lowlink = index
  index = index + 1
  S.push(v)                     // Push v on the stack
  forall (v, v') in E do        // Consider successors of v
    if (v'.index is undefined)  // Was successor v' visited?
      tarjan(v')                // Recurse
      v.lowlink = min(v.lowlink, v'.lowlink)
    elseif (v' in S)            // Is v' on the stack?
      v.lowlink = min(v.lowlink, v'.index)
  if (v.lowlink == v.index)     // Is v the root of an SCC?
    print "SCC:"
    repeat
      v' = S.pop
      print v'
    until (v' == v)
*/
void Tarjan(
		      unsigned state,
		      int& index,
		      std::vector<int>& indices,
		      std::vector<int>& low_link,
		      std::vector<int>& S,
		      std::vector<bool>& on_S,
		      SuccessorVisitor& sv,
		      std::vector<std::vector<unsigned> >& components
		      )  {
	indices[state] = index;
	low_link[state] = index;
	++index;
	S.push_back(state);
	on_S[state] = true;

	std::set<unsigned> successors;
	sv.getSuccessors(state,successors);
	for(std::set<unsigned>::iterator i = successors.begin(); i!=successors.end(); ++i) {
		unsigned successor(*i);
		if(indices[successor] == -1) {
			 Tarjan(successor,
		      		index,
		      		indices,
		      		low_link,
		      		S,
				on_S,
		      		sv,
				components);
			low_link[state] = std::min(low_link[state],low_link[successor]);
		} else if(on_S[successor]) {
			low_link[state] = std::min(low_link[state],indices[successor]);
		}
	}

	if(low_link[state] == indices[state]) {
		unsigned other_state;

		std::vector<unsigned> component;

		do {

			other_state = S.back();
			component.push_back(other_state);
			S.pop_back();
			on_S[other_state] = false;

		} while( other_state != state);
		components.push_back(component);

		MSG(0,"\n");
	}
}

void MDPSparse::strongComponents() {

	std::vector<int> indices(n,-1);
	std::vector<int> low_link(n,-1);
	std::vector<int> S;
	std::vector<bool> on_S(n,false);
	int index = 0;
	SuccessorVisitor sv(*this);

	unsigned init;
	for(unsigned i=0; i<n; ++i) {
		if(init_vec[i] == 1.0) {
			init = i;

			// iterate over different start nodes
			Tarjan(
			init,
			index,
			indices,
			low_link,
			S,
			on_S,
			sv,
			strong_components
			);
		}
	}
}

bool MDPSparse::isRefinable(unsigned state,
		unsigned action ) const {
	const unsigned l1 = row_starts[state];
	const unsigned h1 = row_starts[state+1];

	int counter = 0;
	for(unsigned j=l1 ; j<h1; ++j) {
		if((int)action == actions[j]) {
			++counter;

			// check if several probabilistic choices got merged by abstraction
			unsigned start = choice_starts[j];
			unsigned end = choice_starts[j+1];
			std::set <int> s;
			for(unsigned k=start; k<end;++k) {
				if(s.count(cols[k])>0)
					return true;
				s.insert(cols[k]);
			}
		}
	}
	return counter > 1;
	//TODO: check if this is a must transition!!!
}

void MDPSparse::getWitnessFromState(
		unsigned state,
		unsigned lower_choice,
		unsigned upper_choice,
		Witness& witness) const {
	// lookup entry for the state in the sparse matrix

	assert(lower_choice != upper_choice);
	witness.state = state;

	// lower bound
	assert(upper_choice >= 0 && upper_choice < actions.size());
	assert(lower_choice >= 0 && lower_choice < actions.size());
	witness.lower_action = actions[lower_choice];


	int start = choice_starts[lower_choice];
	int end = choice_starts[lower_choice+1];

	for(int k=start; k<end;++k) {
		witness.lower_states.push_back(cols[k]);
	}

	// upper bound

	witness.upper_action = actions[upper_choice];

	start = choice_starts[upper_choice];
	end = choice_starts[upper_choice+1];
	for(int k=start; k<end;++k) {
			witness.upper_states.push_back(cols[k]);
	}
}

void MDPSparse::computeWitnesses(
		unsigned state,
		unsigned lower_choice,
		unsigned upper_choice,
		const std::vector<double> & lower_soln,
		const std::vector<double> & upper_soln,
		std::vector<Witness>& witness) const {

	unsigned lower_action = actions[lower_choice];
	unsigned upper_action = actions[upper_choice];

	const unsigned l1 = row_starts[state];
	const unsigned h1 = row_starts[state+1];

	if(lower_choice == upper_choice)
		return;

	for(unsigned j=l1 ; j<h1; ++j) {
		int action (actions[j]);

		if(lower_action == upper_action) {
			if(action == lower_action && j!=lower_choice ) {
				Witness wit;
				getWitnessFromState(state,lower_choice,j,wit);
				witness.push_back(wit);
			}
		} else {
			if(action == lower_action && j!=lower_choice ) {
				Witness wit;
				getWitnessFromState(state,lower_choice,j,wit);
				witness.push_back(wit);
			} else if(action == upper_action && j!=upper_choice ) {
				Witness wit;
				getWitnessFromState(state,j,upper_choice,wit);
				witness.push_back(wit);
			}
		}
	}

	/*
	 * (1)
	 * construct witnesses consisting of pairs where the
	 * first one is the lower_choice and the second
	 * a choice with the same action (TODO: but HIGHER lower bound)
	 * (2)
	 * construct witnesses consisting of pairs where the
	 * first one is the upper_choice and the second
	 * a choice with the same action (TODO: but LOWER upper bound)
	 */


}


// build nondeterministic (mdp) sparse matrix
void MDPSparse::computeWitnesses(
		const std::vector<double> & init_vec,
		const std::vector<double> & yes_vec,
		const std::vector<double> & lower_soln,
		const std::vector<double> & upper_soln,
		const std::vector<int> & lower_chosen,
		const std::vector<int> & upper_chosen, std::vector<Witness> & witnesses) const
{
	int chosen_state(-1);
	double max_deviation(-1.0);
	// maximum deviation
	for(unsigned i=0; i<n; ++i) {
		double diff(upper_soln[i] - lower_soln[i]);
		if(max_deviation<diff && lower_chosen[i] != upper_chosen[i]) {
			if(actions[lower_chosen[i]] == actions[upper_chosen[i]]) {
				chosen_state = i;
				max_deviation = diff;
			} else {
				MSG(0,"MDPSparse::computeWitnesses: %d \n",i);
			}
		}
	}

	if(chosen_state!=-1) {
		witnesses.resize(1);
		getWitnessFromState(chosen_state, lower_chosen[chosen_state], upper_chosen[chosen_state], witnesses[0]);
	}
}


}
