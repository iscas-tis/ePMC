#include "util.h"
#include "cuddInt.h"

__attribute__ ((visibility("default")))
DdNode *Cudd_addNot(
  DdManager * dd,
  DdNode * f)
{
  DdNode *zero = DD_ZERO(dd);
  DdNode *one = DD_ONE(dd);
  if (f == zero) {
      return one;
  } else if (f == one) {
      return zero;
  } else {
      return NULL;
  }
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_addApplyNot(
  DdManager * dd,
  DdNode * f)
{
    return Cudd_addMonadicApply(dd, Cudd_addNot, f);
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_addApplyAnd(
  DdManager * dd,
  DdNode * f,
  DdNode * g
)
{
    return Cudd_addApply(dd, Cudd_addTimes, f, g);
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_addApplyOr(
  DdManager * dd,
  DdNode * f,
  DdNode * g
)
{
    return Cudd_addApply(dd, Cudd_addOr, f, g);
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_addApplyEq(
  DdManager * dd,
  DdNode * f,
  DdNode * g
)
{
    return Cudd_addApply(dd, Cudd_addXnor, f, g);
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_addApplyNe(
  DdManager * dd,
  DdNode * f,
  DdNode * g
)
{
    return Cudd_addApply(dd, Cudd_addXor, f, g);
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_dd_one(DdManager *dd) {
    return dd->one;
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_dd_zero(DdManager *dd) {
    return dd->zero;
}
