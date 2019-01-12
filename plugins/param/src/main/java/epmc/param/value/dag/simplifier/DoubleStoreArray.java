package epmc.param.value.dag.simplifier;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

public final class DoubleStoreArray implements DoubleStore {
    public final static String IDENTIFIER = "array";
    
    public final static class Builder implements DoubleStore.Builder {

        @Override
        public DoubleStore build() {
            return new DoubleStoreArray(this);
        }
        
    }

    private final DoubleArrayList evalResultsListDouble = new DoubleArrayList();
    
    private DoubleStoreArray(Builder builder) {
        assert builder != null;
    }

    @Override
    public void add(double entry) {
        evalResultsListDouble.add(entry);
    }

    @Override
    public int size() {
        return evalResultsListDouble.size();
    }

    @Override
    public double get(int index) {
        return evalResultsListDouble.getDouble(index);
    }
}
