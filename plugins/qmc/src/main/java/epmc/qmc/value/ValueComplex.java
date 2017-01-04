package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueRange;
import epmc.value.ValueReal;

public final class ValueComplex implements ValueAlgebra, ValueRange {
	public static boolean isComplex(Value value) {
		return value instanceof ValueComplex;
	}
	
	public static ValueComplex asComplex(Value value) {
		if (isComplex(value)) {
			return (ValueComplex) value;
		} else {
			return null;
		}
	}
	
    private static final long serialVersionUID = 1L;
    private static final int NUM_IMPORT_VALUES = 2;
    private final ValueReal real;
    private final ValueReal imag;
    private final ValueReal accAC;
    private final ValueReal accBD;
    private final ValueReal accAD;
    private final ValueReal accBC;
    private final ValueReal accCC;
    private final ValueReal accDD;
    private final ValueReal accCCDD;
    private final ValueReal accNegD;
    private final ValueReal accACBD;
    private final ValueReal accBCAD;
    private final ValueComplex importComplex[]
            = new ValueComplex[NUM_IMPORT_VALUES];
    private final TypeComplex type;
    private boolean immutable;
    
    ValueComplex(TypeComplex type, ValueReal real, ValueReal imaginary) {
        this.type = type;
        this.real = real;
        this.imag = imaginary;
        this.accAC = getTypeReal().newValue();
        this.accBD = getTypeReal().newValue();
        this.accAD = getTypeReal().newValue();
        this.accBC = getTypeReal().newValue();
        this.accCC = getTypeReal().newValue();
        this.accDD = getTypeReal().newValue();
        this.accCCDD = getTypeReal().newValue();
        this.accNegD = getTypeReal().newValue();
        this.accACBD = getTypeReal().newValue();
        this.accBCAD = getTypeReal().newValue();
    }

    ValueComplex(ValueComplex other) {
        this.type = other.type;
        this.real = UtilValue.clone(other.real);
        this.imag = UtilValue.clone(other.imag);
        this.accAC = getTypeReal().newValue();
        this.accBD = getTypeReal().newValue();
        this.accAD = getTypeReal().newValue();
        this.accBC = getTypeReal().newValue();
        this.accCC = getTypeReal().newValue();
        this.accDD = getTypeReal().newValue();
        this.accCCDD = getTypeReal().newValue();
        this.accNegD = getTypeReal().newValue();
        this.accACBD = getTypeReal().newValue();
        this.accBCAD = getTypeReal().newValue();
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
        real.setImmutable();
        imag.setImmutable();
    }
    
    TypeReal getTypeReal() {
        return getType().getTypeReal();
    }
    
    @Override
    public ValueComplex clone() {
        return new ValueComplex(this);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueComplex)) {
            return false;
        }
        ValueComplex other = (ValueComplex) obj;
        return this.real.equals(other.real)
                && this.imag.equals(other.imag);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = real.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = imag.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        if (imag.toString().equals("0")) {
            return real.toString();
        } else {
            return real + "+" + imag + "i";
        }
    }

    public ValueReal getRealPart() {
        return real;
    }
    
    public ValueReal getImagPart() {
        return imag;
    }
    
    @Override
    public void set(Value op) {
        assert !isImmutable();
        assert op != null;
        ValueComplex opComplex = castOrImport(op, 0);
        getRealPart().set(opComplex.getRealPart());
        getImagPart().set(opComplex.getImagPart());
    }
    
    @Override
    public void add(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        ValueComplex op1Complex = castOrImport(op1, 0);
        ValueComplex op2Complex = castOrImport(op2, 1);
        getRealPart().add(op1Complex.getRealPart(), op2Complex.getRealPart());
        getImagPart().add(op1Complex.getImagPart(), op2Complex.getImagPart());
    }

    @Override
    public void addInverse(Value op) throws EPMCException {
        assert !isImmutable();
        assert op != null;
        ValueComplex opComplex = castOrImport(op, 0);
        getRealPart().addInverse(opComplex.getRealPart());
        getImagPart().addInverse(opComplex.getImagPart());
    }

    @Override
    public void subtract(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        ValueComplex op1Complex = castOrImport(op1, 0);
        ValueComplex op2Complex = castOrImport(op2, 1);
        getRealPart().subtract(op1Complex.getRealPart(), op2Complex.getRealPart());
        getImagPart().subtract(op1Complex.getImagPart(), op2Complex.getImagPart());
    }

    @Override
    public void multiply(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        ValueComplex op1Complex = castOrImport(op1, 0);
        ValueComplex op2Complex = castOrImport(op2, 1);
        accAC.multiply(op1Complex.getRealPart(), op2Complex.getRealPart());
        accBD.multiply(op1Complex.getImagPart(), op2Complex.getImagPart());
        accAD.multiply(op1Complex.getRealPart(), op2Complex.getImagPart());
        accBC.multiply(op1Complex.getImagPart(), op2Complex.getRealPart());
        getRealPart().subtract(accAC, accBD);
        getImagPart().add(accAD, accBC);
    }

    @Override
    public void multInverse(Value op) throws EPMCException {
        assert !isImmutable();
        assert op != null;
        ValueComplex opComplex = castOrImport(op, 0);
        accCC.multiply(opComplex.getRealPart(), opComplex.getRealPart());
        accDD.multiply(opComplex.getImagPart(), opComplex.getImagPart());
        accCCDD.add(accCC, accDD);
        accNegD.addInverse(opComplex.getImagPart());
        getRealPart().divide(opComplex.getRealPart(), accCCDD);
        getImagPart().divide(accNegD, accCCDD);
    }

    @Override
    public void divide(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        ValueComplex op1Complex = castOrImport(op1, 0);
        ValueComplex op2Complex = castOrImport(op2, 1);
        accCC.multiply(op2Complex.getRealPart(), op2Complex.getRealPart());
        accDD.multiply(op2Complex.getImagPart(), op2Complex.getImagPart());
        accCCDD.add(accCC, accDD);
        accAC.multiply(op1Complex.getRealPart(), op2Complex.getRealPart());
        accBD.multiply(op1Complex.getImagPart(), op2Complex.getImagPart());
        accBC.multiply(op1Complex.getImagPart(), op2Complex.getRealPart());
        accAD.multiply(op1Complex.getRealPart(), op2Complex.getImagPart());
        accACBD.add(accAC, accBD);
        accBCAD.subtract(accBC, accAD);
        getRealPart().divide(accACBD, accCCDD);
        getImagPart().divide(accBCAD, accCCDD);
    }

    @Override
    public boolean isZero() {
        return getRealPart().isZero() && getImagPart().isZero();
    }

    @Override
    public boolean isOne() {
        return getRealPart().isOne() && getImagPart().isZero();
    }

    @Override
    public boolean isPosInf() {
        return getRealPart().isPosInf() && getImagPart().isZero();
    }

    @Override
    public double norm() throws EPMCException {
        double realIterNorm = getRealPart().norm();
        double imagIterNorm = getImagPart().norm();
        return Math.max(realIterNorm, imagIterNorm);
    }

    @Override
    public boolean isLt(Value op) throws EPMCException {
        assert op != null;
        ValueComplex opComplex = castOrImport(op, 0);
        accAC.multiply(getRealPart(), getRealPart());
        accAD.multiply(getImagPart(), getImagPart());
        accCCDD.add(accAC, accAD);
        accBC.multiply(opComplex.getRealPart(), opComplex.getRealPart());
        accBD.multiply(opComplex.getImagPart(), opComplex.getImagPart());
        accACBD.add(accBC, accBD);
        return accCCDD.isLt(accACBD);
    }

    @Override
    public void set(int op) {
        assert !isImmutable();
        getRealPart().set(op);
        getImagPart().set(0);
    }
    
    private ValueComplex castOrImport(Value operand, int number) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (operand instanceof ValueComplex) {
            return (ValueComplex) operand;
        } else if (ValueReal.isReal(operand)) {
            if (importComplex[number] == null) {
                importComplex[number] = (ValueComplex) getType().newValue();
            }
            importComplex[number].getRealPart().set(operand);
            importComplex[number].getImagPart().set(getTypeReal().getZero());
            return importComplex[number];
        } else {
            assert false;
            return null;
        }
    }
    
    @Override
    public boolean checkRange() throws EPMCException {
        return ValueRange.checkRange(real) && ValueRange.checkRange(imag);
    }
    
    @Override
    public int compareTo(Value other) {
        ValueComplex otherComplex = castOrImport(other, 0);
        int compareReal = real.compareTo(otherComplex.getRealPart());
        if (compareReal != 0) {
            return compareReal;
        }
        int compareImag = imag.compareTo(otherComplex.getImagPart());
        return compareImag;
    }

    @Override
    public TypeComplex getType() {
        return type;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }    
    
    @Override
    public double distance(Value other) throws EPMCException {
    	if (isComplex(other)) {
    		ValueComplex otherComplex = asComplex(other);
    		return Math.max(getImagPart().distance(otherComplex.getImagPart()),
    				getRealPart().distance(otherComplex.getRealPart()));
    	} else if (ValueAlgebra.isAlgebra(other)) {
    		return Math.max(getImagPart().distance(other), 
    				getRealPart().distance(other));
    	} else {
    		assert false;
    		return Double.NaN;
    	}
    }
    
    @Override
    public void set(String string) throws EPMCException {
        string = string.trim();
        boolean negateReal = false;
        boolean negateImag = false;
        if (string.charAt(0) == '-' && string.length() > 1
                && string.charAt(1) == 'i') {
            negateImag = true;
        } else if (string.charAt(0) == '-') {
            negateReal = true;
        }
        
        negateReal = string.charAt(0) == '-';
        if (string.charAt(0) == '+' || string.charAt(0) == '-') {
            string = string.substring(1);
        }
        String[] parts = null;
        if (string.contains("+")) {
            parts = string.split("\\+");
            parts[1] = parts[1].substring(0, parts[1].length() - 1);
        } else if (string.contains("-")) {
            negateImag = true;
            parts = string.split("-");
            parts[1] = parts[1].substring(0, parts[1].length() - 1);
        } else if (!string.contains("i")){
            parts = new String[2];
            parts[0] = string;
            parts[1] = "0";
        } else if (string.contains("i")){
            parts = new String[2];
            parts[0] = "0";
            parts[1] = string.substring(1);
        } else {
            assert false;
        }
        if (parts[1].equals("")) {
            parts[1] = "1";
        }
        if (negateReal) {
            parts[0] = "-" + parts[0];
        }
        if (negateImag) {
            parts[1] = "-" + parts[1];            
        }        
        this.real.set(parts[0]);
        this.imag.set(parts[1]);
    }

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}
}
