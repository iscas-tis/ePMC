package epmc.param.value.cancellator;

import com.sun.jna.Pointer;

import epmc.param.value.NativeTools;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.util.JNATools;

public final class CancellatorCoCoALib implements Cancellator {
    public final static String IDENTIFIER = "cocoalib";
    
    public final static class Builder implements Cancellator.Builder {
        private TypePolynomial type;

        @Override
        public Builder setType(TypePolynomial type) {
            this.type = type;
            return this;
        }

        @Override
        public Cancellator build() {
            return new CancellatorCoCoALib(this);
        }
        
    }
    
    private final static class CoCoALib {
        static native void epmc_cocoalib_cancel(Pointer poly1, Pointer poly2);
    }
    
    private final static boolean LOADED = JNATools.registerLibrary(CoCoALib.class, "cocoalib");

    private CancellatorCoCoALib(Builder builder) {
        assert LOADED;
        assert builder != null;
        assert builder.type != null;
    }
    
	@Override
	public void cancel(ValuePolynomial function1,
			ValuePolynomial function2) {
		assert function1 != null;
		assert function2 != null;
		function1.adjustNumParameters();
		function2.adjustNumParameters();
		Pointer struct1 = NativeTools.polynomialToNative(function1);
		Pointer struct2 = NativeTools.polynomialToNative(function2);
		CoCoALib.epmc_cocoalib_cancel(struct1, struct2);
		NativeTools.nativeToPolynomialFixOrder(function1, struct1);
		NativeTools.nativeToPolynomialFixOrder(function2, struct2);
		NativeTools.polynomialDelete(struct1);
		NativeTools.polynomialDelete(struct2);
	}
}
