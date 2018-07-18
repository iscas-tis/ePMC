package epmc.param.operatorevaluator.polynomial;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMultiplyPolynomial implements OperatorEvaluator {
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
            if (operator != OperatorMultiply.MULTIPLY) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypePolynomial.is(types[0]) && !TypePolynomial.is(types[1])) {
                return null;
            }
            TypePolynomial typePolynomial = null;
            if (TypePolynomial.is(types[0])) {
                typePolynomial = TypePolynomial.as(types[0]);
            } else if (TypePolynomial.is(types[1])) {
                typePolynomial = TypePolynomial.as(types[1]);
            } else {
                assert false;
            }
            for (Type type : types) {
                if (!TypePolynomial.is(type)
                        && ContextValue.get().getEvaluatorOrNull(OperatorSet.SET, type, typePolynomial) == null) {
                    return null;
                }
            }
            return new EvaluatorMultiplyPolynomial(this);
        }
        
    }

    private static final int NUM_IMPORT_VALUES = 2;
    private final ValuePolynomial importPolynomials[] = new ValuePolynomial[NUM_IMPORT_VALUES];
    private final OperatorEvaluator importSet[] = new OperatorEvaluator[NUM_IMPORT_VALUES];
    private final TypePolynomial typePolynomial;
    private final Geobucket bucket;

    
    private EvaluatorMultiplyPolynomial(Builder builder) {
        assert builder != null;
        TypePolynomial typePolynomial = null;
        if (TypePolynomial.is(builder.types[0])) {
            typePolynomial = TypePolynomial.as(builder.types[0]);
        } else if (TypePolynomial.is(builder.types[1])) {
            typePolynomial = TypePolynomial.as(builder.types[1]);
        } else {
            assert false;
        }
        this.typePolynomial = typePolynomial;
        importPolynomials[0] = typePolynomial.newValue();
        importPolynomials[1] = typePolynomial.newValue();
        if (!TypePolynomial.is(builder.types[0])) {
            importSet[0] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], typePolynomial);
        }
        if (!TypePolynomial.is(builder.types[1])) {
            importSet[1] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[1], typePolynomial);
        }
        bucket = new Geobucket(typePolynomial);
    }

    @Override
    public Type resultType() {
        return typePolynomial;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomial resultPolynomial = ValuePolynomial.as(result);
        
        resultPolynomial.adjustNumParameters();
        TypePolynomial type = resultPolynomial.getType();
        ValuePolynomial function1 = castOrImport(operands[0], 0);
        ValuePolynomial function2 = castOrImport(operands[1], 1);
        bucket.clear();
        int numTerms = function2.getNumTerms();
        for (int termNr = 0; termNr < numTerms; termNr++) {
            ValuePolynomial multiplied = type.newValue();
            multByTerm(multiplied, function1, function2, termNr);
            bucket.add(multiplied);
        }
        bucket.canonicalise(resultPolynomial);
    }
    
    private ValuePolynomial castOrImport(Value operand, int number) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (importSet[number] == null) {
            ValuePolynomial result = ValuePolynomial.as(operand);
            result.adjustNumParameters();
            return result;
        } else {
            importSet[number].apply(importPolynomials[number], operand);
            return importPolynomials[number];
        }
    }
    
    private void multByTerm(ValuePolynomial result, ValuePolynomial p1, ValuePolynomial p2, int whichTerm) {
        result.adjustNumParameters();
        p1.adjustNumParameters();
        p2.adjustNumParameters();
        int numTerms = p1.getNumTerms();
        int numParameters = result.getNumParameters();
        int[] monomials = new int[numParameters * numTerms];
        BigInteger[] coefficients = new BigInteger[numTerms];
        for (int termNr = 0; termNr < p1.getNumTerms(); termNr++) {
            coefficients[termNr] = p1.getCoefficients()[termNr]
                    .multiply(p2.getCoefficients()[whichTerm]);
            for (int symbolNr = 0; symbolNr < numParameters; symbolNr++) {
                monomials[numParameters * termNr + symbolNr] = p1.getMonomials()[numParameters * termNr + symbolNr]
                        + p2.getMonomials()[numParameters * whichTerm + symbolNr];
            }
        }

        result.setNumTerms(numTerms);
        result.setMonomials(monomials);
        result.setCoefficients(coefficients);
    }

}
