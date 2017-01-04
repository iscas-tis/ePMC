/* NOT YET USABLE */
#include <math.h>
#include "util/Util.h"
#include "util/Database.h"
#include <iostream>
#include "GameSparse.h"
#include "FoxGlynn.h"

using namespace std;
using namespace model_checker;

void GameSparse::CTUntil(bool P2min, std::vector<double>& lower,
	   std::vector<double>& upper) {
#if 0	
	std::vector<double> lower2 (num_states, 0.0);
	std::vector<double> upper2 (num_states, 0.0);

	//	std::vector<int> chosen (num_states, -1);
	FoxGlynn fg(rate, term_crit_param);
	unsigned left = fg.getLeft();
	unsigned right = fg.getRight();
	vector<double> &weights = fg.getWeights();
	double total_weight = fg.getTotalWeight();

	for (unsigned i = right; i > 0; i--) {
		double psi = 0.0;
		if (i >= left) {
		  psi = weights[i - left] / total_weight;
		}
		unsigned ps_index = 0;
		
		//traverse states	  
		for (unsigned state = 0; state < num_states; ++state) {
			double d1_lower = 2.0;
			double d1_upper = -1.0;

			unsigned l1 = row_starts[state];
			unsigned h1 = row_starts[state+1];

			// traverse distributions
			for (unsigned j = l1; j < h1; ++j) {
				double d2_lower = P2min ? 2.0 : -1.0;
				double d2_upper = P2min ? 2.0 : -1.0;
				unsigned l2 = p1_starts[j];
				unsigned h2 = p1_starts[j+1];
				int opt_j = -1;
				// traverse successors
				for (unsigned k = l2; k < h2; ++k) {
				  unsigned l3 = p2_starts[k];
				  unsigned h3 = p2_starts[k+1];
				  double d3_lower = 0.0;
				  double d3_upper = 0.0;
				  for (unsigned m = l3; m < h3; ++m) {
				    d3_lower += non_zeros[m] * lower[cols[m]];
				    d3_upper += non_zeros[m] * upper[cols[m]];
				  }
				  if (i >= left) {
				    d3_lower += psi * P_s_alpha_B[ps_index];
				    d3_upper += psi * P_s_alpha_B[ps_index];
				  }

				  // check for optimum
				  if ( P2min ? d3_lower < d2_lower : d3_lower > d2_lower ) {
				    d2_lower = d3_lower;
				  }
				  // check for optimum
				  if ( P2min ? d3_upper < d2_upper : d3_upper > d2_upper ) {
				    d2_upper = d3_lower;
				  }
				  if ( P2min ? d3_lower < d2_lower : d3_upper > d3_upper ) {
				    opt_j = k;
				  }
				  ps_index++;
				}
				// check for optimum
				// d1 = d2; opt_j = j;
				if (d2_lower < d1_lower) {
				  d1_lower = d2_lower;
				}
				if (d2_upper > d1_upper) {
				  d1_upper = d2_upper;
				}
			}
			lower2[state] = d1_lower;
			upper2[state] = d1_upper;
		}
		lower.swap(lower2);
		upper.swap(upper2);
	}

	for (unsigned state = 0; state < num_states; state++) {
	  if (bad_states[state]) {
	    lower[state] = 1.0;
	    upper[state] = 1.0;
	  }
	}
#endif
}

//------------------------------------------------------------------------------
