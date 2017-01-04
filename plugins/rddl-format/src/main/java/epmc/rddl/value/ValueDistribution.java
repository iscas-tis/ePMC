package epmc.rddl.value;

import epmc.value.Value;
import epmc.value.ValueArray;

public interface ValueDistribution extends Value {
    boolean isFinite();
    
    boolean isInfiniteCountable();
    
    boolean isInfiniteUncountable();
    
    default boolean isInfinite() {
        return isInfiniteCountable() || isInfiniteUncountable();
    }
    
    int getSupportSize();
    
    void getSupportNr(ValueArray support, int number);
    
    void getWeightNr(ValueArray weight, int number);
}
