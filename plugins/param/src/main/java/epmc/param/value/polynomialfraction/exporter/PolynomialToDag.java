package epmc.param.value.polynomialfraction.exporter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMultiply;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;

public final class PolynomialToDag {
    private final TypePolynomial typePolynomial;
    private final TypeDag typeDag;
    private final OperatorEvaluator operatorAddDag;
    private final OperatorEvaluator operatorMultiplyDag;
    private final Map<ValuePolynomial,ValueDag> cache;
    private final int numParameters;
    
    public PolynomialToDag(TypePolynomial typePolynomial) {
        assert typePolynomial != null;
        cache = new HashMap<>();
        this.typePolynomial = typePolynomial;
        typeDag = ContextValue.get().makeUnique(new TypeDag(typePolynomial.getParameterSet()));
        operatorAddDag = ContextValue.get().getEvaluator(OperatorAdd.ADD, typeDag, typeDag);
        operatorMultiplyDag = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, typeDag, typeDag);
        numParameters = typePolynomial.getParameterSet().getNumParameters();
    }
    
    public ValueDag convert(ValuePolynomial polynomial) {
        return recPolynomialToDag(polynomial);
    }
    
    private ValueDag recPolynomialToDag(ValuePolynomial polynomial) {
        ValueDag result = cache.get(polynomial);
        if (result != null) {
            return result;
        }
        if (polynomial.isNumerical()) {
            result = getNumerical(polynomial);
        } else {
            polynomial.adjustNumParameters();
            int chosenParamNr = chooseParameter(polynomial);
            ValuePolynomial inner = typePolynomial.newValue();
            ValuePolynomial rest = typePolynomial.newValue();
            int numTermsInner = countNumTermsInner(polynomial, chosenParamNr);
            inner.resize(numTermsInner);
            rest.resize(polynomial.getNumTerms() - numTermsInner);
            int innerTermIndex = 0;
            int restTermIndex = 0;
            for (int termNr = 0; termNr < polynomial.getNumTerms(); termNr++) {
                if (termContainsParameter(polynomial, termNr, chosenParamNr)) {
                    setTerm(inner, polynomial, innerTermIndex, termNr);
                    inner.getMonomials()[numParameters * innerTermIndex + chosenParamNr]--;
                    innerTermIndex++;
                } else {
                    setTerm(rest, polynomial, restTermIndex, termNr);
                    restTermIndex++;
                }
            }
            result = combine(chosenParamNr,
                    recPolynomialToDag(inner),
                    recPolynomialToDag(rest));
        }
        cache.put(polynomial, result);
        return result;
    }
    
    private int countNumTermsInner(ValuePolynomial polynomial, int chosenParamNr) {
        int numTermsInner = 0;
        for (int termNr = 0; termNr < polynomial.getNumTerms(); termNr++) {
            if (termContainsParameter(polynomial, termNr, chosenParamNr)) {
                numTermsInner++;
            }
        }
        return numTermsInner;
    }

    private boolean termContainsParameter(ValuePolynomial polynomial, int termNr, int chosenParamNr) {
        return polynomial.getMonomials()[numParameters * termNr + chosenParamNr] > 0;
    }

    private ValueDag getNumerical(ValuePolynomial polynomial) {
        ValueDag result = typeDag.newValue();
        result.set(polynomial.getBigInteger(), BigInteger.ONE);
        return result;
    }

    private int chooseParameter(ValuePolynomial polynomial) {
        int[] maxNumTermsHas = new int[numParameters];
        for (int termNr = 0; termNr < polynomial.getNumTerms(); termNr++) {
            for (int paramNr = 0; paramNr < numParameters; paramNr++) {
                if (polynomial.getMonomials()[numParameters * termNr + paramNr] > 0) {
                    maxNumTermsHas[paramNr]++;
                }
            }
        }
        int param = -1;
        int paramMaxNum = 0;
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            if (maxNumTermsHas[paramNr] > paramMaxNum) {
                param = paramNr;
                paramMaxNum = maxNumTermsHas[paramNr];
            }
        }
        return param;
    }

    private ValueDag combine(int chosenParamNr, ValueDag inner, ValueDag rest) {
        assert inner != null;
        assert rest != null;
        ValueDag result = typeDag.newValue();
        result.setParameter(typeDag.getParameterSet().getParameter(chosenParamNr));
        operatorMultiplyDag.apply(result, result, inner);
        operatorAddDag.apply(result, result, rest);
        return result;
    }
    
    private void setTerm(ValuePolynomial target, ValuePolynomial source, int targetIndex, int sourceIndex) {
        BigInteger coefficient = source.getCoefficient(sourceIndex);
        target.setCoefficient(targetIndex, coefficient);
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            target.getMonomials()[targetIndex * numParameters + paramNr]
                    = source.getMonomials()[sourceIndex * numParameters + paramNr];
        }
    }

}
