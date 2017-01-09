package epmc.automata.determinisation;

import epmc.util.BitSet;

final class ScheweCacheKey implements Cloneable {
    AutomatonScheweState state;
    BitSet guards;
    
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
        if (!(obj instanceof ScheweCacheKey)) {
            return false;
        }
        ScheweCacheKey other = (ScheweCacheKey) obj;
        if (!state.equals(other.state)) {
        	return false;
        }
        if (!guards.equals(other.guards)) {
        	return false;
        }
        return true;
    }
    
    @Override
    protected ScheweCacheKey clone() {
        ScheweCacheKey result = new ScheweCacheKey();
        result.state = state;
        result.guards = guards.clone();
        return result;
    }
}
