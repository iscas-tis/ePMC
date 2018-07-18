package epmc.param.value.gmp;

import com.sun.jna.Memory;

public final class MPFRSingleMemory extends Memory {
    private final static int DOUBLE_MANTISSA_BITS = 53;
    
    public MPFRSingleMemory() {
        super(GMP.MPFR_T_SIZE);
        GMP.gmp_util_init_mpfr(this, DOUBLE_MANTISSA_BITS);
    }

    @Override
    protected void finalize() {
        GMP.mpfr_clear(this);
        super.finalize();
    }
}
