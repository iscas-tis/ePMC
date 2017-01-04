#ifndef FOX_GLYNN_H
#define FOX_GLYNN_H

#include <vector>
#include <iostream>

namespace model_checker {
  class FoxGlynn {
  public:
    FoxGlynn(double lambda, double tau, double omega, double epsilon);
    FoxGlynn(double lambda, double epsilon);
    FoxGlynn(const FoxGlynn &);
    inline const std::vector<double> &getWeights() const {
      return weights;
    }
    inline unsigned getLeft() const {
      return left;
    }
    
    inline unsigned getRight() const {
      return right;
    }
    double getTotalWeight() const {
      return total_weight;
    }
    inline double operator[](unsigned index) const {
      if (index >= left) {
	return weights[index - left];
      } else {
	return 0.0;
      }
    }
    static unsigned getRight(double, double, double, double);
    static unsigned getRight(double, double);
  private:
    void init(double,double,double,double);
    std::vector<double> weights;
    unsigned left;
    unsigned right;
    double total_weight;
  };
}

#endif
