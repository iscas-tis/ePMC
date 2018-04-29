package epmc.imdp.lump.signature;

import epmc.operator.OperatorEq;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.UtilValue;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

final class ActionSignature implements Cloneable {
    private int size;
    private int[] blocks;
    private ValueInterval[] values;
    private OperatorEvaluator eq;
    private ValueBoolean cmp;
    private OperatorEvaluator set;

    ActionSignature() {
        blocks = new int[0];
        values = new ValueInterval[0];
        eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
        cmp = TypeBoolean.get().newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInterval.get(), TypeInterval.get());
    }

    @Override
    protected ActionSignature clone() {
        ActionSignature clone = new ActionSignature();
        clone.size = this.size;
        clone.blocks = new int[size];
        clone.values = new ValueInterval[size];
        for (int number = 0; number < size; number++) {
            clone.blocks[number] = this.blocks[number];
            clone.values[number] = UtilValue.clone(this.values[number]);
        }
        return clone;
    }

    void setSize(int size) {
        assert size >= 0;
        int oldSize = this.size;
        if (size > oldSize) {
            blocks = new int[size];
            values = new ValueInterval[size];
            for (int number = 0; number < size; number++) {
                values[number] = getTypeInterval().newValue();
            }
        }
        this.size = size;
    }

    int getSize() {
        return size;
    }

    void setBlock(int number, int block) {
        assert number >= 0;
        assert number < size;
        assert block >= 0;
        blocks[number] = block;
    }

    int getBlock(int number) {
        assert number >= 0;
        assert number < size;
        return blocks[number];
    }

    void setValue(int number, ValueInterval value) {
        assert number >= 0;
        assert number < size;
        assert value != null;
        set.apply(values[number], value);
    }

    ValueInterval getValue(int number) {
        assert number >= 0;
        assert number < size;
        return values[number];
    }

    private TypeInterval getTypeInterval() {
        return TypeInterval.get();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ActionSignature)) {
            return false;
        }
        ActionSignature other = (ActionSignature) obj;
        if (this.size != other.size) {
            return false;
        }
        for (int number = 0; number < size; number++) {
            if (this.blocks[number] != other.blocks[number]) {
                return false;
            }
        }
        for (int number = 0; number < size; number++) {
            eq.apply(cmp, this.values[number], other.values[number]);
            if (!cmp.getBoolean()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = size + (hash << 6) + (hash << 16) - hash;
        for (int number = 0; number < size; number++) {
            int block = blocks[number];
            hash = block + (hash << 6) + (hash << 16) - hash;
            ValueInterval entry = values[number];
            double valueLower = entry.getIntervalLower().getDouble();
            long roundedLower = Math.round(valueLower / 1E-6);
            hash = Long.hashCode(roundedLower) + (hash << 6) + (hash << 16) - hash;
            double valueUpper = entry.getIntervalLower().getDouble();
            long roundedUpper = Math.round(valueUpper / 1E-6);
            hash = Long.hashCode(roundedUpper) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int number = 0; number < size; number++) {
            builder.append(blocks[number]);
            builder.append(":");
            builder.append(values[number]);
            builder.append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append("]");
        return builder.toString();
    }
}
