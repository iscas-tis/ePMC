package epmc.mpfr;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import epmc.util.JNATools;

public final class MPFR {
	/** Maximal size MPFR structure can have. */
	public final static int STRUCT_SIZE = 32;
	
	public final static int MPFR_RNDN = 0;
	public final static int MPFR_RNDZ = 1;
	public final static int MPFR_RNDU = 2;
	public final static int MPFR_RNDD = 3;
	public final static int MPFR_RNDA = 4;
	public final static int MPFR_RNDF = 5;
	public final static int MPFR_RNDNA = -1;
	
    private final static boolean LOADED =
            JNATools.registerLibrary(MPFR.class, "mpfr");

    public static boolean isLoaded() {
    	return LOADED;
    }
    
    public native static void mpfr_init2(Pointer x, NativeLong prec);
    
    public native static void mpfr_clear(Pointer x);
    
    
    public native static int mpfr_set_str(Pointer rop, String s, int base, int rnd);
    
    public native static int mpfr_asprintf(Pointer str, String template, Pointer args);

    public native static void mpfr_free_str(Pointer str);

    
    public native static NativeLong mpfr_get_si(Pointer op, int rnd);
    
    public native static double mpfr_get_d(Pointer op, int rnd);
    
    public native static int mpfr_sgn(Pointer op);
    
    
    public native static int mpfr_add(Pointer rop, Pointer op1, Pointer op2, int rnd);

    public native static int mpfr_sub(Pointer rop, Pointer op1, Pointer op2, int rnd);
    
    public native static int mpfr_mul (Pointer rop, Pointer op1, Pointer op2, int rnd);

    public native static int mpfr_div(Pointer rop, Pointer op1, Pointer op2, int rnd);
    
    public native static int mpfr_log(Pointer rop, Pointer op, int rnd);
    
    public native static int mpfr_exp(Pointer rop, Pointer op, int rnd);
    
    public native static int mpfr_ceil(Pointer rop, Pointer op);

    public native static int mpfr_floor(Pointer rop, Pointer op);

    public native static int mpfr_trunc(Pointer rop, Pointer op);
    
    public native static int mpfr_fmod(Pointer r, Pointer x, Pointer y, int rnd);
    
    public native static int mpfr_pow(Pointer rop, Pointer op1, Pointer op2, int rnd);

    public native static int mpfr_abs(Pointer rop, Pointer op, int rnd);
    
    public native static int mpfr_cos(Pointer rop, Pointer op, int rnd);

    public native static int mpfr_sin(Pointer rop, Pointer op, int rnd);
    
    public native static int mpfr_tanh(Pointer rop, Pointer op, int rnd);

    public native static int mpfr_cosh(Pointer rop, Pointer op, int rnd);

    public native static int mpfr_sinh(Pointer rop, Pointer op, int rnd);

    public native static int mpfr_atan(Pointer rop, Pointer op, int rnd);

    public native static int mpfr_acos(Pointer rop, Pointer op, int rnd);

    public native static int mpfr_asin(Pointer rop, Pointer op, int rnd);

    public native static int mpfr_tan(Pointer rop, Pointer op, int rnd);
    
    public native static int mpfr_sqrt(Pointer rop, Pointer op, int rnd);
    
    public native static int mpfr_min(Pointer rop, Pointer op1, Pointer op2, int rnd);

    public native static int mpfr_max(Pointer rop, Pointer op1, Pointer op2, int rnd);
    
    
    public native static int mpfr_set(Pointer rop, Pointer op, int rnd);
    
    public native static int mpfr_set_d(Pointer rop, double op, int rnd);
    
    public native static int mpfr_set_si(Pointer rop, NativeLong op, int rnd);

    
    public native static void mpfr_nextbelow(Pointer x);
    
    public native static void mpfr_nextabove(Pointer x);
    
    
    public native static NativeLong mpfr_get_default_prec();
    
    public native static int mpfr_nan_p(Pointer op);
    
    public native static int mpfr_zero_p(Pointer op);
    
    public native static int mpfr_inf_p(Pointer op);
    

    public native static int mpfr_cmp(Pointer op1, Pointer op2);
    
    public native static int mpfr_cmp_si(Pointer op1, NativeLong op2);

    
    public native static void mpfr_set_inf(Pointer x, int sign);
    
    public native static void mpfr_set_zero(Pointer x, int sign);
    
    public native static int mpfr_neg(Pointer rop, Pointer op, int rnd);    
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MPFR() {
    }
}
