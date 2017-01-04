package epmc.constraintsolver;

import static epmc.value.OperatorNames.ADD;
import static epmc.value.OperatorNames.AND;
import static epmc.value.OperatorNames.CEIL;
import static epmc.value.OperatorNames.DIVIDE;
import static epmc.value.OperatorNames.EQ;
import static epmc.value.OperatorNames.FLOOR;
import static epmc.value.OperatorNames.GE;
import static epmc.value.OperatorNames.GT;
import static epmc.value.OperatorNames.IFF;
import static epmc.value.OperatorNames.IMPLIES;
import static epmc.value.OperatorNames.ITE;
import static epmc.value.OperatorNames.LE;
import static epmc.value.OperatorNames.LOG;
import static epmc.value.OperatorNames.LT;
import static epmc.value.OperatorNames.MAX;
import static epmc.value.OperatorNames.MIN;
import static epmc.value.OperatorNames.MOD;
import static epmc.value.OperatorNames.MULTIPLY;
import static epmc.value.OperatorNames.NE;
import static epmc.value.OperatorNames.NOT;
import static epmc.value.OperatorNames.OR;
import static epmc.value.OperatorNames.POW;
import static epmc.value.OperatorNames.SUBTRACT;

import java.util.ArrayList;
import java.util.HashMap;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import epmc.expression.ContextExpression;
import epmc.expression.Expression;
import epmc.util.JNATools;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

import com.sun.jna.Pointer;

public class Z3SMT {
	
	private static final class Z3Solver {
		 
		static native Pointer z3_mk_config();
		static native void z3_del_config(Pointer cfg);
		static native void z3_set_param_value(Pointer cfg, String param_id, String param_val);
		static native void z3_set_error_handler(Pointer ctx);
		static native Pointer z3_mk_context(Pointer cfg);
		static native void z3_del_context(Pointer ctx);
		// type sort 
		static native Pointer z3_mk_string_symbol(Pointer ctx, String name);
		static native Pointer z3_mk_bool_sort(Pointer ctx);
		static native Pointer z3_mk_int_sort(Pointer ctx);
		static native Pointer z3_mk_real_sort(Pointer ctx);
		static native int z3_get_sort(Pointer ctx, Pointer a);
		// create variables
		static native Pointer z3_mk_var(Pointer ctx, String  name, Pointer ty);
		static native Pointer z3_mk_bool_var(Pointer ctx, String prefix);
		static native Pointer z3_mk_int_var(Pointer ctx, String prefix);
		static native Pointer z3_mk_real_var(Pointer ctx, String prefix);
		static native Pointer z3_mk_bool_const(Pointer ctx, int b);
		static native Pointer z3_mk_int_const(Pointer ctx, int b);
		static native Pointer z3_mk_real(Pointer ctx, int num, int den); /* input num/den */
		static native Pointer z3_mk_real_const(Pointer ctx, String value); // should be num/den
		// simplify
		static native String z3_ast_to_string(Pointer ctx, Pointer e); 
		static native Pointer z3_simplify(Pointer ctx, Pointer a);
		// bool operation
		// quantifier elimination 
		static native Pointer z3_mk_exists(Pointer ctx, Pointer var, Pointer body);
//		static native Pointer z3_mk_exists_arr(Pointer ctx, int num, Pointer vars, Pointer body);
//		static native Pointer z3_mk_and(Pointer ctx, int param_num, Pointer[] args);
//		static native Pointer z3_mk_or(Pointer ctx, int param_num, Pointer[] args);
		static native Pointer z3_mk_and_bin(Pointer ctx, Pointer l, Pointer r);
		static native Pointer z3_mk_or_bin(Pointer ctx, Pointer l, Pointer r);
		static native Pointer z3_mk_not (Pointer ctx, Pointer a);
		 
		static native Pointer z3_mk_eq(Pointer ctx, Pointer l, Pointer r);
		static native Pointer z3_mk_true(Pointer ctx);		 
		static native Pointer z3_mk_false(Pointer ctx);
		static native Pointer z3_mk_iff(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_ite(Pointer ctx, Pointer t1, Pointer t2, Pointer t3);
		static native Pointer z3_mk_implies(Pointer ctx, Pointer t1, Pointer t2);

		// algebraic operations  
//		static native Pointer z3_mk_add(Pointer ctx, int num_args, Pointer[]  args);
//		static native Pointer z3_mk_mul(Pointer ctx, int num_args, Pointer[] args);
//		static native Pointer z3_mk_sub(Pointer ctx, int num_args, Pointer[]  args);
		static native Pointer z3_mk_sub_bin(Pointer ctx,  Pointer l, Pointer r);
		static native Pointer z3_mk_add_bin(Pointer ctx, Pointer l, Pointer r);
		static native Pointer z3_mk_mul_bin(Pointer ctx, Pointer l, Pointer r);
		// algebraic operations  
		 
		static native Pointer z3_mk_unary_minus(Pointer ctx, Pointer arg);
		static native Pointer z3_mk_gt(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_ge(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_lt(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_le(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_div(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_mod(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_rem(Pointer ctx, Pointer t1, Pointer t2); 
		static native Pointer z3_mk_power(Pointer ctx, Pointer t1, Pointer t2);
		static native Pointer z3_mk_int2real(Pointer ctx, Pointer t1);
		static native Pointer z3_mk_real2int (Pointer ctx, Pointer t1);
		static native Pointer z3_mk_is_int(Pointer ctx, Pointer t1);
		// solve process
		static native void z3_push(Pointer ctx);
		static native void z3_pop(Pointer ctx, int num);
		static native void z3_solver_push(Pointer ctx, Pointer s);
		static native void z3_solver_pop(Pointer ctx, Pointer s, int num);
		static native void z3_solver_reset(Pointer ctx, Pointer s);
		static native void z3_assert_cnstr(Pointer ctx, Pointer a);
		static native void z3_solver_assert(Pointer ctx, Pointer s, Pointer a);
		static native Pointer z3_mk_solver(Pointer ctx);
		static native void z3_del_model(Pointer ctx, Pointer model);
		static native int z3_solver_check(Pointer ctx, Pointer s);
		static native Pointer z3_solver_get_model(Pointer ctx, Pointer s);
		static native String z3_solver_to_string(Pointer ctx, Pointer s);
		static native void z3_solver_inc_ref(Pointer ctx, Pointer s);
		static native void z3_solver_dec_ref(Pointer ctx, Pointer s);
		static native String z3_model_eval(Pointer ctx, Pointer model, Pointer e);
		static native String z3_model_get_value(Pointer ctx, Pointer model, Pointer e);
		static native int z3_solver_check_assumption(Pointer ctx, Pointer s
				 , final Pointer assumption);
		/* optimize solver */
		static native Pointer z3_mk_optimize(Pointer ctx);
		static native void z3_optimize_set_params(Pointer ctx, Pointer solver, Pointer param);
		static native  void z3_optimize_inc_ref(Pointer ctx, Pointer solver);
		static native  void z3_optimize_dec_ref(Pointer ctx, Pointer solver);
		static native  void z3_optimize_assert(Pointer ctx, Pointer solver, Pointer a);
		static native  int z3_optimize_assert_soft(Pointer ctx, Pointer solver, Pointer a, String weight, Pointer id);
		static native  int z3_optimize_maximize(Pointer ctx, Pointer solver, Pointer t);
		static native  int z3_optimize_minimize(Pointer ctx, Pointer solver, Pointer t);
		
		static native  void z3_optimize_push(Pointer ctx,Pointer solver);
		static native  void z3_optimize_pop(Pointer ctx,Pointer solver);
		static native  int z3_optimize_check(Pointer ctx, Pointer solver);
		static native Pointer z3_optimize_get_model(Pointer ctx, Pointer solver);
		static native String z3_optimize_to_string(Pointer ctx, Pointer solver);
		static native Pointer z3_optimize_get_upper(Pointer ctx, Pointer solver, int idx);
		static native Pointer z3_optimize_get_lower(Pointer ctx, Pointer solver, int idx);
		
		// parameter setting for optimize
		static native Pointer z3_mk_params(Pointer ctx);
		static native void z3_params_inc_ref(Pointer ctx, Pointer m_params);
		static native void z3_params_dec_ref(Pointer ctx, Pointer m_params);
		static native void z3_params_set_symbol(Pointer ctx, Pointer params, Pointer sym_k, Pointer sym_v);

        private final static boolean loaded =
                JNATools.registerLibrary(Z3Solver.class, "z3");
	}
	
	
	
	public static void test() {
	    Pointer ctx;
	    Pointer cfg = Z3Solver.z3_mk_config();
	    Z3Solver.z3_set_param_value(cfg, "model", "true");
	    Pointer x, y, x_xor_y;

	    ctx     = Z3Solver.z3_mk_context(cfg);
	    Z3Solver.z3_set_error_handler(ctx);
	    Z3Solver.z3_del_config(cfg);
        /*
	    x       = Z3Solver.z3_mk_bool_var(ctx, "x");
	    y       = Z3Solver.z3_mk_bool_var(ctx, "y");
	    x_xor_y = Z3Solver.z3_mk_or_bin(ctx, x, y);

	    System.out.print("model for: x or y\n");
	    Pointer solver = Z3Solver.z3_mk_solver(ctx);
	    Z3Solver.z3_solver_assert(ctx, solver, x_xor_y);
	    int status = Z3Solver.z3_solver_check(ctx, solver);
	    
	    if(status == -1)  {
	    	System.out.println("UNDEF");
	    }else if(status == 0) {
	    	System.out.println("UNSAT");
	    }else {
	    	System.out.println("SAT");
		    Pointer m = Z3Solver.z3_solver_get_model(ctx, solver);
		    System.out.println(Z3Solver.z3_solver_to_string(ctx, solver));
		    
		    System.out.println("x=" + Z3Solver.z3_model_eval(ctx, m, x));
		    System.out.println("y=" + Z3Solver.z3_model_eval(ctx, m, y));
		    Z3Solver.z3_del_model(ctx, m);
	    }
	    */
	    
	    /* max 3x + 4y
	     * s.t.
	     *   x+2y <= 14 
	     *   3x - y >= 0
	     *   x - y <= 2
	     *   */
	    
	    System.out.println("start ...");
	    Pointer opt = Z3Solver.z3_mk_optimize(ctx);
	    Z3Solver.z3_optimize_inc_ref(ctx, opt);
	    // set up 
	    
	    Pointer x_0 = Z3Solver.z3_mk_real_var(ctx, "x0");
	    Pointer y_0 = Z3Solver.z3_mk_real_var(ctx, "y0");
	    Pointer two = Z3Solver.z3_mk_int_const(ctx, 2);
	    Pointer thr = Z3Solver.z3_mk_int_const(ctx, 3);
	    Pointer fur = Z3Solver.z3_mk_int_const(ctx, 4);
	    Pointer zero = Z3Solver.z3_mk_int_const(ctx, 0);
	    Pointer ft = Z3Solver.z3_mk_int_const(ctx, 14);
	    Pointer obj = Z3Solver.z3_mk_add_bin(ctx
	    		, Z3Solver.z3_mk_mul_bin(ctx, thr, x_0), Z3Solver.z3_mk_mul_bin(ctx, fur, y_0));
	    
	    Pointer cnstr1_l = Z3Solver.z3_mk_add_bin(ctx, x_0,
	    		Z3Solver.z3_mk_mul_bin(ctx, two, y_0));
	    Pointer cnstr1 = Z3Solver.z3_mk_le(ctx, cnstr1_l, ft);
	    Z3Solver.z3_optimize_assert(ctx, opt, cnstr1);
	    Pointer cnstr2_l = Z3Solver.z3_mk_sub_bin(ctx, Z3Solver.z3_mk_mul_bin(ctx, thr, x_0),
	    		y_0);
	    Pointer cnstr2 = Z3Solver.z3_mk_ge(ctx, cnstr2_l, zero); 
	    Z3Solver.z3_optimize_assert(ctx, opt, cnstr2);
	    Pointer cnstr3_l = Z3Solver.z3_mk_sub_bin(ctx, x_0,
	    		y_0);
	    Pointer cnstr3 = Z3Solver.z3_mk_le(ctx, cnstr3_l, two);
	    Z3Solver.z3_optimize_assert(ctx, opt, cnstr3);
	    System.out.println("status =" + Z3Solver.z3_optimize_maximize(ctx, opt, obj));
	    System.out.println("computing...");
	    int status = Z3Solver.z3_optimize_check(ctx, opt);
	    System.out.println("return value= " + status);
	    if(status == 1) {
	    	System.out.println("SAT");
		    Pointer m = Z3Solver.z3_solver_get_model(ctx, opt);
		    
		    System.out.println("x=" + Z3Solver.z3_model_eval(ctx, m, x_0));
		    System.out.println("y=" + Z3Solver.z3_model_eval(ctx, m, y_0));
		    Z3Solver.z3_del_model(ctx, m);
	    }
	    System.out.println("pop...");
	    /*
	    Pointer opt = Z3Solver.z3_mk_optimize(ctx);
	    x = Z3Solver.z3_mk_int_var(ctx, "x");
	    Pointer one = Z3Solver.z3_mk_int_const(ctx, 1);
	    Pointer ten = Z3Solver.z3_mk_int_const(ctx, 10);
	    Pointer cnstr1 = Z3Solver.z3_mk_le(ctx, x, ten);
	    Pointer cnstr2 = Z3Solver.z3_mk_ge(ctx, x, one);
	    Z3Solver.z3_optimize_assert(ctx, opt, cnstr1);
	    Z3Solver.z3_optimize_assert(ctx, opt, cnstr2);
	    Z3Solver.z3_optimize_maximize(ctx, opt, x);
	    System.out.println("computing...");
//	    Z3Solver.z3_optimize_push(ctx, opt);
	    if(Z3Solver.z3_optimize_check(ctx, opt) == 1) {
	    	System.out.println("SAT....");
		    Pointer m = Z3Solver.z3_optimize_get_model(ctx, opt);
		    
		    System.out.println("x=" + Z3Solver.z3_model_eval(ctx, m, x));
		    Z3Solver.z3_del_model(ctx, m);
	    }
	    System.out.println("pop....");
//	    Z3Solver.z3_optimize_pop(ctx, opt);*/
	    Z3Solver.z3_optimize_dec_ref(ctx, opt);
	    Z3Solver.z3_del_context(ctx);
	}
	
	public static void test_opt() {
	    Pointer ctx;
	    Pointer cfg = Z3Solver.z3_mk_config();
	    Z3Solver.z3_set_param_value(cfg, "model", "true");
	    Pointer x, y;
	    
	    ctx     = Z3Solver.z3_mk_context(cfg);
	    Z3Solver.z3_set_error_handler(ctx);
	    Z3Solver.z3_del_config(cfg);
	    
	    Pointer opt = Z3Solver.z3_mk_optimize(ctx);
	    Z3Solver.z3_optimize_inc_ref(ctx, opt);
	    Pointer param = Z3Solver.z3_mk_params(ctx);
	    Z3Solver.z3_params_inc_ref(ctx, param);
	    Z3Solver.z3_params_set_symbol(ctx, param, Z3Solver.z3_mk_string_symbol(ctx, "priority")
	    		,  Z3Solver.z3_mk_string_symbol(ctx, "pareto"));
	    Z3Solver.z3_optimize_set_params(ctx, opt, param);
	    
	    x       = Z3Solver.z3_mk_int_var(ctx, "x");
	    y       = Z3Solver.z3_mk_int_var(ctx, "y");
	    Pointer ten = Z3Solver.z3_mk_int_const(ctx, 10);
	    Pointer ele = Z3Solver.z3_mk_int_const(ctx, 11);
	    Pointer zero = Z3Solver.z3_mk_int_const(ctx, 0);
	    Pointer x_b = Z3Solver.z3_mk_and_bin(ctx, Z3Solver.z3_mk_ge(ctx, ten, x)
	    		, Z3Solver.z3_mk_le(ctx, zero, x));
	    Pointer y_b = Z3Solver.z3_mk_and_bin(ctx, Z3Solver.z3_mk_ge(ctx, ten, y)
	    		, Z3Solver.z3_mk_le(ctx, zero, y));
	    Pointer ieq = Z3Solver.z3_mk_le(ctx, Z3Solver.z3_mk_add_bin(ctx, x, y)
	    		,ele);
	    
	    Z3Solver.z3_optimize_assert(ctx, opt, x_b);
	    Z3Solver.z3_optimize_assert(ctx, opt, y_b);
	    Z3Solver.z3_optimize_assert(ctx, opt, ieq);
	    
	    int x_idx = Z3Solver.z3_optimize_maximize(ctx, opt, x);
	    int y_idx = Z3Solver.z3_optimize_maximize(ctx, opt, y);
	    
	    int status = Z3Solver.z3_optimize_check(ctx, opt);
	    
	    if(status == 1) {
	    	Pointer x_v = Z3Solver.z3_optimize_get_lower(ctx, opt, x_idx);
	    	Pointer y_v = Z3Solver.z3_optimize_get_lower(ctx, opt, y_idx);
	    	System.out.println("x=" + Z3Solver.z3_ast_to_string(ctx, x_v));
	    	System.out.println("y=" + Z3Solver.z3_ast_to_string(ctx, y_v));
	    }
	    
	    Z3Solver.z3_params_dec_ref(ctx, param);
	    Z3Solver.z3_optimize_dec_ref(ctx, opt);
	    Z3Solver.z3_del_context(ctx);
	    
	}
	
	public static void testLP() {
	    Pointer ctx;
	    Pointer cfg = Z3Solver.z3_mk_config();
	    Z3Solver.z3_set_param_value(cfg, "model", "true");
	    Pointer x1, x2, x3;

	    ctx     = Z3Solver.z3_mk_context(cfg);
	    Z3Solver.z3_set_error_handler(ctx);
	    Z3Solver.z3_del_config(cfg);
	    System.out.println("start ...");
	    Pointer solver = Z3Solver.z3_mk_solver(ctx);
	    Z3Solver.z3_solver_inc_ref(ctx, solver);
	    // set up 
	    
	    x1 = Z3Solver.z3_mk_real_var(ctx, "x1");
	    x2 = Z3Solver.z3_mk_real_var(ctx, "x2");
	    x3 = Z3Solver.z3_mk_real_var(ctx, "x3");
	    
	    Pointer one2two = Z3Solver.z3_mk_real_const(ctx, "1/2");
//	    Pointer one2six = Z3Solver.z3_mk_real_const(ctx, "1/6");
//	    Pointer two2thr = Z3Solver.z3_mk_real_const(ctx, "2/3");
//	    Pointer one2two = Z3Solver.z3_mk_real(ctx, 1, 2);
//	    Pointer one2six = Z3Solver.z3_mk_real(ctx, 1, 6);
//	    Pointer two2thr = Z3Solver.z3_mk_real(ctx, 2, 3);

	    Pointer one = Z3Solver.z3_mk_int_const(ctx, 1);
	    Pointer zero = Z3Solver.z3_mk_int_const(ctx, 0);
	    /* LP program */
	    /* belong to [0,1]*/
	    Pointer x1_b = Z3Solver.z3_mk_and_bin(ctx, Z3Solver.z3_mk_ge(ctx, one, x1)
	    		, Z3Solver.z3_mk_le(ctx, zero, x1));
	    Pointer x2_b = Z3Solver.z3_mk_and_bin(ctx, Z3Solver.z3_mk_ge(ctx, one, x2)
	    		, Z3Solver.z3_mk_le(ctx, zero, x2));
	    Pointer x3_b = Z3Solver.z3_mk_and_bin(ctx, Z3Solver.z3_mk_ge(ctx, one, x3)
	    		, Z3Solver.z3_mk_le(ctx, zero, x3));
	    /* x1 = 1/2 * x2 */
	    Pointer cnstr1 = Z3Solver.z3_mk_eq(ctx, x1, Z3Solver.z3_mk_mul_bin(ctx, one2two, x2));
	    /* x2 = 1/2 * x3 */
	    Pointer cnstr2 = Z3Solver.z3_mk_eq(ctx, x2, Z3Solver.z3_mk_mul_bin(ctx, one2two, x3));
	    /* x3 = 1/2 * x2 + 1/2 */
	    Pointer cnstr3 = Z3Solver.z3_mk_eq(ctx, x3, Z3Solver.z3_mk_add_bin(ctx, one2two, 
	    		Z3Solver.z3_mk_mul_bin(ctx, one2two, x2)));
	    
	    Z3Solver.z3_solver_assert(ctx, solver, x1_b);
	    Z3Solver.z3_solver_assert(ctx, solver, x2_b);
	    Z3Solver.z3_solver_assert(ctx, solver, x3_b);
	    Z3Solver.z3_solver_assert(ctx, solver, cnstr1);
	    Z3Solver.z3_solver_assert(ctx, solver, cnstr2);
	    Z3Solver.z3_solver_assert(ctx, solver, cnstr3);
	    
	    int status = Z3Solver.z3_solver_check(ctx, solver);
	    
	    if(status == 1) {
	    	System.out.println("SAT....");
		    Pointer m = Z3Solver.z3_solver_get_model(ctx, solver);
		    
		    System.out.println("x1=" + Z3Solver.z3_model_eval(ctx, m, x1));
		    System.out.println("x2=" + Z3Solver.z3_model_eval(ctx, m, x2));
		    System.out.println("x3=" + Z3Solver.z3_model_eval(ctx, m, x3));
		    System.out.println("x2+x3=" + Z3Solver.z3_model_eval(ctx, m, Z3Solver.z3_mk_add_bin(ctx, x2, x3)));
		    System.out.println("x1+x2=" + Z3Solver.z3_model_eval(ctx, m, Z3Solver.z3_mk_add_bin(ctx, x1, x2)));
		    Z3Solver.z3_del_model(ctx, m);
	    }
	    System.out.println("status = " + status);
	    Z3Solver.z3_solver_dec_ref(ctx, solver);
	    Z3Solver.z3_del_context(ctx);
	    
	}
	
	public static void testOptimize() {
	    Pointer ctx;
	    Pointer cfg = Z3Solver.z3_mk_config();
	    Z3Solver.z3_set_param_value(cfg, "model", "true");
	    Pointer delta1, delta2, gama1, gama2, gama3, gama4, gama5;
	    
	    ctx     = Z3Solver.z3_mk_context(cfg);
	    Z3Solver.z3_set_error_handler(ctx);
	    Z3Solver.z3_del_config(cfg);
//	    Z3Solver.z3_del_config(cfg);
	    Pointer opt = Z3Solver.z3_mk_optimize(ctx);
	    Z3Solver.z3_optimize_inc_ref(ctx, opt);
	    Pointer param = Z3Solver.z3_mk_params(ctx);
	    Z3Solver.z3_params_inc_ref(ctx, param);
	    Z3Solver.z3_params_set_symbol(ctx, param, Z3Solver.z3_mk_string_symbol(ctx, "priority")
	    		,  Z3Solver.z3_mk_string_symbol(ctx, "pareto"));
	    Z3Solver.z3_optimize_set_params(ctx, opt, param);
	    
	    delta1       = Z3Solver.z3_mk_int_var(ctx, "d1");
	    delta2       = Z3Solver.z3_mk_int_var(ctx, "d2");
	    
	    gama1        = Z3Solver.z3_mk_real_var(ctx, "g1");
	    gama2        = Z3Solver.z3_mk_real_var(ctx, "g2");
	    gama3        = Z3Solver.z3_mk_real_var(ctx, "g3");
	    gama4        = Z3Solver.z3_mk_real_var(ctx, "g4");
	    gama5        = Z3Solver.z3_mk_real_var(ctx, "g5");
	    
	    Pointer one = Z3Solver.z3_mk_int_const(ctx, 1);
	    Pointer zero = Z3Solver.z3_mk_int_const(ctx, 0);
	    Pointer two = Z3Solver.z3_mk_int_const(ctx, 2);
	    
	    Pointer nottrue = Z3Solver.z3_mk_bool_const(ctx, 0);
	    
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_le(ctx, zero, delta1));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_le(ctx, zero, delta2));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_ge(ctx, one, delta1));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_ge(ctx, one, delta2));
	    
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_le(ctx, zero, gama1));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_le(ctx, zero, gama2));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_le(ctx, zero, gama3));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_le(ctx, zero, gama4));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_le(ctx, zero, gama5));
	    
	    Pointer cnstr1_left = Z3Solver.z3_mk_add_bin(ctx
	    		, Z3Solver.z3_mk_sub_bin(ctx, gama2, gama1)
	    		, Z3Solver.z3_mk_sub_bin(ctx, gama3, Z3Solver.z3_mk_mul_bin(ctx, two, gama4)));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_ge(ctx, cnstr1_left, delta1));
	    
	    Pointer cnstr2_left = Z3Solver.z3_mk_add_bin(ctx
	    		, Z3Solver.z3_mk_sub_bin(ctx, gama1, gama2)
	    		, Z3Solver.z3_mk_sub_bin(ctx, gama3, Z3Solver.z3_mk_mul_bin(ctx, two, gama5)));
	    Z3Solver.z3_optimize_assert(ctx, opt, Z3Solver.z3_mk_ge(ctx, cnstr2_left, delta2));
	    
	    int idx = Z3Solver.z3_optimize_maximize(ctx, opt, Z3Solver.z3_mk_add_bin(ctx, delta1, delta2));
//	    int y_idx = Z3Solver.z3_optimize_maximize(ctx, opt, y);
	    
	    int status = Z3Solver.z3_optimize_check(ctx, opt);
	    
	    if(status == 1) {
	    	Pointer x_v = Z3Solver.z3_optimize_get_lower(ctx, opt, idx);
	    	System.out.println("opt=" + Z3Solver.z3_ast_to_string(ctx, x_v));
	    	Pointer m = Z3Solver.z3_optimize_get_model(ctx, opt);
	    	
		    System.out.println("g1=" + Z3Solver.z3_model_eval(ctx, m, gama1));
		    System.out.println("g2=" + Z3Solver.z3_model_eval(ctx, m, gama2));
		    System.out.println("g3=" + Z3Solver.z3_model_eval(ctx, m, gama3));
		    System.out.println("g4=" + Z3Solver.z3_model_eval(ctx, m, gama4));
		    System.out.println("g5=" + Z3Solver.z3_model_eval(ctx, m, gama5));
		    System.out.println("false=" + Z3Solver.z3_ast_to_string(ctx, nottrue));
		    Z3Solver.z3_del_model(ctx, m);
	    }
	    System.out.println("delete context");
	    Z3Solver.z3_params_dec_ref(ctx, param);
	    Z3Solver.z3_optimize_dec_ref(ctx, opt);
	    Z3Solver.z3_del_context(ctx);
	    
	}
	
	public static void testExists() {
	    Pointer ctx;
	    Pointer cfg = Z3Solver.z3_mk_config();
	    Z3Solver.z3_set_param_value(cfg, "auto_config", "true");
	    Pointer s1,s2,act, s3;
	    
	    ctx     = Z3Solver.z3_mk_context(cfg);
	    Z3Solver.z3_set_error_handler(ctx);
	    Z3Solver.z3_del_config(cfg);
	    
	    s1 = Z3Solver.z3_mk_int_var(ctx, "s1");
	    s2 = Z3Solver.z3_mk_int_var(ctx, "s2");
	    act = Z3Solver.z3_mk_int_var(ctx, "a");
	    s3 = Z3Solver.z3_mk_int_var(ctx, "s3");
	    
	    Pointer one = Z3Solver.z3_mk_int_const(ctx, 1);
	    Pointer zero = Z3Solver.z3_mk_int_const(ctx, 0);
	    Pointer two = Z3Solver.z3_mk_int_const(ctx, 2);
	    
	    Pointer tr1 = Z3Solver.z3_mk_and_bin(ctx, Z3Solver.z3_mk_eq(ctx, two, s2)
	    		, Z3Solver.z3_mk_eq(ctx, one, s1));
	    tr1 = Z3Solver.z3_mk_and_bin(ctx, tr1
	    		, Z3Solver.z3_mk_eq(ctx, zero, act));
	    tr1 = Z3Solver.z3_mk_and_bin(ctx, tr1
	    		, Z3Solver.z3_mk_eq(ctx, one, s2));
	    
	    Pointer ex1 = Z3Solver.z3_mk_exists(ctx, s2, tr1);
	    System.out.println("before: " + Z3Solver.z3_ast_to_string(ctx, ex1));
	    ex1 = Z3Solver.z3_simplify(ctx, ex1);
	    System.out.println("after: " + Z3Solver.z3_ast_to_string(ctx, ex1));
	    
	    Pointer tr2 = Z3Solver.z3_mk_and_bin(ctx, Z3Solver.z3_mk_eq(ctx, two, s2)
	    		, Z3Solver.z3_mk_le(ctx, one, s1));
	    tr2 = Z3Solver.z3_mk_and_bin(ctx, tr2
	    		, Z3Solver.z3_mk_eq(ctx, one, act));
	    tr2 = Z3Solver.z3_mk_and_bin(ctx, tr2
	    		, Z3Solver.z3_mk_le(ctx, two, s2));
	    
	    Pointer solver = Z3Solver.z3_mk_solver(ctx);
	    Z3Solver.z3_solver_inc_ref(ctx, solver);
	    // set up
	    
	    Z3Solver.z3_solver_assert(ctx, solver, ex1);
	    
	    
	    
	    int status = Z3Solver.z3_solver_check(ctx, solver);
	    
	    if(status == 1) {
	    	System.out.println("SAT....");
		    Pointer m = Z3Solver.z3_solver_get_model(ctx, solver);
		    
		    System.out.println("s1=" + Z3Solver.z3_model_eval(ctx, m, s1) + " " + Z3Solver.z3_model_get_value(ctx, m, s1));
		    System.out.println("s3=" + Z3Solver.z3_model_eval(ctx, m, s3) + " " + Z3Solver.z3_model_get_value(ctx, m, s3));
		    System.out.println("a=" + Z3Solver.z3_model_eval(ctx, m, act) + " " + Z3Solver.z3_model_get_value(ctx, m, act));
		    Z3Solver.z3_del_model(ctx, m);
	    }
	    System.out.println("status = " + status);
	    Z3Solver.z3_solver_dec_ref(ctx, solver);
	    
	    Pointer ex2 = Z3Solver.z3_mk_exists(ctx, s2, tr2);
	    System.out.println("before: " + Z3Solver.z3_ast_to_string(ctx, ex2));
	    ex2 = Z3Solver.z3_simplify(ctx, ex2);
	    System.out.println("after: " + Z3Solver.z3_ast_to_string(ctx, ex2));
	    
	    solver = Z3Solver.z3_mk_solver(ctx);
	    Z3Solver.z3_solver_inc_ref(ctx, solver);
	    Z3Solver.z3_solver_assert(ctx, solver, ex2);
	    
	    status = Z3Solver.z3_solver_check(ctx, solver);
	    
	    if(status == 1) {
	    	System.out.println("SAT...." + Z3Solver.z3_solver_to_string(ctx, solver));
		    Pointer m = Z3Solver.z3_solver_get_model(ctx, solver);
		    
		    System.out.println("s1=" + Z3Solver.z3_model_eval(ctx, m, s1) + " " + Z3Solver.z3_model_get_value(ctx, m, s1));
		    System.out.println("s3=" + Z3Solver.z3_model_eval(ctx, m, s3) + " " + Z3Solver.z3_model_get_value(ctx, m, s3));
		    System.out.println("a=" + Z3Solver.z3_model_eval(ctx, m, act) + " " + Z3Solver.z3_model_get_value(ctx, m, act));
		    Z3Solver.z3_del_model(ctx, m);
	    }
	    System.out.println("status = " + status);
	    Z3Solver.z3_solver_dec_ref(ctx, solver);
	    
	    Z3Solver.z3_del_context(ctx);
	    
	}
	
	
	// ------------------ implementation of SMT Solver
	private Pointer configuration;

	
	private TObjectIntMap<String> varMap = new TObjectIntHashMap<>();
	private ArrayList<Pointer> variables = new ArrayList<>();
	private int numberOfVariables;
	
	private final String notTrue = "false";
	
	private ContextExpression context; 
	
	public Z3SMT(ContextExpression context) {
		this.context = context;
	    Pointer cfg = Z3Solver.z3_mk_config();
	    Z3Solver.z3_set_param_value(cfg, "auto_config", "true");
	    
	    configuration   = Z3Solver.z3_mk_context(cfg);
	    Z3Solver.z3_set_error_handler(configuration);
	    Z3Solver.z3_del_config(cfg);
	}
	
	public SMTSolver newSolver() {
		return new Z3SMTSolver();
	}
	
	public void close() {
		Z3Solver.z3_del_context(configuration);
	}

	private Pointer transformToSMTExpr(Expression expr) {
		assert expr != null;
		assert expr.isOperator() || expr.isIdentifier() || expr.isLiteral();
		
		if(expr.isIdentifier()) {
	        String name = expr.getName();
	        assert varMap.get(name) != -1;
	        return variables.get(varMap.get(name));
		}else if(expr.isLiteral()){
			Value value = expr.getValue();
			assert value.isBoolean() || value.isDouble() || value.isInteger();
			if(value.isBoolean()) {
				return Z3Solver.z3_mk_bool_const(configuration, value.isFalse()? 0 : 1);
			}else if(value.isInteger()){
				return Z3Solver.z3_mk_int_const(configuration, value.getInt());
			}
			return Z3Solver.z3_mk_real_const(configuration, value.toString());
		}
		ArrayList<Pointer> exprArr = new ArrayList<Pointer>();
		Operator operator = expr.getOperator();
		Pointer result = null, op1, op2, op3;
		switch(operator.getIdentifier()) {
		case     ADD : 
			for (Expression e : expr.getChildren()) {
				exprArr.add(transformToSMTExpr(e));
			}
			for (Pointer e : exprArr) {
				result = result == null ? e 
						: Z3Solver.z3_mk_add_bin(configuration, result, e);
			}
			break;
		case     MULTIPLY : 
			for (Expression e : expr.getChildren()) {
				exprArr.add(transformToSMTExpr(e));
			}
			for (Pointer e : exprArr) {
				result = result == null ? e 
						: Z3Solver.z3_mk_mul_bin(configuration, result, e);
			}
			break;
		case     SUBTRACT :
			for (Expression e : expr.getChildren()) {
				exprArr.add(transformToSMTExpr(e));
			}
			for (Pointer e : exprArr) {
				result = result == null ? e 
						: Z3Solver.z3_mk_sub_bin(configuration, result, e);
			}
			break;
		case     AND :
			for (Expression e : expr.getChildren()) {
				exprArr.add(transformToSMTExpr(e));
			}
			for (Pointer e : exprArr) {
				result = result == null ? e 
						: Z3Solver.z3_mk_and_bin(configuration, result, e);
			}
			break;
		case     OR :
			for (Expression e : expr.getChildren()) {
				exprArr.add(transformToSMTExpr(e));
			}
			for (Pointer e : exprArr) {
				result = result == null ? e 
						: Z3Solver.z3_mk_or_bin(configuration, result, e);
			}
			break;
		case     NOT:
			op1 =  transformToSMTExpr(expr.getOperand1());
			result = Z3Solver.z3_mk_not(configuration, op1);
			break;
		case     IFF :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_iff(configuration, op1, op2);
			break;
		case     IMPLIES : 
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_implies(configuration, op1, op2);
			break;
		case     ITE :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			op3 = transformToSMTExpr(expr.getOperand3());
			result = Z3Solver.z3_mk_ite(configuration, op1, op2, op3);
			break;
		case     MOD :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_mod(configuration, op1, op2);
			break;
		case     POW :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_power(configuration, op1, op2);
			break;
		case     DIVIDE :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_div(configuration, op1, op2);
			break;
		case     GT :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_gt(configuration, op1, op2);
			break;
		case     GE :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_ge(configuration, op1, op2);
			break;
		case     LT :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_lt(configuration, op1, op2);
			break;
		case     LE :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_le(configuration, op1, op2);
			break;
		case     EQ :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_eq(configuration, op1, op2);
			break;
		case     NE :
			op1 = transformToSMTExpr(expr.getOperand1());
			op2 = transformToSMTExpr(expr.getOperand2());
			result = Z3Solver.z3_mk_eq(configuration, op1, op2);
			result = Z3Solver.z3_mk_not(configuration, result);
			break;
		case     CEIL :
		case     FLOOR :
		case     LOG :
		case     MAX :
		case     MIN :
			assert false;
		default:
				assert false;
		
		}
		return result;
	}
	
	public int addVariable(String name, Type type) {
		// TODO Auto-generated method stub
        assert type != null;
        assert type.isReal() || type.isInteger() || type.isBoolean();
        if(varMap.containsKey(name)) return varMap.get(name);
        Pointer var = null;
        if(type.isInteger()) {
        	var = Z3Solver.z3_mk_int_var(configuration, name);
        }else if(type.isBoolean()) {
        	var = Z3Solver.z3_mk_bool_var(configuration, name);
        }else {
        	var = Z3Solver.z3_mk_real_var(configuration, name);
        }	
        assert var != null;
        variables.add(var);
        varMap.put(name, numberOfVariables);
        assert var == variables.get(numberOfVariables);
        assert numberOfVariables == varMap.get(name);
        ++ numberOfVariables;
		return numberOfVariables;
	}

	public int addVariable(Expression variable, Type type) {
		// TODO Auto-generated method stub
        assert variable != null;
        return addVariable(variable.getName(), type);
	}
	
	private class Z3SMTSolver implements SMTSolver {

		private Pointer solver ;
		private int sizeOfPushStack = 0;
		private boolean hasStack = false;
		private boolean hasResult = false;
		private boolean closed = false;
		
		public Z3SMTSolver() {
			this.solver = Z3Solver.z3_mk_solver(configuration);
			Z3Solver.z3_solver_inc_ref(configuration, solver);
		}
		@Override
		public void push() {
			// TODO Auto-generated method stub
			Z3Solver.z3_solver_push(configuration, solver);
			sizeOfPushStack = 0;
			hasStack = true;
		}

		@Override
		public void pop() {
			// TODO Auto-generated method stub
			Z3Solver.z3_solver_pop(configuration, solver, sizeOfPushStack);
			hasStack = false;
		}

		@Override
		public Value getModelValue(Expression expression) {
			int idx = varMap.get(expression.getName());
			return getModelValue(idx);
		}

		@Override
		public Value getModelValue(int varIdx) {
			Pointer var = variables.get(varIdx);
			if(! hasResult) return null;
			Pointer model = Z3Solver.z3_solver_get_model(configuration, solver);
			String value = Z3Solver.z3_model_eval(configuration, model, var);
			String name = Z3Solver.z3_ast_to_string(configuration, var);
			if(value == null || value.equals(name)) return null;
			Value result = null;
			switch(Z3Solver.z3_get_sort(configuration, var)) {
			case 0:
				if(value.equals(notTrue)) {
					result = context.getContextValue().newValueBoolean(false);
				}else {
					result = context.getContextValue().newValueBoolean(true);
				}
				break;
			case 1:
				result = context.getContextValue().newValueInteger(Integer.parseInt(value));
				break;
			case 2:
				result = context.getContextValue().newReal(value);
				break;
			default:
				assert false : "unkown type";
			}
			return result;
		}

		@Override
		public int addVariable(String name, Type type) {
			// TODO Auto-generated method stub
	        assert type != null;
	        assert type.isReal() || type.isInteger() || type.isBoolean();
	        if(varMap.containsKey(name)) return varMap.get(name);
	        Pointer var = null;
	        if(type.isInteger()) {
	        	var = Z3Solver.z3_mk_int_var(configuration, name);
	        }else if(type.isBoolean()) {
	        	var = Z3Solver.z3_mk_bool_var(configuration, name);
	        }else {
	        	var = Z3Solver.z3_mk_real_var(configuration, name);
	        }	
	        assert var != null;
	        variables.add(var);
	        varMap.put(name, numberOfVariables);
	        assert var == variables.get(numberOfVariables);
	        assert numberOfVariables == varMap.get(name);
	        ++ numberOfVariables;
			return numberOfVariables;
		}

		@Override
		public int addVariable(Expression variable, Type type) {
			// TODO Auto-generated method stub
	        assert variable != null;
	        return addVariable(variable.getName(), type);
		}

		@Override
		public void addConstraint(Value[] row, int[] variables,
				ConstraintType constraintType, Value rightHandSide) {
		}

		@Override
		public void addConstraint(Value row, int[] variables,
				ConstraintType constraintType, Value rightHandSide) {
		}

		@Override
		public void addConstraint(Expression expression) {
			// TODO Auto-generated method stub
			assert expression != null;
			Pointer expr = transformToSMTExpr(expression);
			Z3Solver.z3_solver_assert(configuration, solver, expr);
			if(hasStack) ++ sizeOfPushStack;
		}

		@Override
		public void setAssumption(Expression expression) {
			assert expression != null;
			Pointer expr = transformToSMTExpr(expression);
			Z3Solver.z3_solver_assert(configuration, solver, expr);
			if(hasStack) ++ sizeOfPushStack;	
		}

		@Override
		public int setObjective(Expression objective, Direction direction) {
			return 0;
		}

		@Override
		public int setObjective(Value row, int[] variables, Direction direction) {
			return 0;
		}

		@Override
		public int setObjective(Value[] row, int[] variables, Direction direction) {
			return 0;
		}

		@Override
		public ConstraintSolverResult solve() {
			
			int rv = Z3Solver.z3_solver_check(configuration, solver);
			hasResult = true;
			if(rv == -1) return ConstraintSolverResult.UNKNOWN;
			if(rv == 0) return ConstraintSolverResult.UNSAT; 
			
			return ConstraintSolverResult.SAT;
		}

		@Override
		public ConstraintSolverResult solve(Expression... expressions) {
			Expression assumps = null;
			for(Expression expr : expressions) {
				assumps = assumps == null ? expr : assumps.and(expr);
			}
			Pointer assumption = transformToSMTExpr(assumps);
			int rv = Z3Solver.z3_solver_check_assumption(configuration, solver, assumption);
			hasResult = true;
			if(rv == -1) return ConstraintSolverResult.UNKNOWN;
			if(rv == 0) return ConstraintSolverResult.UNSAT; 
			return ConstraintSolverResult.SAT;
		}

		@Override
		public Value getObjectiveValue(int index) {
			return null;
		}

		@Override
		public void addExists(Expression[] vars, Expression body) {
			Pointer quantifiers = transformToSMTExpr(body);
			
			for(Expression var : vars) {
				quantifiers = Z3Solver.z3_mk_exists(configuration, transformToSMTExpr(var), quantifiers);
			}
			
			Z3Solver.z3_solver_assert(configuration, solver, quantifiers);
			if(hasStack) ++ sizeOfPushStack;
		}

		@Override
		public void close() {
			Z3Solver.z3_solver_dec_ref(configuration, solver);
		}
		@Override
		public ConstraintSolverResult solveByAssumption(
				Expression... expressions) {
			// TODO Auto-generated method stub
			Expression assumps = null;
			for(Expression expr : expressions) {
				assumps = assumps == null ? expr : assumps.and(expr);
			}
			Pointer assumption = transformToSMTExpr(assumps);
			Z3Solver.z3_solver_push(configuration, solver);
			Z3Solver.z3_solver_assert(configuration, solver, assumption);
			int rv = Z3Solver.z3_solver_check(configuration, solver);
			Z3Solver.z3_solver_pop(configuration, solver, 1);
			hasResult = true;
			if(rv == -1) return ConstraintSolverResult.UNKNOWN;
			if(rv == 0) return ConstraintSolverResult.UNSAT; 
			return ConstraintSolverResult.SAT;
		}
		
		public String toString() {
			return Z3Solver.z3_solver_to_string(configuration, solver);
		}
		
	}

}
