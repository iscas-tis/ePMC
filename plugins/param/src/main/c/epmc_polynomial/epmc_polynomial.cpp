#include <sstream>
#include <cassert>
#include <cstring>
#include "epmc_polynomial.hpp"

// #include "epmc_polynomial_sort.hpp"

using namespace std;

extern "C" {
    struct epmc_polynomial_struct {
        int numParameters;
        int numTerms;
        int *monomials;
        char **coefficients;
    };

    __attribute__ ((visibility("default")))
    epmc_polynomial epmc_polynomial_new(int numParameters, int numTerms) {
        assert(numParameters >= 0);
        assert(numTerms >= 0);
        epmc_polynomial result = new epmc_polynomial_struct();
        result->numParameters = numParameters;
        result->numTerms = numTerms;
        result->coefficients = new char*[numTerms]();
        result->monomials = new int[numParameters * numTerms]();
        return result;
    }

    __attribute__ ((visibility("default")))
    void epmc_polynomial_resize(epmc_polynomial polynomial, int numParameters, int numTerms) {
        assert(polynomial != NULL);
        assert(numParameters >= 0);
        assert(numTerms >= 0);
        polynomial->numParameters = numParameters;
        polynomial->numTerms = numTerms;
        delete polynomial->coefficients;
        polynomial->coefficients = new char*[numTerms]();
        delete polynomial->monomials;
        polynomial->monomials = new int[numParameters * numTerms]();
    }

    __attribute__ ((visibility("default")))
    int epmc_polynomial_get_num_parameters(epmc_polynomial polynomial) {
        assert(polynomial != NULL);
        return polynomial->numParameters;
    }

    __attribute__ ((visibility("default")))
    int epmc_polynomial_get_num_terms(epmc_polynomial polynomial) {
        assert(polynomial != NULL);
        return polynomial->numTerms;
    }

    __attribute__ ((visibility("default")))
    void epmc_polynomial_delete(epmc_polynomial polynomial) {
        assert(polynomial != NULL);
        delete polynomial->monomials;
        for (int termNr = 0; termNr < polynomial->numTerms; termNr++) {
            delete polynomial->coefficients[termNr];
        }
        delete polynomial->coefficients;
        delete polynomial;
    }

    __attribute__ ((visibility("default")))
    const char *epmc_polynomial_get_coefficient(epmc_polynomial polynomial, int termNr) {
        assert(polynomial != NULL);
        assert(termNr >= 0);
        assert(termNr < polynomial->numTerms);
        return polynomial->coefficients[termNr];
    }

    __attribute__ ((visibility("default")))
    int epmc_polynomial_get_exponent(epmc_polynomial polynomial, int paramNr, int termNr) {
        assert(polynomial != NULL);
        assert(paramNr >= 0);
        assert(paramNr < polynomial->numParameters);
        assert(termNr >= 0);
        assert(termNr < polynomial->numTerms);
        return polynomial->monomials[polynomial->numParameters * termNr + paramNr];
    }

    __attribute__ ((visibility("default")))
    void epmc_polynomial_set_coefficient(epmc_polynomial polynomial, int termNr, const char *coefficient) {
        assert(polynomial != NULL);
        assert(termNr >= 0);
        assert(termNr < polynomial->numTerms);
        assert(coefficient != NULL);
        char *copy = new char[strlen(coefficient) + 1];
        strcpy(copy, coefficient);
        delete polynomial->coefficients[termNr];
        polynomial->coefficients[termNr] = copy;
    }

    __attribute__ ((visibility("default")))
    void epmc_polynomial_set_exponent(epmc_polynomial polynomial, int paramNr, int termNr, int exponent) {
        assert(polynomial != NULL);
        assert(paramNr >= 0);
        assert(paramNr < polynomial->numParameters);
        assert(termNr >= 0);
        assert(termNr < polynomial->numTerms);
        assert(exponent >= 0);
        polynomial->monomials[polynomial->numParameters * termNr + paramNr] = exponent;
    }

    __attribute__ ((visibility("default")))
    bool epmc_polynomial_is_zero(epmc_polynomial poly) {
        assert(poly != NULL);
        return poly->numTerms == 0;
    }

    __attribute__ ((visibility("default")))
    bool epmc_polynomial_is_constant(epmc_polynomial poly) {
        assert(poly != NULL);
        if (poly->numTerms == 0) {
            return true;
        }
        if (poly->numTerms > 1) {
            return false;
        }
        for (int symbolNr = 0; symbolNr < poly->numParameters; symbolNr++) {
            if (poly->monomials[symbolNr] != 0) {
                return false;
            }
        }
        return true;
    }

    __attribute__ ((visibility("default")))
    bool epmc_polynomial_equals(epmc_polynomial poly1, epmc_polynomial poly2) {
        assert(poly1 != NULL);
        assert(poly2 != NULL);
        if (poly1->numParameters != poly2->numParameters) {
            return false;
        }
        if (poly1->numTerms != poly2->numTerms) {
            return false;
        }
        for (int termNr = 0; termNr < poly1->numTerms; termNr++) {
            if (strcmp(poly1->coefficients[termNr], poly2->coefficients[termNr]) != 0) {
                return false;
            }
            for (int paramNr = 0; paramNr < poly1->numParameters; paramNr++) {
                if (poly1->monomials[poly1->numParameters * termNr + paramNr]
                 != poly2->monomials[poly2->numParameters * termNr + paramNr]) {
                    return false;
                }
            }
        }
        return true;
    }

    __attribute__ ((visibility("default")))
    void epmc_polynomial_set_to_constant(epmc_polynomial poly, int constant) {
        assert(poly != NULL);
        if (constant == 0) {
            poly->numTerms = 0;
            poly->monomials = new int[0];
            poly->coefficients = new char*[0];
        } else {
            poly->numTerms = constant;
            poly->monomials = new int[poly->numParameters];
            for (int paramNr = 0; paramNr < poly->numParameters; paramNr++) {
                poly->monomials[paramNr] = 0;
            }
            poly->coefficients = new char*[1];
            string constantString = to_string(constant);
            char *constantCString = new char[constantString.length() + 1];
            strcpy(constantCString, constantString.c_str());
            poly->coefficients[0] = constantCString;
        }
    }
}
