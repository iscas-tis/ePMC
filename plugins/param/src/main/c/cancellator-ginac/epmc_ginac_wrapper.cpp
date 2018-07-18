#include <sstream>
#include <cassert>
#include <string>
#include <ginac/ginac.h>
#include "epmc_polynomial.hpp"

using namespace GiNaC;
using namespace std;

struct CompCf {
  vector<symbol> *symbols;
  bool operator()(const ex &poly1, const ex &poly2) {
      for (unsigned i(0); i < symbols->size(); i++) {
          const int degree1(degree(poly1, (*symbols)[i]));
          const int degree2(degree(poly2, (*symbols)[i]));
          if (degree1 < degree2) {
              return false;
          } else if (degree1 > degree2) {
              return true;
          }
      }
      return true;
  }
};

void cancel(ex &poly1, ex &poly2) {
  ex result(numer_denom(poly1 / poly2));
  poly1 = expand(result[0]);
  poly2 = expand(result[1]);
}

vector<GiNaC::symbol> ginacSymbols;

void convert(ex &result, epmc_polynomial poly) {
    while (ginacSymbols.size() < epmc_polynomial_get_num_parameters(poly)) {
        ginacSymbols.push_back(symbol("x" + std::to_string(ginacSymbols.size())));
    }

    result = 0;
    for (unsigned termNr(0); termNr < epmc_polynomial_get_num_terms(poly); termNr++) {
      const char *coeff(epmc_polynomial_get_coefficient(poly, termNr));
      lst asdf;
      ex monom(coeff, asdf);
      for (unsigned symNr(0); symNr < epmc_polynomial_get_num_parameters(poly); symNr++) {
        monom *= pow((ginacSymbols)[symNr],
                epmc_polynomial_get_exponent(poly, symNr, termNr));
      }
      result += monom;
    }
}

void convert(epmc_polynomial poly, const ex &exP) {
    const unsigned numSymbols(ginacSymbols.size());
    vector<ex> toSort;
    if (!is_a<add>(exP)) {
        toSort.push_back(exP);
    } else {
        for (const_iterator i = exP.begin(); i != exP.end(); i++) {
            toSort.push_back(*i);
        }
    }
	
    struct CompCf compCf;
    compCf.symbols = &ginacSymbols;
    // TODO sort leads to crash for some reason
 //   sort(toSort.begin(), toSort.end(), compCf);
    epmc_polynomial_resize(poly, numSymbols, toSort.size());
    for (unsigned termNr(0); termNr < epmc_polynomial_get_num_terms(poly); termNr++) {
        for (unsigned symNr(0); symNr < numSymbols; symNr++) {
            const int degr(degree(toSort[termNr], ginacSymbols[symNr]));
            epmc_polynomial_set_exponent(poly, symNr, termNr, degr);
        }
        ex coefff(1);
        if (is_a<numeric>(toSort[termNr])) {
            coefff = toSort[termNr];
        } else if (is_a<mul>(toSort[termNr])) {
            for (const_iterator i = toSort[termNr].begin(); i != toSort[termNr].end(); i++) {
                if (is_a<numeric>(*i)) {
                    coefff = *i;
                }
            }
        }
        stringstream sstream;
        sstream << coefff;
        string coeffStr = sstream.str();
        epmc_polynomial_set_coefficient(poly, termNr, coeffStr.c_str());
    }
}

extern "C" {
    __attribute__ ((visibility("default")))
    void epmc_ginac_cancel(epmc_polynomial poly1, epmc_polynomial poly2) {
      assert(poly1 != NULL);
      assert(poly2 != NULL);
      ex ginac1;
      ex ginac2;
      convert(ginac1, poly1);
      convert(ginac2, poly2);
      cancel(ginac1, ginac2);
      convert(poly1, ginac1);
      convert(poly2, ginac2);
    }
}
