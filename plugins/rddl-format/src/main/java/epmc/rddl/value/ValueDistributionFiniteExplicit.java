package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public class ValueDistributionFiniteExplicit implements ValueDistribution {
    private static final long serialVersionUID = 1L;
    private final TypeDistributionFiniteExplicit type;
    private boolean immutable;
    private int size;
    private ValueArray support;
    private ValueArray weights;

    ValueDistributionFiniteExplicit(TypeDistributionFiniteExplicit type) {
        assert type != null;
        this.type = type;
        this.support = epmc.value.UtilValue.newArray(type.getEntryType().getTypeArray(), type.size());
        this.weights = newValueArrayWeight(type.size());
    }

    @Override
    public Value clone() {
        return new ValueDistributionFiniteExplicit(type);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return this.immutable;
    }
    
    @Override
    public boolean isFinite() {
        return true;
    }

    @Override
    public boolean isInfiniteCountable() {
        return false;
    }

    @Override
    public boolean isInfiniteUncountable() {
        return false;
    }

    @Override
    public int getSupportSize() {
        return size;
    }

    @Override
    public void getSupportNr(ValueArray support, int number) {
        support.get(support, number);
    }

    @Override
    public void getWeightNr(ValueArray weights, int number) {
        weights.get(support, number);
    }

    void setSupport(Value value, int number) {
        this.support.set(value, number);
    }
    
    void getSupport(Value value, int number) {
        this.support.get(value, number);
    }

    void setWeight(Value value, int number) {
        this.weights.set(value, number);
    }
    
    void getWeight(Value value, int number) {
        this.weights.get(value, number);
    }

    private ValueArrayAlgebra newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get(getContextValue()).getTypeArray();
        return epmc.value.UtilValue.newArray(typeArray, size);
    }
    
    private ContextValue getContextValue() {
    	return support.getType().getContext();
    }

	@Override
	public void set(Value value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(Value other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEq(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double distance(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
