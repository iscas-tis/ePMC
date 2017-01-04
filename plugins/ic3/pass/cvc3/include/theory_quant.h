/*****************************************************************************/
/*!
 * \file theory_quant.h
 * 
 * Author: Sergey Berezin, Yeting Ge
 * 
 * Created: Jun 24 21:13:59 GMT 2003
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
 * ! Author: Daniel Wichs
 * ! Created: Wednesday July 2, 2003
 *
 * 
 */
/*****************************************************************************/
#ifndef _cvc3__include__theory_quant_h_
#define _cvc3__include__theory_quant_h_

#include "theory.h"
#include "cdmap.h"
#include "statistics.h"
#include<queue>

namespace CVC3 {

class QuantProofRules;

/*****************************************************************************/
/*!
 *\class TheoryQuant
 *\ingroup Theories
 *\brief This theory handles quantifiers.
 *
 * Author: Daniel Wichs
 *
 * Created: Wednesday July 2,  2003
 */
/*****************************************************************************/

 typedef enum{ Ukn, Pos, Neg, PosNeg} Polarity;

 class Trigger {
 public: 
   Expr trig;
   Polarity polarity;
   
   std::vector<Expr> bvs;
   Expr head;
   bool hasRWOp;
   bool hasTrans;
   bool hasT2; //if has trans of 2,
   bool isSimple; //if of the form g(x,a);
   bool isSuperSimple; //if of the form g(x,y);
   bool isMulti;
   size_t multiIndex; 
   size_t multiId;

   Trigger(TheoryCore* core, Expr e, Polarity pol, std::set<Expr>);
   bool  isPos();
   bool  isNeg();
   Expr  getEx();
   std::vector<Expr> getBVs(); 
   void  setHead(Expr h);
   Expr  getHead();
   void  setRWOp(bool b);
   bool  hasRW(); 
   void  setTrans(bool b);
   bool  hasTr(); 
   void  setTrans2(bool b);
   bool  hasTr2(); 
   void  setSimp();
   bool  isSimp(); 
   void  setSuperSimp();
   bool  isSuperSimp(); 
   void  setMultiTrig();
   bool  isMultiTrig(); 

   
 };

 typedef struct dynTrig{
   Trigger trig;
   size_t univ_id;
   ExprMap<Expr> binds;
   dynTrig(Trigger t, ExprMap<Expr> b, size_t id);
 } dynTrig;
 

 class CompleteInstPreProcessor {
 
   TheoryCore* d_theoryCore; //needed by plusOne and minusOne;
   QuantProofRules* d_quant_rules;

   std::set<Expr> d_allIndex; //a set contains all index

   ExprMap<Polarity> d_expr_pol ;//map a expr to its polarity

   ExprMap<Expr> d_quant_equiv_map ; //map a quant to its equivalent form

   std::vector<Expr> d_gnd_cache; //cache of all ground formulas, before index can be collected, all such ground terms must be put into d_expr_pol.
   
   ExprMap<bool> d_is_macro_def;//if a quant is a macro quant

   ExprMap<Expr> d_macro_quant;//map a macro to its macro quant.

   ExprMap<Expr> d_macro_def;//map a macro head to its def.

   ExprMap<Expr> d_macro_lhs;//map a macro to its lhs.
   
   //! if all formulas checked so far are good
   bool d_all_good ;

   //! if e satisfies the shiled property, that is all bound vars are parameters of uninterpreted functions/predicates and array reads/writes
   bool isShield(const Expr& e);

   //! insert an index
   void addIndex(const Expr& e);
   
   void collect_shield_index(const Expr& e);

   void collect_forall_index(const Expr& forall_quant);

   //! if e is a quant in the array property fragmenet
   bool isGoodQuant(const Expr& e);

   //! return e+1
   Expr plusOne(const Expr& e);

   //! return e-1
   Expr minusOne(const Expr& e);

   void collectHeads(const Expr& assert, std::set<Expr>& heads);
   
   //! if assert is a macro definition
   bool isMacro(const Expr& assert);

   Expr recInstMacros(const Expr& assert);

   Expr substMacro(const Expr&);

   Expr recRewriteNot(const Expr&, ExprMap<Polarity>&);

   //! rewrite neg polarity forall / exists to pos polarity
   Expr rewriteNot(const Expr &);
   
   Expr recSkolemize(const Expr &, ExprMap<Polarity>&);

   Expr pullVarOut(const Expr&);

 public :

   CompleteInstPreProcessor(TheoryCore * , QuantProofRules*);

   //! if e is a formula in the array property fragment 
   bool isGood(const Expr& e);

   //! collect index for instantiation
   void collectIndex(const Expr & e);

   //! inst forall quant using index from collectIndex
   Expr inst(const Expr & e);

   //! if there are macros
   bool hasMacros(const std::vector<Expr>& asserts);

   //! substitute a macro in assert
   Expr instMacros(const Expr& , const Expr );

   //! simplify a=a to True
   Expr simplifyEq(const Expr &);
   
   //! put all quants in postive form and then skolemize all exists 
   Expr simplifyQuant(const Expr &);
 };
 
 class TheoryQuant :public Theory {
   
   Theorem rewrite(const Expr& e);

   Theorem theoryPreprocess(const Expr& e);

   class  TypeComp { //!< needed for typeMap
   public:
     bool operator() (const Type t1, const Type t2) const
     {return (t1.getExpr() < t2.getExpr()); }
   };

  //! used to facilitate instantiation of universal quantifiers
  typedef std::map<Type, std::vector<size_t>, TypeComp > typeMap; 

  //! database of universally quantified theorems
  CDList<Theorem> d_univs; 

  CDList<Theorem> d_rawUnivs; 

  CDList<dynTrig> d_arrayTrigs; 
  CDO<size_t> d_lastArrayPos;

  //! universally quantified formulas to be instantiated, the var bindings is in d_bingQueue and the ground term matched with the trigger is in d_gtermQueue 
  std::queue<Theorem> d_univsQueue;

  std::queue<Theorem> d_simplifiedThmQueue;

  std::queue<Theorem> d_gUnivQueue;
  
  std::queue<Expr> d_gBindQueue;


  ExprMap<std::set<std::vector<Expr> > >  d_tempBinds;

  //!tracks the possition of preds 
  CDO<size_t> d_lastPredsPos;
  //!tracks the possition of terms 
  CDO<size_t> d_lastTermsPos;

  //!tracks the positions of preds for partial instantiation
  CDO<size_t> d_lastPartPredsPos;
  //!tracks the possition of terms for partial instantiation
  CDO<size_t> d_lastPartTermsPos;
  //!tracks a possition in the database of universals for partial instantiation
  CDO<size_t> d_univsPartSavedPos;
  
  //! the last decision level on which partial instantion is called
  CDO<size_t> d_lastPartLevel;

  CDO<bool> d_partCalled;
  
  //! the max instantiation level reached
  CDO<bool> d_maxILReached;


  
  //!useful gterms for matching
  CDList<Expr> d_usefulGterms; 

  //!tracks the position in d_usefulGterms
  CDO<size_t> d_lastUsefulGtermsPos;
  
  //!tracks a possition in the savedTerms map
  CDO<size_t> d_savedTermsPos;
  //!tracks a possition in the database of universals
  CDO<size_t> d_univsSavedPos;
  CDO<size_t> d_rawUnivsSavedPos;
  //!tracks a possition in the database of universals
  CDO<size_t> d_univsPosFull;
  //!tracks a possition in the database of universals if fulleffort mode, the d_univsSavedPos now uesed when fulleffort=0 only.

  CDO<size_t> d_univsContextPos;
  
  
  CDO<int> d_instCount; //!< number of instantiations made in given context

  //! set if the fullEffort = 1
  int d_inEnd; 

  int d_allout; 

  //! a map of types to posisitions in the d_contextTerms list
  std::map<Type, CDList<size_t>* ,TypeComp> d_contextMap;
  //! a list of all the terms appearing in the current context
  CDList<Expr> d_contextTerms;
  //!< chache of expressions
  CDMap<Expr, bool> d_contextCache;
  
  //! a map of types to positions in the d_savedTerms vector
  typeMap d_savedMap;
  ExprMap<bool> d_savedCache; //!< cache of expressions
  //! a vector of all of the terms that have produced conflicts.
  std::vector<Expr> d_savedTerms; 

  //! a map of instantiated universals to a vector of their instantiations
  ExprMap<std::vector<Expr> >  d_insts;

  //! quantifier theorem production rules
  QuantProofRules* d_rules;
  
  const int* d_maxQuantInst; //!< Command line option

  /*! \brief categorizes all the terms contained in an expressions by
   *type.
   *
   * Updates d_contextTerms, d_contextMap, d_contextCache accordingly.
   * returns true if the expression does not contain bound variables, false
   * otherwise.
   */
  bool recursiveMap(const Expr& term);

  /*! \brief categorizes all the terms contained in a vector of  expressions by
   * type.
   *
   * Updates d_contextTerms, d_contextMap, d_contextCache accordingly.
   */
  void mapTermsByType(const CDList<Expr>& terms);
  
  /*! \brief Queues up all possible instantiations of bound
   * variables.
   *
   * The savedMap boolean indicates whether to use savedMap or
   * d_contextMap the all boolean indicates weather to use all
   * instantiation or only new ones and newIndex is the index where
   * new instantiations begin.
   */
  void instantiate(Theorem univ, bool all, bool savedMap, 
		   size_t newIndex);
  //! does most of the work of the instantiate function.
  void recInstantiate(Theorem& univ , bool all, bool savedMap,size_t newIndex, 
				   std::vector<Expr>& varReplacements);

  /*! \brief A recursive function used to find instantiated universals
   * in the hierarchy of assumptions.
   */
  void findInstAssumptions(const Theorem& thm);

  //  CDO<bool> usedup;
  const bool* d_useNew;//!use new way of instantiation
  const bool* d_useLazyInst;//!use lazy instantiation
  const bool* d_useSemMatch;//!use semantic matching
  const bool* d_useCompleteInst; //! Try complete instantiation 
  const bool* d_translate;//!translate only

  const bool* d_usePart;//!use partial instantiaion
  const bool* d_useMult;//use 
  //  const bool* d_useInstEnd;
  const bool* d_useInstLCache;
  const bool* d_useInstGCache;
  const bool* d_useInstThmCache;
  const bool* d_useInstTrue;
  const bool* d_usePullVar;
  const bool* d_useExprScore;
  const int* d_useTrigLoop;
  const int* d_maxInst;
  //  const int* d_maxUserScore;
  const int*  d_maxIL;
  const bool* d_useTrans;
  const bool* d_useTrans2;
  const bool* d_useManTrig;
  const bool* d_useGFact;
  const int* d_gfactLimit;
  const bool* d_useInstAll;
  const bool* d_usePolarity;
  const bool* d_useEqu;
  const bool* d_useNewEqu;
  const int* d_maxNaiveCall;
  const bool* d_useNaiveInst;


  CDO<int> d_curMaxExprScore;

  bool d_useFullTrig;
  bool d_usePartTrig;
  bool d_useMultTrig;

  //ExprMap<std::vector<Expr> > d_arrayIndic; //map array name to a list of indics
  CDMap<Expr, std::vector<Expr> > d_arrayIndic; //map array name to a list of indics
  void arrayIndexName(const Expr& e);

  std::vector<Expr> d_allInsts; //! all instantiations

  int d_initMaxScore;
  int d_offset_multi_trig ;
  
  int d_instThisRound;
  int d_callThisRound;

  int partial_called;

  //  ExprMap<std::vector<Expr> > d_fullTriggers;
  //for multi-triggers, now we only have one set of multi-triggers.


  ExprMap<std::vector<Expr> > d_multTriggers;
  ExprMap<std::vector<Expr> > d_partTriggers;

  ExprMap<std::vector<Trigger> > d_fullTrigs;
  //for multi-triggers, now we only have one set of multi-triggers.
  ExprMap<std::vector<Trigger> > d_multTrigs;
  ExprMap<std::vector<Trigger> > d_partTrigs;

 
  CDO<size_t> d_exprLastUpdatedPos ;//the position of the last expr updated in d_exprUpdate 

  std::map<ExprIndex, int> d_indexScore;

  std::map<ExprIndex, Expr> d_indexExpr;

  int getExprScore(const Expr& e);

  //!the score for a full trigger
  
  ExprMap<bool> d_hasTriggers;
  ExprMap<bool> d_hasMoreBVs;

  int d_trans_num;
  int d_trans2_num;

  typedef struct{
    std::vector<std::vector<size_t> > common_pos;
    std::vector<std::vector<size_t> > var_pos; 
    
    std::vector<CDMap<Expr, bool>* > var_binds_found;

    std::vector<ExprMap<CDList<Expr>* >* > uncomm_list; //
    Theorem univThm; // for test only
    size_t univ_id; // for test only
  } multTrigsInfo ;
  
  ExprMap<multTrigsInfo> d_multitrigs_maps;
  std::vector<multTrigsInfo> d_all_multTrigsInfo;
  
  ExprMap<CDList<Expr>* > d_trans_back;
  ExprMap<CDList<Expr>* > d_trans_forw;
  CDMap<Expr,bool > d_trans_found;
  CDMap<Expr,bool > d_trans2_found;


  inline  bool transFound(const Expr& comb);
  
  inline   void setTransFound(const Expr& comb);

  inline  bool trans2Found(const Expr& comb);

  inline  void setTrans2Found(const Expr& comb);

 
  inline  CDList<Expr> & backList(const Expr& ex);
  
  inline  CDList<Expr> & forwList(const Expr& ex);

  void inline iterFWList(const Expr& sr, const Expr& dt, size_t univ_id, const Expr& gterm);
  void inline iterBKList(const Expr& sr, const Expr& dt, size_t univ_id, const Expr& gterm);

  Expr defaultWriteExpr;
  Expr defaultReadExpr;
  Expr defaultPlusExpr;
  Expr  defaultMinusExpr ;
  Expr  defaultMultExpr ;
  Expr  defaultDivideExpr;
  Expr  defaultPowExpr ;

  Expr getHead(const Expr& e) ;
  Expr getHeadExpr(const Expr& e) ;



  CDList<Expr> null_cdlist;

  Theorem d_transThm;

  inline  void  pushBackList(const Expr& node, Expr ex);
  inline  void  pushForwList(const Expr& node, Expr ex);
  
  
  ExprMap<CDList<std::vector<Expr> >* > d_mtrigs_inst; //map expr to bindings
  
  ExprMap<CDList<Expr>* > d_same_head_expr; //map an expr to a list of expres shard the same head
  ExprMap<CDList<Expr>* > d_eq_list; //the equalities list

  CDList<Theorem> d_eqsUpdate; //the equalities list collected from update()
  CDO<size_t> d_lastEqsUpdatePos;

  CDList<Expr > d_eqs; //the equalities list
  CDO<size_t > d_eqs_pos; //the equalities list

  ExprMap<CDO<size_t>* > d_eq_pos;

  ExprMap<CDList<Expr>* > d_parent_list; 
  void  collectChangedTerms(CDList<Expr>& changed_terms);
  ExprMap<std::vector<Expr> > d_mtrigs_bvorder;

  int loc_gterm(const std::vector<Expr>& border,
		const Expr& gterm, 
		int pos);

  void  recSearchCover(const std::vector<Expr>& border,
		       const std::vector<Expr>& mtrigs, 
		       int cur_depth, 
		       std::vector<std::vector<Expr> >& instSet,
		       std::vector<Expr>& cur_inst
		       );

  void  searchCover(const Expr& thm, 
		    const std::vector<Expr>& border,
		    std::vector<std::vector<Expr> >& instSet
		    );


  std::map<Type, std::vector<Expr>,TypeComp > d_typeExprMap;
  std::set<std::string> cacheHead;

  StatCounter d_allInstCount ; //the number instantiations asserted in SAT
  StatCounter d_allInstCount2 ; 
  StatCounter d_totalInstCount ;// the total number of instantiations.
  StatCounter d_trueInstCount;//the number of instantiation simplified to be true.
  StatCounter d_abInstCount;

  //  size_t d_totalInstCount;
  //  size_t d_trueInstCount;
  //  size_t d_abInstCount;
  


  std::vector<Theorem> d_cacheTheorem;
  size_t d_cacheThmPos;

  void addNotify(const Expr& e);

  int sendInstNew();

  CDMap<Expr, std::set<std::vector<Expr> > > d_instHistory;//the history of instantiations
  //map univ to the trig, gterm and result

  ExprMap<int> d_thmCount; 
  ExprMap<size_t> d_totalThmCount; 

  ExprMap<CDMap<Expr, bool>* > d_bindHistory; //the history of instantiations
  ExprMap<std::hash_map<Expr, bool>* > d_bindGlobalHistory; //the history of instantiations

  ExprMap<std::hash_map<Expr, Theorem>* > d_bindGlobalThmHistory; //the history of instantiations

  ExprMap<std::set<std::vector<Expr> > > d_instHistoryGlobal;//the history of instantiations

  
  ExprMap<std::vector<Expr> > d_subTermsMap;
  //std::map<Expr, std::vector<Expr> > d_subTermsMap;
  const std::vector<Expr>& getSubTerms(const Expr& e);


  void simplifyExprMap(ExprMap<Expr>& orgExprMap);
  void simplifyVectorExprMap(std::vector<ExprMap<Expr> >& orgVectorExprMap);
  std::string exprMap2string(const ExprMap<Expr>& vec);
  std::string exprMap2stringSimplify(const ExprMap<Expr>& vec);
  std::string exprMap2stringSig(const ExprMap<Expr>& vec);

  //ExprMap<int > d_thmTimes; 
  void enqueueInst(const Theorem, const Theorem); 
  void enqueueInst(const Theorem& univ, const std::vector<Expr>& bind, const Expr& gterm);
  void enqueueInst(size_t univ_id , const std::vector<Expr>& bind, const Expr& gterm);
  void enqueueInst(const Theorem& univ, 
		   Trigger& trig,
		   const std::vector<Expr>& binds,  
		   const Expr& gterm
		   );
    
  void synCheckSat(ExprMap<ExprMap<std::vector<dynTrig>* >* >& , bool);
  void synCheckSat(bool);
  void semCheckSat(bool);
  void naiveCheckSat(bool);

  bool insted(const Theorem & univ, const std::vector<Expr>& binds);
  void synInst(const Theorem & univ,  const CDList<Expr>& allterms, size_t tBegin);

  void synFullInst(const Theorem & univ,  const CDList<Expr>& allterms,	size_t tBegin);

  void arrayHeuristic(const Trigger& trig, size_t univid);
  
  Expr simpRAWList(const Expr& org);

  void synNewInst(size_t univ_id, const std::vector<Expr>& binds, const Expr& gterm, const Trigger& trig );
  void synMultInst(const Theorem & univ,  const CDList<Expr>& allterms,	 size_t tBegin);

  void synPartInst(const Theorem & univ,  const CDList<Expr>& allterms,	 size_t tBegin);

  void semInst(const Theorem & univ, size_t tBegin);


  void goodSynMatch(const Expr& e,
		    const std::vector<Expr> & boundVars,
		    std::vector<std::vector<Expr> >& instBindsTerm,
		    std::vector<Expr>& instGterm,
		    const CDList<Expr>& allterms,		       
		    size_t tBegin);
  void goodSynMatchNewTrig(const Trigger& trig,
			   const std::vector<Expr> & boundVars,
			   std::vector<std::vector<Expr> >& instBinds,
			   std::vector<Expr>& instGterms,
			   const CDList<Expr>& allterms,		       
			   size_t tBegin);

  bool goodSynMatchNewTrig(const Trigger& trig,
			   const std::vector<Expr> & boundVars,
			   std::vector<std::vector<Expr> >& instBinds,
			   std::vector<Expr>& instGterms,
			   const Expr& gterm);

  void matchListOld(const CDList<Expr>& list, size_t gbegin, size_t gend);
    //  void matchListOld(const Expr& gterm);
  void matchListNew(ExprMap<ExprMap<std::vector<dynTrig>*>*>& new_trigs,
		    const CDList<Expr>& list,
		    size_t gbegin,
		    size_t gend);
  
  void delNewTrigs(ExprMap<ExprMap<std::vector<dynTrig>*>*>& new_trigs);
  void combineOldNewTrigs(ExprMap<ExprMap<std::vector<dynTrig>*>*>& new_trigs);

  inline void add_parent(const Expr& parent);

  void newTopMatch(const Expr& gterm, 
		   const Expr& vterm, 
		   std::vector<ExprMap<Expr> >& binds, 
		   const Trigger& trig);

  void newTopMatchSig(const Expr& gterm, 
		      const Expr& vterm, 
		      std::vector<ExprMap<Expr> >& binds, 
		      const Trigger& trig);
  
  void newTopMatchNoSig(const Expr& gterm, 
			const Expr& vterm, 
			std::vector<ExprMap<Expr> >& binds, 
			const Trigger& trig);



  void newTopMatchBackupOnly(const Expr& gterm, 
			     const Expr& vterm, 
			     std::vector<ExprMap<Expr> >& binds, 
			     const Trigger& trig);


  bool synMatchTopPred(const Expr& gterm, const Trigger trig, ExprMap<Expr>& env);

  //  inline bool matchChild(const Expr& gterm, const Expr& vterm, ExprMap<Expr>& env);
  //  inline void matchChild(const Expr& gterm, const Expr& vterm, std::vector<ExprMap<Expr> >& env);
  
  bool recSynMatch(const Expr& gterm, const Expr& vterm, ExprMap<Expr>& env);

  bool recMultMatch(const Expr& gterm,const Expr& vterm, std::vector<ExprMap<Expr> >& binds);
  bool recMultMatchDebug(const Expr& gterm,const Expr& vterm, std::vector<ExprMap<Expr> >& binds);
  bool recMultMatchNewWay(const Expr& gterm,const Expr& vterm, std::vector<ExprMap<Expr> >& binds);
  bool recMultMatchOldWay(const Expr& gterm,const Expr& vterm, std::vector<ExprMap<Expr> >& binds);

  inline bool multMatchChild(const Expr& gterm, const Expr& vterm, std::vector<ExprMap<Expr> >& binds, bool top=false);
  inline bool multMatchTop(const Expr& gterm, const Expr& vterm, std::vector<ExprMap<Expr> >& binds);


  bool recSynMatchBackupOnly(const Expr& gterm, const Expr& vterm, ExprMap<Expr>& env);

  bool hasGoodSynInstNewTrigOld(Trigger& trig, 
				std::vector<Expr> & boundVars, 
				std::vector<std::vector<Expr> >& instBinds, 
				std::vector<Expr>& instGterms, 
				const CDList<Expr>& allterms,		       
				size_t tBegin); 
  
  bool hasGoodSynInstNewTrig(Trigger& trig, 
			     const std::vector<Expr> & boundVars, 
   			     std::vector<std::vector<Expr> >& instBinds, 
 			     std::vector<Expr>& instGterms, 
 			     const CDList<Expr>& allterms,		       
 			     size_t tBegin); 


  bool hasGoodSynMultiInst(const Expr& e,
			   std::vector<Expr>& bVars,
			   std::vector<std::vector<Expr> >& instSet,
			   const CDList<Expr>& allterms,		       
			   size_t tBegin);
  
  void recGoodSemMatch(const Expr& e,
		       const std::vector<Expr>& bVars,
		       std::vector<Expr>& newInst,
		       std::set<std::vector<Expr> >& instSet);
  
  bool hasGoodSemInst(const Expr& e,
		   std::vector<Expr>& bVars,
		   std::set<std::vector<Expr> >& instSet,
		   size_t tBegin);

  bool isTransLike (const std::vector<Expr>& cur_trig);
  bool isTrans2Like (const std::vector<Expr>& all_terms, const Expr& tr2);
  

  static const size_t MAX_TRIG_BVS=15;
  Expr d_mybvs[MAX_TRIG_BVS];
 
  Expr recGeneralTrig(const Expr& trig, ExprMap<Expr>& bvs, size_t& mybvs_count);
  Expr generalTrig(const Expr& trig, ExprMap<Expr>& bvs);

  ExprMap<CDMap<Expr, CDList<dynTrig>* >* > d_allmap_trigs;
  
  CDList<Trigger> d_alltrig_list;

  void registerTrig(ExprMap<ExprMap<std::vector<dynTrig>* >* >& cur_trig_map,
		   Trigger trig, 
		   const std::vector<Expr> thmBVs, 
		   size_t univ_id);
    
  void registerTrigReal(Trigger trig, const std::vector<Expr>, size_t univ_id);

  bool canMatch(const Expr& t1, const Expr& t2, ExprMap<Expr>& env);
  void setGround(const Expr& gterm, const Expr& trig, const Theorem& univ, const std::vector<Expr>& subTerms) ;    

  //  std::string getHead(const Expr& e) ;
  void setupTriggers(ExprMap<ExprMap<std::vector<dynTrig>* >*>& trig_maps,
		     const Theorem& thm, 
		     size_t univs_id);

  void saveContext();


  /*! \brief categorizes all the terms contained in an expressions by
   *type.
   *
   * Updates d_contextTerms, d_contextMap, d_contextCache accordingly.
   * returns true if the expression does not contain bound variables, false
   * otherwise.
   */

  
 public:
  TheoryQuant(TheoryCore* core); //!< Constructor
  ~TheoryQuant(); //! Destructor

  QuantProofRules* createProofRules();
  

 
  void addSharedTerm(const Expr& e) {} //!< Theory interface
  
  /*! \brief Theory interface function to assert quantified formulas
   *
   * pushes in negations and converts to either universally or existentially 
   * quantified theorems. Universals are stored in a database while 
   * existentials are enqueued to be handled by the search engine.
   */
  void assertFact(const Theorem& e); 
  

  /* \brief Checks the satisfiability of the universal theorems stored in a 
   * databse by instantiating them.
   *
   * There are two algorithms that the checkSat function uses to find 
   * instnatiations. The first algortihm looks for instanitations in a saved 
   * database of previous instantiations that worked in proving an earlier
   * theorem unsatifiable. All of the class variables with the word saved in
   * them  are for the use of this algorithm. The other algorithm uses terms 
   * found in the assertions that exist in the particular context when 
   * checkSat is called. All of the class variables with the word context in
   * them are used for the second algorithm.
   */
  void checkSat(bool fullEffort);
  void setup(const Expr& e); 
  
  int help(int i);
  
  void update(const Theorem& e, const Expr& d);
  /*!\brief Used to notify the quantifier algorithm of possible 
   * instantiations that were used in proving a context inconsistent.
   */
  void debug(int i);
  void notifyInconsistent(const Theorem& thm); 
  //! computes the type of a quantified term. Always a  boolean.
  void computeType(const Expr& e); 
  Expr computeTCC(const Expr& e);
  
  virtual Expr parseExprOp(const Expr& e);

  ExprStream& print(ExprStream& os, const Expr& e);
 };
 
}

#endif
