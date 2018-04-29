package epmc.imdp.lump;

public final class ClassToNumberIntArray implements ClassToNumber {
    public final static class Builder implements ClassToNumber.Builder {
        private int size;

        @Override
        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        private int getSize() {
            return size;
        }

        @Override
        public ClassToNumber build() {
            assert size >= 0;
            return new ClassToNumberIntArray(this);
        }

    }

    private final int[] array;
    private final int[] mustReset;
    private int numEntries;

    private ClassToNumberIntArray(Builder builder) {
        assert builder != null;
        assert builder.getSize() >= 0;
        int size = builder.getSize();
        array = new int[size];
        for (int entryNr = 0; entryNr < size; entryNr++) {
            array[entryNr] = -1;
        }
        mustReset = new int[size];
        for (int entryNr = 0; entryNr < size; entryNr++) {
            mustReset[entryNr] = -1;
        }
    }

    @Override
    public void reset() {
        for (int entryNr = 0; entryNr < numEntries; entryNr++) {
            int entry = mustReset[entryNr];
            array[entry] = -1;
            mustReset[entryNr] = -1;
        }
        numEntries = 0;
    }

    @Override
    public int get(int classs) {
        return array[classs];
    }

    @Override
    public void set(int classs, int number) {
        if (array[classs] == -1) {
            mustReset[numEntries] = classs;
            numEntries++;
        }
        array[classs] = number;
    }
}
