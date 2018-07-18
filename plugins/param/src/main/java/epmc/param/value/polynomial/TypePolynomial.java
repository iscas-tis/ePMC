package epmc.param.value.polynomial;

import java.math.BigInteger;
import java.util.HashMap;

import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorSet;
import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.cancellator.Cancellator;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.ValueBoolean;

public final class TypePolynomial implements TypeFunction {
    private final static class CancelEntry {
        private ValuePolynomial poly1;
        private ValuePolynomial poly2;
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CancelEntry)) {
                return false;
            }
            CancelEntry other = (CancelEntry) obj;
            if (!this.poly1.equals(other.poly1)) {
                return false;
            }
            if (!this.poly2.equals(other.poly2)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 0;
            hash = poly1.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = poly2.hashCode() + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
    }
    
    CancelEntry testEntry = new CancelEntry();
    private boolean doCache = true;
    HashMap<CancelEntry, CancelEntry> cache = new HashMap<>();
    
    private final static String POLYNOMIAL = "polynomial";
    
    public static boolean is(Type type) {
        return type instanceof TypePolynomial;
    }

    public static TypePolynomial as(Type type) {
        if (is(type)) {
            return (TypePolynomial) type;
        } else {
            return null;
        }
    }
    
	private final ParameterSet parameterSet;
	private final Cancellator cancellator;
    private final OperatorEvaluator setValue;
    private final OperatorEvaluator isOne;
    private final OperatorEvaluator isZero;
    private final ValueBoolean cmp;

	public TypePolynomial(ParameterSet parameterSet) {
		assert parameterSet != null;
		this.parameterSet = parameterSet;
		Cancellator.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_CANCELLATOR);
		cancellator = builder
		        .setType(this)
		        .build();
		setValue = ContextValue.get().getEvaluator(OperatorSet.SET, this, this);
		isOne = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE, this);
		isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, this);
		cmp = TypeBoolean.get().newValue();
	}

	@Override
	public ParameterSet getParameterSet() {
		return this.parameterSet;
	}

	@Override
	public ValuePolynomial newValue() {
		ValuePolynomial function = new ValuePolynomial(this);
		return function;
	}
	
	public void cancelCommonFactors(ValuePolynomial operand1, ValuePolynomial operand2) {
		assert operand1 != null;
		assert operand2 != null;
		isZero.apply(cmp, operand1);
		if (cmp.getBoolean()) {
		    return;
		}
		isZero.apply(cmp, operand2);
		if (cmp.getBoolean()) {
		    return;
		}
		isOne.apply(cmp, operand1);
		if (cmp.getBoolean()) {
		    return;
		}
		isOne.apply(cmp, operand2);
		if (cmp.getBoolean()) {
		    return;
		}
		if (operand1.equals(operand2)) {
		    operand1.set(1);
		    operand2.set(1);
		    return;
		}
		if (operand1.isNumerical()
		        && operand2.isNumerical()) {
		    BigInteger op1Big = operand1.getBigInteger();
		    BigInteger op2Big = operand2.getBigInteger();
		    BigInteger gcd = op1Big.gcd(op2Big);
		    op1Big = op1Big.divide(gcd);
		    op2Big = op2Big.divide(gcd);
		    operand1.set(op1Big);
		    operand2.set(op2Big);
		    return;
		}
        ValuePolynomial orig1 = null;
        ValuePolynomial orig2 = null;
		if (doCache) {
		    testEntry.poly1 = operand1;
		    testEntry.poly2 = operand2;
		    CancelEntry resultEntry = cache.get(testEntry);
		    if (resultEntry != null) {
		        setValue.apply(operand1, resultEntry.poly1);
		        setValue.apply(operand2, resultEntry.poly2);
		        return;
		    }
		    testEntry.poly1 = operand2;
		    testEntry.poly2 = operand1;
		    resultEntry = cache.get(testEntry);
            if (resultEntry != null) {
                setValue.apply(operand1, resultEntry.poly2);
                setValue.apply(operand2, resultEntry.poly1);
                return;
            }
		    orig1 = newValue();
		    setValue.apply(orig1, operand1);
		    orig2 = newValue();
		    setValue.apply(orig2, operand2);
		}
		cancellator.cancel(operand1, operand2);
		if (doCache) {
		    ValuePolynomial cancel1 = newValue();
		    setValue.apply(cancel1, operand1);
		    ValuePolynomial cancel2 = newValue();
		    setValue.apply(cancel2, operand2);
		    CancelEntry orig = new CancelEntry();
		    orig.poly1 = orig1;
		    orig.poly2 = orig2;
		    CancelEntry canceled = new CancelEntry();		
		    canceled.poly1 = cancel1;
		    canceled.poly2 = cancel2;
		    cache.put(orig, canceled);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(POLYNOMIAL);
		builder.append(getParameterSet().getParameters());
		return builder.toString();
	}

	@Override
	public TypeArrayPolynomial getTypeArray() {
	    return ContextValue.get().makeUnique(new TypeArrayPolynomial(this));
	}
}
