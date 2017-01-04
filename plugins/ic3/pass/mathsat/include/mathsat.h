/* -*- C -*-
 * mathsat.h: C API for the MathSAT 4 solver
 * author: Alberto Griggio
 *
 * The API is (loosely) modeled after that of Yices
 */

/**
 * \file mathsat.h
 *
 * \brief API for the MathSAT SMT solver.
 *
 * Notice that at the moment the API offers only a subset of MathSAT
 * functionalities. In particular, none of the search-time parameters can be
 * controlled.
 */

#ifndef MATHSAT_H_INCLUDED
#define MATHSAT_H_INCLUDED

#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \name Data types and special values
 */

/*@{*/

/**
 * \brief MathSAT environment.
 */
typedef struct msat_env { void *repr; } msat_env;

/**
 * \brief MathSAT TCC environment.
 *
 * See the FMCAD'07 paper "Computing Predicate Abstractions by Integrating
 * BDDs and SMT Solvers" by R.Cavada, A.Cimatti, A.Franzen, K.Kalyanasundaram,
 * M.Roveri and R.K.Shyamasundar for details about a TCC solver.
 */
typedef struct msat_tcc_env { void *repr; } msat_tcc_env;

/**
 * \brief MathSAT term.
 *
 * A term is a variable, a number, an atom, or an arbitrary boolean
 * combination of those. It is the basic block of MathSAT abstract syntax
 * trees.
 */
typedef struct msat_term { void *repr; } msat_term;

/**
 * \brief MathSAT declaration.
 *
 * Declaration of variables and uninterpreted functions/predicates.
 */
typedef struct msat_decl { void *repr; } msat_decl;

/**
 * \brief MathSAT model iterator.
 *
 * An iterator object that lets you retrieve the value of variables and
 * uninterpreted function applications that make a formula evaluate to true.
 * Notice that sometimes MathSAT will not assign a model value to every
 * variable/function application in the formula: in this case, the missing
 * terms can be assigned any (legal) value.
 */ 
typedef struct msat_model_iterator { void *repr; } msat_model_iterator;

/**
 * \brief MathSAT data types.
 *
 * Basic data types supported by MathSAT.
 */
enum {
    MSAT_BOOL, /**< Boolean type. */
    MSAT_U,    /**< Uninterpreted type. This is implicitly treated as INT. */
    MSAT_INT,  /**< Integer type. */
    MSAT_REAL, /**< Real (rational) type. */
    MSAT_BV    /**< Bit-vector type class. */
};

typedef unsigned msat_type;

/**
 * \brief MathSAT theories.
 *
 * Basic theories supported by MathSAT. In addition to these, combinations of
 * MSAT_UF with any of the others is allowed.
 */ 
typedef enum {
    MSAT_UF,  /**< Equality and Uninterpreted Functions. */
    MSAT_IDL, /**< Integer Difference Logic. */
    MSAT_RDL, /**< Real Difference Logic. */
    MSAT_LIA, /**< Integer Linear Arithmetic. */
    MSAT_LRA, /**< Real Linear Arithmetic. */
    MSAT_WORD /**< Bit-vectors. */
} msat_theory;

/**
 * Available methods for handling combined theories.
 */
typedef enum {
    MSAT_COMB_NONE, /**< Do not use any theory combination (or pick the
                     * default combination for the enabled theories). */
    MSAT_COMB_ACK,  /**< Remove uninterpreted functions/predicates with
                     * Ackermann's expansion. */
    MSAT_COMB_DTC   /**< Use Delayed Theory Combination. */
} msat_theory_combination;

/**
 * MathSAT result.
 */ 
typedef enum {
    MSAT_UNKNOWN = -1, /**< Unknown. */
    MSAT_UNSAT,        /**< Unsatisfiable. */
    MSAT_SAT           /**< Satisfiable. */
} msat_result;

/**
 * MathSAT truth value.
 */
typedef enum {
    MSAT_UNDEF = -1,  /**< Undefined/unknown. */
    MSAT_FALSE,       /**< False. */
    MSAT_TRUE         /**< True. */
} msat_truth_value;


/**
 * \brief Callback function to be notified about models found by ::msat_all_sat
 *
 * This callback function can be used to be notified about the models found by
 * the AllSat algorithm. Such models contain the truth values of the important
 * atoms, as specified with ::msat_all_sat. Each term in the \a model array is
 * either an important atom or its negation, according to the truth value in
 * the model. Notice that the \a model array is read-only, and will be valid
 * only until the callback function returns.
 */
typedef void (*msat_all_sat_model_callback)(msat_term *model, int size,
                                            void *user_data);


#ifndef SWIG /* avoid exposing macros when wrapping with SWIG */

/**
 * \brief Error checking for terms
 *
 * Use this macro to check whether returned values of type ::msat_term are valid
 */
#define MSAT_ERROR_TERM(term) ((term).repr == NULL)

/**
 * \brief Sets given term to be an error term
 *
 * Use this macro to make terms error terms.
 */
#define MSAT_MAKE_ERROR_TERM(term) ((term).repr = NULL)

/**
 * \brief Error checking for declarations
 *
 * Use this macro to check whether returned values of type ::msat_decl are valid
 */
#define MSAT_ERROR_DECL(decl) ((decl).repr == NULL)

/**
 * \brief Error checking for model iterators
 *
 * Use this macro to check whether returned values of type
 * ::msat_model_iterator are valid
 */
#define MSAT_ERROR_MODEL_ITERATOR(iter) ((iter).repr == NULL)

#endif /* SWIG */


/**
 * \brief Gets the current MathSAT version.
 * \return A version string, with version information about MathSAT, the GMP
 *         library and the compiler used.
 */ 
const char *msat_get_version(void);

/**
 * \brief Sets the verbosity level of MathSAT (default: 0).
 */
void msat_set_verbosity(int level);


/*@}*/ /* end of datatypes and special values group */


/**
 * \name Environment creation
 */
/*@{*/

/**
 * \brief Creates a new MathSAT environment.
 * \return A new environment.
 */
msat_env msat_create_env(void);

/**
 * \brief Creates an environment that can share terms with its \a sibling.
 *
 * Usually, environments are completely independent from one another, but
 * sometimes it is useful to share terms between environments. This function
 * makes it possible to create special shared environments. Notice that shared
 * refers to the terms only: each environment has an independent set of
 * enabled theories (see ::msat_add_theory), and an independent assertion
 * stack.
 *
 * \param sibling The environment with which terms can be shared
 * \return A new environment.
 */
msat_env msat_create_shared_env(msat_env sibling);

/**
 * \brief Resets an environment.
 *
 * Clears the theories (see ::msat_add_theory) and the assertion stack (see
 * ::msat_assert_formula, ::msat_push_backtrack_point,
 * ::msat_pop_backtrack_point) of \a e. However, terms created in \a e are
 * still valid.
 * 
 * \param e The environment to reset
 */
void msat_reset_env(msat_env e);

/**
 * \brief Destroys an environment.
 * \param e The environment to destroy.
 */ 
void msat_destroy_env(msat_env e);

/**
 * \brief Sets an option in the given environment.
 *
 * Notice that the best thing to do is set options right after having created
 * an environment, before starting operating on it. The library tries to
 * capture and report errors, but it does not always succeed.
 *
 * \param e The environment in which to operate.
 * \param option The name of the option to set.
 * \param value The value for the option. For boolean options, use "true" or
 *        "false" (case matters). For flags, the value can be anything.
 * \return zero on success, nonzero on error.
 */
int msat_set_option(msat_env e, const char *option, const char *value);

/**
 * \brief Declares a new variable.
 * \param e The environment of the declaration.
 * \param name A name for the variable. It must be unique in the environment.
 * \param type The type of the variable.
 * \return A variable declaration, or a val s.t. ::MSAT_ERROR_DECL(val) is true
 *         in case of errors.
 */
msat_decl msat_declare_variable(msat_env e, const char *name, msat_type type);

/**
 * \brief Declares a new uninterpreted function (or predicate).
 * \param e The environment of the declaration.
 * \param name A name for the function. It must be unique in the environment.
 * \param out_type The return type of the function. If ::MSAT_BOOL is used, an
 *        uninterpreted predicate is delcared.
 * \param num_args Arity of the function
 * \param args_types Types of the arguments
 * \return A function declaration, or a val s.t. ::MSAT_ERROR_DECL(val) is true
 *         in case of errors.
 */
msat_decl msat_declare_uif(msat_env e, const char *name, msat_type out_type,
                           int num_args, msat_type *args_types);

/*@}*/

/**
 * \name Term creation
 */
/*@{*/

/**
 * \brief Returns a term representing logical truth.
 * \param e The environment of the definition
 * \return The term TRUE, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 * in case of errors.
 */
msat_term msat_make_true(msat_env e);

/**
 * \brief Returns a term representing logical falsity.
 * \param e The environment of the definition
 * \return The term FALSE, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_false(msat_env e);

/**
 * \brief Returns a term representing the equivalence of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BOOL.
 * \param t2 The second argument. Must have type ::MSAT_BOOL.
 * \return The term t1 <-> t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_iff(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the implication t1 -> t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BOOL.
  * \param t2 The second argument. Must have type ::MSAT_BOOL.
 * \return The term t1 -> t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_implies(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the logical OR of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BOOL.
 * \param t2 The second argument. Must have type ::MSAT_BOOL.
 * \return The term t1 | t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_or(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the logical XOR of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BOOL.
 * \param t2 The second argument. Must have type ::MSAT_BOOL.
 * \return The term t1 xor t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_xor(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the logical AND of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BOOL.
 * \param t2 The second argument. Must have type ::MSAT_BOOL.
 * \return The term t1 & t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_and(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the logical negation of t1.
 * \param e The environment of the definition
 * \param t1 The argument to negate. Must have type ::MSAT_BOOL.
 * \return The term !t1, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_not(msat_env e, msat_term t1);

/**
 * \brief Returns a term representing the equivalence of t1 and t2.
 *
 * If ::t1 and ::t2 have type ::MSAT_BOOL, this is equivalent to
 * ::make_iff(t1, t2). Otherwise, the atom (t1 = t2) is returned.
 * 
 * \param e The environment of the definition
 * \param t1 The first argument.
 * \param t2 The second argument.
 * \return The term (t1 = t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_equal(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an atom representing (t1 < t2).
 *
 * The arguments must have the same type. The exception is for integer
 * numbers, which can be casted to ::MSAT_REAL if necessary. 
 * 
 * \param e The environment of the definition
 * \param t1 The first argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \param t2 The second argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \return The term (t1 < t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_lt(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an atom representing (t1 > t2).
 *
 * The arguments must have the same type. The exception is for integer
 * numbers, which can be casted to ::MSAT_REAL if necessary. 
 * 
 * \param e The environment of the definition
 * \param t1 The first argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \param t2 The second argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \return The term (t1 > t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_gt(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an atom representing (t1 <= t2).
 *
 * The arguments must have the same type. The exception is for integer
 * numbers, which can be casted to ::MSAT_REAL if necessary. 
 * 
 * \param e The environment of the definition
 * \param t1 The first argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \param t2 The second argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \return The term (t1 <= t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_leq(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an atom representing (t1 >= t2).
 *
 * The arguments must have the same type. The exception is for integer
 * numbers, which can be casted to ::MSAT_REAL if necessary. 
 * 
 * \param e The environment of the definition
 * \param t1 The first argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \param t2 The second argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \return The term (t1 >= t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_geq(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an expression representing (t1 + t2).
 *
 * The arguments must have the same type. The exception is for integer
 * numbers, which can be casted to ::MSAT_REAL if necessary. 
 * 
 * \param e The environment of the definition
 * \param t1 The first argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \param t2 The second argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \return The term (t1 + t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_plus(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an expression representing (t1 - t2).
 *
 * The arguments must have the same type. The exception is for integer
 * numbers, which can be casted to ::MSAT_REAL if necessary. 
 * 
 * \param e The environment of the definition
 * \param t1 The first argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \param t2 The second argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \return The term (t1 - t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_minus(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an expression representing (t1 * t2).
 *
 * The arguments must have the same type, with the usual exception for integer
 * numbers. Moreover, at least one of them must be a number.
 * 
 * \param e The environment of the definition
 * \param t1 The first argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \param t2 The second argument. Must be of type ::MSAT_REAL or ::MSAT_INT
 * \return The term (t1 * t2), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_times(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns an expression representing (- t).
 *
 * \param e The environment of the definition
 * \param t The first argument to negate. Must be of type ::MSAT_REAL
 *          or ::MSAT_INT
 * \return The term (- t), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
  */
msat_term msat_make_negate(msat_env e, msat_term t);

/**
 * \brief Returns an expression representing a number.
 *
 * \param e The environment of the definition
 * \param num_rep A string representation for the number
 * 
 * \return The numeric term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 *
 * A bit-vector constant has the syntax
 * <tt>0[bdoh]</tt>width<tt>_[0-9a-fA-F]+</tt>
 */
msat_term msat_make_number(msat_env e, const char *num_rep);

/**
 * \brief Returns an expression representing an if-then-else construct.
 *
 * The two arguments ::tt and ::te must have compatible types.
 *
 * \param e The environment of the definition
 * \param c The condition of the test. Must be of type ::MSAT_BOOL
 * \param tt The "then" branch
 * \param te The "else" branch
 * 
 * \return The term representing the if-then-else, or a t s.t.
 *         ::MSAT_ERROR_TERM(t) is true in case of errors.
 */
msat_term msat_make_ite(msat_env e, msat_term c, msat_term tt, msat_term te);

/**
 * \brief Creates a variable from a declaration.
 * \param e The environment of the definition
 * \param var The declaration of the variable. Must come from the same
 *            environment
 * \return The term representing the variable, or a t s.t. ::MSAT_ERROR_TERM(t)
 *         is true in case of errors.
 */
msat_term msat_make_variable(msat_env e, msat_decl var);

/**
 * \brief Creates a function call.
 *
 * The number and type of the arguments must match those of the declaration.
 * 
 * \param e The environment of the definition
 * \param func The declaration of the function
 * \param args The actual parameters
 * \return The term representing the function/predicate call, or a t s.t.
 *         ::MSAT_ERROR_TERM(t) is true in case of errors. */
msat_term msat_make_uif(msat_env e, msat_decl func, msat_term args[]);

/**
 * \brief Returns a term representing the concatenation of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \return The term t1 :: t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_concat(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the selection of t[msb:lsb].
 * \param e The environment of the definition
 * \param t The first argument. Must have type ::MSAT_BV.
 * \param msb The most significant bit of the selection.
 * \param lsb The least significant bit of the selection.
 * \return The term t[msb:lsb], or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_select(msat_env e, msat_term t,
                              const unsigned msb, const unsigned lsb);

/**
 * \brief Returns a term representing the bit-wise OR of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 | t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_or(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the bit-wise XOR of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 xor t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_xor(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the bit-wise AND of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 \& t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_and(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the bit-wise negation of t.
 * \param e The environment of the definition
 * \param t The argument to negate. Must have type ::MSAT_BV.
 * \return The term !t, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_not(msat_env e, msat_term t);

/**
 * \brief Returns a term representing the logical left shift of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \a t1 and \a t2 must have the same width.
 * \return The term t1 << t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_lsl(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the logical right shift of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 >> t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_lsr(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the arithmetic right shift of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 >> t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_asr(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the zero extension of t.
 * \param e The environment of the definition
 * \param t The first argument. Must have type ::MSAT_BV.
 * \param width The width of the result
 * \return The term zext(t,width), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_zext(msat_env e, msat_term t, const unsigned width);

/**
 * \brief Returns a term representing the sign extension of t1 by amount.
 * \param e The environment of the definition
 * \param t The first argument. Must have type ::MSAT_BV.
 * \param width The width of the result
 * \return The term sext(t,width), or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_sext(msat_env e, msat_term t, const unsigned width);

/**
 * \brief Returns a term representing the addition of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 + t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_plus(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the subtraction of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 - t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_minus(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the multiplication of t1 and t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 * t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_times(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the unsigned division of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 / t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_udiv(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the unsigned remainder of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 % t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_urem(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the signed division of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 / t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_sdiv(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the signed remainder of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 % t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_srem(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the signed modulo of t1 by t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 mod t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_smod(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the unsigned t1 < t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 < t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_ult(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the unsigned t1 <= t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 <= t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_uleq(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the unsigned t1 > t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 > t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_ugt(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the unsigned t1 >= t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 >= t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_ugeq(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the signed t1 < t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 < t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_slt(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the signed t1 <= t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 <= t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_sleq(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the signed t1 > t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 > t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_sgt(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Returns a term representing the signed t1 >= t2.
 * \param e The environment of the definition
 * \param t1 The first argument. Must have type ::MSAT_BV.
 * \param t2 The second argument. Must have type ::MSAT_BV.
 * \pre \p t1 and \p t2 must have the same width.
 * \return The term t1 >= t2, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_bv_sgeq(msat_env e, msat_term t1, msat_term t2);

/**
 * \brief Creates a term from its string representation.
 *
 * The syntax of \a repr is that of the "FORMULA" sections of msat files. All
 * the variables and functions must have been previously declared in \a e.
 * The returned term is a "DEFINE" term, which has a name associated with it,
 * like a variable, and which can be retrieved with
 * ::msat_term_get_name. This is quite handy if you want to build a
 * complex term from simple ones using repeated calls of msat_from_string.
 *
 * \param e The environment of the definition
 * \param repr The string to parse
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_string(msat_env e, const char *repr);

/**
 * \brief Like ::msat_from_string, but give an explicit name to the returned
 *        term.
 *
 * \param e The environment of the definition
 * \param repr The string to parse
 * \param name The name to give to the returned term
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_string_and_name(msat_env e, const char *repr,
                                    const char *name);

/**
 * \brief Creates a term in \a e from an equivalent term \a t that was created
 *        in \a src.
 *
 * \param e The environment in which to create the term
 * \param t The term to copy
 * \param src The environment in which \a t was created
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_make_copy_from(msat_env e, msat_term t, msat_env src);

/**
 * \brief Creates a new term from \a t, in which the arguments are replaced
 *        with \a newargs.
 *
 * \param e The environment in which to create the term
 * \param t The "pattern" term
 * \param newargs The new arguments to pass to \a t
 * \pre The length of \a newargs should be equal to ::msat_term_arity(t)
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_replace_args(msat_env e, msat_term t, msat_term *newargs);

/**
 * \brief Creates a term from a string in msat format.
 *
 * \param e The environment in which to create the term.
 * \param data The string representation in msat format.
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_msat(msat_env e, const char *data);

/**
 * \brief Creates a term from a file in msat format.
 *
 * \param e The environment in which to create the term.
 * \param f The file in msat format.
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_msat_file(msat_env e, FILE *f);

/**
 * \brief Creates a term from a string in SMT-LIB format.
 *
 * \param e The environment in which to create the term.
 * \param data The string representation in SMT-LIB format.
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_smt(msat_env e, const char *data);

/**
 * \brief Creates a term from a file in SMT-LIB format.
 *
 * \param e The environment in which to create the term.
 * \param f The file in SMT-LIB format.
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_smt_file(msat_env e, FILE *f);

/**
 * \brief Creates a term from a string in FOCI format.
 *
 * \param e The environment in which to create the term.
 * \param data The string representation in FOCI format.
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_foci(msat_env e, const char *data);

/**
 * \brief Creates a term from a file in FOCI format.
 *
 * \param e The environment in which to create the term.
 * \param f The file in FOCI format.
 * \return The created term, or a t s.t. ::MSAT_ERROR_TERM(t) is true
 *         in case of errors.
 */
msat_term msat_from_foci_file(msat_env e, FILE *f);


/**
 * \brief Converts the given \a term to msat format
 *
 * \param e The environment in which \a term is defined
 * \param term The term to convert
 * \return a string in msat format for the formula represented by \a term,
 *         or NULL in case of errors. If not NULL, the returned string is
 *         allocated with malloc, and must be free'd by the user.
 */ 
char *msat_to_msat(msat_env e, msat_term term);

/**
 * \brief Converts the given \a term to SMT-LIB format
 *
 * \param e The environment in which \a term is defined
 * \param term The term to convert
 * \return a string in SMT-LIB format for the formula represented by \a term,
 *         or NULL in case of errors. If not NULL, the returned string is
 *         allocated with malloc, and must be free'd by the user.
 */ 
char *msat_to_smtlib(msat_env e, msat_term term);

/*@}*/ /* end of term creation group */

/**
 * \name Term access and navigation
 */
/*@{*/

/**
 * \brief Returns a numeric identifier for \a t
 *
 * The returned value is guaranteed to be unique within the environment in
 * which \a t was defined. Therefore, it can be used to test two terms for
 * equality, as well as a hash value.
 *
 * \param t A term. 
 * \return a unique (within the defining env) numeric identifier
 */
int msat_term_id(msat_term t);

/**
 * \brief Returns the arity of \a t
 * \param t A term. 
 * \return The number of arguments of \a t
 */
int msat_term_arity(msat_term t);

/**
 * \brief Returns the nth argument of \a t
 * \param t A term. 
 * \param n The index of the argument. Must be lower than the arity of \a t
 * \return The nth argument of arguments of \a t
 */
msat_term msat_term_get_arg(msat_term t, int n);

/**
 * \brief Returns the type of \a t
 * \param t A term. 
 * \return The type of \a t
 */
msat_type msat_term_get_type(msat_term t);

/**
 * \brief Checks whether \a t is the TRUE term
 * \param t A term. 
 * \return nonzero if \a t is TRUE
 */
int msat_term_is_true(msat_term t);

/**
 * \brief Checks whether \a t is the FALSE term
 * \param t A term. 
 * \return nonzero if \a t is FALSE
 */
int msat_term_is_false(msat_term t);

/**
 * \brief Checks whether \a t is a boolean variable
 * \param t A term. 
 * \return nonzero if \a t is a variable of type ::MSAT_BOOL
 */
int msat_term_is_boolean_var(msat_term t);

/**
 * \brief Checks whether \a t is an atom
 * \param t A term. 
 * \return nonzero if \a t is an atom, i.e. either a boolean variable or
 *         a relation between terms
 */
int msat_term_is_atom(msat_term t);

/**
 * \brief Checks whether \a t is a number
 * \param t A term. 
 * \return nonzero if \a t is a number
 */
int msat_term_is_number(msat_term t);

/**
 * \brief Checks whether \a t is an AND
 * \param t A term. 
 * \return nonzero if \a t is an AND
 */
int msat_term_is_and(msat_term t);

/**
 * \brief Checks whether \a t is an OR
 * \param t A term. 
 * \return nonzero if \a t is an OR
 */
int msat_term_is_or(msat_term t);

/**
 * \brief Checks whether \a t is a NOT
 * \param t A term. 
 * \return nonzero if \a t is a NOT
 */
int msat_term_is_not(msat_term t);

/**
 * \brief Checks whether \a t is an equivalence between boolean terms
 * \param t A term. 
 * \return nonzero if \a t is an IFF
 */
int msat_term_is_iff(msat_term t);

/**
 * \brief Checks whether \a t is an implication
 * \param t A term. 
 * \return nonzero if \a t is an implication
 */
int msat_term_is_implies(msat_term t);

/**
 * \brief Checks whether \a t is an XOR
 * \param t A term. 
 * \return nonzero if \a t is an XOR
 */
int msat_term_is_xor(msat_term t);

/**
 * \brief Checks whether \a t is a boolean if-then-else
 * \param t A term. 
 * \return nonzero if \a t is a boolean if-then-else
 */
int msat_term_is_bool_ite(msat_term t);

/**
 * \brief Checks whether \a t is a term if-then-else
 * \param t A term. 
 * \return nonzero if \a t is a term if-then-else
 */
int msat_term_is_term_ite(msat_term t);

/**
 * \brief Checks whether \a t is a variable
 * \param t A term. 
 * \return nonzero if \a t is a variable
 */
int msat_term_is_variable(msat_term t);

/**
 * \brief Checks whether \a t is an uninterpreted function call
 * \param t A term. 
 * \return nonzero if \a t is a uif call
 */
int msat_term_is_uif(msat_term t);

/**
 * \brief Checks whether \a t is an equality
 * \param t A term. 
 * \return nonzero if \a t is an equality atom
 */
int msat_term_is_equal(msat_term t);

/**
 * \brief Checks whether \a t is a (t1 < t2) atom
 * \param t A term. 
 * \return nonzero if \a t is a (t1 < t2) atom
 */
int msat_term_is_lt(msat_term t);

/**
 * \brief Checks whether \a t is a (t1 <= t2) atom
 * \param t A term. 
 * \return nonzero if \a t is a (t1 <= t2) atom
 */
int msat_term_is_leq(msat_term t);

/**
 * \brief Checks whether \a t is a (t1 > t2) atom
 * \param t A term. 
 * \return nonzero if \a t is a (t1 > t2) atom
 */
int msat_term_is_gt(msat_term t);

/**
 * \brief Checks whether \a t is a (t1 >= t2) atom
 * \param t A term. 
 * \return nonzero if \a t is a (t1 >= t2) atom
 */
int msat_term_is_geq(msat_term t);

/**
 * \brief Checks whether \a t is a (t1 + t2) expression
 * \param t A term. 
 * \return nonzero if \a t is a (t1 + t2) expression
 */
int msat_term_is_plus(msat_term t);

/**
 * \brief Checks whether \a t is a (t1 - t2) expression
 * \param t A term. 
 * \return nonzero if \a t is a (t1 - t2) expression
 */
int msat_term_is_minus(msat_term t);

/**
 * \brief Checks whether \a t is a (t1 * t2) expression
 * \param t A term. 
 * \return nonzero if \a t is a (t1 * t2) expression
 */
int msat_term_is_times(msat_term t);

/**
 * \brief Checks whether \a t is a (- t1) expression
 * \param t A term. 
 * \return nonzero if \a t is a (- t1) expression
 */
int msat_term_is_negate(msat_term t);

/**
 * \brief Checks whether \a t is a concatenation
 * \param t A term. 
 * \return nonzero if \a t is a concatenation
 */
int msat_term_is_bv_concat(msat_term t);

/**
 * \brief Checks whether \a t is a selection
 * \param t A term. 
 * \return nonzero if \a t is a selection
 */
int msat_term_is_bv_select(msat_term t);

/**
 * \brief Checks whether \a t is a bit-wise or
 * \param t A term. 
 * \return nonzero if \a t is a bit-wise or
 */
int msat_term_is_bv_or(msat_term t);

/**
 * \brief Checks whether \a t is a bit-wise xor
 * \param t A term. 
 * \return nonzero if \a t is a bit-wise xor
 */
int msat_term_is_bv_xor(msat_term t);

/**
 * \brief Checks whether \a t is a bit-wise and
 * \param t A term. 
 * \return nonzero if \a t is a bit-wise and
 */
int msat_term_is_bv_and(msat_term t);

/**
 * \brief Checks whether \a t is a bit-wise not
 * \param t A term. 
 * \return nonzero if \a t is a bit-wise not
 */
int msat_term_is_bv_not(msat_term t);

/**
 * \brief Checks whether \a t is a logical shift left
 * \param t A term. 
 * \return nonzero if \a t is a logical shift left
 */
int msat_term_is_bv_lsl(msat_term t);

/**
 * \brief Checks whether \a t is a logical shift right
 * \param t A term. 
 * \return nonzero if \a t is a logical shift right
 */
int msat_term_is_bv_lsr(msat_term t);

/**
 * \brief Checks whether \a t is an arithmetic shift right
 * \param t A term. 
 * \return nonzero if \a t is an arithmetic shift right
 */
int msat_term_is_bv_asr(msat_term t);

/**
 * \brief Checks whether \a t is a zero extension
 * \param t A term. 
 * \return nonzero if \a t is a zero enxtension
 */
int msat_term_is_bv_zext(msat_term t);

/**
 * \brief Checks whether \a t is a sign extension
 * \param t A term. 
 * \return nonzero if \a t is a sign extension
 */
int msat_term_is_bv_sext(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector addition
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector addition
 */
int msat_term_is_bv_plus(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector subtraction
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector subtraction
 */
int msat_term_is_bv_minus(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector multiplication
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector multiplication
 */
int msat_term_is_bv_times(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector unsigned division
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector unsigned division
 */
int msat_term_is_bv_udiv(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector unsigned remainder
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector unsigned remainder
 */
int msat_term_is_bv_urem(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector signed division
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector signed division
 */
int msat_term_is_bv_sdiv(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector signed remainder
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector signed remainder
 */
int msat_term_is_bv_srem(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector signed modulo
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector signed modulo
 */
int msat_term_is_bv_smod(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector unsigned <
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector unsigned <
 */
int msat_term_is_bv_ult(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector unsigned <=
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector unsigned <=
 */
int msat_term_is_bv_uleq(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector unsigned >
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector unsigned >
 */
int msat_term_is_bv_ugt(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector unsigned >=
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector unsigned >=
 */
int msat_term_is_bv_ugeq(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector signed <
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector signed <
 */
int msat_term_is_bv_slt(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector signed <=
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector signed <=
 */
int msat_term_is_bv_sleq(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector signed >
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector signed >
 */
int msat_term_is_bv_sgt(msat_term t);

/**
 * \brief Checks whether \a t is a bit-vector signed >=
 * \param t A term. 
 * \return nonzero if \a t is a bit-vector signed >=
 */
int msat_term_is_bv_sgeq(msat_term t);

/**
 * \brief Returns the declaration associated to \a t (if any)
 *
 * If \a t is not a variable or a function application, the returned value \a
 * ret will be s.t. MSAT_ERROR_DECL(ret) is true
 *
 * \param t The term for which to retrieve the declaration
 * 
 * \return If \a t is a variable, its declaration is returned; if it
 *         is an uif, the declaration of the function is returned; otherwise,
 *         a \a ret s.t. MSAT_ERROR_DECL(ret) is true is returned
 */
msat_decl msat_term_get_decl(msat_term t);

/**
 * \brief Returns the return type of the given declaration
 *
 * The return type for a variable is simply the variable's type, since
 * variables are considered functions of arity zero here.
 *
 * \param d A declaration
 *
 * \return The return type. In case of error, MSAT_U is returned.
 */
msat_type msat_decl_get_return_type(msat_decl d);

/**
 * \brief Returns the arity (number of arguments) of the given
 * declaration. For variables, zero is returned.
 *
 * \param d A declaration
 *
 * \return The arity of the declaration, or -1 on error.
 */
int msat_decl_get_arity(msat_decl d);

/**
 * \brief Returns the type of the given argument for the input declaration.
 *
 * \param d A declaration
 * \param n The index of the argument for which the type is needed
 *
 * \return The type of the given argument, or MSAT_U on error.
 */
msat_type msat_decl_get_arg_type(msat_decl d, int n);

/**
 * \brief Returns the name corresponding to the given declaration.
 *
 * \param d A declaration
 *
 * \return The name of the given declaration. The returned string is allocated
 *         with malloc(), and must be free()'d by the user. NULL is returned
 *         in case of error.
 */
char *msat_decl_get_name(msat_decl d);

/**
 * \brief Returns the name associated to the term \a t, if any.
 * \param e The environment in which \a t has been defined
 * \param t A term.
 * \return if \a t is a variable or a defined symbol (a term created with
 *         ::msat_from_string), its name, and NULL otherwise.
 */
char *msat_term_get_name(msat_env e, msat_term t);

/**
 * \brief Returns a string representation of a term
 *
 * The returned string is such that
 * ::msat_from_string(env, ::msat_term_repr(\a t)) == \a t, provided that
 * env is the environment in which \a t was defined
 *
 * \param t A term.
 * \return a string reprsentation of \a t, or NULL in case of errors. The
 *         string is allocated with malloc(), and must be free()'d by the
 *         caller.
 */
char *msat_term_repr(msat_term t);

/*@}*/ /* end of Term access and navigation group */

/**
 * \name Problem solving
 */

/*@{*/

/**
 * \brief Enables a specific theory.
 *
 * To check the satisfiability of a formula, the appropriate theory solver(s)
 * must be enabled. It is the resposibility of the caller to ensure that the
 * right solvers are enabled, otherwise the result of ::msat_solve can be
 * wrong. In particular, you must add theories <i><b>before</b></i> asserting
 * formulas (with ::msat_assert_formula).
 *
 * It is allowed to enable multiple theories: in this case, layering and
 * theory combination (see ::msat_set_theory_combination) will be used (the
 * order of invocation is the chronological one).
 *
 * \param e The environment in which to operate
 * \param t The theory to enable
 * \return zero on success, nonzero on error
 */
int msat_add_theory(msat_env e, msat_theory t);

/**
 * \brief Enables a specific combination method.
 *
 * This function must be invoked <i><b>before</b></i> asserting formulas (with
 * ::msat_assert_formula). If multiple theories are enabled (see
 * ::msat_add_theory) and no combination method is explicitly given, a default
 * value that is dependent on the enabled theories will be used.
 *
 * \param e The environment in which to operate
 * \param which The desired combination method
 * \return zero on success, nonzero on error
 */
int msat_set_theory_combination(msat_env e, msat_theory_combination which);

/**
 * \brief Pushes a checkpoint for backtracking in an environment
 *
 * \param e The environment in which to operate
 * \return zero on success, nonzero on error
 */
int msat_push_backtrack_point(msat_env e);

/**
 * \brief Backtracks to the last checkpoint set in the environment \a e
 *
 * \param e The environment in which to operate
 * \return zero on success, nonzero on error
 */
int msat_pop_backtrack_point(msat_env e);

/**
 * \brief Adds a logical formula to an environment
 * \param e The environment in which the formula is asserted
 * \param formula The formula to assert. Must have been created in \a e,
 *        otherwise bad things will happen (probably a crash)
 * \return zero on success, nonzero on error
 */
int msat_assert_formula(msat_env e, msat_term formula);

/**
 * \brief Checks the satiafiability of the given environment.
 *
 * Checks the satisfiability of the conjunction of all the formulas asserted
 * in \a e (see ::msat_assert_formula). Before calling this function, the
 * right theory solvers must have been enabled (see ::msat_add_theory).
 *
 * \param e The environment to check.
 * \return ::MSAT_SAT if the problem is satisfiable, ::MSAT_UNSAT if it is
 *         unsatisfiable, and ::MSAT_UNKNOWN if there was some error or if
 *         the satisfiability can't be determined.
 */
msat_result msat_solve(msat_env e);

/**
 * \brief Performs AllSat over the \a important atoms of the conjunction
 * of all formulas asserted in \a e (see ::msat_assert_formula).
 *
 * \param e The environment to use
 * \param important An array of important atoms. If NULL, all atoms are
 *                  considered important
 * \param num_important The size of the \a important array. If \a important is
 *                      NULL, set this to zero
 * \param func The callback function to be notified about models found (see
 *             ::msat_all_sat_model_callback). Can be NULL
 * \param user_data Generic data pointer which will be passed to \a func. Can
 *                  be anything, its value will not be interpreted
 * \return The number of models found, or -1 on error. If the formula has an
 *         infinite number of models, -2 is returned.
 */
int msat_all_sat(msat_env e, msat_term *important, int num_important,
                 msat_all_sat_model_callback func, void *user_data);

/**
 * \brief Returns the value of the term \a term in the current model
 *
 * If the last call to ::msat_solve returned a ::MSAT_SAT result, a model
 * for the formula is built. This function can be used to retrieve the model
 * values for variables and uninterpreted function instances. 
 *
 * \param e The environment in use
 * \param term The variable or function call of interest. If \a term is not a
 *             variable or an uninterpreted function call, the result is
 *             undefined (probably a crash).
 * \return The model value for \a term. If it can't be determined,
 *         ::msat_null_term is returned (this means that \a term can be
 *         assigned any legal value)
 */
msat_term msat_get_model_value(msat_env e, msat_term term);

/**
 * \brief Creates a model iterator
 * \param e The environment in use
 * \return an iterator for the current model
 */
msat_model_iterator msat_create_model_iterator(msat_env e);

/**
 * \brief Checks whether \a i can be incremented
 * \param i A model iterator
 * \return nonzero if \a i can be incremented, zero otherwise
 */
int msat_model_iterator_has_next(msat_model_iterator i);

/**
 * \brief Returns the next (term, value) pair in the model, and increments the
 *        given iterator.
 * \param i The model iterator to increment.
 * \param t Output value for the next variable/function call in the model.
 * \param v Output value for the next value in the model.
 * \return nonzero in case of error.
 */
int msat_model_iterator_next(msat_model_iterator i, msat_term *t, msat_term *v);

/**
 * \brief Destroys a model iterator.
 * \param i the iterator to destroy.
 */
void msat_destroy_model_iterator(msat_model_iterator i);

/**
 * \brief Retrieves the theory lemmas used in the last search (see
 *        ::msat_solve).
 * \param e The environment in which to operate.
 * \param out A pointer to an array of terms, one for each theory lemma. The
 *        array is allocated with malloc(). Must not be NULL.
 * \return The number of theory lemmas retrieved, or -1 on error.
 */
int msat_get_theory_lemmas(msat_env e, msat_term **out);

/**
 * \brief Returns the unsatisfiable core of the last search (see
 *        ::msat_solve), if the problem was unsatisfiable.
 *
 * Note that the unsat core returned is made of a subset of the clauses of the
 * CNF conversion of the input problem. Therefore, it might contain some new
 * boolean variables introduced by the CNF conversion algorithm.
 *
 * In order for this to work, interpolation support must be enabled in the
 * environment (by calling ::msat_init_interpolation).
 *        
 * \param e The environment in which to operate.
 * \return An unsatisfiable core of the problem, or a term t s.t.
 *         MSAT_ERROR_TERM(t) is true in case of errors.
 */
msat_term msat_get_unsat_core(msat_env e);


/*@}*/ /* end of Problem solving group */

/**
 * \name TCC interface
 */
/*@{*/

/**
 * \brief Creates a TCC environment that can share terms with the environment
 *        \a e.
 * 
 * \param e The environment which will provide terms to this TCC env.
 * \param non_chrono_backtracking If nonzero, allow non-chronological
 *        backtracking (a.k.a. backjumping) 
 * \return A new TCC environment.
 */
msat_tcc_env msat_create_tcc_env(msat_env e, int non_chrono_backtracking);

/**
 * \brief Destroys a TCC environment.
 * \param e The TCC environment to destroy.
 */ 
void msat_destroy_tcc_env(msat_tcc_env e);

/**
 * \brief Enables a specific theory in the given TCC env.
 *
 * See ::msat_add_theory for details. <b>WARNING: </b><i>at the moment,
 * combinations of theories are not supported.</i>
 *
 * \param e The TCC environment in which to operate.
 * \param t The theory to enable.
 * \return zero on success, nonzero on error.
 */
int msat_tcc_add_theory(msat_tcc_env e, msat_theory t);

/**
 * \brief Communicates an atom to the TCC environment \a e.
 *
 * Before adding atoms to a TCC env, you should activate the appropriate
 * theories with ::msat_tcc_add_theory.
 *
 * \param e The TCC environment.
 * \param atom A term, which must be an atom.
 * \return zero on success, nonzero on error (e.g. if \a atom is not an atom).
 */
int msat_tcc_add_constraint(msat_tcc_env e, msat_term atom);

/**
 * \brief Adds a formula to the TCC environment \a e.
 *
 * It is an error to add a formula to a TCC env that does not support
 * backjumping. Moreover, you should use either ::msat_tcc_add_constraint or
 * ::msat_tcc_assert_formula, but not both within the same TCC env. As for
 * ::msat_tcc_add_constraint, you should activate the appropriate theory with
 * ::msat_tcc_add_theory before calling this function
 *
 * \param e The TCC environment.
 * \param formula A term.
 * \return zero on success, nonzero on error.
 */
int msat_tcc_assert_formula(msat_tcc_env e, msat_term formula);

/**
 * \brief Adds an assumption to the TCC environment \a e.
 *
 * \param e The TCC env.
 * \param atom The theory atom to assume. Must be known to \a e (see
 *             ::msat_tcc_add_constraint).
 * \param positive if nonzero, the \a atom is assumed to be positive, otherwise
 *                 it is negated.
 */
void msat_tcc_assume(msat_tcc_env e, msat_term atom, int positive);

/**
 * \brief Retracts a previously assumed atom (see ::msat_tcc_assume).
 *
 * The TCC assumptions are stack-based: when an atom is retracted, all the
 * atoms assumed later are also retracted automatically.
 *
 * \param e The TCC env.
 * \param atom The atom to retract. Must have been assumed earlier (otherwise
 *             bad things will happen, probably a segfault).
 */
void msat_tcc_undo(msat_tcc_env e, msat_term atom);

/**
 * \brief Retracts all assumed atoms (see ::msat_tcc_undo).
 *
 * \param e The TCC env.
 */
void msat_tcc_undo_all(msat_tcc_env e);

/**
 * \brief Checks whether the current assumptions in \a e are consistent.
 *
 * If \a approx is nonzero, the check is incomplete (i.e. MSAT_SAT can be
 * returned even if there is an inconsistency). In case of inconsistency,
 * \a *to_undo will be set to the atom that must be undone.
 *
 * \param e The TCC env to check.
 * \param approx If true, use an incomplete (but faster) check.
 * \param to_undo In case of inconsistency, stores the atom to undo to restore
 *                consistency.
 * \return ::MSAT_UNSAT if the TCC env is known to be inconsistent, ::MSAT_SAT
 *         otherwise.
 */ 
msat_result msat_tcc_check(msat_tcc_env e, int approx, msat_term *to_undo);

/**
 * \brief Returns the truth value of an atom in the given TCC env.
 *
 * \param e The TCC env.
 * \param atom The atom to get the value of. Must be known to the TCC env (see
 *             ::msat_tcc_add_constraint).
 * \return the ::msat_truth_value of \a atom.
 */
msat_truth_value msat_tcc_get_value(msat_tcc_env e, msat_term atom);

/**
 * \brief Computes a reason for an assignment that was inferred
 *        by the TCC.
 *
 * \param e The TCC env.
 * \param atom The atom to get the reason for. This atom must
 *             have a truth value in \a e, and it may not
 *             have been assumed using ::msat_tcc_assume by
 *             the user.
 * \param reason The reason for \a atom being assigned.
 *               This is defined as the last assumption that
 *               needs to be undone such that the TCC can no
 *               longer infer a truth assignment to \a atom.
 * \return zero on success, nonzero on error (e.g. if \a atom
 *         does not have an inferred truth value.)
 */
int msat_tcc_get_reason(msat_tcc_env e, msat_term atom, msat_term *reason);


/**
 * \brief Checks whether there is any literal whose value is implied by the
 *        current assumptions (see ::msat_tcc_assume).
 *
 * \param e The TCC env.
 * \return The number of implied literals, or -1 on error.
 */ 
int msat_tcc_has_implied(msat_tcc_env e);

/**
 * \brief Gets the list of literals implied by the current assumptions (see
 *        ::msat_tcc_assume).
 *
 * \param e The TCC env.
 * \param implied An array to store the implied literals. If not NULL, it is
 *        assumed that it is long enough to contain all the values (see
 *        ::msat_tcc_has_implied). 
 * \return The number of implied literals, or -1 on error.
 */
int msat_tcc_get_implied(msat_tcc_env e, msat_term *implied);

/**
 * \brief Checks the satiafiability of the given TCC environment, under
 *        the currently active assumptions.
 *
 * Checks the satisfiability of the conjunction of all the formulas asserted
 * in \a e (see ::msat_tcc_assert_formula), under all the currently active
 * assumptions (see ::msat_tcc_assume). In case of inconsistency,
 * \a *to_undo will be set to the atom that must be undone.
 *
 * \param e The TCC environment to check.
 * \param to_undo In case of inconsistency, stores the atom to undo to restore
 *                consistency.
 * \return ::MSAT_SAT if the problem is satisfiable, ::MSAT_UNSAT if it is
 *         unsatisfiable, and ::MSAT_UNKNOWN if there was some error or if
 *         the satisfiability can't be determined.
 */
msat_result msat_tcc_solve(msat_tcc_env e, msat_term *to_undo);

/**
 * \brief Performs AllSat over the \a important atoms of the conjunction
 * of all formulas asserted in the TCC environment \a e
 * (see ::msat_tcc_assert_formula), under the currently active assumptions.
 *
 * \param e The TCC environment to use
 * \param important An array of important atoms. If NULL, all atoms are
 *                  considered important
 * \param num_important The size of the \a important array. If \a important is
 *                      NULL, set this to zero
 * \param func The callback function to be notified about models found (see
 *             ::msat_all_sat_model_callback). Can be NULL
 * \param user_data Generic data pointer which will be passed to \a func. Can
 *                  be anything, its value will not be interpreted
 * \return The number of models found, or -1 on error. If the formula has an
 *         infinite number of models, -2 is returned.
 */
int msat_tcc_all_sat(msat_tcc_env e, msat_term *important, int num_important,
                     msat_all_sat_model_callback func, void *user_data);



/*@}*/ /* end of TCC interface group */

/**
 * \name Interpolation
 */
/*@{*/

/**
 * \brief Enable Craig interpolation support for the given environment.
 *
 * This function must be called <b><i>before</i></b> asserting formulas with
 * ::msat_assert_formula.
 *
 * \param e The environment in which to operate.
 * \return zero on success, nonzero on failure.
 */
int msat_init_interpolation(msat_env e);

/**
 * \brief Creates a new group for interpolation.
 *
 * When computing an interpolant, formulas are organized into several groups,
 * which are partitioned into two sets GA and GB. The conjuction of formulas
 * in GA will play the role of A, and that of formulas in GB will play the
 * role of B (see ::msat_set_itp_group, ::msat_get_interpolant).
 *
 * \param e The environment in which to operate.
 * \return an identifier for the new group, or -1 in case of error.
 */
int msat_create_itp_group(msat_env e);

/**
 * \brief Sets the current interpolation group.
 *
 * All the formulas asserted after this call (with ::msat_assert_formula) will
 * belong to \a group.
 * <b>WARNING:</b> Do not use the same interpolation group for formulas asserted
 * at different backtrack points! It is not reliable, and can lead to incorrect
 * results.
 *
 * \param e The environment in which to operate.
 * \param group The group. Must have been previously created with
 *        ::msat_create_itp_group.
 * \return zero on success, nonzero on error.
 */ 
int msat_set_itp_group(msat_env e, int group);

/**
 * \brief Computes an interpolant for a pair (A, B) of formulas.
 *
 * A is the conjucntion of all the assumed formulas in the \a groups_of_a
 * groups (see ::msat_create_itp_group), and B is the rest of assumed
 * formulas.
 *
 * This function must be called only after ::msat_solve, and only if
 * MSAT_UNSAT was returned.
 *
 * \param e The environment in which to operate.
 * \param groups_of_a An array of group identifiers.
 * \param n The size of the \a groups_of_a array.
 * \return The interpolating term, or a t s.t. MSAT_ERROR_TERM(t) is true in
 *         case of errors.
 */
msat_term msat_get_interpolant(msat_env e, int *groups_of_a, size_t n);

/*@}*/ /* end of interpolation group */

#ifdef __cplusplus
} /* end of extern "C" */
#endif

#endif /* MATHSAT_H_INCLUDED */
