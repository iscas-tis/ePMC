/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

#include "util.h"
#include "cuddInt.h"

/* operator functions */

__attribute__ ((visibility("default")))
DdNode *
Cudd_MTBDD_addMonadicApplyOpNumber(
  DdManager * dd,
  int op,
  DdNode * f)
{
    dd->epmcOp = op;
    return Cudd_addMonadicApply(dd, Cudd_addEPMCMonadicOp, f);
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_MTBDD_addApplyOpNumber(
  DdManager * dd,
  int op,
  DdNode *f,
  DdNode *g)
{
    dd->epmcOp = op;
    return Cudd_addApply(dd, dd->operators[op], f, g);
}

__attribute__ ((visibility("default")))
DdNode *
Cudd_MTBDD_addApplyTernaryOpNumber(
  DdManager *dd,
  int op,
  DdNode *f,
  DdNode *g,
  DdNode *h)
{
  dd->epmcOp = op;
  return Cudd_addIte(dd, f, g, h);
}

typedef DdNode * (*DD_BIN_FN)(DdManager *, DdNode *, DdNode *);

DdNode *
Cudd_bddApplyOpNumber(
  DdManager * dd,
  int op,
  DdNode *f,
  DdNode *g)
{
    dd->epmcOp = op;
    return ((DD_BIN_FN) dd->bddOperators[op])(dd, f, g);
}

DdNode *
Cudd_bddApplyTernaryOpNumber(
  DdManager *dd,
  int op,
  DdNode *f,
  DdNode *g,
  DdNode *h)
{
  dd->epmcOp = op;
  return Cudd_bddIte(dd, f, g, h);
}

DdNode *
Cudd_addSimAndAbstract(
  DdManager * manager,
  DdNode * f,
  DdNode * g,
  DdNode * cube)
{
    DdNode *res;
    DdNode *and;
    
    and = Cudd_addApply(manager, Cudd_addAnd, f, g);
    cuddRef(and);
    res = Cudd_addOrAbstract(manager, and, cube);
    Cudd_RecursiveDeref(manager, and);
    return res;
}
