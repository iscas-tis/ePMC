/*****************************************************************************/
/*!
 * \file vc.h
 * \brief Generic API for a validity checker
 *
 * Author: Clark Barrett
 *
 * Created: Tue Nov 26 17:45:10 2002
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

#ifndef _cvc3__include__vc_h_
#define _cvc3__include__vc_h_

#include "os.h"
#include "queryresult.h"
#include "expr.h"
#include "formula_value.h"

/*****************************************************************************/
/*! Note that this list of modules is very incomplete
 */
/*****************************************************************************/

/*****************************************************************************/
/*!
 *\defgroup CVC3 CVC3
 *\brief The top level group which includes all of CVC3 documentation.
 *@{
 */
/*****************************************************************************/

/*****************************************************************************/
/*!
 *\defgroup BuildingBlocks Building Blocks
 *\brief Code providing basic infrastructure
 */
/*****************************************************************************/

/*****************************************************************************/
/*!
 *\defgroup VC Validity Checker
 *\brief The modules that make up the validity checker
 */
/*****************************************************************************/

/*@}*/ // end of group CVC3

/*****************************************************************************/
/*!
 *\defgroup VC_API Validity Checker API
 * \ingroup VC
 *\brief The library interface of the validity checker (class ValidityChecker)
 */
/*****************************************************************************/

namespace CVC3 {

class Context;
class CLFlags;
class Statistics;

/*****************************************************************************/
/*!
 *\class ValidityChecker
 *\brief Generic API for a validity checker
 *\ingroup VC_API
 *\anchor vc
 *
 * Author: Clark Barrett
 *
 * Created: Tue Nov 26 18:24:25 2002
 *
 * All terms and formulas are represented as expressions using the Expr class.
 * The notion of a context is also important.  A context is a "background" set
 * of formulas which are assumed to be true or false.  Formulas can be added to
 * the context explicitly, using assertFormula, or they may be added as part of
 * processing a query command.  At any time, the current set of formulas making
 * up the context can be retrieved using getAssumptions.
 */
/*****************************************************************************/
class CVC_DLL ValidityChecker {

public:
  //! Constructor
  ValidityChecker() {}
  //! Destructor
  virtual ~ValidityChecker() {}

  //! Return the set of command-line flags
  /*!  The flags are returned by reference, and if modified, will have an
    immediate effect on the subsequent commands.  Note that not all flags will
    have such an effect; some flags are used only at initialization time (like
    "sat"), and therefore, will not take effect if modified after
    ValidityChecker is created.
  */
  virtual CLFlags& getFlags() const = 0;
  //! Force reprocessing of all flags
  virtual void reprocessFlags() = 0;

  /***************************************************************************/
  /*
   * Static methods
   */
  /***************************************************************************/

  //! Create the set of command line flags with default values;
  /*!
    \return the set of flags by value
  */
  static CLFlags createFlags();
  //! Create an instance of ValidityChecker
  /*!
    \param flags is the set of command line flags.
  */
  static ValidityChecker* create(const CLFlags& flags);
  //! Create an instance of ValidityChecker using default flag values.
  static ValidityChecker* create();

  /***************************************************************************/
  /*!
   *\name Type-related methods
   * Methods for creating and looking up types
   *\sa class Type
   *@{
   */
  /***************************************************************************/

  // Basic types
  virtual Type boolType() = 0; //!< Create type BOOLEAN

  virtual Type realType() = 0; //!< Create type REAL

  virtual Type intType() = 0; //!< Create type INT

  //! Create a subrange type [l..r]
  /*! l and r can be Null; l=Null represents minus infinity, r=Null is
   * plus infinity.
   */
  virtual Type subrangeType(const Expr& l, const Expr& r) = 0;

  //! Creates a subtype defined by the given predicate
  /*!
   * \param pred is a predicate taking one argument of type T and returning
   * Boolean.  The resulting type is a subtype of T whose elements x are those
   * satisfying the predicate pred(x).
   *
   * \param witness is an expression of type T for which pred holds (if a Null
   *  expression is passed as a witness, cvc will try to prove \f$\exists x. pred(x))\f$.
   *  if the witness check fails, a TypecheckException is thrown.
   */
  virtual Type subtypeType(const Expr& pred, const Expr& witness) = 0;

  // Tuple types
  //! 2-element tuple
  virtual Type tupleType(const Type& type0, const Type& type1) = 0;

  //! 3-element tuple
  virtual Type tupleType(const Type& type0, const Type& type1,
			 const Type& type2) = 0;
  //! n-element tuple (from a vector of types)
  virtual Type tupleType(const std::vector<Type>& types) = 0;

  // Record types
  //! 1-element record
  virtual Type recordType(const std::string& field, const Type& type) = 0;

  //! 2-element record
  /*! Fields will be sorted automatically */
  virtual Type recordType(const std::string& field0, const Type& type0,
			  const std::string& field1, const Type& type1) = 0;
  //! 3-element record
  /*! Fields will be sorted automatically */
  virtual Type recordType(const std::string& field0, const Type& type0,
			  const std::string& field1, const Type& type1,
			  const std::string& field2, const Type& type2) = 0;
  //! n-element record (fields and types must be of the same length)
  /*! Fields will be sorted automatically */
  virtual Type recordType(const std::vector<std::string>& fields,
			  const std::vector<Type>& types) = 0;

  // Datatypes

  //! Single datatype, single constructor
  /*! The types are either type exressions (obtained from a type with
   *  getExpr()) or string expressions containing the name of (one of) the
   *  dataType(s) being defined. */
  virtual Type dataType(const std::string& name,
                        const std::string& constructor,
                        const std::vector<std::string>& selectors,
                        const std::vector<Expr>& types) = 0;

  //! Single datatype, multiple constructors
  /*! The types are either type exressions (obtained from a type with
   *  getExpr()) or string expressions containing the name of (one of) the
   *  dataType(s) being defined. */
  virtual Type dataType(const std::string& name,
                        const std::vector<std::string>& constructors,
                        const std::vector<std::vector<std::string> >& selectors,
                        const std::vector<std::vector<Expr> >& types) = 0;

  //! Multiple datatypes
  /*! The types are either type exressions (obtained from a type with
   *  getExpr()) or string expressions containing the name of (one of) the
   *  dataType(s) being defined. */
  virtual void dataType(const std::vector<std::string>& names,
                        const std::vector<std::vector<std::string> >& constructors,
                        const std::vector<std::vector<std::vector<std::string> > >& selectors,
                        const std::vector<std::vector<std::vector<Expr> > >& types,
                        std::vector<Type>& returnTypes) = 0;

  //! Create an array type (ARRAY typeIndex OF typeData)
  virtual Type arrayType(const Type& typeIndex, const Type& typeData) = 0;

  //! Create a bitvector type of length n
  virtual Type bitvecType(int n) = 0;

  //! Create a function type typeDom -> typeRan
  virtual Type funType(const Type& typeDom, const Type& typeRan) = 0;

  //! Create a function type (t1,t2,...,tn) -> typeRan
  virtual Type funType(const std::vector<Type>& typeDom, const Type& typeRan) = 0;

  //! Create named user-defined uninterpreted type
  virtual Type createType(const std::string& typeName) = 0;

  //! Create named user-defined interpreted type (type abbreviation)
  virtual Type createType(const std::string& typeName, const Type& def) = 0;

  //! Lookup a user-defined (uninterpreted) type by name.  Returns Null if none.
  virtual Type lookupType(const std::string& typeName) = 0;

  /*@}*/ // End of Type-related methods

  /***************************************************************************/
  /*!
   *\name General Expr methods
   *\sa class Expr
   *\sa class ExprManager
   *@{
   */
  /***************************************************************************/

  //! Return the ExprManager
  virtual ExprManager* getEM() = 0;

  //! Create a variable with a given name and type
  /*!
    \param name is the name of the variable
    \param type is its type.  The type cannot be a function type.
    \return an Expr representation of a new variable
   */
  virtual Expr varExpr(const std::string& name, const Type& type) = 0;

  //! Create a variable with a given name, type, and value
  virtual Expr varExpr(const std::string& name, const Type& type,
		       const Expr& def) = 0;

  //! Get the variable associated with a name, and its type
  /*!
    \param name is the variable name
    \param type is where the type value is returned

    \return a variable by the name. If there is no such Expr, a NULL \
    Expr is returned.
  */
  virtual Expr lookupVar(const std::string& name, Type* type) = 0;

  //! Get the type of the Expr.
  virtual Type getType(const Expr& e) = 0;

  //! Get the largest supertype of the Expr.
  virtual Type getBaseType(const Expr& e) = 0;

  //! Get the largest supertype of the Type.
  virtual Type getBaseType(const Type& t) = 0;

  //! Get the subtype predicate
  virtual Expr getTypePred(const Type&t, const Expr& e) = 0;

  //! Create a string Expr
  virtual Expr stringExpr(const std::string& str) = 0;

  //! Create an ID Expr
  virtual Expr idExpr(const std::string& name) = 0;

  //! Create a list Expr
  /*! Intermediate representation for DP-specific expressions.
   *  Normally, the first element of the list is a string Expr
   *  representing an operator, and the rest of the list are the
   *  arguments.  For example,
   *
   *  kids.push_back(vc->stringExpr("PLUS"));
   *  kids.push_back(x); // x and y are previously created Exprs
   *  kids.push_back(y);
   *  Expr lst = vc->listExpr(kids);
   *
   * Or, alternatively (using its overloaded version):
   *
   * Expr lst = vc->listExpr("PLUS", x, y);
   *
   * or
   *
   * vector<Expr> summands;
   * summands.push_back(x); summands.push_back(y); ...
   * Expr lst = vc->listExpr("PLUS", summands);
   */
  virtual Expr listExpr(const std::vector<Expr>& kids) = 0;

  //! Overloaded version of listExpr with one argument
  virtual Expr listExpr(const Expr& e1) = 0;

  //! Overloaded version of listExpr with two arguments
  virtual Expr listExpr(const Expr& e1, const Expr& e2) = 0;

  //! Overloaded version of listExpr with three arguments
  virtual Expr listExpr(const Expr& e1, const Expr& e2, const Expr& e3) = 0;

  //! Overloaded version of listExpr with string operator and many arguments
  virtual Expr listExpr(const std::string& op,
			const std::vector<Expr>& kids) = 0;

  //! Overloaded version of listExpr with string operator and one argument
  virtual Expr listExpr(const std::string& op, const Expr& e1) = 0;

  //! Overloaded version of listExpr with string operator and two arguments
  virtual Expr listExpr(const std::string& op, const Expr& e1,
			const Expr& e2) = 0;

  //! Overloaded version of listExpr with string operator and three arguments
  virtual Expr listExpr(const std::string& op, const Expr& e1,
			const Expr& e2, const Expr& e3) = 0;

  //! Prints e to the standard output
  virtual void printExpr(const Expr& e) = 0;

  //! Prints e to the given ostream
  virtual void printExpr(const Expr& e, std::ostream& os) = 0;

  //! Parse an expression using a Theory-specific parser
  virtual Expr parseExpr(const Expr& e) = 0;

  //! Parse a type expression using a Theory-specific parser
  virtual Type parseType(const Expr& e) = 0;

  //! Import the Expr from another instance of ValidityChecker
  /*! When expressions need to be passed among several instances of
   *  ValidityChecker, they need to be explicitly imported into the
   *  corresponding instance using this method.  The return result is
   *  an identical expression that belongs to the current instance of
   *  ValidityChecker, and can be safely used as part of more complex
   *  expressions from the same instance.
   */
  virtual Expr importExpr(const Expr& e) = 0;

  //! Import the Type from another instance of ValidityChecker
  /*! \sa getType() */
  virtual Type importType(const Type& t) = 0;

  //! Parse a sequence of commands from a presentation language string
  virtual void cmdsFromString(const std::string& s,
                              InputLanguage lang=PRESENTATION_LANG) = 0;

  //! Parse an expression from a presentation language string
  virtual Expr exprFromString(const std::string& e) = 0;

  /*@}*/ // End of General Expr Methods

  /***************************************************************************/
  /*!
   *\name Core expression methods
   * Methods for manipulating core expressions
   *
   * Except for equality and ite, the children provided as arguments must be of
   * type Boolean.
   *@{
   */
  /***************************************************************************/

  //! Return TRUE Expr
  virtual Expr trueExpr() = 0;

  //! Return FALSE Expr
  virtual Expr falseExpr() = 0;

  //! Create negation
  virtual Expr notExpr(const Expr& child) = 0;

  //! Create 2-element conjunction
  virtual Expr andExpr(const Expr& left, const Expr& right) = 0;

  //! Create n-element conjunction
  virtual Expr andExpr(const std::vector<Expr>& children) = 0;

  //! Create 2-element disjunction
  virtual Expr orExpr(const Expr& left, const Expr& right) = 0;

  //! Create n-element disjunction
  virtual Expr orExpr(const std::vector<Expr>& children) = 0;

  //! Create Boolean implication
  virtual Expr impliesExpr(const Expr& hyp, const Expr& conc) = 0;

  //! Create left IFF right (boolean equivalence)
  virtual Expr iffExpr(const Expr& left, const Expr& right) = 0;

  //! Create an equality expression.
  /*!
    The two children must have the same type, and cannot be of type
    Boolean.
  */
  virtual Expr eqExpr(const Expr& child0, const Expr& child1) = 0;

  //! Create IF ifpart THEN thenpart ELSE elsepart ENDIF
  /*!
    \param ifpart must be of type Boolean.
    \param thenpart and \param elsepart must have the same type, which will
    also be the type of the ite expression.
  */
  virtual Expr iteExpr(const Expr& ifpart, const Expr& thenpart,
		       const Expr& elsepart) = 0;

  /**
   * Create an expression asserting that all the children are different.
   * @param children the children to be asserted different
   */
  virtual Expr distinctExpr(const std::vector<Expr>& children) = 0;

  /*@}*/ // End of Core expression methods

  /***************************************************************************/
  /*!
   *\name User-defined (uninterpreted) function methods
   * Methods for manipulating uninterpreted function expressions
   *@{
   */
  /***************************************************************************/

  //! Create a named uninterpreted function with a given type
  /*!
    \param name is the new function's name (as ID Expr)
    \param type is a function type ( [range -> domain] )
  */
  virtual Op createOp(const std::string& name, const Type& type) = 0;

  //! Create a named user-defined function with a given type
  virtual Op createOp(const std::string& name, const Type& type,
		      const Expr& def) = 0;

  //! Get the Op associated with a name, and its type
  /*!
    \param name is the operator name
    \param type is where the type value is returned

    \return an Op by the name. If there is no such Op, a NULL \
    Op is returned.
  */
  virtual Op lookupOp(const std::string& name, Type* type) = 0;

  //! Unary function application (op must be of function type)
  virtual Expr funExpr(const Op& op, const Expr& child) = 0;

  //! Binary function application (op must be of function type)
  virtual Expr funExpr(const Op& op, const Expr& left, const Expr& right) = 0;

  //! Ternary function application (op must be of function type)
  virtual Expr funExpr(const Op& op, const Expr& child0,
		       const Expr& child1, const Expr& child2) = 0;

  //! n-ary function application (op must be of function type)
  virtual Expr funExpr(const Op& op, const std::vector<Expr>& children) = 0;

  /*@}*/ // End of User-defined (uninterpreted) function methods

  /***************************************************************************/
  /*!
   *\name Arithmetic expression methods
   * Methods for manipulating arithmetic expressions
   *
   * These functions create arithmetic expressions.  The children provided
   * as arguments must be of type Real.
   *@{
   */
  /***************************************************************************/

  /*!
   * Add the pair of variables to the variable ordering for aritmetic solving.
   * Terms that are not arithmetic will be ignored.
   * \param smaller the smaller variable
   * \param bigger the bigger variable
   */
  virtual bool addPairToArithOrder(const Expr& smaller, const Expr& bigger) = 0;

  //! Create a rational number with numerator n and denominator d.
  /*!
    \param n the numerator
    \param d the denominator, cannot be 0.
  */
  virtual Expr ratExpr(int n, int d = 1) = 0;

  //! Create a rational number with numerator n and denominator d.
  /*!
    Here n and d are given as strings.  They are converted to
    arbitrary-precision integers according to the given base.
  */
  virtual Expr ratExpr(const std::string& n, const std::string& d, int base) = 0;

  //! Create a rational from a single string.
  /*!
    \param n can be a string containing an integer, a pair of integers
    "nnn/ddd", or a number in the fixed or floating point format.
    \param base is the base in which to interpret the string.
  */
  virtual Expr ratExpr(const std::string& n, int base = 10) = 0;

  //! Unary minus.
  virtual Expr uminusExpr(const Expr& child) = 0;

  //! Create 2-element sum (left + right)
  virtual Expr plusExpr(const Expr& left, const Expr& right) = 0;

  //! Create n-element sum
  virtual Expr plusExpr(const std::vector<Expr>& children) = 0;

  //! Make a difference (left - right)
  virtual Expr minusExpr(const Expr& left, const Expr& right) = 0;

  //! Create a product (left * right)
  virtual Expr multExpr(const Expr& left, const Expr& right) = 0;

  //! Create a power expression (x ^ n); n must be integer
  virtual Expr powExpr(const Expr& x, const Expr& n) = 0;

  //! Create expression x / y
  virtual Expr divideExpr(const Expr& numerator, const Expr& denominator) = 0;

  //! Create (left < right)
  virtual Expr ltExpr(const Expr& left, const Expr& right) = 0;

  //! Create (left <= right)
  virtual Expr leExpr(const Expr& left, const Expr& right) = 0;

  //! Create (left > right)
  virtual Expr gtExpr(const Expr& left, const Expr& right) = 0;

  //! Create (left >= right)
  virtual Expr geExpr(const Expr& left, const Expr& right) = 0;

  /*@}*/ // End of Arithmetic expression methods

  /***************************************************************************/
  /*!
   *\name Record expression methods
   * Methods for manipulating record expressions
   *@{
   */
  /***************************************************************************/

  //! Create a 1-element record value (# field := expr #)
  /*! Fields will be sorted automatically */
  virtual Expr recordExpr(const std::string& field, const Expr& expr) = 0;

  //! Create a 2-element record value (# field0 := expr0, field1 := expr1 #)
  /*! Fields will be sorted automatically */
  virtual Expr recordExpr(const std::string& field0, const Expr& expr0,
			  const std::string& field1, const Expr& expr1) = 0;

  //! Create a 3-element record value (# field_i := expr_i #)
  /*! Fields will be sorted automatically */
  virtual Expr recordExpr(const std::string& field0, const Expr& expr0,
			  const std::string& field1, const Expr& expr1,
			  const std::string& field2, const Expr& expr2) = 0;

  //! Create an n-element record value (# field_i := expr_i #)
  /*!
   * \param fields
   * \param exprs must be the same length as fields
   *
   * Fields will be sorted automatically
   */
  virtual Expr recordExpr(const std::vector<std::string>& fields,
			  const std::vector<Expr>& exprs) = 0;

  //! Create record.field (field selection)
  /*! Create an expression representing the selection of a field from
    a record. */
  virtual Expr recSelectExpr(const Expr& record, const std::string& field) = 0;

  //! Record update; equivalent to "record WITH .field := newValue"
  /*! Notice the `.' before field in the presentation language (and
    the comment above); this is to distinguish it from datatype
    update.
  */
  virtual Expr recUpdateExpr(const Expr& record, const std::string& field,
			     const Expr& newValue) = 0;

  /*@}*/ // End of Record expression methods

  /***************************************************************************/
  /*!
   *\name Array expression methods
   * Methods for manipulating array expressions
   *@{
   */
  /***************************************************************************/

  //! Create an expression array[index] (array access)
  /*! Create an expression for the value of array at the given index */
  virtual Expr readExpr(const Expr& array, const Expr& index) = 0;

  //! Array update; equivalent to "array WITH index := newValue"
  virtual Expr writeExpr(const Expr& array, const Expr& index,
			 const Expr& newValue) = 0;

  /*@}*/ // End of Array expression methods

  /***************************************************************************/
  /*!
   *\name Bitvector expression methods
   * Methods for manipulating bitvector expressions
   *@{
   */
  /***************************************************************************/

  // Bitvector constants
  // From a string of digits in a given base
  virtual Expr newBVConstExpr(const std::string& s, int base = 2) = 0;
  // From a vector of bools
  virtual Expr newBVConstExpr(const std::vector<bool>& bits) = 0;
  // From a rational: bitvector is of length 'len', or the min. needed length when len=0.
  virtual Expr newBVConstExpr(const Rational& r, int len = 0) = 0;

  // Concat and extract
  virtual Expr newConcatExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newConcatExpr(const std::vector<Expr>& kids) = 0;
  virtual Expr newBVExtractExpr(const Expr& e, int hi, int low) = 0;

  // Bitwise Boolean operators: Negation, And, Nand, Or, Nor, Xor, Xnor
  virtual Expr newBVNegExpr(const Expr& t1) = 0;

  virtual Expr newBVAndExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVAndExpr(const std::vector<Expr>& kids) = 0;

  virtual Expr newBVOrExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVOrExpr(const std::vector<Expr>& kids) = 0;

  virtual Expr newBVXorExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVXorExpr(const std::vector<Expr>& kids) = 0;

  virtual Expr newBVXnorExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVXnorExpr(const std::vector<Expr>& kids) = 0;

  virtual Expr newBVNandExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVNorExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVCompExpr(const Expr& t1, const Expr& t2) = 0;

  // Unsigned bitvector inequalities
  virtual Expr newBVLTExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVLEExpr(const Expr& t1, const Expr& t2) = 0;

  // Signed bitvector inequalities
  virtual Expr newBVSLTExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVSLEExpr(const Expr& t1, const Expr& t2) = 0;

  // Sign-extend t1 to a total of len bits
  virtual Expr newSXExpr(const Expr& t1, int len) = 0;

  // Bitvector arithmetic: unary minus, plus, subtract, multiply
  virtual Expr newBVUminusExpr(const Expr& t1) = 0;
  virtual Expr newBVSubExpr(const Expr& t1, const Expr& t2) = 0;
  //! 'numbits' is the number of bits in the result
  virtual Expr newBVPlusExpr(int numbits, const std::vector<Expr>& k) = 0;
  virtual Expr newBVPlusExpr(int numbits, const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVMultExpr(int numbits,
                             const Expr& t1, const Expr& t2) = 0;

  virtual Expr newBVUDivExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVURemExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVSDivExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVSRemExpr(const Expr& t1, const Expr& t2) = 0;
  virtual Expr newBVSModExpr(const Expr& t1, const Expr& t2) = 0;

  // Left shift by r bits: result is old size + r bits
  virtual Expr newFixedLeftShiftExpr(const Expr& t1, int r) = 0;
  // Left shift by r bits: result is same size as t1
  virtual Expr newFixedConstWidthLeftShiftExpr(const Expr& t1, int r) = 0;
  // Logical right shift by r bits: result is same size as t1
  virtual Expr newFixedRightShiftExpr(const Expr& t1, int r) = 0;
  // Get value of BV Constant
  virtual Rational computeBVConst(const Expr& e) = 0;

  /*@}*/ // End of Bitvector expression methods

  /***************************************************************************/
  /*!
   *\name Other expression methods
   * Methods for manipulating other kinds of expressions
   *@{
   */
  /***************************************************************************/

  //! Tuple expression
  virtual Expr tupleExpr(const std::vector<Expr>& exprs) = 0;

  //! Tuple select; equivalent to "tuple.n", where n is an numeral (e.g. tup.5)
  virtual Expr tupleSelectExpr(const Expr& tuple, int index) = 0;

  //! Tuple update; equivalent to "tuple WITH index := newValue"
  virtual Expr tupleUpdateExpr(const Expr& tuple, int index,
			       const Expr& newValue) = 0;

  //! Datatype constructor expression
  virtual Expr datatypeConsExpr(const std::string& constructor, const std::vector<Expr>& args) = 0;

  //! Datatype selector expression
  virtual Expr datatypeSelExpr(const std::string& selector, const Expr& arg) = 0;

  //! Datatype tester expression
  virtual Expr datatypeTestExpr(const std::string& constructor, const Expr& arg) = 0;

  //! Create a bound variable with a given name, unique ID (uid) and type
  /*!
    \param name is the name of the variable
    \param uid is the unique ID (a string), which must be unique for
    each variable
    \param type is its type.  The type cannot be a function type.
    \return an Expr representation of a new variable
   */
  virtual Expr boundVarExpr(const std::string& name,
			    const std::string& uid,
			    const Type& type) = 0;

  //! Universal quantifier
  virtual Expr forallExpr(const std::vector<Expr>& vars, const Expr& body) = 0;
  //! Universal quantifier with a trigger
  virtual Expr forallExpr(const std::vector<Expr>& vars, const Expr& body, 
                          const Expr& trigger) = 0;
  //! Universal quantifier with a set of triggers.
  virtual Expr forallExpr(const std::vector<Expr>& vars, const Expr& body,
                          const std::vector<Expr>& triggers) = 0;
  //! Universal quantifier with a set of multi-triggers.
  virtual Expr forallExpr(const std::vector<Expr>& vars, const Expr& body,
			  const std::vector<std::vector<Expr> >& triggers) = 0;

  //! Set triggers for quantifier instantiation
  /*!
   * \param e the expression for which triggers are being set.
   * \param triggers Each item in triggers is a vector of Expr containing one
   * or more patterns.  A pattern is a term or Atomic predicate sub-expression
   * of e.  A vector containing more than one pattern is treated as a
   * multi-trigger.  Patterns will be matched in the order they occur in
   * the vector.
  */
  virtual void setTriggers(const Expr& e, const std::vector<std::vector<Expr> > & triggers) = 0;
  //! Set triggers for quantifier instantiation (no multi-triggers)
  virtual void setTriggers(const Expr& e, const std::vector<Expr>& triggers) = 0;
  //! Set a single trigger for quantifier instantiation
  virtual void setTrigger(const Expr& e, const Expr& trigger) = 0;
  //! Set a single multi-trigger for quantifier instantiation
  virtual void setMultiTrigger(const Expr& e, const std::vector<Expr>& multiTrigger) = 0;

  //! Existential quantifier
  virtual Expr existsExpr(const std::vector<Expr>& vars, const Expr& body) = 0;

  //! Lambda-expression
  virtual Op lambdaExpr(const std::vector<Expr>& vars, const Expr& body) = 0;

  //! Transitive closure of a binary predicate
  virtual Op transClosure(const Op& op) = 0;

  //! Symbolic simulation expression
  /*!
   * \param f is the next state function (LAMBDA-expression)
   * \param s0 is the initial state
   * \param inputs is the vector of LAMBDA-expressions representing
   * the sequences of inputs to f
   * \param n is a constant, the number of cycles to run the simulation.
   */
  virtual Expr simulateExpr(const Expr& f, const Expr& s0,
			    const std::vector<Expr>& inputs,
			    const Expr& n) = 0;

  /*@}*/ // End of Other expression methods

  /***************************************************************************/
  /*!
   *\name Validity checking methods
   * Methods related to validity checking
   *
   * This group includes methods for asserting formulas, checking
   * validity in the given logical context, manipulating the scope
   * level of the context, etc.
   *@{
   */
  /***************************************************************************/

  //! Set the resource limit (0==unlimited, 1==exhausted).
  /*! Currently, the limit is the total number of processed facts. */
  virtual void setResourceLimit(unsigned limit) = 0;

  //! Set a time limit in tenth of a second,
  /*! counting the cpu time used by the current process from now on.
   *  Currently, when the limit is reached, cvc3 tries to quickly
   *  terminate, probably with the status unknown.
   */
  virtual void setTimeLimit(unsigned limit) = 0;

  //! Assert a new formula in the current context.
  /*! This creates the assumption e |- e.  The formula must have Boolean type.
  */
  virtual void assertFormula(const Expr& e) = 0;

  //! Register an atomic formula of interest.
  /*! Registered atoms are tracked by the decision procedures.  If one of them
      is deduced to be true or false, it is added to a list of implied literals.
      Implied literals can be retrieved with the getImpliedLiteral function */
  virtual void registerAtom(const Expr& e) = 0;

  //! Return next literal implied by last assertion.  Null Expr if none.
  /*! Returned literals are either registered atomic formulas or their negation
   */
  virtual Expr getImpliedLiteral() = 0;

  //! Simplify e with respect to the current context
  virtual Expr simplify(const Expr& e) = 0;

  //! Check validity of e in the current context.
  /*! If it returns VALID, the scope and context are the same
   *  as when called.  If it returns INVALID, the context will be one which
   *  falsifies the query.  If it returns UNKNOWN, the context will falsify the
   *  query, but the context may be inconsistent.  Finally, if it returns
   *  ABORT, the context will be one which satisfies as much as possible.
   *
   *  \param e is the queried formula
   */
  virtual QueryResult query(const Expr& e) = 0;

  //! Check satisfiability of the expr in the current context.
  /*! Equivalent to query(!e) */
  virtual QueryResult checkUnsat(const Expr& e) = 0;

  //! Get the next model
  /*! This method should only be called after a query which returns
    INVALID.  Its return values are as for query(). */
  virtual QueryResult checkContinue() = 0;

  //! Restart the most recent query with e as an additional assertion.
  /*! This method should only be called after a query which returns
    INVALID.  Its return values are as for query(). */
  virtual QueryResult restart(const Expr& e) = 0;

  //! Returns to context immediately before last invalid query.
  /*! This method should only be called after a query which returns false.
   */
  virtual void returnFromCheck() = 0;

  //! Get assumptions made by the user in this and all previous contexts.
  /*! User assumptions are created either by calls to assertFormula or by a
   * call to query.  In the latter case, the negated query is added as an
   * assumption.
   * \param assumptions should be empty on entry.
  */
  virtual void getUserAssumptions(std::vector<Expr>& assumptions) = 0;

  //! Get assumptions made internally in this and all previous contexts.
  /*! Internal assumptions are literals assumed by the sat solver.
   * \param assumptions should be empty on entry.
  */
  virtual void getInternalAssumptions(std::vector<Expr>& assumptions) = 0;

  //! Get all assumptions made in this and all previous contexts.
  /*! \param assumptions should be empty on entry.
  */
  virtual void getAssumptions(std::vector<Expr>& assumptions) = 0;

  //! Returns the set of assumptions used in the proof of queried formula.
  /*! It returns a subset of getAssumptions().  If the last query was false
   *  or there has not yet been a query, it does nothing.
   *  NOTE: this functionality is not supported yet
   *  \param assumptions should be empty on entry.
  */
  virtual void getAssumptionsUsed(std::vector<Expr>& assumptions) = 0;

  virtual Expr getProofQuery() = 0;


  //! Return the internal assumptions that make the queried formula false.
  /*! This method should only be called after a query which returns
    false.  It will try to return the simplest possible subset of
    the internal assumptions sufficient to make the queried expression
    false.
    \param assumptions should be empty on entry.
    \param inOrder if true, returns the assumptions in the order they
    were made.  This is slightly more expensive than inOrder = false.
  */
  virtual void getCounterExample(std::vector<Expr>& assumptions,
                                 bool inOrder=true) = 0;

  //! Will assign concrete values to all user created variables
  /*! This function should only be called after a query which return false.
  */
  virtual void getConcreteModel(ExprMap<Expr> & m) = 0;

  //! If the result of the last query was UNKNOWN try to actually build the model
  //! to verify the result.
  /*! This function should only be called after a query which return unknown.
  */
  virtual QueryResult tryModelGeneration() = 0;

  //:ALEX: returns the current truth value of a formula
  // returns UNKNOWN_VAL if e is not associated
  // with a boolean variable in the SAT module,
  // i.e. if its value can not determined without search.
  virtual FormulaValue value(const Expr& e) = 0;

  //! Returns true if the current context is inconsistent.
  /*! Also returns a minimal set of assertions used to determine the
   inconsistency.
   \param assumptions should be empty on entry.
  */
  virtual bool inconsistent(std::vector<Expr>& assumptions) = 0;

  //! Returns true if the current context is inconsistent.
  virtual bool inconsistent() = 0;

  //! Returns true if the invalid result from last query() is imprecise
  /*!
   * Some decision procedures in CVC are incomplete (quantifier
   * elimination, non-linear arithmetic, etc.).  If any incomplete
   * features were used during the last query(), and the result is
   * "invalid" (query() returns false), then this result is
   * inconclusive.  It means that the system gave up the search for
   * contradiction at some point.
   */
  virtual bool incomplete() = 0;

  //! Returns true if the invalid result from last query() is imprecise
  /*!
   * \sa incomplete()
   *
   * The argument is filled with the reasons for incompleteness (they
   * are intended to be shown to the end user).
   */
  virtual bool incomplete(std::vector<std::string>& reasons) = 0;

  //! Returns the proof term for the last proven query
  /*! If there has not been a successful query, it should return a NULL proof
  */
  virtual Proof getProof() = 0;

  //! Returns the TCC of the last assumption or query
  /*! Returns Null if no assumptions or queries were performed. */
  virtual Expr getTCC() = 0;

  //! Return the set of assumptions used in the proof of the last TCC
  virtual void getAssumptionsTCC(std::vector<Expr>& assumptions) = 0;

  //! Returns the proof of TCC of the last assumption or query
  /*! Returns Null if no assumptions or queries were performed. */
  virtual Proof getProofTCC() = 0;

  //! After successful query, return its closure |- Gamma => phi
  /*! Turn a valid query Gamma |- phi into an implication
   * |- Gamma => phi.
   *
   * Returns Null if last query was invalid.
   */
  virtual Expr getClosure() = 0;

  //! Construct a proof of the query closure |- Gamma => phi
  /*! Returns Null if last query was Invalid. */
  virtual Proof getProofClosure() = 0;

  /*@}*/ // End of Validity checking methods

  /***************************************************************************/
  /*!
   *\name Context methods
   * Methods for manipulating contexts
   *
   * Contexts support stack-based push and pop.  There are two
   * separate notions of the current context stack.  stackLevel(), push(),
   * pop(), and popto() work with the user-level notion of the stack.
   *
   * scopeLevel(), pushScope(), popScope(), and poptoScope() work with
   * the internal stack which is more fine-grained than the user
   * stack.
   *
   * Do not use the scope methods unless you know what you are doing.
   * *@{
   */
  /***************************************************************************/

  //! Returns the current stack level.  Initial level is 0.
  virtual int stackLevel() = 0;

  //! Checkpoint the current context and increase the scope level
  virtual void push() = 0;

  //! Restore the current context to its state at the last checkpoint
  virtual void pop() = 0;

  //! Restore the current context to the given stackLevel.
  /*!
    \param stackLevel should be greater than or equal to 0 and less
    than or equal to the current scope level.
  */
  virtual void popto(int stackLevel) = 0;

  //! Returns the current scope level.  Initially, the scope level is 1.
  virtual int scopeLevel() = 0;

  /*! @brief Checkpoint the current context and increase the
   * <strong>internal</strong> scope level.  Do not use unless you
   * know what you're doing!
   */
  virtual void pushScope() = 0;

  /*! @brief Restore the current context to its state at the last
   * <strong>internal</strong> checkpoint.  Do not use unless you know
   * what you're doing!
   */
  virtual void popScope() = 0;

  //! Restore the current context to the given scopeLevel.
  /*!
    \param scopeLevel should be less than or equal to the current scope level.

    If scopeLevel is less than 1, then the current context is reset
    and the scope level is set to 1.
  */
  virtual void poptoScope(int scopeLevel) = 0;

  //! Get the current context
  virtual Context* getCurrentContext() = 0;

  //! Destroy and recreate validity checker: resets everything except for flags
  virtual void reset() = 0;

  /*@}*/ // End of Context methods

  /***************************************************************************/
  /*!
   *\name Reading files
   * Methods for reading external files
   *@{
   */
  /***************************************************************************/

  //! Read and execute the commands from a file given by name ("" means stdin)
  virtual void loadFile(const std::string& fileName,
			InputLanguage lang = PRESENTATION_LANG,
			bool interactive = false,
                        bool calledFromParser = false) = 0;

  //! Read and execute the commands from a stream
  virtual void loadFile(std::istream& is,
			InputLanguage lang = PRESENTATION_LANG,
			bool interactive = false) = 0;

  /*@}*/ // End of methods for reading files

  /***************************************************************************/
  /*!
   *\name Reporting Statistics
   * Methods for collecting and reporting run-time statistics
   *@{
   */
  /***************************************************************************/

  //! Get statistics object
  virtual Statistics& getStatistics() = 0;

  //! Print collected statistics to stdout
  virtual void printStatistics() = 0;

  /*@}*/ // End of Statistics Methods


}; // End of class ValidityChecker

} // End of namespace CVC3

#endif
