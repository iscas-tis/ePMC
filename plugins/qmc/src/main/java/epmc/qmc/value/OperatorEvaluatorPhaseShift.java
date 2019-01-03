package epmc.qmc.value;

import epmc.jani.extensions.trigonometricfunctions.OperatorCos;
import epmc.jani.extensions.trigonometricfunctions.OperatorSin;
import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorPhaseShift;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorPhaseShift implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private boolean built;
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            assert !built;
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            built = true;
            if (operator != OperatorPhaseShift.PHASE_SHIFT) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            return new OperatorEvaluatorPhaseShift(this);
        }
    }

    private final ValueReal phaseReal;
    private final OperatorEvaluator setReal;
    private final Value one;
    private final ValueReal cos;
    private final OperatorEvaluator cosEvaluator;
    private final TypeComplex typeComplex;
    private final Value sin;
    private final OperatorEvaluator sinEvaluator;
    private final ValueComplex complexAcc;
    private ValueAlgebra complexZero;
    
    private OperatorEvaluatorPhaseShift(Builder builder) {
        phaseReal = TypeReal.get().newValue();
        setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        one = UtilValue.newValue(TypeComplex.get(), 1);
        cos = TypeReal.get().newValue();
        cosEvaluator = ContextValue.get().getEvaluator(OperatorCos.COS, TypeReal.get());
        typeComplex = TypeComplex.get();
        sin = TypeReal.get().newValue();
        sinEvaluator = ContextValue.get().getEvaluator(OperatorSin.SIN, TypeReal.get());
        complexAcc = TypeComplex.get().newValue();
        complexZero = UtilValue.newValue(typeComplex, 0);
    }

    @Override
    public Type resultType() {
        return TypeMatrix.get(TypeComplex.get());
    }

    @Override
    public void apply(Value result, Value... operands) {
        Value phase = operands[0];
        assert phase != null;
        setReal.apply(phaseReal, phase);

        ValueMatrix resultMatrix = ValueMatrix.as(result);
        resultMatrix.setDimensions(2, 2);
        resultMatrix.set(one, 0, 0);

        cosEvaluator.apply(cos, phaseReal);
        sinEvaluator.apply(sin, phaseReal);
        setReal.apply(complexAcc.getRealPart(), cos);
        setReal.apply(complexAcc.getImagPart(), sin);
        resultMatrix.set(complexAcc, 1, 1);
        resultMatrix.set(complexZero, 1, 0);
        resultMatrix.set(complexZero, 0, 1);
    }
}
