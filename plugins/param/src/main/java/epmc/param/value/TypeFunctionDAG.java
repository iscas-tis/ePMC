package epmc.param.value;

import gnu.trove.map.hash.THashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorDivide;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeArrayReal;
import epmc.value.TypeExact;
import epmc.value.TypeInteger;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;

final class TypeFunctionDAG implements TypeFunction, TypeNumBitsKnown {
    private final static class ApplyEntry {
        private final Operator operator;
        private final Entry[] entries;
        
        ApplyEntry(Operator operator, Entry... entries) {
            assert operator != null;
            assert entries != null;
            for (Entry entry : entries) {
                assert entry != null;
            }
            this.operator = operator;
            this.entries = entries.clone();
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(operator);
            builder.append("(");
            for (int i = 0; i < entries.length; i++) {
                builder.append(entries[i]);
                if (i < entries.length - 1) {
                    builder.append(",");
                }
            }
            builder.append(")");
            return builder.toString();
        }
    }
    
    private final class Entry implements Comparable<Entry>, Serializable {
        private static final long serialVersionUID = 1L;
        private final Value constant;
        private final int variable;
        private final Operator operator;
        private final Entry[] operands;
        private final int hash;
        private int number;
        
        private Entry(Value constant, int variable, Operator operator, Entry[] operands) {
            this.constant = UtilValue.clone(constant);
            this.variable = variable;
            this.operator = operator;
            if (operands == null) {
                this.operands = null;
            } else {
                this.operands = operands.clone();
            }
            this.hash = computeEntryHashStructure(constant, variable, operator, operands);
        }
        
        Entry(Value value) {
            this(value, -1, null, null);
            assert value != null;
        }

        Entry(int variable) {
            this(null, variable, null, null);
            assert variable >= 0;
            assert variable < parameterList.size();
        }

        Entry(Operator operator, Entry... operands) {
            this(null, -1, operator, operands);
            assert operator != null;
            assert operands != null;
            for (Entry operand : operands) {
                assert operand != null;
            }
        }

        public int getNumber() {
            return number;
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            Entry other = (Entry) obj;
            if (!valueEquals) {
                return equalsStruct(other);
            }
            return false;
        }

        private boolean equalsStruct(Entry other) {
            if ((this.constant == null) != (other.constant == null)) {
                return false;
            }
            if (this.constant != null && !this.constant.equals(other.constant)) {
                return false;
            }
            if (this.variable != other.variable) {
                return false;
            }
            if (this.operator != other.operator) {
                return false;
            }
            if (this.operands.length != other.operands.length) {
                return false;
            }
            for (int i = 0; i < operands.length; i++) {
                if (this.operands[i] != other.operands[i]) {
                    return false;
                }
            }
            return true;
        }
        
        boolean isConstant() {
            return constant != null;
        }
        
        boolean isVariable() {
            return variable >= 0;
        }
        
        boolean isOperator() {
            return operator != null;
        }
        
        public Value getConstant() {
            assert constant != null;
            return constant;
        }
        
        public int getVariable() {
            assert variable >= 0;
            return variable;
        }
        
        public Operator getOperator() {
            assert operator != null;
            return operator;
        }
        
        @Override
        public String toString() {
            if (isConstant()) {
                return constant.toString();
            } else if (isVariable()) {
                return parameterList.get(variable).toString();
            } else if (isOperator()) {
                String[] operandStrings = new String[operands.length];
                for (int i = 0; i < operands.length; i++) {
                    operandStrings[i] = operands[i].toString();
                }
                if (operator.getIdentifier().equals(OperatorAdd.IDENTIFIER) || operator.getIdentifier().equals(OperatorMultiply.IDENTIFIER)
                        || operator.getIdentifier().equals(OperatorSubtract.IDENTIFIER)) {
                    return "(" + operandStrings[0] + ")" + operator
                            + "(" + operandStrings[1] + ")";
                } else if (operator.getIdentifier().equals(OperatorKleene.IDENTIFIER)) {
                    return "(" + operandStrings[0] + ")*";
                } else {
                    return operator +  "(" + operandStrings[0] + ")";
                }
            } else {
                assert false;
                return null;
            }
        }

        @Override
        public int compareTo(Entry other) {
            assert other != null;
            if (!valueEquals) {
                return compareToStruct(other);
            } else {
                return 0;
            }
        }

        private int compareToStruct(Entry other) {
            if (constant != null && other.constant == null) {
                return -1;
            } else if (constant != null) {
                return constant.compareTo(other.constant);
            } if (variable >= 0 && other.variable == -1) {
                return -1;
            } else if (variable >= 0) {
                if (variable < other.variable) {
                    return -1;
                } else if (variable > other.variable) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                assert operator != null;
                assert other.operator != null;
                if (getContext().getOperatorNumber(operator) < getContext().getOperatorNumber(other.operator)) {
                    return -1;
                } else if (getContext().getOperatorNumber(operator) > getContext().getOperatorNumber(other.operator)) {
                    return 1;
                }
                if (operands.length < other.operands.length) {
                    return -1;
                } else if (operands.length > other.operands.length) {
                    return 1;
                }
                for (int i = 0; i < operands.length; i++) {
                    int compare = operands[i].compareTo(other.operands[i]);
                    if (compare < 0) {
                        return -1;
                    } else if (compare > 0) {
                        return 1;
                    }
                }                
                return 0;
            }
        }
    }
    
    private static final long serialVersionUID = 1L;
    private final boolean valueEquals;
    private final Map<Object,Entry> parameters = new THashMap<>();
    private final List<Object> parameterList = new ArrayList<>();
    private final Map<Entry,Entry> dag;
    private Point currentPoint;
    private final List<Entry> entries = new ArrayList<>();
    private final Map<Point,List<Value>> evaluateEntries = new THashMap<>();
    private final Random random;
    private final List<List<Value>> checkPoints;
    private final ContextValuePARAM context;
    private final int numRandomBits = 50;
    private final int numCheckPoints = 10;
    private final int zeroEntry;
    private final int oneEntry;
    private final int posInfEntry;
    private final ValueFunctionDAG zeroValue;
    private final ValueFunctionDAG oneValue;
    private final TypeReal typeReal;
    
    private static int computeEntryHashStructure(Value value,
            int variable, Operator operator, Entry[] operands) {
        int hash = 0;
        if (value != null) {
            hash = value.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = variable + (hash << 6) + (hash << 16) - hash;
        if (operator != null) {
            hash = operator.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        if (operands != null) {
            hash = Arrays.hashCode(operands) + (hash << 6) + (hash << 16) - hash;
        }
        
        return hash;
    }

    TypeFunctionDAG(ContextValuePARAM context) {
        this.context = context;
        this.typeReal = TypeReal.get(context.getContextValue());
        boolean valueEquals = true;
        boolean exactReals = TypeExact.isExact(typeReal);
        if (exactReals) {
            this.dag = new THashMap<>();
        } else {
            this.dag = new TreeMap<>();
        }
        this.valueEquals = valueEquals;
        this.random = new Random();
        if (valueEquals) {
            this.checkPoints = computeCheckPoints(numCheckPoints);
        } else {
            this.checkPoints = null;
        }
        this.zeroEntry = newEntry(typeReal.getZero());
        this.oneEntry = newEntry(typeReal.getOne());
        this.posInfEntry = newEntry(typeReal.getPosInf());
        this.zeroValue = newValue();
        this.zeroValue.setEntry(zeroEntry);
        this.oneValue = newValue();
        this.oneValue.set(oneEntry);
    }

    private static List<List<Value>> computeCheckPoints(int numCheckPoints) {
        List<List<Value>> checkPoints = new ArrayList<>();
        for (int c = 0; c < numCheckPoints; c++) {
            List<Value> checkPoint = new ArrayList<>();
            checkPoints.add(checkPoint);
        }
        return checkPoints;
    }

    /*
    private Entry addParameter(Object parameter) throws EPMCException {
        assert parameter != null;
        Entry entry = parameters.get(parameter);
        if (entry == null) {
            assert !parameterList.contains(parameter);
            parameterList.add(parameter);
            if (valueEquals) {
                for (List<Value> point : checkPoints) {
                    BigInteger bigInt = new BigInteger(numRandomBits, random);
                    Value value = typeReal.newValue(bigInt.toString());
                    Value divisor = typeReal.newValue();
                    divisor.pow(typeReal.newValue(2), typeReal.newValue(numRandomBits));
                    point.add(value);
                }
            }
            entry = makeUnique(new Entry(parameterList.size() - 1));
            parameters.put(parameter, entry);
        }
        return entry;
    }
    */
    
    private Entry makeUnique(Entry entry) {
        assert entry != null;
        Entry result = dag.get(entry);
        if (result == null) {
            result = entry;
            result.number = dag.size();
            dag.put(result, result);
            entries.add(result);
            for (List<Value> ent : evaluateEntries.values()) {
                ent.add(null);
            }
        }
        return result;
    }
    
    @Override
    public ValueFunctionDAG newValue() {
        return new ValueFunctionDAG(this, -1);
    }    
        
    int apply(Operator operator, int[] numbers) throws EPMCException {
        Entry[] entries = new Entry[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            entries[i] = this.entries.get(i);
        }
        return apply(operator, entries).getNumber();
    }

    Entry apply(Operator operator, Entry... entries) throws EPMCException {
        switch (operator.getIdentifier()) {
        case OperatorAdd.IDENTIFIER: case OperatorSubtract.IDENTIFIER: case OperatorDivide.IDENTIFIER: case OperatorMultiply.IDENTIFIER:
        case OperatorMultiplyInverse.IDENTIFIER: case OperatorAddInverse.IDENTIFIER: case OperatorKleene.IDENTIFIER:
            Entry newEntry = makeUnique(new Entry(operator, entries));
            return newEntry;
        default:
        	return null;
//            return addParameter(new ApplyEntry(operator, entries));
        }
    }
    
    int newEntry(Value value) {
        assert value != null;
        assert value.getType().getContext() == getContext();
        Entry entry = makeUnique(new Entry(value));
        return entry.getNumber();
    }
    
    int newEntry(int value) {
        Value intValue = UtilValue.newValue(TypeInteger.get(getContext()), value);
        Entry entry = makeUnique(new Entry(intValue));
        return entry.getNumber();
    }
    
    @Override
    public ValueFunctionDAG getOne() {
        return oneValue;
    }
    
    @Override
    public ValueFunctionDAG getZero() {
        return zeroValue;
    }
    
    int getOneEntry() {
        return oneEntry;
    }
    
    int getZeroEntry() {
        return zeroEntry;
    }
    
    public int getPosInfEntry() {
        return posInfEntry;
    }

    Entry getEntry(int number) {
        assert number >= 0;
        assert number < entries.size();
        return entries.get(number);
    }

    public String entryToString(int number) {
        assert number >= -1;
        assert number < entries.size();
        if (number == -1) {
            return "invalid";
        } else {
            return entries.get(number).toString();
        }
    }

    @Override
    public int getNumBits() {
        return Integer.SIZE;
    }

    void evaluate(Value result, int entryNumber, Point point) {
        assert result != null;
        assert entryNumber >= 0;
        assert entryNumber < entries.size();
        assert point != null;
        List<Value> evEntries = evaluateEntries.get(point);
        if (evEntries != null) {
            Value value = evEntries.get(entryNumber);
            if (value == null) {
                
                // TODO
            }
            result.set(value);
        } else {
            Entry entry = entries.get(entryNumber);
            evaluate(result, entry, point);
        }
    }

    private void evaluate(Value result, Entry entry, Point point) {
        if (entry.isConstant()) {
            result.set(entry.getConstant());
        } else if (entry.isVariable()) {
            point.getContent().get(result, entry.getVariable());
        } else if (entry.isOperator()) {
            
        }
        // TODO Auto-generated method stub
        
    }

    @Override
    public ContextValue getContext() {
        return context.getContextValue();
    }
    
    @Override
	public ContextValuePARAM getContextPARAM() {
    	return this.context;
    }

	@Override
	public boolean isSupportOperator(String identifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ValueReal getUnderflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueReal getOverflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeArrayReal getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean canImport(Type type) {
        assert type != null;
        if (this == type) {
            return true;
        }
        return false;
	}
}
