package epmc.param.operatorevaluator.polynomialfraction;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddSubtractPolynomialFraction implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert types != null;
            assert operator != null;
            if (operator != OperatorAdd.ADD
                    && operator != OperatorSubtract.SUBTRACT) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypePolynomialFraction.is(types[0])
                    && !TypePolynomialFraction.is(types[1])) {
                return null;
            }
            TypePolynomialFraction typeFraction = null;
            if (TypePolynomialFraction.is(types[0])) {
                typeFraction = TypePolynomialFraction.as(types[0]);
            } else if (TypePolynomialFraction.is(types[1])) {
                typeFraction = TypePolynomialFraction.as(types[1]);
            } else {
                assert false;
            }
            for (Type type : types) {
                if (!TypePolynomialFraction.is(type)
                        && ContextValue.get().getEvaluatorOrNull(OperatorSet.SET, type, typeFraction) == null) {
                    return null;
                }
            }
            return new EvaluatorAddSubtractPolynomialFraction(this);
        }
    }

    private static final int NUM_IMPORT_VALUES = 2;
    private final ValuePolynomialFraction importPolynomials[] = new ValuePolynomialFraction[NUM_IMPORT_VALUES];
    private final OperatorEvaluator importSet[] = new OperatorEvaluator[NUM_IMPORT_VALUES];
    private final TypePolynomialFraction typePolynomialFraction;
    private final TypePolynomial typePolynomial;
    private final OperatorEvaluator multiplyPolynomials;
    private final ValuePolynomial newNumerator;
    private final ValuePolynomial newDenominator;
    private final ValuePolynomial newNumeratorLeft;
    private final ValuePolynomial newNumeratorRight;
    private final OperatorEvaluator addOrSubtractPolynomials;
    private final OperatorEvaluator setPolynomial;
    private final ValuePolynomial v1;
    private final ValuePolynomial v2;

    private EvaluatorAddSubtractPolynomialFraction(Builder builder) {
        assert builder != null;
        TypePolynomialFraction typeFraction = null;
        if (TypePolynomialFraction.is(builder.types[0])) {
            typeFraction = TypePolynomialFraction.as(builder.types[0]);
        } else if (TypePolynomialFraction.is(builder.types[1])) {
            typeFraction = TypePolynomialFraction.as(builder.types[1]);
        } else {
            assert false;
        }
        typePolynomialFraction = typeFraction;
        typePolynomial = typePolynomialFraction.getTypePolynomial();
        newNumerator = typePolynomial.newValue();
        newDenominator = typePolynomial.newValue();
        newNumeratorLeft = typePolynomial.newValue();
        newNumeratorRight = typePolynomial.newValue();
        multiplyPolynomials = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, typePolynomial, typePolynomial);
        addOrSubtractPolynomials = ContextValue.get().getEvaluator(builder.operator,
                typePolynomial, typePolynomial);
        setPolynomial = ContextValue.get().getEvaluator(OperatorSet.SET,
                typePolynomial, typePolynomial);
        if (!TypePolynomialFraction.is(builder.types[0])) {
            importSet[0] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], typeFraction);
        }
        if (!TypePolynomialFraction.is(builder.types[1])) {
            importSet[1] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[1], typeFraction);
        }
        importPolynomials[0] = typePolynomialFraction.newValue();
        importPolynomials[1] = typePolynomialFraction.newValue();
        v1 = typePolynomial.newValue();        
        v2 = typePolynomial.newValue();        
    }

    @Override
    public Type resultType() {
        return typePolynomialFraction;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomialFraction resultFraction = ValuePolynomialFraction.as(result);
        ValuePolynomialFraction poly1 = castOrImport(operands[0], 0);
        ValuePolynomialFraction poly2 = castOrImport(operands[1], 1);

        if (poly1.getDenominator().equals(poly2.getDenominator())) {
            addOrSubtractPolynomials.apply(newNumerator, poly1.getNumerator(), poly2.getNumerator());
            setPolynomial.apply(resultFraction.getNumerator(), newNumerator);
            setPolynomial.apply(resultFraction.getDenominator(), poly1.getDenominator());
            resultFraction.normalise();
        } else {
            multiplyPolynomials.apply(newNumeratorLeft, poly1.getNumerator(), poly2.getDenominator());
            multiplyPolynomials.apply(newNumeratorRight, poly2.getNumerator(), poly1.getDenominator());
            addOrSubtractPolynomials.apply(newNumerator, newNumeratorLeft, newNumeratorRight);
//            multiplyPolynomials.apply(newDenominator, poly1.getDenominator(), poly2.getDenominator());
            setPolynomial.apply(v1, poly1.getDenominator());
            setPolynomial.apply(v2, poly2.getDenominator());
            typePolynomial.cancelCommonFactors(newNumerator, v1);
            typePolynomial.cancelCommonFactors(newNumerator, v2);
            multiplyPolynomials.apply(newDenominator, v1, v2);
        
            setPolynomial.apply(resultFraction.getNumerator(), newNumerator);
            setPolynomial.apply(resultFraction.getDenominator(), newDenominator);
        }
        assert !resultFraction.getDenominator().toString().equals("0");
    }
    
    private ValuePolynomialFraction castOrImport(Value operand, int number) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (importSet[number] == null) {
            ValuePolynomialFraction result = ValuePolynomialFraction.as(operand);
            result.adjustNumParameters();
            return result;
        } else {
            importSet[number].apply(importPolynomials[number], operand);
            return importPolynomials[number];
        }
    }
}
