__attribute__ ((visibility("default"))) 
// optimize setting
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
Z3_string z3_optimize_to_string(Z3_context c, Z3_optimize o)
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
__attribute__ ((visibility("default")))
Z3_params z3_mk_params(Z3_context ctx)
{
    return Z3_API Z3_mk_params(ctx);
}
