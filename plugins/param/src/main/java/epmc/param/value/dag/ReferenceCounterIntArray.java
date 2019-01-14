package epmc.param.value.dag;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ReferenceCounterIntArray implements ReferenceCounter {
    private IntArrayList counters = new IntArrayList();
    
    @Override
    public void addNode() {
        counters.add(0);
    }
    
    @Override
    public boolean isAlive(int node) {
        assert node >= 0 : node;
        assert node < counters.size();
        return counters.getInt(node) > 0;
    }

    @Override
    public void incRef(int node) {
        assert node >= 0 : node;
        assert node < counters.size();
        if (counters.getInt(node) == Integer.MAX_VALUE) {
            return;
        }
        counters.set(node, counters.getInt(node) + 1);
    }

    @Override
    public void decRef(int node) {
        assert node >= 0 : node;
        assert node < counters.size();
        assert counters.getInt(node) > 0;
        if (counters.getInt(node) == Integer.MAX_VALUE) {
            return;
        }
        counters.set(node, counters.getInt(node) - 1);
    }
}
