package epmc.param.value.dag.simplifier;

import gnu.trove.list.array.TDoubleArrayList;

public final class DoubleStoreArray implements DoubleStore {
    public final static String IDENTIFIER = "array";
    
    public final static class Builder implements DoubleStore.Builder {

        @Override
        public DoubleStore build() {
            return new DoubleStoreArray(this);
        }
        
    }

    private final TDoubleArrayList evalResultsListDouble = new TDoubleArrayList();
    
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
        return evalResultsListDouble.get(index);
    }
}
