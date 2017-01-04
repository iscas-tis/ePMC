package epmc.mpfr;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;

public class MPFRMemory extends Memory {
	public MPFRMemory(int precision) {
		super(MPFR.STRUCT_SIZE);
		MPFR.mpfr_init2(this, new NativeLong(precision));
	}
	
	@Override
	protected void finalize() {
		MPFR.mpfr_clear(this);
		super.finalize();
	}
}
