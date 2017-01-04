/*****************************************************************************/
/*!
 * \file common_proof_rules.h
 * 
 * Author: Sergey Berezin
 * 
 * Created: Dec 11 18:15:37 GMT 2002
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
// CLASS: CommonProofRules
//
// AUTHOR: Sergey Berezin, 12/09/2002
//
// Description: Commonly used proof rules (reflexivity, symmetry,
// transitivity, substitutivity, etc.).
//
// Normally, proof rule interfaces belong to their decision
// procedures.  However, in the case of equational logic, the rules
// are so useful, that even some basic classes like Transformer use
// these rules under the hood.  Therefore, it is made public, and its
// implementation is provided by the 'theorem' module.
///////////////////////////////////////////////////////////////////////////////

#ifndef _cvc3__common_proof_rules_h_
#define _cvc3__common_proof_rules_h_

#include <vector>

namespace CVC3 {

  class Theorem;
  class Theorem3;
  class Expr;
  class Op;

  class CommonProofRules {
  public:
    //! Destructor
    virtual ~CommonProofRules() { }

    ////////////////////////////////////////////////////////////////////////
    // TCC rules (3-valued logic)
    ////////////////////////////////////////////////////////////////////////

    //  G1 |- phi   G2 |- D_phi
    // -------------------------
    //       G1,G2 |-_3 phi
    /*!
     * @brief Convert 2-valued formula to 3-valued by discharging its
     *  TCC (\f$D_\phi\f$):
     *  \f[\frac{\Gamma_1\vdash_2 \phi\quad \Gamma_2\vdash_2 D_{\phi}}
     *          {\Gamma_1,\,\Gamma_2\vdash_3\phi}\f]
     */
    virtual Theorem3 queryTCC(const Theorem& phi, const Theorem& D_phi) = 0;

    //  G0,a1,...,an |-_3 phi  G1 |- D_a1 ... Gn |- D_an
    // -------------------------------------------------
    //    G0,G1,...,Gn |-_3 (a1 & ... & an) -> phi
    /*!
     * @brief 3-valued implication introduction rule:
     * \f[\frac{\Gamma_0,\,\alpha_1\,\ldots,\,\alpha_n\vdash_3\phi\quad
     *          (\Gamma_i\vdash D_{\alpha_i})_{i\in[1..n]}}
     *         {\Gamma_0,\,\Gamma_1, \ldots, \Gamma_n\vdash_3
     *              (\bigwedge_{i=1}^n\alpha_i)\to\phi}\f]
     *
     * \param phi is the formula \f$\phi\f$
     * \param assump is the vector of assumptions \f$\alpha_1\ldots\alpha_n\f$
     * \param tccs is the vector of TCCs for assumptions
     *   \f$D_{\alpha_1}\ldots D_{\alpha_n}\f$
     */
    virtual Theorem3 implIntro3(const Theorem3& phi,
				const std::vector<Expr>& assump,
				const std::vector<Theorem>& tccs) = 0;

    ////////////////////////////////////////////////////////////////////////
    // Common rules
    ////////////////////////////////////////////////////////////////////////

    // ==> u:a |- a
    //! \f[\frac{}{a\vdash a}\f]
    virtual Theorem assumpRule(const Expr& a, int scope = -1) = 0;

    //  ==> a == a   or   ==> a IFF a
    //! \f[\frac{}{a = a}\quad or \quad\frac{}{a \Leftrightarrow a}\f]
    virtual Theorem reflexivityRule(const Expr& a) = 0;

    //! ==> (a == a) IFF TRUE
    virtual Theorem rewriteReflexivity(const Expr& a_eq_a) = 0;

    //  a1 == a2 ==> a2 == a1 (same for IFF)
    //! \f[\frac{a_1=a_2}{a_2=a_1}\f] (same for IFF)
    virtual Theorem symmetryRule(const Theorem& a1_eq_a2) = 0;

    // ==> (a1 == a2) IFF (a2 == a1)
    //! \f[\frac{}{(a_1=a_2)\Leftrightarrow (a_2=a_1)}\f]
    virtual Theorem rewriteUsingSymmetry(const Expr& a1_eq_a2) = 0;

    // (a1 == a2) & (a2 == a3) ==> (a1 == a3) [same for IFF]
    //! \f[\frac{a_1=a_2\quad a_2=a_3}{a_1=a_3}\f] (same for IFF)
    virtual Theorem transitivityRule(const Theorem& a1_eq_a2,
                                     const Theorem& a2_eq_a3) = 0;

    //! Optimized case for expr with one child
    virtual Theorem substitutivityRule(const Expr& e, const Theorem& thm) = 0;

    //! Optimized case for expr with two children
    virtual Theorem substitutivityRule(const Expr& e, const Theorem& thm1,
                                       const Theorem& thm2) = 0;

    // (c_1 == d_1) & ... & (c_n == d_n)
    //   ==> op(c_1,...,c_n) == op(d_1,...,d_n)
    /*! @brief 
      \f[\frac{(c_1=d_1)\wedge\ldots\wedge(c_n=d_n)}
              {op(c_1,\ldots,c_n)=op(d_1,\ldots,d_n)}\f]
    */
    virtual Theorem substitutivityRule(const Op& op,
                                       const std::vector<Theorem>& thms) = 0;

    // (c_1 == d_1) & ... & (c_n == d_n)
    //   ==> op(c_1,...,c_n) == op(d_1,...,d_n)
    /*! @brief 
      \f[\frac{(c_1=d_1)\wedge\ldots\wedge(c_n=d_n)}
              {op(c_1,\ldots,c_n)=op(d_1,\ldots,d_n)}\f]
      except that only those arguments are given that \f$c_i\not=d_i\f$.
      \param e is the original expression \f$op(c_1,\ldots,c_n)\f$.
      \param changed is the vector of indices of changed kids
      \param thms are the theorems \f$c_i=d_i\f$ for the changed kids.
    */
    virtual Theorem substitutivityRule(const Expr& e,
                                       const std::vector<unsigned>& changed,
                                       const std::vector<Theorem>& thms) = 0;
    virtual Theorem substitutivityRule(const Expr& e, const int changed, const Theorem& thm) = 0;

    // |- e,  |- !e ==> |- FALSE
    /*! @brief
      \f[\frac{\Gamma_1\vdash e\quad\Gamma_2\vdash \neg e}
              {\Gamma_1\cup\Gamma_2\vdash \mathrm{FALSE}}
      \f]
     */
    virtual Theorem contradictionRule(const Theorem& e,
				      const Theorem& not_e) = 0;

    // |- e OR !e
    virtual Theorem excludedMiddle(const Expr& e) = 0;

    // e ==> e IFF TRUE
    //! \f[\frac{\Gamma\vdash e}{\Gamma\vdash e\Leftrightarrow\mathrm{TRUE}}\f]
    virtual Theorem iffTrue(const Theorem& e) = 0;

    // e ==> !e IFF FALSE
    //! \f[\frac{\Gamma\vdash e}{\Gamma\vdash\neg e\Leftrightarrow\mathrm{FALSE}}\f]
    virtual Theorem iffNotFalse(const Theorem& e) = 0;

    // e IFF TRUE ==> e
    //! \f[\frac{\Gamma\vdash e\Leftrightarrow\mathrm{TRUE}}{\Gamma\vdash e}\f]
    virtual Theorem iffTrueElim(const Theorem& e) = 0;

    // e IFF FALSE ==> !e
    //! \f[\frac{\Gamma\vdash e\Leftrightarrow\mathrm{FALSE}}{\Gamma\vdash\neg e}\f]
    virtual Theorem iffFalseElim(const Theorem& e) = 0;

    //! e1 <=> e2  ==>  ~e1 <=> ~e2
    /*!  \f[\frac{\Gamma\vdash e_1\Leftrightarrow e_2}
     *           {\Gamma\vdash\sim e_1\Leftrightarrow\sim e_2}\f]
     * Where ~e is the <em>inverse</em> of e (that is, ~(!e') = e').
     */
    virtual Theorem iffContrapositive(const Theorem& thm) = 0;

    // !!e ==> e
    //! \f[\frac{\Gamma\vdash\neg\neg e}{\Gamma\vdash e}\f]
    virtual Theorem notNotElim(const Theorem& not_not_e) = 0;

    // e1 AND (e1 IFF e2) ==> e2
    /*! @brief
      \f[\frac{\Gamma_1\vdash e_1\quad \Gamma_2\vdash(e_1\Leftrightarrow e_2)}
              {\Gamma_1\cup\Gamma_2\vdash e_2}
      \f]
    */
    virtual Theorem iffMP(const Theorem& e1, const Theorem& e1_iff_e2) = 0;

    // e1 AND (e1 IMPLIES e2) ==> e2
    /*! @brief
      \f[\frac{\Gamma_1\vdash e_1\quad \Gamma_2\vdash(e_1\Rightarrow e_2)}
              {\Gamma_1\cup\Gamma_2\vdash e_2}
      \f]
    */
    virtual Theorem implMP(const Theorem& e1, const Theorem& e1_impl_e2) = 0;

    // AND(e_1,...e_n) ==> e_i
    //! \f[\frac{\vdash e_1\wedge\cdots\wedge e_n}{\vdash e_i}\f]
    virtual Theorem andElim(const Theorem& e, int i) = 0;

    // e1, e2 ==> AND(e1, e2)
    /*! @brief
      \f[\frac{\Gamma_1\vdash e_1\quad \Gamma_2\vdash e_2}
              {\Gamma_1\cup\Gamma_2\vdash e_1\wedge e_2}
      \f]
    */
    virtual Theorem andIntro(const Theorem& e1, const Theorem& e2) = 0;

    // e1, ..., en ==> AND(e1, ..., en)
    /*! @brief
      \f[\frac{\Gamma_1\vdash e_1\quad \cdots \quad\Gamma_n\vdash e_n}
              {\bigcup_{i=1}^n\Gamma_i\vdash \bigwedge_{i=1}^n e_i}
      \f]
    */
    virtual Theorem andIntro(const std::vector<Theorem>& es) = 0;

    //  G,a1,...,an |- phi
    // -------------------------------------------------
    //    G |- (a1 & ... & an) -> phi
    /*!
     * @brief Implication introduction rule:
     * \f[\frac{\Gamma,\,\alpha_1\,\ldots,\,\alpha_n\vdash\phi}
     *         {\Gamma\vdash(\bigwedge_{i=1}^n\alpha_i)\to\phi}\f]
     *
     * \param phi is the formula \f$\phi\f$
     * \param assump is the vector of assumptions \f$\alpha_1\ldots\alpha_n\f$
     */
    virtual Theorem implIntro(const Theorem& phi,
			      const std::vector<Expr>& assump) = 0;

    //! e1 => e2  ==>  ~e2 => ~e1
    /*!  \f[\frac{\Gamma\vdash e_1\Rightarrow e_2}
     *           {\Gamma\vdash\sim e_2\Rightarrow\sim e_1}\f]
     * Where ~e is the <em>inverse</em> of e (that is, ~(!e') = e').
     */
    virtual Theorem implContrapositive(const Theorem& thm) = 0;

    //! ==> ITE(TRUE, e1, e2) == e1
    virtual Theorem rewriteIteTrue(const Expr& e) = 0;
    //! ==> ITE(FALSE, e1, e2) == e2
    virtual Theorem rewriteIteFalse(const Expr& e) = 0;
    //! ==> ITE(c, e, e) == e
    virtual Theorem rewriteIteSame(const Expr& e) = 0;

    // NOT e ==> e IFF FALSE
    //! \f[\frac{\vdash\neg e}{\vdash e\Leftrightarrow\mathrm{FALSE}}\f]
    virtual Theorem notToIff(const Theorem& not_e) = 0;

    // e1 XOR e2 ==> e1 IFF (NOT e2)
    //! \f[\frac{\vdash e_1 XOR e_2}{\vdash e_1\Leftrightarrow(\neg e_2)}\f]
    virtual Theorem xorToIff(const Expr& e) = 0;

    //! ==> (e1 <=> e2) <=> [simplified expr]
    /*! Rewrite formulas like FALSE/TRUE <=> e,  e <=> NOT e, etc. */
    virtual Theorem rewriteIff(const Expr& e) = 0;

    // AND and OR rewrites check for TRUE and FALSE arguments and
    // remove them or collapse the entire expression to TRUE and FALSE
    // appropriately

    //! ==> AND(e1,e2) IFF [simplified expr]
    virtual Theorem rewriteAnd(const Expr& e) = 0;

    //! ==> OR(e1,...,en) IFF [simplified expr]
    virtual Theorem rewriteOr(const Expr& e) = 0;

    //! ==> NOT TRUE IFF FALSE
    virtual Theorem rewriteNotTrue(const Expr& e) = 0;

    //! ==> NOT FALSE IFF TRUE
    virtual Theorem rewriteNotFalse(const Expr& e) = 0;

    //! ==> NOT NOT e IFF e, takes !!e
    virtual Theorem rewriteNotNot(const Expr& e) = 0;

    //! ==> NOT FORALL (vars): e  IFF EXISTS (vars) NOT e
    virtual Theorem rewriteNotForall(const Expr& forallExpr) = 0;

    //! ==> NOT EXISTS (vars): e  IFF FORALL (vars) NOT e
    virtual Theorem rewriteNotExists(const Expr& existsExpr) = 0;

    //From expr EXISTS(x1: t1, ..., xn: tn) phi(x1,...,cn)
    //we create phi(c1,...,cn) where ci is a skolem constant
    //defined by the original expression and the index i.
    virtual Expr skolemize(const Expr& e) = 0;

    /*! skolem rewrite rule: Introduces axiom |- Exists(x) phi(x) <=> phi(c)
     * where c is a constant defined by the expression Exists(x) phi(x)
     */
    virtual Theorem skolemizeRewrite(const Expr& e) = 0;

    //! Special version of skolemizeRewrite for "EXISTS x. t = x"
    virtual Theorem skolemizeRewriteVar(const Expr& e) = 0;

    //! |- EXISTS x. e = x
    virtual Theorem varIntroRule(const Expr& e) = 0;

    /*! @brief If thm is (EXISTS x: phi(x)), create the Skolemized version
      and add it to the database.  Otherwise returns just thm. */
    /*!
     * \param thm is the Theorem(EXISTS x: phi(x))
     */
    virtual Theorem skolemize(const Theorem& thm) = 0;

    //! Retrun a theorem "|- e = v" for a new Skolem constant v
    /*!
     * This is equivalent to skolemize(d_core->varIntroRule(e)), only more
     * efficient.
     */
    virtual Theorem varIntroSkolem(const Expr& e) = 0;

    // Derived rules

    //! ==> TRUE
    virtual Theorem trueTheorem() = 0;

    //! AND(e1,e2) ==> [simplified expr]
    virtual Theorem rewriteAnd(const Theorem& e) = 0;

    //! OR(e1,...,en) ==> [simplified expr]
    virtual Theorem rewriteOr(const Theorem& e) = 0;

    // TODO: do we really need this?
    virtual std::vector<Theorem>& getSkolemAxioms() = 0;

    //TODO: do we need this?
    virtual void clearSkolemAxioms() = 0;

    virtual Theorem ackermann(const Expr& e1, const Expr& e2) = 0;

    // Given a propositional atom containing embedded ite's, lifts first ite condition
    // to form a Boolean ITE
    // |- P(...ite(a,b,c)...) <=> ite(a,P(...b...),P(...c...))
    virtual Theorem liftOneITE(const Expr& e) = 0;

  }; // end of class CommonProofRules

} // end of namespace CVC3

#endif
