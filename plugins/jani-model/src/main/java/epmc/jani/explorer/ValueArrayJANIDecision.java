package epmc.jani.explorer;

import epmc.util.BitStream;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueBitStoreable;

public final class ValueArrayJANIDecision implements ValueArray {
    private final class DecisionBitStream implements BitStream {

        @Override
        public boolean read() {
            int bitNr = bitIndex % Long.SIZE;
            int entryNr = bitIndex / Long.SIZE;
            boolean value = (content[entryNr] & (1L << bitNr)) != 0L;
            bitIndex++;
            return value;
        }

        @Override
        public void write(boolean value) {
            int bitNr = bitIndex % Long.SIZE;
            int entryNr = bitIndex / Long.SIZE;
            if (value) {
                content[entryNr] |= 1L << bitNr;
            }
            bitIndex++;
        }
    }

    private final TypeArrayJANIDecisionType type;
    private final int bitsPerEntry;
    private final DecisionBitStream stream = new DecisionBitStream();
    private long content[] = new long[0];
    private int bitIndex;
    private int size;

    public ValueArrayJANIDecision(TypeArrayJANIDecisionType type) {
        assert type != null;
        this.type = type;
        bitsPerEntry = type.getEntryType().getTotalNumBits();
    }

    @Override
    public TypeArray getType() {
        return type;
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value instanceof ValueDecision;
        assert index >= 0;
        assert index < size();
        ValueDecision valueJaniDecision = (ValueDecision) value;
        Value[] values = valueJaniDecision.getValues();
        bitIndex = index * bitsPerEntry;
        for (Value entry : values) {
            ValueBitStoreable.as(entry).read(stream);
        }
    }

    @Override
    public void set(Value value, int index) {
        ValueDecision valueJaniDecision = (ValueDecision) value;
        Value[] values = valueJaniDecision.getValues();
        bitIndex = index * bitsPerEntry;
        for (Value entry : values) {
            ValueBitStoreable.as(entry).write(stream);
        }
    }

    @Override
    public void setSize(int size) {
        int totalNumBits = size * bitsPerEntry;
        int numEntries = totalNumBits / Long.SIZE + (totalNumBits % Long.SIZE == 0 ? 0 : 1);
        content = new long[numEntries];
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }
}
