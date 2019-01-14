package epmc.param.value.dag;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public final class NodeStoreArray implements NodeStore {
    public final static class Builder implements NodeStore.Builder {

        @Override
        public NodeStore build() {
            return new NodeStoreArray(this);
        }
        
    }
    
    public final static String IDENTIFIER = "array";
    
    private final LongArrayList entriesList = new LongArrayList();
    
    private NodeStoreArray(Builder builder) {
        assert builder != null;
    }
    
    @Override
    public int add(OperatorType type, int operandLeft, int operandRight) {
        long entry = EntryUtil.makeEntry(type, operandLeft, operandRight);
        int result = getNumNodes();
        addEntry(entry, result);
        return result;
    }

    @Override
    public boolean assertValidNumber(int number) {
        assert number >= 0 : number;
        assert number < getNumNodes();
        return true;
    }

    @Override
    public OperatorType getType(int number) {
        return EntryUtil.getType(loadEntry(number));
    }

    @Override
    public int getOperandLeft(int number) {
        return EntryUtil.getOperandLeft(loadEntry(number));
    }

    @Override
    public int getOperandRight(int number) {
        return EntryUtil.getOperandRight(loadEntry(number));
    }

    @Override
    public int getNumNodes() {
        return entriesList.size();
    }
    
    private long loadEntry(int number) {
        return entriesList.getLong(number);
    }
    
    private void addEntry(long entry, int result) {
        entriesList.add(entry);
    }
    
    // TODO
    @Override
    public void sendStatistics() {
        // TODO Auto-generated method stub
    }
}
