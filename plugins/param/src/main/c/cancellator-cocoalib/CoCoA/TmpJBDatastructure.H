#ifndef CoCoA_JBDatastructure_H
#define CoCoA_JBDatastructure_H

#include <list>
#include <algorithm>
#include "CoCoA/PPMonoid.H"
#include "CoCoA/SparsePolyRing.H"
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>

namespace CoCoA
{



  //forward Declaration
  class JanetHandle;
  class JanetIterator;
 
  /****************************************************************************************************************************/
  /*!\brief This class stores the JanetTriple. It contains the myPolynom
   *  p, its ancestor, and its nonmultiplicative variables
   *
   * This class stores the JanetTriple. It contains the polynom p, its
   * ancestor, and its nonmultiplicative variables
   */
  /*****************************************************************************************************************************/
  class JanetTriple
  {

    friend struct isLower;
    friend struct IsLowerLPP;
    friend class JBSets;
  
  public:
    /**
     * The constructor of the JanetTriple.
     * @param pol a RingElem for the polynom
     * @param anc a PPMonoid for the ancestor
     * The JanetTriple doesn't contain any nonmultiplicative variables
     **/
    JanetTriple(ConstRefRingElem pol, ConstRefPPMonoidElem anc):
      myPolynom(pol), myAncestor(anc), myNonMultiplicatives(NumIndets(owner(pol)), false)
    {  
    }

    /**
     * The constructor of the JanetTriple.
     * @param pol a RingElem for the polynom
     * The ancestor is the LPP of polynom.
     * The JanetTriple doesn't contain any nonmultiplicative variables
     **/
    JanetTriple(ConstRefRingElem  pol): myPolynom(pol), myAncestor(LPP(pol)), myNonMultiplicatives(NumIndets(owner(pol)), false)
    {
    }

    /** The constructor of the JanetTriple
     *	@param pol the new polynom
     *	@param anc the new ancestor
     *	@param nm the nonmultiplicatives Variables as std::vector<bool>
     */
    JanetTriple(ConstRefRingElem pol, ConstRefPPMonoidElem anc, std::vector<bool> nm):myPolynom(pol), myAncestor(anc), myNonMultiplicatives(nm)
    {
    }

    /**
     * This function changes the polynomial of the JanetTriple
     * @param pol the new polynomial
     */
    inline void mySetPol(ConstRefRingElem pol)
    {
      myPolynom = pol;
    }

    /**
     * This function changes the ancestor of the JanetTriple
     * @param anc the the ancestor
     */
    inline void mySetAnc(ConstRefPPMonoidElem anc)
    {
      myAncestor = anc;
    }

    /**
     * This function sets a nonmultiplicative variable
     * @param var becomes nonmultiplicative
     * If var is already a nonmultiplicative variable nothing will happen
     */
    void mySetNM(ConstRefPPMonoidElem var);

    /**
     * This function sets a nonmultiplicative variable
     * @param index is the index of the variable which shall be nonmultiplicative
     * If the variable is already nonmultiplicative, nothing will happen
     */
    inline void mySetNM(const long& index)
    {
      myNonMultiplicatives[index] = true;
    }

    /**
     * This function deletes a nonmultiplicative variable (sets a
     * multiplicative variable)
     * @param var becomes multiplicative.
     * If the variable is already multiplicative, nothing will happen
     */
    void myDelNM(ConstRefPPMonoidElem var);

    /**
     * This function deletes a nonmultiplicative variable (sets a multiplicative variable)
     * @param index becomes multiplicative
     * If the variable is already multiplicative, nothing will happen
     */
    inline void myDelNM(const long& index){
      myNonMultiplicatives[index] = false;
    }

    /**
     * This function returns the current polynom as RingElem
     */
    inline const RingElem& myGetPol() const
    {
      return myPolynom;
    }

    /**
     *This function returns a pointer to the current polynom
     */
    inline RingElem* myGetPolPtr() 
    {
      return &myPolynom;
    }

    inline const RingElem* myGetPolPtr() const
    {
      return &myPolynom;
    }

    /**
     *This function returns a pointer to the current ancestor
     *???Do I use this???
     */
    inline PPMonoidElem* myGetAncPtr()
    {
      return &myAncestor;
    }

    inline const PPMonoidElem* myGetAncPtr() const
    {
      return &myAncestor;
    }

    /**
     * This function returns the current ancestor as PPMonoidElem
     */
    inline PPMonoidElem myGetAnc() const
    {
      return myAncestor;
    }

    /**
     * This function returns the current nonmultiplicative variables
     * as std::vector<PPMonoidElem> 
     * (The i-th multiplicative variable is represented by x_i)
     */
    std::vector<PPMonoidElem> myGetNM();

    /**
     * This function returns the current nonmultiplicative variables
     * as std::vector<bool>
     */
    inline std::vector<bool>  myGetVectorNM() const
    {
      return myNonMultiplicatives;
    }

    /**
     * This function checks if a variable is nonmultiplicative
     * @param var is the variable which shall be checked
     * returns true if the variable is nonmultiplicative
     */
    bool IamNM(ConstRefPPMonoidElem var);
 
    /**
     * This function checks if a variable is nonmultiplicative
     * @param index is the index of the variable which shall be checked
     * returns true if the variable is nonmultiplicative
     */
    inline bool IamNM(const long& index) const
    {
      return myNonMultiplicatives[index];
    }

    /**
     * This function deletes all nonmultiplicativ variables
     */
    void myClearNM();

  private:

    /**
     * The polynom of the Janet-Triple. Its type is RingElem
     */  
    RingElem myPolynom;

    /**
     * The ancestor of the polynom. It is a monomial and its type is
     * PPMonoidElem
     */
    PPMonoidElem myAncestor; 

    /**
     * The nonmultiplicatives variables of the Polynom. We store it in
     * a std::vector<bool>.
     */
    std::vector<bool> myNonMultiplicatives; 

  }; 

  typedef std::list<JanetTriple>::iterator nodeData;

  /****************************************************************************************************************************/
  /*! \brief The JanetNode are the nodes in the JanetTree. It's the
   *   father class of two different node types
   *
   * The JanetNode are the nodes in the JanetTree. It's the father
   * class of two different node types. In the father-class we store
   * the varDistance and the degDistance to the next node and
   * define some virtual functions for the child-classes (We have to
   * do this because we use a Handle-Class to store the JanetNodes in
   * std::lists.
   */
  class JanetNodeBase
  {
  protected:

    /**
     * The constructor of the JanetNode. The constructor is proteced
     * because the user shall not use this class
     * @param deg set the distance to the next degree-node
     * @param var set the distance to the next var-node
     * @param newTriple set the JanetTriple in the node
     */
    JanetNodeBase(const long& deg, const long& var):myDisNextDeg(deg), myDisNextVar(var)
    {
    }

  public:

    /**
     * This function clone the JanetNode. You need this function for
     * the class JanetHandle.
     */
    virtual inline JanetNodeBase* myClone() const
    {
      CoCoA_ERROR("You don't have the permisson to deal with JanetNodes, maybe there is a mistake in the datastructure", "myClone");
      return new JanetNodeBase(*this);
    }

    /**
     * Virtual destructor (further informations: C++ Primer)
     */
    virtual ~JanetNodeBase()
    {
    }

    /**
     * This function returns the degree-distance to the next
     * degree-node
     */
    inline long myGetDisNextDeg() const
    {
      return myDisNextDeg;
    }

    /**
     * This function returns the variable-distance to the next
     * variable-node
     */ 
    inline long myGetDisNextVar() const
    {
      return myDisNextVar;
    }

    /**
     * This function sets the degree-distance to the next degree-node
     * @param dis is the new degree-distance
     */
    inline void mySetDisNextDeg(const long& dis)
    {
      myDisNextDeg = dis;
    }

    /**
     * This function sets the variable-distance to the next
     * variable-node
     * @param dis is the new variable-distance
     * maybe protected and a new virtual function so that JanetLeafNodeImpl can't
     * set another distance
     */
    inline void mySetDisNextVar(const long& dis)
    {
      myDisNextVar = dis;
    }

    /**
     * only a virtual function. You need this function in the class
     * JanetInternalNode
     */
    virtual inline std::list<JanetHandle>* myNextVarArm() 
    {
      CoCoA_ERROR("You don't have the permisson to deal with JanetNodes, maybe there is a mistake in the datastructure", "myNextVarArm"); //??remove in the final version??
      return 0;
    }

    /**
     * only a virtual function. You need this function in both child
     * classes
     */
    virtual inline bool IamLeafNode()
    {
      return false;
    }

    /**
     * only a virtual function. You need this function in the class
     * JanetInternalNode
     * Warnings because I don't use nextArm
     */
    virtual inline void mySetNextVarArm(const std::list<JanetHandle>& /*NextArm*/)
    {
      CoCoA_ERROR("You don't have the permisson to deal with JanetNodes, maybe there is a mistake in the datastructure", "mySetNextVarArm");
    }

    /**
     *You need this function for the class JanetLeafNode.
     *It returns a pointer on the JanetTriple.
     *In this case it returns 0
     */
    virtual inline JanetTriple* myGetTriplePtr()
    {
      CoCoA_ERROR("You don't have the permisson to deal with JanetNodes, maybe there is a mistake in the datastructure", "myGetTriplePtr");
      return 0;
    }

    /**
     *You need this function for the class JanetLeafNode. It returns
     *a pointer on the nodeData In this case it returns 0
     */
    virtual inline nodeData* myGetListIterPtr()
    {
      return 0;
    }

  protected:

    /**
     * the degree-distance to the next degree-node in the
     * JanetTree. If the distance is zero there is no next
     * degree-node in the JanetTree
     */
    long myDisNextDeg;

    /**
     * the variable-distance to the next variable-node in the
     * JanetTree. If the distance is zero there is no next
     * variable-node in the JanetTree
     */
    long myDisNextVar;
  };


  /***********************************************************************************************************************************/
  /** \brief A child-class of JanetNode. In the JanetTree this class
   * represents the leaf nodes
   *
   * The class represents the leaf nodes in the JanetTree. The class
   * contains the variables disNextDeg and disNextVar from the
   * father-class JanetNode and a JanetTriple as nodeData
   */
  class JanetLeafNodeImpl: public JanetNodeBase
  {
  public:
    /**
     * The constructor of the JanetLeafNode
     * @param newTriple is the triple of the JanetLeafNode
     * disNextVar and disNextDeg is set to zero
     */
    JanetLeafNodeImpl(const nodeData  newTriple): JanetNodeBase(0,0), myTriple(newTriple)//, triple(newTriple)
    {
    }

    /**
     * This function clones the JanetNode. You need this function for
     * the class JanetHandle.
     */
    inline JanetLeafNodeImpl* myClone() const
    {
      return new JanetLeafNodeImpl(*this);
    }

    
    /**
     *This function returns a pointer on the JanetTriple. 
     */
    virtual JanetTriple* myGetTriplePtr()
    {
      return &(*myTriple);
    }


    /**
     * This function returns always true. It checks if the node is a
     * leafNode
     */
    inline bool IamLeafNode()
    {
      return true;
    }

    /**
     * only dummy function which we don't need in this class. We
     * return an empty arm
     */
    inline std::list<JanetHandle>* myNextVarArm()
    {
      CoCoA_ERROR("You don't have the permisson to deal with arms in JanetLeafNodes, maybe there is a mistake in the datastructure", "myNextVarArm"); //??maybe remove this in the final version??
      return 0;
    }
    
    /**
     * only dummy function which we don't need in this class. If it
     * isn't a leaf node then it set's a new arm
     * !!WARNING because we don't need nextArm!!
     */   
    inline void mySetNextVarArm(const std::list<JanetHandle>& /*NextArm*/)
    {
      CoCoA_ERROR("You don't have the permisson to deal with arms JanetLeafNodes, maybe there is a mistake in the datastructure", "mySetNextVarArm");
    }

    /**
     *This function returns a pointer on the iterator of the list.
     *??TODO: constant??
     */
    inline nodeData* myGetListIterPtr()
    {
      return &myTriple;
    }
  private:
    /**
     *Stores the data for the leaf nodes. It is an iterator of a list
     *where we store the janet triples.
     */
    nodeData myTriple;
  };

  
  /***********************************************************************************************************************************/
  /** \brief A child-class of JanetNode. In the JanetTree this class
   * represent the internal nodes
   *
   * The class represents the internal nodes in the JanetTree. The
   * class contains the data of the janetsubtree in var direction
   */
  class JanetInternalNodeImpl: public JanetNodeBase
  {
  public:

    /**
     * the constructor of the class JanetInternalNode. It initializes
     * an empty arm of the JanetTree. The arm represents the
     * degree-direction in the next variable
     */
    JanetInternalNodeImpl(/*const SparsePolyRing& polyRing*/): JanetNodeBase(0, 0), myArm()
    {
    }

    /**
     *You need this function for the class JanetLeafNode. It returns
     *a pointer on the JanetTriple. In this case it returns 0
     */
    virtual JanetTriple* myGetTriplePtr()
    {
      return 0;
    }
 

    /**
     * This function clones the JanetInternalNode. You need this function for
     * the class JanetHandle.
     */
    inline JanetInternalNodeImpl* myClone() const
    {
      return new JanetInternalNodeImpl(*this);
    }
  
    /**
     * This function returns a pointer to the degree-arm in the next
     * variable
     */
    inline std::list<JanetHandle>* myNextVarArm()
    {
      return &myArm;
    }

    /**
     * This function checks if there is a node which is a leafNode and returns
     * false because it is an internal node
     */
    inline bool IamLeafNode()
    {
      return false;
    }

    /**
     * This function replaces the next degree-arm with a new one
     */
    inline void mySetNextVarArm(const std::list<JanetHandle>& NextArm)
    {
      myArm = NextArm;
    }

    /**
     *You need this function for the class JanetLeafNode. It returns
     *a pointer on the nodeData. In this case it returns 0
     */
    inline nodeData* myGetListIterPtr()
    {
      return 0;
    }

  private:

    /**
     * The list where we store the next nodes in var direction
     */
    std::list<JanetHandle> myArm;
 
  };


  /**************************************************************************************************************************************/
  /**
   * \brief a Handle-Class for the JanetNodes. Therefore we can
   * construct a list of different JanetNodes
   *
   * With this class we can construct a list of different
   * JanetNodes. It defines several constructers, a destructor and
   * overload the following operators: "*", "->"
   * The class uses reference-counting of pointers
   * Template: C++ Primer
   */

  class JanetHandle
  {
  public:
    /**
     * The default constructer. It constructs a null-pointer and
     * initializes the counter with one
     * useless?
     */
    JanetHandle():myPtr(0), myUse(new long(1)) 
    {
    }

    /**
     * Another constructor which constructs a Handle with a JanetNode.
     * @param node is pointed by the Handle
     * Here we use the clone()-functions of the JanetNode and we initzialize the
     * reference counter with one
     */
    JanetHandle(const JanetNodeBase& node): myPtr(node.myClone()), myUse(new long(1))
    {
    }

    /**
     * The copy-constructor
     **/
    JanetHandle(const JanetHandle& handle):myPtr(handle.myPtr), myUse(handle.myUse)
    {
      ++(*myUse);
    }

    /**
     * The destructor. It uses the function decrUse()
     **/
    ~JanetHandle()
    {
      myDecrUse();
    }

    /*
     * The assignment operator
     */
    JanetHandle& operator=(const JanetHandle& rhs)
    {
      ++*rhs.myUse;
      myDecrUse();
      myPtr = rhs.myPtr;
      myUse = rhs.myUse;
      return *this;
    } 

    /**
     * This function overloads the "->"-operator
     */
    JanetNodeBase* operator->() const
    {
      if (!myPtr) 
        CoCoA_ERROR("unbound JanetHandle", "operator->");
      return myPtr;
    }


    /**
     * This function overloads the "*"-operator
     */
    JanetNodeBase& operator*() const
    {
      if (myPtr == 0) 
        CoCoA_ERROR("unbound JanetHandle", "operator*");
      return *myPtr;
    }
 
  private:
    /**
     * Pointer to the JanetNode
     */
    JanetNodeBase *myPtr;
    /**
     * Pointer to the counter
     */
    long *myUse;

    /**
     * the "really" destructor. If the reference-counter is unequal to
     * zero it only decreases the counter. If the counter is zero it
     * deletes the object.
     */
    void myDecrUse();
  };

  /*************************************************************************************************************************************/ 
  /**
   * \brief The JanetTree.
   *
   * This class stores only the beginning of the tree. The rest of the
   * tree is stored in the JanetInternalNodes
   */

  class JanetTree
  {
  public:
    /**
     * The constructor. The constructor initializes a list with one Element. The element is a JanetInternalNode.
     * @param deg is the beginning degree of the tree
     * @param var is the beginning variable of the tree
     */
    JanetTree(const SparsePolyRing& PolyRing, const long& deg, const long& var): myArm(), myBeginDeg(deg), myBeginVar(var), myPolyRing(PolyRing)
    {
      myArm.push_back(JanetHandle(JanetInternalNodeImpl())); 
    }
    

    /**
     * Returns the whole rootArm as std::list<JanetHandle>
     * JanetIterator needs this function
     */
    inline std::list<JanetHandle>* myGetRootArm()
    {
      return &myArm;
    }

    /**
     * Same as above, but readonly
     */
    inline std::list<JanetHandle>* myGetRootArm() const
    {
      return const_cast<std::list<JanetHandle> *> (&myArm);
    }

    /**
     * Returns the beginning degree of the tree
     */
    inline long myGetBeginDeg() const
    {
      return myBeginDeg;
    }

    /** 
     *Returns the beginning variable of the tree
     */
    inline long myGetBeginVar() const
    {
      return myBeginVar;
    }

    /**
     *Returns the polyRing of the tree
     */
    inline SparsePolyRing myGetPolyRing() const
    {
      return myPolyRing;
    }

    /**
     *Deletes the content of the tree, but not the tree itself.
     *Attention: the polyRing remains!
     */
    inline void myDelete()
    {
      myArm.clear();
      myArm.push_back(JanetHandle(JanetInternalNodeImpl()));
    }

    /**
     *Deletes the content of the Tree and adds the new tree.
     *Attention: the polyRing remains!
     */
    inline void myCopy(const JanetTree& tree)
    {
      myArm = tree.myArm;
      myBeginDeg = tree.myBeginDeg;
      myBeginVar = tree.myBeginVar;
    }

    /**
     * Delete the current JanetTree and add the new JanetTree
     * @param tree the new JanetTree
     * Only works if current Tree is empty!!!
     */
    void myAddAtBegin(JanetTree& tree);

    JanetTriple* myJDivisor(ConstRefPPMonoidElem w) const;

  private:
    /**
     * only declaration, there isn't a definition because we can't
     * copy the tree
     * Nobody shall use this operator, therefore private
     */
    JanetTree& operator=(const JanetTree &);

    /**
     * The first degree arm of the JanetTree
     */
    std::list<JanetHandle> myArm;

    /** 
     * The beginning degree of the JanetTree
     */
    long myBeginDeg;

    /**
     * The beginning variable of the JanetTree
     */
    long myBeginVar;

    /**
     * The polyRing of the Tree
     */
    SparsePolyRing myPolyRing;
  };

  /*************************************************************************************************************************************/
  /**
   * \brief With an object of JanetIterator we can navigate through
   * the JanetTreẹ
   * 
   * I try to programm this class similar to the STL (because of the
   * tree-structure I had to add some extra functionalities)
   *
   * After thinking about it: I guess, the only similarity is the name ;-)
   */
  class JanetIterator
  {
  public:

    /**
     * The constructor of the JanetIterator
     * @param tree is the JanetTree where the Iterator navigates through
     */
    JanetIterator(const JanetTree &tree): myCurTree(&tree), myCurArm(tree.myGetRootArm()), myMonomial(NumIndets(tree.myGetPolyRing())), myCurVar(tree.myGetBeginVar())
    {
      // myCurTree = &tree; 
      myCurIter = myCurArm->begin();
      myMonomial[myCurVar] = tree.myGetBeginDeg();
    }

    /**
     * Changes the type of node
     * @param triple is the new Triple in the LeafNode
     * 
     * Changes any kind of node in a leaf node. This function deletes
     * all the nodes behind the current node
     */
    void myChangeToLeafNode(const nodeData triple);

    /**
     * Changes the type of node
     * @param triple is the new Triple in the LeafNode
     * 
     * Changes any kind of node in a internal node. This function
     * deletes all the nodes behind the current node
     * ??? useless, where do we need this ???
     */
    void myChangeToInternalNode();


    /**
     * This function sets the iterator on the next degree-node and
     * returns the degree-distance. If it returns 0 then there is no
     * next node
     */
    long myNextDeg();

    /**
     * This function set the iterator on the next variable-node and
     * returns the variable-distance. If it returns 0 then there is no
     * next node
     */
    long myNextVar();

    /**
     * This function returns the current degree of the current
     * variable on which the iterator points
     */
    inline long myCurrentDeg() const
    {
      return myMonomial[myCurVar];
    }

    /**
     * This function returns the current variable on which the iterator points
     */
    inline long myCurrentVar() const
    {
      return myCurVar;
    }

    /**
     * This function sets a new variable-node. This node is an
     * internal-node
     * 
     * @param dis is the variable distance between the current
     * and the new node
     *
     * If there is another node in this direction, this node lies
     * behind the new node.  If the variable-distance of
     * the next node is lower or equal to dis then
     * this node will be deleted
     *
     * If the current node is a leaf Node we change this node
     * into an internal node
     */
    void mySetNextVar(long dis);

    /**
     * This function sets a new degree-node. This node is an
     * internal-node
     * 
     * @param dis is the degree distance between the current node and
     * the new node
     * 
     * If there is another node in this direction then the new  node lies
     * between the current node and this node.  If the degree-distance of
     * the next node is lower or equal to dis
     * this node will be deleted and the new node will be inserted.
     *
     * ?? There is an assertion where we check if it isn't a leaf Node ??
     */
    void mySetNextDeg(long dis);

    /**
     * This function sets a new degree-node. This node is an
     * leaf-node
     *
     * @param triple the JanetTriple in the new leafTriple
     * @param dis is the degree distance between the current node
     * and the new node
     *
     * If there is another node in this direction this node will
     * be deleted and after this we insert the new node. If the
     * current node is a leaf node we change this node to an
     * internal node and then we insert the new node.
     */
    void mySetNextDeg(const nodeData& triple,long dis);

    /**
     * This function connects two JanetTrees
     *
     * @param tree is the JanetTree which shall be connect with the current tree
     *
     * tree is appended on the current postion of the
     * iterator in deg-direction. The tree will be copied after this position in degree
     * direction. 
     *
     * ?? WARNING: NO TESTS IF THE CONNECTION MAKES SENSE ??
     * Shall I warn?
     */
    void myConnectJanetTreeDeg(JanetTree& tree);


    /**
     * This function deletes the next node in variable-direction. The
     * whole degree direction of this node will be also deleted. The
     * nodes in variable direction won't be deleted
     */
    void myDelNextVar();


    /**
     * Returns the JanetTriple of the current Node. If it isn't a leaf
     * Node it returns an default triple
     */
    const JanetTriple& myGetTriple() const;

    /**
     * Returns a pointer to the JanetTriple of the current Node. If it
     * isn't a leaf Node it returns 0
     */
    inline JanetTriple* myGetTriplePtr() const
    {
      return (*myCurIter)->myGetTriplePtr();
    }

    /**
     * Set a new Triple in the current node
     *
     * @param newTriple is the new JanetTriple
     *
     * This function changes the triple only if we are in a leaf
     * node. Then it returns true. If we are in a internal node we
     * do nothing and return false
     */
    bool IamSetTriple(const JanetTriple& NewTriple);

    /**
     * This function changes the polynomial of the JanetTriple
     * 
     * @param newPol is the new polynomial
     * 
     * This functions changes the polynomial only if we are in a leaf
     * node. Then it returns true. If we are in a internal node
     * we do nothing and return false
     */
    bool IamSetPol(ConstRefRingElem NewPol);

    /**
     * This function changes the ancestor of the JanetTriple
     * 
     * @param newAnc is the new ancestor
     * 
     * This functions changes the ancestor only if we are in a leaf
     * node. Then it returns true. If we are in a internal node
     * we do nothing and return false
     */
    bool IamSetAnc(ConstRefPPMonoidElem NewAnc);

    /**
     * This function sets a nonMultiplicative variable
     * 
     * @param var is the variable which we want to set
     * 
     * This functions sets the varibale only if we are in a leaf
     * node. Then it returns true. If we are in a internal node
     * we do nothing and return false
     */
    bool IamSetNM(ConstRefPPMonoidElem var);

    /**
     * This function sets a nonMultiplicative variable
     * 
     * @param index is the index of the variable which we want to set
     * 
     * This functions sets the varibale only if we are in a leaf
     * node. Then it returns true. If we are in a internal node
     * we do nothing and return false
     */
    bool IamSetNM(const long &index);

    /**
     * This function returns the current polynom of the current
     * JanetTriple as RingElem
     */
    inline const RingElem& myGetPol() const
    {
      CoCoA_ASSERT((*myCurIter)->myGetTriplePtr() != 0); 
      return ((*myCurIter)->myGetTriplePtr())->myGetPol();
    }

    /**
     * This function returns a pointer to the current polynom of the current
     * JanetTriple.
     */
    inline const RingElem* myGetPolPtr() const
    {
      CoCoA_ASSERT((*myCurIter)->myGetTriplePtr() != 0);
      return ((*myCurIter)->myGetTriplePtr())->myGetPolPtr();
    }

    /**
     * This function returns the current ancestor of the current
     * JanetTriple as PPMonoidElem
     */
    inline PPMonoidElem myGetAnc() const
    {
      CoCoA_ASSERT((*myCurIter)->myGetTriplePtr() != 0); 
      return ((*myCurIter)->myGetTriplePtr())->myGetAnc();
    }
 
    /**
     * This function returns the current nonmultiplicative variables
     * of the current JanetTriple as std::vector<PPmonoidElem>
     */
    inline std::vector<bool> myGetVectorNM() const
    {
      CoCoA_ASSERT((*myCurIter)->myGetTriplePtr() != 0); 
      return ((*myCurIter)->myGetTriplePtr())->myGetVectorNM();
    }

    /**
     * This function checks if a variable is nonmultiplicative
     * 
     * @param var: The variable which we test
     * 
     * If the variable is nonMultiplicative we return true
     * otherwise false
     */
    inline bool IamNM(ConstRefPPMonoidElem var) const
    {
      CoCoA_ASSERT((*myCurIter)->myGetTriplePtr() != 0); 
      return ((*myCurIter)->myGetTriplePtr())->IamNM(var);
    }

    /**
     * This function checks if a variable is nonmultiplicative
     * 
     * @param index: The variable with the index which we test
     * 
     * If the variable is nonMultiplicative we return true
     * othwise false
     */
    inline bool IamNM(const long &index) const
    {
      CoCoA_ASSERT((*myCurIter)->myGetTriplePtr() != 0); 
      return ((*myCurIter)->myGetTriplePtr())->IamNM(index);
    }


    /**
     * This function deletes a nonmultiplicative variable (sets a
     * multiplicative variable)
     *
     * @param var becomes multiplicative
     * 
     * If the variable is already multiplicative, nothing happens
     */  
 
    bool IamDelNM(ConstRefPPMonoidElem var);

    /**
     * This function deletes a nonmultiplicative variable (set a
     * multiplicative variable)
     *
     * @param index becomes multiplicative
     * 
     * If the variable is already multiplicative, nothing happens
     */
    bool IamDelNM(const long &index);

    /**
     * The iterator returns to the beginning of the JanetTree
     */
    void myReturnToBegin();


    /**
     * This function returns the distance to the next degNode. If
     * there is no deg node the function returns zero
     */
    inline long myDisNextDeg()
    {
      return (*myCurIter)->myGetDisNextDeg();
    }


    /**
     * This function returns the distance to the next varNode. If
     * there is no deg node the function returns zero
     */
    inline long myDisNextVar()
    {
      return (*myCurIter)->myGetDisNextVar();
    }

    /**
     * The iterator jumps to the previous degNode. If there is no
     * previous degNode this function returns the current node
     */
    long myPrevDeg();

    /**
     *Returns the current monomial
     */
    inline PPMonoidElem myGetMonomial() const
    {
      return PPMonoidElem(PPM(myCurTree->myGetPolyRing()), myMonomial);
    }

    /**
     *Return a pointer to the current list-iterator which is in the leaf node
     *
     *TODO: Explain what I mean... ;-)
     *
     */
    inline nodeData* myGetListIterPtr()
    {
      return (*myCurIter)->myGetListIterPtr();
    }

    /**
    * Returns the highest Iterator starting from current node
    * 
    */
    JanetIterator myGotoHighestNode();

  private:

    /**
     * A pointer to the tree on which the iterator works
     */
    const JanetTree* myCurTree;

    /** 
     * A pointer on the arm on which the iterator is working at the
     * moment
     */
    std::list<JanetHandle>* myCurArm;

    /**
     * The iterator which points at the current node
     */
    std::list<JanetHandle>::iterator myCurIter;

    /**
     * The current monomial which we have at the current position
     */
    std::vector<long> myMonomial;

    /**
     * The current variable
     */
    long myCurVar;

  };

} // end of namespace CoCoa
#endif

