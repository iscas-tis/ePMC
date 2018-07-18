package epmc.param.value.gmp;

import com.sun.jna.Memory;

public final class MPQSingleMemory extends Memory {
    public MPQSingleMemory() {
        super(GMP.MPQ_T_SIZE);
        GMP.__gmpq_init(this);
    }

    @Override
    protected void finalize() {
        GMP.__gmpq_clear(this);
        super.finalize();
    }
}
