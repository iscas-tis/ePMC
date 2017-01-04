package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueArray;

public class ValueDistributionNormal implements ValueDistribution {
    private static final long serialVersionUID = 1L;
    private final TypeDistributionNormal type;
    private boolean immutable;
    private Value mean;
    private Value variance;

    ValueDistributionNormal(TypeDistributionNormal type) {
        assert type != null;
        this.type = type;
        this.mean = TypeReal.get(type.getContext()).newValue();
        this.variance = TypeReal.get(type.getContext()).newValue();
    }

    @Override
    public Value clone() {
        return new ValueDistributionNormal(type);
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
        return false;
    }

    @Override
    public boolean isInfiniteCountable() {
        return false;
    }

    @Override
    public boolean isInfiniteUncountable() {
        return true;
    }

    @Override
    public int getSupportSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void getSupportNr(ValueArray support, int number) {
        support.get(support, number);
    }

    @Override
    public void getWeightNr(ValueArray weight, int number) {
        assert false;
    }

    public void setMean(Value mean) {
        this.mean.set(mean);
    }
    
    public void getMean(Value mean) {
        mean.set(this.mean);
    }
    
    public void setVariance(Value variance) {
        this.variance.set(variance);
    }
    
    public void getVariance(Value variance) {
        variance.set(this.variance);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Normal(");
        builder.append(this.mean);
        builder.append(", ");
        builder.append(this.variance);
        builder.append(")");
        return builder.toString();
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
