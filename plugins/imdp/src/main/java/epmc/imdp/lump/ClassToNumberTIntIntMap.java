package epmc.imdp.lump;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class ClassToNumberTIntIntMap implements ClassToNumber {
    public final static class Builder implements ClassToNumber.Builder {
        @Override
        public Builder setSize(int size) {
            return this;
        }

        @Override
        public ClassToNumber build() {
            return new ClassToNumberTIntIntMap(this);
        }

    }

    private final Int2IntOpenHashMap hash = new Int2IntOpenHashMap();

    private ClassToNumberTIntIntMap(Builder builder) {
        hash.defaultReturnValue(-1);
        assert builder != null;
    }

    @Override
    public void reset() {
        hash.clear();
    }

    @Override
    public int get(int classs) {
        return hash.get(classs);
    }

    @Override
    public void set(int classs, int number) {
        hash.put(classs, number);
    }
}
