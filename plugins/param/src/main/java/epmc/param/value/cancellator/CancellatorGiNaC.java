package epmc.param.value.cancellator;

import com.sun.jna.Pointer;

import epmc.param.value.NativeTools;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.util.JNATools;

public final class CancellatorGiNaC implements Cancellator {
    public final static class Builder implements Cancellator.Builder {
        private TypePolynomial type;

        @Override
        public Builder setType(TypePolynomial type) {
            this.type = type;
            return this;
        }

        @Override
        public Cancellator build() {
            return new CancellatorGiNaC(this);
        }
        
    }
    
    public final static String IDENTIFIER = "ginac";
    
    private final static class GiNaC {
        static native void epmc_ginac_cancel(Pointer poly1, Pointer poly2);
    }

    private final static boolean loaded =
            JNATools.registerLibrary(GiNaC.class, "ginac");

    private CancellatorGiNaC(Builder builder) {
        assert loaded;
        assert builder != null;
        assert builder.type != null;
    }
    
	@Override
	public void cancel(ValuePolynomial function1,
			ValuePolynomial function2) {
		assert function1 != null;
		assert function2 != null;
		Pointer struct1 = NativeTools.polynomialToNative(function1);
		assert struct1 != null;
		Pointer struct2 = NativeTools.polynomialToNative(function2);
		assert struct2 != null;
		
		GiNaC.epmc_ginac_cancel(struct1, struct2);
		NativeTools.nativeToPolynomialFixOrder(function1, struct1);
		NativeTools.nativeToPolynomialFixOrder(function2, struct2);
		NativeTools.polynomialDelete(struct1);
		NativeTools.polynomialDelete(struct2);
	}
}
