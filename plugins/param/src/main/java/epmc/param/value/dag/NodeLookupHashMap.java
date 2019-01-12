package epmc.param.value.dag;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public final class NodeLookupHashMap implements NodeLookup {
    public final static String IDENTIFIER = "hash";
    
    public final static class Builder implements NodeLookup.Builder {

        private NodeStore nodeStore;

        @Override
        public Builder setNodeStore(NodeStore nodeStore) {
            this.nodeStore = nodeStore;
            return this;
        }

        @Override
        public NodeLookup build() {
            return new NodeLookupHashMap(this);
        }
        
    }
    
    // TODO optionally externalise nodes list and map
    // TODO try out BDDs for filtering, though unlikely it works nicely
    // TODO add program options for following stuff
    private final static int INVALID = -1;
    private final boolean unify = true;
    private final boolean useHashMap = true;
    private final boolean useBloomFilter = false;
    private final int bloomExpectedInsertions = 1000000;
    private final double bloomError = 0.001;

    private final Long2IntOpenHashMap entriesMap;
    private final Long2IntOpenHashMap specialMap;
    private final BloomFilter<Long> bloomFilter;
    private final NodeStore nodeStore;

    NodeLookupHashMap(Builder builder) {
        assert builder != null;
        assert builder.nodeStore != null;
        NodeStore nodeStore = builder.nodeStore;
        assert nodeStore != null;
        this.nodeStore = nodeStore;
        if (useHashMap) {
            entriesMap = new Long2IntOpenHashMap();
            entriesMap.defaultReturnValue(INVALID);
            specialMap = null;
        } else {
            entriesMap = null;
            specialMap = new Long2IntOpenHashMap();
            specialMap.defaultReturnValue(INVALID);
        }
        if (useBloomFilter) {
            bloomFilter = BloomFilter.create(Funnels.longFunnel(), bloomExpectedInsertions, bloomError);
        } else {
            bloomFilter = null;
        }
    }
    
    @Override
    public int get(OperatorType type, int operandLeft, int operandRight) {
        if (unify) {
            long entry = EntryUtil.makeEntry(type, operandLeft, operandRight);
            return lookupEntry(entry);
        }
        return INVALID;
    }
    
    @Override
    public void put(OperatorType type, int operandLeft, int operandRight, int number) {
        long entry = EntryUtil.makeEntry(type, operandLeft, operandRight);
        if (useHashMap) {
            entriesMap.put(entry, number);
        } else if (EntryUtil.getType(entry).isSpecial()) {
            specialMap.put(entry, number);
        }
        if (useBloomFilter) {
            bloomFilter.put(entry);
        }
    }

    private int lookupEntry(long entry) {
        if (useBloomFilter && !bloomFilter.mightContain(entry)) {
            return INVALID;
        }
        OperatorType type = EntryUtil.getType(entry);
        int operandLeft = EntryUtil.getOperandLeft(entry);
        int operandRight = EntryUtil.getOperandRight(entry);
        if (useHashMap) {
            return entriesMap.get(entry);
        } else {
            int limit = 0;
            if (type.isSpecial()) {
                return specialMap.get(entry);
            } else {
                limit = Math.max(EntryUtil.getOperandLeft(entry),
                        EntryUtil.getOperandRight(entry));
            }
            for (int index = nodeStore.getNumNodes() - 1; index >= limit; index--) {
                OperatorType compareType = nodeStore.getType(index);
                int compareOperandLeft = nodeStore.getOperandLeft(index);
                int compareOperandRight = nodeStore.getOperandRight(index);
                if (type == compareType
                        && operandLeft == compareOperandLeft
                        && operandRight == compareOperandRight) {
                    return index;
                }
            }
            return INVALID;
        }
    }

    @Override
    public void sendStatistics() {
        // TODO
    }
}
