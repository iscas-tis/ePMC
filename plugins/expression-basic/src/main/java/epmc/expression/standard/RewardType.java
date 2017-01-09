package epmc.expression.standard;

public enum RewardType {
    REACHABILITY("F "),
    STEADYSTATE("S"),
    CUMULATIVE("C<="),
    INSTANTANEOUS("I="),
    DISCOUNTED("D=");
    
    private final String string;

    private RewardType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
    
    public boolean isReachability() {
        return this == REACHABILITY;
    }
    
    public boolean isSteadystate() {
        return this == STEADYSTATE;
    }
    
    public boolean isCumulative() {
        return this == CUMULATIVE;
    }
    
    public boolean isInstantaneous() {
        return this == INSTANTANEOUS;
    }
    
    public boolean isDiscounted() {
        return this == DISCOUNTED;
    }
}
