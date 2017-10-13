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
    return Cudd_addApply(dd, Cudd_addEPMCBinaryOp, f, g);
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

#if 0 // TODO


DdNode *
Cudd_addExistAbstract(
  DdManager * manager,
  DdNode * f,
  DdNode * cube)
{
    DdNode *res;

    two = cuddUniqueConst(manager, manager->two_value);
    if (two == NULL) return(NULL);
    cuddRef(two);

    if (addCheckPositiveCube(manager, cube) == 0) {
        (void) fprintf(manager->err,"Error: Can only abstract cubes");
        return(NULL);
    }

    do {
      manager->reordered = 0;
      res = cuddAddGeneralAbstractRecur(manager, f, cube);
    } while (manager->reordered == 1);

    if (res == NULL) {
      Cudd_RecursiveDeref(manager,two);
      return(NULL);
    }
    cuddRef(res);
    Cudd_RecursiveDeref(manager,two);
    cuddDeref(res);

    return(res);

} /* end of Cudd_addGeneralAbstract */

DdNode *
cuddAddGeneralAbstractRecur(
  DdManager * manager,
  DdNode * f,
  DdNode * cube)
{
    DdNode	*T, *E, *res, *res1, *res2, *zero;

    statLine(manager);
    zero = DD_ZERO(manager);

    /* Cube is guaranteed to be a cube at this point. */	
    /*
    if (f == zero || cuddIsConstant(cube)) {  
        return(f);
    }
    */
    if (cuddIsConstant(cube)) {
        return(f);
    }


    if (cuddI(manager,f->index) > cuddI(manager,cube->index)) {
	res1 = cuddAddGeneralAbstractRecur(manager, f, cuddT(cube));
	if (res1 == NULL) {
      return(NULL);
    }
	cuddRef(res1);
	/* Use the "internal" procedure to be alerted in case of
	** dynamic reordering. If dynamic reordering occurs, we
	** have to abort the entire abstraction.
	*/
	//  TODO check following change OK?
	res = cuddAddApplyRecur(manager,Cudd_addPlus,res1,res1);
	if (res == NULL) {
	    Cudd_RecursiveDeref(manager,res1);
	    return(NULL);
	}
	cuddRef(res);
	Cudd_RecursiveDeref(manager,res1);
	cuddDeref(res);
        return(res);
    }

    if ((res = cuddCacheLookup2(manager, Cudd_addExistAbstract, f, cube)) != NULL) {
	return(res);
    }

    T = cuddT(f);
    E = cuddE(f);

    /* If the two indices are the same, so are their levels. */
    if (f->index == cube->index) {
	res1 = cuddAddGeneralAbstractRecur(manager, T, cuddT(cube));
	if (res1 == NULL) return(NULL);
        cuddRef(res1);
	res2 = cuddAddGeneralAbstractRecur(manager, E, cuddT(cube));
	if (res2 == NULL) {
	    Cudd_RecursiveDeref(manager,res1);
	    return(NULL);
	}
        cuddRef(res2);
	res = cuddAddApplyRecur(manager, Cudd_addPlus, res1, res2);
	if (res == NULL) {
	    Cudd_RecursiveDeref(manager,res1);
	    Cudd_RecursiveDeref(manager,res2);
	    return(NULL);
	}
	cuddRef(res);
	Cudd_RecursiveDeref(manager,res1);
	Cudd_RecursiveDeref(manager,res2);
	cuddCacheInsert2(manager, Cudd_addExistAbstract, f, cube, res);
	cuddDeref(res);
        return(res);
    } else { /* if (cuddI(manager,f->index) < cuddI(manager,cube->index)) */
	res1 = cuddAddGeneralAbstractRecur(manager, T, cube);
	if (res1 == NULL) return(NULL);
        cuddRef(res1);
	res2 = cuddAddGeneralAbstractRecur(manager, E, cube);
	if (res2 == NULL) {
	    Cudd_RecursiveDeref(manager,res1);
	    return(NULL);
	}
        cuddRef(res2);
	res = (res1 == res2) ? res1 :
	    cuddUniqueInter(manager, (int) f->index, res1, res2);
	if (res == NULL) {
	    Cudd_RecursiveDeref(manager,res1);
	    Cudd_RecursiveDeref(manager,res2);
	    return(NULL);
	}
	cuddDeref(res1);
	cuddDeref(res2);
	cuddCacheInsert2(manager, Cudd_addExistAbstract, f, cube, res);
        return(res);
    }	    

} /* end of cuddAddExistAbstractRecur */
#endif
