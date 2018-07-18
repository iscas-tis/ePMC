package epmc.param.value.dag.simplifier;

import epmc.options.Options;
import epmc.param.options.OptionsParam;
import gnu.trove.map.hash.TDoubleIntHashMap;

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
    private final TDoubleIntHashMap evalResultsMapDoubleHash;

    private DoubleLookupHashMap(Builder builder) {
        assert builder != null;
        numDigitsRoundOffHash = Options.get().getInteger(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_CUTOFF_BIN_DIGITS);
        evalResultsMapDoubleHash = new TDoubleIntHashMap(100, 0.5f, Double.NaN, INVALID);
    }

    @Override
    public int get(double entry) {
        return evalResultsMapDoubleHash.get(roundDouble(entry));
    }

    @Override
    public void put(double entry, int number) {
        evalResultsMapDoubleHash.put(roundDouble(entry), number);
    }

    // https://stackoverflow.com/questions/41583249/how-to-round-a-double-float-to-binary-precision
    private double roundDouble(double value) {
        double factor = (1 | (1 << numDigitsRoundOffHash)) * value;
        value -= factor;
        value += factor;
        return value;
    }
}
