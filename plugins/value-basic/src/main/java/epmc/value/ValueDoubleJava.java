package epmc.value;

import epmc.options.Options;
import epmc.util.BitStream;

import static epmc.error.UtilError.fail;

public final class ValueDoubleJava implements ValueDouble {
    public static boolean is(Value value) {
        return value instanceof ValueDoubleJava;
    }

    public static ValueDoubleJava as(Value value) {
        if (is(value)) {
            return (ValueDoubleJava) value;
        } else {
            return null;
        }
    }

    private final static String NAN = "NaN";
    private final static String DIVIDED = "/";

    private final TypeDoubleJava type;
    private double value;
    private final boolean outputNative = Options.get().getBoolean(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_NATIVE);
    private final String format = Options.get().getString(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT);
    
    ValueDoubleJava(TypeDoubleJava type, double value) {
        assert type != null;
        this.type = type;
        this.value = value;
    }

    ValueDoubleJava(TypeDoubleJava type) {
        this(type, 0.0);
    }    

    @Override
    public TypeDoubleJava getType() {
        return type;
    }

    @Override
    public double getDouble() {
        return value;
    }

    @Override
    public void set(double value) {
        this.value = value;
    }

    @Override
    public ValueDoubleJava clone() {
        return new ValueDoubleJava(getType(), value);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueDoubleJava)) {
            return false;
        }
        ValueDoubleJava other = (ValueDoubleJava) obj;
        if (Double.isNaN(this.value) != Double.isNaN(other.value)) {
            return false;
        }
        if (!Double.isNaN(this.value) && this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        if (Double.isNaN(value)) {
            return -232;
        }
        if (Double.isInfinite(this.value)) {
            return -23333;
        }
        final int low = (int) Double.doubleToLongBits(value);
        final int up = (int) (Double.doubleToLongBits(value) >> 32);
        hash = low + (hash << 6) + (hash << 16) - hash;
        hash = up + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        if (Double.isNaN(value)) {
            return NAN;
        } else {
            if (outputNative) {
                return String.valueOf(value);
            } else {
                assert format != null;
                return String.format(format, value);
            }
        }
    }

    @Override
    public int getInt() {
        return (int) getDouble();
    }

    @Override
    public void set(int operand) {
        set((double) operand);
    }

    @Override
    public void set(String string) {
        assert string != null;
        if (string.contains(DIVIDED)) {
            String[] parts = string.split(DIVIDED);
            assert parts.length == 2;
            String numString = parts[0];
            String denString = parts[1];
            double num = Double.parseDouble(numString);
            double den = Double.parseDouble(denString);
            value = num / den;
        } else {
            try {
                value = Double.parseDouble(string);
            } catch (NumberFormatException e) {
                fail(ProblemsValueBasic.VALUES_STRING_INVALID_VALUE, e, value, type);
            }
        }

    }

    @Override
    public void read(BitStream reader) {
        value = reader.readDouble();
    }

    @Override
    public void write(BitStream writer) {
        writer.writeDouble(value);
    }
}
