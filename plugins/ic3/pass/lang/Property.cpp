/******************************** CPPFile *****************************

* FileName [Property.cpp]

* PackageName [main]

* Synopsis [Method definitions of PCTL Properties.]

* SeeAlso [Property.h]

* Author [Bjoern Wachter]

* Copyright [ Copyright (c) 2006 by Saarland University. All
* Rights Reserved. This software is for educational purposes only.
* Permission is given to academic institutions to use, copy, and
* modify this software and its documentation provided that this
* introductory message is not removed, that this software and its
* documentation is used for the institutions' internal research and
* educational purposes, and that no monies are exchanged. No guarantee
* is expressed or implied by the distribution of this code. Send
* bug-reports and/or questions to: bwachter@cs.uni-sb.de. ]

**********************************************************************/

#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"
#include "Node.h"
#include "util/Database.h"
#include "Property.h"

namespace lang {

std::string Bound::toString() const {
  std::string result;
  switch(kind) {
  case Bound::GR : result = "> " +util::doubleToString(bound); break;
  case Bound::GEQ: result = ">= "+util::doubleToString(bound); break;
  case Bound::LE : result = "< " +util::doubleToString(bound); break;
  case Bound::LEQ: result = "<= "+util::doubleToString(bound); break;
  case Bound::DK : result = "=?"; break;
  default: assert(false); break;
  }
  return result;
}

Property::Property(PropertyKind __kind) : 
	kind(__kind) 
{
}

Property* Property::Replace(const CVC3::ExprHashMap<CVC3::Expr>& map,
			    Property* prop) {
	if(typeid(*prop)==typeid(PropExpr)) {
		return new PropExpr(((PropExpr*)prop)->getExpr().substExpr(map));
	} else if(typeid(*prop)==typeid(PropNeg)) {
		return new PropNeg((Property*)Replace(map,((PropNeg*)prop)->getProp()));
	} else if(typeid(*prop)==typeid(PropBinary)) {
		const PropBinary* pb = (PropBinary*)prop;
		Property* p1 = (Property*) Replace(map,pb->getProp1());
		Property* p2 = (Property*) Replace(map,pb->getProp2());
		return new PropBinary(pb->getOp(),p1,p2);
	} else if(typeid(*prop)==typeid(Quant)) {
		const Quant* quant = (Quant*)prop; 
		const Bound& b  = quant->getBound();
		Property*     pp = (Property*)Replace(map,quant->getProp());
		return new Quant(b,pp);
	} else if(typeid(*prop)==typeid(Next)) {
		const Next* next = (Next*)prop;
		Property* p    = (Property*) Replace(map,next->getProp());
		return new Next(p);
	} else if(typeid(*prop)==typeid(Until)) {
		const Until* until = (Until*)prop;
		const Time& time = until->getTime();
		Property* prop1 = (Property*)Replace(map,until->getProp1());
		Property* prop2 = (Property*)Replace(map,until->getProp2());
		return new Until(time,prop1,prop2);
	}
	assert(false);
	return 0 ;
}

void Property::CollectExprs(Property* prop,std::hash_set<CVC3::Expr>& exprs) {
	if(typeid(*prop)==typeid(PropExpr)) {
		ExprManager::CollectExprs(((PropExpr*)prop)->getExpr(),exprs);
	} else if(typeid(*prop)==typeid(PropNeg)) {
		CollectExprs(((PropNeg*)prop)->getProp(),exprs);
	} else if(typeid(*prop)==typeid(PropBinary)) {
		const PropBinary* pb = (PropBinary*)prop;
		CollectExprs(pb->getProp1(),exprs);
		CollectExprs(pb->getProp2(),exprs);
	} else if(typeid(*prop)==typeid(Quant)) {
		const Quant* quant = (Quant*)prop; 
		CollectExprs(quant->getProp(),exprs);
	} else if(typeid(*prop)==typeid(Next)) {
		const Next* next = (Next*)prop;
		CollectExprs(next->getProp(),exprs);
	} else if(typeid(*prop)==typeid(Until)) {
		const Until* until = (Until*)prop;
		CollectExprs(until->getProp1(),exprs);
		CollectExprs(until->getProp2(),exprs);
	}
}

PropExpr::PropExpr(const CVC3::Expr& __e) : Property(expr), e(__e) {
}

std::string PropExpr::toString() const {
	return e.toString();
}

ReachabilityReward::ReachabilityReward(Property *__prop)
  : Property(reachability_reward),prop(__prop) {
}

std::string ReachabilityReward::toString() const {
  return "R=? [ F " + prop->toString() + "]";
}

void ReachabilityReward::Apply(DFSAdapter &a) const {

}

ReachabilityReward* ReachabilityReward::Clone() const {
	return new ReachabilityReward(prop.get());
}

void ReachabilityReward::Cleanup() {
  
}

CumulativeReward::CumulativeReward(const double __time)
  : Property(cumulative_reward) {
  time = __time;
}

std::string CumulativeReward::toString() const {
  std::string time_string;
  std::stringstream time_sstream;
  time_sstream << time;
  time_sstream >> time_string;
  return "R=? [ C<=" + time_string + " ]";
}

void CumulativeReward::Apply(DFSAdapter &a) const {

}

CumulativeReward* CumulativeReward::Clone() const {
	return new CumulativeReward(time);
}


void CumulativeReward::Cleanup() {
  
}

InstantaneousReward::InstantaneousReward(double __time)
  : Property(instantaneous_reward) {
  time = __time;
}

std::string InstantaneousReward::toString() const {
  std::string time_string;
  std::stringstream time_sstream;
  time_sstream << time;
  time_sstream >> time_string;
  return "R=? [ I=" + time_string + "]";
}

void InstantaneousReward::Apply(DFSAdapter &a) const {

}

InstantaneousReward* InstantaneousReward::Clone() const {
	return new InstantaneousReward(time);
}


void InstantaneousReward::Cleanup() {
  
}

SteadyStateReward::SteadyStateReward(const Bound &bound__)
  : Property(steadystate_reward), bound(bound__) {
}

std::string SteadyStateReward::toString() const {
  std::string result("R");
  result +=(bound.min ? "min" : "max");
  result += bound.toString();
  result +="[ S ]";

  return result;
}

void SteadyStateReward::Apply(DFSAdapter &a) const {

}

SteadyStateReward* SteadyStateReward::Clone() const {
  return new SteadyStateReward(bound);
}


void SteadyStateReward::Cleanup() {
  
}

SteadyState::SteadyState(Property *__prop)
  : Property(steadystate), prop(__prop) {
}

SteadyState::SteadyState(const Bound& __bound, Property* __prop)
  : Property(steadystate), prop(__prop), bound(__bound) {
	assert(__prop);
}


std::string SteadyState::toString() const {
  std::string result( "S" );
  result +=(bound.min ? "min" : "max");
  result += bound.toString();
  result +="[ ";
  result += prop->toString();
  result += " ]";

  return result;
}

void SteadyState::Apply(DFSAdapter &a) const {

}

SteadyState* SteadyState::Clone() const {
  return new SteadyState(prop.get());
}


void SteadyState::Cleanup() {
  
}

void PropExpr::Apply(DFSAdapter& a) const {
}

PropExpr* PropExpr::Clone() const {
	return new PropExpr(e);
}

void PropExpr::Cleanup() {
}

PropNeg::PropNeg(Property* __p) : Property(neg), p(__p) {
}

std::string PropNeg::toString() const {
	return "!"+p->toString();
}

void  PropNeg::Apply(DFSAdapter& a) const {
	p->Apply(a);
}

PropNeg* PropNeg::Clone() const {
	return new PropNeg(p.get());
}

void  PropNeg::Cleanup() {
	p->Cleanup();
}


PropBinary::PropBinary(Operator __op,Property* __p1,Property* __p2) : Property(binary), p1(__p1), p2(__p2) {
	assert(__p1 && __p2);
	assert(__op == OR || __op == AND || __op == IMPL);
	op = __op;
}

std::string PropBinary::toString() const {
	std::string s1 (p1->toString()),
		   s2 (p2->toString());
	std::string ops;
	switch(op) {
		case OR:  ops = "|"; break;
		case AND: ops = "&"; break;
		case IMPL:ops = "=>";break;
	}
	return s1 + ops + s2;
}

void PropBinary::Apply(DFSAdapter& a) const {
	p1->Apply(a);
	p2->Apply(a);
}

PropBinary* PropBinary::Clone() const {
	return new PropBinary(op,p1.get(),p2.get());
}

void PropBinary::Cleanup() {
	p1->Cleanup();
	if(p1!=p2) //one never knows :-)
		p2->Cleanup();
}

Quant::Quant(const Bound& __bound, Property* __pathprop) : Property(quant), bound(__bound), pathprop(__pathprop)  {
	assert(__pathprop);
}

Quant::Quant(Bound::Kind kind, double d, bool min, Property* __pathprop) :  Property(quant), bound(kind,d,min),
	pathprop (__pathprop) {
	assert(__pathprop);
}

std::string Quant::toString() const {
	std::string result( "P" );
	result +=(bound.min ? "min" : "max");
    result += bound.toString();
	result +="[ ";
	result += pathprop->toString();
	result += " ]";
	return result;
}

void Quant::Apply(DFSAdapter& a) const {
	pathprop->Apply(a);
}

Quant* Quant::Clone() const {
	return new Quant(bound,pathprop.get());
}

void Quant::Cleanup() {
	pathprop->Cleanup();
}


std::string Time::toString() const {
	std::string result;
	switch(kind) {
		case GE:        result = ">=" + util::floatToString(t1);
				break;
		case LE:        result  = "<=" + util::floatToString(t2);
				break;
		case INTERVAL:  result = "["+util::floatToString(t1)+","+util::floatToString(t2) + "]";
				break;
		case UNBOUNDED: result = "[0,infty]"; 
				break;
		default:
		break;
	} 
	return result;
}


Next::Next(Property* __prop) : Property(next), prop(__prop) {
	assert(__prop);
}

Next::Next(const Time& __time,Property* __prop) : 
	Property(next), time(__time),	prop (__prop) {
	assert(__prop );
}

Next::Next(Time::Kind k,double t1, double t2, Property* __prop) 
	: Property(next)
	, time(k,t1,t2), prop ( __prop ) {
	assert(__prop );
}
	
std::string Next::toString() const {
	return "X "+ time.toString() + " " + prop->toString();
}

void Next::Apply(DFSAdapter& a) const {
	prop->Apply(a);
}

Next* Next::Clone() const {
	return new Next(prop.get());
}

void Next::Cleanup() {
	prop->Cleanup();
}

Until::Until(const Time& __time,Property* __prop1,Property*__prop2) : Property(until), time(__time),
		prop1 (__prop1), prop2 (__prop2) {
	assert(__prop1 && __prop2);
}

Until::Until(Time::Kind k,double t1, double t2,Property* __prop1,Property* __prop2) 
	: Property(until)
	, time(k,t1,t2), prop1 (__prop1), prop2 (__prop2) {
	assert(__prop1 && __prop2);
	
}

Until::Until(Property* __prop1,Property* __prop2) :
	 Property(until), prop1 (__prop1), prop2 (__prop2){
	assert(__prop1 && __prop2);
}

std::string Until::toString() const {
	std::string result;
	result += prop1->toString()+" U ";

	result += time.toString() + " ";		

	result += prop2->toString();
	return result;
}

void Until::Apply(DFSAdapter& a) const {
	prop1->Apply(a);
	prop2->Apply(a);
}

Until* Until::Clone() const {
	return new Until(time,prop1.get(),prop2.get());
}

void Until::Cleanup() {
	prop1->Cleanup();
	prop2->Cleanup();
}

} //end of namespace lang
