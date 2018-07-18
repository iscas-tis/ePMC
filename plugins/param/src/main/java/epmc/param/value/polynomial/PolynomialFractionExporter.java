package epmc.param.value.polynomial;

import java.io.IOException;

import epmc.param.value.polynomialfraction.ValuePolynomialFraction;

public interface PolynomialFractionExporter {
    interface Builder {
        Builder addFunction(ValuePolynomialFraction function);
        
        PolynomialFractionExporter build();
    }
    
    void export(Appendable result) throws IOException;
}
