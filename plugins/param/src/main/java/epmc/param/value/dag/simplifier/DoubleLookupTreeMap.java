package epmc.param.value.dag.simplifier;

import java.util.Comparator;
import java.util.TreeMap;

import epmc.options.Options;
import epmc.param.options.OptionsParam;

public final class DoubleLookupTreeMap implements DoubleLookup {
    public final static String IDENTIFIER = "treemap";
    
    public final static class Builder implements DoubleLookup.Builder {
        
        @Override
        public Builder setStore(DoubleStore store) {
            return this;
        }

        @Override
        public DoubleLookup build() {
            return new DoubleLookupTreeMap(this);
        }
        
    }
    
    private final static class CmpDouble implements Comparator<Double> {
        
        @Override
        public int compare(Double o1, Double o2) {
            if (o1 ==  o2) {
                return 0;
            } else {
                return Double.compare(o1, o2);
            }
        }
    }

    private final static int INVALID = -1;
    private final int numDigitsRoundOffHash;
    private final TreeMap<Double, Integer> evalResultsMapDoubleTree;

    private DoubleLookupTreeMap(Builder builder) {
        assert builder != null;
        numDigitsRoundOffHash = Options.get().getInteger(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_CUTOFF_BIN_DIGITS);
        evalResultsMapDoubleTree = new TreeMap<>(new CmpDouble());
    }

    @Override
    public int get(double entry) {
        Integer i = evalResultsMapDoubleTree.get(roundDouble(entry));
        return i == null ? INVALID : i;                
    }

    @Override
    public void put(double entry, int number) {
        evalResultsMapDoubleTree.put(roundDouble(entry), number);
    }

    // https://stackoverflow.com/questions/41583249/how-to-round-a-double-float-to-binary-precision
    private double roundDouble(double value) {
        double factor = (1 | (1 << numDigitsRoundOffHash)) * value;
        value -= factor;
        value += factor;
        return value;
    }
}
