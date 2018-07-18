package epmc.param.value.polynomialfraction.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import epmc.operator.OperatorDivide;
import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.param.value.dag.exporter.DagExporter;
import epmc.param.value.polynomial.PolynomialFractionExporter;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;

public final class PolynomialFractionExporterDag implements PolynomialFractionExporter {
    public final static String IDENTIFIER = "dag";
    
    public final static class Builder implements PolynomialFractionExporter.Builder {
        private final List<ValuePolynomialFraction> functions = new ArrayList<>();

        @Override
        public Builder addFunction(ValuePolynomialFraction function) {
            assert function != null;
            functions.add(function);
            return this;
        }

        @Override
        public PolynomialFractionExporter build() {
            return new PolynomialFractionExporterDag(this);
        }
        
    }
    
    private List<ValuePolynomialFraction> functions = new ArrayList<>();
    
    private PolynomialFractionExporterDag(Builder builder) {
        assert builder != null;
        functions.addAll(builder.functions);
    }

    @Override
    public void export(Appendable result) throws IOException {
        assert result != null;
        TypePolynomialFraction typeFraction = functions.get(0).getType();
        TypeDag typeDag = ContextValue.get().makeUnique(new TypeDag(typeFraction.getParameterSet()));        
        OperatorEvaluator divideDag = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, typeDag, typeDag);
        DagExporter.Builder dagExporterBuilder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_EXPORTER);
        dagExporterBuilder.setDag(typeDag.getDag());
        PolynomialToDag polyToDag = new PolynomialToDag(typeFraction.getTypePolynomial());
        ValueDag valueDag = typeDag.newValue();
        for (ValuePolynomialFraction function : functions) {
            ValueDag numeratorDag = polyToDag.convert(function.getNumerator());
            ValueDag denominatorDag = polyToDag.convert(function.getDenominator());
            divideDag.apply(valueDag, numeratorDag, denominatorDag);
            dagExporterBuilder.addRelevantNode(valueDag.getNumber());
        }
        dagExporterBuilder.build().export(result);
    }
}
