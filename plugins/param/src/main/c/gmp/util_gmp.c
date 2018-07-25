#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include <stdio.h>
#include "gmp.h"
#include "mpfr.h"

static int DOUBLE_MANTISSA_BITS = 53;

__attribute__ ((visibility("default")))
int gmp_util_size_of_mpq() {
    return sizeof(mpq_t);
}

__attribute__ ((visibility("default")))
int gmp_util_size_of_mpfr() {
    return sizeof(mpfr_t);
}

__attribute__ ((visibility("default")))
void gmp_util_free_string(char *str) {
    assert(str != NULL);
    free(str);
}

__attribute__ ((visibility("default")))
int gmp_util_mpq_get_int(mpq_t value) {
    assert(value != NULL);
    return mpz_get_si(mpq_numref(value));
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_set_int(mpq_t rop, int to) {
    assert(rop != NULL);
    mpq_set_si(rop, to, 1);
}

__attribute__ ((visibility("default")))
char *gmp_util_mpq_get_num(mpq_t value) {
    assert(value != NULL);
    return mpz_get_str(NULL, 10, mpq_numref(value));
}

__attribute__ ((visibility("default")))
char *gmp_util_mpq_get_den(mpq_t value) {
    assert(value != NULL);
    return mpz_get_str(NULL, 10, mpq_denref(value));
}

__attribute__ ((visibility("default")))
int gmp_util_mpq_is_zero(mpq_t value) {
    assert(value != NULL);
    if (mpz_cmp_si(mpq_numref(value), 0) != 0) {
        return 0;
    } else if (mpz_cmp_si(mpq_denref(value), 1) != 0) {
        return 0;
    }
    return 1;
}

__attribute__ ((visibility("default")))
int gmp_util_mpq_is_one(mpq_t value) {
    assert(value != NULL);
    if (mpz_cmp_si(mpq_numref(value), 1) != 0) {
        return 0;
    } else if (mpz_cmp_si(mpq_denref(value), 1) != 0) {
        return 0;
    }
    return 1;
}

__attribute__ ((visibility("default")))
int gmp_util_mpq_equals(mpq_t op1, mpq_t op2) {
    assert(op1 != NULL);
    assert(op2 != NULL);
    return mpq_equal(op1, op2);
}

__attribute__ ((visibility("default")))
void gmp_util_init_array(mpq_t rop, int size) {
    assert(rop != NULL);
    assert(size >= 0);
    for (int i = 0; i < size; i++) {
        mpq_init(rop + i);
    }
}

__attribute__ ((visibility("default")))
void gmp_util_clear_array(mpq_t rop, int size) {
    assert(rop != NULL);
    assert(size >= 0);
    for (int i = 0; i < size; i++) {
        mpq_clear(rop + i);
    }
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_array_set_int(mpq_t rop, int index, int to) {
    assert(rop != NULL);
    assert(index >= 0);
    assert(to >= 0);
    mpq_set_si(rop + index, to, 1);
}

__attribute__ ((visibility("default")))
int gmp_util_mpq_array_equals(mpq_t op1, mpq_t op2, int length) {
    assert(op1 != NULL);
    assert(op2 != NULL);
    assert(length >= 0);
    for (int index = 0; index < length; index++) {
        if (mpq_equal(op1 + index, op2 + index) == 0) {
            return 0;
        }
    }
    return 1;
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_array_set(mpq_t rop, int index, mpq_t to) {
    assert(rop != NULL);
    assert(index >= 0);
    assert(to != NULL);
    mpq_set(rop + index, to);
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_array_get(mpq_t rop, int index, mpq_t to) {
    assert(rop != NULL);
    assert(index >= 0);
    assert(to != NULL);
    mpq_set(to, rop + index);
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_to_double_interval(double *result, mpq_t op, mpfr_t buffer) {
    assert(result != NULL);
    assert(op != NULL);
    assert(buffer != NULL);
    assert(mpfr_get_prec(buffer) == DOUBLE_MANTISSA_BITS);
    double *left = result;
    double *right = result + 1;
    int dir = mpfr_set_q(buffer, op, MPFR_RNDD);
    *left = mpfr_get_d(buffer, MPFR_RNDD);
    if (dir == 0) {
        *right = *left;
    } else {
        // dir == -1
        *right = nextafter(*left, INFINITY);
    }
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_interval_to_double_interval(double *result, mpq_t op_left, mpq_t op_right, mpfr_t buffer) {
    assert(result != NULL);
    assert(op_left != NULL);
    assert(op_right != NULL);
    assert(buffer != NULL);
    assert(mpfr_get_prec(buffer) == DOUBLE_MANTISSA_BITS);
    double *left = result;
    double *right = result + 1;
    mpfr_set_q(buffer, op_left, MPFR_RNDD);
    *left = mpfr_get_d(buffer, MPFR_RNDD);
    mpfr_set_q(buffer, op_right, MPFR_RNDU);
    *right = mpfr_get_d(buffer, MPFR_RNDU);
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_pow(mpq_t result, mpq_t op1, int op2) {
    assert(result != NULL);
    assert(op1 != NULL);
    assert(op2 >= 0);
    mpz_pow_ui(mpq_numref(result), mpq_numref(op1), op2);
    mpz_pow_ui(mpq_denref(result), mpq_denref(op1), op2);
}

__attribute__ ((visibility("default")))
void gmp_util_mpq_max(mpq_t result, mpq_t op1, mpq_t op2) {
    assert(result != NULL);
    assert(op1 != NULL);
    assert(op2 != NULL);
    if (mpq_cmp(op1, op2) >= 0) {
        mpq_set(result, op1);
    } else {
        mpq_set(result, op2);
    }
}

__attribute__ ((visibility("default")))
double gmp_util_mpq_get_double(mpq_t op, mpfr_t buffer) {
    assert(op != NULL);
    assert(buffer != NULL);
    assert(mpfr_get_prec(buffer) == DOUBLE_MANTISSA_BITS);
    mpfr_set_q(buffer, op, MPFR_RNDN);
    return mpfr_get_d(buffer, MPFR_RNDN);
}

__attribute__ ((visibility("default")))
void gmp_util_init_mpfr(mpfr_t x, int prec) {
    mpfr_init2(x, (mpfr_prec_t) prec);
}
