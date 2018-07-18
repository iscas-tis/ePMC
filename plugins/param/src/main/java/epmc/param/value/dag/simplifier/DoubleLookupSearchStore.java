package epmc.param.value.dag.simplifier;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import epmc.options.Options;
import epmc.param.options.OptionsParam;

public final class DoubleLookupSearchStore implements DoubleLookup {
    public final static String IDENTIFIER = "searchstore";
    
    public final static class Builder implements DoubleLookup.Builder {

        private DoubleStore store;

        @Override
        public Builder setStore(DoubleStore store) {
            this.store = store;
            return this;
        }

        @Override
        public DoubleLookup build() {
            return new DoubleLookupSearchStore(this);
        }
        
    }

    private final static int INVALID = -1;
    private final int numDigitsRoundOffHash;
    private final int lookBackLimit;
    // TODO automatically rebuild bloom filter if we get too many elements
    // TODO for smaller lookBackLimit Bloom filter size can be
    // limited if we rebuild periodically
    // add command line options for bloom filter size etc.
    private BloomFilter<Long> currentFilter;
    private BloomFilter<Long> nextFilter;
    private int lookBackCounter;
    private DoubleStore store;
    
    private DoubleLookupSearchStore(Builder builder) {
        assert builder != null;
        assert builder.store != null;
        numDigitsRoundOffHash = Options.get().getInteger(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_CUTOFF_BIN_DIGITS);
        this.store = builder.store;
        lookBackLimit = 6000;
        currentFilter = BloomFilter.create(Funnels.longFunnel(), 2*lookBackLimit, 1E-10);
        nextFilter = BloomFilter.create(Funnels.longFunnel(), 2*lookBackLimit, 1E-10);
    }

    @Override
    public int get(double entry) {
        int resultNode = INVALID;
        double roundedEntry = roundDouble(entry);
        if (!currentFilter.mightContain(Double.doubleToLongBits(roundedEntry))) {
            return INVALID;
        }
        for (int i = store.size() - 1; i >= Math.max(0, store.size() - lookBackLimit); i--) {
            if (roundDouble(store.get(i)) == roundedEntry) {
                resultNode = i;
                break;
            }
        }
        if (resultNode == INVALID) {
            System.out.println("FFF");
        }
        return resultNode;
    }

    @Override
    public void put(double entry, int number) {
        if (lookBackCounter == lookBackLimit / 2) {
            currentFilter = nextFilter;
            nextFilter = BloomFilter.create(Funnels.longFunnel(), 2*lookBackLimit, 1E-10);
            lookBackCounter = 0;
        }
        currentFilter.put(Double.doubleToLongBits(roundDouble(entry)));
        nextFilter.put(Double.doubleToLongBits(roundDouble(entry)));
        lookBackCounter++;
    }

    // https://stackoverflow.com/questions/41583249/how-to-round-a-double-float-to-binary-precision
    private double roundDouble(double value) {
        double factor = (1 | (1 << numDigitsRoundOffHash)) * value;
        value -= factor;
        value += factor;
        return value;
    }
}
