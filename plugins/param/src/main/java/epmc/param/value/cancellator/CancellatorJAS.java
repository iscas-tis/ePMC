package epmc.param.value.cancellator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.jas.arith.BigInteger;
import edu.jas.poly.ExpVector;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.ufd.Quotient;
import edu.jas.ufd.QuotientRing;
import epmc.param.value.Term;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;

public final class CancellatorJAS implements Cancellator {
    public final static class Builder implements Cancellator.Builder {
        private TypePolynomial type;

        @Override
        public Builder setType(TypePolynomial type) {
            this.type = type;
            return this;
        }

        @Override
        public Cancellator build() {
            return new CancellatorJAS(this);
        }
        
    }
    
    public final static String IDENTIFIER = "jas";
    
	private int previousNumParameters = -1;
	private GenPolynomialRing<BigInteger> jasPolyRing;
	private QuotientRing<BigInteger> jasQuotRing;
	private final TypePolynomial type;

	private CancellatorJAS(Builder builder) {
	    assert builder != null;
		assert builder.type != null;
		this.type = builder.type;
		rebuildJASIfNeeded();
	}
	
	private void rebuildJASIfNeeded() {
		if (previousNumParameters == type.getParameterSet().getNumParameters()) {
			return;
		}
		BigInteger fac = new BigInteger();
		String[] pNameReversed = new String[type.getParameterSet().getNumParameters()];
		for (int paramNr = 0; paramNr < pNameReversed.length; paramNr++) {
			pNameReversed[paramNr] = type.getParameterSet().getParameters().get(paramNr).toString();
		}
		Collections.reverse(Arrays.asList(pNameReversed));
		jasPolyRing = new GenPolynomialRing<>(fac, pNameReversed.length, pNameReversed);
		jasQuotRing = new QuotientRing<>(jasPolyRing);
		this.previousNumParameters = type.getParameterSet().getNumParameters();
	}

	@Override
	public void cancel(ValuePolynomial operand1,
			ValuePolynomial operand2) {
		rebuildJASIfNeeded();
		GenPolynomial<BigInteger> jas1 = toJAS(operand1);
		GenPolynomial<BigInteger> jas2 = toJAS(operand2);
		Quotient<BigInteger> quot = jasQuotRing.create(jas1, jas2);
		jas1 = quot.num;
		jas2 = quot.den;
		toPolynomial(operand1, jas1);
		toPolynomial(operand2, jas2);
	}

	private GenPolynomial<BigInteger> toJAS(ValuePolynomial operand) {
		assert operand != null;
		GenPolynomial<BigInteger> result = jasPolyRing.fromInteger(0);
		for (int termNr = 0; termNr < operand.getNumTerms(); termNr++) {
			GenPolynomial<BigInteger> term = jasPolyRing.fromInteger(operand.getCoefficient(termNr));
			for (int paramNr = 0; paramNr < operand.getNumParameters(); paramNr++) {
				int exponent = operand.getExponent(paramNr, termNr);
				GenPolynomial<BigInteger> paramPol = jasPolyRing.univariate(paramNr, exponent);
				term = term.multiply(paramPol);
			}
			result.doAddTo(term);
		}
		return result;
	}

	private void toPolynomial(ValuePolynomial result,
			GenPolynomial<BigInteger> jas) {
		assert result != null;
		assert jas != null;
		Iterator<BigInteger> coeffIter = jas.coefficientIterator();
		Iterator<ExpVector> expIter = jas.exponentIterator();
		List<Term> terms = new ArrayList<>();
		while (coeffIter.hasNext()) {
			BigInteger coefficient = coeffIter.next();
			ExpVector exponent = expIter.next();
			terms.add(new Term(new java.math.BigInteger(coefficient.toString()),
					expVectorToArray(exponent)));
		}
		Term.toPolynomial(result, terms);
	}

	private static int[] expVectorToArray(ExpVector exponents) {
		int[] result = new int[exponents.length()];
		for (int exponentNr = 0; exponentNr < exponents.length(); exponentNr++) {
			result[exponentNr] = (int) exponents.getVal(exponentNr);
		}
		return result;
	}
}
