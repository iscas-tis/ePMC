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

/**CFile***********************************************************************

  FileName    [cuddAddApply.c]

  PackageName [cudd]

  Synopsis    [Apply functions for ADDs and their operators.]

  Author      [Fabio Somenzi]

  Copyright   [Copyright (c) 1995-2012, Regents of the University of Colorado

  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

  Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

  Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

  Neither the name of the University of Colorado nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.]

******************************************************************************/

#include <stdlib.h>
#include <stdint.h>
#include "util.h"
#include "cuddInt.h"

// Moritz: strongly modified. removed functions we don't need and added a
// few new ones


#ifndef lint
static char rcsid[] DD_UNUSED = "$Id: cuddAddApply.c,v 1.19 2012/02/05 01:07:18 fabio Exp $";
#endif
// https://stackoverflow.com/questions/22751762/how-to-make-compiler-not-show-int-to-void-pointer-cast-warnings
#define INT2VOIDP(i) (void*)(uintptr_t)(i)



/*---------------------------------------------------------------------------*/
/* Macro declarations                                                        */
/*---------------------------------------------------------------------------*/


/**AutomaticStart*************************************************************/

/*---------------------------------------------------------------------------*/
/* Static function prototypes                                                */
/*---------------------------------------------------------------------------*/


/**AutomaticEnd***************************************************************/


/*---------------------------------------------------------------------------*/
/* Definition of exported functions                                          */
/*---------------------------------------------------------------------------*/

/**Function********************************************************************

  Synopsis    [Applies op to the corresponding discriminants of f and g.]

  Description [Applies op to the corresponding discriminants of f and g.
  Returns a pointer to the result if succssful; NULL otherwise.]

  SideEffects [None]

  SeeAlso     [Cudd_addMonadicApply Cudd_addPlus Cudd_addTimes
  Cudd_addDivide Cudd_addMin Cudd_addMax Cudd_addOr]

******************************************************************************/
DdNode *
Cudd_addApply(
  DdManager * dd,
  DD_AOP op,
  DdNode * f,
  DdNode * g)
{
    DdNode *res;

    do {
      dd->reordered = 0;
      res = cuddAddApplyRecur(dd,op,f,g);
    } while (dd->reordered == 1);
    return(res);

} /* end of Cudd_addApply */


DdNode *
Cudd_addPlus(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    /*
    if (F == DD_ZERO(dd)) {
      return(G);
    }
    if (G == DD_ZERO(dd)) {
      return(F);
    }
    */
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      value = dd->epmcCallback2(dd->opnr_add, cuddV(F), cuddV(G));
      res = cuddUniqueConst(dd, value);
      return(res);
    }
    if (F > G) { /* swap f and g */
      *f = G;
      *g = F;
    }
    return(NULL);
} /* end of Cudd_addPlus */

DdNode *
Cudd_addEq(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;
    
    F = *f; G = *g;
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      if (F == G) {
        res = DD_TRUE(dd);
      } else {
        if (Cudd_Type(F) == Cudd_Type(G)) {
          res = DD_FALSE(dd);
        } else {
          value = dd->epmcCallback2(dd->opnr_eq, cuddV(F), cuddV(G));
          res = cuddUniqueConst(dd, value);
        }
      }
      return(res);
    }
    if (F > G) { /* swap f and g */
      *f = G;
      *g = F;
    }
    return(NULL);
}

DdNode *
Cudd_addNe(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      if (F == G) {
        res = DD_TRUE(dd);
      } else {
        if (Cudd_Type(F) == Cudd_Type(G)) {
          res = DD_FALSE(dd);
        } else {
          value = dd->epmcCallback2(dd->opnr_ne, cuddV(F), cuddV(G));
          res = cuddUniqueConst(dd, value);
        }
      }
      if (res == DD_TRUE(dd)) {
        res = DD_FALSE(dd);
      } else {
        res = DD_TRUE(dd);
      }
      return(res);
    }
    if (F > G) { /* swap f and g */
      *f = G;
      *g = F;
    }
    return(NULL);
}

DdNode *
Cudd_addIff(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      if (F == G) {
        res = DD_TRUE(dd);
      } else {
        res = DD_FALSE(dd);
      }
      return(res);
    }
    if (F > G) { /* swap f and g */
      *f = G;
      *g = F;
    }
    return(NULL);
}


DdNode *
Cudd_addImplies(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      if (F == DD_FALSE(dd) || G == DD_TRUE(dd)) {
        res = DD_TRUE(dd);
      } else {
        res = DD_FALSE(dd);
      }
      return(res);
    }
    return(NULL);
}

DdNode *
Cudd_addTimes(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    /*
    if (F == DD_ZERO(dd) || G == DD_ZERO(dd)) return(DD_ZERO(dd));
    if (F == DD_ONE(dd)) return(G);
    if (G == DD_ONE(dd)) return(F);
    */
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      value = dd->epmcCallback2(dd->opnr_multiply, cuddV(F), cuddV(G));
      res = cuddUniqueConst(dd,value);
	  return(res);
    }
    /*
    if (F > G) {
      *f = G;
      *g = F;
    }
    */
    return(NULL);

} /* end of Cudd_addTimes */

DdNode *
Cudd_addMultiply(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
  return Cudd_addTimes(dd, f, g);
}

DdNode *
Cudd_addDivide(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    /* We would like to use F == G -> F/G == 1, but F and G may
    ** contain zeroes. */
    /*
    if (F == DD_ZERO(dd)) return(DD_ZERO(dd));
    if (G == DD_ONE(dd)) return(F);
    */
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      value = dd->epmcCallback2(dd->opnr_divide, cuddV(F), cuddV(G));
      res = cuddUniqueConst(dd,value);
	  return(res);
    }
    return(NULL);

} /* end of Cudd_addDivide */

DdNode *Cudd_addAddInverse(
  DdManager * dd,
  DdNode * f);

DdNode *
Cudd_addAddInverse(
  DdManager * dd,
  DdNode * f)
{
    exit(1);
    if (cuddIsConstant(f)) {
        // ???
      CUDD_VALUE_TYPE value = dd->epmcCallback1(1, cuddV(f));
      DdNode *res = cuddUniqueConst(dd,value);
      return(res);
    }
    return(NULL);
}

DdNode *
Cudd_addSubtract(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    /*
    if (F == G) return(DD_ZERO(dd));
    if (F == DD_ZERO(dd)) return(cuddAddMonadicApplyRecur(dd,Cudd_addAddInverse,G));
    if (G == DD_ZERO(dd)) return(F);
    */
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      value = dd->epmcCallback2(dd->opnr_subtract, cuddV(F), cuddV(G));
      res = cuddUniqueConst(dd,value);
      return(res);
    }
    return(NULL);
}

/**Function********************************************************************

  Synopsis    [Integer and floating point min.]

  Description [Integer and floating point min for Cudd_addApply.
  Returns NULL if not a terminal case; min(f,g) otherwise.]

  SideEffects [None]

  SeeAlso     [Cudd_addApply]

******************************************************************************/
DdNode *
Cudd_addMin(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;
    DdNode *res;

    F = *f; G = *g;
    /*
    if (F == DD_PLUS_INFINITY(dd)) return(G);
    if (G == DD_PLUS_INFINITY(dd)) return(F);
    */
    if (F == G) return(F);
#if 0
    /* These special cases probably do not pay off. */
    if (F == DD_MINUS_INFINITY(dd)) return(F);
    if (G == DD_MINUS_INFINITY(dd)) return(G);
#endif
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
        value = dd->epmcCallback2(dd->opnr_min, cuddV(F), cuddV(G));
        res = cuddUniqueConst(dd,value);
        return(res);
    }
    if (F > G) { /* swap f and g */
	*f = G;
	*g = F;
    }
    return(NULL);

} /* end of Cudd_addMinimum */


/**Function********************************************************************

  Synopsis    [Integer and floating point max.]

  Description [Integer and floating point max for Cudd_addApply.
  Returns NULL if not a terminal case; max(f,g) otherwise.]

  SideEffects [None]

  SeeAlso     [Cudd_addApply]

******************************************************************************/
DdNode *
Cudd_addMax(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;
    DdNode *res;

    F = *f; G = *g;
    if (F == G) return(F);
    /*
    if (F == DD_MINUS_INFINITY(dd)) return(G);
    if (G == DD_MINUS_INFINITY(dd)) return(F);
    */
#if 0
    /* These special cases probably do not pay off. */
    if (F == DD_PLUS_INFINITY(dd)) return(F);
    if (G == DD_PLUS_INFINITY(dd)) return(G);
#endif
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
        // ???
        value = dd->epmcCallback2(dd->opnr_max, cuddV(F), cuddV(G));
        res = cuddUniqueConst(dd,value);
        return(res);
    }
    if (F > G) { /* swap f and g */
	*f = G;
	*g = F;
    }
    return(NULL);

} /* end of Cudd_addMax */

DdNode *
Cudd_addOr(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *F, *G;

    F = *f; G = *g;
    if (F == DD_TRUE(dd) || G == DD_TRUE(dd)) return(DD_TRUE(dd));
    if (cuddIsConstant(F)) return(G);
    if (cuddIsConstant(G)) return(F);
    if (F == G) return(F);
    if (F > G) { /* swap f and g */
	*f = G;
	*g = F;
    }
    return(NULL);
}

DdNode *
Cudd_addAnd(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *F, *G;

    F = *f; G = *g;
    if (F == DD_TRUE(dd) && G == DD_TRUE(dd)) {
      return(DD_TRUE(dd));
    }
    if (F == DD_FALSE(dd)) {
      return DD_FALSE(dd);
    }
    if (G == DD_FALSE(dd)) {
      return DD_FALSE(dd);
    }
    if (cuddIsConstant(F)) {
      return G;
    }
    if (cuddIsConstant(G)) {
      return F;
    }
    if (F > G) { /* swap f and g */
      *f = G;
      *g = F;
    }
    return(NULL);
}

/**Function********************************************************************

  Synopsis    [Applies op to the discriminants of f.]

  Description [Applies op to the discriminants of f.
  Returns a pointer to the result if succssful; NULL otherwise.]

  SideEffects [None]

  SeeAlso     [Cudd_addApply Cudd_addLog]

******************************************************************************/
__attribute__ ((visibility("default")))
DdNode *
Cudd_MTBDD_addMonadicApply(
  DdManager * dd,
  DD_MAOP op,
  DdNode * f)
{
  return Cudd_addMonadicApply(dd, op, f);
}

DdNode *
Cudd_addMonadicApply(
  DdManager * dd,
  DD_MAOP op,
  DdNode * f)
{
    DdNode *res;

    do {
	dd->reordered = 0;
	res = cuddAddMonadicApplyRecur(dd,op,f);
    } while (dd->reordered == 1);
    return(res);

} /* end of Cudd_addMonadicApply */

DdNode *
Cudd_addEPMCMonadicOp(
  DdManager * dd,
  DdNode * f)
{
    if (cuddIsConstant(f)) {
      CUDD_VALUE_TYPE value = dd->epmcCallback1(dd->epmcOp, cuddV(f));
      DdNode *res = cuddUniqueConst(dd,value);
      return(res);
    }
    return(NULL);
}

DdNode *
Cudd_addEPMCBinaryOp(
  DdManager * dd,
  DdNode ** f,
  DdNode ** g)
{
    DdNode *res;
    DdNode *F, *G;
    CUDD_VALUE_TYPE value;

    F = *f; G = *g;
    if (cuddIsConstant(F) && cuddIsConstant(G)) {
      value = dd->epmcCallback2(dd->epmcOp, cuddV(F), cuddV(G));
      res = cuddUniqueConst(dd,value);
      return(res);
    }
    return(NULL);
}


/*---------------------------------------------------------------------------*/
/* Definition of internal functions                                          */
/*---------------------------------------------------------------------------*/


/**Function********************************************************************

  Synopsis    [Performs the recursive step of Cudd_addApply.]

  Description [Performs the recursive step of Cudd_addApply. Returns a
  pointer to the result if successful; NULL otherwise.]

  SideEffects [None]

  SeeAlso     [cuddAddMonadicApplyRecur]

******************************************************************************/
DdNode *
cuddAddApplyRecur(
  DdManager * dd,
  DD_AOP op,
  DdNode * f,
  DdNode * g)
{
    DdNode *res,
	   *fv, *fvn, *gv, *gvn,
	   *T, *E;
    unsigned int ford, gord;
    unsigned int index;
    DD_CTFP cacheOp;

    /* Check terminal cases. Op may swap f and g to increase the
     * cache hit rate.
     */
    statLine(dd);
    res = (*op)(dd,&f,&g);
    if (res != NULL) return(res);

    /* Check cache. */
    if (op == Cudd_addEPMCBinaryOp) {
      cacheOp = (DD_CTFP) INT2VOIDP(dd->epmcOp);
    } else {
      cacheOp = (DD_CTFP) op;
    }
    res = cuddCacheLookup2(dd,cacheOp,f,g);
    if (res != NULL) return(res);

    /* Recursive step. */
    ford = cuddI(dd,f->index);
    gord = cuddI(dd,g->index);
    if (ford <= gord) {
      index = f->index;
      fv = cuddT(f);
      fvn = cuddE(f);
    } else {
      index = g->index;
      fv = fvn = f;
    }
    if (gord <= ford) {
      gv = cuddT(g);
      gvn = cuddE(g);
    } else {
      gv = gvn = g;
    }

    T = cuddAddApplyRecur(dd,op,fv,gv);
    if (T == NULL) {
      return(NULL);
    }
    cuddRef(T);

    E = cuddAddApplyRecur(dd,op,fvn,gvn);
    if (E == NULL) {
      Cudd_RecursiveDeref(dd,T);
      return(NULL);
    }
    cuddRef(E);

    res = (T == E) ? T : cuddUniqueInter(dd,(int)index,T,E);
    if (res == NULL) {
      Cudd_RecursiveDeref(dd, T);
      Cudd_RecursiveDeref(dd, E);
      return(NULL);
    }
    cuddDeref(T);
    cuddDeref(E);

    /* Store result. */
    cuddCacheInsert2(dd,cacheOp,f,g,res);

    return(res);
} /* end of cuddAddApplyRecur */


/**Function********************************************************************

  Synopsis    [Performs the recursive step of Cudd_addMonadicApply.]

  Description [Performs the recursive step of Cudd_addMonadicApply. Returns a
  pointer to the result if successful; NULL otherwise.]

  SideEffects [None]

  SeeAlso     [cuddAddApplyRecur]

******************************************************************************/
DdNode *
cuddAddMonadicApplyRecur(
  DdManager * dd,
  DD_MAOP op,
  DdNode * f)
{
    DdNode *res, *ft, *fe, *T, *E;
    unsigned int index;
    DD_CTFP1 cacheOp;

    /* Check terminal cases. */
    statLine(dd);
    res = (*op)(dd,f);
    if (res != NULL) return(res);

    /* Check cache. */
     if (op == Cudd_addEPMCMonadicOp) {
      cacheOp = (DD_CTFP1) INT2VOIDP(dd->epmcOp);
    } else {
      cacheOp = (DD_CTFP1) op;
    }
    
    res = cuddCacheLookup1(dd,cacheOp,f);
    if (res != NULL) return(res);

    /* Recursive step. */
    index = f->index;
    ft = cuddT(f);
    fe = cuddE(f);

    T = cuddAddMonadicApplyRecur(dd,op,ft);
    if (T == NULL) return(NULL);
    cuddRef(T);

    E = cuddAddMonadicApplyRecur(dd,op,fe);
    if (E == NULL) {
	Cudd_RecursiveDeref(dd,T);
	return(NULL);
    }
    cuddRef(E);

    res = (T == E) ? T : cuddUniqueInter(dd,(int)index,T,E);
    if (res == NULL) {
	Cudd_RecursiveDeref(dd, T);
	Cudd_RecursiveDeref(dd, E);
	return(NULL);
    }
    cuddDeref(T);
    cuddDeref(E);

    /* Store result. */
    cuddCacheInsert1(dd,cacheOp,f,res);

    return(res);

} /* end of cuddAddMonadicApplyRecur */


/*---------------------------------------------------------------------------*/
/* Definition of static functions                                            */
/*---------------------------------------------------------------------------*/
