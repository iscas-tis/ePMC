package epmc.param.value.gmp;

import java.math.BigInteger;

import com.sun.jna.Pointer;

import epmc.param.value.rational.UtilRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.Value;

public final class ValueMPQ implements ValueRational {
    private final static String DIVIDE = "/";
    private final TypeMPQ type;
    private final MPQSingleMemory content = new MPQSingleMemory();
    private final MPFRSingleMemory convert = new MPFRSingleMemory();

    public static boolean is(Value value) {
        return value instanceof ValueMPQ;
    }
    
    public static ValueMPQ as(Value value) {
        if (is(value)) {
            return (ValueMPQ) value;
        } else {
            return null;
        }
    }
    
    ValueMPQ(TypeMPQ type) {
        assert type != null;
        this.type = type;
    }
    
    @Override
    public double getDouble() {
        return GMP.gmp_util_mpq_get_double(content, convert);
    }

    @Override
    public int getInt() {
        return GMP.gmp_util_mpq_get_int(content);
    }

    @Override
    public void set(int value) {
        GMP.gmp_util_mpq_set_int(content, value);
    }

    @Override
    public void set(String value) {
        assert value != null;
        if (value.contains(DIVIDE)) {
            GMP.__gmpq_set_str(content, value, 10);
        } else {
            UtilRational.set(this, value);
        }
    }

    @Override
    public void set(BigInteger numerator, BigInteger denominator) {
        GMP.__gmpq_set_str(content, numerator + DIVIDE + denominator, 10);
    }

    @Override
    public BigInteger getNumerator() {
        Pointer numPtr = GMP.gmp_util_mpq_get_num(content);
        BigInteger result = new BigInteger(numPtr.getString(0));
        GMP.gmp_util_free_string(numPtr);
        return result;
    }

    @Override
    public BigInteger getDenominator() {
        Pointer denPtr = GMP.gmp_util_mpq_get_den(content);
        BigInteger result = new BigInteger(denPtr.getString(0));
        GMP.gmp_util_free_string(denPtr);
        return result;
    }

    @Override
    public TypeMPQ getType() {
        return type;
    }

    @Override
    public String toString() {
        Pointer p = GMP.__gmpq_get_str(null, 10, content);
        String result = p.getString(0);
        GMP.gmp_util_free_string(p);
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueMPQ)) {
            return false;
        }
        ValueMPQ other = (ValueMPQ) obj;
        if (type != other.type) {
            return false;
        }
        if (GMP.gmp_util_mpq_equals(content, other.content) == 0) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = type.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = getNumerator().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = getDenominator().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    public MPQSingleMemory getContent() {
        return content;
    }
}
