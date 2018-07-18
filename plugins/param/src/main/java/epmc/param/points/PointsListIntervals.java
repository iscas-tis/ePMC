package epmc.param.points;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import epmc.operator.OperatorSet;
import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeInterval;
import epmc.value.ValueArrayInterval;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;
import epmc.value.ValueSetString;

public final class PointsListIntervals implements Points {
    public final static String IDENTIFIER = "list-intervals";
    private final static char SPACE = ' ';
    private final static char COMMA = ' ';

    public final static class Builder implements Points.Builder {
        private Reader reader;
        private ParameterSet parameters = TypeFunction.get().getParameterSet();

        @Override
        public Builder setInput(Reader input) {
            this.reader = input;
            return this;
        }

        @Override
        public Builder setParameters(ParameterSet parameters) {
            this.parameters = parameters;
            return this;
        }

        @Override
        public Points build() {
            return new PointsListIntervals(this);
        }
    }

    private final TypeInterval typeInterval;
    private final BufferedReader input;
    private final StringBuilder numberRead = new StringBuilder();
    private final ValueReal value;
    private final ValueInterval valueInterval;
    private final ParameterSet parameters;
    private final OperatorEvaluator setReal;
    private boolean hasNext;

    private PointsListIntervals(Builder builder) {
        assert builder != null;
        TypeRational typeRational = TypeRational.get();
        typeInterval = ContextValue.get().makeUnique(new TypeInterval(typeRational));
        Reader input = builder.reader;
        if (input instanceof BufferedReader) {
            this.input = (BufferedReader) input;
        } else {
            this.input = new BufferedReader(builder.reader);
        }
        this.parameters = builder.parameters;
        value = typeRational.newValue();
        valueInterval = typeInterval.newValue();
        setReal = ContextValue.get().getEvaluator(OperatorSet.SET, typeRational, typeRational);
        readNextValue();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ParameterSet getParameters() {
        return parameters;
    }
    
    @Override
    public void next(ValueArrayInterval point) {
        assert point != null;
        assert hasNext;
        for (int param = 0; param < parameters.getNumParameters(); param++) {
            setReal.apply(valueInterval.getIntervalLower(), value);
            readNextValue();
            setReal.apply(valueInterval.getIntervalUpper(), value);
            point.set(valueInterval, param);
            readNextValue();
        }
    }

    private void readNextValue() {
        try {
            boolean nonSpaceCommaSeen = false;
            for (int c = input.read(); c != -1; c = input.read()) {
                char cc = (char) c;
                if ((cc == SPACE || cc == COMMA) && !nonSpaceCommaSeen) {
                    continue;
                } else if ((cc == SPACE || cc == COMMA) && nonSpaceCommaSeen) {
                    break;
                }
                nonSpaceCommaSeen = true;
                numberRead.append(cc);
            }
            if (numberRead.length() > 0) {
                ValueSetString.as(value).set(numberRead.toString());
                numberRead.setLength(0);
                hasNext = true;
            } else {
                hasNext = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
