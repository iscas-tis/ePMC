package epmc.param.value.cancellator;

import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;

public interface Cancellator {
    interface Builder {
        Builder setType(TypePolynomial type);
        
        Cancellator build();
    }
    
	void cancel(ValuePolynomial operand1, ValuePolynomial operand2);
}
