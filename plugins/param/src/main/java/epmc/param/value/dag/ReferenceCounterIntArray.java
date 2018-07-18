package epmc.param.value.dag;

import gnu.trove.list.array.TIntArrayList;

public final class ReferenceCounterIntArray implements ReferenceCounter {
    private TIntArrayList counters = new TIntArrayList();
    
    @Override
    public void addNode() {
        counters.add(0);
    }
    
    @Override
    public boolean isAlive(int node) {
        assert node >= 0 : node;
        assert node < counters.size();
        return counters.get(node) > 0;
    }

    @Override
    public void incRef(int node) {
        assert node >= 0 : node;
        assert node < counters.size();
        if (counters.get(node) == Integer.MAX_VALUE) {
            return;
        }
        counters.set(node, counters.get(node) + 1);
    }

    @Override
    public void decRef(int node) {
        assert node >= 0 : node;
        assert node < counters.size();
        assert counters.get(node) > 0;
        if (counters.get(node) == Integer.MAX_VALUE) {
            return;
        }
        counters.set(node, counters.get(node) - 1);
    }
}
