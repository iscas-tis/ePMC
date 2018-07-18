package epmc.param.operatorevaluator.polynomial;

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;

// http://ecommons.library.cornell.edu/bitstream/1813/7262/1/96-1607.pdf

final class Geobucket {
	private final TypePolynomial typePolynomial;
	private final static int d = 4;
	private final List<ValuePolynomial> buckets = new ArrayList<>();
    private final OperatorEvaluator add;
    private final OperatorEvaluator set;
	
	Geobucket(TypePolynomial typePolynomial) {
		assert typePolynomial != null;
		this.typePolynomial = typePolynomial;
		add = ContextValue.get().getEvaluator(OperatorAdd.ADD, typePolynomial, typePolynomial);
		set = ContextValue.get().getEvaluator(OperatorSet.SET, typePolynomial, typePolynomial);
	}
	
	void clear() {
	    buckets.clear();
	}
	
	void add(ValuePolynomial poly) throws EPMCException {
		assert poly != null;
		ValuePolynomial f = typePolynomial.newValue();
		set.apply(f, poly);
		int i = Math.max(1, logd(poly.getNumTerms()));
		int dtoi = 1;
		for (int icnt = 0; icnt < i; icnt++) {
			dtoi *= d;
		}

		if (i <= buckets.size()) {
			add.apply(f, f, buckets.get(i-1));
			while (i <= buckets.size() && f.getNumTerms() > dtoi) {
				if (i < buckets.size() && 0 != buckets.get(i).getNumTerms()) {
					add.apply(f, f, buckets.get(i));
				}
				buckets.set(i - 1, typePolynomial.newValue());
				i++;
				dtoi *= d;
			}
		}
		while (buckets.size() < i) {
			ValuePolynomial fill = typePolynomial.newValue();
			buckets.add(fill);
		}

		buckets.set(i - 1, f);
	}
	
	void canonicalise(ValuePolynomial result) throws EPMCException {
	    result.set(0);
		for (int i = 0; i < buckets.size(); i++) {
			add.apply(result, result, buckets.get(i));
		}
	}
	
	private int logd(int value) {
		assert value >= 0;
		int result = 0;
		int dtoi = 1;
	    while (dtoi < value) {
	    	dtoi *= d;
	    	result++;
	    }
	    return result;
	}

	@Override
	public String toString() {
		return buckets.toString();
	}
}
