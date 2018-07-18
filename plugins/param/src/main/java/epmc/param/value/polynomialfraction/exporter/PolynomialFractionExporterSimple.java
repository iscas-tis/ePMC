package epmc.param.value.polynomialfraction.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import epmc.operator.OperatorIsOne;
import epmc.param.value.polynomial.PolynomialFractionExporter;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.ValueBoolean;

public final class PolynomialFractionExporterSimple implements PolynomialFractionExporter {
    public final static String IDENTIFIER = "simple";
    private final static String SPACE = " ";
    private final static String DIVIDE = "/";
    private final static String COMMA = ",";
    private final OperatorEvaluator isOnePoly;
    private final ValueBoolean cmp;
    
    public final static class Builder implements PolynomialFractionExporter.Builder {
        private final List<ValuePolynomialFraction> functions = new ArrayList<>();

        @Override
        public Builder addFunction(ValuePolynomialFraction function) {
            functions.add(function);
            return this;
        }

        @Override
        public PolynomialFractionExporter build() {
            return new PolynomialFractionExporterSimple(this);
        }
        
    }
    
    private final List<ValuePolynomialFraction> functions = new ArrayList<>();
    
    public PolynomialFractionExporterSimple(Builder builder) {
        isOnePoly = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE,
                builder.functions.get(0).getType().getTypePolynomial());
        cmp = TypeBoolean.get().newValue();
        functions.addAll(builder.functions);
    }

    @Override
    public void export(Appendable result) throws IOException {
        for (int functionNr = 0; functionNr < functions.size(); functionNr++) {
            ValuePolynomial numerator = functions.get(functionNr).getNumerator();
            ValuePolynomial denominator = functions.get(functionNr).getDenominator();
            isOnePoly.apply(cmp, denominator);
            if (cmp.getBoolean()) {
                result.append(numerator.toString());
            } else {
                result.append(numerator.toString())
                .append(SPACE)
                .append(DIVIDE)
                .append(SPACE)
                .append(denominator.toString());
            }
            if (functionNr < functions.size() - 1) {
                result.append(COMMA);
            }
        }
    }

}
