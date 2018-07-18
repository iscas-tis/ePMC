package epmc.param.value.polynomialfraction.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import epmc.param.points.PointResultsExporter;
import epmc.param.points.UtilPoints;
import epmc.param.value.ValueFunction;
import epmc.param.value.polynomial.PolynomialFractionExporter;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;

public final class PolynomialFractionExporterPoints implements PolynomialFractionExporter {
    public final static String IDENTIFIER = "points";
    
    public final static class Builder implements PolynomialFractionExporter.Builder {
        List<ValueFunction> functions = new ArrayList<>();
        
        @Override
        public Builder addFunction(ValuePolynomialFraction function) {
            assert function != null;
            functions.add(function);
            return this;
        }
        
        @Override
        public PolynomialFractionExporter build() {
            return new PolynomialFractionExporterPoints(this);
        }
    }

    private final PointResultsExporter exporter;

    private PolynomialFractionExporterPoints(Builder builder) {
        assert builder != null;
        exporter = UtilPoints.getExporter(builder.functions);
        assert exporter != null;
    }

    @Override
    public void export(Appendable result) throws IOException {
        exporter.export(result);
    }
}
