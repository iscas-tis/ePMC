package epmc.param.value.gmp;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import epmc.util.JNATools;

public final class GMP {
    public final static boolean LOADED = JNATools.registerLibrary(GMP.class, "gmpparam");
    public final static int MPQ_T_SIZE = GMP.gmp_util_size_of_mpq();
    public final static int MPFR_T_SIZE = GMP.gmp_util_size_of_mpfr();
    
    public static native int gmp_util_size_of_mpq();
    public static native int gmp_util_size_of_mpfr();
    public static native int gmp_util_mpq_get_int(Pointer x);
    public static native void gmp_util_mpq_set_int(Pointer x, int value);
    public static native Pointer gmp_util_mpq_get_num(Pointer value);
    public static native Pointer gmp_util_mpq_get_den(Pointer value);
    public static native void gmp_util_free_string(Pointer str);
    public static native int gmp_util_mpq_is_zero(Pointer value);
    public static native int gmp_util_mpq_is_one(Pointer value);
    public static native int gmp_util_mpq_equals(Pointer op1, Pointer op2);
    public static native void gmp_util_mpq_array_set_int(Pointer rop, int index, int to);
    public static native int gmp_util_mpq_array_equals(Pointer op1, Pointer op2, int length);
    public static native void gmp_util_mpq_array_set(Pointer rop, int index, Pointer to);
    public static native void gmp_util_mpq_array_get(Pointer rop, int index, Pointer to);
    
    public static native void gmp_util_mpq_to_double_interval(Pointer result, Pointer op, Pointer buffer);
    public static native void gmp_util_mpq_interval_to_double_interval(Pointer result, Pointer op_left, Pointer op_right, Pointer buffer);
    public static native void gmp_util_mpq_pow(Pointer result, Pointer op1, int op2);
    public static native void gmp_util_mpq_max(Pointer result, Pointer op1, Pointer op2);

    public static native void gmp_util_init_array(Pointer rop, int size);
    public static native void gmp_util_clear_array(Pointer rop, int size);
    public static native double gmp_util_mpq_get_double(Pointer op, Pointer buffer);

    public static native void __gmpq_init(Pointer x);
    public static native void __gmpq_clear(Pointer x);
    public static native void __gmpq_canonicalize(Pointer x);
    public static native void __gmpq_set(Pointer rop, Pointer op);
    public static native int __gmpq_set_str(Pointer rop, String str, int base);
    public static native void __gmpq_set_si(Pointer rop, NativeLong op1, NativeLong op2);
    public static native void __gmpq_swap(Pointer rop1, Pointer rop2);
//    public static native double __gmpq_get_d(Pointer op);
    public static native void __gmpq_set_d(Pointer rop, double op);
    public static native Pointer __gmpq_get_str(String str, int base, Pointer op);
    
    public static native void __gmpq_add(Pointer sum, Pointer addend1, Pointer addend2);
    public static native void __gmpq_sub(Pointer difference, Pointer minuend, Pointer subtrahend);
    public static native void __gmpq_mul(Pointer product, Pointer multiplier, Pointer multiplicand);
    public static native void __gmpq_mul_2exp(Pointer rop, Pointer op1, NativeLong op2);
    public static native void __gmpq_div(Pointer quotient, Pointer dividend, Pointer divisor);
    public static native void __gmpq_div_2exp(Pointer rop, Pointer op1, NativeLong op2);
    public static native void __gmpq_neg(Pointer negated_operand, Pointer operand);
    public static native void __gmpq_abs(Pointer rop, Pointer op);
    public static native void __gmpq_inv(Pointer inverted_number, Pointer number);

    public static native int __gmpq_cmp(Pointer op1, Pointer op2);

    public static native void evaluate_gmp(Pointer variables, int[] program, int num_statements, Pointer numbers, Pointer point);

//    public static native void mpfr_init(Pointer x);
    public static native void mpfr_clear(Pointer x);
    public static native void gmp_util_init_mpfr(Pointer x, int prec);

    private GMP() {
    }
}
