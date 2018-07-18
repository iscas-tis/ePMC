package epmc.param.points;

import java.io.IOException;

import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.UtilValue;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInterval;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

public final class PointResultsExporterPgfplots implements PointResultsExporter {
    public final static String IDENTIFIER = "pgfplots";

    public final static class Builder implements PointResultsExporter.Builder {
        private PointResults results;

        @Override
        public Builder setPointResults(PointResults results) {
            this.results = results;
            return this;
        }

        @Override
        public PointResultsExporter build() {
            Type resultType = results.getResultType();
            if (TypeInterval.is(resultType)) {
                return null;
            }
            if (results.pointsIsIntervals()) {
                return null;
            }
            return new PointResultsExporterPgfplots(this);
        }
    }

    private final static String COMMA = ",";
    private final static String NEWLINE = "\n";
    private final PointResults results;

    private PointResultsExporterPgfplots(Builder builder) {
        assert builder != null;
        results = builder.results;
    }

    @Override
    public void export(Appendable result) {
        try {
            exportIoExeption(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void exportIoExeption(Appendable result) throws IOException {
        assert result != null;
        appendHeader(result);
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(TypeRational.get()));
        ValueInterval pointValue = typeInterval.newValue();
        ValueReal resultValue = ValueReal.as(results.getResultType().newValue());
        ValueArrayInterval pointArray = UtilValue.newArray(typeInterval.getTypeArray(),
                results.getParameters().getNumParameters());
        ValueArrayAlgebra resultArray = UtilValue.newArray(results.getResultType().getTypeArray(),
                results.getResultDimensions());
        double last = Double.NaN;
        while (results.hasNext()) {
            results.next(pointArray, resultArray);
            if (results.getParameters().getNumParameters() == 2) {
                pointArray.get(pointValue, 0);
                double pointDouble = toDouble(pointValue);
                if (!Double.isNaN(last) && last != pointDouble) {
                    result.append(NEWLINE);
                }
                last = pointDouble;
            }
            result.append("(");
            for (int dim = 0; dim < results.getParameters().getNumParameters(); dim++) {
                pointArray.get(pointValue, dim);
                result.append(doubleString(pointValue));
                if (dim < results.getParameters().getNumParameters() - 1) {
                    result.append(COMMA);
                }
            }
            result.append(COMMA);
            for (int dim = 0; dim < results.getResultDimensions(); dim++) {
                resultArray.get(resultValue, dim);
                result.append(doubleString(resultValue));
                if (dim < results.getResultDimensions() - 1) {
                    result.append(COMMA);
                }
            }
            result.append(")");
            result.append(NEWLINE);
        }
        addFooter(result);
    }
    
    private void appendHeader(Appendable result) throws IOException {
        result.append("\\documentclass{standalone}\n"
                + "\\usepackage{luatex85}\n"
                + "\\def\\pgfsysdriver{pgfsys-pdftex.def}\n"
                + "\\usepackage{pgfplots}\n"
                + "\\pgfplotsset{compat=1.14}\n"
                + "\\begin{document}\n"
                + "\\begin{tikzpicture}\n"
                + "\\begin{axis}");
        int numParameters = results.getParameters().getNumParameters();
        if (numParameters == 1) {
            result.append("\\addplot coordinates {\n");
        } else if (numParameters == 2) {
            result.append("\\addplot3[surf,z buffer=sort] coordinates {\n");            
        } else {
            assert false;
        }
        /*
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            result.append(results.getParameters().getParameter(paramNr).toString());
            result.append(SPACE);
        }
        if (results.getResultDimensions() == 1) {
            result.append(RESULT);
        } else {
            for (int resultNr = 0; resultNr < results.getResultDimensions(); resultNr++) {
                result.append(RESULT);
                result.append(Integer.toString(resultNr + 1));
                if (resultNr < results.getResultDimensions() - 1) {
                    result.append(SPACE);
                }
            }
        }
        result.append(NEWLINE);
        */
    }

    private void addFooter(Appendable result) throws IOException {
        result.append("};\n"
                + "\\end{axis}\n"
                + "\\end{tikzpicture}\n"
                + "\\end{document}\n");
    }


    private double toDouble(ValueInterval interval) {
        return ValueReal.as(interval.getIntervalLower()).getDouble();
    }
    
    private String doubleString(ValueInterval interval) {
//        System.out.println("D " + ValueReal.as(interval.getIntervalLower()).getDouble());
        return Double.toString(ValueReal.as(interval.getIntervalLower()).getDouble());
    }
    
    private String doubleString(ValueReal value) {
        return Double.toString(value.getDouble());
    }
}
