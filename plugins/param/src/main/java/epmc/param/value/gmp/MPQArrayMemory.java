package epmc.param.value.gmp;

import com.sun.jna.Memory;

public final class MPQArrayMemory extends Memory {
    private final int size;
    
    public MPQArrayMemory(int size) {
        super(Math.max(GMP.MPQ_T_SIZE * size, 1));
        GMP.gmp_util_init_array(this, size);
        this.size = size;
    }

    @Override
    protected void finalize() {
        GMP.gmp_util_clear_array(this, size);
        super.finalize();
    }
}
