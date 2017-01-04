package epmc.param.value.cancellator;

import com.sun.jna.Pointer;

import epmc.param.value.NativeTools;
import epmc.param.value.TypeFunctionPolynomial;
import epmc.param.value.ValueFunctionPolynomial;
import epmc.util.JNATools;

public final class CancellatorCoCoALib implements Cancellator {
    private final static class CoCoALib {
    	static native void epmc_cocoalib_cancel(Pointer poly1, Pointer poly2);
    }
    
    private final static boolean loaded = JNATools.registerLibrary(CoCoALib.class, "cocoalib");

    public CancellatorCoCoALib(TypeFunctionPolynomial type) {
    	assert loaded;
    	assert type != null;
    }
    
	@Override
	public void cancel(ValueFunctionPolynomial function1,
			ValueFunctionPolynomial function2) {
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
