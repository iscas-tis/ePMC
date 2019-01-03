package epmc.qmc.value;

import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueRange;
import epmc.value.ValueReal;
import epmc.value.ValueSetString;

public final class ValueComplex implements ValueAlgebra, ValueRange, ValueSetString {
    public static boolean is(Value value) {
        return value instanceof ValueComplex;
    }

    public static ValueComplex as(Value value) {
        if (is(value)) {
            return (ValueComplex) value;
        } else {
            return null;
        }
    }
    private final ValueReal real;
    private final ValueReal imag;
    private final TypeComplex type;
    private boolean immutable;

    ValueComplex(TypeComplex type) {
        this.type = type;
        real = type.getTypeReal().newValue();
        imag = type.getTypeReal().newValue();
    }

    public void setImmutable() {
        this.immutable = true;
    }

    TypeReal getTypeReal() {
        return getType().getTypeReal();
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
    public void set(int op) {
        assert !isImmutable();
        getRealPart().set(op);
        getImagPart().set(0);
    }

    @Override
    public boolean checkRange() {
        return ValueRange.checkRange(real) && ValueRange.checkRange(imag);
    }

    @Override
    public TypeComplex getType() {
        return type;
    }

    public boolean isImmutable() {
        return immutable;
    }    

    @Override
    public void set(String string) {
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
        ValueSetString.as(this.real).set(parts[0]);
        ValueSetString.as(this.imag).set(parts[1]);
    }

    public static ValueAlgebra getReal(Value operand) {
        if (is(operand)) {
            return ValueComplex.as(operand).getRealPart();
        } else {
            return ValueAlgebra.as(operand);
        }
    }

    public static ValueAlgebra getImag(Value operand) {
        if (is(operand)) {
            return ValueComplex.as(operand).getImagPart();
        } else {
            return UtilValue.newValue(ValueAlgebra.as(operand).getType(), 0);
        }
    }
}
