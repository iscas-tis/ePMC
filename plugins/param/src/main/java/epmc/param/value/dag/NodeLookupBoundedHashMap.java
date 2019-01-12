package epmc.param.value.dag;

import java.util.Arrays;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public final class NodeLookupBoundedHashMap implements NodeLookup {
    public final static String IDENTIFIER = "bounded-hashmap";
    
    public final static class Builder implements NodeLookup.Builder {

        @Override
        public Builder setNodeStore(NodeStore store) {
            return this;
        }

        @Override
        public NodeLookup build() {
            return new NodeLookupBoundedHashMap(this);
        }
        
    }

    private final static int INVALID = -1;
    private final int lookback = 4096 * 2;
    private final long[] ring;
    private int ringIndex = 0;
    private final Long2IntOpenHashMap evalResultsMapLongHash;

    private NodeLookupBoundedHashMap(Builder builder) {
        assert builder != null;
        ring = new long[lookback];
        Arrays.fill(ring, -1L);
        evalResultsMapLongHash = new Long2IntOpenHashMap();
        evalResultsMapLongHash.defaultReturnValue(INVALID);
    }

    @Override
    public int get(OperatorType type, int operandLeft, int operandRight) {
        long entry = EntryUtil.makeEntry(type, operandLeft, operandRight);
        return evalResultsMapLongHash.get(entry);
    }

    @Override
    public void put(OperatorType type, int operandLeft, int operandRight, int number) {
        long entry = EntryUtil.makeEntry(type, operandLeft, operandRight);
        evalResultsMapLongHash.remove(ring[ringIndex]);
        ring[ringIndex] = entry;
        ringIndex++;
        ringIndex %= lookback;
        evalResultsMapLongHash.put(entry, number);
    }

    @Override
    public void sendStatistics() {
        // TODO Auto-generated method stub
        
    }
}
