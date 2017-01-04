/*****************************************************************************/
/*!
 * \file theory_records.h
 * 
 * Author: Daniel Wichs
 * 
 * Created: 7/22/03
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
#ifndef _cvc3__include__theory_records_h_
#define _cvc3__include__theory_records_h_

#include "theory.h"

namespace CVC3 {

  class RecordsProofRules;

 typedef enum {
   RECORD = 2500,
   RECORD_SELECT,
   RECORD_UPDATE,
   RECORD_TYPE,
   TUPLE,
   TUPLE_SELECT,
   TUPLE_UPDATE,
   TUPLE_TYPE
 } RecordKinds;
                                    
/*****************************************************************************/
/*!
 *\class TheoryRecords
 *\ingroup Theories
 *\brief This theory handles records.
 *
 * Author: Daniel Wichs
 *
 * Created: 7/22/03
 */
/*****************************************************************************/
class TheoryRecords :public Theory {
  RecordsProofRules* d_rules;
  //! Auxiliary rewrites: Processing of AND and OR of equations.  Returns e=e'.
  Theorem rewriteAux(const Expr& e);
  //! Takes Thm(e), returns Thm(e'), where e rewrites to e' by rewriteAux.
  Theorem rewriteAux(const Theorem& thm);

public:
  TheoryRecords(TheoryCore* core); //!< Constructor
  ~TheoryRecords(); //!< Destructor
  //! creates a reference to the proof rules
  RecordsProofRules* createProofRules();
  
  // Theory interface

  //! assert a fact to the theory of records
  void assertFact(const Theorem& e);
  //! empty implementation to fit theory interface
  void checkSat(bool fullEffort) {}
  //! rewrites an expression e to one of several allowed forms
  Theorem rewrite(const Expr& e);
  //! check record or tuple type
  void checkType(const Expr& e);
  Cardinality finiteTypeInfo(Expr& e, Unsigned& n,
                             bool enumerate, bool computeSize);
  //! computes the type of a record or a tuple
  void computeType(const Expr& e);
  Type computeBaseType(const Type& t);
  
  Expr computeTypePred(const Type& t, const Expr& e);
  void computeModelTerm(const Expr& e, std::vector<Expr>& v);
  void computeModel(const Expr& e, std::vector<Expr>& vars);

  Expr computeTCC(const Expr& e);
  virtual Expr parseExprOp(const Expr& e);
  void setup(const Expr& e);
  void update(const Theorem& e, const Expr& d);
  //! pretty printing
  ExprStream& print(ExprStream& os, const Expr& e);
  //! Test whether expr is a record literal
  bool isRecord(const Expr& e) {
    return e.isApply() && e.getOpKind() == RECORD;
  }
  //! Test whether expr is a record type
  bool isRecordType(const Expr& e) {
    return e.isApply() && e.getOpKind() == RECORD_TYPE;
  }
  //! Test whether expr is a record type
  bool isRecordType(const Type& t) {
    return isRecordType(t.getExpr());
  }
  //! Test whether expr is a record select/update subclass
  bool isRecordAccess(const Expr& e)
  { return e.isApply() &&
      (e.getOpKind() == RECORD_SELECT || e.getOpKind() == RECORD_UPDATE); }
  //! Create a record literal
  Expr recordExpr(const std::vector<std::string>& fields,
		  const std::vector<Expr>& kids);
  //! Create a record literal
  Expr recordExpr(const std::vector<Expr>& fields,
		  const std::vector<Expr>& kids);
  //! Create a record type
  Type recordType(const std::vector<std::string>& fields,
		  const std::vector<Type>& types);
  //! Create a record type (field types are given as a vector of Expr)
  Type recordType(const std::vector<std::string>& fields,
		  const std::vector<Expr>& types);
  //! Create a record type (fields and types are given as a vector of Expr)
  Type recordType(const std::vector<Expr>& fields,
		  const std::vector<Expr>& types);
  //! Create a record field select expression
  Expr recordSelect(const Expr& r, const std::string& field);
  //! Create a record field update expression
  Expr recordUpdate(const Expr& r, const std::string& field, const Expr& val);
  //! Get the list of fields from a record literal
  const std::vector<Expr>& getFields(const Expr& r);
  //! Get the i-th field name from the record literal or type
  const std::string& getField(const Expr& e, int i);
  //! Get the field index in the record literal or type
  /*! The field must be present in the record; otherwise it's an error. */
  int getFieldIndex(const Expr& e, const std::string& field);
  //! Get the field name from the record select and update expressions
  const std::string& getField(const Expr& e);
  //! Create a tuple literal
  Expr tupleExpr(const std::vector<Expr>& kids);
  //! Create a tuple type
  Type tupleType(const std::vector<Type>& types);
  //! Create a tuple type (types of components are given as Exprs)
  Type tupleType(const std::vector<Expr>& types);
  //! Create a tuple index selector expression
  Expr tupleSelect(const Expr& tup, int i);
  //! Create a tuple index update expression
  Expr tupleUpdate(const Expr& tup, int i, const Expr& val);
  //! Get the index from the tuple select and update expressions
  int getIndex(const Expr& e);
  //! Test whether expr is a tuple select/update subclass
  bool isTupleAccess(const Expr& e)
    { return e.isApply() &&
        (e.getOpKind() == TUPLE_SELECT || e.getOpKind() == TUPLE_UPDATE); }
  //! Test if expr is a tuple literal
  bool isTuple(const Expr& e) { return e.isApply() && e.getOpKind() == TUPLE; }
  //! Test if expr represents a tuple type
  bool isTupleType(const Expr& e)
    { return e.isApply() && e.getOpKind() == TUPLE_TYPE; }
  //! Test if 'tp' is a tuple type
  bool isTupleType(const Type& tp) { return isTupleType(tp.getExpr()); }
};  // end of class TheoryRecords

}

#endif
