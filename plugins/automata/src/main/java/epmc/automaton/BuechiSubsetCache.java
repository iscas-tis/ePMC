package epmc.automaton;

import java.util.HashMap;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;

public final class BuechiSubsetCache <S extends AutomatonStateBuechiSubset,T> {
    private final class CacheKey implements Cloneable {
        private S state;
        private BitSet guards;

        @Override
        public int hashCode() {
            int hash = 0;
            hash = state.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = guards.hashCode() + (hash << 6) + (hash << 16) - hash;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof BuechiSubsetCache.CacheKey)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            CacheKey other = (BuechiSubsetCache<S,T>.CacheKey) obj;
            return state.equals(other.state) && guards.equals(other.guards);
        }

        @Override
        protected CacheKey clone() {
            CacheKey result = new CacheKey();
            result.state = state;
            result.guards = guards.clone();
            return result;
        }
    }

    public final class CacheValue {
        private final S state;
        private final T labeling;

        CacheValue(S state, T labeling) {
            assert state != null;
            assert labeling != null;
            this.state = state;
            this.labeling = labeling;
        }
        
        public S getState() {
            return state;
        }
        
        public T getLabeling() {
            return labeling;
        }
    }

    private final GraphExplicit automaton;
    private final BitSet guardsValid;
    private final CacheKey cacheKey = new CacheKey();
    private final HashMap<CacheKey,CacheValue> succCache = new HashMap<>();

    public BuechiSubsetCache(Buechi buechi) {
        assert buechi != null;
        automaton = buechi.getGraph();
        guardsValid = UtilBitSet.newBitSetUnbounded();
    }
    
    public CacheValue lookup(S subsetState) {
        int entryNr = 0;
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            boolean stateSet  = subsetState.getStates().get(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(state); succNr++) {
                BuechiTransition trans = labels.getObject(state, succNr);
                guardsValid.set(entryNr, stateSet && trans.guardFulfilled());
                entryNr++;
            }
        }
        cacheKey.state = subsetState;
        cacheKey.guards = guardsValid;
        return succCache.get(cacheKey);
    }

    public void insert(S succState, T succLabel) {
        CacheValue value = new CacheValue(succState, succLabel);
        succCache.put(cacheKey.clone(), value);
    }
}
