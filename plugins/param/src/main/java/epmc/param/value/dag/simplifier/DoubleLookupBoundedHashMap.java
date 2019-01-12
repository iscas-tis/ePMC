package epmc.param.value.dag.simplifier;

import java.util.Arrays;

import epmc.options.Options;
import epmc.param.options.OptionsParam;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;

public final class DoubleLookupBoundedHashMap implements DoubleLookup {
    public final static String IDENTIFIER = "bounded-hashmap";
    
    public final static class Builder implements DoubleLookup.Builder {

        @Override
        public Builder setStore(DoubleStore store) {
            return this;
        }

        @Override
        public DoubleLookup build() {
            return new DoubleLookupBoundedHashMap(this);
        }
        
    }

    private final static int INVALID = -1;
    private final int numDigitsRoundOffHash;
    private final int lookback = 4096 * 2;
    private final double[] ring;
    private int ringIndex = 0;
    private final Double2IntOpenHashMap evalResultsMapDoubleHash;

    private DoubleLookupBoundedHashMap(Builder builder) {
        assert builder != null;
        numDigitsRoundOffHash = Options.get().getInteger(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_CUTOFF_BIN_DIGITS);
        ring = new double[lookback];
        Arrays.fill(ring, Double.POSITIVE_INFINITY);
        evalResultsMapDoubleHash = new Double2IntOpenHashMap();
        evalResultsMapDoubleHash.defaultReturnValue(INVALID);
    }

    @Override
    public int get(double entry) {
        return evalResultsMapDoubleHash.get(roundDouble(entry));
    }

    @Override
    public void put(double entry, int number) {
        double roundedEntry = roundDouble(entry);
        evalResultsMapDoubleHash.remove(ring[ringIndex]);
        ring[ringIndex] = roundedEntry;
        ringIndex++;
        ringIndex %= lookback;
        evalResultsMapDoubleHash.put(roundedEntry, number);
    }

    // https://stackoverflow.com/questions/41583249/how-to-round-a-double-float-to-binary-precision
    private double roundDouble(double value) {
        assert !Double.isNaN(value); // TODO user exception
        if (Double.isInfinite(value)) {
            return value;
        }
        double factor = (1 | (1 << numDigitsRoundOffHash)) * value;
        value -= factor;
        value += factor;
        assert !Double.isNaN(value); // TODO user exception
        return value;
    }
}
