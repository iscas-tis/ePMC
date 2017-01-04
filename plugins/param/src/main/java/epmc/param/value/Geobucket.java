package epmc.param.value;

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.value.UtilValue;

// http://ecommons.library.cornell.edu/bitstream/1813/7262/1/96-1607.pdf

final class Geobucket {
	private final TypeFunctionPolynomial typePolynomial;
	private final static int d = 4;
	private final List<ValueFunctionPolynomial> buckets = new ArrayList<>();
	
	Geobucket(TypeFunctionPolynomial typeFunctionPolynomial) {
		assert typeFunctionPolynomial != null;
		this.typePolynomial = typeFunctionPolynomial;
	}
	
	void add(ValueFunctionPolynomial poly) throws EPMCException {
		assert poly != null;
		ValueFunctionPolynomial f = UtilValue.clone(poly);
		int i = Math.max(1, logd(poly.getNumTerms()));
		int dtoi = 1;
		for (int icnt = 0; icnt < i; icnt++) {
			dtoi *= d;
		}
		
		if (i <= buckets.size()) {
			ValueFunctionPolynomial fp = typePolynomial.newValue();
			fp.add(f, buckets.get(i-1));
			f = fp;
			while (i <= buckets.size() && f.getNumTerms() > dtoi) {
				if (i < buckets.size() && 0 != buckets.get(i).getNumTerms()) {
					fp.add(f, buckets.get(i));
					f = fp;
				}
				buckets.set(i - 1, typePolynomial.newValue());
				i++;
				dtoi *= d;
			}
		}
		while (buckets.size() < i) {
			ValueFunctionPolynomial fill = typePolynomial.newValue();
			buckets.add(fill);
		}

		buckets.set(i - 1, f);
	}
	
	ValueFunctionPolynomial canonicalise() throws EPMCException {
		ValueFunctionPolynomial result = typePolynomial.newValue();
		for (int i = 0; i < buckets.size(); i++) {
			ValueFunctionPolynomial resultP = typePolynomial.newValue();
			resultP.add(result, buckets.get(i));
			result = resultP;
		}
		return result;
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
