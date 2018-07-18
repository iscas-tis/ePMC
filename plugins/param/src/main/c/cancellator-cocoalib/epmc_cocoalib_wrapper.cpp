#include <sstream>
#include <cassert>
#include "epmc_polynomial.hpp"
#include "CoCoA/library.H"

using namespace std;
using namespace CoCoA;

GlobalManager CoCoAFoundations;

template <typename T>
string to_string(const T& object) {
    ostringstream ss;
    ss << object;
    return ss.str();
}

void convert(RingElem &result, epmc_polynomial poly, const PolyRing& field) {
    assert(poly != NULL);
    int numTerms = epmc_polynomial_get_num_terms(poly);
    result = 0;
    const vector<RingElem>& parameters = indets(field);
    int numParams = epmc_polynomial_get_num_parameters(poly);
    for (int termNr = 0; termNr < numTerms; termNr++) {
        RingElem term(field);
        BigInt monomialInt(epmc_polynomial_get_coefficient(poly, termNr));
        RingElem monomial(field, monomialInt);
        term = monomial;
        for (int paramNr = 0; paramNr < numParams; paramNr++) {
            int exponentInt = epmc_polynomial_get_exponent(poly, paramNr, termNr);
            RingElem exponent = IndetPower(field, paramNr, exponentInt);
            term *= exponent;
        }
        result += term;
    }
}

void convert(epmc_polynomial result, RingElem &poly) {
    assert(result != NULL);
    int numParameters = epmc_polynomial_get_num_parameters(result);
    int numTerms = (int) NumTerms(poly);
    epmc_polynomial_resize(result, numParameters, numTerms);
    int termNr = 0;
    for (SparsePolyIter i=BeginIter(poly); !IsEnded(i); ++i) {
        RingElemAlias coefficient = coeff(i);
        string coefficientString = to_string(coefficient);
        epmc_polynomial_set_coefficient(result, termNr, coefficientString.c_str());
        ConstRefPPMonoidElem pp = PP(i);
        vector<long> exps;
        exponents(exps, pp);
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            int exponent = (int) exps[paramNr];
            epmc_polynomial_set_exponent(result, paramNr, termNr, exponent);
        }
        termNr++;
    }
}

void cancel(RingElem &poly1, RingElem &poly2) {
    RingElem gcdE = gcd(poly1, poly2);
    poly1 /= gcdE;
    poly2 /= gcdE;
}

extern "C" {
    __attribute__ ((visibility("default")))
    void epmc_cocoalib_cancel(epmc_polynomial poly1, epmc_polynomial poly2) {
        assert(poly1 != NULL);
        assert(poly2 != NULL);
        vector<symbol> symbols;
        int numParameters = epmc_polynomial_get_num_parameters(poly1);
        if (numParameters == 0) {
            numParameters = 1;
        }
        for (int i = 0; i < numParameters; i++) {
            symbols.push_back(symbol("x" + to_string(i)));
        }
        const PolyRing polyRing(NewPolyRing(RingZZ(), symbols));
        RingElem cocoa1(polyRing);
        RingElem cocoa2(polyRing);
        convert(cocoa1, poly1, polyRing);
        convert(cocoa2, poly2, polyRing);
        cancel(cocoa1, cocoa2);
        convert(poly1, cocoa1);
        convert(poly2, cocoa2);
    }
}
