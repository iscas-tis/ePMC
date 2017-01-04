#include "util/Error.h"
#include <math.h>
#include <float.h>
#include <assert.h>
#include "FoxGlynn.h"

using namespace std;

namespace model_checker {
  FoxGlynn::FoxGlynn
  (double q_tmax, double underflow, double overflow, double accuracy) {
    assert(q_tmax > 0.0);
    assert(accuracy > 0.0);
    init(q_tmax, underflow, overflow, accuracy);
  }

  FoxGlynn::FoxGlynn(double lambda, double epsilon) {
    if (lambda > 0.0) {
      init(lambda, DBL_MIN, DBL_MAX, epsilon);
    } else {
      left = 0;
      right = 0;
      weights.assign(1, 1.0);
      total_weight = 1.0;
    }
  }

  void FoxGlynn::init
  (double q_tmax, double underflow, double overflow, double accuracy) {
    int m;
    double q;
    
    m = (int)floor(q_tmax);
    
    {
      double m2 = m;
      double k;
      
      if (q_tmax == 0.0) {
	cerr << "Overflow: TA parameter qtmax = time * maxExitRate = 0." << endl;
      }
      if (q_tmax < 25.0) {
	left = 0;
      }
      if (q_tmax < 400.0) {
	// Find right using Corollary 1 with q_tmax=400
	double sqrt2  = sqrt(2.0);
	double sqrtl  = 20;
	double a = 1.0025 * exp (0.0625) * sqrt2;
	double startk = 1.0/(2.0 * sqrt2 * 400);
	double stopk = sqrtl/(2*sqrt2);
	
	for (k = startk; k <= stopk; k += 3.0) {
	  double d = 1.0/(1 - exp ((-2.0/9.0)*(k*sqrt2*sqrtl + 1.5)));
	  double f = a * d * exp (-0.5*k*k) / (k * sqrt (2.0 * 3.1415926));
	  
	  if (f <= accuracy/2.0)
	    break;
	}
	
	if (k > stopk) 
	  k = stopk;
	
	right = (int) ceil(m2 + k*sqrt2*sqrtl + 1.5);
      }
      
      if (q_tmax >= 400.0) {
	// Find right using Corollary 1 using actual q_tmax 
	double sqrt2  = sqrt (2.0);
	double sqrtl  = sqrt (q_tmax);
	double a = (1.0 + 1.0/q_tmax) * exp (0.0625) * sqrt2;
	double startk = 1.0/(2.0 * sqrt2 * q_tmax);
	double stopk = sqrtl/(2*sqrt2);
	
	for (k = startk; k <= stopk; k += 3.0) {
	  double d = 1.0/(1 - exp ((-2.0/9.0)*(k*sqrt2*sqrtl + 1.5)));
	  double f = a * d * exp (-0.5*k*k) / (k * sqrt (2.0 * 3.1415926));
	  
	  if (f <= accuracy/2.0)
	    break;
	}
	
	if (k > stopk) 
	  k = stopk;
	
	right = (int) ceil(m2 + k*sqrt2*sqrtl + 1.5);
      }
      if (q_tmax >= 25.0) {
	// Find left using Corollary 2 using actual q_tmax 
	double sqrt2  = sqrt (2.0);
	double sqrtl  = sqrt (q_tmax);
	double b = (1.0 + 1.0/q_tmax) * exp (0.125/q_tmax);
	double startk = 1.0/(sqrt2*sqrtl);
	double stopk =  (m2 - 1.5)/(sqrt2*sqrtl);
	
	for (k = startk; k <= stopk; k += 3.0) {
	  if (b * exp(-0.5*k*k)/(k * sqrt (2.0 * 3.1415926)) <= accuracy/2.0)
	    break;
	}
	
	if (k > stopk) 
	  k = stopk;
	
	left = (int) floor(m2 - k*sqrtl - 1.5);
      }
      
      if (left < 0) {
	left = 0;
	//printf("Weighter: negative left truncation point found. Ignored.\n");
      }
      
      q = overflow / (pow(10.0, 10.0) * (right - left));
    }
    
    weights.resize(right-left+1);
    weights[m-left] = q;
    
    // down
    for (int j=m; j>(int)left; j--) {
      weights[j-1-left] = (j/q_tmax) * weights[j-left];
    }
    
    //up
    if (q_tmax < 400) {
      
      if (right > 600) {
	cerr << "Overflow: right truncation point > 600." << endl;
      } 
      
      for (int j=m; j<(int)right; ) {
	q = q_tmax / (j+1);
	
	if (weights[j-left] > underflow/q) {
	  weights[j+1-left] = q * weights[j-left];
	  j++;
	}
	else {
	  right = j;
	}
      } 
      
    }
    else {
      for (int j=m; j<(int) right; j++) {
	weights[j+1-left] = (q_tmax/(j+1)) * weights[j-left];
      }
    }
    
    {
      int l = left;
      int r = right;
      total_weight = 0.0;
      
      while (l < r) {
	if (weights[l-left] <= weights[r-left]) {
	  total_weight += weights[l-left];
	  ++l;
	}
	else {
	  total_weight += weights[r-left];
	  --r;
	}
      }
      total_weight += weights[l-left];
    }	
  }

  unsigned FoxGlynn::getRight
  (double q_tmax, double underflow, double overflow, double accuracy) {
    int m;
    int right = 0;
    
    m = (int)floor(q_tmax);
    
    double m2 = m;
    double k;
    
    if (q_tmax == 0.0) {
      cerr << "Overflow: TA parameter qtmax = time * maxExitRate = 0." << endl;
    }
    if (q_tmax < 400.0) {
      // Find right using Corollary 1 with q_tmax=400
      double sqrt2  = sqrt(2.0);
      double sqrtl  = 20;
      double a = 1.0025 * exp (0.0625) * sqrt2;
      double startk = 1.0/(2.0 * sqrt2 * 400);
      double stopk = sqrtl/(2*sqrt2);
      
      for (k = startk; k <= stopk; k += 3.0) {
	double d = 1.0/(1 - exp ((-2.0/9.0)*(k*sqrt2*sqrtl + 1.5)));
	double f = a * d * exp (-0.5*k*k) / (k * sqrt (2.0 * 3.1415926));
	
	if (f <= accuracy/2.0)
	  break;
      }
      
      if (k > stopk) 
	k = stopk;
      
      right = (int) ceil(m2 + k*sqrt2*sqrtl + 1.5);
    }
    
    if (q_tmax >= 400.0) {
      // Find right using Corollary 1 using actual q_tmax 
      double sqrt2  = sqrt (2.0);
      double sqrtl  = sqrt (q_tmax);
      double a = (1.0 + 1.0/q_tmax) * exp (0.0625) * sqrt2;
      double startk = 1.0/(2.0 * sqrt2 * q_tmax);
      double stopk = sqrtl/(2*sqrt2);
      
      for (k = startk; k <= stopk; k += 3.0) {
	double d = 1.0/(1 - exp ((-2.0/9.0)*(k*sqrt2*sqrtl + 1.5)));
	double f = a * d * exp (-0.5*k*k) / (k * sqrt (2.0 * 3.1415926));
	
	if (f <= accuracy/2.0)
	  break;
      }
      
      if (k > stopk) 
	k = stopk;
      
      right = (int) ceil(m2 + k*sqrt2*sqrtl + 1.5);
    }

    if ((q_tmax < 400) && (right > 600)) {
      cerr << "Overflow: right truncation point > 600." << endl;
    }
    return right;
  }

  unsigned FoxGlynn::getRight(double lambda, double epsilon) {
    return getRight(lambda, DBL_MIN, DBL_MAX, epsilon);
  }
  
}
