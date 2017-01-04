/*****************************************************************************/
/*!
 * \file kinds.h
 *
 * Author: Clark Barrett
 *
 * Created: Mon Jan 20 13:38:52 2003
 *
 * <hr>
 *
 * License to use, copy, modify, sell and/or distribute this software
 * and its documentation for any purpose is hereby granted without
 * royalty, subject to the terms and conditions defined in the \ref
 * LICENSE file provided with this distribution.
 *
 * <hr>
 *
 */
/*****************************************************************************/

#ifndef _cvc3__include__kinds_h_
#define _cvc3__include__kinds_h_

namespace CVC3 {

  // The commonly used kinds and the kinds needed by the parser.  All
  // these kinds are registered by the ExprManager and are readily
  // available for everyone else.
typedef enum {
  NULL_KIND = 0,

  // Constant (Leaf) Exprs
  TRUE_EXPR = 1,
  FALSE_EXPR = 2,
  RATIONAL_EXPR = 3,
  STRING_EXPR = 4,

  // All constants should have kinds less than MAX_CONST
  MAX_CONST = 100,

  // Generic LISP kinds for representing raw parsed expressions
  RAW_LIST, //!< May have any number of children >= 0
  //! Identifier is (ID (STRING_EXPR "name"))
  ID,
  // Types
  BOOLEAN,
//   TUPLE_TYPE,
  ANY_TYPE,
  ARROW,
  // The "type" of any expression type (as in BOOLEAN : TYPE).
  TYPE,
  // Declaration of new (uninterpreted) types: T1, T2, ... : TYPE
  // (TYPEDECL T1 T2 ...)
  TYPEDECL,
  // Declaration of a defined type T : TYPE = type === (TYPEDEF T type)
  TYPEDEF,

  // Equality
  EQ,
  NEQ,
  DISTINCT,

  // Propositional connectives
  NOT,
  AND,
  OR,
  XOR,
  IFF,
  IMPLIES,
  //  BOOL_VAR, //!< Boolean variables are treated as 0-ary predicates

  // Propositional relations (for circuit propagation)
  AND_R,
  IFF_R,
  ITE_R,

  // (ITE c e1 e2) == IF c THEN e1 ELSE e2 ENDIF, the internal
  // representation of the conditional.  Parser produces (IF ...).
  ITE,

  // Quantifiers
  FORALL,
  EXISTS,

  // Uninterpreted function
  UFUNC,
  // Application of a function
  APPLY,

  // Top-level Commands
  ASSERT,
  QUERY,
  CHECKSAT,
  CONTINUE,
  RESTART,
  DBG,
  TRACE,
  UNTRACE,
  OPTION,
  HELP,
  TRANSFORM,
  PRINT,
  CALL,
  ECHO,
  INCLUDE,
  DUMP_PROOF,
  DUMP_ASSUMPTIONS,
  DUMP_SIG,
  DUMP_TCC,
  DUMP_TCC_ASSUMPTIONS,
  DUMP_TCC_PROOF,
  DUMP_CLOSURE,
  DUMP_CLOSURE_PROOF,
  WHERE,
  ASSERTIONS,
  ASSUMPTIONS,
  COUNTEREXAMPLE,
  COUNTERMODEL,
  PUSH,
  POP,
  POPTO,
  PUSH_SCOPE,
  POP_SCOPE,
  POPTO_SCOPE,
  RESET,
  CONTEXT,
  FORGET,
  GET_TYPE,
  CHECK_TYPE,
  GET_CHILD,
  SUBSTITUTE,
  SEQ,
  ARITH_VAR_ORDER,

  // Kinds used mostly in the parser

  TCC,
  // Variable declaration (VARDECL v1 v2 ... v_n type).  A variable
  // can be an ID or a BOUNDVAR.
  VARDECL,
  // A list of variable declarations (VARDECLS (VARDECL ...) (VARDECL ...) ...)
  VARDECLS,

  // Bound variables have a "printable name", the one the user typed
  // in, and a uniqueID used to distinguish it from other bound
  // variables, which is effectively the alpha-renaming:

  // Op(BOUND_VAR (BOUND_ID "user_name" "uniqueID")).  Note that
  // BOUND_VAR is an operator (Expr without children), just as UFUNC
  // and UCONST.

  // The uniqueID normally is just a number, so one can print a bound
  // variable X as X_17.

  // NOTE that in the parsed expressions like LET x: T = e IN foo(x),
  // the second instance of 'x' will be an ID, and *not* a BOUNDVAR.
  // The parser does not know how to resolve bound variables, and it
  // has to be resolved later.
  BOUND_VAR,
  BOUND_ID,

  // Updator "e1 WITH <bunch of stuff> := e2" is represented as
  // (UPDATE e1 (UPDATE_SELECT <bunch of stuff>) e2), where <bunch
  // of stuff> is the list of accessors:
  // (READ idx)
  // ID (what's that for?)
  // (REC_SELECT ID)
  // and (TUPLE_SELECT num).
//   UPDATE,
//   UPDATE_SELECT,
  // Record type [# f1 : t1, f2 : t2 ... #] is represented as
  // (RECORD_TYPE (f1 t1) (f2 t2) ... )
//   RECORD_TYPE,
//   // (# f1=e1, f2=e2, ...#) == (RECORD (f1 e1) ...)
//   RECORD,
//   RECORD_SELECT,
//   RECORD_UPDATE,

//   // (e1, e2, ...) == (TUPLE e1 e2 ...)
//   TUPLE,
//   TUPLE_SELECT,
//   TUPLE_UPDATE,

//   SUBRANGE,
  // Enumerated type (SCALARTYPE v1 v2 ...)
//   SCALARTYPE,
  // Predicate subtype: the argument is the predicate (lambda-expression)
  SUBTYPE,
  // Datatype is Expr(DATATYPE, Constructors), where Constructors is a
  // vector of Expr(CONSTRUCTOR, id [ , arg ]), where 'id' is an ID,
  // and 'arg' a VARDECL node (list of variable declarations with
  // types).  If 'arg' is present, the constructor has arguments
  // corresponding to the declared variables.
//   DATATYPE,
//   THISTYPE, // Used to indicate recursion in recursive datatypes
//   CONSTRUCTOR,
//   SELECTOR,
//   TESTER,
  // Expression e WITH accessor := e2 is transformed by the command
  // processor into (DATATYPE_UPDATE e accessor e2), where e is the
  // original datatype value C(a1, ..., an) (here C is the
  // constructor), and "accessor" is the name of one of the arguments
  // a_i of C.
  //  DATATYPE_UPDATE,
  // Statement IF c1 THEN e1 ELSIF c2 THEN e2 ... ELSE e_n ENDIF is
  // represented as (IF (IFTHEN c1 e1) (IFTHEN c2 e2) ... (ELSE e_n))
  IF,
  IFTHEN,
  ELSE,
  // Lisp version of multi-branch IF:
  // (COND (c1 e1) (c2 e2) ... (ELSE en))
  COND,

  // LET x1: t1 = e1, x2: t2 = e2, ... IN e
  // Parser builds:
  // (LET (LETDECLS (LETDECL x1 t1 e1) (LETDECL x2 t2 e2) ... ) e)
  // where each x_i is a BOUNDVAR.
  // After processing, it is rebuilt to have (LETDECL var def); the
  // type is set as the attribute to var.
  LET,
  LETDECLS,
  LETDECL,
  // Lambda-abstraction LAMBDA (<vars>) : e  === (LAMBDA <vars> e)
  LAMBDA,
  // Symbolic simulation operator
  SIMULATE,

  // Uninterpreted constants (variables) x1, x2, ... , x_n : type
  // (CONST (VARLIST x1 x2 ... x_n) type)
  // Uninterpreted functions are declared as constants of functional type.

  // After processing, uninterpreted functions and constants
  // (a.k.a. variables) are represented as Op(UFUNC, (ID "name")) and
  // Op(UCONST, (ID "name")) with the appropriate type attribute.
  CONST,
  VARLIST,
  UCONST,

  // User function definition f(args) : type = e === (DEFUN args type e)
  // Here 'args' are bound var declarations
  DEFUN,

  // Arithmetic types and operators
//   REAL,
//   INT,

//   UMINUS,
//   PLUS,
//   MINUS,
//   MULT,
//   DIVIDE,
//   INTDIV,
//   MOD,
//   LT,
//   LE,
//   GT,
//   GE,
//   IS_INTEGER,
//   NEGINF,
//   POSINF,
//   DARK_SHADOW,
//   GRAY_SHADOW,

//   //Floor theory operators
//   FLOOR,
  // Kind for Extension to Non-linear Arithmetic
//   POW,

  // Kinds for proof terms
  PF_APPLY,
  PF_HOLE,


//   // Mlss
//   EMPTY, // {}
//   UNION, // +
//   INTER, // *
//   DIFF,
//   SINGLETON,
//   IN,
//   INCS,
//   INCIN,

  //Skolem variable
  SKOLEM_VAR,
  // Expr that holds a theorem
  THEOREM_KIND,
  //! Must always be the last kind
  LAST_KIND
} Kind;

} // end of namespace CVC3

#endif
