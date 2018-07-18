package epmc.param.points;

import epmc.param.value.FunctionEvaluator;
import epmc.param.value.ParameterSet;
import epmc.value.TypeAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInterval;

public final class PointResultsFunctionEvaluator implements PointResults {
    public final static class Builder {
        private Points points;
        private FunctionEvaluator functionEvaluator;

        public Builder setPoints(Points points) {
            assert points != null;
            this.points = points;
            return this;
        }
        
        public Builder setFunctionEvaluator(FunctionEvaluator functionEvaluator) {
            assert functionEvaluator != null;
            this.functionEvaluator = functionEvaluator;
            return this;
        }
        
        public PointResultsFunctionEvaluator build() {
            assert points != null;
            assert functionEvaluator != null;
            return new PointResultsFunctionEvaluator(this);
        }
    }

    private final Points points;
    private FunctionEvaluator functionEvaluator;
    
    private PointResultsFunctionEvaluator(Builder builder) {
        assert builder != null;
        this.points = builder.points;
        this.functionEvaluator = builder.functionEvaluator;
    }

    @Override
    public TypeAlgebra getResultType() {
        return functionEvaluator.getResultType();
    }

    @Override
    public ParameterSet getParameters() {
        return points.getParameters();
    }
    
    @Override
    public int getResultDimensions() {
        return functionEvaluator.getResultDimensions();
    }

    @Override
    public boolean hasNext() {
        return points.hasNext();
    }

    @Override
    public void next(ValueArrayInterval point, ValueArrayAlgebra result) {
        assert point != null;
        assert result != null;
        points.next(point);
        functionEvaluator.evaluate(result, point);
    }

    @Override
    public boolean pointsIsIntervals() {
        return points.isIntervals();
    }
}
