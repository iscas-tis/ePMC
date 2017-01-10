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

#include <jna.h>

DD_VOP1 callback1;
DD_VOP2 callback2;
GET_OPERATOR_NUMBER getOpNr;
Operators *ops;
generic_callback *opToCallback;
MTBDD custom_true;
MTBDD custom_false;

TASK_DECL_2(MTBDD, callback_eq_native, MTBDD*, MTBDD*);
TASK_IMPL_2(MTBDD, callback_eq_native, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return *a == *b ? mtbdd_true : mtbdd_false;
}
MTBDD MTBDD_ite(MTBDD a, MTBDD b, MTBDD c) {
	LACE_ME
	return CALL(mtbdd_custom_ite, a, b, c);
	// return mtbdd_ite(mtbdd_apply(a, custom_true, TASK(callback_eq_native)),b,c);
}
MTBDD MTBDD_compose(MTBDD a, MTBDDMAP map) {
	LACE_ME
	return mtbdd_compose(a,map);
}
MTBDDMAP MTBDD_map_empty() {
	LACE_ME
	return mtbdd_map_empty();
}
void printlabel_cb(FILE *out, uint32_t type, uint64_t value) {
	fprintf(out, "%llx\n", value);
}

MTBDDMAP MTBDD_map_add(MTBDDMAP map, uint32_t from, uint32_t to) {
	MTBDD value = mtbdd_makenode(to, mtbdd_false, mtbdd_true);
	return mtbdd_map_add(map, from, value);
}
void MTBDD_print(MTBDD f) {
	mtbdd_printdot(f, printlabel_cb);
}
MTBDD MTBDD_support(MTBDD a) {
	LACE_ME
	return mtbdd_support(a);
}


void epmc_init_mtbdd(
	DD_VOP1 epmcCallback1,
	DD_VOP2 epmcCallback2,
	GET_OPERATOR_NUMBER getOperatorNumber,
	MTBDD epmcT,
	MTBDD epmcF) {
	callback1 = epmcCallback1;
	callback2 = epmcCallback2;
	getOpNr = getOperatorNumber;
	custom_true = epmcT;
	custom_false = epmcF;

	int num_ops = sizeof(Operators) / sizeof(int);
	ops = (Operators*) malloc(sizeof(Operators));
	(*ops).id = getOpNr("id");
	(*ops).not = getOpNr("not");
	(*ops).add = getOpNr("add");
	(*ops).multiply = getOpNr("multiply");
	(*ops).subtract = getOpNr("subtract");
	(*ops).divide = getOpNr("divide");
	(*ops).divide_ignore_zero = getOpNr("divide-ignore-zero");
	(*ops).max = getOpNr("max");
	(*ops).min = getOpNr("min");
	(*ops).and = getOpNr("and");
	(*ops).or = getOpNr("or");
	(*ops).iff = getOpNr("iff");
	(*ops).implies = getOpNr("implies");
	(*ops).eq = getOpNr("eq");
	(*ops).ne = getOpNr("ne");
	int i;
	int maxOpNr = 0;
	for(i = 0; i < num_ops; i += sizeof(int)) {
		int currOpNr = *(((int*)ops) + i);
		if(currOpNr > maxOpNr) {
			maxOpNr = currOpNr;
		}
	}

	opToCallback = (generic_callback*) malloc((maxOpNr + 1) * sizeof(mtbdd_apply_op));
	opToCallback[(*ops).id] = (generic_callback) TASK(callback_id);
	opToCallback[(*ops).not] = (generic_callback) TASK(callback_not);
	opToCallback[(*ops).add] = (generic_callback) TASK(callback_add);
	opToCallback[(*ops).multiply] = (generic_callback) TASK(callback_multiply);
	opToCallback[(*ops).subtract] = (generic_callback) TASK(callback_subtract);
	opToCallback[(*ops).divide] = (generic_callback) TASK(callback_divide);
	opToCallback[(*ops).divide_ignore_zero] = (generic_callback) TASK(callback_divide_ignore_zero);
	opToCallback[(*ops).max] = (generic_callback) TASK(callback_max);
	opToCallback[(*ops).min] = (generic_callback) TASK(callback_min);
	opToCallback[(*ops).and] = (generic_callback) TASK(callback_and);
	opToCallback[(*ops).or] = (generic_callback) TASK(callback_or);
	opToCallback[(*ops).iff] = (generic_callback) TASK(callback_iff);
	opToCallback[(*ops).implies] = (generic_callback) TASK(callback_implies);
	opToCallback[(*ops).eq] = (generic_callback) TASK(callback_eq);
	opToCallback[(*ops).ne] = (generic_callback) TASK(callback_ne);
}

MTBDD MTBDD_uapply(MTBDD a, int op) {
	LACE_ME
	return mtbdd_uapply(a, (mtbdd_uapply_op) opToCallback[op], 0);
}

MTBDD MTBDD_apply(MTBDD a, MTBDD b, int op) {
	LACE_ME
	return mtbdd_apply(a, b, (mtbdd_apply_op) opToCallback[op]);
}

MTBDD MTBDD_abstract(MTBDD a, MTBDD vars, int op) {
	LACE_ME
	return CALL(mtbdd_custom_abstract, a, vars, (mtbdd_apply_op) opToCallback[op]);
}


/**
 * MTBDD node structure
 */
typedef struct __attribute__((packed)) mtbddnode {
    uint64_t a, b;
} * mtbddnode_t; // 16 bytes
#define GETNODE(mtbdd) ((mtbddnode_t)llmsset_index_to_ptr(nodes, mtbdd&0x000000ffffffffff))
#define MTBDD_TOGGLEMARK(s)           (s^mtbdd_complement)
#define MTBDD_TRANSFERMARK(from, to)  (to ^ (from & mtbdd_complement))
static inline int
mtbddnode_isleaf(mtbddnode_t n)
{
    return n->a & 0x4000000000000000 ? 1 : 0;
}
static inline uint32_t
mtbddnode_getvariable(mtbddnode_t n)
{
    return (uint32_t)(n->b >> 40);
}
static inline uint64_t
mtbddnode_getlow(mtbddnode_t n)
{
    return n->b & 0x000000ffffffffff; // 40 bits
}

static inline uint64_t
mtbddnode_gethigh(mtbddnode_t n)
{
    return n->a & 0x800000ffffffffff; // 40 bits plus high bit of first
}
static inline MTBDD
node_getlow(MTBDD mtbdd, mtbddnode_t node)
{
    return MTBDD_TRANSFERMARK(mtbdd, mtbddnode_getlow(node));
}

static inline MTBDD
node_gethigh(MTBDD mtbdd, mtbddnode_t node)
{
    return MTBDD_TRANSFERMARK(mtbdd, mtbddnode_gethigh(node));
}
TASK_IMPL_3(MTBDD, mtbdd_custom_ite, MTBDD, f, MTBDD, g, MTBDD, h)
{
	/* Terminal cases */
	if (f == custom_true) return g;
	if (f == custom_false) return h;
	if (g == h) return g;
	if (g == custom_true && h == custom_false) return f;
	if (h == custom_true && g == custom_false) return mtbdd_uapply(f, TASK(callback_not), 0);

	// If all MTBDD's are Boolean, then there could be further optimizations (see sylvan_bdd.c)

	/* Maybe perform garbage collection */
	sylvan_gc_test();

	/* Check cache */
	MTBDD result;
	if (cache_get(f | CACHE_MTBDD_ITE, g, h, &result)) return result;

	/* Get top variable */
	int lg = mtbdd_isleaf(g);
	int lh = mtbdd_isleaf(h);
	mtbddnode_t nf = GETNODE(f);
	mtbddnode_t ng = lg ? 0 : GETNODE(g);
	mtbddnode_t nh = lh ? 0 : GETNODE(h);
	uint32_t vf = mtbddnode_getvariable(nf);
	uint32_t vg = lg ? 0 : mtbddnode_getvariable(ng);
	uint32_t vh = lh ? 0 : mtbddnode_getvariable(nh);
	uint32_t v = vf;
	if (!lg && vg < v) v = vg;
	if (!lh && vh < v) v = vh;

	/* Get cofactors */
	MTBDD flow, fhigh, glow, ghigh, hlow, hhigh;
	flow = (vf == v) ? node_getlow(f, nf) : f;
	fhigh = (vf == v) ? node_gethigh(f, nf) : f;
	glow = (!lg && vg == v) ? node_getlow(g, ng) : g;
	ghigh = (!lg && vg == v) ? node_gethigh(g, ng) : g;
	hlow = (!lh && vh == v) ? node_getlow(h, nh) : h;
	hhigh = (!lh && vh == v) ? node_gethigh(h, nh) : h;

	/* Recursive calls */
	mtbdd_refs_spawn(SPAWN(mtbdd_custom_ite, fhigh, ghigh, hhigh));
	MTBDD low = mtbdd_refs_push(CALL(mtbdd_custom_ite, flow, glow, hlow));
	MTBDD high = mtbdd_refs_push(mtbdd_refs_sync(SYNC(mtbdd_custom_ite)));
	result = mtbdd_makenode(v, low, high);
	mtbdd_refs_pop(2);

	/* Store in cache */
	cache_put(f | CACHE_MTBDD_ITE, g, h, result);
	return result;
}
TASK_IMPL_3(MTBDD, mtbdd_custom_abstract, MTBDD, a, MTBDD, v, mtbdd_apply_op, op)
{
    /* Check terminal case */
    if (mtbdd_isleaf(a)) return a;
    if (v == custom_true) return a;

    /* Maybe perform garbage collection */
    sylvan_gc_test();

    /* Check cache */
    MTBDD result;
    if (cache_get(a | CACHE_MTBDD_ABSTRACT, v, (size_t)op, &result)) return result;

    /* a != constant, v != constant */
    mtbddnode_t na = GETNODE(a);
    mtbddnode_t nv = GETNODE(v);

    /* Recursive */
    if (mtbddnode_isleaf(na)) {
        result = CALL(mtbdd_custom_abstract, a, node_gethigh(v, nv), op);
        mtbdd_refs_push(result);
        result = mtbdd_apply(result, result, op);
        mtbdd_refs_pop(1);
    } else {
        uint32_t var_a = mtbddnode_getvariable(na);
        uint32_t var_v = mtbddnode_getvariable(nv);
        if (var_a < var_v) {
            SPAWN(mtbdd_custom_abstract, node_gethigh(a, na), v, op);
            MTBDD low = CALL(mtbdd_custom_abstract, node_getlow(a, na), v, op);
            mtbdd_refs_push(low);
            MTBDD high = SYNC(mtbdd_custom_abstract);
            mtbdd_refs_pop(1);
            result = mtbdd_makenode(var_a, low, high);
        } else if (var_a > var_v) {
            result = CALL(mtbdd_custom_abstract, a, node_gethigh(v, nv), op);
            mtbdd_refs_push(result);
            result = mtbdd_apply(result, result, op);
            mtbdd_refs_pop(1);
        } else /* var_a == var_v */ {
            SPAWN(mtbdd_custom_abstract, node_gethigh(a, na), node_gethigh(v, nv), op);
            MTBDD low = CALL(mtbdd_custom_abstract, node_getlow(a, na), node_gethigh(v, nv), op);
            mtbdd_refs_push(low);
            MTBDD high = SYNC(mtbdd_custom_abstract);
            mtbdd_refs_push(high);
            result = mtbdd_apply(low, high, op);
            mtbdd_refs_pop(2);
        }
    }

    /* Store in cache */
    cache_put(a | CACHE_MTBDD_ABSTRACT, v, (size_t)op, result);
    return result;
}


TASK_IMPL_2(MTBDD, callback_id, MTBDD, a, size_t, param) {
	if(!(mtbdd_isleaf(a))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback1((*ops).id, mtbdd_getvalue(a)));
}
TASK_IMPL_2(MTBDD, callback_not, MTBDD, a, size_t, param) {
	if(!(mtbdd_isleaf(a))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback1((*ops).not, mtbdd_getvalue(a)));
}
TASK_IMPL_2(MTBDD, callback_add, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).add, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_multiply, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).multiply, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_subtract, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).subtract, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_divide, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).divide, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_divide_ignore_zero, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).divide_ignore_zero, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_max, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).max, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_min, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).min, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_and, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).and, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_or, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).or, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_iff, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).iff, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_implies, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).implies, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_eq, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).eq, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
TASK_IMPL_2(MTBDD, callback_ne, MTBDD*, a, MTBDD*, b) {
	if(!(mtbdd_isleaf(*a) && mtbdd_isleaf(*b))) {
		return mtbdd_invalid;
	}
	return mtbdd_makeleaf(3,callback2((*ops).ne, mtbdd_getvalue(*a), mtbdd_getvalue(*b)));
}
