package epmc.dd;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnumerable;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueEnumerable;
import epmc.value.ValueInteger;

final class VariableDDImpl implements VariableDD {
    private final static String UNDERSCORE = "_";
    private final static String SPACE = " ";
    private final static String NAME = "name";
    private final static String TYPE = "type";
    private final static String LOWER = "lower";
    private final static String UPPER = "upper";
    private final static String COPIES = "copies";
    
    private boolean closed;
    private final TypeEnumerable type;
    private final int copies;
    private final List<List<DD>> ddVariables;
    private final List<DD> valueEncodings;
    private final ContextDD contextDD;
    private final String name;

    VariableDDImpl(ContextDD contextDD, int copies, Type type, String name,
            List<List<DD>> ddVariables)
            throws EPMCException {
        assert contextDD != null;
        assert copies > 0;
        assert type != null;
        assert contextDD.getContextValue() == type.getContext();
        assert TypeBoolean.isBoolean(type) || TypeInteger.isInteger(type) || TypeEnumerable.asEnumerable(type).getNumValues() != -1 : type;
        assert !TypeInteger.isInteger(type) || TypeInteger.isIntegerBothBounded(type) :
            name + SPACE + TypeInteger.asInteger(type).getLowerInt() + SPACE + TypeInteger.asInteger(type).getUpperInt();
        assert name != null;
        
        this.contextDD = contextDD;
        this.copies = copies;
        this.type = TypeEnumerable.asEnumerable(type);
        this.ddVariables = new ArrayList<>(copies);
        this.valueEncodings = new ArrayList<>(copies);
        for (int copy = 0; copy < copies; copy++) {
            valueEncodings.add(null);
        }
        this.name = name;
        
        if (TypeInteger.isInteger(type)) {
            prepareIntegerDDVariables(ddVariables);
        } else if (TypeBoolean.isBoolean(type)) {
            prepareBooleanDDVariables(ddVariables);
        } else {
            prepareGeneralDDVariables(ddVariables);
        }
    }

    VariableDDImpl(ContextDD contextDD, int copies, Type type, String name)
            throws EPMCException {
        this(contextDD, copies, type, name, null);
    }

    private void prepareIntegerDDVariables(List<List<DD>> ddVariables) throws EPMCException {
        final int numValues = getUpper() - getLower() + 1;
        final int numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1);
        for (int copy = 0; copy < copies; copy++) {
            this.ddVariables.add(new ArrayList<>());
        }
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            for (int copy = 0; copy < copies; copy++) {
                if (ddVariables == null) {
                    contextDD.addGroup(contextDD.numVariables(), copies, true);
                    int varNr = contextDD.numVariables();
                    DD dd = contextDD.newVariable();
                    String ddName = name + UNDERSCORE + bitNr + UNDERSCORE + copy;
                    contextDD.setVariableName(varNr, ddName);
                    this.ddVariables.get(copy).add(dd);
                } else {
                    this.ddVariables.get(copy).add(ddVariables.get(copy).get(bitNr).clone());
                }
            }
        }
    }

    private void prepareBooleanDDVariables(List<List<DD>> ddVariables) throws EPMCException {
        contextDD.addGroup(contextDD.numVariables(), copies, true);
        for (int copy = 0; copy < copies; copy++) {
            ArrayList<DD> var = new ArrayList<>(1);
            if (ddVariables == null) {
                int varNr = contextDD.numVariables();
                String ddName = name + UNDERSCORE + copy;
                contextDD.setVariableName(varNr, ddName);
                var.add(contextDD.newVariable());
            } else {
                var.add(ddVariables.get(copy).get(0).clone());
            }
            this.ddVariables.add(var);
        }
    }
    
    private void prepareGeneralDDVariables(List<List<DD>> ddVariables) throws EPMCException {
        final int numValues = type.getNumValues();
        final int numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1);
        for (int copy = 0; copy < copies; copy++) {
            this.ddVariables.add(new ArrayList<>());
        }
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            for (int copy = 0; copy < copies; copy++) {
                if (ddVariables == null) {
                    contextDD.addGroup(contextDD.numVariables(), copies, true);
                    int varNr = contextDD.numVariables();
                    DD dd = contextDD.newVariable();
                    String ddName = name + UNDERSCORE + bitNr + UNDERSCORE + copy;
                    contextDD.setVariableName(varNr, ddName);
                    this.ddVariables.get(copy).add(dd);
                } else {
                    this.ddVariables.get(copy).add(ddVariables.get(copy).get(bitNr));
                }
            }
        }
    }

    private DD computeValueEncoding(int copy) throws EPMCException {
        if (TypeInteger.isInteger(type)) {
            return computeValueEncodingInteger(copy);
        } else if (TypeBoolean.isBoolean(type)) {
            return computeValueEncodingBoolean(copy);
        } else {
            return computeValueEncodingGeneral(copy);
        }
    }

    private DD computeValueEncodingInteger(int copy) throws EPMCException {
        int numValues = getUpper() - getLower() + 1;
        DD encoding = contextDD.newConstant(getLower());
        int bitValue = 1;
        final int numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1);
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            DD bitVar = ddVariables.get(copy).get(bitNr).clone();
            DD valueDD = contextDD.newConstant(bitValue);
            DD zeroDD = contextDD.newConstant(0);
            DD bitEnc = bitVar.iteWith(valueDD, zeroDD);
            encoding = encoding.addWith(bitEnc);
            bitValue <<= 1;
        }
        return encoding;
    }

    private DD computeValueEncodingBoolean(int copy) {
        return ddVariables.get(copy).get(0).clone();
    }

    private DD computeValueEncodingGeneral(int copy) throws EPMCException {
        // TODO fix
        int numValues = type.getNumValues();
        // TODO value should actually be 'invalid'
        ValueEnumerable constant = type.newValue();
        constant.setValueNumber(type.getNumValues() - 1);
        DD encoding = contextDD.newConstant(constant);
        final int numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numValues - 1);
        for (int valueNr = 0; valueNr < numValues; valueNr++) {
            DD currentEncoding = contextDD.newConstant(true);
            int bitValue = 1;
            for (int bitNr = 0; bitNr < numBits; bitNr++) {
                DD bitVar = ddVariables.get(copy).get(bitNr).clone();
                DD literal = (valueNr & bitValue) != 0 ? bitVar.clone() : bitVar.not();
                currentEncoding = currentEncoding.andWith(literal);
                bitValue <<= 1;
            }
            constant = type.newValue();
            constant.setValueNumber(valueNr);
            DD value = contextDD.newConstant(constant);
            encoding = currentEncoding.iteWith(value, encoding);
        }
        return encoding;
    }

    @Override
    public List<DD> getDDVariables(int copy) {
        assert alive();
        assert copy >= 0;
        assert copy < ddVariables.size();
        return ddVariables.get(copy);
    }
    
    @Override
    public DD getValueEncoding(int copy) throws EPMCException {
        assert alive();
        assert copy >= 0;
        assert copy < valueEncodings.size();
        if (valueEncodings.get(copy) == null) {
            valueEncodings.set(copy, computeValueEncoding(copy));
        }
        
        return valueEncodings.get(copy);
    }

    @Override
    public int getNumCopies() {
        assert alive();
        return copies;
    }
    
    @Override
    public String toString() {
        assert alive();
        ToStringHelper helper = MoreObjects.toStringHelper(this);
        helper.add(NAME, name);
        helper.add(TYPE, type);
        if (TypeInteger.isInteger(type)) {
            helper.add(LOWER, TypeInteger.asInteger(type).getLowerInt());
            helper.add(UPPER, TypeInteger.asInteger(type).getUpperInt());
        }
        helper.add(COPIES, copies);
        return helper.toString();
    }
    
    @Override
    public ContextDD getContext() {
        assert alive();
        return contextDD;
    }
    
    @Override
    public void close() {
        if (!alive()) {
            return;
        }
        closed = true;
        for (List<DD> copy : ddVariables) {
            for (DD dd : copy) {
                dd.dispose();
            }
        }
        for (DD dd : valueEncodings) {
            if (dd != null) {
                dd.dispose();
            }
        }
    }
    
    @Override
    public boolean alive() {
        return !closed && contextDD.alive();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public DD newIntValue(int copy, int value) throws EPMCException {
        assert alive();
        assert copy >= 0;
        assert copy < getNumCopies();
        assert isInteger();
        assert value >= TypeInteger.asInteger(getType()).getLowerInt();
        assert value <= TypeInteger.asInteger(getType()).getUpperInt();
        
        value -= getLower();
        DD dd = getContext().newConstant(true);
        int bit = 1;
        for (DD bitVar : getDDVariables(copy)) {
            DD oldDD = dd;
            DD bitVarNot = bitVar.not();
            dd = dd.and((value & bit) != 0 ? bitVar : bitVarNot);
            oldDD.dispose();
            bitVarNot.dispose();
            bit <<= 1;
        }
        return dd;
    }

    @Override
    public DD newVariableValue(int copy, Value value) throws EPMCException {
        assert copy >= 0;
        assert copy < copies;
        assert value != null;
        assert type.canImport(value.getType());
        value = UtilValue.clone(value);
        if (TypeInteger.isInteger(type)) {
            return newIntValue(copy, ValueInteger.asInteger(value).getInt());
        }
 
        int valueNr = ValueEnumerable.asEnumerable(value).getValueNumber();
        assert valueNr >= 0;
        DD dd = getContext().newConstant(true);
        int bit = 1;
        for (DD bitVar : getDDVariables(copy)) {
            DD oldDD = dd;
            DD bitVarNot = bitVar.not();
            dd = dd.and((valueNr & bit) != 0 ? bitVar : bitVarNot);
            oldDD.dispose();
            bitVarNot.dispose();
            bit <<= 1;
        }
        return dd;
    }
}
