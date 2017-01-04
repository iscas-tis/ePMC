package epmc.param.value;

import java.util.Arrays;

final class Region {
    private final ValueFunction[] bounds;
    private final int hash;
    
    Region(ValueFunction[] bounds) {
        this.bounds = bounds.clone();
        this.hash = computeHash(this.bounds);
        // TODO remove duplicates
        // TODO sort
    }
    
    private static int computeHash(ValueFunction[] bounds) {
        int hash = Arrays.hashCode(bounds);
        return hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }
}
