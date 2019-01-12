package epmc.param.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.util.HashingStrategyArrayInt;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.math.BigInteger;

public final class Term implements Comparable<Term>, Cloneable {
	private BigInteger coefficient;
	private final int[] exponents;
	
	public Term(TypePolynomial type, BigInteger coefficient, int[] exponents) {
		assert type != null || coefficient != null;
		assert type != null || exponents != null;
		if (coefficient == null) {
			coefficient = BigInteger.ZERO;
		}
		if (exponents == null) {
			exponents = new int[type.getParameterSet().getNumParameters()];
		}
		this.coefficient = coefficient;
		this.exponents = Arrays.copyOf(exponents, exponents.length);
	}

	public Term(BigInteger coefficient, int[] exponents) {
		this(null, coefficient, exponents);
	}

	public Term(TypePolynomial type) {
		this(type, null, null);
	}
	
	@Override
	public boolean equals(Object obj) {
		assert obj != null;
		if (!(obj instanceof Term)) {
			return false;
		}
		Term other = (Term) obj;
		if (!this.coefficient.equals(other.coefficient)) {
			return false;
		}
		if (!Arrays.equals(this.exponents, other.exponents)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int compareTo(Term other) {
		assert other != null;
		for (int paramNr = 0; paramNr < this.exponents.length; paramNr++) {
			if (this.exponents[paramNr] < other.exponents[paramNr]) {
				return 1;
			} else if (this.exponents[paramNr] > other.exponents[paramNr]) {
				return -1;
			}
		}
		return 0;
	}
	
	public int[] getExponents() {
		return exponents;
	}
	
	@Override
	public Term clone() {
		return new Term(this.coefficient, this.exponents);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(coefficient);
		builder.append(",");
		builder.append(Arrays.toString(exponents));
		builder.append(")");
		return builder.toString();
	}
	
	public BigInteger getCoefficient() {
		return coefficient;
	}
	
	public int getExponent(int paramNr) {
		return exponents[paramNr];
	}
	
	public void setCoefficient(BigInteger coefficient) {
		assert coefficient != null;
		this.coefficient = coefficient;
	}

	public void setCoefficient(String coefficient) {
		assert coefficient != null;
		this.coefficient = new BigInteger(coefficient);
	}
	
	public void setExponent(int paramNr, int exponent) {
		assert paramNr >= 0 : paramNr;
		assert paramNr < this.exponents.length : paramNr;
		assert exponent >= 0;
		this.exponents[paramNr] = exponent;
	}
	
	public static void toPolynomial(ValuePolynomial result,
			List<Term> terms) {
		assert result != null;
		assert terms != null;
		Object2ObjectOpenCustomHashMap<int[],Term> termsMap = new Object2ObjectOpenCustomHashMap<>(HashingStrategyArrayInt.getInstance());
		for (Term term : terms) {
			int[] exponents = term.getExponents();
			if (termsMap.containsKey(exponents)) {
				Term existing = termsMap.get(exponents);
				BigInteger newCoefficient = existing.getCoefficient().add(term.getCoefficient());
				if (newCoefficient.equals(BigInteger.ZERO)) {
					termsMap.remove(exponents);
				} else {
					Term newTerm = existing.clone();
					newTerm.setCoefficient(newCoefficient);
					termsMap.put(exponents, newTerm);
				}
			} else {
				termsMap.put(term.getExponents(), term);
			}
		}
		List<Term> sortedTerms = new ArrayList<>(termsMap.values());
		sortedTerms.sort(null);
		result.resize(result.getNumParameters(), sortedTerms.size());
		for (int termNr = 0; termNr < sortedTerms.size(); termNr++) {
			Term term = sortedTerms.get(termNr);
			result.setCoefficient(termNr, term.getCoefficient());
			for (int paramNr = 0; paramNr < result.getNumParameters(); paramNr++) {
				int exponent = term.getExponent(paramNr);
				result.setExponent(paramNr, termNr, exponent);
			}
		}
	}
}
