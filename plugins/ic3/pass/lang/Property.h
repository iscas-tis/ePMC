/* 
property syntax:

State Properties:
-----------------
prop ::= expr |
         !prop |
         P bound [ pathprop ] |
         S bound [ prop ]

comment:
1. "P" refers to the probability measure of the paths given by "pathprob"
2. "S" refers to steady-state probability

Probability Bounds and Query
----------------------------
bound ::= >=p | >p | <=p | <p | =?

comment: "=?" means "compute the path's measure".

Path Properties
---------------
pathprop ::= X prop |
             prop U prop |
             prop U time prop

comment: "X", "U" are the usual next-state and (bound) until operators

time ::= >=t | <=t | [t,t]
*/

#ifndef __PROPERTY_H
#define __PROPERTY_H

#include "Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"

namespace lang {


enum PropertyKind {
	expr,
	binary,
	neg,
	next,
	until,
	quant,
	steadystate,
	reachability_reward,
	cumulative_reward,
	instantaneous_reward,
	steadystate_reward
};

 struct Bound {
   enum Kind { GR,  // >  bound ... greater
               GEQ, // >= bound ... greater or equal
               LE,  // <  bound ... strictly less
               LEQ, // <= bound ... less or equal
               EQ,  // =  bound ... equal
               DK   // = ?      ... value to be computed
   } kind;
   double bound;
   bool min; // min=true means "minimum", min=false means "maximum"
   
 Bound(const Bound& b) : kind(b.kind), bound(b.bound), min(b.min) {}
 Bound(Kind __kind, double __bound, bool __min) : kind(__kind), bound(__bound), min(__min) {}
 Bound() : kind(DK) {}
   std::string toString() const;
 };

class Property : public Node {
public:
	PropertyKind kind;

	Property(PropertyKind kind);

	static Property* Replace(const CVC3::ExprHashMap<CVC3::Expr>& map, Property* prop);
	static void CollectExprs(Property* prop,std::hash_set<CVC3::Expr>& exprs);

	virtual std::string toString() const = 0;
	virtual void Apply(DFSAdapter& a) const = 0;
	virtual Property* Clone() const = 0;
	virtual void Cleanup() = 0;
    virtual unsigned arity() const = 0;
    virtual const Property &operator[](unsigned) const = 0;
};

class ReachabilityReward : public Property {
 private:
  boost::shared_ptr<Property> prop;
 public:
  ReachabilityReward(Property *);
  virtual std::string toString() const;
  virtual void Apply(DFSAdapter& a) const;
  virtual ReachabilityReward* Clone() const;
  virtual void Cleanup();
  inline Property *getProp() const { return prop.get(); };
  inline virtual unsigned arity() const {
    return 1;
  }
  inline virtual const Property &operator[](unsigned index) const {
    assert(0 == index);
    return *prop.get();
  }
};

class CumulativeReward : public Property {
 private:
  double time;
 public:
  CumulativeReward(const double);
  virtual std::string toString() const;
  virtual void Apply(DFSAdapter& a) const;
  virtual CumulativeReward* Clone() const;
  virtual void Cleanup();
  inline const double getTime() const { return time; };
  inline virtual unsigned arity() const {
    return 0;
  }
  inline virtual const Property &operator[](unsigned index) const {
    assert(false);
    return *this;
  }
};


class InstantaneousReward : public Property {
 protected:
  double time;
 public:
  InstantaneousReward(double time);
  virtual std::string toString() const;
  virtual void Apply(DFSAdapter& a) const;
  virtual InstantaneousReward* Clone() const;
  virtual void Cleanup();
  inline const double getTime() const { return time; };
  inline virtual unsigned arity() const {
    return 0;
  }
  inline virtual const Property &operator[](unsigned index) const {
    assert(false);
    return *this;
  }
};

class SteadyStateReward : public Property {
 public:
  SteadyStateReward(const Bound &);
  virtual std::string toString() const;
  virtual void Apply(DFSAdapter& a) const;
  virtual SteadyStateReward* Clone() const;
  virtual void Cleanup();
  inline virtual unsigned arity() const {
    return 0;
  }
  inline virtual const Property &operator[](unsigned index) const {
    assert(false);
    return *this;
  }
  inline const Bound& getBound() const {
    return bound;
  }
 private:
  Bound bound;
};

class SteadyState : public Property {
 public: 
  SteadyState(Property*);
  SteadyState(const Bound&, Property*);
  virtual std::string toString() const;
  virtual void Apply(DFSAdapter& a) const;
  virtual SteadyState* Clone() const;
  virtual void Cleanup();
  inline Property* getProp() const { return prop.get(); }
  inline virtual unsigned arity() const {
    return 1;
  }
  inline virtual const Property &operator[](unsigned index) const {
    assert(0 == index);
    return *prop.get();
  }
  inline const Bound& getBound() const {
    return bound;
  }

 private:
  boost::shared_ptr<Property> prop;
  Bound bound;
};

/*! \brief state formula

*/
class PropExpr : public Property {
protected:
	CVC3::Expr e;
public:
	PropExpr(const CVC3::Expr& e);
	virtual std::string toString() const;
	virtual void Apply(DFSAdapter& a) const;
	virtual PropExpr* Clone() const;
	virtual void Cleanup();
	inline CVC3::Expr getExpr() const { return e; }
    inline virtual unsigned arity() const {
      return 0;
    }
    inline virtual const Property &operator[](unsigned index) const {
      assert(false);
      return *this;
    }
};

class PropNeg : public Property {
protected:
	boost::shared_ptr<Property> p;
public:
	PropNeg(Property*);
	virtual std::string toString() const;
	virtual void Apply(DFSAdapter& a) const;
	virtual PropNeg* Clone() const;
	virtual void Cleanup();
	inline Property* getProp() const { return p.get(); }
    inline virtual unsigned arity() const {
      return 1;
    }
    inline virtual const Property &operator[](unsigned index) const {
      assert(0 == index);
      return *p.get();
    }
};


class PropBinary : public Property {
protected:


	boost::shared_ptr<Property> p1;
	boost::shared_ptr<Property> p2;
public:
	enum Operator { OR,   // p1 |  p2
			AND,  // p1 &  p2
			IMPL  // p1 => p2
		      } op;
	PropBinary(Operator,Property*,Property*);
	virtual std::string toString() const;
	virtual void Apply(DFSAdapter& a) const;
	virtual PropBinary* Clone() const;
	virtual void Cleanup();
	inline Operator getOp()       const { return op; }
	inline Property* getProp1() const { return p1.get(); }
	inline Property* getProp2() const { return p2.get(); }
    inline virtual unsigned arity() const {
      return 2;
    }
    inline virtual const Property &operator[](unsigned index) const {
      assert(index < 2);
      if (0 == index) {
        return *p1.get();
      } else {
        return *p2.get();
      }
    }
};

class Quant : public Property {
public:
protected:
	Bound bound;
	boost::shared_ptr<Property> pathprop;
public:
	Quant(const Bound&, Property*);
	Quant(Bound::Kind,double,bool,Property*);
	virtual std::string toString() const;
	virtual void Apply(DFSAdapter& a) const;
	virtual Quant* Clone() const;
	virtual void Cleanup();
	inline const Bound& getBound()       const { return bound; }
	inline bool isMin() const { return bound.min; }
	inline Property* getProp() const { return pathprop.get(); }
    inline virtual unsigned arity() const {
      return 1;
    }
    inline virtual const Property &operator[](unsigned index) const {
      assert(0 == index);
      return *pathprop.get();
    }
};

struct Time {
	enum Kind {
		GE,        // >=t1
		LE,        // <=t1
		INTERVAL,  // [t1,t2]
		UNBOUNDED  // [0,infty]
	} kind;
	double t1, t2;
	Time(const Time& t) : kind(t.kind), t1(t.t1), t2(t.t2) {}
	Time() : kind(UNBOUNDED) {}
	Time(Kind __kind, double __t1, double __t2) : 
	kind(__kind), t1(__t1), t2(__t2) { assert(t1<=t2); }
	std::string toString() const;
};


class Next : public Property {
protected:
	Time time;
	boost::shared_ptr<Property> prop;
public:
	Next(const Time&,Property*);
	Next(Time::Kind,double,double,Property*);
	Next(Property*);
	virtual std::string toString() const;
	virtual void Apply(DFSAdapter& a) const;
	virtual Next* Clone() const;
	virtual void Cleanup();
	inline const Time& getTime() const { return time;}
	inline Property* getProp() const { return prop.get(); }
    inline virtual unsigned arity() const {
      return 1;
    }
    inline virtual const Property &operator[](unsigned index) const {
      assert(0 == index);
      return *prop.get();
    }
};

class Until : public Property {
public:

protected:
	Time time;
	boost::shared_ptr<Property> prop1, prop2;
public:
	Until(const Time&,Property*,Property*);
	Until(Time::Kind,double,double,Property*,Property*);
	Until(Property*,Property*);
	virtual std::string toString() const;
	virtual void Apply(DFSAdapter& a) const;
	virtual Until* Clone() const;
	virtual void Cleanup();
	inline const Time& getTime() const { return time;}
	inline Property* getProp1() const { return prop1.get(); }
	inline Property* getProp2() const { return prop2.get(); }	
    inline virtual unsigned arity() const {
      return 2;
    }
    inline virtual const Property &operator[](unsigned index) const {
      assert(index < 2);
      if (0 == index) {
        return *prop1.get();
      } else {
        return *prop2.get();
      }
    }
};

}

#endif
