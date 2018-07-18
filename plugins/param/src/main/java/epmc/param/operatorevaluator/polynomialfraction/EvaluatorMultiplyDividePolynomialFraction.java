package epmc.param.operatorevaluator.polynomialfraction;

import epmc.error.EPMCException;
import epmc.operator.Operator;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMultiplyDividePolynomialFraction implements OperatorEvaluator {
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
            if (operator != OperatorMultiply.MULTIPLY
                    && operator != OperatorDivide.DIVIDE) {
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
            return new EvaluatorMultiplyDividePolynomialFraction(this);
        }
        
    }

    private static final int NUM_IMPORT_VALUES = 2;
    private final TypePolynomialFraction typePolynomialFraction;
    private final ValuePolynomialFraction importPolynomials[] = new ValuePolynomialFraction[NUM_IMPORT_VALUES];
    private final OperatorEvaluator importSet[] = new OperatorEvaluator[NUM_IMPORT_VALUES];
    private final TypePolynomial typePolynomial;
    private final OperatorEvaluator multiplyPolynomials;
    private final boolean divide;
    private final OperatorEvaluator setPolynomial;
    private final ValuePolynomial poly1NumeratorClone;
    private final ValuePolynomial poly1DenominatorClone;
    private final ValuePolynomial poly2NumeratorClone;
    private final ValuePolynomial poly2DenominatorClone;
    private final ValuePolynomial newNumerator;
    private final ValuePolynomial newDenominator;



    private EvaluatorMultiplyDividePolynomialFraction(Builder builder) {
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
        multiplyPolynomials = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY,
                typePolynomial, typePolynomial);
        divide = builder.operator == OperatorDivide.DIVIDE;
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
        poly1NumeratorClone = typePolynomial.newValue();
        poly1DenominatorClone = typePolynomial.newValue();
        poly2NumeratorClone = typePolynomial.newValue();
        poly2DenominatorClone = typePolynomial.newValue();
        newNumerator = typePolynomial.newValue();
        newDenominator = typePolynomial.newValue();
    }

    @Override
    public Type resultType() {
        return typePolynomialFraction;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomialFraction resultFraction = ValuePolynomialFraction.as(result);
        resultFraction.adjustNumParameters();
        ValuePolynomialFraction polynomial1 = castOrImport(operands[0], 0);
        ValuePolynomialFraction polynomial2 = castOrImport(operands[1], 1);
        if (divide) {
            multiply(resultFraction.getNumerator(), resultFraction.getDenominator(),
                    polynomial1.getNumerator(), polynomial1.getDenominator(),
                    polynomial2.getDenominator(), polynomial2.getNumerator());
        } else {
            multiply(resultFraction.getNumerator(), resultFraction.getDenominator(),
                    polynomial1.getNumerator(), polynomial1.getDenominator(),
                    polynomial2.getNumerator(), polynomial2.getDenominator());
        }
        assert !resultFraction.getDenominator().toString().equals("0")
        : operands[0] + " " + operands[1] + " " + divide;
    }
    
    private void multiply(ValuePolynomial resultNumerator,
            ValuePolynomial resultDenominator,
            ValuePolynomial poly1Numerator, ValuePolynomial poly1Denominator,
            ValuePolynomial poly2Numerator, ValuePolynomial poly2Denominator)
                    throws EPMCException {
        setPolynomial.apply(poly1NumeratorClone, poly1Numerator);
        setPolynomial.apply(poly1DenominatorClone, poly1Denominator);
        setPolynomial.apply(poly2NumeratorClone, poly2Numerator);
        setPolynomial.apply(poly2DenominatorClone, poly2Denominator);
        typePolynomial.cancelCommonFactors(poly1NumeratorClone, poly2DenominatorClone);
        typePolynomial.cancelCommonFactors(poly1DenominatorClone, poly2NumeratorClone);
        multiplyPolynomials.apply(newNumerator, poly1NumeratorClone, poly2NumeratorClone);
        multiplyPolynomials.apply(newDenominator, poly1DenominatorClone, poly2DenominatorClone);
        setPolynomial.apply(resultNumerator, newNumerator);
        setPolynomial.apply(resultDenominator, newDenominator);
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
