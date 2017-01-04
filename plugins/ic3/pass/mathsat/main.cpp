/*
 *  * msat_api_example.c: A simple example of usage of the MathSAT API
 *   * author: Alberto Griggio <alberto.griggio@disi.unitn.it>
 *    *
 *     * to compile: gcc msat_api_example.c -o msat_api_example -I${MSAT_DIR}/include
 *      *             -L${MSAT_DIR}/lib -lmathsat -lgmp -lgmpxx -lstdc++
 *       */

#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include "mathsat.h"


static void example1();
static void example_interpolation();
static void example_interpolation2();

int main()
{
    printf("MathSAT API Examples\n");

    printf("Press RETURN to begin...\n");

    example1();

    printf("Press RETURN to continue...\n");

    example_interpolation();

    printf("Press RETURN to continue...\n");
    example_interpolation2();
    return 0;
}

/* 
 *  * this example shows how to generate an interpolant for the following pair of
 *   * formulas:
 *    * A = (f(x1) + x2 = x3) & (f(y1) + y2 = y3) & (y1 <= x1)
 *     * B = (x2 = g(b)) & (y2 = g(b)) & (x1 <= y1) & (x3 < y3)
 *      */
static void example_interpolation2()
{
    msat_env env;
    msat_term formula;
    const char *vars[] = {"i", "j","x1", "x2", "x3", "y1", "y2", "y3", "b"};
    const char *ufs[] = {"f", "g"};
    unsigned int i;
    int res, group_a, group_b;

    printf("\nInterpolation example\n");

    env = msat_create_env();
    /* enable interpolation support */
    res = msat_init_interpolation(env);
    assert(res == 0);
    /* enable the right theories and their combination with DTC. You must do
 *      * this before asserting formulas, otherwise you'll get wrong results! */
	res = msat_add_theory(env, MSAT_LRA);    
	//res = msat_add_theory(env, MSAT_IDL);
    
 //     res = msat_add_theory(env, MSAT_LRA);
//      res = msat_add_theory(env, MSAT_LIA);

    assert(res == 0);
    

    /* now create the interpolation groups representing the two formulas A and
 *      * B */
    group_a = msat_create_itp_group(env);
    group_b = msat_create_itp_group(env);

        /* declare variables/functions */
    for (i = 0; i < sizeof(vars)/sizeof(vars[0]); ++i) {
        msat_decl d = msat_declare_variable(env, vars[i], MSAT_INT);
        assert(!MSAT_ERROR_DECL(d));
    }
    for (i = 0; i < sizeof(ufs)/sizeof(ufs[0]); ++i) {
        msat_type tp[] = {MSAT_REAL};
        msat_decl d = msat_declare_uif(env, ufs[i], MSAT_INT, 1, tp);
        assert(!MSAT_ERROR_DECL(d));
    }


    assert(group_a != -1 && group_b != -1);
    
    /* create and assert formula A */
    formula = msat_from_string(
        env, "i > 2 & j = 3" /*"(f(x1) + x2 = x3) & (f(y1) + y2 = y3) & (y1 <= x1)"*/);
    assert(!MSAT_ERROR_TERM(formula));

    /* tell MathSAT that all subsequent formulas belong to group A */
    res = msat_set_itp_group(env, group_a);
    assert(res == 0);
    res = msat_assert_formula(env, formula);
    assert(res == 0);
    {
        char *s = msat_term_repr(formula);
        assert(s);
        printf("Asserted formula A (in group %d): %s\n", group_a, s);
        free(s);
    }

    /* create and assert formula B */
    formula = msat_from_string(
        env, "i < 3" /* (x2 = g(b)) & (y2 = g(b)) & (x1 <= y1) & (x3 < y3)"*/);
    assert(!MSAT_ERROR_TERM(formula));

    /* tell MathSAT that all subsequent formulas belong to group B */
    res = msat_set_itp_group(env, group_b);
    assert(res == 0);
    res = msat_assert_formula(env, formula);
    assert(res == 0);
    {
        char *s = msat_term_repr(formula);
        assert(s);
        printf("Asserted formula B (in group %d): %s\n", group_b, s);
        free(s);
    }

    if (msat_solve(env) == MSAT_UNSAT) {
        int groups_of_a[1];
        msat_term interpolant;
        char *s;
        groups_of_a[0] = group_a;
        interpolant = msat_get_interpolant(env, groups_of_a, 1);
        assert(!MSAT_ERROR_TERM(interpolant));
        s = msat_term_repr(interpolant);
        assert(s);
        printf("\nOK, the interpolant is: %s\n", s);
        free(s);
    } else {
        assert(0 && "should not happen!");
    }

    msat_destroy_env(env);

    printf("\nInterpolation example done\n");
}



static msat_term create_a_formula(msat_env env)
{
    const char *vn[] = {"v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8"};
    msat_decl vars[sizeof(vn)/sizeof(vn[0])];
    const char *fn[] = {"f", "h"};
    msat_decl funcs[sizeof(fn)/sizeof(fn[0])];
    unsigned int i;
    msat_term res, tmp;

    /* first, let's declare the variables and functions we are going to use */
    for (i = 0; i < sizeof(vn)/sizeof(vn[0]); ++i) {
        vars[i] = msat_declare_variable(env, vn[i], MSAT_REAL);
        assert(!MSAT_ERROR_DECL(vars[i]));
    }
    for (i = 0; i < sizeof(fn)/sizeof(fn[0]); ++i) {
        msat_type at[] = {MSAT_REAL};
        funcs[i] = msat_declare_uif(env, fn[i], MSAT_REAL, 1, at);
        assert(!MSAT_ERROR_DECL(funcs[i]));
    }

    /* we can create terms in two ways. The easiest one is by using
 *      * msat_from_string */
    res = msat_from_string(env,
                           "v3 = h(v0) & v4 = h(v1) & v6 = f(v2) & v7 = f(v5)");
    assert(!MSAT_ERROR_TERM(res));

    /* the other one is by using the various msat_make_* functions */
    tmp = msat_make_geq(env,
                        msat_make_variable(env, vars[0]),
                        msat_make_variable(env, vars[1]));
    res = msat_make_and(env, res, tmp);
    tmp = msat_make_not(env,
                        msat_make_equal(env,
                                        msat_make_variable(env, vars[6]),
                                        msat_make_variable(env, vars[7])));
    res = msat_make_and(env, res, tmp);
    tmp = msat_from_string(env, "v0 <= v1 & v2 = v3 - v4");
    res = msat_make_and(env, res, tmp);

    return res;
}


static void print_model(msat_env env)
{
    /* we use a model iterator to retrieve the model values for all the
 *      * variables, and the necessary function instantiations */
    msat_model_iterator iter = msat_create_model_iterator(env);
    assert(!MSAT_ERROR_MODEL_ITERATOR(iter));

    printf("Model:\n");
    while (msat_model_iterator_has_next(iter)) {
        msat_term t, v;
        char *s;
        msat_model_iterator_next(iter, &t, &v);
        s = msat_term_repr(t);
        assert(s);
        printf(" %s = ", s);
        free(s);
        s = msat_term_repr(v);
        assert(s);
        printf("%s\n", s);
        free(s);
    }
    msat_destroy_model_iterator(iter);
}


/* 
 *  * This example shows the basic usage of the API for creating formulas,
 *   * checking satisfiability, and using the solver incrementally
 *    */ 
static void example1()
{
    msat_env env;
    msat_term formula;
    msat_result status;
    int res;
    char *s;

    printf("\nExample 1\n");

    env = msat_create_env(); /* create an environment */
    /* initialize the theory solvers and their combination. You must do this
 *      * before asserting formulas in env, otherwise you'll get wrong results */
    msat_add_theory(env, MSAT_UF);
    msat_add_theory(env, MSAT_LRA);
    msat_set_theory_combination(env, MSAT_COMB_DTC);

    /* create a formula and assert it permanently */
    formula = create_a_formula(env);
    res = msat_assert_formula(env, formula);
    assert(res == 0);

    s = msat_term_repr(formula);
    assert(s);
    printf("Asserted: %s\n", s);
    free(s);

    /* incrementally add an assertion */
    res = msat_push_backtrack_point(env);
    assert(res == 0);
    formula = msat_from_string(env, "v5 = 0");
    assert(!MSAT_ERROR_TERM(formula));
    res = msat_assert_formula(env, formula);
    assert(res == 0);

    s = msat_term_repr(formula);
    assert(s);
    printf("Added constraint: %s\n", s);

    /* check satisfiability */
    status = msat_solve(env);
    assert(status == MSAT_UNSAT);

    printf("Environment is inconsistent, retracting constraint %s\n", s);

    /* retract the assertion and try again with another one */
    res = msat_pop_backtrack_point(env);
    assert(res == 0);
    res = msat_push_backtrack_point(env);
    assert(res == 0);
    formula = msat_from_string(env, "v5 = v8");
    assert(!MSAT_ERROR_TERM(formula));
    res = msat_assert_formula(env, formula);
    assert(res == 0);

    free(s);
    s = msat_term_repr(formula);
    assert(s);
    printf("Added constraint: %s\n", s);
    free(s);

    status = msat_solve(env);
    assert(status == MSAT_SAT);

    printf("Environment is now consistent, getting the model...\n");
    
    /* display the model */
    print_model(env);

    msat_destroy_env(env);

    printf("\nExample 1 done\n");
}


/* 
 *  * this example shows how to generate an interpolant for the following pair of
 *   * formulas:
 *    * A = (f(x1) + x2 = x3) & (f(y1) + y2 = y3) & (y1 <= x1)
 *     * B = (x2 = g(b)) & (y2 = g(b)) & (x1 <= y1) & (x3 < y3)
 *      */
static void example_interpolation()
{
    msat_env env;
    msat_term formula;
    const char *vars[] = {"x1", "x2", "x3", "y1", "y2", "y3", "b"};
    const char *ufs[] = {"f", "g"};
    unsigned int i;
    int res, group_a, group_b;

    printf("\nInterpolation example\n");

    env = msat_create_env();
    /* enable interpolation support */
    res = msat_init_interpolation(env);
    assert(res == 0);
    /* enable the right theories and their combination with DTC. You must do
 *      * this before asserting formulas, otherwise you'll get wrong results! */
    res = msat_add_theory(env, MSAT_LRA);
    assert(res == 0);
    
    /* declare variables/functions */
    for (i = 0; i < sizeof(vars)/sizeof(vars[0]); ++i) {
        msat_decl d = msat_declare_variable(env, vars[i], MSAT_REAL);
        assert(!MSAT_ERROR_DECL(d));
    }
    for (i = 0; i < sizeof(ufs)/sizeof(ufs[0]); ++i) {
        msat_type tp[] = {MSAT_REAL};
        msat_decl d = msat_declare_uif(env, ufs[i], MSAT_REAL, 1, tp);
        assert(!MSAT_ERROR_DECL(d));
    }

    /* now create the interpolation groups representing the two formulas A and
 *      * B */
    group_a = msat_create_itp_group(env);
    group_b = msat_create_itp_group(env);
    assert(group_a != -1 && group_b != -1);
    
    /* create and assert formula A */
    formula = msat_from_string(
        env, "x1 = 2" /*"(f(x1) + x2 = x3) & (f(y1) + y2 = y3) & (y1 <= x1)"*/);
    assert(!MSAT_ERROR_TERM(formula));

    /* tell MathSAT that all subsequent formulas belong to group A */
    res = msat_set_itp_group(env, group_a);
    assert(res == 0);
    res = msat_assert_formula(env, formula);
    assert(res == 0);
    {
        char *s = msat_term_repr(formula);
        assert(s);
        printf("Asserted formula A (in group %d): %s\n", group_a, s);
        free(s);
    }

    /* create and assert formula B */
    formula = msat_from_string(
        env, "x1 = 3" /* (x2 = g(b)) & (y2 = g(b)) & (x1 <= y1) & (x3 < y3)"*/);
    assert(!MSAT_ERROR_TERM(formula));

    /* tell MathSAT that all subsequent formulas belong to group B */
    res = msat_set_itp_group(env, group_b);
    assert(res == 0);
    res = msat_assert_formula(env, formula);
    assert(res == 0);
    {
        char *s = msat_term_repr(formula);
        assert(s);
        printf("Asserted formula B (in group %d): %s\n", group_b, s);
        free(s);
    }

    if (msat_solve(env) == MSAT_UNSAT) {
        int groups_of_a[1];
        msat_term interpolant;
        char *s;
        groups_of_a[0] = group_a;
        interpolant = msat_get_interpolant(env, groups_of_a, 1);
        assert(!MSAT_ERROR_TERM(interpolant));
        s = msat_term_repr(interpolant);
        assert(s);
        printf("\nOK, the interpolant is: %s\n", s);
        free(s);
    } else {
        assert(0 && "should not happen!");
    }

    msat_destroy_env(env);

    printf("\nInterpolation example done\n");
}

