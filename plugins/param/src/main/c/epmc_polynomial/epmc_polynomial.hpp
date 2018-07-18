#ifndef EPMC_polynomial_H
#define EPMC_polynomial_H

extern "C" {
    typedef struct epmc_polynomial_struct *epmc_polynomial;

    epmc_polynomial epmc_polynomial_new(int num_parameters, int num_terms);

    void epmc_polynomial_resize(epmc_polynomial polynomial, int num_parameters, int num_terms);

    int epmc_polynomial_get_num_parameters(epmc_polynomial polynomial);

    int epmc_polynomial_get_num_terms(epmc_polynomial polynomial);

    void epmc_polynomial_delete(epmc_polynomial polynomial);

    const char *epmc_polynomial_get_coefficient(epmc_polynomial polynomial, int term_nr);

    int epmc_polynomial_get_exponent(epmc_polynomial polynomial, int param_nr, int term_nr);

    void epmc_polynomial_set_coefficient(epmc_polynomial polynomial, int term_nr, const char *coefficient);

    void epmc_polynomial_set_exponent(epmc_polynomial polynomial, int param_nr, int term_nr, int exponent);
}

#endif
