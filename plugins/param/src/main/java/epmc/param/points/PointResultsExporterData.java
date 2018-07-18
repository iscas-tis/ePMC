package epmc.param.points;

import java.io.IOException;

import epmc.param.options.OptionsParam;
import epmc.param.value.rational.TypeRational;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInterval;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

public final class PointResultsExporterData implements PointResultsExporter {
    public final static String IDENTIFIER = "data";

    public final static class Builder implements PointResultsExporter.Builder {
        private PointResults results;

        @Override
        public Builder setPointResults(PointResults results) {
            this.results = results;
            return this;
        }

        @Override
        public PointResultsExporter build() {
            return new PointResultsExporterData(this);
        }
    }

    private final static String SPACE = " ";
    private final static String NEWLINE = "\n";
    private final static String COMMENT_BEGIN = "# ";
    private final static String RESULT = "result";
    private final PointResults results;
    private final ValueFormat pointFormat;
    private final IntervalFormat pointIntervalFormat;
    private final ValueFormat resultFormat;
    private final IntervalFormat resultIntervalFormat;
    private final boolean forceIntervalPoints;
    private final boolean forceIntervalResults;
    private final ValueInterval resultInterval;
    private final OperatorEvaluator setResultInterval;

    private PointResultsExporterData(Builder builder) {
        assert builder != null;
        results = builder.results;
        pointFormat = UtilOptions.getInstance(OptionsParam.PARAM_POINTS_EXPORTER_POINT_FORMAT);
        pointIntervalFormat = UtilOptions.getInstance(OptionsParam.PARAM_POINTS_EXPORTER_POINT_INTERVAL_FORMAT);
        resultFormat = UtilOptions.getInstance(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_FORMAT);
        resultIntervalFormat = UtilOptions.getInstance(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_INTERVAL_FORMAT);
        forceIntervalPoints = Options.get().getBoolean(OptionsParam.PARAM_POINTS_EXPORTER_POINT_FORCE_INTERVAL);
        forceIntervalResults = Options.get().getBoolean(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_FORCE_INTERVAL);
        Type resultType = results.getResultType();
        if (TypeInterval.is(resultType) || !forceIntervalResults) {
            resultInterval = null;
            setResultInterval = null;
        } else {
            TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(TypeReal.as(resultType)));
            resultInterval = typeInterval.newValue();
            setResultInterval = ContextValue.get().getEvaluator(OperatorSet.SET, resultType, typeInterval);
        }
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
        Value resultValue = results.getResultType().newValue();
        ValueArrayInterval pointArray = UtilValue.newArray(typeInterval.getTypeArray(),
                results.getParameters().getNumParameters());
        ValueArrayAlgebra resultArray = UtilValue.newArray(results.getResultType().getTypeArray(),
                results.getResultDimensions());
        while (results.hasNext()) {
            results.next(pointArray, resultArray);
            for (int dim = 0; dim < results.getParameters().getNumParameters(); dim++) {
                pointArray.get(pointValue, dim);
                result.append(formatPointValue(pointValue));
                if (dim < results.getParameters().getNumParameters() - 1) {
                    result.append(SPACE);
                }
            }
            result.append(SPACE);
            for (int dim = 0; dim < results.getResultDimensions(); dim++) {
                resultArray.get(resultValue, dim);
                result.append(formatResultValue(resultValue));
                if (dim < results.getResultDimensions() - 1) {
                    result.append(SPACE);
                }
            }
            result.append(NEWLINE);
        }
    }
    
    private void appendHeader(Appendable result) throws IOException {
        result.append(COMMENT_BEGIN);
        int numParameters = results.getParameters().getNumParameters();
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
    }

    private String formatPointValue(ValueAlgebra point) {
        assert point != null;
        if (results.pointsIsIntervals() || forceIntervalPoints) {
            return pointIntervalFormat.format(ValueInterval.as(point), pointFormat);
        } else {
            return pointFormat.format(ValueReal.as(ValueInterval.as(point).getIntervalLower()), Side.MIDDLE);
        }
    }
    
    private String formatResultValue(Value result) {
        assert result != null;
        if (ValueInterval.is(result)) {
            return resultIntervalFormat.format(ValueInterval.as(result), resultFormat);
        } else if (forceIntervalResults) {
            setResultInterval.apply(resultInterval, result);
            return resultIntervalFormat.format(resultInterval, resultFormat);
        } else {
            return resultFormat.format(ValueReal.as(result), Side.MIDDLE);
        }
    }
}
