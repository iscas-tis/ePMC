package epmc.param.points;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.CharStreams;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorSet;
import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayInterval;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.value.ValueSetString;

public final class PointsRangeIntervals implements Points {
    public final static String IDENTIFIER = "range-intervals";
    private final static String COMMA = ",";
    private final static String COLON = ":";
    
    public final static class Builder implements Points.Builder {
        private Reader inputReader;
        private String inputString;
        private ParameterSet parameters = TypeFunction.get().getParameterSet();

        @Override
        public Builder setInput(Reader input) {
            this.inputReader = input;
            return this;
        }

        @Override
        public Builder setInput(String input) {
            this.inputString = input;
            return this;
        }
        
        @Override
        public Builder setParameters(ParameterSet parameters) {
            this.parameters = parameters;
            return this;
        }
        
        @Override
        public Points build() {
            assert inputReader != null || inputString != null;
            assert parameters != null;
            return new PointsRangeIntervals(this);
        }
    }

    private final int numPoints;
    private final int numParameters;
    private final int[] numValuesPerParameter;
    private final List<List<ValueAlgebra>> parameterValues;
    private final ParameterSet parameters;
    private final ValueInterval valueInterval;
    private final OperatorEvaluator setInterval;
    private int pointNr;
    
    private PointsRangeIntervals(Builder builder) {
        assert builder != null;
        assert builder.inputReader != null || builder.inputString != null;
        assert builder.parameters != null;
        String input = null;
        if (builder.inputString != null) {
            input = builder.inputString;
        } else if (builder.inputReader != null) {
            try {
                input = CharStreams.toString(builder.inputReader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        TypeRational evaluationType = TypeRational.get();
        parameterValues = generateParameterValues(evaluationType, input);
        int numPoints = 1;
        numParameters = parameterValues.size();
        numValuesPerParameter = new int[numParameters];
        int index = 0;
        for (List<ValueAlgebra> list : parameterValues) {
            numPoints *= list.size();
            numValuesPerParameter[index] = list.size();
            index++;
        }
        this.numPoints = numPoints;
        parameters = builder.parameters;
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(evaluationType));
        valueInterval = typeInterval.newValue();
        setInterval = ContextValue.get().getEvaluator(OperatorSet.SET, typeInterval, typeInterval);
    }

    private static List<List<ValueAlgebra>> generateParameterValues(TypeReal evaluationType, String pointsRangeString) {
        assert pointsRangeString != null;
        String[] rangesStringsList = pointsRangeString.split(COMMA);
        List<List<ValueAlgebra>> parameterValues = new ArrayList<>();
        for (String rangeString : rangesStringsList) {
            List<ValueAlgebra> paramV = computeParameterValues(evaluationType, rangeString);
            parameterValues.add(paramV);
        }
        return parameterValues;
    }

    private static List<ValueAlgebra> computeParameterValues(TypeReal evaluationType, String rangeString) {
        assert evaluationType != null;
        assert rangeString != null;
        String[] rangeDefStrings = rangeString.split(COLON);
        assert rangeDefStrings.length == 3;
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(evaluationType));
        ValueAlgebra start = evaluationType.newValue();
        ValueAlgebra end = evaluationType.newValue();
        ValueAlgebra step = evaluationType.newValue();
        ValueSetString.as(start).set(rangeDefStrings[0]);
        ValueSetString.as(end).set(rangeDefStrings[1]);
        ValueSetString.as(step).set(rangeDefStrings[2]);
        List<ValueAlgebra> result = new ArrayList<>();
        ValueAlgebra leftBorder = evaluationType.newValue();
        ValueAlgebra rightBorder = evaluationType.newValue();
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, evaluationType, evaluationType);
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, evaluationType, evaluationType);
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, evaluationType, evaluationType);
        ValueBoolean isGt = TypeBoolean.get().newValue();
        gt.apply(isGt, rightBorder, end);
        isGt.set(false);
        set.apply(leftBorder, start);
        set.apply(rightBorder, start);
        add.apply(rightBorder, rightBorder, step);
        while (!isGt.getBoolean()) {
            ValueInterval interval = typeInterval.newValue();
            set.apply(interval.getIntervalLower(), leftBorder);
            set.apply(interval.getIntervalUpper(), rightBorder);
            result.add(interval);
            add.apply(leftBorder, leftBorder, step);
            add.apply(rightBorder, rightBorder, step);
            gt.apply(isGt, rightBorder, end);
        }
        return result;
    }

    @Override
    public boolean hasNext() {
        return pointNr < numPoints;
    }

    @Override
    public ParameterSet getParameters() {
        return parameters;
    }

    @Override
    public void next(ValueArrayInterval point) {
        assert point != null;
        int pointV = pointNr;
        for (int paramNr = numParameters - 1; paramNr >= 0; paramNr--) {
            int paramValueNr = pointV % numValuesPerParameter[paramNr];
            pointV /= numValuesPerParameter[paramNr];
            setInterval.apply(valueInterval, parameterValues.get(paramNr).get(paramValueNr));
            point.set(valueInterval, paramNr);
        }
        pointNr++;
    }
    
    @Override
    public boolean isIntervals() {
        return true;
    }
}
