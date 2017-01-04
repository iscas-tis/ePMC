package epmc.value;

import epmc.error.EPMCException;
import epmc.util.BitStream;
import epmc.value.Value;

public final class ValueEnum implements ValueEnumerable, ValueNumBitsKnown, ValueBitStoreable {
	public static boolean isEnum(Value value) {
		return value instanceof ValueEnum;
	}
	
	public static ValueEnum asEnum(Value value) {
		if (isEnum(value)) {
			return (ValueEnum) value;
		} else {
			return null;
		}
	}
	
    private Enum<?> value;
    private final TypeEnum type;
    private boolean immutable;
    
    ValueEnum(TypeEnum type) {
        assert type != null;
        assert TypeEnum.isEnum(type);
        this.type = type;
        value = type.getEnumClass().getEnumConstants()[0];
    }

    ValueEnum(ValueEnum other) {
        assert other != null;
        this.type = (TypeEnum) other.getType();
        this.value = other.value;
    }
    
    @Override
    public TypeEnum getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public  <T extends Enum<?>> T getEnum() {
        return (T) value;
    }

    public Class<? extends Enum<?>> getEnumClass() {
        return getType().getEnumClass();
    }
    
    public void set(Enum<?> value) {
        assert !isImmutable();
        assert value != null;
        assert value.getClass() == getType().getEnumClass() : value.getClass()
                + " != " + getType().getEnumClass();
        this.value = value;
    }
    
    @Override
    public ValueEnum clone() {
        return new ValueEnum(this);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueEnum)) {
            return false;
        }
        ValueEnum other = (ValueEnum) obj;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
    
    @Override
    public void set(Value op) {
        assert !isImmutable();
        assert op != null;
        assert ValueEnum.isEnum(op) : op;
        assert getType().getEnumClass() == TypeEnum.asEnum(op.getType()).getEnumClass();
        set((Enum<?>) ValueEnum.asEnum(op).getEnum());
    }

    @Override
    public boolean isEq(Value other) throws EPMCException {
        assert other != null;
        assert ValueEnum.isEnum(other);
        assert this.getType().getEnumClass() == TypeEnum.asEnum(other.getType()).getEnumClass();
        return this.value == ((ValueEnum) other).value;
    }
    
    @Override
    public void write(BitStream writer) {
        assert writer != null;
        int value = this.value.ordinal();
        int marker = 1;
        for (int bitNr = 0; bitNr < getNumBits(); bitNr++) {
            writer.write((value & marker) != 0);
            marker <<= 1;
        }
    }
    
    @Override
    public void read(BitStream reader) {
        assert !isImmutable();
        assert reader != null;
        int value = 0;
        int marker = 1;
        for (int bitNr = 0; bitNr < getNumBits(); bitNr++) {
            if (reader.read()) {
                value |= marker;
            }
            marker <<= 1;
        }
        assert value >= 0;
        assert value < getType().getEnumClass().getEnumConstants().length;
        this.value = getType().getEnumClass().getEnumConstants()[value];
    }
    
    @Override
    public int compareTo(Value other) {
        assert other != null;
        assert ValueEnum.isEnum(other);
        assert this.getType().getEnumClass() == TypeEnum.asEnum(other.getType()).getEnumClass();
        return Integer.compare(value.ordinal(), ((ValueEnum) other).value.ordinal());
    }
    
    @Override
    public int getValueNumber() {
        return value.ordinal();
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public double distance(Value other) throws EPMCException {
    	ValueEnum otherEnum = asEnum(other);
    	return value == otherEnum.value ? 0.0 : 1.0;
    }
    
    @Override
    public void setValueNumber(int number) {
        assert number >= 0 : number;
        assert number < type.getNumValues() : number;
        set(type.getEnumClass().getEnumConstants()[number]);
    }

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
