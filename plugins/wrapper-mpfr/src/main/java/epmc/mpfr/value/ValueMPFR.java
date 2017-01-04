package epmc.mpfr.value;

import java.io.IOException;
import java.lang.reflect.Field;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import epmc.error.EPMCException;
import epmc.mpfr.MPFR;
import epmc.mpfr.MPFRMemory;
import epmc.mpfr.options.OptionsMPFR;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;
import epmc.value.ValueTrigonometric;

public final class ValueMPFR implements ValueReal, ValueTrigonometric {
	private final static String SPACE = " ";
	/** 1L, as I don't know any better. */
	private static final long serialVersionUID = 1L;
    private final static String DIVIDED = "/";
    private static final int NUM_IMPORT_VALUES = 2;
    private final static int DECIMAL_BASE = 10;
    /** Output format allowing to store and restore MPFR number exactly. */
    private final static String FORMAT_EXACT = "%Re";
	private final static String FIELD_MPFR = "mpfr";
	private final static String FIELD_IMPORT_MPFR = "importMPFR";
    
    private final transient Pointer importMPFR[] = new Pointer[NUM_IMPORT_VALUES];
    private transient Pointer distanceValue;
    private final TypeMPFR type;
	private boolean immutable;
	private final transient Pointer mpfr;

	public ValueMPFR(TypeMPFR type) {
		assert type != null;
		this.type = type;
        mpfr = new MPFRMemory(getPrecision());
	}

	@Override
	public ValueMPFR clone() {
		ValueMPFR result = new ValueMPFR(type);
		result.set(this);
		return result;
	}

	@Override
	public TypeMPFR getType() {
		return type;
	}

	@Override
	public void setImmutable() {
		immutable = true;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	void free() {
		MPFR.mpfr_clear(mpfr);
	}
	
	@Override
	public void set(String string) throws EPMCException {
		assert string != null;
        assert string != null;
        if (string.contains(DIVIDED)) {
            String[] parts = string.split(DIVIDED);
            assert parts.length == 2;
            String numString = parts[0];
            String denString = parts[1];
            Pointer num = new MPFRMemory(getPrecision());
            Pointer den = new MPFRMemory(getPrecision());
            MPFR.mpfr_set_str(num, numString, DECIMAL_BASE, MPFR.MPFR_RNDN);
            MPFR.mpfr_set_str(den, denString, DECIMAL_BASE, MPFR.MPFR_RNDN);
            MPFR.mpfr_div(getMpfr(), num, den, MPFR.MPFR_RNDN);
        } else {
            MPFR.mpfr_set_str(mpfr, string, DECIMAL_BASE, MPFR.MPFR_RNDN);
        }
	}
	
	@Override
	public void set(double value) {
		MPFR.mpfr_set_d(mpfr, value, MPFR.MPFR_RNDN);
	}
	
	@Override
	public void set(int value) {
		MPFR.mpfr_set_si(mpfr, new NativeLong(value), MPFR.MPFR_RNDN);
	}

	@Override
	public void set(Value value) {
		assert value != null;
		if (value instanceof ValueMPFR) {
			ValueMPFR valueMPFR = (ValueMPFR) value;
			MPFR.mpfr_set(getMpfr(), valueMPFR.getMpfr(), MPFR.MPFR_RNDN);
		} else if (ValueInteger.isInteger(value)) {
			int valueInt = ValueInteger.asInteger(value).getInt();
			MPFR.mpfr_set_si(getMpfr(), new NativeLong(valueInt), MPFR.MPFR_RNDN);
		} else if (ValueReal.isReal(value)) {
			double valueDouble = ValueReal.asReal(value).getDouble();
			MPFR.mpfr_set_d(getMpfr(), valueDouble, MPFR.MPFR_RNDN);
		} else {
			assert false : value;
		}
	}
	
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueMPFR)) {
            return false;
        }
        ValueMPFR other = (ValueMPFR) obj;
        
        if ((MPFR.mpfr_nan_p(getMpfr()) == 0) != (MPFR.mpfr_nan_p(other.getMpfr()) == 0)) {
            return false;
        }
        if (MPFR.mpfr_nan_p(getMpfr()) == 0
        		&& MPFR.mpfr_cmp(getMpfr(), other.getMpfr()) != 0) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
    	/* following not great, but should suffice for now. */
        Pointer strPoint = new Memory(Pointer.SIZE);
        MPFR.mpfr_asprintf(strPoint, FORMAT_EXACT, mpfr);
        String string = strPoint.getPointer(0).getString(0);
        MPFR.mpfr_free_str(strPoint.getPointer(0));
        return string.hashCode();
    }
	
	@Override
	public String toString() {
        Pointer strPoint = new Memory(Pointer.SIZE);
        String format = getType().getContext().getOptions().get(OptionsMPFR.MPFR_OUTPUT_FORMAT);
        if (format == null) {
        	format = "%Re";
        }
        assert format != null;
        MPFR.mpfr_asprintf(strPoint, format, mpfr);
        String string = strPoint.getPointer(0).getString(0);
        MPFR.mpfr_free_str(strPoint.getPointer(0));
        return string;
	}
	
    @Override
    public void add(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        Pointer mpfrOp1 = castOrImport(op1, 0);
        Pointer mpfrOp2 = castOrImport(op2, 1);
        MPFR.mpfr_add(getMpfr(), mpfrOp1, mpfrOp2, MPFR.MPFR_RNDN);
    }

    @Override
    public void multiply(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        Pointer mpfrOp1 = castOrImport(op1, 0);
        Pointer mpfrOp2 = castOrImport(op2, 1);
        MPFR.mpfr_mul(getMpfr(), mpfrOp1, mpfrOp2, MPFR.MPFR_RNDN);
    }

    @Override
    public void multInverse(Value op) {
        assert !isImmutable();
        assert op != null;
        divide(getType().getOne(), op);
    }

    @Override
    public int ceilInt() {
    	MPFRMemory mem = new MPFRMemory(getPrecision());
    	MPFR.mpfr_ceil(mem, getMpfr());
    	return MPFR.mpfr_get_si(mem, MPFR.MPFR_RNDN).intValue();
    }

    @Override
    public int floorInt() {
    	MPFRMemory mem = new MPFRMemory(getPrecision());
    	MPFR.mpfr_floor(mem, getMpfr());
    	return MPFR.mpfr_get_si(mem, MPFR.MPFR_RNDN).intValue();
    }
    
    @Override
    public int intcastInt() {
    	MPFRMemory mem = new MPFRMemory(getPrecision());
    	MPFR.mpfr_trunc(mem, getMpfr());
    	return MPFR.mpfr_get_si(mem, MPFR.MPFR_RNDN).intValue();
    }

    @Override
    public int signInt() {
    	return MPFR.mpfr_sgn(getMpfr());
    }

    @Override
    public void log(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        Pointer mpfrOp1 = castOrImport(op1, 0);
        Pointer mpfrOp2 = castOrImport(op2, 0);
        Pointer log1 = new MPFRMemory(getPrecision());
        Pointer log2 = new MPFRMemory(getPrecision());        
        MPFR.mpfr_log(log1, mpfrOp1, MPFR.MPFR_RNDN);
        MPFR.mpfr_log(log2, mpfrOp2, MPFR.MPFR_RNDN);
        MPFR.mpfr_div(getMpfr(), log1, log2, MPFR.MPFR_RNDN);
    }
    
    public void log(Value op) throws EPMCException {
        assert !isImmutable();
        assert op != null;
        Pointer mpfrOp1 = castOrImport(op, 0);
        MPFR.mpfr_log(getMpfr(), mpfrOp1, MPFR.MPFR_RNDN);
    }

    @Override
    public void exp(Value op) {
        assert !isImmutable();
        assert op != null;
        Pointer mpfrOp = castOrImport(op, 0);
        MPFR.mpfr_exp(getMpfr(), mpfrOp, MPFR.MPFR_RNDN);
    }

    @Override
    public void pow(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        Pointer mpfrOp1 = castOrImport(op1, 0);
        Pointer mpfrOp2 = castOrImport(op2, 1);
        MPFR.mpfr_pow(getMpfr(), mpfrOp1, mpfrOp2, MPFR.MPFR_RNDN);
    }

    @Override
    public void addInverse(Value op) {
        assert !isImmutable();
        assert op != null;
        Pointer mpfrOp = castOrImport(op, 0);
        MPFR.mpfr_neg(getMpfr(), mpfrOp, MPFR.MPFR_RNDN);
    }

    @Override
    public void divide(Value op1, Value op2) {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        Pointer mpfrOp1 = castOrImport(op1, 0);
        Pointer mpfrOp2 = castOrImport(op2, 1);
        MPFR.mpfr_div(getMpfr(), mpfrOp1, mpfrOp2, MPFR.MPFR_RNDN);
        if (MPFR.mpfr_nan_p(getMpfr()) != 0) {
        	MPFR.mpfr_set_zero(getMpfr(), 0);
        }
    }

    @Override
    public void subtract(Value op1, Value op2) {
        assert !isImmutable() : this;
        assert op1 != null;
        assert op2 != null;
        Pointer mpfrOp1 = castOrImport(op1, 0);
        Pointer mpfrOp2 = castOrImport(op2, 1);
        MPFR.mpfr_sub(getMpfr(), mpfrOp1, mpfrOp2, MPFR.MPFR_RNDN);
    }

    @Override
    public double distance(Value op) throws EPMCException {
        assert op != null;
        if (!getType().canImport(op.getType())) {
        	assert op.getType().canImport(this.getType());
            return op.distance(this);
        }
        Pointer opMpfr = castOrImport(op, 0);
        if (MPFR.mpfr_nan_p(getMpfr()) != 0 && MPFR.mpfr_nan_p(opMpfr) != 0) {
            return 0.0;
        } else {
            if (distanceValue == null) {
                distanceValue = new MPFRMemory(getPrecision());
            }
            MPFR.mpfr_sub(distanceValue, getMpfr(), opMpfr, MPFR.MPFR_RNDN);
            return Math.abs(MPFR.mpfr_get_d(distanceValue, MPFR.MPFR_RNDN));
        }
    }
    
    @Override
    public double norm() throws EPMCException {
    	double value = MPFR.mpfr_get_d(getMpfr(), MPFR.MPFR_RNDN);
    	return Math.abs(value);
    }
    
    @Override
    public boolean isZero() {
    	return MPFR.mpfr_zero_p(getMpfr()) != 0;
    }

    @Override
    public boolean isOne() {
    	return MPFR.mpfr_cmp_si(getMpfr(), new NativeLong(1)) == 0;
    }

    @Override
    public boolean isPosInf() {
    	return MPFR.mpfr_inf_p(getMpfr()) != 0;
    }

    @Override
    public int getInt() {
    	return MPFR.mpfr_get_si(getMpfr(), MPFR.MPFR_RNDN).intValue();
    }

    @Override
    public boolean isLt(Value operand) {
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        return MPFR.mpfr_cmp(getMpfr(), opMpfr) < 0;
    }

    @Override
    public void cos(Value op) {
        assert !isImmutable();
        assert op != null;
        Pointer opMpfr = castOrImport(op, 0);
        MPFR.mpfr_cos(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }
    
    @Override
    public void sin(Value op) {
        assert !isImmutable();
        assert op != null;
        Pointer opMpfr = castOrImport(op, 0);
        MPFR.mpfr_sin(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }
    @Override
    public void tanh(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_tanh(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }

    @Override
    public void cosh(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_cosh(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }
    
    @Override
    public void sinh(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_sinh(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }
    
    @Override
    public void atan(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_atan(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }

    @Override
    public void acos(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_acos(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }

    @Override
    public void asin(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_asin(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }

    @Override
    public void tan(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_tan(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }

    @Override
    public void sqrt(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_sqrt(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }
    
    @Override
    public void abs(Value operand) throws EPMCException {
        assert !isImmutable();
        assert operand != null;
        Pointer opMpfr = castOrImport(operand, 0);
        MPFR.mpfr_abs(getMpfr(), opMpfr, MPFR.MPFR_RNDN);
    }
        
    @Override
    public void max(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        assert operand1 != null;
        assert operand2 != null;
        Pointer opMpfr1 = castOrImport(operand1, 0);
        Pointer opMpfr2 = castOrImport(operand2, 1);
        MPFR.mpfr_max(getMpfr(), opMpfr1, opMpfr2, MPFR.MPFR_RNDN);
    }
    
    @Override
    public void min(Value operand1, Value operand2) throws EPMCException {
        assert !isImmutable();
        assert operand1 != null;
        assert operand2 != null;
        Pointer opMpfr1 = castOrImport(operand1, 0);
        Pointer opMpfr2 = castOrImport(operand2, 1);
        MPFR.mpfr_min(getMpfr(), opMpfr1, opMpfr2, MPFR.MPFR_RNDN);
    }

    @Override
    public double getDouble() {
    	return MPFR.mpfr_get_d(getMpfr(), MPFR.MPFR_RNDN);
    }
    
    private int getPrecision() {
    	return type.getPrecision();
    }
    
    private Pointer castOrImport(Value operand, int number) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (operand instanceof ValueMPFR) {
        	return ((ValueMPFR) operand).getMpfr();
        }
        if (importMPFR[number] == null) {
            importMPFR[number] = new MPFRMemory(getPrecision());
        }
        if (ValueReal.isReal(operand)) {
        	MPFR.mpfr_set_d(importMPFR[number], ValueReal.asReal(operand).getDouble(), MPFR.MPFR_RNDN);
        } else if (ValueInteger.isInteger(operand)) {
        	MPFR.mpfr_set_si(importMPFR[number], new NativeLong(ValueInteger.asInteger(operand).getInt()), MPFR.MPFR_RNDN);
        } else {
            assert false : operand + SPACE + operand.getType();
            return null;
        }
        return importMPFR[number];
    }

    private void readObject(java.io.ObjectInputStream stream)
    		throws IOException, ClassNotFoundException {
    	stream.defaultReadObject();
    	String string = (String) stream.readObject();
    	setField(FIELD_MPFR, new MPFRMemory(type.getPrecision()));
    	setField(FIELD_IMPORT_MPFR, new Pointer[NUM_IMPORT_VALUES]);
        
    	MPFR.mpfr_set_str(getMpfr(), string, DECIMAL_BASE, MPFR.MPFR_RNDN);
    }
    
    private void setField(String name, Object value) {
    	assert name != null;
		try {
	    	Field field = this.getClass().getDeclaredField(name);
	    	boolean accessible = field.isAccessible();
	        field.setAccessible(true);
	        field.set(this, value);
	        field.setAccessible(accessible);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
    }
    
    private void writeObject(java.io.ObjectOutputStream stream)
    		throws IOException {
    	stream.defaultWriteObject();
        Pointer strPoint = new Memory(Pointer.SIZE);
        MPFR.mpfr_asprintf(strPoint, FORMAT_EXACT, mpfr);
        String string = strPoint.getPointer(0).getString(0);
        MPFR.mpfr_free_str(strPoint.getPointer(0));
    	stream.writeObject(string);
    }

	Pointer getMpfr() {
		return mpfr;
	}

	@Override
	public void acosh(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void atanh(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void asinh(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pi() throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}

}
