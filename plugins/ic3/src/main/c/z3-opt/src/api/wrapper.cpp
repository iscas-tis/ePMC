#include<stdio.h>
#include<stdlib.h>
#include<stdarg.h>
#include<memory.h>
#include<setjmp.h>
#include "z3.h"

#define LOG_Z3_CALLS

#ifdef LOG_Z3_CALLS
#define LOG_MSG(msg) Z3_append_log(msg)
#else
#define LOG_MSG(msg) ((void)0)
#endif

// by LI YONG
void error_handler(Z3_context c, Z3_error_code e) 
{
    printf("Error code: %d\n", e);
}

int fromLbool(Z3_lbool b)
{
    if(b == Z3_L_FALSE ) return 0;
    if(b == Z3_L_TRUE) return 1;
    return -1;
}

extern "C" {

//typedef context context_ptr;

//extern const int z3_true  = 1;
//extern const int z3_false = 0;
//extern const int z3_undef = -1;

// configuration set up
__attribute__ ((visibility("default"))) 
Z3_config z3_mk_config() 
{ 
    return Z3_mk_config();
}
__attribute__ ((visibility("default"))) 
void z3_del_config(Z3_config cfg) 
{
    Z3_del_config(cfg);
}
__attribute__ ((visibility("default"))) 
void z3_set_param_value(Z3_config cfg, char* param_id, char * param_val) 
{
    Z3_set_param_value(cfg, param_id, param_val);
}
__attribute__ ((visibility("default"))) 
void z3_set_error_handler(Z3_context ctx) 
{ 
    Z3_set_error_handler(ctx, error_handler); 
}
__attribute__ ((visibility("default"))) 
Z3_context z3_mk_context(Z3_config cfg) 
{
    return Z3_mk_context(cfg);
}
__attribute__ ((visibility("default"))) 
void z3_del_context(Z3_context ctx) 
{
    Z3_del_context(ctx);
}
// type sort 
__attribute__ ((visibility("default"))) 
Z3_sort z3_mk_bool_sort(Z3_context ctx) 
{
    return Z3_mk_bool_sort(ctx);
}
__attribute__ ((visibility("default"))) 
Z3_sort z3_mk_int_sort(Z3_context ctx) 
{
    return Z3_mk_int_sort(ctx);
}
__attribute__ ((visibility("default"))) 
Z3_sort z3_mk_real_sort(Z3_context ctx) 
{
    return Z3_mk_real_sort(ctx);
}
// create variables
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_var(Z3_context ctx, char*  name, Z3_sort ty) 
{
    Z3_symbol  s  = Z3_mk_string_symbol(ctx, name);
    return Z3_mk_const(ctx, s, ty);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_bool_var(Z3_context ctx, char* prefix)
{
    Z3_sort sort = Z3_mk_bool_sort(ctx);
    return z3_mk_var(ctx, prefix, sort);
}
__attribute__ ((visibility("default"))) 
Z3_symbol z3_mk_string_symbol(Z3_context ctx, char* name) 
{
    return Z3_mk_string_symbol(ctx, name);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_int_var(Z3_context ctx, char* prefix)
{
    Z3_sort sort = Z3_mk_int_sort(ctx);
    return z3_mk_var(ctx, prefix, sort);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_real_var(Z3_context ctx, char* prefix)
{
    Z3_sort sort = Z3_mk_real_sort(ctx);
    return z3_mk_var(ctx, prefix, sort);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_bool_const(Z3_context ctx, int b)
{
    if(b == 1) return Z3_mk_true(ctx);
    return Z3_mk_false(ctx);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_int_const(Z3_context ctx, int b)
{
    Z3_sort sort = Z3_mk_int_sort(ctx);
    return Z3_mk_int(ctx, b, sort);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_real(Z3_context ctx, int num, int den)
{
    return Z3_mk_real(ctx, num, den);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_real_const(Z3_context ctx, char* value)
{
    Z3_sort sort = Z3_mk_real_sort(ctx);
    return Z3_mk_numeral(ctx, value, sort);
}
// simplify
__attribute__ ((visibility("default"))) 
Z3_ast z3_simplify(Z3_context ctx, Z3_ast a)
{
    return Z3_simplify(ctx, a);
}
__attribute__ ((visibility("default"))) 
Z3_string z3_ast_to_string(Z3_context ctx, Z3_ast e) 
{
    return Z3_ast_to_string(ctx, e);
}
// bool operation
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_and(Z3_context ctx, int param_num, Z3_ast args[]) 
{
    return Z3_mk_and(ctx, param_num, args);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_or(Z3_context ctx, int param_num, Z3_ast args[]) 
{
    return Z3_mk_or(ctx, param_num, args);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_and_bin(Z3_context ctx, Z3_ast l, Z3_ast r) 
{
    Z3_ast op[2];
    op[0] = l; op[1] = r;
    return Z3_mk_and(ctx, 2, op);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_or_bin(Z3_context ctx, Z3_ast l, Z3_ast r) 
{
    Z3_ast op[2];
    op[0] = l; op[1] = r;
    return Z3_mk_or(ctx, 2, op);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_not (Z3_context ctx, Z3_ast a)
{
    return Z3_mk_not(ctx, a);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_eq(Z3_context ctx, Z3_ast l, Z3_ast r)
{
    return Z3_mk_eq(ctx, l, r);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_true(Z3_context ctx) 
{
    return Z3_mk_true(ctx);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_false(Z3_context ctx) 
{
    return Z3_mk_false(ctx);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_iff(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_iff(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_ite(Z3_context ctx, Z3_ast t1, Z3_ast t2, Z3_ast t3)
{
    return Z3_mk_ite(ctx, t1, t2, t3);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_implies(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_implies(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_add(Z3_context ctx, unsigned num_args, Z3_ast  args[])
{
    return Z3_mk_add(ctx, num_args, args);
}
// algebraic operations  
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_mul(Z3_context ctx, unsigned num_args, Z3_ast args[])
{
    return Z3_mk_mul(ctx, num_args, args);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_sub(Z3_context ctx, unsigned num_args, Z3_ast  args[])
{
    return Z3_mk_sub(ctx, num_args, args);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_sub_bin(Z3_context ctx,  Z3_ast l, Z3_ast r)
{
    Z3_ast op[2];
    op[0] = l; op[1] = r;
    return Z3_mk_sub(ctx, 2, op);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_add_bin(Z3_context ctx, Z3_ast l, Z3_ast r)
{
    Z3_ast op[2];
    op[0] = l; op[1] = r;
    return Z3_mk_add(ctx, 2, op);
}
// algebraic operations  
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_mul_bin(Z3_context ctx, Z3_ast l, Z3_ast r)
{
    Z3_ast op[2];
    op[0] = l; op[1] = r;
    return Z3_mk_mul(ctx, 2, op);
}

__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_unary_minus(Z3_context ctx, Z3_ast arg)
{
    return Z3_mk_unary_minus(ctx, arg);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_gt(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_gt(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_ge(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_ge(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_lt(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_lt(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_le(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_le(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_div(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_div(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_mod(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_mod(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_rem(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_rem(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_power(Z3_context ctx, Z3_ast t1, Z3_ast t2)
{
    return Z3_mk_power(ctx, t1, t2);
}
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_int2real(Z3_context ctx, Z3_ast t1)
{
    return Z3_mk_int2real(ctx, t1);
}    
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_real2int (Z3_context ctx, Z3_ast t1)
{
    return Z3_mk_real2int(ctx, t1);
}  
__attribute__ ((visibility("default"))) 
Z3_ast z3_mk_is_int(Z3_context ctx, Z3_ast t1)
{
    return Z3_mk_is_int(ctx, t1);
}  
// solve process
__attribute__ ((visibility("default"))) 
void z3_assert_cnstr(Z3_context ctx, Z3_ast a)
{
    Z3_assert_cnstr(ctx, a);
}
__attribute__ ((visibility("default"))) 
void z3_solver_assert(Z3_context ctx, Z3_solver s, Z3_ast a)
{
    Z3_solver_assert(ctx, s, a);
}
__attribute__ ((visibility("default"))) 
Z3_solver z3_mk_solver(Z3_context ctx)
{
    return Z3_mk_solver(ctx);
}
__attribute__ ((visibility("default"))) 
void z3_del_model(Z3_context ctx, Z3_model model)
{
    Z3_del_model(ctx, model);
}
__attribute__ ((visibility("default"))) 
int z3_solver_check(Z3_context ctx, Z3_solver s)
{
    Z3_lbool b = Z3_solver_check(ctx, s);
    return fromLbool(b);
}
__attribute__ ((visibility("default"))) 
Z3_model z3_solver_get_model(Z3_context ctx, Z3_solver s)
{
    return Z3_solver_get_model(ctx, s);
}
__attribute__ ((visibility("default"))) 
Z3_string 	z3_solver_to_string(Z3_context ctx, Z3_solver s)
{
    return Z3_solver_to_string(ctx, s);
}
__attribute__ ((visibility("default"))) 
void z3_solver_inc_ref(Z3_context ctx, Z3_solver s)
{
    Z3_solver_inc_ref(ctx, s);
}
__attribute__ ((visibility("default"))) 
void z3_solver_dec_ref(Z3_context ctx, Z3_solver s)
{
    Z3_solver_dec_ref(ctx, s);
}
__attribute__ ((visibility("default"))) 
Z3_string z3_model_eval(Z3_context ctx, Z3_model model, Z3_ast e) 
{
    Z3_ast v;
    Z3_eval(ctx, model, e, &v);
    return Z3_ast_to_string(ctx, v);
}

// optimize solver
__attribute__ ((visibility("default"))) 
Z3_optimize z3_mk_optimize(Z3_context ctx)
{
    return Z3_mk_optimize(ctx);
}
__attribute__ ((visibility("default"))) 
void z3_optimize_inc_ref(Z3_context ctx, Z3_optimize d)
{
    Z3_optimize_inc_ref(ctx, d);
}
__attribute__ ((visibility("default"))) 
void z3_optimize_dec_ref(Z3_context ctx, Z3_optimize d)
{
    Z3_API Z3_optimize_dec_ref(ctx, d);
}
__attribute__ ((visibility("default"))) 
void z3_optimize_set_params(Z3_context ctx, Z3_optimize o, Z3_params p)
{
    Z3_optimize_set_params(ctx, o, p);
}
__attribute__ ((visibility("default"))) 
void z3_optimize_assert(Z3_context ctx, Z3_optimize o, Z3_ast a)
{
    Z3_optimize_assert(ctx, o, a);
}
__attribute__ ((visibility("default")))
unsigned z3_optimize_assert_soft(Z3_context ctx, Z3_optimize o, Z3_ast a, Z3_string weight, Z3_symbol id)
{
    return Z3_optimize_assert_soft(ctx, o, a, weight, id);
}
__attribute__ ((visibility("default")))
unsigned z3_optimize_maximize(Z3_context ctx, Z3_optimize o, Z3_ast t)
{
    return Z3_optimize_maximize(ctx, o, t);
}
__attribute__ ((visibility("default")))
unsigned z3_optimize_minimize(Z3_context ctx, Z3_optimize o, Z3_ast t)
{
    return Z3_optimize_minimize(ctx, o, t);
}
__attribute__ ((visibility("default")))
void z3_optimize_push(Z3_context ctx,Z3_optimize d)
{
    Z3_optimize_push(ctx, d);
}
__attribute__ ((visibility("default")))
void z3_optimize_pop(Z3_context ctx,Z3_optimize d)
{
    Z3_optimize_pop(ctx, d);
}
__attribute__ ((visibility("default")))
int z3_optimize_check(Z3_context ctx, Z3_optimize o)
{
    Z3_lbool b = Z3_optimize_check(ctx, o);
    return fromLbool(b);
}
__attribute__ ((visibility("default")))
Z3_model z3_optimize_get_model(Z3_context ctx, Z3_optimize o)
{
    return Z3_optimize_get_model( ctx, o);
}
__attribute__ ((visibility("default")))
Z3_string z3_optimize_to_string(Z3_context ctx, Z3_optimize o)
{
    return Z3_optimize_to_string(ctx, o);
}
__attribute__ ((visibility("default")))
Z3_ast z3_optimize_get_upper(Z3_context ctx, Z3_optimize o, unsigned idx)
{
    return Z3_optimize_get_upper(ctx, o, idx);
}
__attribute__ ((visibility("default")))
Z3_ast z3_optimize_get_lower(Z3_context ctx, Z3_optimize o, unsigned idx)
{
    return Z3_optimize_get_lower(ctx, o, idx);
}

//params setting
__attribute__ ((visibility("default")))
Z3_params z3_mk_params(Z3_context ctx)
{
    return Z3_API Z3_mk_params(ctx);
}
__attribute__ ((visibility("default")))
void z3_params_inc_ref(Z3_context ctx, Z3_params m_params) 
{
    Z3_params_inc_ref(ctx, m_params);
}
__attribute__ ((visibility("default")))
void z3_params_dec_ref(Z3_context ctx, Z3_params m_params) 
{
    Z3_params_dec_ref(ctx, m_params);
}
__attribute__ ((visibility("default")))
void z3_params_set_symbol(Z3_context ctx, Z3_params p, Z3_symbol k, Z3_symbol v)
{
    Z3_params_set_symbol(ctx, p, k, v);
}

}



