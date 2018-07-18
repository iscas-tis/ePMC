package epmc.param.operatorevaluator.polynomial;

import java.math.BigInteger;

import epmc.error.EPMCException;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddSubtractPolynomial implements OperatorEvaluator {
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
            return new EvaluatorAddSubtractPolynomial(this);
        }
        
    }

    private static final int NUM_IMPORT_VALUES = 2;
    private final ValuePolynomial importPolynomials[] = new ValuePolynomial[NUM_IMPORT_VALUES];
    private final OperatorEvaluator importSet[] = new OperatorEvaluator[NUM_IMPORT_VALUES];

    private final TypePolynomial typeFunctionPolynomial;
    private final boolean subtract;
    
    private EvaluatorAddSubtractPolynomial(Builder builder) {
        assert builder != null;
        TypePolynomial typePolynomial = null;
        if (TypePolynomial.is(builder.types[0])) {
            typePolynomial = TypePolynomial.as(builder.types[0]);
        } else if (TypePolynomial.is(builder.types[1])) {
            typePolynomial = TypePolynomial.as(builder.types[1]);
        } else {
            assert false;
        }
        typeFunctionPolynomial = typePolynomial;
        importPolynomials[0] = typeFunctionPolynomial.newValue();
        importPolynomials[1] = typeFunctionPolynomial.newValue();
        if (!TypePolynomial.is(builder.types[0])) {
            importSet[0] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], typePolynomial);
        }
        if (!TypePolynomial.is(builder.types[1])) {
            importSet[1] = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[1], typePolynomial);
        }
        subtract = builder.operator == OperatorSubtract.SUBTRACT;
    }

    @Override
    public Type resultType() {
        return typeFunctionPolynomial;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomial resultPolynomial = ValuePolynomial.as(result);
        ValuePolynomial op1 = castOrImport(operands[0], 0);
        ValuePolynomial op2 = castOrImport(operands[1], 1);
        addOrSubtract(resultPolynomial, op1, op2, subtract);
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
    
    public void addOrSubtract(ValuePolynomial result,
            ValuePolynomial function1, ValuePolynomial function2, boolean subtract)
            throws EPMCException {
        result.adjustNumParameters();
        int resultSize = computeAddOrSubNumMonomials(function1, function2, subtract);
        int term1Nr = 0;
        int term2Nr = 0;
        int resultIndex = 0;
        int[] resultMonomials = new int[result.getNumParameters() * resultSize];
        BigInteger[] resultCoefficients = new BigInteger[resultSize];
        int numParameters = result.getNumParameters();
        while (term1Nr < function1.getNumTerms() && term2Nr < function2.getNumTerms()) {
            int monomCmp = compareMonomials(function1, function2, term1Nr, term2Nr);
            BigInteger coefficient1 = function1.getCoefficients()[term1Nr];
            BigInteger coefficient2 = function2.getCoefficients()[term2Nr];
            switch (monomCmp) {
            case 0:
                BigInteger resultCoefficient;
                if (subtract) {
                    resultCoefficient = coefficient1.subtract(coefficient2);
                } else {
                    resultCoefficient = coefficient1.add(coefficient2);
                }
                if (!resultCoefficient.equals(BigInteger.ZERO)) {
                    resultCoefficients[resultIndex] = resultCoefficient;
                    for (int symbolNr = 0; symbolNr < numParameters; symbolNr++) {
                        resultMonomials[numParameters * resultIndex
                                + symbolNr] = function1.getMonomials()[numParameters * term1Nr + symbolNr];
                    }
                    resultIndex++;
                }
                term1Nr++;
                term2Nr++;
                break;
            case 1:
                resultCoefficients[resultIndex] = function1.getCoefficients()[term1Nr];
                for (int symbolNr = 0; symbolNr < numParameters; symbolNr++) {
                    resultMonomials[numParameters * resultIndex
                            + symbolNr] = function1.getMonomials()[numParameters * term1Nr + symbolNr];
                }
                resultIndex++;
                term1Nr++;
                break;
            case -1:
                if (subtract) {
                    resultCoefficients[resultIndex] = function2.getCoefficients()[term2Nr].negate();
                } else {
                    resultCoefficients[resultIndex] = function2.getCoefficients()[term2Nr];
                }
                for (int symbolNr = 0; symbolNr < numParameters; symbolNr++) {
                    resultMonomials[numParameters * resultIndex
                            + symbolNr] = function2.getMonomials()[numParameters * term2Nr + symbolNr];
                }
                resultIndex++;
                term2Nr++;
                break;
            }
        }

        while (term1Nr < function1.getNumTerms()) {
            resultCoefficients[resultIndex] = function1.getCoefficients()[term1Nr];
            for (int symbolNr = 0; symbolNr < numParameters; symbolNr++) {
                resultMonomials[numParameters * resultIndex
                        + symbolNr] = function1.getMonomials()[numParameters * term1Nr + symbolNr];
            }
            resultIndex++;
            term1Nr++;
        }

        while (term2Nr < function2.getNumTerms()) {
            resultCoefficients[resultIndex] = function2.getCoefficients()[term2Nr];
            for (int symbolNr = 0; symbolNr < numParameters; symbolNr++) {
                resultMonomials[numParameters * resultIndex
                        + symbolNr] = function2.getMonomials()[numParameters * term2Nr + symbolNr];
            }
            resultIndex++;
            term2Nr++;
        }
        result.setNumTerms(resultIndex);
        result.setCoefficients(resultCoefficients);
        result.setMonomials(resultMonomials);
    }

    private int computeAddOrSubNumMonomials(ValuePolynomial function1, ValuePolynomial function2,
            boolean subtract) {
        int term1Nr = 0;
        int term2Nr = 0;
        int resultSize = 0;
        while (term1Nr < function1.getNumTerms() && term2Nr < function2.getNumTerms()) {
            int monomCmp = compareMonomials(function1, function2, term1Nr, term2Nr);
            switch (monomCmp) {
            case 0:
                BigInteger coefficient1 = function1.getCoefficients()[term1Nr];
                BigInteger coefficient2 = function2.getCoefficients()[term2Nr];
                BigInteger addTo = subtract ? coefficient2 : coefficient2.negate();
                if (!coefficient1.equals(addTo)) {
                    resultSize++;
                }
                term1Nr++;
                term2Nr++;
                break;
            case 1:
                resultSize++;
                term1Nr++;
                break;
            case -1:
                resultSize++;
                term2Nr++;
                break;
            default:
                assert false;
            }
        }
        resultSize += function1.getNumTerms() - term1Nr;
        resultSize += function2.getNumTerms() - term2Nr;
        return resultSize;
    }
    
    private int compareMonomials(ValuePolynomial function1, ValuePolynomial function2, int term1Nr,
            int term2Nr) {
        int result = 0;
        int numParameters = function1.getNumParameters();
        for (int symbolNr = 0; symbolNr < numParameters; symbolNr++) {
            int index1 = term1Nr * numParameters + symbolNr;
            int index2 = term2Nr * numParameters + symbolNr;
            if (function1.getMonomials()[index1] > function2.getMonomials()[index2]) {
                result = 1;
                break;
            } else if (function1.getMonomials()[index1] < function2.getMonomials()[index2]) {
                result = -1;
                break;
            }
        }
        return result;
    }
}
