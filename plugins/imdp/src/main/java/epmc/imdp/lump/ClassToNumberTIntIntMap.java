package epmc.imdp.lump;

import gnu.trove.map.hash.TIntIntHashMap;

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

    private final TIntIntHashMap hash = new TIntIntHashMap(100, 0.5f, -1, -1);

    private ClassToNumberTIntIntMap(Builder builder) {
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
