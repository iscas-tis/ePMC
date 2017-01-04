#ifndef CoCoA_JBSets_H
#define CoCoA_JBSets_H

#include <iostream>
#include <string>
#include "CoCoA/TmpJBDatastructure.H"
#include <set>
#include <list>
#include <bitset>
#include "CoCoA/FractionField.H"
#include "CoCoA/ReductionCog.H"

namespace CoCoA
{
  //typedef for easier writing
  typedef std::list<JanetTriple>::iterator Iterator;


  /*
  * JBSets stores the sets T and Q of our TQ-Degree algorithm. In our first version we store the
  * elements in two multisets. It turns out that the movement of elements from T to Q is very costly.
  * Therefore we create the class JBSets. This class has a basic Set which store all elements form T and Q.
  * The set T and Q contain only iterators of the basic Set. Now the movement of elements is very cheap.
  * In addition, the class contains some efficient reduction procedures (in comparison to the previous versions)
  */
  class JBSets
  {
  private:

    /*
    * inner class (only declaration)
    * for comparing elements in the set T and Q
    */
    class CompareIterator;

    friend class CompareIterator;

    /*
    * definition of the inner class
    */
    class CompareIterator
    {
    public:
      /**
       * This function compares two JanetTriples: i1 < i2 iff (i1.pol < i2.pol) || (i1.pol == i2.pol) && (i1.anc < i2.anc)
       * @param  i1 is the Iterator which points to the first JanetTriple
       * @param  i2 is the Iterator which points to the second JanetTriple
       * @return returns true if i1 < i2 otherwise false
       */
      bool operator() (const Iterator& i1, const Iterator& i2)
      {
      	const SparsePolyRingBase* myRing = SparsePolyRingPtr(owner(i1->myPolynom));
        const PPMonoid& myPPM(myRing->myPPM());
      	//	return (LPP(i1->myPolynom) < LPP(i2->myPolynom)) || ((LPP(i1->myPolynom) == LPP(i2->myPolynom)) && ((i1->myAncestor < i2->myAncestor) || ((i1->myAncestor == i2->myAncestor) && (((AsPolyRing(owner(i1->myPolynom))->myNumTerms(raw(i1->myPolynom))) < AsPolyRing(owner(i1->myPolynom))->myNumTerms(raw(i2->myPolynom))) || ((AsPolyRing(owner(i1->myPolynom))->myNumTerms(raw(i1->myPolynom)) == AsPolyRing(owner(i1->myPolynom))->myNumTerms(raw(i2->myPolynom))) && (abs(LC(i1->myPolynom)) < abs(LC(i2->myPolynom))))))));
      	//return (myRing->myLPP(raw(i1->myPolynom)) < myRing->myLPP(raw(i2->myPolynom))) ||((myRing->myLPP(raw(i1->myPolynom)) == myRing->myLPP(raw(i2->myPolynom))) && ((i1->myAncestor < i2->myAncestor) ));
      	const int comp(myPPM->myCmp(raw(myRing->myLPP(raw(i1->myPolynom))), raw(myRing->myLPP(raw(i2->myPolynom)))));
      	return (comp < 0) || (comp == 0 && (myPPM->myCmp(raw(i1->myAncestor), raw(i2->myAncestor)) < 0 ));
      	//	return (LPP(i1->myPolynom) < LPP(i2->myPolynom)) || ((LPP(i1->myPolynom) == LPP(i2->myPolynom)) && ((i1->myAncestor < i2->myAncestor) || ((i1->myAncestor == i2->myAncestor) && (((abs(LC(i1->myPolynom)) < abs(LC(i2->myPolynom))) || ((abs(LC(i1->myPolynom)) < abs(LC(i2->myPolynom))) &&((AsPolyRing(owner(i1->myPolynom))->myNumTerms(raw(i1->myPolynom))) < AsPolyRing(owner(i1->myPolynom))->myNumTerms(raw(i2->myPolynom)))))))));
      }
    };

    /*
    * our basic set 
    */
    std::list<JanetTriple> myBasicSet;

    /*
    * the set T
    */
    std::multiset<Iterator, CompareIterator> mySetT;

    /*
    * the set Q
    */
    std::multiset<Iterator, CompareIterator> mySetQ;

    /*
    * the set P
    */
    std::multiset<Iterator, CompareIterator> mySetP;
    
    /*
    * our poly Ring
    */
    SparsePolyRing myPolyRing;

    /*
    * which involutive criteria the algorithm shall use?
    */
    std::bitset<3> myCriteria;

    /*
    * which strategy should use blockTQ?  high strategy: true, low strategy: false
    */
    bool myHigh;

    /*
    * reduction cog to perform reductions
    */
    ReductionCog myReductionCog;


 
   /**
    * IAmCriteria checks if p satifies one of the selected criteria (in myCritieria) with respect to g
    * @param  p is the first JanetTriple
    * @param  g is the second JanetTriple
    * @return returns true if p satifies one of the selected criteria otherwiese false
    */
    bool IamCriteria(const JanetTriple& p, const JanetTriple& g) const;

   /**
    * IamCriteria3 checks if p (from IAmCriteria) satifies the third involutive Criteria with respect to g (from IAmCriteria)
    * @param  AncP is the ancestor of p
    * @param  AncG is the ancestor of g
    * @return returns true if AncP satifies the third criteria
    */
    bool IamCriteria3(const PPMonoidElem& AncP, const PPMonoidElem& AncG) const;
    
  public:

    /**
     * typedef
     */
    typedef std::multiset<Iterator,CompareIterator>::iterator MultisetIterator;


    /*
    * constructor
    */
    JBSets(SparsePolyRing ring, std::bitset<3> criteria): myPolyRing(ring), myCriteria(criteria), myHigh(false), myReductionCog(NewRedCogPolyField(myPolyRing))
    {
    }

    /*
    * constructor
    */
    JBSets(SparsePolyRing ring, std::bitset<3> criteria, bool updateHigh): myPolyRing(ring), myCriteria(criteria), myHigh(updateHigh), myReductionCog(NewRedCogPolyField(myPolyRing))
    {
    }

    /*
    * tests if the set mySetQ is empty
     */
    inline bool IamEmptySetQ() const
    {
      return mySetQ.empty();
    }

    /*
    * tests if the set mySetP is empty
    */
    inline bool IamEmptySetP() const
    {
      return mySetP.empty();
    }

   /**
    * myTailReduceFirstElemSetQ reduces the tail of the first element in the set Q with respect to JTree (involutive reduction!!!)
    * @param JTree is the JanetTree
    */
    void myTailReduceFirstElemSetQ(JanetTree& JTree);

   /**
    * myHeadRecueSetQ performs an involutive headreduction on every element in mySetQ with respect to the JanetTree
    * @param  JTree is the JanetTree
    * @return It returns the size of mySetQ after the headreduction 
    */
    long myHeadReduceSetQ(JanetTree& JTree); //done

   /**
    * myMinimizeAndInsertSetT select in the first step the smallest element in the multiset mySetQ 
    * (with respect to the number of terms and (if possible) the absolutue value of the leading coefficient)
    * Then the algorithm reduces the tail of the element and checks if the leading term of this element divides the leading
    * term of any element in mySetQT. If this is the case it moves all elements which are greater then the selected element to mySetQ.
    * At the end it inserts the selected element in mySetT and returns the Iterator to this element.
    * @param  JTree is the JanetTree
    * @return       see above
    */
    Iterator myMinimizeAndInsertSetT(JanetTree& JTree); 

   /**
    * myTailReduceSetT performs a involutive tailreduction on every element in mySetT which are bigger then TripleIter 
    * @param JTree      is the Janeet Tree
    * @param TripleIter an interator to a JanetTriple
    */
    void myTailReduceSetT(JanetTree& JTree, const std::list<JanetTriple>::iterator& TripleIter);


   /**
    * myTailReduceSetTAll performs a involutive tailreduction on every element in mySetT
    * @param JTree      is the Janeet Tree
    */
    void myTailReduceSetTAll(JanetTree& JTree);
    

    /*
    * returns iterator to the beginning/end of mySetT
    */
    MultisetIterator myBeginSetT() const; //done
    MultisetIterator myEndSetT() const; //done

    /*
    * returns iterator to the beginning/end of mySetQ
    */
    MultisetIterator myBeginSetQ() const;
    MultisetIterator myEndSetQ() const;

    /*
    * returns iterator to the beginning/end of mySetP
    */
    MultisetIterator myBeginSetP() const;
    MultisetIterator myEndSetP() const;


   /**
    * myInsertSetQ insert triple in myBasicSet and the corresponding Iterator in mySetQ
    * @param triple is a JanetTriple
    */
    void myInsertSetQ(JanetTriple& triple); //done


   /**
    * myMoveFromQtoP moves elements from mySetQ to mySetQ. It move all elements which have the lowest StdDeg.
    * The function also perorms a full involutive reduction at these elements.
    * @param JTree is the JanetTree
    */
    void myMoveFromQtoP(JanetTree& JTree);

   /**
    * myMinimizeSetT moves all elements from mySetT to mySetQ, which have a smaller StdDeg as the beginning element of mySetP
    */
    void myMinimizeSetT();

   /**
    * myTailReuceSetP performs a involutive tailreduction on every element in mySetP
    * @param JTree      is the Janeet Tree
    */
    void myTailReduceSetP(JanetTree& JTree);

   /**
    * myInsertSetPInSetT moves all elements from mySetP to mySetQ
    */
    void myInsertSetPInSetT();

    /*
    * Update of the block P. True if there is still a prolongation in the block
    */
   
   /**
    * IamJUpdate updates mySetP.
    * In IamUpdate an element f in mySetP (which is J−reduced modulo mySetT when the subalgorithm is invoked in the main algorithm) 
    * is chosen at the initialization with the highest or lowest leading term, depends on the selection strategy used.
    * We put f in the set stack 
    * After that, the other polynomial p in mySetP, if any, with the same leading monomial as that in f is reduced modulo f. 
    * In the case of nonzero reduction (when monic p is different from monic f) the normal form obtained is added to the stack 
    * to be involved in the further reductions. 
    * Then, the processes of the selection and reduction of elements in mySetP is repeated until mySetP becomes empty. 
    * At the end we copy the content of stack back to mySetP
    * @return true if there is still a prolongation in mySetP
    */
    bool IamJUpdate();

    /**
     * Performs the process described for the function IamJUpdate() choosing always the biggest element in mySetP
     * after each reduction process we have to recompute the order in mySetP.
     * @param stack is the set described in IamJUpdate
     */
    void myJUpdateHighPart(std::list<Iterator>& stack);


    /**
     * Performs the process described for the function IamJUpdate() choosing always the smalles element in mySetP
     * after each reduction process we have to recompute the order in mySetP.
     * @param stack is the set described in IamJUpdate
     */
    void myJUpdateLowPart(std::list<Iterator>& stack);

    /**
     * This function refines the search for the smallest/highest element. 
     * They also observed the number of terms and (if ring is an ordered domain) the leading coefficient
     * @param ChooseIter (Call-by-ref) the iterator pointing to the choosen element
     */
    void myChoosingJUpdate(JBSets::MultisetIterator& ChooseIter);

    /**
     * Performs a reduction on PIter. Adepting the ancestor an nonmultiplicative variables
     * @param PIter         iterator to be reduced
     * @param ReductionIter reducing iterator
     */
    void myReductionStepJUpdate(MultisetIterator& PIter, std::list<Iterator>::iterator ReductionIter);

    /**
     * The reduction step myReductionStepJUpdate destroies the ordering in mySetP. 
     * This function recomputes the ordering.
     */
    void myRecomputeOrderingSetP();

    /**
     * 
     * @return returns size of mySetT
     */
    long mySizeSetT();

   /**
    * myJTailNormalForm computes the involutive tail normalform of iter w.r.t JTree
    * @param JTree the janet tree
    * @param iter  the iterator which contains a janettriple
    */
    void myJTailNormalForm(JanetTree& JTree, Iterator iter);

   /**
    * myJFullNormalForm computes the involutive full normalform of iter w.r.t JTree
    * @param JTree the janet tree
    * @param iter  the iterator which contains a janettriple
    */
    void myJFullNormalForm(JanetTree& JTree, Iterator iter);

    /**
     * myTailNormalForm computes the tail normal form of ToRed w.r.t. red (which is only one element!!!)
     * @param ToRed the iterator which contains a janettriple which we want to reduce
     * @param red   the iterator which contains a janettriple which reduces the triple above
     */
    void myTailNormalForm(Iterator ToRed, Iterator red);

    /**
    * myOneLeafTree create a JanetTree from w (with starting variable i) to ps
    * @param  ps is an iterator to a JanetTriple
    * @param  i  is the starting variable
    * @param  w  is the starting monomial
    * @return returns the JanetTree which is beginning in m and ending in ps
    */
    JanetTree myOneLeafTree(const std::list<JanetTriple>::iterator& ps, long i, PPMonoidElem w);

    /**
     * insert the triple which is in ps in the JanetTree and prolong.
     */
    void myJInsert(std::list<JanetTriple>::iterator& ps, JanetTree& JTree); 

    /**
     *This algorithm prolong all triples which are behind iter with x_index and insert the prolongation into setQ
     */
    void myJProlong(JanetIterator& iter, const long& index);

   /**
    * myJInsertWithoutProlong insert the JanetTriple ps into the JanetTree JTree but doesn't perform any prolongations
    * @param JTree the JanetTree where we insert the triple
    * @param ps    the Itertor to the triple which we want to insert
    */
    void myJInsertWithoutProlong(JanetTree& JTree, const std::list<JanetTriple>::iterator& ps); 

  };


}
#endif
