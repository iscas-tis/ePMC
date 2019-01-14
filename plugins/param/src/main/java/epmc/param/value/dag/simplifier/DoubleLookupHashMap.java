package epmc.param.value.dag.simplifier;

import epmc.options.Options;
import epmc.param.options.OptionsParam;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;

public final class DoubleLookupHashMap implements DoubleLookup {
    public final static String IDENTIFIER = "hashmap";
    
    public final static class Builder implements DoubleLookup.Builder {

        @Override
        public Builder setStore(DoubleStore store) {
            return this;
        }

        @Override
        public DoubleLookup build() {
            return new DoubleLookupHashMap(this);
        }
        
    }

    private final static int INVALID = -1;
    private final int numDigitsRoundOffHash;
    private final Double2IntOpenHashMap evalResultsMapDoubleHash;

    private DoubleLookupHashMap(Builder builder) {
        assert builder != null;
        numDigitsRoundOffHash = Options.get().getInteger(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_CUTOFF_BIN_DIGITS);
        evalResultsMapDoubleHash = new Double2IntOpenHashMap();
        evalResultsMapDoubleHash.defaultReturnValue(INVALID);
    }

    @Override
    public int get(double entry) {
        assert !Double.isNaN(entry);
        return evalResultsMapDoubleHash.get(roundDouble(entry));
    }

    @Override
    public void put(double entry, int number) {
        evalResultsMapDoubleHash.put(roundDouble(entry), number);
    }

    // https://stackoverflow.com/questions/41583249/how-to-round-a-double-float-to-binary-precision
    private double roundDouble(double value) {
        assert !Double.isNaN(value);
        if (Double.isInfinite(value)) {
            return value;
        }
        double factor = (1 | (1 << numDigitsRoundOffHash)) * value;
        assert !Double.isNaN(factor);
        value -= factor;
        assert !Double.isNaN(value);
        value += factor;
        assert !Double.isNaN(value);
        return value;
    }
}
