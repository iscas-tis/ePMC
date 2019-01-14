package epmc.value;

import it.unimi.dsi.fastutil.Hash;

public final class StrategyIdentity implements Hash.Strategy<Object> {
    private final static StrategyIdentity INSTANCE = new StrategyIdentity();
    
    @Override
    public boolean equals(Object arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int hashCode(Object arg0) {
        return System.identityHashCode(arg0);
    }
    
    public static StrategyIdentity get() {
        return INSTANCE;
    }
    
    private StrategyIdentity() {
    }
}
